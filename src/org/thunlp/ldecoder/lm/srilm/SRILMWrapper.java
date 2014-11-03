package org.thunlp.ldecoder.lm.srilm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import org.thunlp.ldecoder.lm.INgramLanguageModel;
import org.thunlp.ldecoder.util.LRUCache;

public class SRILMWrapper implements INgramLanguageModel {
	private int order;
	private SRILMLanguageModel lm;
	private SWIGTYPE_p_unsigned_int contextArray;
	private LRUCache<String, Float> cache;
	private float baseFactor = 2.302585092994f;
	
	static {
		System.loadLibrary("srilmwrap");
	}

	public SRILMWrapper(String filename, int order) {
		this(filename, order, 10000);
	}
	
	public SRILMWrapper(String filename, int order, int cacheSize) {
		long start = System.currentTimeMillis();
		System.err.println(String.format("Loading lm from \"%s\"...", filename));
		cache = new LRUCache<String, Float>(cacheSize);
		this.order = order;
		lm = new SRILMLanguageModel(filename, order);
		contextArray = SRILM.new_unsigned_array(order + 1);
		long end = System.currentTimeMillis();
		double time = (end-start)/1000.0;
		System.err.println(String.format("Loaded [%5.4f]s", time));
	}

	/**
	 * 获得词word的概率
	 * @param word 词
	 * @return log(probability)，底为e
	 */
	public float prob(String word) {
		long wordIndex = lm.getWordIndex(word);
		SRILM.unsigned_array_setitem(contextArray, 0, lm.getVocabNone());
		return lm.prob(wordIndex, contextArray)*baseFactor;
	}

	/**
	 * 计算句子sentence的概率
	 * @param sentence
	 * @return log(probability)，底为e
	 */
	public float prob(List<String> sentence) {
		float r = 0;
		for(int i = 0; i < sentence.size(); i++)
			r += prob(sentence.get(i), sentence, 0, i);
		return r;
	}
	
	/**
	 * prob(sentence[n:]|sentence[1:n-1])
	 * @param sentence
	 * @return log(probability)，底为e
	 */
	public float probReliable(List<String> sentence) {
		float r = 0;
		for(int i = order-1; i < sentence.size(); i++)
			r += prob(sentence.get(i), sentence, 0, i);
		return r;
	}
	
	/**
	 * 计算sentence里[start, end)范围内词的概率
	 * 注意：在计算过程中并不会考虑[start,end)以外的词
	 * @param sentence
	 * @param start
	 * @param end
	 * @return log(probability)，底为e
	 */
	public float prob(List<String> sentence, int start, int end) {
		float r = 0;
		for(int i = start; i < end; i++)
			r += prob(sentence.get(i), sentence, start, i);
		return r;
	}


	/**
	 * 计算sentence的概率
	 * @param sentence
	 * @return log(probability)，底为e
	 */
	public float prob(String[] sentence) {
		float r = 0;
		for(int i = 0; i < sentence.length; i++)
			r += prob(sentence[i], sentence, 0, i);
		return r;
	}
	
	/**
	 * prob(sentence[n:]|sentence[1:n-1])
	 * @param sentence
	 * @return log(probability)，底为e
	 */
	public float probReliable(String[] sentence) {
		float r = 0;
		for(int i = order-1; i < sentence.length; i++)
			r += prob(sentence[i], sentence, 0, i);
		return r;
	}
	
	/**
	 * 计算sentence里[start, end)范围内词的概率
	 * 注意：在计算过程中并不会考虑[start,end)以外的词
	 * @param sentence
	 * @param contextStart
	 * @param contextEnd
	 * @return log(probability)，底为e
	 */
	public float prob(String[] sentence, int contextStart, int contextEnd) {
		float r = 0;
		for(int i = contextStart; i < contextEnd; i++)
			r += prob(sentence[i], sentence, contextStart, i);
		return r;
	}
	
	/**
	 * 计算prob(word|context)
	 * @param word
	 * @param context 未经倒序的
	 * @return log(probability)，底为e
	 */
	public float prob(String word, String[] context) {
		return prob(word, context, 0, context.length);
	}

	/**
	 * 计算prob(word|context[contextStart],context[contextStart-1],...,context[contextEnd-1]))
	 * @param word
	 * @param context 未经倒序的
	 * @param contextStart
	 * @param contextEnd
	 * @return log(probability)，底为e
	 */
	public float prob(String word, String[] context, int contextStart, int contextEnd) {
		StringBuilder sb = new StringBuilder();
		sb.append(word).append('|');
		int i = 0;
		if (contextEnd > contextStart) {
			contextStart = Math.max(contextStart, contextEnd-((int)lm.get_order()-1));
			for (; i < contextEnd-contextStart; i++)
				sb.append(' ').append(context[contextEnd - 1 - i]);
		}
		String key = sb.toString();
		if (cache.containsKey(key))
			return cache.get(key);
		
		long wordIndex = lm.getWordIndex(word);
		i = 0;
		if (contextEnd > contextStart) {
			contextStart = Math.max(contextStart, contextEnd-((int)lm.get_order()-1));
			for (; i < contextEnd-contextStart; i++) {
				long index = lm.getWordIndex(context[contextEnd - 1 - i]);
				SRILM.unsigned_array_setitem(contextArray, i, index);
			}
		}
		SRILM.unsigned_array_setitem(contextArray, i, lm.getVocabNone());
		float p = lm.prob(wordIndex, contextArray)*baseFactor;
		cache.put(key, p);
		return p;
	}

	/**
	 * 计算prob(word|context)
	 * @param word
	 * @param context 未经倒序的
	 * @return log(probability)，底为e
	 */
	public float prob(String word, List<String> context) {
		return prob(word, context, 0, context.size());
	}

	/**
	 * 计算prob(word|context[contextStart],context[contextStart-1],...,context[contextEnd-1]))
	 * @param word
	 * @param context 未经倒序的
	 * @param contextStart
	 * @param contextEnd
	 * @return log(probability)，底为e
	 */
	public float prob(String word, List<String> context, int contextStart, int contextEnd) {
		StringBuilder sb = new StringBuilder();
		sb.append(word).append('|');
		int i = 0;
		if (contextEnd > contextStart) {
			contextStart = Math.max(contextStart, contextEnd-((int)lm.get_order()-1));
			for (; i < contextEnd-contextStart; i++)
				sb.append(' ').append(context.get(contextEnd - 1 - i));
		}
		String key = sb.toString();
		if (cache.containsKey(key))
			return cache.get(key);
		
		long wordIndex = lm.getWordIndex(word);
		i = 0;
		if (contextEnd > contextStart) {
			contextStart = Math.max(contextStart, contextEnd-((int)lm.get_order()-1));
			for (; i < contextEnd-contextStart; i++) {
				long index = lm.getWordIndex(context.get(contextEnd - 1 - i));
				SRILM.unsigned_array_setitem(contextArray, i, index);
			}
		}
		SRILM.unsigned_array_setitem(contextArray, i, lm.getVocabNone());
		float p = lm.prob(wordIndex, contextArray)*baseFactor;
		cache.put(key, p);
		return p;
	}

	public int getOrder() {
		return order;
	}

	protected void finalize() {
		delete();
	}
	
	@Override
	public long getWordIndex(String word) {
		return lm.getWordIndex(word);
	}

	private synchronized void delete() {
		SRILM.delete_unsigned_array(contextArray);
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Usage: java org.thunlp.lm.srilm.SRILMWrapper lm order");
			System.exit(-1);
		}
		
		String parten = "#.###############";
		java.text.DecimalFormat decimal = new java.text.DecimalFormat(parten);
		
		String lmData = args[0];
		int order = Integer.parseInt(args[1]);
		SRILMWrapper lm = new SRILMWrapper(lmData, order);		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line;
		System.out.print("Input (quit):");
		while((line = br.readLine()) != null) {
			if (line.equals("quit"))
				System.exit(0);
			else {
				String[] words = line.split(" ");

        // compute the probability of words[n:...]
				float score = 0;
				for (int i = lm.getOrder()-1; i < words.length; i++) {
					score += lm.prob(words[i], words, 0, i);
				}
				System.out.println(decimal.format(score));
				
				float score2 = lm.prob(words);
				System.out.println(decimal.format(score2));
				
        // compute the probability of the entire sentence
				List<String> context = new ArrayList<String>(lm.getOrder()-1);
				context.add(SENT_START);
				int bound = Math.min(lm.getOrder()-1, words.length);
				for (int i = 0; i < bound; i++) {
					score += lm.prob(words[i], context);
					context.add(words[i]);
				}
				score += lm.prob(SENT_END, context);
				System.out.println(decimal.format(score));
			}
			System.out.print("Input (quit):");			
		}
	}

}
