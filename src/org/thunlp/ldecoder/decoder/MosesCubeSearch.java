package org.thunlp.ldecoder.decoder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.thunlp.ldecoder.config.Config;
import org.thunlp.ldecoder.distortion.IDistortionModel;
import org.thunlp.ldecoder.phrasetable.IPhraseTable;
import org.thunlp.ldecoder.phrasetable.MosesPhraseTable;
import org.thunlp.lm.srilm.SRILMWrapper;

public class MosesCubeSearch {
	ArrayList<ArrayList<MosesHypothesis>> stacks; //翻译过程的stack，每个stack中是一个Hyp。
	ArrayList<ArrayList<MosesHypothesis>> recombinedList; //合并列表，stack中每一个经历过合并的hyp对应这里面的一个list，list里存有被该hyp合并后省略掉的hyp 
	MosesPhraseTable phraseTable;
	IDistortionModel distortionModel;
	SRILMWrapper lm;
	MosesTranslationOptionCollector collector;
	
	public MosesCubeSearch(IPhraseTable phraseTable,	IDistortionModel distortionModel,
			SRILMWrapper lm, MosesTranslationOptionCollector collector) {
		this.phraseTable = (MosesPhraseTable)phraseTable;
		this.distortionModel = distortionModel;
		this.lm = lm;
		this.collector = collector;
	}
	
	/**
	 * 实现search算法，生成Search Graph
	 */
	public void search() {
		recombinedList = new ArrayList<ArrayList<MosesHypothesis>>();
		int length = collector.sourceSentenceLength;
		stacks = new ArrayList<ArrayList<MosesHypothesis>>(length+1); //stack的id从0到n，第0个里仅有初始hyp0
		for(int i = 0; i <= length; i++) {
			ArrayList<MosesHypothesis> stacki = new ArrayList<MosesHypothesis>();
			stacks.add(i, stacki);
		}
		//将hyp0加入第0个stack中
		MosesHypothesis hyp0 = new MosesHypothesis(0);
		hyp0.buildFirstHyp(collector);
		stacks.get(0).add(hyp0);
		
		int hypId = 1;
		//从第0到第n-1个stack，通过当前stack中的hyp，再翻译一个短语生成新的hyp，加入对应stack中
		for(int stackId = 0; stackId < length; stackId++) {
			ArrayList<MosesHypothesis> stack = stacks.get(stackId);
			//System.out.println("processing stack "+stackId+" . There are "+stack.size()+" hyps");
			//遍历stack中的hyp，加上一个option来生成新的hyp
			for(int x = 0; x < stack.size() && x < Config.stackSize; x++) {
				MosesHypothesis hyp = stack.get(x);
				//1 根据bitmap，获取可行的option列表
				boolean[] bitmap = hyp.bitmap; //通过bitmap，找出可用的option的range(startIndex, endIndex)
				ArrayList<MosesTranslationOption> optionList = new ArrayList<MosesTranslationOption>();
				int t = hyp.option.endIndex+1;
				//保证OOV的调序是M
				if(collector.translationOptions.get(t*length+t) != null && collector.translationOptions.get(t*length+t).get(0).phrasePair.isOOV) {
					ArrayList<MosesTranslationOption> l = collector.translationOptions.get(t*length+t);
					optionList.addAll(l);
				}
				else {
					//选取可能的options 
					for(int startIndex = Math.max(0, hyp.option.beginIndex-Config.distortionLimit); 
							startIndex < Math.min(length, hyp.option.beginIndex+Config.distortionLimit); startIndex++) {
						int endIndex = startIndex;
						while(endIndex < length && !bitmap[endIndex] && endIndex-startIndex < Config.phraseMaxLength && endIndex < length) {
							ArrayList<MosesTranslationOption> l = collector.translationOptions.get(startIndex*length+endIndex);
							if(l != null) {
								//System.out.println("from "+startIndex+" "+endIndex+" get "+l.size()+" options");
								//System.out.println("such as "+l.get(0).phrasePair.toString());
								optionList.addAll(l);
							}
							endIndex++;
						}
					}
				}
				//2 选取一个option，生成新的hyp

				for(MosesTranslationOption op : optionList) {
					MosesHypothesis newHyp = new MosesHypothesis(hypId++);
					newHyp.buildFromLastHyp(hyp, op, collector);
					ArrayList<MosesHypothesis> nextStack = stacks.get(newHyp.allCoverNum);
					/*
					 * 和stack中已有的hyp进行逐一比较，可以1.确定插入位置，保证排序；2.判断是否可以合并。stack中只保留合并后最优（score+futurescore最大）的结果
					 * 被合并掉的hyp将放置在recombined list中，同时设置hyp的recombinedListId（只要进行了合并，不论是留下来还是被合并
					 * 掉的hyp，都要设置recombinedListId，这样才能回溯的时候找到）
					 * 注：如果在找到插入位置前先找到可以合并，那么就不用插入了；否则设置完recombinedListId后，先把已在stack中要被
					 * 合并掉的hyp从stack中删除，再插入该hyp
					 * */
					int insertIndex = -1;
					for(int i = 0; i < nextStack.size(); i++) {
						MosesHypothesis hypInStack = nextStack.get(i);
						if(hypInStack.score+hypInStack.futureScore < newHyp.score+newHyp.futureScore && insertIndex == -1) {
							insertIndex = i;
						}
						if(hypInStack.canCombine(newHyp)) {
							if(insertIndex == -1) { //不具备insert的资格，那说明newHyp肯定比hypInStack差，直接将newHyp加入recombineList就行
								int recombinedListId = hypInStack.recombinedListId;
								if(recombinedListId >= 0) {
									recombinedList.get(recombinedListId).add(newHyp);
									newHyp.recombinedListId = recombinedListId;
								}
								else {
									recombinedListId = recombinedList.size();
									ArrayList<MosesHypothesis> rlist = new ArrayList<MosesHypothesis>();
									recombinedList.add(rlist);
									
									recombinedList.get(recombinedListId).add(newHyp);
									newHyp.recombinedListId = recombinedListId;
									hypInStack.recombinedListId = recombinedListId;
								}
								insertIndex = -2;
							}
							else {//具备insert资格，将hypInStack加入recombineList，同时将hypInStack从stack中删除
								int recombinedListId = hypInStack.recombinedListId;
								if(recombinedListId >= 0) {
									recombinedList.get(recombinedListId).add(hypInStack);
									newHyp.recombinedListId = recombinedListId;
								}
								else {
									recombinedListId = recombinedList.size();
									ArrayList<MosesHypothesis> rlist = new ArrayList<MosesHypothesis>();
									recombinedList.add(rlist);
									
									recombinedList.get(recombinedListId).add(hypInStack);
									newHyp.recombinedListId = recombinedListId;
									hypInStack.recombinedListId = recombinedListId;
								}
								nextStack.remove(i);
							}
						}
					} //for(int i = 0; i < nextStack.size(); i++)
					
					if(insertIndex == -1)
						insertIndex = nextStack.size();
					if(insertIndex >= 0) {
						nextStack.add(insertIndex, newHyp);
						if(nextStack.size() == Config.stackSize)
							nextStack.remove(nextStack.size()-1);
					}
					
				}
				
			}
		}//遍历stack
	}
	
	/**
	 * 根据search graph得到nbest
	 * @return
	 */
	public ArrayList<String> getNbest(int n) {
		ArrayList<String> nbest = new ArrayList<String>();
		Comparator<Path> OrderIsdn =  new Comparator<Path>(){
			public int compare(Path p1, Path p2) {
				if(p2.score > p1.score)
					return 1;
				else
					return -1;
			}
		};
		
		PriorityQueue<Path> paths = new PriorityQueue<Path>(n, OrderIsdn);
		
		//将最终stack中的hyp生成路径加入
		for(MosesHypothesis hyp : stacks.get(stacks.size()-1)) {
			Path p = new Path();
			MosesHypothesis current = hyp;
			p.score = hyp.score;
			p.scores = new float[hyp.scores.length];
			while(current.lastHyp != null) {
				p.hypPath.add(current);
				p.translation = current.option.phrasePair.targetPhrase+ " " + p.translation;
				p.lastChange = 0;
				for(int i = 0; i < p.scores.length; i++)
					p.scores[i] += current.scores[i];
				current = current.lastHyp;
			}
			String s = "";
			for(int i = 0; i < p.scores.length; i++)
				s += p.scores[i]+" ";
			p.translation += " ||| "+s+"||| "+p.score;
			paths.add(p);
		}
		
		//System.out.println("get nbest");
		while(nbest.size() < n && !paths.isEmpty()) {
			Path bestPath = paths.poll();
			nbest.add(bestPath.translation);
			for(int i = bestPath.lastChange; i < bestPath.hypPath.size(); i++) {
				MosesHypothesis current = bestPath.hypPath.get(i);
				if(current.recombinedListId >= 0 && recombinedList.get(current.recombinedListId).size() > 0) {
					for(MosesHypothesis h : recombinedList.get(current.recombinedListId)) {
						Path newPath = new Path();
						for(int j = 0; j < i; j++) {
							newPath.hypPath.add(bestPath.hypPath.get(j));
						}
						newPath.hypPath.add(h);
						MosesHypothesis hx = h.lastHyp;
						while(hx.lastHyp != null) {
							newPath.hypPath.add(hx);
							hx = hx.lastHyp;
						}
						//newPath.score = bestPath.score - current.score + h.score;
						newPath.scores = new float[h.scores.length];
						//for(int x = 0; x < h.scores.length; x++) {
							//newPath.scores[x] = bestPath.scores[x] - current.scores[x] + h.scores[x];
						//}
						newPath.lastChange = i+1;
						//newPath.hypPath.addAll(bestPath.hypPath);
						//newPath.hypPath.remove(i);
						//newPath.hypPath.add(i, h);
						for(MosesHypothesis nh : newPath.hypPath) {
							newPath.score += nh.transition;
							for(int x = 0; x < h.scores.length; x++) {
								newPath.scores[x] += nh.scores[x];
							}
							newPath.translation = nh.option.phrasePair.targetPhrase + " " + newPath.translation;
						}
						String s = "";
						for(int x = 0; x < h.scores.length; x++) {
							s += newPath.scores[x] + " ";
						}
						newPath.translation += " ||| "+s+"||| "+newPath.score;
						paths.add(newPath);
					}
				}
			}
		}
		
		return nbest;
	}
	
	class Path {
		ArrayList<MosesHypothesis> hypPath = new ArrayList<MosesHypothesis>();
		String translation="";
		int lastChange;
		float[] scores;
		float score = 0;
	}
}
