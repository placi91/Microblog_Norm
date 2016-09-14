import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class SetOOVFrequency {

	public static void main(String[] args) {

		HashMap<String, Integer> freqWords = new HashMap<>();
		List<Word> oov = new ArrayList<>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader("words.txt"));
			String line;
			while ((line = br.readLine()) != null) {
					String[] parts = line.split(" ");
					int freq = Integer.parseInt(parts[1].trim());
					if(freq >= 10) {
						freqWords.put(parts[0], freq);
					}
			}
			br = new BufferedReader(new FileReader("oov.txt"));
			while ((line = br.readLine()) != null) {
				String oovWord = line.trim();
				if(freqWords.containsKey(oovWord) && oovWord.length() >= 3) {
					oov.add(new Word(oovWord, freqWords.get(oovWord)));
				}

			}
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("most_common_oov.txt"), "UTF8"));
			Collections.sort(oov, new Comparator<Word>() {
				@Override
				public int compare(Word arg0, Word arg1) {
					return arg1.getFrequency() - arg0.getFrequency();
				}
			});
			for (Word word : oov) {
				bw.write(word.getWord() + " " + word.getFrequency());
			    bw.newLine();
				
			}
			
			bw.close();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(oov.size());
	}

}
