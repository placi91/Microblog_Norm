import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
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
				String p0 = parts[0].trim(), p2 = parts[2].trim(); 
				if(p2.equals("WARNING") || (p0.equals(p2) && parts.length == 3)) {
					continue;
				}
				if(!p0.equals(p2)) {
					lemmas.put(p0, p2);
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
			
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("tweets_pruned_accents_lemmas_put.txt"), "UTF-8"));
			
			for (String line2 : lines) {
				line2 = line2.replaceAll("\\!+", "!");
				line2 = line2.replaceAll("\\?+", "?");
				line2 = line2.replaceAll("(\\?\\!)+\\?*", "?!");
				line2 = line2.replaceAll("(\\!\\?)+\\!*", "?!");
				String[] parts = line2.split(" ");
				for (int i = 0; i < parts.length; ++i) {
					String word = parts[i];
					if ((word.contains("...") || word.contains("…")) && i >= parts.length - 3) {
						word = "";
					}
					if (word.contains("http")) {
						word = "http";
					}
					boolean hashtag = false;
					if (word.startsWith("#")) {
						hashtag = true;
						word = word.substring(1);
					}
					if (accents.containsKey(word)) {
						word = accents.get(word);
					}
					if (lemmas.containsKey(word)) {
						word = lemmas.get(word);
					}
					if (hashtag) {
						word = "#" + word;
					}

					if (i > 1 && word.equals("):") && parts[i - 1].equals("@mention") && parts[i - 2].equals("(")) {
						word = ")";
					}

					if (!word.contains("http")) {
						word = word.replaceAll(":+ *-*/+", ":/");
						word = word.replaceAll("o+ *-*:+", ":o");
						word = word.replaceAll("p+ *-*:+", ":p");
						word = word.replaceAll("d+ *-*:+", ":d");
						word = word.replaceAll("s+ *-*:+", ":s");
					}

					if (word.matches("\\d+(?::\\d+){1,2}")) {
						word = "time:time";
					} else {
						word = word.replaceAll(":+ *-*3+", ":3");
					}

					word = word.replaceAll("\\)+ *-*:+", ":(");
					word = word.replaceAll("\\(+ *-*:+", ":)");
					word = word.replaceAll("\\(+ *-*;+", ";)");
					word = word.replaceAll("/+ *-*:+", ":/");
					word = word.replaceAll("\\|+ *-*:+", ":|");

					word = word.replaceAll(":+ *-*\\\\+", ":/");
					word = word.replaceAll(":+ *-*\\|+", ":|");
					word = word.replaceAll(":+ *-*o+", ":o");
					word = word.replaceAll(":+ *-*d+", ":d");
					word = word.replaceAll(":+ *-*s+", ":s");
					word = word.replaceAll(":+ *-*p+", ":p");

					word = word.replaceAll(";+ *-*o+", ":o");
					word = word.replaceAll(";+ *-*d+", ":d");
					word = word.replaceAll(";+ *-*s+", ":s");
					word = word.replaceAll(";+ *-*p+", ":p");
					word = word.replaceAll(";+ *-*\\)+", ";)");
					word = word.replaceAll(":+ *-*\\(+", ":(");
					word = word.replaceAll(":+ *-* *\\)+", ":)");
					word = word.replaceAll("x+d+", "xd");

					if (!word.isEmpty()) {
						out.write(word + " ");
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