import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

@SuppressWarnings("unused")
public class LDATopicWiseQuerySampling {
	
	public ArrayList<Query> topiclist1, topiclist2, topiclist3, topiclist4, topiclist5, topiclist6, topiclist7, topiclist8, topiclist9, topiclist10;
	public QDataset d;
	public HashMap<Integer, ArrayList<Query>> topiclistMap;
	
	public LDATopicWiseQuerySampling(QDataset d)
	{
		this.d = d;
		this.topiclistMap = new HashMap<Integer, ArrayList<Query>>();
		this.topiclist1 = new ArrayList<Query>();
		this.topiclist2 = new ArrayList<Query>();
		this.topiclist3 = new ArrayList<Query>();
		this.topiclist4 = new ArrayList<Query>();
		this.topiclist5 = new ArrayList<Query>();
		this.topiclist6 = new ArrayList<Query>();
		this.topiclist7 = new ArrayList<Query>();
		this.topiclist8 = new ArrayList<Query>();
		this.topiclist9 = new ArrayList<Query>();
		this.topiclist10 = new ArrayList<Query>();
		populateQueryTopicProportions();
		populateTopicWiseQueryList();
	}
	
	public void computeTopicWiseSimilarity()
	{
		Iterator<ArrayList<Query>> itr_t = this.topiclistMap.values().iterator();
		while(itr_t.hasNext())
		{
			ArrayList<Query> topiclist = itr_t.next();

			Iterator<Query> itr = topiclist.iterator();
			while(itr.hasNext())
			{
				Query q1 = itr.next();
				if(q1.qID==0) continue;
				
				double[] testProb1 = q1.getTopicProportions();
				
				Iterator<Query> itr2 = topiclist.iterator();
				int c = 0; float sumSim = 0;
				while(itr2.hasNext())
				{
					Query q2 = itr2.next();
					if(q2.qID==0) continue;
					if(q1.qID != q2.qID)
					{
						float sim = 0;
						double[] testProb2 = q2.getTopicProportions();//inferencer.getSampledDistribution(testing.get(1), d.numTopics, 1, 5);
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
				q1.similarityLDA = sumSim/c;
				//if(sumSim == 0) {zeroDist++;if(zeroDist > 5) System.out.println("\n\n*************\n\nZeroDIstance: qid:"+q1.qID);}
				//System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Avg LDA sim for query "+q1.qID+" is: "+q1.similarityLDA);
			}
		}
	}
	
	public void populateQueryTopicProportions()
	{
		Iterator<Query> itr = d.candidates.iterator();
		int c = 0;
		while(itr.hasNext())
		{
			Query q = itr.next();
			InstanceList testing = new InstanceList(d.ldaModel.instances.getPipe());
			testing.addThruPipe(new Instance(q.termString, null, "test instance", null));
			TopicInferencer inferencer = d.ldaModel.model.getInferencer();
			double[] testProb1 = inferencer.getSampledDistribution(testing.get(0), d.numTopics, 1, 5);	
			q.setTopicProportions(testProb1);
			c++;
		}
		System.out.println("Topic Proportions populated for "+c+" quries.");
	}
	
	public void populateTopicWiseQueryList()
	{
		Iterator<Query> itr = d.candidates.iterator();
		while(itr.hasNext())
		{
			Query q = itr.next();
			double topicProb[] = q.getTopicProportions();
			int max = 0; double maxProb=-1;
			for(int i=0;i<d.numTopics;i++)
			{
				if(topicProb[i] > maxProb)
				{
					maxProb = topicProb[i];
					max = i;
				}
			}
			// now we need to insert this query into the arraylist corresponding to topic i
			if(max == 0) this.topiclist1.add(q);
			if(max == 1) this.topiclist2.add(q);
			if(max == 2) this.topiclist3.add(q);
			if(max == 3) this.topiclist4.add(q);
			if(max == 4) this.topiclist5.add(q);
			if(max == 5) this.topiclist6.add(q);
			if(max == 6) this.topiclist7.add(q);
			if(max == 7) this.topiclist8.add(q);
			if(max == 8) this.topiclist9.add(q);
			if(max == 9) this.topiclist10.add(q);
		}
		System.out.println("populated the topic wise query lists with sizes:");
		System.out.println(this.topiclist1.size());
		System.out.println(this.topiclist2.size());
		System.out.println(this.topiclist3.size());
		System.out.println(this.topiclist4.size());
		System.out.println(this.topiclist5.size());
		System.out.println(this.topiclist6.size());
		System.out.println(this.topiclist7.size());
		System.out.println(this.topiclist8.size());
		System.out.println(this.topiclist9.size());
		System.out.println(this.topiclist10.size());
		
		// put everything inside the hashmap now
		topiclistMap.put(new Integer(1), this.topiclist1);
		topiclistMap.put(new Integer(2), this.topiclist2);
		topiclistMap.put(new Integer(3), this.topiclist3);
		topiclistMap.put(new Integer(4), this.topiclist4);
		topiclistMap.put(new Integer(5), this.topiclist5);
		topiclistMap.put(new Integer(6), this.topiclist6);
		topiclistMap.put(new Integer(7), this.topiclist7);
		topiclistMap.put(new Integer(8), this.topiclist8);
		topiclistMap.put(new Integer(9), this.topiclist9);
		topiclistMap.put(new Integer(10), this.topiclist10);
	}
	
}
