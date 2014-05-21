import java.io.*;
import java.util.*;

public class Driver {
	
	public static QDataset d;
	
	public static void main(String args[]) throws IOException
	{
		System.out.println("Hello! World.");
		
		
		
		//String filename = "src/data/set2.train.txt";
		String filename = "src/data/LETOR/train.txt";
		d = new QDataset(filename);
		System.out.println("Total No of queries= "+d.nQ);
		System.out.println("Total no of queries in Base Set= "+d.base.size());
		
		int N = 100;
		
		int i,j,k,l,m;
		int sizeDl = d.base.size();
		int sizeSubset = (int) (0.5*sizeDl);
		
		for(i=1;i<=N;i++)
		{
			//deleteBinFiles();
			//obtain the two sample subsets of size sizeSubset
			sizeSubset = (int) (0.5*d.nBase);
			d.subset1 = (ArrayList<Query>) randomSample(d.base,sizeSubset);
			d.subset2 = (ArrayList<Query>) randomSample(d.base,sizeSubset);
			// make the training files for both the subsets
			prepareTrainFiles(d.subset1,1);
			prepareTrainFiles(d.subset2,2);
			prepareTrainFiles(d.subset2,3);
			prepareTrainFiles(d.subset2,4);
			prepareCandidateFile(d.candidates);
			// now that we have both the subsets, we have to train 2 models on these 2 subsets
			new RunLearningAlgorithm(d.subset1,1);
			new RunLearningAlgorithm(d.subset2,2);
			new RunLearningAlgorithm(d.subset2,3);
			new RunLearningAlgorithm(d.subset2,4);
			// now we have both the models in the ensemble1/2.txt files, we need to measure the disagreement for each query among these 2 models
			// the queries-doc pairs are present line by line in the test.txt file and corresponding lines in the predictions1/2.txt file contain the
			// scores assigned by the models to each of these query-doc pairs..
			// we now need to store these results in a data structure by reading line by line both these files
			new WorkWithResults("src/data/LETOR/candidate.txt",1, d, "src/data/LETOR/predictions1.txt", "src/data/LETOR/predictions2.txt", "src/data/LETOR/predictions3.txt", "src/data/LETOR/predictions4.txt");
			// so now we have all the test queries populated with their respective scores from each of the models learnt
			// and we have populated the disagreement scores for all the queries...we now have to find query with maximum vote entropy/disagreement
			
			
			int batch = d.experimentSize;
			while(batch>0)
			{
				new QuerySimilarity(d);
				batch--;
				// we need to normalize the disagreement to make it a value between 0 & 1
				double maxDisagreement = 0, minDisagreement = 100000;
				Iterator<Query> it = d.candidates.iterator();
				while(it.hasNext())
				{
					Query q = it.next();
					if(q.disagreement < minDisagreement) minDisagreement = q.disagreement;
					if(q.disagreement > maxDisagreement) maxDisagreement = q.disagreement;
				}
				System.out.println("Max disagreement: "+maxDisagreement+" min disagreement: "+minDisagreement);
				it = d.candidates.iterator();
				while(it.hasNext())
				{
					Query q = it.next();
					Double normalizedDisagreement = q.disagreement/maxDisagreement;
					q.setNormalizedDisagreement(normalizedDisagreement);
				}
		
				
				Query next = null, nextBySim = null, nextByCombined = null; double max=0, maxSimilarity=0, maxCombined=0;

				Iterator<Query> itr = d.candidates.iterator();
				//for(j=0;j<d.nCandidateQ;j++)
				while(itr.hasNext())
				{
					Query q = itr.next();
					//Query q = d.listOfCandidateQueries[j];
					if(q.currentAvgSimilarity >= maxSimilarity) {maxSimilarity = q.currentAvgSimilarity;nextBySim = q;}
					if(q.disagreement > max) {max = q.disagreement;next = q;}
					double combine = q.normalizedDisagreement*q.currentAvgSimilarity;
					q.combine = combine;
					if(combine > maxCombined) {maxCombined = combine;nextByCombined = q;}
					//System.out.println("For query with qID= "+q.qID+" the disagreement is= "+q.disagreement);
				}
				// now we have the query which should be used next and we need to add it to the base list on which we should train henceforth
				if(next == null) System.out.println("/n/nThe NEXT query selected is NULL, something went wrong, have a look!/n/n");
				//d.base.add(next);
				System.out.println("Comparing Disagreement & Similarity:\n By Disagreement "+next.disagreement+ "__"+ next.currentAvgSimilarity+" "+next.combine+"\n By Similarity: "+nextBySim.disagreement+"__"+nextBySim.currentAvgSimilarity+" "+nextBySim.combine+"\n By Combine"+nextByCombined.disagreement+"__"+nextByCombined.currentAvgSimilarity+" "+nextByCombined.combine);
				d.base.add(nextBySim);
				d.nBase++;
				System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^Max similarity query being removed which has sim= "+nextBySim.currentAvgSimilarity);
				//System.out.println("---------- Query being removed now: "+next.qID+"_"+next.nD+"_"+next.disagreement+"_"+next.listOfDocuments.get(0));
				// now we need to remove this particular selected NEXT query from the list of candidates
				//System.out.println("Size before query removal from candidate set: "+d.nCandidateQ);
				//removeQueryFromCandidateSet(next);
				removeQueryFromCandidateSet(nextBySim);
				//System.out.println("Size after query removal from candidate set: "+d.nCandidateQ);
				//System.out.println("Size of the new base set: "+d.base.size());
			}
			// now we have the new base set ready, we should extract the subset from it now and see how it performs
			// also, we need code for converting scores to NDCG measure now...
			new RunTestingAlgorithm(d.base, d);
			double avg = 0;
			for(int ii=0;ii<5;ii++)
			{
				File dir = new File("src/data/LETOR/forEval/randomQ/");
				for(File files: dir.listFiles()) files.delete();
				populateRandomQueries();
				new RunRandomTestingAlgorithm(d.randomQ, d);
				avg += d.resultRandom;
			}
			FileWriter fstream = new FileWriter("src/data/LETOR/forEval/resultsAT10.txt", true);
			BufferedWriter out = new BufferedWriter(fstream);
			avg/= 5;
			out.write("Average for this run: "+avg+"\n");
			out.close();
		}
		
		
		/*double randomResults = 0.0, sumRandom = 0.0;
		{
			deleteBinFiles();
			populateRandomQueries();
			new RunRandomTestingAlgorithm(d.randomQ, d);
			randomResults = d.resultRandom;
			System.out.print(randomResults+"  ");
			sumRandom += randomResults;
		}
		d.resultRandom = sumRandom/5;*/
		System.out.println("Final Results: Random: "+d.resultRandom+" Candidates: "+d.resultCandidates);
	}
	
	public static void populateRandomQueries() throws IOException
	{
		int size = d.base.size();
		// now we need to get a random dataset of size same as that of base from among the forst 300 queries
		ArrayList<Query> basePlusCandidate = new ArrayList<Query>();
		for(int i=0;i<d.limit;i++)
		{
			basePlusCandidate.add(d.listOfQueries[i]);
		}
		d.setRandomQ((ArrayList<Query>) randomSample(basePlusCandidate,size));
		// now write it to file
		System.out.println("Size of random collection= "+d.randomQ.size());
		FileWriter fstream = new FileWriter("src/data/LETOR/forEval/randomQ/forEval_random_train.txt");
		BufferedWriter out = new BufferedWriter(fstream);
		Iterator<Query> itr = d.randomQ.iterator();
		while(itr.hasNext())
		{
			Query q = itr.next();
			int nD = q.nD;
			Iterator<Document> itr2 = q.listOfDocuments.iterator();
			while(itr2.hasNext())
			{
				Document d = itr2.next();
				String s = d.docFeatures;
				out.write(s+"\n");
			}
		}
		out.close();
		System.out.println("Random file created with "+d.randomQ.size()+" RANDOM queries");
	}
	
	public static void removeQueryFromCandidateSet(Query q) {
		// we need to remove the query with qID from the list of candidate queries
		// and then update the candidate set accordingly
		d.candidates.remove(q);
		/*int size = d.nCandidateQ;
		for(int i=0;i<size;i++)
		{
			if(d.listOfCandidateQueries[i].qID == q.qID)
			{
				for(int j=i; j<size-1;j++)
				{
					d.listOfCandidateQueries[j]=d.listOfCandidateQueries[j+1];
				}
				break;
			}
		}*/
		System.out.println("Query with qID = "+q.qID+" removed from candidate set");
		d.nCandidateQ--;
	}

	public static void prepareTrainFiles(ArrayList<Query> subset, int i) throws IOException {
		System.out.println("Size of subset= "+subset.size());
		FileWriter fstream = new FileWriter("src/data/LETOR/train"+i+".txt");
		BufferedWriter out = new BufferedWriter(fstream);
		Iterator<Query> itr = subset.iterator();
		while(itr.hasNext())
		{
			Query q = itr.next();
			int nD = q.nD;
			Iterator<Document> itr2 = q.listOfDocuments.iterator();
			while(itr2.hasNext())
			{
				Document d = itr2.next();
				String s = d.docFeatures;
				out.write(s+"\n");
			}
		}
		out.close();
		System.out.println("Trainging fle created");
	}
	
	public static void prepareCandidateFile(ArrayList<Query> subset) throws IOException {
		System.out.println("Size of candidate collection= "+subset.size());
		FileWriter fstream = new FileWriter("src/data/LETOR/candidate.txt");
		BufferedWriter out = new BufferedWriter(fstream);
		Iterator<Query> itr = subset.iterator();
		while(itr.hasNext())
		{
			Query q = itr.next();
			int nD = q.nD;
			Iterator<Document> itr2 = q.listOfDocuments.iterator();
			while(itr2.hasNext())
			{
				Document d = itr2.next();
				String s = d.docFeatures;
				out.write(s+"\n");
			}
		}
		out.close();
		System.out.println("Candidate file created");
	}

	public static <T> List<T> randomSample(List<T> items, int m){
		Random rnd = new Random();
	    ArrayList<T> res = new ArrayList<T>(m);
	    int visited = 0;
	    Iterator<T> it = items.iterator();
	    while (m > 0){
	        T item = it.next();
	        if (rnd.nextDouble() < ((double)m)/(items.size() - visited)){
	            res.add(item);
	            m--;
	        }
	        visited++;
	    }
	    System.out.println("Subset created with size-- "+res.size());
	    return res;
	}
	
	public static void deleteBinFiles() {
		/*File file = new File("src/data/LETOR/train1.bin");
		if(file.delete()){System.out.println(file.getName() + " is deleted!");}else{System.out.println("Delete operation is failed.");}
		
		file = new File("src/data/LETOR/train2.bin");
		if(file.delete()){System.out.println(file.getName() + " is deleted!");}else{System.out.println("Delete operation is failed.");}
		
		file = new File("src/data/LETOR/candidate.bin");
		if(file.delete()){System.out.println(file.getName() + " is deleted!");}else{System.out.println("Delete operation is failed.");}
		
		file = new File("src/data/LETOR/forEval/forEval_train.bin");
		if(file.delete()){System.out.println(file.getName() + " is deleted!");}else{System.out.println("Delete operation is failed.");}
		
		file = new File("src/data/LETOR/forEval/randomQ/forEval_random_train.bin");
		if(file.delete()){System.out.println(file.getName() + " is deleted!");}else{System.out.println("Delete operation is failed.");}
		
		file = new File("src/data/LETOR/forEval/randomQ/jforests-feature-stats.txt");
		if(file.delete()){System.out.println(file.getName() + " is deleted!");}else{System.out.println("Delete operation is failed.");}
		
		file = new File("src/data/LETOR/forEval/randomQ/jforests-feature-stats.txt");
		if(file.delete()){System.out.println(file.getName() + " is deleted!");}else{System.out.println("Delete operation is failed.");}
		*/
		File dir = new File("src/data/LETOR/forEval/randomQ/");
		//for(File files: dir.listFiles()) {if(files.getName().equalsIgnoreCase("forEval_random_train.txt")); else files.delete();}
		for(File files: dir.listFiles()) files.delete();
		
		dir = new File("src/data/LETOR/forEval/");
		for(File files: dir.listFiles()) {if(files.getName().equalsIgnoreCase("test.txt") || files.getName().equalsIgnoreCase("valid.txt") || files.getName().equalsIgnoreCase("resultsAT10.txt") || files.getName().equalsIgnoreCase("randomQ") || files.getName().equalsIgnoreCase("results")); else files.delete();}
		
		dir = new File("src/data/LETOR/");
		for(File files: dir.listFiles()) {if(files.getName().equalsIgnoreCase("train.txt") || files.getName().equalsIgnoreCase("valid.txt") || files.getName().equalsIgnoreCase("forEval")); else files.delete();}
	}
}
