import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Jaccard {

	public static void main(String[] args) {
		
		HashMap<String, Integer> contextWords = new HashMap<>();
		HashMap<String, Word> words = new HashMap<>();
		HashMap<String, String> lemmas = new HashMap<>();
		
		Pattern pattern = Pattern.compile("\\p{IsAlphabetic}+");
		int s = 1;
		try {
			BufferedReader br = new BufferedReader(new FileReader("hun_tweets_2015_10_2016_02.lemmas"));
			String line;
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
			
			br = new BufferedReader(new FileReader("tweets_pruned.txt"));
			while ((line = br.readLine()) != null) {
				System.out.println(s++);
				String[] parts = line.split(" ");
				for (int i = 0; i < parts.length; ++i) {
					if (parts[i].contains("http")) {
						parts[i] = "";
					} 
				}
				for (int i = 0; i < parts.length; ++i) {
					String word = parts[i].trim();
					if (word.isEmpty() || word.startsWith("#") || word.startsWith("@")) {
						continue;
					}

					Matcher m = pattern.matcher(word);
					if (m.find()) {
						Word w;
						if (words.containsKey(word)) {
							w = words.get(word);
							w.setFrequent(w.getFrequent() + 1);
						} else {
							words.put(word, new Word(word, 1));
							w = words.get(word);
						}
						int left = i, right = i;
						--left;
						++right;
						int length = 0;
						while ((left >= 0 || right < parts.length) && length < 6) {
							if (left >= 0 && !parts[left].isEmpty()) {
								String string = parts[left];
								if(lemmas.containsKey(string)){
									string = lemmas.get(string);
								}
								if (!contextWords.containsKey(string)) {
									w.add((Integer) contextWords.size());
									contextWords.put(string, contextWords.size());
								} else if(!w.getContextSet().contains(contextWords.get(string))){
									w.add(contextWords.get(string));
								}
							}
							if (right < parts.length && !parts[right].isEmpty()) {
								String string = parts[right];
								if(lemmas.containsKey(string)){
									string = lemmas.get(string);
								}
								if (!contextWords.containsKey(string)) {
									w.add((Integer) contextWords.size());
									contextWords.put(string, contextWords.size());
								} else if(!w.getContextSet().contains(contextWords.get(string))){
									w.add(contextWords.get(string));
								}
							}
							++length;
							++right;
							--left;
						}
					}
				}
			}
			br.close();
			lemmas.clear();
			
			HashMap<String, HashMap<String, Integer>> clusters = new HashMap<>();
			br = new BufferedReader(new FileReader("clusters.txt"));
			String clusterName = null;
			while ((line = br.readLine()) != null) {
				if(line.isEmpty()) {
					continue;
				}
				String[] parts = line.split(" ");
				if(line.contains("cluster:")) {
					clusterName = parts[1].trim();
					clusters.put(clusterName, new HashMap<String, Integer>());
				} else {
					HashMap<String, Integer> cluster = clusters.get(clusterName);
					if(!clusters.containsKey(clusterName)) {
						System.err.println("WARNING");
					}
					cluster.put(parts[0], Integer.parseInt(parts[1]));
				}
			}
			br.close();
			
			HashMap<String, Boolean> oov = new HashMap<>();
			
			br = new BufferedReader(new FileReader("oov.txt"));
			while ((line = br.readLine()) != null) {
				line = line.toLowerCase();
				oov.put(line.trim(), false);
			}
			br.close();
			
			br = new BufferedReader(new FileReader("norm.txt"));
			while ((line = br.readLine()) != null) {
				//String[] parts = line.split(" ");
				line = line.toLowerCase();
				oov.put(line.trim(), true);
			}
			br.close();
			
			HashMap<String, Word> OVwords = new HashMap<>();
			HashMap<String, HashMap<String, Word>> IVwords = new HashMap<>();
			for (Entry<String, Word> w : words.entrySet()) {
				if (!oov.containsKey(w.getKey())) {
					String cname = null;
					for (Entry<String, HashMap<String, Integer>> c : clusters.entrySet()) {
						if(c.getValue().containsKey(w.getKey())) {
							cname = c.getKey();
							break;
						} 
					}
					HashMap<String, Word> map = new HashMap<>();
					if(cname != null) {
						if(IVwords.containsKey(cname)) {
							map = IVwords.get(cname);
						}
						map.put(w.getKey(), w.getValue());
						IVwords.put(cname, map);
					}
					
				} else if (oov.get(w.getKey())) {
					for (Entry<String, HashMap<String, Integer>> c : clusters.entrySet()) {
						if(c.getValue().containsKey(w.getKey())) {
							w.getValue().setCluster(c.getKey());
							break;
						} 
					}
					OVwords.put(w.getKey(), w.getValue());
				}
			}
			words.clear();
			clusters.clear();
			System.gc();

			System.out.println("Writing to jaccard.txt");
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("jaccard.txt"), "UTF8"));
			s = 1;
			int z = 1;

			Set<Integer> oovContext = new HashSet<>();
			Set<Integer> ivContext = new HashSet<>();
			Set<Integer> common = new HashSet<>();

			for (Entry<String, Word> out : OVwords.entrySet()) {
				String cluster = out.getValue().getCluster();
				if(cluster == null)
					continue;
				Word[] max = new Word[5];
				for (int i = 0; i < max.length; ++i) {
					max[i] = new Word("empty", 0);
				}
				oovContext.addAll(out.getValue().getContextSet());
				HashMap<String, Word> inVocWords = IVwords.get(cluster);
				for (Entry<String, Word> in : inVocWords.entrySet()) {
					Word iv = in.getValue();
					double size;
					if (oovContext.size() < iv.getContextSet().size()) {
						size = ((double) oovContext.size()) / iv.getContextSet().size();
					} else {
						size = ((double) iv.getContextSet().size()) / oovContext.size();
					}
					if (size <= 0.04) {
						continue;
					}
					ivContext.addAll(iv.getContextSet());
					common.addAll(oovContext);
					common.retainAll(ivContext);
					if (common.size() == 0) {
						continue;
					}
					System.out.println(s++ + " oov " + out.getKey());
					System.out.println(z++ + " iv " + in.getKey());

					ivContext.addAll(oovContext);
					double jaccard = ((double) common.size())/ ivContext.size();
					System.out.println("jaccard: " + jaccard);
					ivContext.clear();
					common.clear();
					
					if (jaccard > max[4].getJaccard()) {
						max[4].setJaccard(jaccard);
						max[4].setWord(iv.getWord());
					}
					Arrays.sort(max, new Comparator<Word>() {
						@Override
						public int compare(Word arg0, Word arg1) {
							return Double.compare(arg1.getJaccard(), arg0.getJaccard());
						}
					});
				}
				z = 1;
				bw.write(out.getKey());
				for (Word word : max) {
					bw.write("\t" + word.getWord() + "\t" + word.getJaccard());
					System.out.println("\t" + word.getWord() + "\t" + word.getJaccard());
				}
				bw.newLine();
				bw.flush();
				oovContext.clear();
			}
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
