package org.thunlp.ldecoder.decoder;

import java.util.ArrayList;

public interface IDecoder {
	public void decode(String sourceSentence);
	public String getBest();
	public ArrayList<String> getNbest(int n);
}
