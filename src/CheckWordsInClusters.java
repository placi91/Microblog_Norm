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
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckWordsInClusters {

	public static void main(String[] args) {

		try {
			BufferedReader br = new BufferedReader(new FileReader("paths_lemmas_put"));
			HashMap<String, HashSet<String>> clusters = new HashMap<>();
			HashSet<String> clusterSet = new HashSet<>();
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\t");
				String clusterName = parts[0].trim();
				String clusterWord = parts[1].trim();
				clusterSet.add(clusterWord);
				if (!clusters.containsKey(clusterName)) {
					clusters.put(clusterName, new HashSet<String>());
				}
				HashSet<String> clusterWords = clusters.get(clusterName);
				clusterWords.add(clusterWord);
			}
			br.close();
			
			HashMap<String, Word> test = new HashMap<>();
			br = new BufferedReader(new FileReader("test.txt"));
			while ((line = br.readLine()) != null) {
				line = line.toLowerCase();
				String[] parts = line.split(" ");
				HashSet<String> pairs = new HashSet<>();
				for (int j = 1; j < parts.length; j++) {
					pairs.add(parts[j].trim());
				}
				test.put(parts[0].trim(), new Word(true, pairs));
			}
			br.close();

			BufferedWriter writeSameCluster = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream("test_same_cluster.txt"), "UTF-8"));
			BufferedWriter writeDiffCluster = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream("test_diff_cluster.txt"), "UTF-8"));
			
			
			int clusterPairs = 0;
			for (Entry<String, Word> t : test.entrySet()) {
				Word testWord = t.getValue();
				String testString = t.getKey();
				
				if(!clusterSet.contains(testString)) {
					System.err.println("Not in cluster: " + testString);
				}
				boolean inCluster = false;
				HashSet<String> pairs = testWord.getPairs();
				for (Entry<String, HashSet<String>> c : clusters.entrySet()) {
					HashSet<String> clusterWords = c.getValue();
					if (clusterWords.contains(testString)) {
						for (String pair : pairs) {
							if (clusterWords.contains(pair)) {
								inCluster = true;
							} 						
						}
						break;
					}
				}
				if (inCluster) {
					writeSameCluster.write(testString.toUpperCase());
					clusterPairs++;
					for (String pair : pairs) {
						writeSameCluster.write(" " + pair.toUpperCase()); 						
					}
					writeSameCluster.newLine();
				} else {
					writeDiffCluster.write(testString.toUpperCase());
					for (String pair : pairs) {
						writeDiffCluster.write(" " + pair.toUpperCase()); 						
					}
					writeDiffCluster.newLine();
				}
			} 
			
			double testSize = (double)test.entrySet().size();
			double score = ((double)clusterPairs) / testSize * 100;
			System.out.println("Test size: " + testSize);
			System.out.println("Pairs in same cluster: " + score + " number pairs: " + clusterPairs);
			writeSameCluster.close();
			writeDiffCluster.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

