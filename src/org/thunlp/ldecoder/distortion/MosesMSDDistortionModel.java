package org.thunlp.ldecoder.distortion;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;


public class MosesMSDDistortionModel implements IDistortionModel {
	public final static int DIS_M = 0, DIS_S = 1, DIS_D = 2;

	HashMap<String, MSDDistribution> reorderingTable = new HashMap<String, MSDDistribution>();
	
	@Override
	// load msd reordering table
	public void loadModel(String distortionModelFileName) {
		try{
			BufferedReader br;
			if(distortionModelFileName.endsWith(".gz")) {
				br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(distortionModelFileName))));
			}
			else {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(distortionModelFileName)));
			}
			String line;
			while((line = br.readLine()) != null) {
				String[] tags = line.split(" \\|\\|\\| ");
				reorderingTable.put(tags[0], new MSDDistribution(tags[1]));
			}
			br.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	/**
	 * @param sourcePhrase 源语言短语，DIS是DIS_M,DIS_S,DIS_D其中之一
	 * @return scores DIS对应位置为该短语M/S/D的log probability（底为e），其他位置为0。对于OOV返回全0项
	 */
	public float[] getDistortionScores(String sourcePhrase, int DIS) {
		
		float[] scores = new float[3];
		MSDDistribution distribution = reorderingTable.get(sourcePhrase);
		if(distribution != null) {
			scores[DIS] = distribution.get(DIS);
		}
		return scores;
	}

	class MSDDistribution {
		float[] msdScores;
		
		MSDDistribution(String msd) {
			String[] tags = msd.split(" ");
			msdScores = new float[3];
			for(int i = 0; i < 3; i++)
				msdScores[i] = (float)Math.log(Double.valueOf(tags[i]));
		}
		
		float get(int DIS) {
			return msdScores[DIS];
		}
	}
}
