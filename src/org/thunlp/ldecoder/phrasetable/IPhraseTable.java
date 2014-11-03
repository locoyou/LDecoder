package org.thunlp.ldecoder.phrasetable;

import java.util.ArrayList;

public interface IPhraseTable {
	public void loadTable(String phraseTableFileName);
	public ArrayList<IPhrasePair> getPhraseRules(String sourcePhrase);
}
