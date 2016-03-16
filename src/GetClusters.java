import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class GetClusters {
	
   public static void main(String[] args){
	   
		HashMap<String, HashMap<String, Integer>> clusters = new HashMap<>();
		
		try {
			final File folder = new File("brown-clusters");
			for (final File fileEntry : folder.listFiles()) {
		            String clusterName = null;
					BufferedReader br = new BufferedReader(new FileReader("brown-clusters" + "\\" + fileEntry.getName()));
					String line;
					while ((line = br.readLine()) != null) {
						if(line.contains("</table>"))
							break;
						if(line.contains("<tr><td>")) {
							int start = line.indexOf("<td class=\"\">");
							int end = line.indexOf("<td class=tdcount>");
							String word = line.substring(start+13, end-1);
							String scount = line.substring(end+18);
							if(scount.contains(",")) {
								scount = scount.replace(",", "");
							}
							int count = Integer.parseInt(scount);
							HashMap<String, Integer> words = clusters.get(clusterName);
							if(!clusters.containsKey(clusterName)) 
								System.out.println("WARNING");
							words.put(word, count);
							
						} else if(line.contains("<h1>")) {
							int start = line.indexOf("path");
							int end = line.indexOf("</h1>");
							clusterName = line.substring(start+5, end);
							clusters.put(clusterName, new HashMap<String, Integer>());
						}
					}
					br.close();
		    }
			
			BufferedWriter out = new BufferedWriter(new FileWriter("clusters.txt"));

			for (Entry<String, HashMap<String, Integer>> cluster : clusters.entrySet()) {
				out.write("cluster: " +  cluster.getKey());
				out.newLine();
				for (Entry<String, Integer> word : cluster.getValue().entrySet()) {
					out.write(word.getKey() + " " + word.getValue());
					out.newLine();
				}
				out.newLine();
			}
			out.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
   }
}