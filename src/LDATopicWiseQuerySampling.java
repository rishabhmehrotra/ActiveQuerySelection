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

	public ArrayList<Query> getMinMaxPLQueryFromTopics_TopLDASim()//min-max from each topic
	{
		ArrayList<Query> top10 = new ArrayList<Query>();
		HashMap<Integer, ArrayList<Query>> topSimilarQ = new HashMap<Integer, ArrayList<Query>>();
		for(int i=1;i<=d.numTopics;i++)
		{
			ArrayList<Query> topSim = new ArrayList<Query>();
			ArrayList<Query> topiclist = this.topiclistMap.get(new Integer(i));
			
			// the idea is that we will create a new array list with all queries from this topic along with their LDA similarity scores
			// and then sort it and use the top 10 to select via PL probability
			
			float maxTopiclistSim = -1000;Query maxQ = null;
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
						float sim = 0; float num = 0, d1=0,d2=0;
						double[] testProb2 = q2.getTopicProportions();//inferencer.getSampledDistribution(testing.get(1), d.numTopics, 1, 5);
						// now compute cosine similarity between these LDA Topic distributions between both the queries
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
				q1.topiclistSimilarity = sumSim/c;
				topSim.add(q1);
			}
			
			// now we have all the queries of this topic with their similarity scores in the array list topSim
			// now sort this array list and pick one from the top 10
			
			Collections.sort(topSim, new Comparator<Query>()  
					{

						public int compare(Query q1, Query q2) {
							if(q1.topiclistSimilarity < q2.topiclistSimilarity) return 1;
							else if(q1.topiclistSimilarity > q2.topiclistSimilarity) return -1;
							else return 0;
						}
					  
					});
			
			// now just look at the top 10 (say) queries and pick one which has the least min-max PL probability
			double max_qMaxPL = -1000, min_qMaxPL = 1000;
			Query minPLQ = null;
			for(int j=0;j<10;j++)
			{
				Query q = topSim.get(j);
				if(q.qID==0) continue;
				double qMaxPL = getMax(q.PL1,q.PL2,q.PL3,q.PL4);
				
				if(qMaxPL < min_qMaxPL)
				{
					min_qMaxPL = qMaxPL;minPLQ = q;
					//System.out.println("new min found_"+min_qMaxPL+ "_for query: "+ q.qID);
				}
			}
			top10.add(minPLQ);
			topiclist.remove(minPLQ);
			this.topiclistMap.put(new Integer(i), topiclist);
		}
		return top10;
	}
	
	public ArrayList<Query> getMinMaxPLQueryFromTopics()//min-max from each topic
	{
		ArrayList<Query> top10 = new ArrayList<Query>();
		while(top10.size()<10)
		for(int i=1;i<=d.numTopics;i++)
		{
			if(top10.size()==d.numTopics) break;
			int flag = 1;
			ArrayList<Query> topiclist = null;
			while(flag ==1)
			{
				topiclist = this.topiclistMap.get(new Integer(i));
				if(topiclist.size()<3) {i++;topiclist = this.topiclistMap.get(new Integer(i));}
				else flag = 0;
				if(i==11) i=1;
			}
			 
			
			double max_qMaxPL = -1000, min_qMaxPL = 1000;
			Query minPLQ = null;// from this topic, we have to select the query which has the minimum PL among all queries(among the max of its 4 rankers)
			Iterator<Query> itr = topiclist.iterator();
			while(itr.hasNext())
			{
				Query q = itr.next();
				if(q.qID==0) continue;
				double qMaxPL = getMax(q.PL1,q.PL2,q.PL3,q.PL4);
				
				if(qMaxPL < min_qMaxPL)
				{
					min_qMaxPL = qMaxPL;minPLQ = q;
					//System.out.println("new min found_"+min_qMaxPL+ "_for query: "+ q.qID);
				}
				//if(qMaxPL > max_qMaxPL) max_qMaxPL = qMaxPL;
			}
			// now we know which query has the max similarity, now we need to remove it from the topic wise list and add it to the list of 10 recommendations
			topiclist.remove(minPLQ);
			this.topiclistMap.put(new Integer(i), topiclist);
			top10.add(minPLQ);
			if(top10.size()==d.numTopics) break;
			//System.out.println("adding query to top10;qID= :"+minPLQ.qID+" ---------------------------------------- from topic "+i);
		}// end of for loop for 1-10 10=d.numTopics
		//System.exit(0);
		return top10;
	}
	
	public ArrayList<Query> computeTopicWiseSimilarity()
	{
		ArrayList<Query> top10 = new ArrayList<Query>();
		while(top10.size()<10)
		for(int i=1;i<=d.numTopics;i++)
		{
			if(top10.size()==d.numTopics) break;
			int flag = 1;
			ArrayList<Query> topiclist = null;
			while(flag ==1)
			{
				topiclist = this.topiclistMap.get(new Integer(i));
				if(topiclist.size()<3) {i++;topiclist = this.topiclistMap.get(new Integer(i));}
				else flag = 0;
				if(i==11) i=1;
			}
			 
			
			float maxTopiclistSim = -1000;Query maxQ = null;
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
						float sim = 0; float num = 0, d1=0,d2=0;
						double[] testProb2 = q2.getTopicProportions();//inferencer.getSampledDistribution(testing.get(1), d.numTopics, 1, 5);
						// now compute cosine similarity between these LDA Topic distributions between both the queries
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
				q1.topiclistSimilarity = sumSim/c;
				if(q1.topiclistSimilarity > maxTopiclistSim)
				{
					maxTopiclistSim = q1.topiclistSimilarity;
					maxQ = q1;
				}
			}
			// now we know which query has the max similarity, now we need to remove it from the topic wise list and add it to the list of 10 recommendations
			topiclist.remove(maxQ);
			this.topiclistMap.put(new Integer(i), topiclist);
			top10.add(maxQ);
			if(top10.size()==d.numTopics) break;
			System.out.println("adding query to top10;qID= :"+maxQ.qID+" ---------------------------------------- from topic "+i);
		}// end of for loop for 1-10 10=d.numTopics
		//System.exit(0);
		return top10;
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
			//System.out.println(testProb1[0]+"_"+testProb1[1]+"_"+testProb1[2]+"_"+testProb1[3]+"_");
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
	
	public double getMax(double p1, double p2, double p3, double p4) {
		// TODO Auto-generated method stub
		if(p1 > p2 && p1 > p3 && p1 > p4) return p1;
		if(p2 > p1 && p2 > p3 && p2 > p4) return p2;
		if(p3 > p1 && p3 > p2 && p3 > p4) return p3;
		if(p4 > p1 && p4 > p2 && p4 > p3) return p4;
		return 0;
	}
}
