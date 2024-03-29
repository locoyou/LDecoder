package org.thunlp.ldecoder.decoder;

import java.util.ArrayList;

import org.thunlp.ldecoder.config.Config;
import org.thunlp.ldecoder.distortion.IDistortionModel;
import org.thunlp.ldecoder.distortion.MosesMSDDistortionModel;
import org.thunlp.ldecoder.phrasetable.IPhraseTable;
import org.thunlp.ldecoder.phrasetable.MosesPhraseTable;
import org.thunlp.lm.srilm.SRILMWrapper;

public class MosesDecoder implements IDecoder {
	IPhraseTable phraseTable;
	IDistortionModel distortionModel;
	SRILMWrapper lm;
	String sourceSentence;
	String[] sourceSentenceWords;
	int sourceSentenceLength;
	MosesCubeSearch searcher;
	
	/**
	 * 读入配置文件
	 * 载入语言模型、翻译模型、调序模型
	 * @param configFileName
	 */
	public MosesDecoder(String configFileName) {
		Config.config(configFileName);
		phraseTable = new MosesPhraseTable();
		phraseTable.loadTable(Config.phraseTableFileName);
		distortionModel = new MosesMSDDistortionModel();
		distortionModel.loadModel(Config.distortionModelFileName);
		lm = new SRILMWrapper(Config.lmModel, Config.ngramOrder);		
	}
	
	/**
	 * 翻译一句话
	 */
	@Override
	public void decode(String sourceSentence) {
		this.sourceSentence = sourceSentence;
		this.sourceSentenceWords = sourceSentence.split(" ");
		this.sourceSentenceLength = this.sourceSentenceWords.length;
		
		//Create translation options
		MosesTranslationOptionCollector collector = new MosesTranslationOptionCollector(sourceSentence,
				phraseTable, distortionModel, lm);
		collector.createOptions();
		/*
		 //fot test
		for(int i = 0; i < sourceSentenceLength*sourceSentenceLength; i++) {
			System.out.println(i+"=====");
			ArrayList<MosesTranslationOption> x = collector.translationOptions.get(i);
			if(x != null) {
				System.out.println(x.get(0).phrasePair.toString());
				System.out.println(x.get(1).phrasePair.toString());
			}
		}
		*/
		
		//通过cube search生成search graph
		searcher = new MosesCubeSearch(phraseTable, distortionModel, lm, collector);
		searcher.search();
	}

	
	/**
	 * 返回最佳结果
	 * @return
	 */
	@Override
	public String getBest() {
		MosesHypothesis bestHyp = searcher.stacks.get(searcher.stacks.size()-1).get(0);
		if(bestHyp == null)
			return "";
		else {
			float[] score = new float[Config.scoreNum];
			String translation = "";
			MosesHypothesis hyp = bestHyp;
			while(hyp.lastHyp != null) {
				translation = hyp.option.phrasePair.targetPhrase + " " + translation;
				for(int i = 0; i < score.length; i++)
					score[i] += hyp.scores[i];
				hyp = hyp.lastHyp;
			}
			String s = "";
			for(int i = 0; i < score.length; i++)
				s += score[i]+" ";
			return translation + " ||| "+s+"||| "+ bestHyp.score;
		}
	}
	
	/**
	 * 搜索graph，得到nbest结果
	 * @return nbest list
	 */
	@Override
	public ArrayList<String> getNbest(int n) {
		return searcher.getNbest(n);
	}
	
	public static void main(String[] args) {
		//for test
		MosesDecoder decoder = new MosesDecoder("");
		long start = System.currentTimeMillis();
		decoder.decode("今年 前 两 月 广东 高 新 技术 产品 出口 37.6亿 美元");
		//System.out.println(decoder.getBest());
		ArrayList<String> nbest = decoder.getNbest(30);
		System.out.println(System.currentTimeMillis()-start);
		for(String s : nbest)
			System.out.println(s);
		/*
		for(MosesHypothesis h : decoder.searcher.stacks.get(3)) {
			if(h.lastHyp.hypid == 90) {
				System.out.println(h.hypid+"|"+h.option.phrasePair.sourcePhrase+"|"+h.option.phrasePair.targetPhrase);
			}
		}
		*/
	}
}
