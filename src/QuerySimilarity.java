import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class QuerySimilarity {
	
	public QDataset d;
	//public HashMap<String, HashMap<String, Float>> queryTerms;
	
	public QuerySimilarity(QDataset d) throws IOException
	{
		this.d = d;
		//populateQueryTerms();
		computeQueryTermSimilarity();
	}
	
	private void computeQueryTermSimilarity() {
		// find similarity between queries from the entire current candidate set
		int zeroDist=0;
		Iterator<Query> itr = d.candidates.iterator();
		while(itr.hasNext())
		{
			Query q1 = itr.next();
			Iterator<Query> itr2 = d.candidates.iterator();
			int c = 0; float sumSim = 0;
			while(itr2.hasNext())
			{
				Query q2 = itr2.next();
				if(q1.qID != q2.qID)
				{
					HashMap<String, Float> map1 = d.queryTerms.get(""+q1.qID);
					HashMap<String, Float> map2 = d.queryTerms.get(""+q2.qID);
					float sim = 0;
					if(map1 == null || map2 == null)
					{
						//System.out.println("Got NULL in similarity maps with query sizes "+q1.nD+"_"+q2.nD);
					}
					else
						sim = cosSimilarityBetweenFreqMaps(map1, map2);
					sumSim+= sim;
					c++;
				}
			}
			q1.currentAvgSimilarity = sumSim/c;
			//if(sumSim == 0) {zeroDist++;if(zeroDist > 5) System.out.println("\n\n*************\n\nZeroDIstance: qid:"+q1.qID);}
			//System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Avg sim for query "+q1.qID+" is: "+q1.currentAvgSimilarity);
		}
	}

	public float cosSimilarityBetweenFreqMaps(HashMap<String, Float> map1, HashMap<String, Float> map2) {
		  float d1 = 0f, d2 = 0f;
		  for (Float v : map1.values())
		   d1 += v * v;
		  for (Float v : map2.values())
		   d2 += v * v;
		  float denominator = (float) (Math.sqrt(d1) * Math.sqrt(d2));
		  float numerator = 0f;
		  if (map1.size() <= map2.size()) {
		   for (String key : map1.keySet()) {
		    numerator += map1.get(key) * getWordFreqFrom(key, map2);
		   }
		  } else {
		   for (String key : map2.keySet()) {
		    numerator += map2.get(key) * getWordFreqFrom(key, map1);
		   }
		  }
		  return numerator / denominator;
	}
	
	public static float getWordFreqFrom(String word, HashMap<String, Float> map) {
		  Float count = map.get(word);
		  if (count == null)
		   return 0f;
		  else
		   return count;
	}
}
