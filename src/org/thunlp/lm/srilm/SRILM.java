/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.11
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.thunlp.lm.srilm;

public class SRILM {
  public static SWIGTYPE_p_unsigned_int new_unsigned_array(int nelements) {
    long cPtr = SRILMJNI.new_unsigned_array(nelements);
    return (cPtr == 0) ? null : new SWIGTYPE_p_unsigned_int(cPtr, false);
  }

  public static void delete_unsigned_array(SWIGTYPE_p_unsigned_int ary) {
    SRILMJNI.delete_unsigned_array(SWIGTYPE_p_unsigned_int.getCPtr(ary));
  }

  public static long unsigned_array_getitem(SWIGTYPE_p_unsigned_int ary, int index) {
    return SRILMJNI.unsigned_array_getitem(SWIGTYPE_p_unsigned_int.getCPtr(ary), index);
  }

  public static void unsigned_array_setitem(SWIGTYPE_p_unsigned_int ary, int index, long value) {
    SRILMJNI.unsigned_array_setitem(SWIGTYPE_p_unsigned_int.getCPtr(ary), index, value);
  }

}
