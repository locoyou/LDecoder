package org.thunlp.ldecoder.config;

public class Config {
	public static String phraseTableFileName;
	public static String distortionModelFileName;
	
	public static float[] translationWeights;
	
	public static float[] distortionWeights;
	public static float distantWeight;
	
	public static float wordLengthWeight;
	public static float lmWeight;
	
	public static int distortionLimit = 6;
	public static int stackSize = 1000;
	public static int phraseLimit = 20;
	
	public static int ngramOrder = 4;
	public static String lmModel;
	
	public static int phraseMaxLength = 7;
	
	public static void config(String configFileName) {
		//TODO
	}
}
