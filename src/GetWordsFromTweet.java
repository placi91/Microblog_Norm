import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetWordsFromTweet {

	public static void main(String[] args) {
		Map<String, Integer> words = new TreeMap<>();
		int s = 1;
		try {
			BufferedReader br = new BufferedReader(new FileReader("tweets_pruned_accents_lemmas_put.txt"));
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(s++);
				line = line.toUpperCase();
				String[] parts = line.split(" ");
				for (int i = 0; i < parts.length; ++i) {
					String word = parts[i].trim();
					if (word.contains("HTTP") || word.startsWith("#") || word.startsWith("@") || word.isEmpty()) {
						continue;
					}
					if (word.matches("[A-ZÍÉÁŰÚŐÓÜÖ]+")) {
						if (word.length() < 3) {
							continue;
						}
						if (words.containsKey(word)) {
							words.put(word, words.get(word) + 1);
						} else {
							words.put(word, 1);
						}
					}
				}
			}
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("words.txt"), "UTF8"));
			for (Entry<String, Integer> out : words.entrySet()) {
				if(out.getValue() < 5) {
					continue;
				}
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
