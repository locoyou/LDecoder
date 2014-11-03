package org.thunlp.ldecoder.lm;

import java.util.List;

public interface INgramLanguageModel {
	
	String SENT_START = "<s>";
	String SENT_END = "</s>";
	
	/**
	 * 获得词word的概率
	 * @param word
	 * @return log(probability)，底为e
	 */
	float prob(String word);
	
	/**
	 * 计算句子sentence的概率，注意，头和尾不会加<s>和</s>
	 * @param sentence
	 * @return log(probability)，底为e
	 */
	float prob(List<String> sentence);
	
	/**
	 * prob(sentence[n:]|sentence[1:n-1])
	 * @param sentence
	 * @return log(probability)，底为e
	 */
	float probReliable(List<String> sentence);

	/**
	 * 计算sentence里[start, end)范围内词的概率
	 * 注意：在计算过程中并不会考虑[start,end)以外的词
	 * @param sentence
	 * @param start
	 * @param end
	 * @return log(probability)，底为e
	 */
	float prob(List<String> sentence, int start, int end);
	
	/**
	 * 计算sentence的概率，注意，头和尾不会加<s>和</s>
	 * @param sentence
	 * @return log(probability)，底为e
	 */
	float prob(String[] sentence);
	
	/**
	 * prob(sentence[n:]|sentence[1:n-1])
	 * @param sentence
	 * @return log(probability)，底为e
	 */
	float probReliable(String[] sentence);
	
	/**
	 * 计算sentence里[start, end)范围内词的概率
	 * 注意：在计算过程中并不会考虑[start,end)以外的词
	 * @param sentence
	 * @param start
	 * @param end
	 * @return log(probability)，底为e
	 */
	float prob(String[] sentence, int start, int end);

	/**
	 * 计算prob(word|context)
	 * @param word
	 * @param context 未经倒序的
	 * @return log(probability)，底为e
	 */
	float prob(String word, String[] context);
	
	/**
	 * 计算prob(word|context[contextStart],context[contextStart-1],...,context[contextEnd-1]))
	 * @param word
	 * @param context 未经倒序的
	 * @param contextStart
	 * @param contextEnd
	 * @return log(probability)，底为e
	 */
	float prob(String word, String[] context, int contextStart, int contextEnd);
	
	/**
	 * 计算prob(word|context)
	 * @param word
	 * @param context 未经倒序的
	 * @return log(probability)，底为e
	 */
	float prob(String word, List<String> context);
	
	/**
	 * 计算prob(word|context[contextStart],context[contextStart-1],...,context[contextEnd-1]))
	 * @param word
	 * @param context 未经倒序的
	 * @param contextStart
	 * @param contextEnd
	 * @return log(probability)，底为e
	 */
	float prob(String word, List<String> context, int contextStart, int contextEnd);
	
	int getOrder();
	
	long getWordIndex(String word);
}
