import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


public class Accents {

	public static void main(String[] args) throws Exception {

		BufferedReader br = new BufferedReader(new FileReader("suggestions.txt"));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("accents.txt"), "UTF8"));
		
		HashMap<Character, HashSet<Character>> accents = new HashMap<>();
		accents.put('a', new HashSet<Character>(Arrays.asList('á')));
		accents.put('e', new HashSet<Character>(Arrays.asList('é')));
		accents.put('i', new HashSet<Character>(Arrays.asList('í')));
		accents.put('u', new HashSet<Character>(Arrays.asList('ú', 'ű', 'ü')));
		accents.put('o', new HashSet<Character>(Arrays.asList('ó', 'ő', 'ö')));
		accents.put('ö', new HashSet<Character>(Arrays.asList('ő')));
		accents.put('ü', new HashSet<Character>(Arrays.asList('ű')));
		accents.put('ô', new HashSet<Character>(Arrays.asList('ő', 'ó')));
		accents.put('û', new HashSet<Character>(Arrays.asList('ű', 'ú')));
		
		String line;
		while ((line = br.readLine()) != null) {
			if(!line.startsWith("&")) {
				continue;
			}
			line = line.toLowerCase();
			String[] parts = line.split(" ");
			char[] str = parts[1].toCharArray();
			String rightSuggestion = null;
			for (int i = 4; i < parts.length; ++i) {
				String suggestionStr = parts[i];
				if(suggestionStr.contains(",")) {
					suggestionStr = suggestionStr.replace(",", "").trim();
				}
				if(str.length != suggestionStr.length()) {
					continue;
				}
				char[] suggestion = suggestionStr.toCharArray();
				boolean isRight = true;
				for (int j = 0; j < suggestion.length; j++) {
					if(str[j] != suggestion[j]) {
						if(!(accents.containsKey(str[j]) && accents.get(str[j]).contains(suggestion[j]))) {
							isRight = false;
						}
					} 
				}
				if(isRight) {
					rightSuggestion = suggestionStr;
					break;
				}
			}
			if(rightSuggestion != null) {
				System.out.println(parts[1] + " " + rightSuggestion);
				bw.write(parts[1] + " " + rightSuggestion);
				bw.newLine();
			}
		}
		br.close();
		bw.close();
		
	}

}
