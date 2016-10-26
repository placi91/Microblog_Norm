import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VectorSpace {

	public static void main(String[] args) {

		HashMap<String, Integer> contextWords = new HashMap<>();
		HashMap<String, Word> words = new HashMap<>();
		
		Pattern pattern = Pattern.compile("[a-z]");
		int s = 1;
		try {
			BufferedReader br = new BufferedReader(new FileReader("paths_lemmas_put"));
			HashMap<String, String> clusters = new HashMap<>();
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\t");
				String clusterName = parts[0].trim();
				String clusterWord = parts[1].trim();
				clusters.put(clusterWord, clusterName);
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
				accents.put(parts[0].trim(), parts[1].trim());
			}
			br.close();
			
			br = new BufferedReader(new FileReader("stopwords.txt"));
			HashSet<String> stopwords = new HashSet<>();
			while ((line = br.readLine()) != null) {
				stopwords.add(line.trim());
			}
			br.close();

			br = new BufferedReader(new FileReader("tweets_pruned_lemmas_put.txt"));
			while ((line = br.readLine()) != null) {
				System.out.println(s++);
				String[] parts = line.split(" ");
				for (int i = 0; i < parts.length; ++i) {
					String word = parts[i].trim();
					if (word.isEmpty() || word.startsWith("#") || word.startsWith("@") || word.contains("http")) {
						continue;
					}
					Matcher m = pattern.matcher(word);
					if (!m.find()|| word.length() < 3 || !clusters.containsKey(word)) {
						continue;
					}
					
					Word w;
					if (words.containsKey(word)) {
						w = words.get(word);
						w.setFrequency(w.getFrequency() + 1);
					} else {
						words.put(word, new Word(word, 1));
						w = words.get(word);
					}
					int left = i - 1, right = i + 1;
					int lengthLeft = 0, lengthRight = 0;
					while ((left >= 0 || right < parts.length)) {
						if (left >= 0 && !parts[left].isEmpty() && lengthLeft <= 4
								&& !stopwords.contains(parts[left])) {
							String leftWord = parts[left];
							m = pattern.matcher(leftWord);
							if((m.find() || leftWord.contains(":")) && leftWord.length() >= 2) {
								if (accents.containsKey(leftWord)) {
									leftWord = accents.get(leftWord);
								}
								if (!contextWords.containsKey(leftWord)) {
									Integer wordNumber = new Integer(contextWords.size());
									w.addContext(wordNumber);
									contextWords.put(leftWord, wordNumber);
								} else if (!w.containsContext(contextWords.get(leftWord))) {
									w.addContext(contextWords.get(leftWord));
								} else {
									w.updateContext(contextWords.get(leftWord));
								}
								++lengthLeft;
							}
						}
						if (right < parts.length && !parts[right].isEmpty() && lengthRight <= 4
								&& !stopwords.contains(parts[right])) {
							String rightWord = parts[right];
							m = pattern.matcher(rightWord);
							if((m.find() || rightWord.contains(":")) && rightWord.length() >= 2) {
								if (accents.containsKey(rightWord)) {
									rightWord = accents.get(rightWord);
								}
								if (!contextWords.containsKey(rightWord)) {
									Integer wordNumber = new Integer(contextWords.size());
									w.addContext(wordNumber);
									contextWords.put(rightWord, wordNumber);
								} else if (!w.containsContext(contextWords.get(rightWord))) {
									w.addContext(contextWords.get(rightWord));
								} else {
									w.updateContext(contextWords.get(rightWord));
								}
								++lengthRight;
							}
						}
						++right;
						--left;
					}
				}
			}
			br.close();

			HashMap<String, Word> oovWords = new HashMap<>();
			br = new BufferedReader(new FileReader("oov_lemmas_put.txt"));
			while ((line = br.readLine()) != null) {
				String oov = line.toLowerCase().trim();
				oovWords.put(oov, new Word(false));
			}
			br.close();
			
			/*br = new BufferedReader(new FileReader("most_common_oov_lemmas_put.txt"));
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(" ");
				String oov = parts[0].toLowerCase().trim();
				if(!oovWords.containsKey(oov)) 
					System.err.println(oov.toUpperCase());
				oovWords.get(oov).setCommon(true);
			}
			br.close();*/
			
			br = new BufferedReader(new FileReader("test_same_cluster.txt"));
			while ((line = br.readLine()) != null) {
				line = line.toLowerCase();
				String[] parts = line.split(" ");
				String testString = parts[0].trim();
				oovWords.put(testString, new Word(true));
			}
			br.close();
			
			HashMap<String, Word> words2 = new HashMap<>();
			for (Entry<String, Word> wordEntry : words.entrySet()) {
				Word word = wordEntry.getValue();
				String wordString = wordEntry.getKey();
				if(word.getFrequency() < 5) {
					continue;
				}
				if (clusters.containsKey(wordString)) {
					String clusterName = clusters.get(wordString);
					word.setCluster(clusterName);
					if (!oovWords.containsKey(wordString) && wordString.matches("[a-zíéáöőóúűü]+")) {
						word.setType("IV");
						words2.put(wordString, word);
					} else if (oovWords.containsKey(wordString) && oovWords.get(wordString).isCommon()) {
						word.setType("OOV");
						words2.put(wordString, word);
					}
				}

			}
			words.clear();
			clusters.clear();
			oovWords.clear();
			System.gc();

			String file = "word_sameclust_vectors_ignore_stopw.txt";
			System.out.println("Writing to " + file);
			System.out.println(words2.size());
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream("vectorspace/" + file), "UTF-8"));			
			
			for (Entry<String, Word> w : words2.entrySet()) {
				String wordString = w.getKey();
				Word word = w.getValue();
				bw.write(wordString + "\t" + word.getFrequency() + "\t" + word.getCluster() + "\t" + word.getType());
				HashMap<Integer, Integer> contextMap = word.getContextMap();
				for (Entry<Integer, Integer> contextWord : contextMap.entrySet()) {
					Integer contextNum = contextWord.getKey();
					Integer contextFreq = contextWord.getValue();
					bw.write("\t" + contextNum + "\t" + contextFreq);
				}
				bw.newLine();
				bw.flush();
			}
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
    
}