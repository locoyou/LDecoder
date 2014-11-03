package org.thunlp.ldecoder.distortion;

public interface IDistortionModel {
	public void loadModel(String distortionModelFileName);
	
	/**
	 * 获得短语sourcePhrase的调序分数
	 * @param sourcePhrase 源语言短语
	 * @param DIS 调序标记，不同的调序模型定义不同的调序标记，如MSD
	 * @return scores 调序分数，可能有多维，如MSD模型会返回3个值，其中两个为0
	 */
	public float[] getDistortionScores(String sourcePhrase, int DIS);
}
