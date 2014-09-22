import java.io.*;
import java.util.*;

// this class implements the Query Selection algorithm described in the SIGIR 2010 paper Active Learning for Ranking through Expected Loss Optimization

public class ELO {
	
	public QDataset d;
	
	public ELO(QDataset d)
	{
		this.d = d;
		calculateELScoreForEachQuery();
	}
	
	public void calculateELScoreForEachQuery()
	{
		Iterator<Query> itr = d.candidates.iterator();
		while(itr.hasNext())
		{
			Query q = itr.next();
			// now for this query we have to calculate the EL score
			ArrayList<Float> gj1 = new ArrayList<Float>();
			ArrayList<Float> gj2 = new ArrayList<Float>();
			ArrayList<Float> gj3 = new ArrayList<Float>();
			ArrayList<Float> gj4 = new ArrayList<Float>();
			ArrayList<Float> gj_avg = new ArrayList<Float>();
			Iterator<Document> itr1 = q.listOfDocuments.iterator();
			double max1 = -1000.0, min1 = 1000;
			double max2 = -1000.0, min2 = 1000;
			double max3 = -1000.0, min3 = 1000;
			double max4 = -1000.0, min4 = 1000;
			while(itr1.hasNext())
			{
				Document d = itr1.next();
				if(d.score1 < min1) min1 = d.score1;
				if(d.score2 < min2) min2 = d.score2;
				if(d.score3 < min3) min3 = d.score3;
				if(d.score4 < min4) min4 = d.score4;
				if(d.score1 > max1) max1 = d.score1;
				if(d.score2 > max2) max2 = d.score2;
				if(d.score3 > max3) max3 = d.score3;
				if(d.score4 > max4) max4 = d.score4;
			}
			
			
			itr1 = q.listOfDocuments.iterator();
			while(itr1.hasNext())
			{
				Document d = itr1.next();
				
				double nd1 = 5*(d.score1 - min1)/(max1-min1);
				double nd2 = 5*(d.score2 - min2)/(max2-min2);
				double nd3 = 5*(d.score3 - min3)/(max3-min3);
				double nd4 = 5*(d.score4 - min4)/(max4-min4);
				
				//System.out.println("-----------------------------------------------normalized score: "+nd1+"_"+nd2+"_"+nd3+"_"+nd4);
				
				/*float d1 = (float) Math.pow(2, d.score1) -1;
				float d2 = (float) Math.pow(2, d.score2) -1;
				float d3 = (float) Math.pow(2, d.score3) -1;
				float d4 = (float) Math.pow(2, d.score4) -1;*/
				float d1 = (float) Math.pow(2, nd1) -1;
				float d2 = (float) Math.pow(2, nd2) -1;
				float d3 = (float) Math.pow(2, nd3) -1;
				float d4 = (float) Math.pow(2, nd4) -1;
				float davg = (d1+d2+d3+d4)/4;
				gj1.add(d1);
				gj1.add(d2);
				gj1.add(d3);
				gj1.add(d4);
				gj_avg.add(davg);
			}
			q.d1 = BDCG(gj1);
			q.d2 = BDCG(gj2);
			q.d3 = BDCG(gj3);
			q.d4 = BDCG(gj4);
			q.d = BDCG(gj_avg);
			
			q.ELScore = ((q.d1 + q.d2 + q.d3 + q.d4)/4) - q.d;
			// now the second phase inside the query loop in  Algorithm 1 in ELO paper
		}
	}
	
	public float BDCG(ArrayList<Float> gj)
	{
		float bdcg = 0.0f;
		Collections.sort(gj);
		for(int j=0;j<gj.size();j++)
		{
			float temp = 0.0f;
			float num = gj.get(j);
			float pi_j = (float) j+1;//this is because the rank in sorted gj is just its current position, since we sorted gj befre starting of this loop
			float den = (float) (Math.log(1+pi_j)/Math.log(2));
			if(den>0) temp = num/den;
			bdcg += temp;
		}
		return bdcg;
	}
}
