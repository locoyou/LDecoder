package org.thunlp.ldecoder.decoder;

import org.thunlp.ldecoder.config.Config;

public class MosesHypothesis {
	int hypid;
	String remainWords; //最后的n-1个词，n是LM的order。这个用于判断是否可以合并hyp，并用于下一个hyp生成时语言模型分数的计算
	MosesHypothesis lastHyp;
	MosesHypothesis bestNextHyp;
	float[] scores;
	float score;
	float futureScore;
	MosesTranslationOption option;
	int coverHead;
	int coverTail;
	boolean[] bitmap; //该Hyp已经覆盖的源语言词的bitmap。用来判断是否可以合并hyp
	int bitmapId; //bitmap的二进制数表示。可以加快判断bitmap是否相同
	int recombinedListId;
	
	public MosesHypothesis(int hypid) {
		this.hypid = hypid;
		recombinedListId = -1;
	}

	/**
	 * 生成初始的hyp0
	 * @param collector
	 */
	public void buildFirstHyp(MosesTranslationOptionCollector collector) {
		score = 0f;
		futureScore = collector.futureScoreTable[collector.sourceSentenceLength];
		scores = new float[Config.scoreNum];
		lastHyp = null;
		remainWords = "<s>"; //SENT_START
		option = null;
		coverHead = coverTail = -1;
		bitmap = new boolean[collector.sourceSentenceLength];
		for(int i = 0; i < bitmap.length; i++)
			bitmap[i] = false;
		bitmapId = 0;
	}
	
	/**
	 * 从上一个Hyp生成该Hyp，新翻译的短语为option
	 * @param lastHyp
	 * @param option
	 * @param collector
	 */
	public void buildFromLastHyp(MosesHypothesis lastHyp, MosesTranslationOption option, MosesTranslationOptionCollector collector) {
		this.lastHyp = lastHyp;
		this.option = option;
		//TODO 计算scores, score, futureScore
		
		//判断是否是lastHyp的bestNextHyp
	}
	
	/**
	 * 完善最后一个stack中的hyp，会在buildFromLastHyp之后调用，主要用于计算LM最后的SENT_END的分数
	 * @param collector
	 */
	public void buildFinalHyp(MosesTranslationOptionCollector collector) {
		
	}

}
