import java.io.*;
import java.util.HashMap;
public class TestDemo {

	public static HashMap<String, HashMap<String, Float>> queryTerms;

	public static void main(String[] args) throws IOException {
		queryTerms = new HashMap<String, HashMap<String, Float>>();
			BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/queryTerms"));
			String line = br.readLine();
			String prevQID = "";
			int justStarted = 1;
			HashMap<String, Float> hm = new HashMap<String, Float>();
			while(line != null)
			{
				String qID = line.substring(6, line.indexOf('.', 6));
				String word = line.substring(line.indexOf('>')+1, line.indexOf('<', line.indexOf('>')));
				System.out.println("Query: "+qID+"_"+word+"_");
				if(prevQID.compareTo(qID) != 0)
				{
					// this means this is a new qID, so create a new HashMap for this qID
					// but first, put this hm into the queryTerms
					if(justStarted == 1) {justStarted++;} else queryTerms.put(prevQID, hm);
					System.out.println("Putting query "+prevQID +" into the hashmap with no of words: "+hm.size()+"\n\n");
					hm = new HashMap<String, Float>();
					
					//q = new Query();
				}
				if(hm.containsKey(word))
				{
					Float f = hm.get(word);
					f++;
					hm.put(word, f);
				}
				else
				{
					hm.put(word, new Float(1.0));
				}
				prevQID = qID;
				line = br.readLine();
			}
			queryTerms.put(prevQID, hm);
			System.out.println("Putting query "+prevQID +" into the hashmap with no of words: "+hm.size()+"\n\n");
			System.out.println("Total final size of queryterms: "+queryTerms.size());
	}

}
