import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class Word {

	private String word;
	private String type;
	private String cluster = null;
	private HashSet<String> pairs = null;
	private boolean isCommon;
	private int frequency;
	private double editDistance = 10000.0;
	private double lcs = 0.0;
	private double jaccard = 0.0;
	private double jaccardWeight = 0.0;
	private double dice = 0.0;
	private double cosine = 0.0;
	private double euclidean = 10000.0;
	private HashMap<Integer, Integer> contextMap = new HashMap<>();
	HashMap<String, Integer> contextMapString = new HashMap<>();

	public Word() {
		this.word = "empty";
	}
	
	public Word(String word) {
		this.word = word;
	}
	
	public Word(String word, int frequency) {
		this.word = word;
		this.frequency = frequency;
	}
	
	public Word(boolean isCommon, HashSet<String> pairs) {
		this.isCommon = isCommon;
		this.pairs = pairs;
	}

	public Word(String word, String type, String cluster, int frequency) {
		this.word = word;
		this.type = type;
		this.cluster = cluster;
		this.frequency = frequency;
	}
	
	public Word(boolean isCommon) {
		this.isCommon = isCommon;
	}

	public void addContext(Integer word) {
		contextMap.put(word, 1);
	}
	
	public void updateContext(Integer word) {
		contextMap.put(word, contextMap.get(word) + 1);
	}
	
	public boolean containsContext(Integer word) {
		return contextMap.containsKey(word);
	}
	
	public HashMap<Integer, Integer> getContextMap() {
		return contextMap;
	}	
	
	public void setContextMap(HashMap<Integer, Integer> contextMap) {
		this.contextMap = contextMap;
	}

	public Set<Integer> getContextSet() {
		return contextMap.keySet();
	}
	
	public int getContextFrequency(Integer word) {
		return contextMap.get(word);
	}
	
	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public double getJaccard() {
		return jaccard;
	}

	public void setJaccard(double jaccard) {
		this.jaccard = jaccard;
	}
	
	public double getDice() {
		return dice;
	}

	public void setDice(double dice) {
		this.dice = dice;
	}

	public double getJaccardWeight() {
		return jaccardWeight;
	}

	public void setJaccardWeight(double jaccardWeight) {
		this.jaccardWeight = jaccardWeight;
	}
	
	public double getCosine() {
		return cosine;
	}

	public void setCosine(double angle) {
		this.cosine = angle;
	}

	public double getEuclidean() {
		return euclidean;
	}

	public void setEuclidean(double euclidean) {
		this.euclidean = euclidean;
	}

	public double getEditDistance() {
		return editDistance;
	}

	public void setEditDistance(double editDistance) {
		this.editDistance = editDistance;
	}

	public double getLcs() {
		return lcs;
	}

	public void setLcs(double lcs) {
		this.lcs = lcs;
	}

	public String getCluster() {
		return cluster;
	}

	public void setCluster(String cluster) {
		this.cluster = cluster;
	}
	
	public HashSet<String> getPairs() {
		return pairs;
	}

	public void setPairs(HashSet<String> pairs) {
		this.pairs = pairs;
	}

	public boolean isCommon() {
		return isCommon;
	}

	public void setCommon(boolean isCommon) {
		this.isCommon = isCommon;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Word [word=" + word + ", frequency=" + frequency + ", jaccard="
				+ jaccard +  ", cosine=" + cosine + ", contextSet=" + contextMap + "]";
	}
}

