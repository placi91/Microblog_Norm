import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;

public class DownloadHTML {

	public static void main(String[] args) throws Exception {
		
		ArrayList<String> urls = new ArrayList<>();
		
		BufferedReader in = new BufferedReader(new FileReader("cluster_viewer.html"));
		String line;
		while ((line = in.readLine()) != null) {
			int start = line.indexOf("^<a target=_blank href=\"paths/");
			if(start != -1) {
				int end = line.indexOf(".html\">");
				String url = line.substring(start + 30, end);
				urls.add(url);
				
			} 
		}
		in.close();
		
		for (String string : urls) {
			URL url = new URL("http://rgai.inf.u-szeged.hu/~berend/szgnyelveszet/cluster_viewer2/paths/" + string + ".html");

			String passwdstring = "szamitogepes:nyelveszet11";
			String encoding = new sun.misc.BASE64Encoder().encode(passwdstring.getBytes());

			URLConnection uc = url.openConnection();
			uc.setRequestProperty("Authorization", "Basic " + encoding);

			InputStream content = (InputStream) uc.getInputStream();
			in = new BufferedReader(new InputStreamReader(content));
			BufferedWriter out = new BufferedWriter(new FileWriter("brown-clusters\\" + string + ".html"));

			String line2;
			while ((line2 = in.readLine()) != null) {
				out.write(line2);
				out.newLine();
			}

			in.close();
			out.close();
		}
	}
}