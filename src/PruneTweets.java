import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class PruneTweets {

	public static void main(String[] args) {

		try {
			BufferedReader br = new BufferedReader(new FileReader("hun_tweets_2015_10_2016_02.tokenized"));
			HashSet<String> lines = new HashSet<>();
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("rt ")) {
					line = line.replaceFirst("rt ", "");
				}
				line = line.replaceAll("@[\\p{IsAlphabetic}0-9_]+", "@mention");
				if(!lines.contains(line)) {
					lines.add(line);
				} 
			}
			br.close();
			
			br = new BufferedReader(new FileReader("hun_tweets_2015_10_2016_02.lemmas"));
			HashMap<String, String> lemmas = new HashMap<>();
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\t");
				if(parts[2].equals("WARNING") || (parts[0].equals(parts[2]) && parts.length == 3)) {
					continue;
				}
				if(!parts[0].equals(parts[2])) {
					lemmas.put(parts[0], parts[2]);
				}
			}
			br.close();
	
			br = new BufferedReader(new FileReader("accents.txt"));
			HashMap<String, String> accents = new HashMap<>();
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(" ");
				accents.put(parts[0], parts[1]);
			}
			br.close();

			BufferedWriter out = new BufferedWriter(new FileWriter("tweets_pruned.txt"));
			for (String line2 : lines) {
				String[] parts = line2.split(" ");
				for (int i = 0; i < parts.length; ++i) {
					if((parts[i].contains("...") || parts[i].contains("…")) && i >= parts.length - 3) {
						parts[i] = "";
					} else if(parts[i].contains("http")) {
						parts[i] = "";
					}
					if(accents.containsKey(parts[i])){
						parts[i] = accents.get(parts[i]);
					}
					if(lemmas.containsKey(parts[i])){
						parts[i] = lemmas.get(parts[i]);
					}

					if(!parts[i].contains("http"))
						parts[i] = parts[i].replaceAll(":+ *-*/+", ":/");
					
					parts[i] = parts[i].replaceAll("\\(+ *-*:+", ":)");
					parts[i] = parts[i].replaceAll(":+ *-*\\\\+", ":\\\\");
					parts[i] = parts[i].replaceAll(":+ *-*\\|+", ":|");
					parts[i] = parts[i].replaceAll(":+ *-*o+", ":o");
					parts[i] = parts[i].replaceAll(":+ *-*d+", ":d");
					parts[i] = parts[i].replaceAll(":+ *-*s+", ":s");
					parts[i] = parts[i].replaceAll(":+ *-*p+", ":p");
					parts[i] = parts[i].replaceAll(":+ *-*\\(+", ":(");
					parts[i] = parts[i].replaceAll(":+ *-*\\)+", ":)");
					parts[i] = parts[i].replaceAll(";+ *-*\\)+", ";)");
					parts[i] = parts[i].replaceAll("x+d+", "xd");
					
					if(!parts[i].isEmpty())
						out.write(parts[i]);
					if(i != parts.length-1)
						out.write(" ");
				}
				out.newLine();
				out.flush();
			}
			out.close();
			
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

}
