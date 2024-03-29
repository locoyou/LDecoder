package org.thunlp.ldecoder.phrasetable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;


public class MosesPhraseTable implements IPhraseTable{

	HashMap<String, ArrayList<IPhrasePair>> phraseTable = new HashMap<String, ArrayList<IPhrasePair>>();
	public HashSet<String> vocabulary = new HashSet<String>();
	
	@Override
	public void loadTable(String phraseTableFileName) {
		try{
			BufferedReader br;
			if(phraseTableFileName.endsWith(".gz")) {
				br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(phraseTableFileName))));
			}
			else {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(phraseTableFileName)));
			}
			String line;
			while((line = br.readLine()) != null) {
				String[] tags = line.split(" \\|\\|\\| ");
				ArrayList<IPhrasePair> list = phraseTable.get(tags[0]);
				if(list == null) {
					list = new ArrayList<IPhrasePair>();
					phraseTable.put(tags[0], list);
				}
				list.add(new MosesPhrasePair(tags));
				String[] words = tags[0].split(" ");
				for(String word : words)
					vocabulary.add(word);
			}
			br.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public ArrayList<IPhrasePair> getPhraseRules(String sourcePhrase) {
		return phraseTable.get(sourcePhrase);
	}

	public void addOOVPair(MosesPhrasePair mosesPair) {
		ArrayList<IPhrasePair> list = new ArrayList<IPhrasePair>();
		list.add(mosesPair);
		
		vocabulary.add(mosesPair.sourcePhrase);
		phraseTable.put(mosesPair.sourcePhrase, list);
		
	}

}
