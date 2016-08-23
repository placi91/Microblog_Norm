import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;

public class CopyTweets {

	public static void main(String[] args) {

		try {
			BufferedReader br = new BufferedReader(new FileReader("hun_tweets_2015_10_2016_02.txt"));
			PrintStream output = new PrintStream(new FileOutputStream("tweets.txt"), true, "UTF-8");
			String line;
			while ((line = br.readLine()) != null) {
				line = line.replaceAll("\\\\n", " ");
				line = line.substring(1, line.length() - 1);
				line = line.toLowerCase();
				output.print(line);
				output.print("\n");
			}
			br.close();
			output.close();
		}catch(Exception e) {}
	}
}
