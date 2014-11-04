package org.thunlp.ldecoder.decoder;

import java.util.ArrayList;

import org.thunlp.ldecoder.config.Config;
import org.thunlp.ldecoder.distortion.IDistortionModel;
import org.thunlp.ldecoder.distortion.MosesMSDDistortionModel;
import org.thunlp.ldecoder.lm.srilm.SRILMWrapper;
import org.thunlp.ldecoder.phrasetable.IPhraseTable;
import org.thunlp.ldecoder.phrasetable.MosesPhraseTable;

public class MosesDecoder implements IDecoder {
	IPhraseTable phraseTable;
	IDistortionModel distortionModel;
	SRILMWrapper lm;
	String sourceSentence;
	String[] sourceSentenceWords;
	int sourceSentenceLength;
	
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
		
		//TODO search
		MosesCubeSearch search = new MosesCubeSearch();
		search.search();
	}

	
	/**
	 * 返回最佳结果
	 * @return
	 */
	@Override
	public String getBest() {
		
		return "";
	}
	
	/**
	 * 搜索graph，得到nbest结果
	 * @return nbest list
	 */
	@Override
	public ArrayList<String> getNbest() {
		
		return null;
	}
	
	public static void main(String[] args) {
		//for test
	}
}
