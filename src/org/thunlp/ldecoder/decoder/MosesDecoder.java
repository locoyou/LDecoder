package org.thunlp.ldecoder.decoder;

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
	
	public MosesDecoder(String configFileName) {
		Config.config(configFileName);
		phraseTable = new MosesPhraseTable();
		phraseTable.loadTable(Config.phraseTableFileName);
		distortionModel = new MosesMSDDistortionModel();
		distortionModel.loadModel(Config.distortionModelFileName);
		lm = new SRILMWrapper(Config.lmModel, Config.ngramOrder);		
	}
	
	@Override
	public String decode(String sourceSentence) {
		//Create translation options
		MosesTranslationOptionCollector collector = new MosesTranslationOptionCollector(sourceSentence,
				phraseTable, distortionModel, lm);
		collector.createOptions();
		
		//TODO decode
		
		return null;
	}

	public static void main(String[] args) {
		//TODO
	}
}
