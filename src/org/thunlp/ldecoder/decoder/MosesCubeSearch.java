package org.thunlp.ldecoder.decoder;

import java.util.ArrayList;

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
			//遍历stack中的hyp，加上一个option来生成新的hyp
			for(int x = 0; x < stack.size() && x < Config.stackSize; x++) {
				MosesHypothesis hyp = stack.get(x);
				//1 根据bitmap，获取可行的option列表
				boolean[] bitmap = hyp.bitmap; //通过bitmap，找出可用的option的range(startIndex, endIndex)
				ArrayList<MosesTranslationOption> optionList = new ArrayList<MosesTranslationOption>();
				for(int startIndex = Math.max(0, hyp.option.beginIndex-Config.distortionLimit); 
						startIndex < Math.min(length, hyp.option.beginIndex+Config.distortionLimit); startIndex++) {
					int endIndex = startIndex;
					while(!bitmap[endIndex] && endIndex-startIndex < Config.phraseMaxLength && endIndex < length) {
						ArrayList<MosesTranslationOption> l = collector.translationOptions.get(startIndex*length+endIndex);
						if(l != null)
							optionList.addAll(l);
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
					if(insertIndex >= 0)
						nextStack.add(insertIndex, newHyp);
					
				}
				
			}
		}//遍历stack
	}
	
}
