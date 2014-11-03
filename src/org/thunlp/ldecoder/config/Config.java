package org.thunlp.ldecoder.config;

public class Config {
	public static String phraseTableFileName;
	public static String distortionModelFileName;
	
	public static float[] translationWeights;
	
	public static float[] distortionWeights;
	public static float distantWeight;
	
	public static float wordLengthWeight;
	public static float languageModelWeight;
	
	public static int distortionLimit = 6;
	public static int stackSize = 1000;
	public static int phraseLimit = 20;
	
	public static void config(String configFileName) {
		//TODO
	}
}
