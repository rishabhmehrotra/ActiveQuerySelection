import java.io.*;
import java.util.*;

import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class Driver {
	
	public static QDataset d;
	
	public Driver() throws IOException
	//public static void main(String args[]) throws IOException
	{
		System.out.println("Hello! World.");
		
		// TODO: as a baseline, the strongest thing could be to just select each query one by one and train a ranker by adding each query
		// and now select the next query corresponding to the ranker that gives the best NDCG now...
		// this would be the best possible query selection we can potentially do, in HINDSIGHT...something Filip Radlinski suggested...
		
		
		
		//String filename = "src/data/set2.train.txt";
		String filename = "src/data/LETOR/train.txt";
		d = new QDataset(filename);
		System.out.println("Total No of queries= "+d.nQ);
		System.out.println("Total no of queries in Base Set= "+d.base.size());
		
		populateQueryTermsToLDAFile();
		d.ldaModel = new BuildLDAModel(d);
		
		InstanceList testing = new InstanceList(d.ldaModel.instances.getPipe());
		Iterator<Query> itt = d.candidates.iterator();
		while(itt.hasNext()){
			Query q = itt.next();
			testing.addThruPipe(new Instance(q.termString, null, "test instance", null));
		}
		
		
		//System.exit(0);

		/*TopicInferencer inferencer = d.ldaModel.model.getInferencer();
		for(int y = 0; y<200;y++)
		{
			System.out.println("For Query: ");
			double[] testProb = inferencer.getSampledDistribution(testing.get(y), d.numTopics, 1, 5);
			for(int j=0;j<d.numTopics;j++)
	    	{
				System.out.print(testProb[j]+" ");
	    	}
		}*/
		//System.exit(0);
		
		
		int N = 50;
		
		int i,j,k,l,m;
		int sizeDl = d.base.size();
		int sizeSubset = (int) (0.5*sizeDl);
		int temp =1;
		for(i=1;i<=N;i++)
		{
			//deleteBinFiles();
			//obtain the two sample subsets of size sizeSubset
			sizeSubset = (int) (d.sizeOfSubset*d.nBase);
			d.subset1 = (ArrayList<Query>) randomSample(d.base,sizeSubset);
			d.subset2 = (ArrayList<Query>) randomSample(d.base,sizeSubset);
			d.subset3 = (ArrayList<Query>) randomSample(d.base,sizeSubset);
			d.subset4 = (ArrayList<Query>) randomSample(d.base,sizeSubset);
			// make the training files for both the subsets
			
			prepareTrainFiles(d.subset1,1);
			prepareTrainFiles(d.subset2,2);
			prepareTrainFiles(d.subset3,3);
			prepareTrainFiles(d.subset4,4);
			prepareCandidateFile(d.candidates);
			
			// now that we have both the subsets, we have to train 2 models on these 2 subsets
			if(temp==1) new RunLearningAlgorithm(d.subset1,1);
			if(temp==1) new RunLearningAlgorithm(d.subset2,2);
			if(temp==1) new RunLearningAlgorithm(d.subset3,3);
			if(temp==1) new RunLearningAlgorithm(d.subset4,4);
			
			// now we have both the models in the ensemble1/2.txt files, we need to measure the disagreement for each query among these 2 models
			// the queries-doc pairs are present line by line in the test.txt file and corresponding lines in the predictions1/2.txt file contain the
			// scores assigned by the models to each of these query-doc pairs..
			// we now need to store these results in a data structure by reading line by line both these files
			if(temp==1) new WorkWithResults("src/data/LETOR/candidate.txt",1, d, "src/data/LETOR/predictions1.txt", "src/data/LETOR/predictions2.txt", "src/data/LETOR/predictions3.txt", "src/data/LETOR/predictions4.txt");
			// so now we have all the test queries populated with their respective scores from each of the models learnt
			// and we have populated the disagreement scores for all the queries...we now have to find query with maximum vote entropy/disagreement
			
			
			int batch = d.batchSize;
			if(temp==1) new QuerySimilarity(d);
			//if(temp==1) new ELO(d);
			
			LDATopicWiseQuerySampling topicwise = new LDATopicWiseQuerySampling(d);
			//ArrayList<Query> top10 = topicwise.computeTopicWiseSimilarity();
			ArrayList<Query> top10 = topicwise.getMinMaxPLQueryFromTopics();
			System.out.println("--------Obtained top10...");
			for(int ii=0;ii<top10.size();ii++)
			{
				Query q = top10.get(ii);
				System.out.println(d.base.size());
				d.base.add(q);
				System.out.println(d.base.size());
				d.nBase++;
				System.out.println(d.candidates.size());
				removeQueryFromCandidateSet(q);
				System.out.println(d.candidates.size());
			}
			batch = 0;//if running experiments with topic based sampling, we just use the top10 Query list returned by the function and add them directly to the base set 
			while(batch>0)
			{
				batch--;
				// we need to normalize the disagreement to make it a value between 0 & 1
				double maxDisagreement = 0, minDisagreement = 100000;
				float maxSim=0, minSim=1000, maxLDASim=0, minLDASim=1000;
				double max_avgPL = -1000, min_avgPL = 1000;
				Iterator<Query> it = d.candidates.iterator();
				while(it.hasNext())
				{
					Query q = it.next();
					if(q.qID == 0) continue;
					if(q.disagreement < minDisagreement) minDisagreement = q.disagreement;
					if(q.disagreement > maxDisagreement) maxDisagreement = q.disagreement;
					if(q.currentAvgSimilarity > maxSim) maxSim = q.currentAvgSimilarity;
					if(q.similarityLDA > maxLDASim) maxLDASim = q.similarityLDA;
					if(q.similarityLDA < minLDASim) minLDASim = q.similarityLDA;
					if(q.disagreement == 0) System.out.println("0 disagreement: "+q.qID);
					if(q.similarityLDA == 0) System.out.println("0 LDASim: "+q.qID);
					
					double avgPL = q.PL1+q.PL2+q.PL3+q.PL4;
					avgPL /= 4;
					double maxPL = getMax(q.PL1,q.PL2,q.PL3,q.PL4);
					avgPL = maxPL;
					
					if(avgPL < min_avgPL) min_avgPL = avgPL;
					if(avgPL > max_avgPL) max_avgPL = avgPL;
					
					
					q.avgPL = avgPL;
				}
				System.out.println("Max disagreement: "+maxDisagreement+" min disagreement: "+minDisagreement);
				System.out.println("Max LDASim: "+maxLDASim+" minLDASim: "+minLDASim);
				//System.exit(0);
				it = d.candidates.iterator();
				while(it.hasNext())
				{
					Query q = it.next();
					if(q.qID == 0) continue;
					Double normalizedDisagreement = (q.disagreement-minDisagreement)/(maxDisagreement-minDisagreement);
					q.setNormalizedDisagreement(normalizedDisagreement);
					float normalizedSim = q.currentAvgSimilarity/maxSim;
					q.setNormalizedSimilarity(normalizedSim);
					float normalizedLDASim = (q.similarityLDA-minLDASim)/(maxLDASim-minLDASim);
					q.setNormalizedLDASimilarity(normalizedLDASim);
					double normalizedAvgPL = (q.avgPL - min_avgPL)/(max_avgPL - min_avgPL);
					q.setNormalizedAvgPL(normalizedAvgPL);
					//System.out.println("------------------------------------------------------normalizedAvgPL: "+normalizedAvgPL);
				}
		
				
				Query next = null, nextBySim = null, nextByLDASim=null, nextByCombined = null, nextByCombined2 = null, nextByCombined3 = null, nextByPL = null, nextByELO = null;
				double max=0, maxSimilarity=0, maxLDASimilarity=0, maxCombined=0, maxCombined2=0, maxCombined3=0, minPL = 1000, maxEL = -1000;

				Iterator<Query> itr = d.candidates.iterator();
				//for(j=0;j<d.nCandidateQ;j++)
				while(itr.hasNext())
				{
					Query q = itr.next();
					if(q.qID == 0) continue;
					//Query q = d.listOfCandidateQueries[j];
					//if(q.currentAvgSimilarity >= maxSimilarity) {maxSimilarity = q.currentAvgSimilarity;nextBySim = q;}
					if(q.normalizedSimilarity >= maxSimilarity) {maxSimilarity = q.normalizedSimilarity;nextBySim = q;}
					if(q.normalizedLDASimilarity >= maxLDASimilarity) {maxLDASimilarity = q.normalizedLDASimilarity;nextByLDASim = q;}
					if(q.disagreement > max) {max = q.disagreement;next = q;}
					if(q.ELScore > maxEL) {maxEL = q.ELScore;nextByELO = q;}
					//System.out.println(q.PL1+" "+q.PL2+" "+q.PL3+" "+q.PL4);
					
					// the query which has the least maximum PL among the 4 rankers will be selected
					
					//System.out.println("avg PL:"+avgPL+" query:"+q.qID);
					if(q.normalizedAvgPL < minPL) {minPL = q.normalizedAvgPL; nextByPL = q;}
					double combine = q.normalizedDisagreement*q.currentAvgSimilarity;
					double combine2 = Math.sqrt(q.normalizedDisagreement)+q.currentAvgSimilarity;
					double combine3 = q.normalizedDisagreement+q.normalizedLDASimilarity;
					q.combine = combine;
					q.combine2 = combine2;
					q.combine3 = combine3;
					if(combine >= maxCombined) {maxCombined = combine;nextByCombined = q;}
					if(combine2 >= maxCombined2) {maxCombined2 = combine2;nextByCombined2 = q;}
					if(combine3 > maxCombined3) {maxCombined3 = combine3;nextByCombined3 = q;}
					//System.out.println("For query with qID= "+q.qID+" the disagreement is= "+q.disagreement);
				}
				// now we have the query which should be used next and we need to add it to the base list on which we should train henceforth
				if(next == null) System.out.println("/n/nThe NEXT query selected is NULL, something went wrong, have a look!/n/n");
				//d.base.add(next);
				if(nextByPL == null) {System.out.println("Skipping coz of null PL===\n\n\n\n===="); continue;}
				
				//if(nextByCombined2 == null) {removeQueryFromCandidateSet(nextByCombined2);batch++; continue;}
				
				
				System.out.println("------\nComparing Disagreement & Similarity:\nBy Disagreement "+next.disagreement+ "__"+next.normalizedDisagreement+ "__"+ next.currentAvgSimilarity+" "+next.combine+" "+next.combine2+" "+next.combine3+" NormalizedLDASim:"+next.normalizedLDASimilarity+" Min PL:"+next.avgPL+" NormalizedAvgPL: "+next.normalizedAvgPL+" MaxELO:"+next.ELScore);
				//System.out.println("By Similarity: "+nextBySim.disagreement+ "__"+nextBySim.normalizedDisagreement+"__"+nextBySim.currentAvgSimilarity+" "+nextBySim.combine +" "+nextBySim.combine2+" "+nextBySim.combine3+" NormalizedLDASim:"+nextBySim.normalizedLDASimilarity+" Min PL:"+nextBySim.avgPL);
				//System.out.println("By LDANSimilarity: "+nextByLDASim.disagreement+ "__"+nextByLDASim.normalizedDisagreement+"__"+nextByLDASim.currentAvgSimilarity+" "+nextByLDASim.combine +" "+nextByLDASim.combine2+" "+nextByLDASim.combine3+" NormalizedLDASim:"+nextByLDASim.normalizedLDASimilarity+" Min PL:"+nextByLDASim.avgPL+" MinNormalizedAvgPL: "+nextByPL.normalizedAvgPL+" MaxELO:"+nextByELO.ELScore);
				//System.out.println("By Combine"+nextByCombined.disagreement+ "__"+nextByCombined.normalizedDisagreement+"__"+nextByCombined.currentAvgSimilarity+" "+nextByCombined.combine+" "+nextByCombined.combine2+" "+nextByCombined.combine3+" NormalizedLDASim:"+nextByCombined.normalizedLDASimilarity+" Min PL:"+nextByCombined.avgPL+" MinNormalizedAvgPL: "+nextByPL.normalizedAvgPL+" MaxELO:"+nextByELO.ELScore);
				//System.out.println("By Combine2"+nextByCombined2.disagreement+ "__"+nextByCombined2.normalizedDisagreement+"__"+nextByCombined2.currentAvgSimilarity+" "+nextByCombined2.combine+" "+nextByCombined2.combine2+" "+nextByCombined2.combine3+" NormalizedLDASim:"+nextByCombined2.normalizedLDASimilarity+" Min PL:"+nextByCombined2.avgPL+" MinNormalizedAvgPL: "+nextByPL.normalizedAvgPL+" MaxELO:"+nextByELO.ELScore);
				//System.out.println("By Combine3"+nextByCombined3.disagreement+ "__"+nextByCombined3.normalizedDisagreement+"__"+nextByCombined3.currentAvgSimilarity+" "+nextByCombined3.combine+" "+nextByCombined3.combine2+" "+nextByCombined3.combine3+" NormalizedLDASim:"+nextByCombined3.normalizedLDASimilarity+" Min PL:"+nextByCombined3.avgPL+" MinNormalizedAvgPL: "+nextByPL.normalizedAvgPL+" MaxELO:"+nextByELO.ELScore);
				System.out.println("By minAvgPL"+nextByPL.disagreement+ "__"+nextByPL.normalizedDisagreement+"__"+nextByPL.currentAvgSimilarity+" "+nextByPL.combine+" "+nextByPL.combine2+" "+nextByPL.combine3+" NormalizedLDASim:"+nextByPL.normalizedLDASimilarity+" Min PL:"+nextByPL.avgPL+" NormalizedAvgPL: "+nextByPL.normalizedAvgPL+" MaxELO:"+nextByPL.ELScore);
				System.out.println("By maxELO"+nextByELO.disagreement+ "__"+nextByELO.normalizedDisagreement+"__"+nextByELO.currentAvgSimilarity+" "+nextByELO.combine+" "+nextByELO.combine2+" "+nextByELO.combine3+" NormalizedLDASim:"+nextByELO.normalizedLDASimilarity+" Min PL:"+nextByELO.avgPL+" NormalizedAvgPL: "+nextByELO.normalizedAvgPL+" MaxELO:"+nextByELO.ELScore);
				
				
				//System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^Max similarity query being removed which has minAvgPL= "+nextBySim.avgPL);
				//System.out.println("---------- Query being removed now: "+next.qID+"_"+next.nD+"_"+next.disagreement+"_"+next.listOfDocuments.get(0));
				// now we need to remove this particular selected NEXT query from the list of candidates
				//System.out.println("Size before query removal from candidate set: "+d.nCandidateQ);
				//removeQueryFromCandidateSet(next);
				/*if(batch%3==1)
				{
					d.base.add(next);
					d.nBase++;
					removeQueryFromCandidateSet(next);
				}
				else if(batch%3==0)
				{
					d.base.add(nextByLDASim);
					d.nBase++;
					removeQueryFromCandidateSet(nextByLDASim);
				}
				else if(batch%3==2)
				{
					d.base.add(next);
					d.nBase++;
					removeQueryFromCandidateSet(next);
				}*/
				//d.base.add(nextByELO);
				d.base.add(nextByPL);
				d.nBase++;
				//removeQueryFromCandidateSet(nextByELO);
				removeQueryFromCandidateSet(nextByPL);
				
				
				//System.out.println("Size after query removal from candidate set: "+d.nCandidateQ);
				//System.out.println("Size of the new base set: "+d.base.size());
			} // end of while(batch>0)
			// now we have the new base set ready, we should extract the subset from it now and see how it performs
			// also, we need code for converting scores to NDCG measure now...
			new RunTestingAlgorithm(d.base, d);
			// uncomment this part if yoi have to run the random part as well
			/*double avg = 0, avgAP=0;
			for(int ii=0;ii<1;ii++)
			{
				File dir = new File("src/data/LETOR/forEval/randomQ/");
				for(File files: dir.listFiles()) files.delete();
				populateRandomQueries();
				new RunRandomTestingAlgorithm(d.randomQ, d);
				avg += d.resultRandom;
				avgAP += d.resultRandomAP;
			}
			FileWriter fstream = new FileWriter("src/data/LETOR/forEval/resultsAT10.txt", true);
			BufferedWriter out = new BufferedWriter(fstream);
			avg/= 10;
			avgAP/= 10;
			out.write("Average NDCG for this run: "+avg+"\n");
			out.write("Average AP for this run: "+avgAP+"\n");
			out.close();
			*/
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
	
	public double getMax(double p1, double p2, double p3, double p4) {
		// TODO Auto-generated method stub
		if(p1 > p2 && p1 > p3 && p1 > p4) return p1;
		if(p2 > p1 && p2 > p3 && p2 > p4) return p2;
		if(p3 > p1 && p3 > p2 && p3 > p4) return p3;
		if(p4 > p1 && p4 > p2 && p4 > p3) return p4;
		return 0;
	}

	public static void populateQueryTermsToLDAFile() throws IOException
	{
		int c =0;
		FileWriter fstream = new FileWriter("src/data/queriesForLDA");
		BufferedWriter out = new BufferedWriter(fstream);
		for (String key : d.queryTerms.keySet()) {
			HashMap<String, Float> map = d.queryTerms.get(key);
			for(String words: map.keySet())
			{
				Float size = map.get(words);
				for(int i=1;i<=size;i++)
				{
					c++;out.write(words+" ");
				}
			}
			out.write("\n");
		}
		System.out.println(c);
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
