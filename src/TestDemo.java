import java.io.*;
import java.util.*;
import java.io.*;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import edu.uci.jforests.config.TrainingConfig;
import edu.uci.jforests.dataset.Dataset;
import edu.uci.jforests.dataset.DatasetLoader;
import edu.uci.jforests.dataset.RankingDataset;
import edu.uci.jforests.dataset.RankingDatasetLoader;
import edu.uci.jforests.input.RankingRaw2BinConvertor;
import edu.uci.jforests.input.Raw2BinConvertor;
import edu.uci.jforests.learning.LearningUtils;
import edu.uci.jforests.learning.trees.Ensemble;
import edu.uci.jforests.learning.trees.decision.DecisionTree;
import edu.uci.jforests.learning.trees.regression.RegressionTree;
import edu.uci.jforests.sample.RankingSample;
import edu.uci.jforests.sample.Sample;
import edu.uci.jforests.util.IOUtils;
import edu.uci.jforests.applications.*;


public class TestDemo {
	
	public static ArrayList<Query> random;
	public static ArrayList<Query> listTestQueries;
	Runner r;
	String[] args;
	public static String testFile = "src/data/LETOR/forEval/test.txt";
	public static String predictionFile = "src/data/LETOR/forEval/randomQ/forEval_random_predictions.txt";
	//public static String predictionFile = "src/data/LETOR/forEval/forEval_predictions.txt";
	public QDataset d;
	
	public static void main(String[] args) throws IOException
	{
		//this.random = random;
		listTestQueries = new ArrayList<Query>();
		//this.d = d;
		//System.out.println("\n\nThe system is now going to perform evaluation for "+random.size()+" number of queries");
		
		
		populateTestScoresFromFile();
		computeNDCG1();
	}

	public static void populateTestScoresFromFile()
	{
		BufferedReader br, br1;
		try {
			br = new BufferedReader(new FileReader(testFile));
			br1 = new BufferedReader(new FileReader(predictionFile));
			String line = br.readLine();
			String line1 = br1.readLine();
			
			String prevQID = "";
			Query q = new Query();
			int c=0, nTQ=0;
			Double testScore;
			
			while(line != null)
			{
				String qID = line.substring(6, line.indexOf(' ', line.indexOf(' ', 6)));
				//System.out.println("WWR qID= "+qID);
				testScore = Double.parseDouble(line1);
				if(prevQID.compareTo(qID) != 0)
				{
					//System.out.println("new query found:"+prevQID+" "+qID);
					// new query found
					//first add previous query to the list of queries in the dataset
					//listOfCandidateQueries[nTQ++] = q;
					listTestQueries.add(q);
					nTQ++;
					c+=q.nD;
					q = new Query();
				}
				prevQID = qID;
				q.addDoc(line, testScore);
				if(nTQ%1000 == 0) System.out.println(qID);
				
				line = br.readLine();
				line1 = br1.readLine();
			}
			//listOfCandidateQueries[nTQ++] = q;
			listTestQueries.add(q);
			nTQ++;
			//d.setListOfCandidateQueries(listOfCandidateQueries);
			//d.setnTQ(nTQ);
			//d.setCandidates(listOfCandidates);
			//d.setnCandidateQ(nCQ);
			c+= q.nD;
			//for(int kk=0;kk<nCQ;kk++) System.out.println(listOfCandidates.get(kk).listOfDocuments.size());
			System.out.println("No of test queries populated with their respective scores nCQ= "+nTQ);
			System.out.println("cT= "+c);
		} catch (FileNotFoundException e) {e.printStackTrace();} catch (IOException e) {e.printStackTrace();}
	}
	
	public static void computeNDCG1() throws IOException
	{
		int thresNDGC = 10;// remember to change it in RunRandomTest class as well
		// sort the documents for each query in base
		double avgNDCG=0.0;
		int totalCount=0;
		Iterator<Query> itr = listTestQueries.iterator();
		
		while(itr.hasNext())
		{
			Query q = itr.next();
			// now we need to sort the documents in this query based on the testScores they got
			
			
			Collections.sort(q.listOfDocuments, new Comparator<Document>()  
					{

						public int compare(Document d1, Document d2) {
							if(d1.testScore < d2.testScore) return 1;
							else if(d1.testScore > d2.testScore) return -1;
							else return 0;
						}
					  
					});
			Iterator<Document> itr3 = q.listOfDocuments.iterator();
			int count = 0, count0 = 0, count1 = 0, count2 = 0;
			while(itr3.hasNext())
			{
				Document d = itr3.next();
				if(d.relevance == 0) count0++;
				if(d.relevance == 1) count1++;
				if(d.relevance == 2) count2++;
			}
			// now we have the documents for this query sorted
			//System.out.print("qID:"+q.qID+"___");
			Iterator<Document> itr2 = q.listOfDocuments.iterator();
			double DCG=0.0, IDCG=0.0;
			while(itr2.hasNext()){
				Document d = itr2.next();
				count++;
				if(count==1) {DCG+= d.relevance;/*IDCG+= d.relevance;*/System.out.print("__dcg+="+d.relevance+"with score:"+d.testScore);}
				else
				{
					DCG+= (d.relevance/Math.log(count));
					System.out.print("  dcg+= "+d.relevance+"/Math.log "+count+"with score:"+d.testScore);
				}
				//System.out.print(d.relevance+" ~ "+d.testScore+"_______");
				if(count == thresNDGC) break;
			}
			System.out.println("0s: "+count0+"1s: "+count1+"___2s: "+count2);
			//calculate IDCG now
			int c = 1;
			while(count2>0 && c<=thresNDGC)
			{
				if(c==1) {IDCG+=2;System.out.print("IDCG+=2");}
				else {IDCG+= (2/Math.log(c));System.out.print("  2/Math.log "+c);}
				count2--;
				c++;
			}
			while(count1>0 && c<=thresNDGC)
			{
				if(c==1) {IDCG+=1;System.out.print("IDCG+=2");}
				else {IDCG+= (1/Math.log(c));System.out.print("  1/Math.log "+c);}
				count1--;
				c++;
			}
			System.out.print("\nqID:"+q.qID+"___DCG= "+DCG+" IDCG= "+IDCG+"_____");
			double NDCG = DCG/IDCG;
			q.NDCG = NDCG;
			
			if(NDCG>=0 && NDCG<=1)
			{
				avgNDCG += NDCG;totalCount++;
				
			}
			System.out.println("NDCG Score for Query "+q.qID+" is equal to "+q.NDCG);
			//System.exit(0);
		}
		
		System.out.println(avgNDCG);
		avgNDCG = avgNDCG/totalCount;
		//d.resultCandidates = avgNDCG;
		
		
		System.out.println("================= Average NDGC score for a total of "+listTestQueries.size()+" = "+totalCount+" queries: "+avgNDCG);
	}
	
	public static void computeNDCG() throws IOException
	{
		int thresNDGC = 10;
		// sort the documents for each query in base
		double avgNDCG=0.0;
		int totalCount=0;
		Iterator<Query> itr = listTestQueries.iterator();
		//FileWriter fstream1 = new FileWriter("src/data/LETOR/NDCG_RANDOM_errorBars.txt", true);
		//BufferedWriter out1 = new BufferedWriter(fstream1);
		//out1.write(d.base.size()+"\t");
		while(itr.hasNext())
		{
			Query q = itr.next();
			// now we need to sort the documents in this query based on the testScores they got
			
			
			Collections.sort(q.listOfDocuments, new Comparator<Document>()  
					{

						public int compare(Document d1, Document d2) {
							if(d1.testScore < d2.testScore) return 1;
							else if(d1.testScore > d2.testScore) return -1;
							else return 0;
						}
					  
					});
			Iterator<Document> itr3 = q.listOfDocuments.iterator();
			int count = 0, count0 = 0, count1 = 0, count2 = 0;
			while(itr3.hasNext())
			{
				Document d = itr3.next();
				if(d.relevance == 0) count0++;
				if(d.relevance == 1) count1++;
				if(d.relevance == 2) count2++;
			}
			// now we have the documents for this query sorted
			//System.out.print("qID:"+q.qID+"___");
			Iterator<Document> itr2 = q.listOfDocuments.iterator();
			double DCG=0.0, IDCG=0.0;
			while(itr2.hasNext()){
				Document d = itr2.next();
				count++;
				if(count==1) {DCG+= d.relevance;/*IDCG+= d.relevance;*/}
				else
				{
					DCG+= (d.relevance/Math.log(count));
				}
				//System.out.print(d.relevance+" ~ "+d.testScore+"_______");
				if(count == thresNDGC) break;
			}
			//calculate IDCG now
			System.out.println("0s: "+count0+"1s: "+count1+"___2s: "+count2);
			//calculate IDCG now
			int c = 1;
			while(count2>0 && c<=thresNDGC)
			{
				if(c==1) {IDCG+=2;System.out.print("IDCG+=2");}
				else {IDCG+= (2/Math.log(c));System.out.print("  2/Math.log "+c);}
				count2--;
				c++;
			}
			while(count1>0 && c<=thresNDGC)
			{
				if(c==1) {IDCG+=1;System.out.print("IDCG+=2");}
				else {IDCG+= (1/Math.log(c));System.out.print("  1/Math.log "+c);}
				count1--;
				c++;
			}

			//System.out.print("qID:"+q.qID+"___DCG= "+DCG+" IDCG= "+IDCG+"_____");
			double NDCG = DCG/IDCG;
			q.NDCG = NDCG;
			
			if(NDCG>=0 && NDCG<=1)
			{
				avgNDCG += NDCG;totalCount++;
				//out1.write(NDCG+"\t");
			}
			//System.out.println("NDCG Score for Query "+q.qID+" is equal to "+q.NDCG);
		}
		//out1.write("\n");
		//out1.close();
		System.out.println(avgNDCG);
		avgNDCG = avgNDCG/totalCount;
		//d.resultRandom = avgNDCG;
		System.out.println("================= Average NDGC score for a total of "+listTestQueries.size()+" = "+totalCount+" RANDOM queries: "+avgNDCG);
	}
	
}











/*public class TestDemo {

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
*/