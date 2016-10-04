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
	
	private static int correctJacPairs = 0, correctAnglePairs = 0;
	private static int correctJacWPairs = 0, correctDicePairs = 0;
	private static int correctEuPairs = 0;

	public static void main(String[] args) {
		
		try {
			int s = 1;
			BufferedReader br = new BufferedReader(new FileReader("vectorspace/word_vectors.txt"));
			
			HashMap<String, Word> OOVwords = new HashMap<>();
			HashMap<String, HashSet<Word>> IVwords = new HashMap<>();
			
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(s++);
				String[] parts = line.split("\t");
				String wordString = parts[0];
				int frequency = Integer.parseInt(parts[1]);
				String cluster = parts[2];
				String type = parts[3];
				Word word = new Word(wordString, type, cluster, frequency);
				HashMap<Integer, Integer> contextMap = new HashMap<>();
				for (int i = 4; i < parts.length; i+=2) {
					int contextNum = Integer.parseInt(parts[i]);
					int contextFreq = Integer.parseInt(parts[i + 1]);
					contextMap.put(contextNum, contextFreq);
				}
				word.setContextMap(contextMap);
				if (type.equals("IV")) {
					HashSet<Word> clusterWords;
					if (IVwords.containsKey(cluster)) {
						clusterWords = IVwords.get(cluster);
						clusterWords.add(word);
					} else {
						clusterWords = new HashSet<>();
						clusterWords.add(word);
						IVwords.put(cluster, clusterWords);
					}
				} else if(type.equals("OOV")) {
					OOVwords.put(wordString, word);
				}
			}
			br.close();
			
			HashSet<String> test = new HashSet<>();
			br = new BufferedReader(new FileReader("test.txt"));
			while ((line = br.readLine()) != null) {
				line = line.toLowerCase();
				String[] parts = line.split(" ");
				String testString = parts[0].trim();
				HashSet<String> pairs = new HashSet<>();
				for (int j = 1; j < parts.length; j++) {
					pairs.add(parts[j].trim());
				}
				if(OOVwords.containsKey(testString)) {
					Word testWord = OOVwords.get(testString);
					testWord.setPairs(pairs);
				}
				test.add(testString);
			}
			br.close();

			System.out.println("Writing to similarity.txt");
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream("similarity.txt"), "UTF-8"));
			s = 1;
			int z = 1;
			
			for (Entry<String, Word> out : OOVwords.entrySet()) {
				Word oov = out.getValue();
				String clusterOOV = oov.getCluster();
				if (clusterOOV == null) {
					continue;
				}
				Set<Integer> oovContext = oov.getContextSet();
				
				ArrayList<Word> maxJaccard = new ArrayList<>();
				ArrayList<Word> maxJaccardWeight = new ArrayList<>();
				ArrayList<Word> maxDice = new ArrayList<>();
				ArrayList<Word> minAngles = new ArrayList<>();
				ArrayList<Word> minEuclidean = new ArrayList<>();
				initWords(maxJaccard);
				initWords(maxJaccardWeight);
				initWords(maxDice);
				initWords(minAngles);
				initWords(minEuclidean);
				
				for (Entry<String, HashSet<Word>> cIV : IVwords.entrySet()) {
					String clusterIV = cIV.getKey();
					int ham = hamming(clusterIV.toCharArray(), clusterOOV.toCharArray());
					double a = 1.0, b = 1.0;
					if (ham > 3 || ham == -1) {
						continue;
					}
					if (ham != 0) {
						//a = 1.0 / ((double) 1.0 + ham);
						//b = 1.0 * ((double) 1.0 + ham);
						continue;
					}
					HashSet<Word> inVocWords = cIV.getValue();
					for (Word iv : inVocWords) {
						System.out.println(s++ + " oov " + out.getKey());
						System.out.println(z++ + " iv " + iv.getWord());
						double size;
						if (oovContext.size() < iv.getContextSet().size()) {
							size = ((double) oovContext.size()) / iv.getContextSet().size();
						} else {
							size = ((double) iv.getContextSet().size()) / oovContext.size();
						}
						if (size <= 0.001) {
							//continue;
						}

						System.out.println(s++ + " oov " + out.getKey());
						System.out.println(z++ + " iv " + iv.getWord());
						
						Set<Integer> ivContext = iv.getContextSet();
						Set<Integer> common = new HashSet<>(ivContext);
						common.retainAll(oovContext);
						if(common.size() == 0) {
							continue;
						}
						
						double jaccard = WordSimilarity.jaccard(ivContext, oovContext, common);
						double angle = WordSimilarity.cosine(ivContext, oovContext, common, oov, iv);
						double jaccardWeight = WordSimilarity.jaccardWeight(ivContext, oovContext, common, oov, iv);
						double dice = WordSimilarity.dice(ivContext, oovContext, common, oov, iv);
						double euclidean = WordSimilarity.euclidean(ivContext, oovContext, common, oov, iv);
						//jaccard = a * jaccard;
						//angle = b * angle;
						
						addResult(minAngles, "angle", angle, iv.getWord());
						addResult(maxJaccard, "jaccard", jaccard, iv.getWord());
						addResult(maxJaccardWeight, "jaccardWeight", jaccardWeight, iv.getWord());
						addResult(maxDice, "dice", dice, iv.getWord());
						addResult(minEuclidean, "eu", euclidean, iv.getWord());
						
						sortResults(minAngles, "angle");
						sortResults(maxJaccard, "jaccard");
						sortResults(maxJaccardWeight, "jaccardWeight");
						sortResults(maxDice, "dice");
						sortResults(minEuclidean, "eu");
					}

				}

				z = 1;
				char[] oovCharArray = oov.getWord().toCharArray();
				rankWordsBylcs(minAngles, oovCharArray);
				rankWordsBylcs(maxJaccard, oovCharArray);
				rankWordsBylcs(maxJaccardWeight, oovCharArray);
				rankWordsBylcs(maxDice, oovCharArray);
				rankWordsBylcs(minEuclidean, oovCharArray);
				
				HashSet<String> pairs = oov.getPairs();
				bw.write(out.getKey());
				printResults("jaccard", maxJaccard, pairs, bw);
				printResults("jaccardWeight", maxJaccardWeight, pairs, bw);
				printResults("dice", maxDice, pairs, bw);
				printResults("angle", minAngles, pairs, bw);
				printResults("eu", minEuclidean, pairs, bw);
				bw.flush();
			}
			int notFound = 0;
			for (String t : test) {
				if(!OOVwords.containsKey(t)) {
					System.err.println(t);
					notFound++;
				}
			}
			double testSize = (double)(test.size()-notFound);
			double scoreJaccard = ((double)correctJacPairs) / testSize * 100;
			double scoreJaccardW = ((double)correctJacWPairs) / testSize * 100;
			double scoreDice = ((double)correctDicePairs) / testSize * 100;
			double scoreAngle = ((double)correctAnglePairs) / testSize * 100;
			double scoreEu = ((double)correctEuPairs) / testSize * 100;
			System.out.println("Test size: " + testSize);
			System.out.println("Jaccard score: " + scoreJaccard + " correct pairs: " + correctJacPairs);
			System.out.println("Jaccard Weight score: " + scoreJaccardW + " correct pairs: " + correctJacWPairs);
			System.out.println("Dice score: " + scoreDice + " correct pairs: " + correctDicePairs);
			System.out.println("Angle score: " + scoreAngle + " correct pairs: " + correctAnglePairs);
			System.out.println("Euclidean score: " + scoreEu + " correct pairs: " + correctEuPairs);
			bw.write("Test size: " + testSize + "\n");
			bw.write("Jaccard score: " + scoreJaccard + " correct pairs: " + correctJacPairs + "\n");
			bw.write("Jaccard Weight score: " + scoreJaccardW + " correct pairs: " + correctJacWPairs + "\n");
			bw.write("Dice score: " + scoreDice + " correct pairs: " + correctDicePairs + "\n");
			bw.write("Angle score: " + scoreAngle + " correct pairs: " + correctAnglePairs + "\n");
			bw.write("Euclidean score: " + scoreEu + " correct pairs: " + correctEuPairs);
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
		for (int i = 0; i < 5; i++) {
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
	
	public static void printResults(String simType, ArrayList<Word> bestWords, HashSet<String> pairs, BufferedWriter bw) {
		try {
			boolean isCorrect = false;
			bw.write("\t" + simType);
			System.out.println(simType);
			for (int i = 0; i < 5; ++i) {
				Word word = bestWords.get(i);
				String wString = word.getWord();
				double result = 0.0;
				if (simType.equals("jaccard")) {
					result = word.getJaccard();
				} else if (simType.equals("jaccardWeight")) {
					result = word.getJaccardWeight();
				} else if (simType.equals("dice")) {
					result = word.getDice();
				} else if (simType.equals("angle")) {
					result = word.getAngle();
				} else if (simType.equals("eu")) {
					result = word.getEuclidean();
				}
				bw.write("\t" + wString + "\t" + result);
				System.out.println("\t" + wString + "\t" + result);
				if (pairs.contains(wString)) {
					isCorrect = true;
				}
			}
			if (isCorrect) {
				bw.write("\tcorrect");
				if (simType.equals("jaccard")) {
					correctJacPairs++;
				} else if (simType.equals("jaccardWeight")) {
					correctJacWPairs++;
				} else if (simType.equals("dice")) {
					correctDicePairs++;
				} else if (simType.equals("angle")) {
					correctAnglePairs++;
				} else if (simType.equals("eu")) {
					correctEuPairs++;
				}
			} else {
				bw.write("\twrong");
			}
			bw.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void rankWordsBylcs(ArrayList<Word> simType, char[] oovCharArray) {
		Collections.sort(simType, new Comparator<Word>() {
			@Override
			public int compare(Word arg0, Word arg1) {
				return Integer.compare(WordSimilarity.lcs(arg1.getWord().toCharArray(), oovCharArray),
						WordSimilarity.lcs(arg0.getWord().toCharArray(), oovCharArray));
			}
		});
	}
	
	public static void sortResults(ArrayList<Word> similarity, String simType) {
		Collections.sort(similarity, new Comparator<Word>() {
			@Override
			public int compare(Word arg0, Word arg1) {
				if (simType.equals("jaccard")) {
					return Double.compare(arg1.getJaccard(), arg0.getJaccard());
				} else if (simType.equals("jaccardWeight")) {
					return Double.compare(arg1.getJaccardWeight(), arg0.getJaccardWeight());
				} else if (simType.equals("dice")) {
					return Double.compare(arg1.getDice(), arg0.getDice());
				} else if (simType.equals("angle")) {
					return Double.compare(arg0.getAngle(), arg1.getAngle());
				} else {
					return Double.compare(arg0.getEuclidean(), arg1.getEuclidean());
				}
			}
		});	
	}
	
	public static void addResult(ArrayList<Word> similarity, String simType, double result, String iv) {
		double last = 0.0;
		if (simType.equals("jaccard")) {
			last = similarity.get(similarity.size()-1).getJaccard();
		} else if (simType.equals("jaccardWeight")) {
			last = similarity.get(similarity.size()-1).getJaccardWeight();
		} else if (simType.equals("dice")) {
			last = similarity.get(similarity.size()-1).getDice();
		} else if (simType.equals("angle")) {
			last = similarity.get(similarity.size()-1).getAngle();
		} else if (simType.equals("eu")) {
			last = similarity.get(similarity.size()-1).getEuclidean();
		}
		if ((simType.equals("angle") || simType.equals("eu")) && result < last) {
			Word w = new Word(iv);
			if (simType.equals("angle")) {
				w.setAngle(result);
			} else if (simType.equals("eu")) {
				w.setEuclidean(result);
			}
			if(similarity.size() == 60) {
				similarity.remove(similarity.size()-1);
			}
			similarity.add(w);
		} else if (result > last) {
			Word w = new Word(iv);
			if (simType.equals("jaccard")) {
				w.setJaccard(result);
			} else if (simType.equals("jaccardWeight")) {
				w.setJaccardWeight(result);
			} else if (simType.equals("dice")) {
				w.setDice(result);
			} 
			if(similarity.size() == 60) {
				similarity.remove(similarity.size()-1);
			}
			similarity.add(w);
		}

	}
    
}
