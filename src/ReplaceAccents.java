import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;

public class ReplaceAccents {

	public static void main(String[] args) {

		try {
			BufferedReader br = new BufferedReader(new FileReader("hun_tweets_2015_10_2016_02.txt"));
			PrintStream output = new PrintStream(new FileOutputStream("tweets.txt"), true, "UTF-8");
			String line;
			while ((line = br.readLine()) != null) {
				line = line.toLowerCase();
				line = line.replace("\\n", " ");
				line = line.substring(1, line.length() - 1);
				line = line.replace('à', 'á');
				line = line.replace('â', 'á');
				line = line.replace('è', 'é');
				line = line.replace('ê', 'é');
				line = line.replace('ì', 'í');
				line = line.replace('î', 'í');
				line = line.replace('ò', 'ó');
				line = line.replace('õ', 'ő');
				line = line.replace('ù', 'ú');
				line = line.replace('ũ', 'ű');
				
				output.print(line);
				output.print("\n");
			}
			br.close();
			output.close();
		} catch(Exception e) {
			
		}
	}
}
