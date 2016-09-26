import java.util.HashSet;
import java.util.Set;

public class WordSimilarity {
	
	public static double jaccard(Set<Integer> ivContext, Set<Integer> oovContext, Set<Integer> common) {
		Set<Integer> union = new HashSet<>(ivContext);
		union.addAll(oovContext);
		return((double) common.size()) / union.size();
	}

	public static double cosine(Set<Integer> ivContext, Set<Integer> oovContext, Set<Integer> common, Word oov, Word iv) {
		double product = 0.0, lengthOOV = 0.0, lengthIV = 0.0;
		for (Integer commonWord : common) {
			double oovFreq = oov.getContextFrequency(commonWord);
			double ivFreq = iv.getContextFrequency(commonWord);
			product += oovFreq  * ivFreq;
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
		return Math.acos(product / (lengthIV * lengthOOV));
	}
	
	public static double jaccardWeight(Set<Integer> ivContext, Set<Integer> oovContext, Set<Integer> common, Word oov, Word iv) {
		double jaccardWeight1 = 0.0, jaccardWeight2 = 0.0;
		for (Integer commonWord : common) {
			double oovFreq = oov.getContextFrequency(commonWord);
			double ivFreq = iv.getContextFrequency(commonWord);
			jaccardWeight1 += Math.min(oovFreq, ivFreq);
			jaccardWeight2 += Math.max(oovFreq, ivFreq);
		}
		Set<Integer> asymmetric = new HashSet<>(ivContext);
		asymmetric.removeAll(oovContext);
		for (Integer ivContextWord : asymmetric) {
			double ivFreq = iv.getContextFrequency(ivContextWord);
			jaccardWeight2 += ivFreq;
		}
		asymmetric.clear();
		asymmetric.addAll(oovContext);
		asymmetric.removeAll(ivContext);
		for (Integer oovContextWord : asymmetric) {
			double oovFreq = oov.getContextFrequency(oovContextWord);
			jaccardWeight2 += oovFreq;
		}
		return jaccardWeight1 / jaccardWeight2;
	}
	
	public static double dice(Set<Integer> ivContext, Set<Integer> oovContext, Set<Integer> common, Word oov, Word iv) {
		double dice1 = 0.0, dice2 = 0.0;
		for (Integer commonWord : common) {
			double oovFreq = oov.getContextFrequency(commonWord);
			double ivFreq = iv.getContextFrequency(commonWord);
			dice1 += Math.min(oovFreq, ivFreq);
			dice2 += oovFreq + ivFreq;
		}
		dice1 = 2 * dice1;
		Set<Integer> asymmetric = new HashSet<>(ivContext);
		asymmetric.removeAll(oovContext);
		for (Integer ivContextWord : asymmetric) {
			double ivFreq = iv.getContextFrequency(ivContextWord);
			dice2 += ivFreq;
		}
		asymmetric.clear();
		asymmetric.addAll(oovContext);
		asymmetric.removeAll(ivContext);
		for (Integer oovContextWord : asymmetric) {
			double oovFreq = oov.getContextFrequency(oovContextWord);
			dice2 += oovFreq;
		}
		return dice1 / dice2;
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
	
}
