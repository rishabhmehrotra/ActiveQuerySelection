import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class QuerySimilarity {
	
	public QDataset d;
	//public HashMap<String, HashMap<String, Float>> queryTerms;
	
	public QuerySimilarity(QDataset d) throws IOException
	{
		this.d = d;
		//populateQueryTerms();
		addQueryTermsToQuery();
		computeQueryTermSimilarity();
		computeQueryLDASimilarity();
	}
	
	public void computeQueryLDASimilarity()
	{
		Iterator<Query> itr = this.d.candidates.iterator();
		while(itr.hasNext())
		{
			Query q1 = itr.next();
			if(q1.qID==0) continue;
			Iterator<Query> itr2 = this.d.candidates.iterator();
			int c = 0; float sumSim = 0;
			while(itr2.hasNext())
			{
				Query q2 = itr2.next();
				if(q2.qID==0) continue;
				if(q1.qID != q2.qID)
				{
					float sim = 0;
					InstanceList testing = new InstanceList(d.ldaModel.instances.getPipe());
					testing.addThruPipe(new Instance(q1.termString, null, "test instance", null));
					testing.addThruPipe(new Instance(q2.termString, null, "test instance", null));
					TopicInferencer inferencer = d.ldaModel.model.getInferencer();
					double[] testProb1 = inferencer.getSampledDistribution(testing.get(0), d.numTopics, 1, 5);
					double[] testProb2 = inferencer.getSampledDistribution(testing.get(1), d.numTopics, 1, 5);
					// now compute cosine similarity between these LDA Topic distributions between both the queries
					float num = 0, d1=0,d2=0;
					for(int j=0;j<d.numTopics;j++)
			    	{
						num += (float) (testProb1[j]*testProb2[j]);
						d1 += (testProb1[j]*testProb1[j]);
						d2 += (testProb2[j]*testProb2[j]);
			    	}
					float den = (float) (Math.sqrt(d1)*Math.sqrt(d2));
					sim = num/den;
					//System.out.println("LDA Similarity Score:"+sim+"for queries: "+q1.qID+"_"+q1.termString+"___________________________"+q2.qID+" "+q2.termString);
					sumSim+= sim;
					c++;
				}
			}
			q1.similarityLDA = sumSim/c;//this is the avg LDA similarity with the rest of the candidates
			//if(sumSim == 0) {zeroDist++;if(zeroDist > 5) System.out.println("\n\n*************\n\nZeroDIstance: qid:"+q1.qID);}
			//System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Avg LDA sim for query "+q1.qID+" is: "+q1.similarityLDA);
		}
	}
	
	public void addQueryTermsToQuery()
	{
		//for(int i=0;i<this.nQ;i++)
		Iterator<Query> itr = d.candidates.iterator();
		while(itr.hasNext())
		{
			Query q = itr.next();
			if(q.qID == 0) continue;
			HashMap<String, Float> map = d.queryTerms.get(""+q.qID);
			q.setTermMap(map);
			String str = "";
			for(String words: map.keySet())
			{
				Float size = map.get(words);
				for(int j=1;j<=size;j++)
				{
					str += words+" ";
				}
			}
			str = str.trim();
			q.setTermString(str);
			//System.out.println(this.listOfQueries[i].qID+"QueryTermsString:_"+this.listOfQueries[i].termString+"_");
		}
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
