package org.thunlp.ldecoder.phrasetable.mosesphrasetable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import org.thunlp.ldecoder.phrasetable.PhrasePair;
import org.thunlp.ldecoder.phrasetable.PhraseTable;

public class MosesPhraseTable implements PhraseTable{

	HashMap<String, ArrayList<PhrasePair>> phraseTable = new HashMap<String, ArrayList<PhrasePair>>();
	
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
				ArrayList<PhrasePair> list = phraseTable.get(tags[0]);
				if(list == null) {
					list = new ArrayList<PhrasePair>();
					phraseTable.put(tags[0], list);
				}
				list.add(new MosesPhrasePair(tags));
			}
			br.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public ArrayList<PhrasePair> getPhraseRules(String sourcePhrase) {
		return phraseTable.get(sourcePhrase);
	}

}
