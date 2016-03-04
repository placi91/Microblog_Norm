import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
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
			
			br = new BufferedReader(new FileReader("hun_tweets_2015_10_2016_02.tokenized"));
			while ((line = br.readLine()) != null) {
				System.out.println(s++);
				if (s > 1500000)
					break;
				if (line.contains("tetszett egy videó ( @youtube ):") || line.contains("egy videója ( @youtube ):") 
					|| line.startsWith("rt ")) {
					continue;
				}
				
				line = line.toUpperCase();
				String[] parts = line.split(" ");
				for (int i = 0; i < parts.length; ++i) {
					if (parts[i].contains("HTTP") || parts[i].startsWith("#")) {
						parts[i] = "";
					} else if(parts[i].startsWith("@")) {
						parts[i] = "@MENTION";
					}
					//parts[i] = parts[i].replaceAll("[^\\p{IsAlphabetic}0-9]", "");
				}
				for (int i = 0; i < parts.length; ++i) {
					String word = parts[i].trim();
					if (word.isEmpty() || word.equals("@MENTION")) {
						continue;
					}

					Matcher m = pattern.matcher(word);
					if (m.find()) {
						if (word.length() < 4) {
							continue;
						}
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
							if (left >= 0 && !parts[left].isEmpty() && parts[left].length() > 2) {
								String string = parts[left];
								if(lemmas.containsKey(string)){
									string = lemmas.get(string);
								}
								if (!contextWords.containsKey(string)) {
									w.add((Integer) contextWords.size());
									contextWords.put(string, contextWords.size());
								} else {
									w.add(contextWords.get(string));
								}
							}
							if (right < parts.length && !parts[right].isEmpty() && parts[right].length() > 2) {
								String string = parts[right];
								if(lemmas.containsKey(string)){
									string = lemmas.get(string);
								}
								if (!contextWords.containsKey(string)) {
									w.add((Integer) contextWords.size());
									contextWords.put(string, contextWords.size());
								} else {
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
			lemmas = null;
			HashMap<String, Boolean> oov = new HashMap<>();
			
			br = new BufferedReader(new FileReader("oov.txt"));
			while ((line = br.readLine()) != null) {
				oov.put(line.trim(), false);
			}

			br = new BufferedReader(new FileReader("mostCommonOOV.txt"));
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(" ");
				oov.put(parts[0].trim(), true);
			}
			HashMap<String, Word> IVwords = new HashMap<String, Word>();
			HashMap<String, Word> OVwords = new HashMap<String, Word>();
			//HashSet<String> s = new HashSet<>();
			
			for (Entry<String, Word> inn : words.entrySet()) {
				if (!oov.containsKey(inn.getKey())) {
					IVwords.put(inn.getKey(), inn.getValue());
				} else if (oov.get(inn.getKey())
						&& (inn.getKey().equals("VMIT")
								|| inn.getKey().equals("VMIKOR")
								|| inn.getKey().equals("VMELYIK")
								|| inn.getKey().equals("LTAM")
								|| inn.getKey().equals("SZTEM")
								|| inn.getKey().equals("VHOGY")
								|| inn.getKey().equals("BIOSZ")
								|| inn.getKey().equals("SZIAA")
								|| inn.getKey().equals("OKOSTELEFON")
								|| inn.getKey().equals("LÉGYSZI")
								|| inn.getKey().equals("VHOL")
								|| inn.getKey().equals("APPOT")
								|| inn.getKey().equals("APPOK")
								|| inn.getKey().equals("APPON")
								|| inn.getKey().equals("APPBÓL") || inn.getKey().equals("APPBAN"))) {
					OVwords.put(inn.getKey(), inn.getValue());
				}
			}

			words.clear();
			System.gc();

			s = 1;
			System.out.println("Writing to jaccard.txt");
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("jaccard.txt"), "UTF8"));
			s = 1;
			int z = 1;

			Set<Integer> oovContext = new HashSet<>();
			Set<Integer> ivContext = new HashSet<>();
			Set<Integer> common = new HashSet<>();

			for (Entry<String, Word> out : OVwords.entrySet()) {
				Word[] max = new Word[10];
				for (int i = 0; i < max.length; ++i) {
					max[i] = new Word("empty", 0);
				}
				oovContext.addAll(out.getValue().getContextSet());

				for (Entry<String, Word> inn : IVwords.entrySet()) {
					Word iv = inn.getValue();
					double size;
					if (oovContext.size() < iv.getContextSet().size()) {
						size = ((double) oovContext.size()) / iv.getContextSet().size();
					} else {
						size = ((double) iv.getContextSet().size()) / oovContext.size();
					}
					if (size <= 0.1) {
						continue;
					}

					ivContext.addAll(iv.getContextSet());
					common.addAll(oovContext);

					common.retainAll(ivContext);

					if (common.size() == 0) {
						continue;
					}

					System.out.println(s + " oov " + out.getKey());
					System.out.println(z++ + " iv " + inn.getKey());

					ivContext.addAll(oovContext);

					double jaccard = ((double) common.size())
							/ ivContext.size();
					System.out.println("jaccard: " + jaccard);

					ivContext.clear();
					common.clear();

					if (jaccard > max[9].getJaccard()) {
						max[9].setJaccard(jaccard);
						max[9].setWord(iv.getWord());
					}
					Arrays.sort(max, new Comparator<Word>() {
						@Override
						public int compare(Word arg0, Word arg1) {
							return Double.compare(arg1.getJaccard(),
									arg0.getJaccard());
						}
					});
				}
				z = 1;
				++s;
				bw.write(out.getKey());
				for (Word word : max) {
					bw.write("\t" + word.getWord() + "\t" + word.getJaccard());
					System.out.println("\t" + word.getWord() + "\t"
							+ word.getJaccard());
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
