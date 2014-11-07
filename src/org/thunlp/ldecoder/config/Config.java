package org.thunlp.ldecoder.config;

public class Config {
	public static String phraseTableFileName = "/home/liucy/moses/test/model/phrase-table.gz";
	public static String distortionModelFileName = "/home/liucy/moses/test/model/reordering-table.wbe-msd-backward-f.gz";
	
	public static float[] translationWeights={0.1f,0.1f,0.1f,0.1f,0.1f};
	
	public static float[] distortionWeights={0.1f,0.1f,0.1f};
	public static float distantWeight=0.1f;
	
	public static float wordLengthWeight=0.1f;
	public static float lmWeight=0.1f;
	
	public static int distortionLimit = 6;
	public static int stackSize = 50;
	public static int phraseLimit = 5;
	
	public static int ngramOrder = 3;
	public static String lmModel = "/home/liucy/tools/srilm-java-wrapper/test-data/lm.3.arpa";
	
	public static int phraseMaxLength = 4;
	
	public static int scoreNum = 5+3+1+1+1+1; //translation model, msd, distance, lm, length, oov 
	
	public static void config(String configFileName) {
		//read config from config file
	}
	
	public static void mosesConfig(String mosesConfigFileName) {
		//read moses config file
	}
}
