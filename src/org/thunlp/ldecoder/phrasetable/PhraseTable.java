package org.thunlp.ldecoder.phrasetable;

import java.util.ArrayList;

public interface PhraseTable {
	public void loadTable(String phraseTableFileName);
	public ArrayList<PhrasePair> getPhraseRules(String sourcePhrase);
}
