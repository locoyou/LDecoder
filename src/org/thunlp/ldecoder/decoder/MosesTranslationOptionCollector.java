package org.thunlp.ldecoder.decoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.thunlp.ldecoder.config.Config;
import org.thunlp.ldecoder.distortion.IDistortionModel;
import org.thunlp.ldecoder.lm.srilm.SRILMWrapper;
import org.thunlp.ldecoder.phrasetable.IPhrasePair;
import org.thunlp.ldecoder.phrasetable.IPhraseTable;
import org.thunlp.ldecoder.phrasetable.MosesPhrasePair;
import org.thunlp.ldecoder.phrasetable.MosesPhraseTable;

public class MosesTranslationOptionCollector {

	MosesPhraseTable phraseTable;
	IDistortionModel distortionModel;
	SRILMWrapper lm;
	String sourceSentence;
	String[] sourceSentenceWords;
	int sourceSentenceLength;
	HashMap<Integer, ArrayList<MosesTranslationOption>> translationOptions;
	float[] futureScoreTable;
	OptionComparator comparator = new OptionComparator();
	
	public MosesTranslationOptionCollector(String sourceSentence, IPhraseTable phraseTable,
			IDistortionModel distortionModel,
			SRILMWrapper lm) {
		this.phraseTable = (MosesPhraseTable)phraseTable;
		this.distortionModel = distortionModel;
		this.lm = lm;
		this.sourceSentence = sourceSentence;
		this.sourceSentenceWords = sourceSentence.split(" ");
		this.sourceSentenceLength = sourceSentenceWords.length;
		translationOptions = new HashMap<Integer, ArrayList<MosesTranslationOption>>();
	}
	
	class OptionComparator implements Comparator<MosesTranslationOption> {
		@Override
		public int compare(MosesTranslationOption arg0, MosesTranslationOption arg1) {
			if(arg1.phrasePair.futureScore > arg0.phrasePair.futureScore)
				return 1;
			else
				return -1;
		}
		
	}
	
	public void createOptions() {
		for(int beginIndex = 0; beginIndex < sourceSentenceLength; beginIndex++) {
			for(int endIndex = beginIndex; endIndex < sourceSentenceLength && 
					endIndex < beginIndex + Config.phraseMaxLength; endIndex++) {
				createTranslationOptionsForRange(beginIndex, endIndex);
			}
		}
		
		processUnknownWord();
		
		prune();
		
		sort();
		
		calcFutureScore();
		
		cacheLexDistortion();
		
		preCalculateScores();
	}

	/**
	 * 对剪枝后保留下来的每个translation option，计算所有可以预先计算的分数
	 * 这一步暂时是多余的，因为所有可预先计算的分数在createTranslationOptionsForRange（计算了翻译模型的分数）
	 * 和incorporateLMScores（计算了可计算的部分ngram分数）中都完成了
	 */
	private void preCalculateScores() {
		return; //do nothing
	}

	/**
	 * 对剪枝后保留下来的每个translation option，先从调序模型里读取MSD的分数，预先存在translation option里
	 */
	private void cacheLexDistortion() {
		for(int i = 0; i < sourceSentenceLength; i++) {
			for(int j = i; j < sourceSentenceLength && j < i+Config.phraseMaxLength; j++) {
				ArrayList<MosesTranslationOption> list = translationOptions.get(i);
				if(list != null) {
					for(MosesTranslationOption o : list)
						o.cacheLexDistortion();
				}
			}
		}
	}

	/**
	 * 计算每个范围区间的future score
	 * 具体做法是先将每个区间的future score设为该区间translation options（如果有）最大的future score，
	 * 然后用动态规划计算出所有区间的最大future score
	 */
	private void calcFutureScore() {
		futureScoreTable = new float[sourceSentenceLength*sourceSentenceLength];
		
		for(int i = 0; i < sourceSentenceLength*sourceSentenceLength; i++) {
			ArrayList<MosesTranslationOption> list = translationOptions.get(i);
			if(list != null)
				futureScoreTable[i] = list.get(0).phrasePair.futureScore;
			else
				futureScoreTable[i] = -10000;
		}
		
		for(int colstart = 1; colstart < sourceSentenceLength; colstart++) {
			for(int shift = 0; shift < sourceSentenceLength-colstart; shift++) {
				int beginIndex = shift;
				int endIndex = colstart+shift;
				for(int joinAt = beginIndex; joinAt < endIndex; joinAt++) {
					float joinedScore = futureScoreTable[beginIndex*sourceSentenceLength+joinAt] +
							futureScoreTable[joinAt*sourceSentenceLength+endIndex];
					if(joinedScore > futureScoreTable[beginIndex*sourceSentenceLength+endIndex])
						futureScoreTable[beginIndex*sourceSentenceLength+endIndex] = joinedScore;
				}

			}
		}
	}

	/**
	 * 根据future score，对每个区间的translation option进行排序
	 */
	private void sort() {
		for(int i = 0; i < sourceSentenceLength*sourceSentenceLength; i++) {
			ArrayList<MosesTranslationOption> list = translationOptions.get(i);
			if(list != null) {
				Collections.sort(list, comparator);
			}
		}
	}

	/**
	 * 对translationOption进行剪枝，保证每个range的option数目不超过上限
	 * moses中还通过阈值进行了剪枝
	 * 
	 * 读入option数目限制在create的时候就处理了，目前还没有加入阈值限制
	 * 因此目前这一步什么都不用做
	 */
	private void prune() {
		return; //do nothing
	}

	/**
	 * 处理OOV
	 * 为OOV生成一条短语翻译规则，加入短语表中（这一步其实值得商榷，如果长期运行可能会导致短语表越来越大）
	 */
	private void processUnknownWord() {
		for(int i = 0; i < sourceSentenceLength; i++) {
			if(!phraseTable.vocabulary.contains(sourceSentenceWords[i])) {
				MosesPhrasePair mosesPair = new MosesPhrasePair(sourceSentenceWords[i], true, Config.translationWeights.length);
				mosesPair.setLMAndDistortion(lm, distortionModel);
				MosesTranslationOption option = new MosesTranslationOption();
				option.beginIndex = i;
				option.endIndex = i;
				option.phrasePair = mosesPair;
				ArrayList<MosesTranslationOption> options = new ArrayList<MosesTranslationOption>();
				options.add(option);
				translationOptions.put(i*sourceSentenceLength+i, options);
				phraseTable.addOOVPair(mosesPair);
			}
		}
	}

	/**
	 * 为源语言短语生成translationOption，从短语表中读取短语对，加入collector中
	 * 这一步会预先计算option的翻译模型分数，同时为translationOption计算lm score，获得全部的future score
	 * @param beginIndex
	 * @param endIndex
	 * 
	 */
	private void createTranslationOptionsForRange(int beginIndex, int endIndex) {
		String sourcePhrase = "";
		for(int i = beginIndex; i <= endIndex; i++) {
			sourcePhrase = sourcePhrase + sourceSentenceWords[i];
			if(i != endIndex)
				sourcePhrase += " ";
		}
		
		ArrayList<IPhrasePair> phrasePairs = phraseTable.getPhraseRules(sourcePhrase);
		if(phrasePairs.size() == 0)
			return;
		
		ArrayList<MosesTranslationOption> options = new ArrayList<MosesTranslationOption>();
		
		for(IPhrasePair pair : phrasePairs) {
			MosesPhrasePair mosesPair = (MosesPhrasePair)pair;
			mosesPair.setLMAndDistortion(lm, distortionModel);
			MosesTranslationOption option = new MosesTranslationOption();
			option.beginIndex = beginIndex;
			option.endIndex = endIndex;
			option.phrasePair = mosesPair;
			option.computeTranslationScore();
			option.precomputeLMScore();
			option.computeFutureScore();
			
			options.add(option);
			if(options.size() == Config.phraseLimit)
				break;
		}
		translationOptions.put(beginIndex*sourceSentenceLength+endIndex, options);
	}
}
