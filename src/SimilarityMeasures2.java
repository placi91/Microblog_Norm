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
import java.util.Map.Entry;

public class SimilarityMeasures2 {

	private static String vectorspace = "all_word_vectors_ignore_stopw.txt";


	public static void main(String[] args) {

		try {
			ArrayList<Word> words = new ArrayList<>();

			System.out.println("Loading vector space into memory...");
			loadVectorSpace(words);

			System.out.println("Writing to context_size.txt");
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream("context_size.txt"), "UTF-8"));
			
			Collections.sort(words, new Comparator<Word>() {
				@Override
				public int compare(Word arg0, Word arg1) {
					return arg1.getContextSet().size() - arg0.getContextSet().size();
				}
			});
			
			for (Word word : words) {
				bw.write(word.getFrequency() + "\t" + word.getContextSet().size() + "\n");
				bw.flush();
			}
			
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void loadVectorSpace(ArrayList<Word> words) {
		try {
			BufferedReader br = new BufferedReader(new FileReader("vectorspace/" + vectorspace));
			int row = 1;
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(row++);
				String[] parts = line.split("\t");
				String wordString = parts[0];
				int frequency = Integer.parseInt(parts[1]);
				String cluster = parts[2];
				String type = parts[3];
				Word word = new Word(wordString, type, cluster, frequency);
				HashMap<Integer, Integer> contextMap = new HashMap<>();
				for (int i = 4; i < parts.length; i += 2) {
					int contextNum = Integer.parseInt(parts[i]);
					int contextFreq = Integer.parseInt(parts[i + 1]);
					contextMap.put(contextNum, contextFreq);
				}
				word.setContextMap(contextMap);
				words.add(word);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
