import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

public class Word {

	private String word;
	private int frequent;
	private double jaccard = 0.0;
	private HashSet<Integer> contextSet = new HashSet<>();

	public Word(String word, int frequent) {
		this.word = word;
		this.frequent = frequent;
	}
	
	public void add(Integer word) {
		contextSet.add(word);
	}
	
	public HashSet<Integer> getContextSet() {
		return contextSet;
	}	

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getFrequent() {
		return frequent;
	}

	public void setFrequent(int frequent) {
		this.frequent = frequent;
	}

	public double getJaccard() {
		return jaccard;
	}

	public void setJaccard(double jaccard) {
		this.jaccard = jaccard;
	}

	@Override
	public String toString() {
		return "Word [word=" + word + ", frequent=" + frequent + ", jaccard="
				+ jaccard + ", contextSet=" + contextSet + "]";
	}
	
	

}
