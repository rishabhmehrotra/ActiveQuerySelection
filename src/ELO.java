import java.io.*;
import java.util.*;

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
			Iterator<Document> itr1 = q.listOfDocuments.iterator();
			while(itr1.hasNext())
			{
				Document d = itr1.next();
				gj1.add(new Float(d.score1));
				gj2.add(new Float(d.score2));
				gj3.add(new Float(d.score3));
				gj4.add(new Float(d.score4));
			}
			q.d1 = BDCG(gj1);
			q.d2 = BDCG(gj2);
			q.d3 = BDCG(gj3);
			q.d4 = BDCG(gj4);
		}
	}
	
	public float BDCG(ArrayList<Float> gj)
	{
		float bdcg = 0.0f;
		for(int i=0;i<gj.size();i++)
		{
			float temp = gj.get(i);
			
		}
		return bdcg;
	}
}
