import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class WordSimilarity {
	
	private Set<Integer> common;
	private Set<Integer> onlyIV;
	private Set<Integer> onlyOOV;
	private Set<Integer> ivContext;
	private Set<Integer> oovContext;
	private double lengthOOV;
	private double lengthIV;
	private Word oov;
	private Word iv;
	
	public WordSimilarity(Word oov, Word iv) {
		this.oov = oov;
		this.iv = iv;
		ivContext = iv.getContextSet();
		oovContext = oov.getContextSet();
		common = new HashSet<>(ivContext);
		common.retainAll(oovContext);
		onlyIV = new HashSet<>(ivContext);
		onlyIV.removeAll(oovContext);
		onlyOOV = new HashSet<>(oovContext);
		onlyOOV.removeAll(ivContext);
		
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
	}
	
	public Set<Integer> getCommon() {
		return common;
	}
	
	public double jaccard() {
		Set<Integer> union = new HashSet<>(ivContext);
		union.addAll(oovContext);
		return((double) common.size()) / union.size();
	}

	public double cosine() {
		double product = 0.0;
		for (Integer commonWord : common) {
			double oovFreq = oov.getContextFrequency(commonWord);
			double ivFreq = iv.getContextFrequency(commonWord);
			product += oovFreq  * ivFreq;
		}
		return product / (lengthIV * lengthOOV);
	}
	
	public double jaccardWeight() {
		double numerator = 0.0, denominator = 0.0;
		for (Integer commonWord : common) {
			double oovFreq = oov.getContextFrequency(commonWord) / lengthOOV;
			double ivFreq = iv.getContextFrequency(commonWord) / lengthIV;
			numerator += Math.min(oovFreq, ivFreq);
			denominator += Math.max(oovFreq, ivFreq);
		}
		for (Integer ivContextWord : onlyIV) {
			double ivFreq = iv.getContextFrequency(ivContextWord) / lengthIV;
			denominator += ivFreq;
		}
		for (Integer oovContextWord : onlyOOV) {
			double oovFreq = oov.getContextFrequency(oovContextWord) / lengthOOV;
			denominator += oovFreq;
		}
		return numerator / denominator;
	}
	
	public double dice() {
		double numerator = 0.0, denominator = 0.0;
		for (Integer commonWord : common) {
			double oovFreq = oov.getContextFrequency(commonWord) / lengthOOV;
			double ivFreq = iv.getContextFrequency(commonWord) / lengthIV;
			numerator += Math.min(oovFreq, ivFreq);
			denominator += oovFreq + ivFreq;
		}
		numerator = 2 * numerator;
		for (Integer ivContextWord : onlyIV) {
			double ivFreq = iv.getContextFrequency(ivContextWord) / lengthIV;
			denominator += ivFreq;
		}
		for (Integer oovContextWord : onlyOOV) {
			double oovFreq = oov.getContextFrequency(oovContextWord) / lengthOOV;
			denominator += oovFreq;
		}
		return numerator / denominator;
	}
	
	public double euclidean() {
		double sum = 0.0;
		for (Integer commonWord : common) {
			double oovFreq = oov.getContextFrequency(commonWord) / lengthOOV;
			double ivFreq = iv.getContextFrequency(commonWord) / lengthIV;
			sum += (oovFreq - ivFreq) * (oovFreq - ivFreq);
		}
		for (Integer ivContextWord : onlyIV) {
			double ivFreq = iv.getContextFrequency(ivContextWord) / lengthIV;
			sum += ivFreq * ivFreq;
		}
		for (Integer oovContextWord : onlyOOV) {
			double oovFreq = oov.getContextFrequency(oovContextWord) / lengthOOV;
			sum += oovFreq * oovFreq;
		}
		return Math.sqrt(sum);
	}
	
	public static int lcs(char[] a, char[] b) {
		int[][] lcs = new int[a.length + 1][b.length + 1];
		for (int i = 0; i <= b.length; i++) {
			lcs[0][i] = 0;
		}

		for (int i = 0; i <= a.length; i++) {
			lcs[i][0] = 0;
		}

		for (int i = 1; i <= a.length; i++) {
			for (int j = 1; j <= b.length; j++) {
				if (a[i - 1] == b[j - 1]) {
					lcs[i][j] = lcs[i - 1][j - 1] + 1;
				} else {
					lcs[i][j] = Math.max(lcs[i - 1][j], lcs[i][j - 1]);
				}
			}
		}
		return lcs[a.length][b.length];
	}
	
    private static int minimum(int a, int b, int c) {                            
        return Math.min(Math.min(a, b), c);                                      
    }                                                                            
                                                                                 
    public static int editDistance(CharSequence lhs, CharSequence rhs) {      
        int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];        
                                                                                 
        for (int i = 0; i <= lhs.length(); i++)                                 
            distance[i][0] = i;                                                  
        for (int j = 1; j <= rhs.length(); j++)                                 
            distance[0][j] = j;                                                  
                                                                                 
        for (int i = 1; i <= lhs.length(); i++)                                 
            for (int j = 1; j <= rhs.length(); j++)                             
                distance[i][j] = minimum(                                        
                        distance[i - 1][j] + 1,                                  
                        distance[i][j - 1] + 1,                                  
                        distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));
                                                                                 
        return distance[lhs.length()][rhs.length()];                           
    }
	
	public static void main(String[] args) {
		System.out.println(SimilarityMeasures.log2(1));
		System.out.println(SimilarityMeasures.log2(2));
		System.out.println(SimilarityMeasures.log2(3));
	}
	
}
