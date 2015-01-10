import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import edu.uci.jforests.applications.Runner;



public class DemoTesting {
	
	ArrayList<Query> base;
	public static ArrayList<Query> listTestQueries;
	Runner r;
	String[] args;
	public static String testFile = "src/data/LETOR/forEval/test.txt";
	public static String predictionFile = "src/data/LETOR/forEval/myscorefile.txt";
	public QDataset d;

	public static void main(String[] args) throws IOException {
		listTestQueries = new ArrayList<Query>();
		populateTestScoresFromFile();
		computeNDCG();
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
					if(q.qID!=0) listTestQueries.add(q);
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
	
	public static void computeNDCG() throws IOException
	{
		int nonrelevant = 0, total=0;
		int thresNDGC = 10;// remember to change it in RunRandomTest class as well
		// sort the documents for each query in base
		double avgNDCG=0.0;
		double avgAP=0.0;
		int totalCount=0, count4AP=0;
		Iterator<Query> itr = listTestQueries.iterator();
		FileWriter fstream1 = new FileWriter("src/data/LETOR/NDCG_errorBars.txt", true);
		BufferedWriter out1 = new BufferedWriter(fstream1);
		//out1.write(d.base.size()+"\t");
		FileWriter fstream2 = new FileWriter("src/data/LETOR/AP_errorBars.txt", true);
		BufferedWriter out2 = new BufferedWriter(fstream2);
		//out2.write(d.base.size()+"\t");
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
			if(count1 ==0 && count2 ==0) nonrelevant++;
			total++;
			
			if(count1 == 0 && count2 == 0) ;
			else
			{
				Iterator<Document> itr4 = q.listOfDocuments.iterator();
				int relevant=0, count3=0, numRel=0;
				double AP=0.0;
				while(itr4.hasNext())
				{
					Document d = itr4.next();
					count3++;
					if(d.relevance > 0)
					{
						relevant++;
						AP += (double) (relevant/count3);
						numRel++;
					}
					//System.out.println("relevance for this doc: "+d.relevance+"score: "+d.testScore+" AP+= "+relevant+" / "+count3);
					//if(count3 == 10) break;
				}
				System.out.println("AP: "+AP);
				AP = AP/numRel;
				q.AP = AP;
				count4AP++;
				avgAP += AP;
				out2.write(AP+"\t");
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
			
			//if(NDCG>1) NDCG=0;
			if(NDCG>=0 && NDCG<=1)
			{
				avgNDCG += NDCG;
				out1.write(NDCG+"\t");
			}
			totalCount++;
			
			//if(Double.isNaN(NDCG)) totalCount++;
			System.out.println("NDCG Score for Query "+q.qID+" is equal to "+q.NDCG);
			//System.exit(0);
		}
		out1.write("\n");
		out1.close();
		out2.write("\n");
		out2.close();
		System.out.println(avgNDCG);
		avgNDCG = avgNDCG/totalCount;
		avgAP = avgAP/count4AP;
		//d.resultCandidates = avgNDCG;
		//d.resultCandidatesAP = avgAP;
		System.out.println("No of queries with 0 1s and 0 2s: "+nonrelevant+ " & total no of queries: "+total);
		
		FileWriter fstream = new FileWriter("src/data/LETOR/forEval/resultsAT10.txt", true);
		BufferedWriter out = new BufferedWriter(fstream);
		//out.write("\n\n"+"avgNDCG for Candidates training size of "+d.base.size()+" queries= "+avgNDCG+"\n");
		//out.write("avgAP for Candidates training size of "+d.base.size()+" queries= "+avgAP+"\n");
		out.close();
		System.out.println("================= Average NDGC score for a total of "+listTestQueries.size()+" = "+totalCount+" queries: "+avgNDCG);
	}
}
