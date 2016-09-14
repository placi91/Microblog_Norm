import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Word {

	private String word;
	private String cluster = null;
	private HashSet<String> pairs = null;
	private boolean isCommon;
	private int frequency;
	private double jaccard = 0.0;
	private double angle = 1000.0;
	private HashMap<Integer, Integer> contextSet = new HashMap<>();

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

	public Word(boolean isCommon) {
		this.isCommon = isCommon;
	}
	
	public void addContext(Integer word) {
		contextSet.put(word, 1);
	}
	
	public void updateContext(Integer word) {
		contextSet.put(word, contextSet.get(word) + 1);
	}
	
	public boolean containsContext(Integer word) {
		return contextSet.containsKey(word);
	}
	
	public HashMap<Integer, Integer> getContextMap() {
		return contextSet;
	}	
	
	public Set<Integer> getContextSet() {
		return contextSet.keySet();
	}
	
	public int getContextFrequency(Integer word) {
		return contextSet.get(word);
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
	
	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
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

	@Override
	public String toString() {
		return "Word [word=" + word + ", frequency=" + frequency + ", jaccard="
				+ jaccard +  ", angle=" + angle + ", contextSet=" + contextSet + "]";
	}
}

