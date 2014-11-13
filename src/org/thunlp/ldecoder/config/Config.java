package org.thunlp.ldecoder.config;

public class Config {
	public static String phraseTableFileName = "/home/liucy/moses/test/model/phrase-table.gz";
	public static String distortionModelFileName = "/home/liucy/moses/test/model/reordering-table.wbe-msd-backward-f.gz";
	
	/*
	public static float[] translationWeights={0.0437227f,0.0499158f,0.06036f,0.0733148f,0.0402887f};
	
	public static float[] distortionWeights={0.142835f,0.0310744f,0.110324f};
	public static float distantWeight=0.0653981f;
	
	public static float wordLengthWeight=-0.263661f;
	public static float lmWeight=0.119106f;
	*/
	public static float[] translationWeights={1f,1f,1f,1f,1f};
	
	public static float[] distortionWeights={1f,1f,1f};
	public static float distantWeight=1f;
	
	public static float wordLengthWeight=1f;
	public static float lmWeight=1f;
	
	public static int distortionLimit = 6;
	public static int stackSize = 1000;
	public static int phraseLimit = 20;
	
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
