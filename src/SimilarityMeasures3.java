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

public class SimilarityMeasures3 {

	private static String vectorspace = "word_vectors_ignore_stopw.txt";

	private static int correctJacPairs = 0, correctCosinePairs = 0;
	private static int correctJacWPairs = 0, correctDicePairs = 0;
	private static int correctEditPairs = 0, correctLcsPairs = 0;
	private static int correctEuPairs = 0;
	private static int jacCG = 0, cosineCG = 0, euCG = 0;
	private static int jacWeightCG = 0, diceCG = 0;
	private static int editCG = 0, lcsCG = 0;
	private static int jacDCG = 0, cosineDCG = 0, euDCG = 0;
	private static int jacWeightDCG = 0, diceDCG = 0;
	private static int editDCG = 0, lcsDCG = 0;
	
	private static double log2 = Math.log(2);
	

	public static void main(String[] args) {

		try {
			HashMap<String, Word> OOVwords = new HashMap<>();
			HashMap<String, HashSet<Word>> IVwords = new HashMap<>();
			HashSet<String> test = new HashSet<>();

			System.out.println("Loading vector space into memory...");
			loadVectorSpace(OOVwords, IVwords);
			loadTest(test, OOVwords);

			System.out.println("Writing to similarity.txt");
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream("similarity.txt"), "UTF-8"));
			
			int oovNum = 1, ivNum = 1;
			
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
				ArrayList<Word> maxCosines = new ArrayList<>();
				ArrayList<Word> minEuclidean = new ArrayList<>();
				ArrayList<Word> minEdit = new ArrayList<>();
				ArrayList<Word> maxLcs = new ArrayList<>();
				initWords(maxJaccard);
				initWords(maxJaccardWeight);
				initWords(maxDice);
				initWords(maxCosines);
				initWords(minEuclidean);
				initWords(minEdit);
				initWords(maxLcs);

				for (Entry<String, HashSet<Word>> cIV : IVwords.entrySet()) {
					String clusterIV = cIV.getKey();
					int ham = hamming(clusterIV.toCharArray(), clusterOOV.toCharArray());
					if (ham > 3 || ham == -1) {
						continue;
					}
					if (ham != 0) {
						continue;
					}
					HashSet<Word> inVocWords = cIV.getValue();
					for (Word iv : inVocWords) {
						double size;
						if (oovContext.size() < iv.getContextSet().size()) {
							size = ((double) oovContext.size()) / iv.getContextSet().size();
						} else {
							size = ((double) iv.getContextSet().size()) / oovContext.size();
						}
						if (size <= 0.001) {
							// continue;
						}

						System.out.println(oovNum + " oov " + out.getKey());
						System.out.println(ivNum++ + " iv " + iv.getWord());

						WordSimilarity wordSimilarity = new WordSimilarity(oov, iv);
						if (wordSimilarity.getCommon().isEmpty()) {
							continue;
						}
						double jaccard = wordSimilarity.jaccard();
						double cosine = wordSimilarity.cosine();
						double jaccardWeight = wordSimilarity.jaccardWeight();
						double dice = wordSimilarity.dice();
						double euclidean = wordSimilarity.euclidean();
						double editDistance = WordSimilarity.editDistance(oov.getWord(), iv.getWord());
						double lcs = WordSimilarity.lcs(oov.getWord().toCharArray(), iv.getWord().toCharArray());

						addResult(maxCosines, "cosine", cosine, iv.getWord());
						addResult(maxJaccard, "jaccard", jaccard, iv.getWord());
						addResult(maxJaccardWeight, "jaccardWeight", jaccardWeight, iv.getWord());
						addResult(maxDice, "dice", dice, iv.getWord());
						addResult(minEuclidean, "eu", euclidean, iv.getWord());
						addResult(minEdit, "edit", editDistance, iv.getWord());
						addResult(maxLcs, "lcs", lcs, iv.getWord());

						sortResults(maxCosines, "cosine");
						sortResults(maxJaccard, "jaccard");
						sortResults(maxJaccardWeight, "jaccardWeight");
						sortResults(maxDice, "dice");
						sortResults(minEuclidean, "eu");
						sortResults(minEdit, "edit");
						sortResults(maxLcs, "lcs");
					}
				}
				++oovNum;
				ivNum = 1;
				
				/*char[] oovCharArray = oov.getWord().toCharArray();
				rankWordsBylcs(maxCosines, oovCharArray);
				rankWordsBylcs(maxJaccard, oovCharArray);
				rankWordsBylcs(maxJaccardWeight, oovCharArray);
				rankWordsBylcs(maxDice, oovCharArray);
				rankWordsBylcs(minEuclidean, oovCharArray);*/

				bw.write(out.getKey());
				printResults("jaccard", maxJaccard, bw, oov);
				printResults("jaccardWeight", maxJaccardWeight, bw, oov);
				printResults("dice", maxDice, bw, oov);
				printResults("cosine", maxCosines, bw, oov);
				printResults("eu", minEuclidean, bw, oov);
				printResults("edit", minEdit, bw, oov);
				printResults("lcs", maxLcs, bw, oov);
				bw.flush();
			}
			
			int notFound = 0, testFreq = 0;
			for (String t : test) {
				if (!OOVwords.containsKey(t)) {
					System.err.println(t);
					notFound++;
				} else {
					testFreq += OOVwords.get(t).getFrequency();
				}
			}
			double testSize = (double) (test.size() - notFound);
			
			double scoreJaccard = ((double) correctJacPairs) / testSize * 100;
			double scoreJacCG = ((double) jacCG) / testFreq * 100;
			double scoreJacDCG = ((double) jacDCG) / testFreq * 100;
			double scoreJacW = ((double) correctJacWPairs) / testSize * 100;
			double scoreJacWCG = ((double) jacWeightCG) / testFreq * 100;
			double scoreJacWDCG = ((double) jacWeightDCG) / testFreq * 100;
			double scoreDice = ((double) correctDicePairs) / testSize * 100;
			double scoreDiceCG = ((double) diceCG) / testFreq * 100;
			double scoreDiceDCG = ((double) diceDCG) / testFreq * 100;
			double scoreCosine = ((double) correctCosinePairs) / testSize * 100;
			double scoreCosineCG = ((double) cosineCG) / testFreq * 100;
			double scoreCosineDCG = ((double) cosineDCG) / testFreq * 100;
			double scoreEu = ((double) correctEuPairs) / testSize * 100;
			double scoreEuCG = ((double) euCG) / testFreq * 100;
			double scoreEuDCG = ((double) euDCG) / testFreq * 100;
			double scoreEdit = ((double) correctEditPairs) / testSize * 100;
			double scoreEditCG = ((double) editCG) / testFreq * 100;
			double scoreEditDCG = ((double) editDCG) / testFreq * 100;
			double scoreLcs = ((double) correctLcsPairs) / testSize * 100;
			double scoreLcsCG = ((double) lcsCG) / testFreq * 100;
			double scoreLcsDCG = ((double) lcsDCG) / testFreq * 100;
			
			System.out.println("Test size: " + testSize);
			System.out.println("Jaccard score: " + scoreJaccard + " correct pairs: " + correctJacPairs);
			System.out.println("Jaccard score CG: " + scoreJacCG);
			System.out.println("Jaccard score DCG: " + scoreJacDCG);
			System.out.println("Jaccard Weight score: " + scoreJacW + " correct pairs: " + correctJacWPairs);
			System.out.println("Jaccard Weight score CG: " +scoreJacWCG);
			System.out.println("Jaccard Weight score DCG: " +scoreJacWDCG);
			System.out.println("Dice score: " + scoreDice + " correct pairs: " + correctDicePairs);
			System.out.println("Dice score CG: " + scoreDiceCG);
			System.out.println("Dice score DCG: " + scoreDiceDCG);
			System.out.println("Cosine score: " + scoreCosine + " correct pairs: " + correctCosinePairs);
			System.out.println("Cosine score CG: " + scoreCosineCG);
			System.out.println("Cosine score DCG: " + scoreCosineDCG);
			System.out.println("Euclidean score: " + scoreEu + " correct pairs: " + correctEuPairs);
			System.out.println("Euclidean score CG: " + scoreEuCG);
			System.out.println("Euclidean score DCG: " + scoreEuDCG);
			System.out.println("Edit distance score: " + scoreEdit + " correct pairs: " + correctEditPairs);
			System.out.println("Edit distance score CG: " + scoreEditCG);
			System.out.println("Edit distance score DCG: " + scoreEditDCG);
			System.out.println("Lcs score: " + scoreLcs + " correct pairs: " + correctLcsPairs);
			System.out.println("Lcs score CG: " + scoreLcsCG);
			System.out.println("Lcs score DCG: " + scoreLcsDCG);
			
			bw.write("Test size: " + testSize + "\n");
			bw.write("Jaccard score: " + scoreJaccard + " correct pairs: " + correctJacPairs + "\n");
			bw.write("Jaccard Weight score: " + scoreJacW + " correct pairs: " + correctJacWPairs + "\n");
			bw.write("Dice score: " + scoreDice + " correct pairs: " + correctDicePairs + "\n");
			bw.write("Cosine score: " + scoreCosine + " correct pairs: " + correctCosinePairs + "\n");
			bw.write("Euclidean score: " + scoreEu + " correct pairs: " + correctEuPairs + "\n");
			
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static double log2(double a) {
		return Math.log(a) / log2;
	}

	public static int hamming(char[] left, char[] right) {
		int distance = 0;
		int length = left.length < right.length ? left.length : right.length;
		for (int i = length - 1; i >= 0; i--) {
			if (left[i] != right[i]) {
				if (i >= length - 3)
					distance++;
				else
					return -1;
			}
		}
		return distance;
	}

	public static void initWords(ArrayList<Word> words) {
		for (int i = 0; i < 1; i++) {
			words.add(new Word());
		}
	}

	public static void printResults(String simType, ArrayList<Word> bestWords, BufferedWriter bw, Word oov) {
		try {
			HashSet<String> pairs = oov.getPairs();
			boolean isCorrect = false;
			int rank = -1;
			bw.write("\t" + simType);
			System.out.println(simType);
			for (int i = 0; i < bestWords.size(); ++i) {
				Word word = bestWords.get(i);
				String wString = word.getWord();
				double result = 0.0;
				if (simType.equals("jaccard")) {
					result = word.getJaccard();
				} else if (simType.equals("jaccardWeight")) {
					result = word.getJaccardWeight();
				} else if (simType.equals("dice")) {
					result = word.getDice();
				} else if (simType.equals("cosine")) {
					result = word.getCosine();
				} else if (simType.equals("eu")) {
					result = word.getEuclidean();
				} else if (simType.equals("edit")) {
					result = word.getEditDistance();
				} else if (simType.equals("lcs")) {
					result = word.getLcs();
				}
				bw.write("\t" + wString + "\t" + result);
				System.out.println("\t" + wString + "\t" + result);
				if (pairs.contains(wString)) {
					isCorrect = true;
					rank = i + 2;
					break;
				}
			}
			if (isCorrect) {
				bw.write("\tcorrect");
				if (simType.equals("jaccard")) {
					correctJacPairs++;
					jacCG += oov.getFrequency();
					jacDCG += oov.getFrequency() / log2(rank);
				} else if (simType.equals("jaccardWeight")) {
					correctJacWPairs++;
					jacWeightCG += oov.getFrequency();
					jacWeightDCG += oov.getFrequency() / log2(rank);
				} else if (simType.equals("dice")) {
					correctDicePairs++;
					diceCG += oov.getFrequency();
					diceDCG += oov.getFrequency() / log2(rank);
				} else if (simType.equals("cosine")) {
					correctCosinePairs++;
					cosineCG += oov.getFrequency();
					cosineDCG += oov.getFrequency() / log2(rank);
				} else if (simType.equals("eu")) {
					correctEuPairs++;
					euCG += oov.getFrequency();
					euDCG += oov.getFrequency() / log2(rank);
				} else if (simType.equals("edit")) {
					correctEditPairs++;
					editCG += oov.getFrequency();
					editDCG += oov.getFrequency() / log2(rank);
				} else if (simType.equals("lcs")) {
					correctLcsPairs++;
					lcsCG += oov.getFrequency();
					lcsDCG += oov.getFrequency() / log2(rank);
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
				} else if (simType.equals("cosine")) {
					return Double.compare(arg1.getCosine(), arg0.getCosine());
				} else if (simType.equals("eu")) {
					return Double.compare(arg0.getEuclidean(), arg1.getEuclidean());
				} else if (simType.equals("edit")) {
					return Double.compare(arg0.getEditDistance(), arg1.getEditDistance());
				} else {
					return Double.compare(arg1.getLcs(), arg0.getLcs());
				}
			}
		});
	}

	public static void addResult(ArrayList<Word> similarity, String simType, double result, String iv) {
		double last = 0.0;
		if (simType.equals("jaccard")) {
			last = similarity.get(similarity.size() - 1).getJaccard();
		} else if (simType.equals("jaccardWeight")) {
			last = similarity.get(similarity.size() - 1).getJaccardWeight();
		} else if (simType.equals("dice")) {
			last = similarity.get(similarity.size() - 1).getDice();
		} else if (simType.equals("cosine")) {
			last = similarity.get(similarity.size() - 1).getCosine();
		} else if (simType.equals("eu")) {
			last = similarity.get(similarity.size() - 1).getEuclidean();
		} else if (simType.equals("edit")) {
			last = similarity.get(similarity.size() - 1).getEditDistance();
		} else if (simType.equals("lcs")) {
			last = similarity.get(similarity.size() - 1).getLcs();
		}
		
		if (simType.equals("eu") || simType.equals("edit")) {
			if (result < last) {
				Word w = new Word(iv);
				if (simType.equals("eu")) {
					w.setEuclidean(result);
				} else if (simType.equals("edit")) {
					w.setEditDistance(result);
				} 
				if (similarity.size() == 1) {
					similarity.remove(similarity.size() - 1);
				}
				similarity.add(w);
			}
		} else if (result > last) {
			Word w = new Word(iv);
			if (simType.equals("jaccard")) {
				w.setJaccard(result);
			} else if (simType.equals("jaccardWeight")) {
				w.setJaccardWeight(result);
			} else if (simType.equals("dice")) {
				w.setDice(result);
			} else if (simType.equals("cosine")) {
				w.setCosine(result);
			} else if (simType.equals("lcs")) {
				w.setLcs(result);
			}
			if (similarity.size() == 1) {
				similarity.remove(similarity.size() - 1);
			}
			similarity.add(w);
		}
	}

	public static void loadVectorSpace(HashMap<String, Word> OOVwords, HashMap<String, HashSet<Word>> IVwords) {
		try {
			BufferedReader br = new BufferedReader(new FileReader("vectorspace/" + vectorspace));
			int row = 1;
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(row++);
				String[] parts = line.split("\t");
				String wordString = parts[0];
				int frequency = Integer.parseInt(parts[1]);
				String cluster = parts[2];
				String type = parts[3];
				Word word = new Word(wordString, type, cluster, frequency);
				HashMap<Integer, Integer> contextMap = new HashMap<>();
				for (int i = 4; i < parts.length; i += 2) {
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
				} else if (type.equals("OOV")) {
					OOVwords.put(wordString, word);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadTest(HashSet<String> test, HashMap<String, Word> OOVwords) {
		try {
			BufferedReader br = new BufferedReader(new FileReader("test.txt"));
			String line;
			while ((line = br.readLine()) != null) {
				line = line.toLowerCase();
				String[] parts = line.split(" ");
				String testString = parts[0].trim();
				HashSet<String> pairs = new HashSet<>();
				for (int j = 1; j < parts.length; j++) {
					pairs.add(parts[j].trim());
				}
				if (OOVwords.containsKey(testString)) {
					Word testWord = OOVwords.get(testString);
					testWord.setPairs(pairs);
				}
				test.add(testString);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
