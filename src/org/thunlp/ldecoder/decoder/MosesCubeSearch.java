package org.thunlp.ldecoder.decoder;

import java.util.ArrayList;

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
	@SuppressWarnings("unused")
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
		//TODO 从第0到第n-1个stack，通过当前stack中的hyp，再翻译一个短语生成新的hyp，加入对应stack中
		for(int stackId = 0; stackId < length; stackId++) {
			ArrayList<MosesHypothesis> stack = stacks.get(stackId);
			//遍历stack中的hyp，加上一个option来生成新的hyp
			for(MosesHypothesis hyp : stack) {
				//1 选取一个option，生成新的hyp
				boolean[] bitmap = hyp.bitmap; //通过bitmap，找出可用的option的range(startIndex, endIndex)
				
				/*
				 * 2 和stack中已有的hyp进行逐一比较，可以1.确定插入位置，保证排序；2.判断是否可以合并。stack中只保留合并后最优（score+futurescore最大）的结果
				 * 被合并掉的hyp将放置在recombined list中，同时设置hyp的recombinedListId（只要进行了合并，不论是留下来还是被合并
				 * 掉的hyp，都要设置recombinedListId，这样才能回溯的时候找到）
				 * 注：如果在找到插入位置前先找到可以合并，那么就不用插入了；否则设置完recombinedListId后，先把已在stack中要被
				 * 合并掉的hyp从stack中删除，再插入该hyp
				 * */
			}
		}
		
		//最后对第n个stack排序
	}
	
}
