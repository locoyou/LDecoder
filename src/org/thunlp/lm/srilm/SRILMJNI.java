/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.11
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.thunlp.lm.srilm;

public class SRILMJNI {
  public final static native long new_unsigned_array(int jarg1);
  public final static native void delete_unsigned_array(long jarg1);
  public final static native long unsigned_array_getitem(long jarg1, int jarg2);
  public final static native void unsigned_array_setitem(long jarg1, int jarg2, long jarg3);
  public final static native long new_SRILMLanguageModel(String jarg1, long jarg2);
  public final static native long SRILMLanguageModel_getWordIndex(long jarg1, SRILMLanguageModel jarg1_, String jarg2);
  public final static native long SRILMLanguageModel_getVocabNone(long jarg1, SRILMLanguageModel jarg1_);
  public final static native float SRILMLanguageModel_prob(long jarg1, SRILMLanguageModel jarg1_, long jarg2, long jarg3);
  public final static native long SRILMLanguageModel_get_order(long jarg1, SRILMLanguageModel jarg1_);
  public final static native void delete_SRILMLanguageModel(long jarg1);
}