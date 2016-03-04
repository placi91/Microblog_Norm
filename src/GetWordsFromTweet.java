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
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetWordsFromTweet {

	public static void main(String[] args) {
		HashMap<String, Integer> words = new HashMap<>();
		Pattern pattern = Pattern.compile("\\p{IsAlphabetic}+");
		int s = 1;
		try {
			BufferedReader br = new BufferedReader(new FileReader("hun_tweets_2015_10_2016_02.tokenized"));
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(s++);
				
				if (line.contains("tetszett egy videó ( @youtube ):") || line.contains("egy videója ( @youtube ):") 
					|| line.startsWith("rt ")) {
					continue;
				}
				
				line = line.toUpperCase();
				String[] parts = line.split(" ");
				for (int i = 0; i < parts.length; ++i) {
					if (parts[i].contains("HTTP") || parts[i].startsWith("#") || parts[i].startsWith("@")) {
						parts[i] = "";
					} 
					parts[i] = parts[i].replaceAll("[^\\p{IsAlphabetic}0-9-]", "");
				}
				for (int i = 0; i < parts.length; ++i) {
					String word = parts[i].trim();
					if (word.isEmpty()) {
						continue;
					}

					Matcher m = pattern.matcher(word);
					if (m.find()) {
						if (word.length() < 4) {
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
