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
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetWordsFromLemmas {

	public static void main(String[] args) {

		HashMap<String, Integer> words = new HashMap<>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader("hun_tweets_2015_10_2016_02.lemmas"));
			String line;
			while ((line = br.readLine()) != null) {
				line = line.toUpperCase();
				String[] parts = line.split("\t");
				String word = parts[0];
				words.put(word, Integer.parseInt(parts[1]));
			}
			
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("words.txt"), "UTF8"));

			for (Entry<String, Integer> out : words.entrySet()) {
				bw.write(out.getKey() + " " + out.getValue());
		    	bw.newLine();
			}
			
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(words.size());
	}

}
