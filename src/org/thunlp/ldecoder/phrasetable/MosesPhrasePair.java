package org.thunlp.ldecoder.phrasetable;


public class MosesPhrasePair implements IPhrasePair {
	String sourcePhrase, targetPhrase;
	float[] scores;
	String wordAlignStr, freqStr;
	
	@Override
	public float[] getScores() {
		return scores;
	}

	public MosesPhrasePair(String[] tags) {
		sourcePhrase = tags[0];
		sourcePhrase = tags[1];
		String[] s = tags[2].split(" ");
		scores = new float[s.length];
		for(int i = 0; i < s.length; i++)
			scores[i] = (float)Math.log(Double.valueOf(s[i]));
		wordAlignStr = tags[3];
		freqStr = tags[4];
	}
}
