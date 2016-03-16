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


public class Copy {

	public static void main(String[] args) {

		try {
			BufferedReader br = new BufferedReader(new FileReader("hun_tweets_2015_10_2016_02.tokenized"));
			BufferedWriter out = new BufferedWriter(new FileWriter("tweets_pruned.txt"));
			HashSet<String> lines = new HashSet<>();
			HashMap<String, Integer> lines2 = new HashMap<>();
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("rt ")) {
					line = line.replaceFirst("rt ", "");
				}
				if(!lines.contains(line)) {
					lines.add(line);
				} 
				
			}
			for (String line2 : lines) {
				String[] parts = line2.split(" ");
				for (int i = 0; i < parts.length; ++i) {
					if(parts[i].startsWith("@")) {
						parts[i] = "@mention";
					} else if((parts[i].contains("...") || parts[i].contains("…")) && i >= parts.length - 3) {
						parts[i] = "";
					}
					String s = parts[i];
					if(!s.equals(parts[i])) {
						System.out.println(line2);
						System.out.println(s);
						System.out.println(parts[i]);
						System.out.println();
					}
					if(!parts[i].contains("http"))
						parts[i] = parts[i].replaceAll(":+ *-*/+", ":/");
					
					parts[i] = parts[i].replaceAll("\\(+ *-*:+", ":)");
					parts[i] = parts[i].replaceAll(":+ *-*\\\\+", ":\\\\");
					parts[i] = parts[i].replaceAll(":+ *-*\\|+", ":|");
					parts[i] = parts[i].replaceAll(":+ *-*o+", ":o");
					parts[i] = parts[i].replaceAll(":+ *-*d+", ":d");
					parts[i] = parts[i].replaceAll(":+ *-*\\(+", ":(");
					parts[i] = parts[i].replaceAll(":+ *-*\\)+", ":)");
					parts[i] = parts[i].replaceAll("x+d+", "xd");
					
					
					if(!parts[i].isEmpty())
						out.write(parts[i]);
					if(i != parts.length-1)
						out.write(" ");
				}
				out.newLine();
				out.flush();
			}
			br.close();
			out.close();
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		
	}
	

}
