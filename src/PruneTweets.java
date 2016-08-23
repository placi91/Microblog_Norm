import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PruneTweets {

	public static void main(String[] args) {

		try {
			BufferedReader br = new BufferedReader(new FileReader("hun_tweets_2015_10_2016_02.tokenized"));
			HashSet<String> lines = new HashSet<>();
			String line;
			while ((line = br.readLine()) != null) {
				line = line.toLowerCase();
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
			
			BufferedWriter out = new BufferedWriter(new FileWriter("tweets_pruned_accents_lemmas_put.txt"));
			
			for (String line2 : lines) {
				line2 = line2.replaceAll("\\!+", "!");
				line2 = line2.replaceAll("\\?+", "?");
				line2 = line2.replaceAll("(\\?\\!)+\\?*", "?!");
				line2 = line2.replaceAll("(\\!\\?)+\\!*", "?!");
				String[] parts = line2.split(" ");
				for (int i = 0; i < parts.length; ++i) {
					if((parts[i].contains("...") || parts[i].contains("…")) && i >= parts.length - 3) {
						parts[i] = "";
					} 
					if(parts[i].contains("http")) {
						parts[i] = "http";
					}
					boolean hashtag = false;
					if(parts[i].startsWith("#")) {
						hashtag = true;
						parts[i] = parts[i].substring(1);
					}
					if(accents.containsKey(parts[i])){
						parts[i] = accents.get(parts[i]);
					}
					if(lemmas.containsKey(parts[i])){
						parts[i] = lemmas.get(parts[i]);
					}
					if(hashtag) {
						parts[i] = "#" + parts[i];
					}
					
					if(i > 1 && parts[i].equals("):") && parts[i-1].equals("@mention") && parts[i-2].equals("(")) {
						parts[i] = ")";
					}

					if(!parts[i].contains("http")) {
						parts[i] = parts[i].replaceAll(":+ *-*/+", ":/");
						parts[i] = parts[i].replaceAll("o+ *-*:+", ":o");
						parts[i] = parts[i].replaceAll("p+ *-*:+", ":p");
						parts[i] = parts[i].replaceAll("d+ *-*:+", ":d");
						parts[i] = parts[i].replaceAll("s+ *-*:+", ":s");
					}
					
					if(parts[i].matches("\\d+(?::\\d+){1,2}")) {
						parts[i] = "time:time";
					} else {
						parts[i] = parts[i].replaceAll(":+ *-*3+", ":3");
					}
					
					parts[i] = parts[i].replaceAll("\\)+ *-*:+", ":(");
					parts[i] = parts[i].replaceAll("\\(+ *-*:+", ":)");
					parts[i] = parts[i].replaceAll("\\(+ *-*;+", ";)");
					parts[i] = parts[i].replaceAll("/+ *-*:+", ":/");
					parts[i] = parts[i].replaceAll("\\|+ *-*:+", ":|");
					
					parts[i] = parts[i].replaceAll(":+ *-*\\\\+", ":/");
					parts[i] = parts[i].replaceAll(":+ *-*\\|+", ":|");
					parts[i] = parts[i].replaceAll(":+ *-*o+", ":o");
					parts[i] = parts[i].replaceAll(":+ *-*d+", ":d");
					parts[i] = parts[i].replaceAll(":+ *-*s+", ":s");
					parts[i] = parts[i].replaceAll(":+ *-*p+", ":p");
					
					parts[i] = parts[i].replaceAll(";+ *-*o+", ":o");
					parts[i] = parts[i].replaceAll(";+ *-*d+", ":d");
					parts[i] = parts[i].replaceAll(";+ *-*s+", ":s");
					parts[i] = parts[i].replaceAll(";+ *-*p+", ":p");
					parts[i] = parts[i].replaceAll(";+ *-*\\)+", ";)");
					parts[i] = parts[i].replaceAll(":+ *-*\\(+", ":(");
					parts[i] = parts[i].replaceAll(":+ *-* *\\)+", ":)");
					parts[i] = parts[i].replaceAll("x+d+", "xd");
					
					if(!parts[i].isEmpty()) {
						out.write(parts[i] + " ");
					}
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