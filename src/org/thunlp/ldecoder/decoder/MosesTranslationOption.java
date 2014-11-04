package org.thunlp.ldecoder.decoder;

import org.thunlp.ldecoder.phrasetable.MosesPhrasePair;

public class MosesTranslationOption implements ITranslationOption {
	int beginIndex, endIndex;
	MosesPhrasePair phrasePair;
	
	public void computeTranslationScore() {
		phrasePair.computeTranslationScore();
	}
	
	public void precomputeLMScore() {
		phrasePair.precomputeLMScore();
	}
	
	public void cacheLexDistortion() {
		// TODO Auto-generated method stub
		
	}

	public void computeFutureScore() {
		phrasePair.computeFutureScore();
	}
}
