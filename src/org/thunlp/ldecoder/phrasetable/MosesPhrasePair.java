package org.thunlp.ldecoder.phrasetable;

import org.thunlp.ldecoder.config.Config;
import org.thunlp.ldecoder.distortion.IDistortionModel;
import org.thunlp.ldecoder.lm.srilm.SRILMWrapper;


public class MosesPhrasePair implements IPhrasePair {
	String sourcePhrase, targetPhrase;
	float[] transScores; //翻译模型的各个分数（log probability）
	String wordAlignStr, freqStr;
	boolean preComputedTrans = false;
	float[] distortionScores;
	boolean cacheDistortion = false;
	boolean preComputedLM = false;
	boolean computedFuture = false;
	SRILMWrapper lm;
	IDistortionModel distortionModel;
	boolean isOOV = false;
	
	float preScore; //预算的分数，在moses中只有translation model的分数预先计算
	float preNgram; //如果目标语言短语长度比ngram的阶数大，那么后面一部分的ngram分数可以预先计算。这里没有乘以LM的权重
	float allNgram; //用于计算futureScore，详情见SPBT论文
	public float futureScore; // = preScore + lmWeight * allNgram；预估的future score
	//在hypothesis中, score = preScore + reordering score + lmWeigth*(preNgram + headNgram + tailNgram)
	
	public void computeTranslationScore() {
		if(preComputedTrans)
			return;
		
		preScore = 0;
		for(int i = 0; i < transScores.length; i++) {
			preScore += Config.translationWeights[i] * transScores[i];
		}
		preComputedTrans = true;
	}
	
	public void precomputeLMScore() {
		if(preComputedLM)
			return;
		
		preNgram = 0;
		allNgram = 0;
		
		String[] words = sourcePhrase.split(" ");
		for (int i = lm.getOrder()-1; i < words.length; i++) {
			preNgram += lm.prob(words[i], words, 0, i);
		}
		
		//这一步可能是有问题的，需要看一下SRILM在前文不足的时候是怎么算的
		allNgram = preNgram;
		for(int i = 0; i < lm.getOrder()-1; i++) {
			allNgram += lm.prob(words[i], words, 0, i);
		}
		
		preComputedLM = true;
	}
	
	public void computeFutureScore() {
		if(computedFuture)
			return;
		if(!preComputedTrans)
			computeTranslationScore();
		if(!preComputedLM)
			precomputeLMScore();
		futureScore = preScore + Config.lmWeight*allNgram;
		computedFuture = true;
	}
	
	@Override
	public float[] getScores() {
		return transScores;
	}

	public MosesPhrasePair(String[] tags) {
		sourcePhrase = tags[0];
		sourcePhrase = tags[1];
		String[] s = tags[2].split(" ");
		transScores = new float[s.length];
		for(int i = 0; i < s.length; i++)
			transScores[i] = (float)Math.log(Double.valueOf(s[i]));
		wordAlignStr = tags[3];
		freqStr = tags[4];
		preScore = 0;
		preNgram = 0;
		futureScore = 0;
	}
	
	/**
	 * add OOV to phrase table
	 * @param sourcePhrase
	 * @param isOOV
	 * @param scoreLength
	 */
	public MosesPhrasePair(String sourcePhrase, boolean isOOV, int scoreLength) {
		if(!isOOV) {
			System.err.println("not OOV");
			return;
		}
		
		this.isOOV = isOOV;
		this.sourcePhrase = sourcePhrase;
		this.targetPhrase = sourcePhrase;
		transScores = new float[scoreLength];
		for(int i = 0; i < scoreLength; i++)
			transScores[i] = 0;
		futureScore = -100; //OOV
		preScore = -100; //OOV
		preNgram = 0;
		allNgram = 0;
		wordAlignStr = "0-0";

		preComputedTrans = false;
		cacheDistortion = false;
		preComputedLM = false;
		computedFuture = false;
	}

	public void setLMAndDistortion(SRILMWrapper lm, IDistortionModel distortionModel) {
		this.lm = lm;
		this.distortionModel = distortionModel;
	}

	public void cacheLexDistortion() {
		if(cacheDistortion)
			return;
		distortionScores = distortionModel.getDistortionScores(sourcePhrase);
		cacheDistortion = true;
	}
}
