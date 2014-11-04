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
	public static int stackSize = 1000;
	public static int phraseLimit = 20;
	
	public static int ngramOrder = 4;
	public static String lmModel = "/home/liucy/moses/test/model/xinhua.low.4.lm";
	
	public static int phraseMaxLength = 7;
	
	public static void config(String configFileName) {
		//TODO
	}
	
	public static void mosesConfig(String mosesConfigFileName) {
		//TODO
	}
}
