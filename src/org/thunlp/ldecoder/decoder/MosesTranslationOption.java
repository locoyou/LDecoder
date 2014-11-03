package org.thunlp.ldecoder.decoder;

import org.thunlp.ldecoder.phrasetable.MosesPhrasePair;

public class MosesTranslationOption implements ITranslationOption {
	int beginIndex, endIndex;
	MosesPhrasePair phrasePair;
	float futureScore;
	float score;
	
}
