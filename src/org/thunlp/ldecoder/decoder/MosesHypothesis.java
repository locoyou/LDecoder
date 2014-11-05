package org.thunlp.ldecoder.decoder;

import java.util.ArrayList;

import org.thunlp.ldecoder.config.Config;

public class MosesHypothesis {
	int hypid;
	String remainWords; //最后的n-1个词，n是LM的order。这个用于判断是否可以合并hyp，并用于下一个hyp生成时语言模型分数的计算
	MosesHypothesis lastHyp;
	MosesHypothesis bestNextHyp;
	float[] scores;
	float score;
	float transition;
	float futureScore;
	MosesTranslationOption option;
	int allCoverNum; //该Hyp到目前为止覆盖的源语言词数，即该hyp应该在的stackId
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
		score = 0;
		transition = 0;
		futureScore = collector.futureScoreTable[collector.sourceSentenceLength];
		scores = new float[Config.scoreNum];
		lastHyp = null;
		remainWords = "<s>"; //SENT_START
		option = new MosesTranslationOption();
		option.beginIndex = option.endIndex = -1;
		bitmap = new boolean[collector.sourceSentenceLength];
		for(int i = 0; i < bitmap.length; i++)
			bitmap[i] = false;
		bitmapId = 0;
		allCoverNum = 0;
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
		allCoverNum = lastHyp.allCoverNum + option.endIndex-option.beginIndex+1;
		
		bitmap = new boolean[collector.sourceSentenceLength];
		for(int i = 0; i < bitmap.length; i++)
			bitmap[i] = lastHyp.bitmap[i];
		bitmapId = lastHyp.bitmapId;
		
		//填补bitmap
		for(int i = option.beginIndex; i <= option.endIndex; i++) {
			bitmap[i] = true;
			bitmapId += (int)Math.pow(2, i);
		}
		
		//计算scores, transition, score
		scores = new float[Config.scoreNum];
		transition = 0;
		
		int index = 0;
		//1. translation model
		for(int i = 0; i < option.phrasePair.transScores.length; i++)
			scores[i] = option.phrasePair.transScores[i];
		transition += option.phrasePair.preScore;
		index += option.phrasePair.transScores.length;
		
		//2. reordering model
		int k = 2; //d
		if(option.beginIndex - lastHyp.option.endIndex == 1) //m
			k = 0;
		else if(option.beginIndex - lastHyp.option.endIndex == -1) //s
			k = 1;
		
		scores[index+k] = option.phrasePair.distortionScores[k];
		transition += Config.distortionWeights[k]*scores[index+k];
		index += 3;
		
		//3. distance
		scores[index] = -Math.abs(option.beginIndex - lastHyp.option.beginIndex);
		transition += Config.distantWeight * scores[index];
		index ++;
		
		//4. lm
		String[] remainContext = lastHyp.remainWords.split(" ");
		String[] words = option.phrasePair.targetPhrase.split(" ");
		remainWords = "";
		
		float lmScore = 0;
		ArrayList<String> context = new ArrayList<String>();
		for(int i = 0; i < remainContext.length; i++)
			context.add(remainContext[i]);
		int bound = Math.min(Config.ngramOrder-1, words.length);
		for (int i = 0; i < bound; i++) {
			lmScore += collector.lm.prob(words[i], context);
			context.add(words[i]);
		}
		for(int i = bound; i < words.length; i++)
			context.add(words[i]);
		
		for(int i = Math.max(0, context.size()-Config.ngramOrder+1); i < context.size(); i++)
			remainWords += context.get(i) + " ";
		remainWords = remainWords.substring(0, remainWords.length()-1);
		
		if(allCoverNum == collector.sourceSentenceLength) { //SENT_END
			String[] contextWords = remainWords.split(" ");
			lmScore +=  collector.lm.prob("</s>", contextWords);
		}
		
		scores[index] = lmScore + option.phrasePair.preNgram;
		transition += Config.lmWeight * scores[index];
		index++;
		
		//5. length
		scores[index] = -option.phrasePair.targetPhraseLength;
		transition += Config.wordLengthWeight * scores[index];
		index ++;
		
		//6. OOV
		if(option.phrasePair.isOOV) {
			scores[index] = -100;
			transition -= 100;
		}
		score = lastHyp.score + transition;
		
		// 计算futureScore
		futureScore = 0;
		if(collector.bitmapFutureScore.containsKey(bitmapId))
			futureScore = collector.bitmapFutureScore.get(bitmapId);
		else {
			int startIndex = 0, endIndex;
			
			while(startIndex < collector.sourceSentenceLength) {
				if(bitmap[startIndex]) {
					startIndex++;
				}
				else{
					endIndex = startIndex;
					while(endIndex < bitmap.length && !bitmap[endIndex])
						endIndex++;
					futureScore += collector.futureScoreTable[startIndex*collector.sourceSentenceLength+endIndex-1];
					startIndex = endIndex;
				}
			}
			collector.bitmapFutureScore.put(bitmapId, futureScore);
		}
		
		//判断是否是lastHyp的bestNextHyp
		if(lastHyp.bestNextHyp == null || lastHyp.bestNextHyp.score < this.score) {
			lastHyp.bestNextHyp = this;
		}
	}

	public boolean canCombine(MosesHypothesis newHyp) {
		return (newHyp.remainWords.equals(remainWords) && newHyp.bitmapId == bitmapId);
	}

}
