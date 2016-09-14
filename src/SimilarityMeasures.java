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

public class SimilarityMeasures {

	public static void main(String[] args) {

		HashMap<String, Integer> contextWords = new HashMap<>();
		HashMap<String, Word> words = new HashMap<>();
		
		Pattern pattern = Pattern.compile("[a-z]"); // \\p{IsAlphabetic}+ [a-zíéáűüúöőó]{3}
		int s = 1;
		try {
			BufferedReader br = new BufferedReader(new FileReader("paths_lemmas_put"));
			HashMap<String, HashSet<String>> clusters = new HashMap<>();
			HashSet<String> clusterSet = new HashSet<>();
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\t");
				clusterSet.add(parts[1]);
				String clusterName = parts[0];
				if (!clusters.containsKey(clusterName)) {
					clusters.put(clusterName, new HashSet<String>());
				}
				HashSet<String> cluster = clusters.get(clusterName);
				cluster.add(parts[1]);
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
					if (!m.find()|| word.length() < 3 || !clusterSet.contains(word)) {
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
					int lengthLeft = 0;
					int lengthRight = 0;
					while ((left >= 0 || right < parts.length)) {
						if (left >= 0 && !parts[left].isEmpty() && lengthLeft <= 4) {
							String leftWord = parts[left];
							m = pattern.matcher(leftWord);
							if((m.find() && leftWord.length() >= 2) || leftWord.contains(":")) {
								if (accents.containsKey(leftWord)) {
									leftWord = accents.get(leftWord);
								}
								if (lemmas.containsKey(leftWord)) {
									//leftWord = lemmas.get(leftWord);
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
							}
							if(leftWord.length() >= 2)
								++lengthLeft;
						}
						if (right < parts.length && !parts[right].isEmpty() && lengthRight <= 4) {
							String rightWord = parts[right];
							m = pattern.matcher(rightWord);
							if((m.find() && rightWord.length() >= 2) || rightWord.contains(":")) {
								if (accents.containsKey(rightWord)) {
									rightWord = accents.get(rightWord);
								}
								if (lemmas.containsKey(rightWord)) {
									//rightWord = lemmas.get(rightWord);
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
							}
							if(rightWord.length() >= 2)
								++lengthRight;
						}
						++right;
						--left;
					}
				}
			}
			br.close();

			HashMap<String, Word> oovWords = new HashMap<>();
			HashMap<String, Boolean> test = new HashMap<>();
			br = new BufferedReader(new FileReader("oov_lemmas_put.txt"));
			while ((line = br.readLine()) != null) {
				line = line.toLowerCase();
				oovWords.put(line.trim(), new Word(false));
			}
			br.close();

			br = new BufferedReader(new FileReader("test.txt"));
			while ((line = br.readLine()) != null) {
				line = line.toLowerCase();
				String[] parts = line.split(" ");
				HashSet<String> pairs = new HashSet<>();
				for (int j = 1; j < parts.length; j++) {
					pairs.add(parts[j].trim());
				}
				oovWords.put(parts[0].trim(), new Word(true, pairs));
				test.put(parts[0].trim(), true);
			}
			br.close();
			
			HashMap<String, Word> OVwords = new HashMap<>();
			HashMap<String, HashMap<String, Word>> IVwords = new HashMap<>();
			for (Entry<String, Word> w : words.entrySet()) {
				if(w.getValue().getFrequency() < 5) {
					continue;
				}
				if (!oovWords.containsKey(w.getKey()) && w.getKey().matches("[a-zíéáöőóúűü]+")) {
					String cname = null;
					for (Entry<String, HashSet<String>> c : clusters.entrySet()) {
						if (c.getValue().contains(w.getKey())) {
							cname = c.getKey();
							break;
						}
					}
					HashMap<String, Word> clusterWords;
					if (cname != null) {
						if (IVwords.containsKey(cname)) {
							clusterWords = IVwords.get(cname);
							clusterWords.put(w.getKey(), w.getValue());
						} else {
							clusterWords = new HashMap<>();
							clusterWords.put(w.getKey(), w.getValue());
							IVwords.put(cname, clusterWords);
						}
					}

				} else if (oovWords.containsKey(w.getKey()) && oovWords.get(w.getKey()).isCommon()) {
					for (Entry<String, HashSet<String>> c : clusters.entrySet()) {
						if (c.getValue().contains(w.getKey())) {
							w.getValue().setCluster(c.getKey());
							break;
						}
					}
					HashSet<String> pairs = oovWords.get(w.getKey()).getPairs();
					if (pairs != null) {
						w.getValue().setPairs(pairs);
					}
					OVwords.put(w.getKey(), w.getValue());
				}
			}
			words.clear();
			clusters.clear();
			oovWords.clear();
			System.gc();

			System.out.println("Writing to jaccard.txt");
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream("similarity.txt"), "UTF-8"));
			s = 1;
			int z = 1;
			int correctJacPairs = 0;
			int correctAnglePairs = 0;
			
			
			for (Entry<String, Word> out : OVwords.entrySet()) {
				Word oov = out.getValue();
				String clusterOOV = oov.getCluster();
				if (clusterOOV == null) {
					continue;
				}
				Set<Integer> oovContext = oov.getContextSet();
				
				ArrayList<Word> maxJaccard = new ArrayList<>();
				ArrayList<Word> minAngles = new ArrayList<>();
				initWords(maxJaccard);
				initWords(minAngles);
				
				for (Entry<String, HashMap<String, Word>> cIV : IVwords.entrySet()) {
					String clusterIV = cIV.getKey();
					int ham = hamming(clusterIV.toCharArray(), clusterOOV.toCharArray());
					double a = 1.0, b = 1.0;
					if (ham > 2 || ham == -1) {
						continue;
					}
					if (ham != 0) {
						//a = 1.0 / ((double) 1.0 + ham);
						//b = 1.0 * ((double) 1.0 + ham);
					}
					if(!clusterOOV.equals(clusterIV)) {
						continue;
					}
					HashMap<String, Word> inVocWords = cIV.getValue();
					for (Entry<String, Word> in : inVocWords.entrySet()) {
						Word iv = in.getValue();
						System.out.println(s++ + " oov " + out.getKey());
						System.out.println(z++ + " iv " + in.getKey());
						double size;
						if (oovContext.size() < iv.getContextSet().size()) {
							size = ((double) oovContext.size()) / iv.getContextSet().size();
						} else {
							size = ((double) iv.getContextSet().size()) / oovContext.size();
						}
						if (size <= 0.02) {
							continue;
						}
						Set<Integer> ivContext = new HashSet<>();
						Set<Integer> common = new HashSet<>();
						ivContext.addAll(iv.getContextSet());
						common.addAll(oovContext);
						common.retainAll(ivContext);
						if (common.size() < 2) {
							continue;
						}
						System.out.println(s++ + " oov " + out.getKey());
						System.out.println(z++ + " iv " + in.getKey());
						
						double product = 0.0, lengthOOV = 0.0, lengthIV = 0.0;
						for (Integer commonWord : common) {
							double oovFreq = oov.getContextFrequency(commonWord);
							double ivFreq = iv.getContextFrequency(commonWord);
							product += oovFreq  * ivFreq ; 
						}
						for (Integer ivContextWord : ivContext) {
							double ivFreq = iv.getContextFrequency(ivContextWord);
							lengthIV += ivFreq * ivFreq;
						}
						for (Integer oovContextWord : oovContext) {
							double oovFreq = oov.getContextFrequency(oovContextWord);
							lengthOOV += oovFreq * oovFreq;
						}
						lengthOOV = Math.sqrt(lengthOOV);
						lengthIV = Math.sqrt(lengthIV);
						double angle = Math.acos(product / (lengthIV * lengthOOV));
						
						ivContext.addAll(oovContext);
						double jaccard = ((double) common.size()) / ivContext.size();
						jaccard = a * jaccard;
						angle = b * angle;
						System.out.println("jaccard: " + jaccard);
						System.out.println("angle: " + angle);
						
						if (angle < minAngles.get(minAngles.size()-1).getAngle()) {
							Word w = new Word(iv.getWord());
							w.setAngle(angle);
							if(minAngles.size() == 30)
								minAngles.remove(minAngles.size()-1);
							minAngles.add(w);
						}
						
						if (jaccard > maxJaccard.get(maxJaccard.size()-1).getJaccard()) {
							Word w = new Word(iv.getWord());
							w.setJaccard(jaccard);
							if(maxJaccard.size() == 30)
								maxJaccard.remove(maxJaccard.size()-1);
							maxJaccard.add(w);
						}
						Collections.sort(maxJaccard, new Comparator<Word>() {
							@Override
							public int compare(Word arg0, Word arg1) {
								return Double.compare(arg1.getJaccard(), arg0.getJaccard());
							}
						});
						Collections.sort(minAngles, new Comparator<Word>() {
							@Override
							public int compare(Word arg0, Word arg1) {
								return Double.compare(arg0.getAngle(), arg1.getAngle());
							}
						});
					}

				}

				z = 1;
				
				Collections.sort(minAngles, new Comparator<Word>() {
					@Override
					public int compare(Word arg0, Word arg1) {
						return Integer.compare(lcs(arg1.getWord().toCharArray(), oov.getWord().toCharArray()), lcs(arg0.getWord().toCharArray(), oov.getWord().toCharArray()));
					}
				});
				Collections.sort(maxJaccard, new Comparator<Word>() {
					@Override
					public int compare(Word arg0, Word arg1) {
						return Integer.compare(lcs(arg1.getWord().toCharArray(), oov.getWord().toCharArray()), lcs(arg0.getWord().toCharArray(), oov.getWord().toCharArray()));
					}
				});
				
				HashSet<String> pairs = oov.getPairs();
				boolean correctJaccard = false, correctAngle = false;
				bw.write(out.getKey());
				bw.write("\tJaccard");
				System.out.println("Jaccard");
				for (int i = 0; i < 4; ++i) {
					Word word = maxJaccard.get(i);
					bw.write("\t" + word.getWord() + "\t" + word.getJaccard());
					System.out.println("\t" + word.getWord() + "\t" + word.getJaccard());
					for (String pair : pairs) {
						if (word.getWord().equals(pair)) {
							correctJaccard = true;
						} 						
					}
				}
				if (correctJaccard) {
					bw.write("\tcorrect");
					correctJacPairs++;
				} else {
					bw.write("\twrong");						
				}
				bw.newLine();
				System.out.println("Angle");
				bw.write("\t\tAngle");
				for (int i = 0; i < 4; ++i) {
					Word word = minAngles.get(i);
					bw.write("\t" + word.getWord() + "\t" + word.getAngle());
					System.out.println("\t" + word.getWord() + "\t" + word.getAngle());
					for (String pair : pairs) {
						if (word.getWord().equals(pair)) {
							correctAngle = true;
						} 						
					};
				}
				if (correctAngle) {
					bw.write("\tcorrect");
					correctAnglePairs++;
				} else {
					bw.write("\twrong");						
				}
				bw.newLine();
				bw.flush();
			}
			int notFound = 0;
			for (Entry<String, Boolean> w : test.entrySet()) {
				if(!OVwords.containsKey(w.getKey())) {
					System.err.println(w.getKey());
					notFound++;
				}
			}
			double testSize = (double)(test.entrySet().size()-notFound);
			double scoreJaccard = ((double)correctJacPairs) / testSize * 100;
			double scoreAngle = ((double)correctAnglePairs) / testSize * 100;
			System.out.println("Test size: " + testSize);
			System.out.println("Jaccard score: " + scoreJaccard + " correct pairs: " + correctJacPairs);
			System.out.println("Angle score: " + scoreAngle + " correct pairs: " + correctAnglePairs);
			bw.write("Test size: " + testSize + "\n");
			bw.write("Jaccard score: " + scoreJaccard + " correct pairs: " + correctJacPairs + "\n");
			bw.write("Angle score: " + scoreAngle + " correct pairs: " + correctAnglePairs);
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static int hamming(char[] left, char[] right) {
		int distance = 0;
		int length = left.length < right.length ? left.length : right.length;
		for (int i = length - 1; i >= 0; i--) {
			if (left[i] != right[i]) {
				if(i >= length - 3)
					distance++;
				else
					return -1;
			}
		}
		return distance;
	}

	public static void initWords(ArrayList<Word> words) {
		for (int i = 0; i < 4; i++) {
			words.add(new Word());
		}
	}
	
	public static void resetWords(Word[] words) {
		for (int i = 0; i < words.length; i++) {
			words[i].setWord(null);
			words[i].setJaccard(0.0);
			words[i].setAngle(1000.0);
		}
	}
	
	public static int lcs(char[] A, char[] B) {
		int[][] LCS = new int[A.length + 1][B.length + 1];
		for (int i = 0; i <= B.length; i++) {
			LCS[0][i] = 0;
		}

		for (int i = 0; i <= A.length; i++) {
			LCS[i][0] = 0;
		}

		for (int i = 1; i <= A.length; i++) {
			for (int j = 1; j <= B.length; j++) {
				if (A[i - 1] == B[j - 1]) {
					LCS[i][j] = LCS[i - 1][j - 1] + 1;
				} else {
					LCS[i][j] = Math.max(LCS[i - 1][j], LCS[i][j - 1]);
				}
			}
		}
		return LCS[A.length][B.length];
	}
    
}
