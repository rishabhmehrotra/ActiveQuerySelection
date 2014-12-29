import java.util.*;

import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class SubmodularAL {

	public QDataset d;
	public double alpha = 0.5;

	public SubmodularAL(QDataset d)
	{
		this.d = d;
		populateV();
		populateQueryTopicProportions();
		
		// we now have each query's LDA topics
		calculateL();
		populateR();
	}

	

	public void calculateL()
	{
		// in this function we need to populate the L function of the submodular function
		// for each query, we would need to calculate the L score
		Iterator<Query> itr1, itr2;
		double sim=0;
		// calculate Cv
		itr1 = d.V.iterator();
		while(itr1.hasNext())
		{
			Query q1 = itr1.next();
			q1.wWithOthers = 0;
			itr2 = d.V.iterator();
			while(itr2.hasNext())
			{
				Query q2 = itr2.next();
				sim = getQueryPairLDASimilarity(q1, q2);
				q1.wWithOthers+=sim;
			}
			System.out.println("wWithOthers: "+q1.wWithOthers);
		}
		
		
		// now we iterate through the candidate list to form potential subsets S
		double L=0;
		// find common cS part for each query in V
		itr1 = d.V.iterator();
		while(itr1.hasNext())
		{
			Query qi = itr1.next();
			qi.wWithBase = 0;
			itr2 = d.base.iterator();
			while(itr2.hasNext())
			{
				Query qj = itr2.next();
				qi.wWithBase += getQueryPairLDASimilarity(qi, qj);
			}
			System.out.println("wWithBase: "+qi.wWithOthers);
		}
		
		Iterator<Query> itrS = d.candidates.iterator();
		while(itrS.hasNext())
		{
			L = 0;
			Query qS = itrS.next();
			double cS = 0, cV=0;
			itr1 = d.V.iterator();
			while(itr1.hasNext())
			{
				Query qi = itr1.next();
				cS = qi.wWithBase + getQueryPairLDASimilarity(qi, qS);
				cV = qi.wWithOthers;
				L += Math.min(cS, alpha*cV);
				System.out.println("cS: "+cS+" cV: "+cV);
			}
			// now for this set (base+qS) we have the L score
			qS.LScore = L;
			System.out.println("LSCORE ========================== "+L);
		}
	}

	public void populateV()
	{
		d.V = new ArrayList<Query>();
		Iterator<Query> itr1 = d.candidates.iterator();
		while(itr1.hasNext())
		{
			Query qi = itr1.next();
			d.V.add(qi);
		}
		Iterator<Query> itr2 = d.base.iterator();
		while(itr2.hasNext())
		{
			Query qj = itr2.next();
			d.V.add(qj);
		}
		System.out.println("Populated all query list V with "+d.V.size()+" queries.");
		//System.exit(0);
	}

	public void populateQueryTopicProportions()
	{
		Iterator<Query> itr = d.V.iterator();
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
	
	public double getQueryPairLDASimilarity(Query q1, Query q2)
	{
		double sim = 0;
		double num = 0, d1=0,d2=0;
		for(int j=0;j<d.numTopics;j++)
		{
			//System.out.print(q1.topicProportions[j]+"-------");
			num += (float) (q1.topicProportions[j]*q2.topicProportions[j]);
			d1 += (q1.topicProportions[j]*q1.topicProportions[j]);
			d2 += (q2.topicProportions[j]*q2.topicProportions[j]);
		}
		//System.out.println();
		double den = (double) (Math.sqrt(d1)*Math.sqrt(d2));
		//System.out.println("num:"+num+" den:"+den);
		//if(num ==0 || den==0) System.out.println(q1.topicProportions[0]+" "+q2.topicProportions[0]);
		sim = num/den;
		if(sim>10000) {System.out.println("inside getQueryPairLDASimilarity in Submodular class, NaN similarity case happened");System.exit(0);}
		return sim;
	}

	public void populateR()
	{

	}
}
