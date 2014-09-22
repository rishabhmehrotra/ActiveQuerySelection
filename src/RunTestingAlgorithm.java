import java.util.*;
import java.io.*;

import javax.swing.plaf.synth.SynthOptionPaneUI;

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
public class RunTestingAlgorithm {
	
	ArrayList<Query> base;
	ArrayList<Query> listTestQueries;
	Runner r;
	String[] args;
	public String testFile = "src/data/LETOR/forEval/test.txt";
	public String predictionFile = "src/data/LETOR/forEval/forEval_predictions.txt";
	public QDataset d;
	
	public RunTestingAlgorithm(ArrayList<Query> base, QDataset d) throws IOException
	{
		this.base = base;
		this.listTestQueries = new ArrayList<Query>();
		this.d = d;
		System.out.println("\n\nThe system is now going to perform evaluation for "+base.size()+" number of queries");
		populateTrainFiles();
		
		deleteBinFiles();
		
		try {
			runAlgo(buildArgsForGenerateBinary());
			runAlgo(buildArgsForRanking());
			runAlgo(buildArgsForPredicting());
		} catch (Exception e) {e.printStackTrace();}
		System.out.println("Running of the testing algorithm complete now...Computing NDGC now...");
		// now we have the cores for all the documents for each query, we now need to calculate NDGC scores
		populateTestScoresFromFile();
		computeNDCG();
	}
	
	public static void deleteBinFiles() {
		File dir = new File("src/data/LETOR/forEval/");
		for(File files: dir.listFiles()) {if((files.getName().contains(".bin") || files.getName().contains("jforest")) && (!files.getName().equalsIgnoreCase("test.bin")) && (!files.getName().equalsIgnoreCase("valid.bin"))) files.delete();}
		
		dir = new File("src/data/LETOR/");
		for(File files: dir.listFiles()) {if(files.getName().contains(".bin") || files.getName().contains("jforest")) files.delete();}
		
		dir = new File("src/data/LETOR/forEval/randomQ/");
		for(File files: dir.listFiles()) {if(files.getName().contains(".bin") || files.getName().contains("jforest")) files.delete();}
	}
	
	
	
	public void populateTestScoresFromFile()
	{
		BufferedReader br, br1;
		try {
			br = new BufferedReader(new FileReader(this.testFile));
			br1 = new BufferedReader(new FileReader(this.predictionFile));
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
					if(q.qID!=0) this.listTestQueries.add(q);
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
			this.listTestQueries.add(q);
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
	
	public void computeNDCG() throws IOException
	{
		int nonrelevant = 0, total=0;
		int thresNDGC = 10;// remember to change it in RunRandomTest class as well
		// sort the documents for each query in base
		double avgNDCG=0.0;
		double avgAP=0.0;
		int totalCount=0, count4AP=0;
		Iterator<Query> itr = this.listTestQueries.iterator();
		FileWriter fstream1 = new FileWriter("src/data/LETOR/NDCG_errorBars.txt", true);
		BufferedWriter out1 = new BufferedWriter(fstream1);
		out1.write(d.base.size()+"\t");
		FileWriter fstream2 = new FileWriter("src/data/LETOR/AP_errorBars.txt", true);
		BufferedWriter out2 = new BufferedWriter(fstream2);
		out2.write(d.base.size()+"\t");
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
			
			if(NDCG>=0 && NDCG<=1)
			{
				avgNDCG += NDCG;totalCount++;
				out1.write(NDCG+"\t");
			}
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
		d.resultCandidates = avgNDCG;
		d.resultCandidatesAP = avgAP;
		System.out.println("No of queries with 0 1s and 0 2s: "+nonrelevant+ " & total no of queries: "+total);
		
		FileWriter fstream = new FileWriter("src/data/LETOR/forEval/resultsAT10.txt", true);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write("\n\n"+"avgNDCG for Candidates training size of "+d.base.size()+" queries= "+avgNDCG+"\n");
		out.write("avgAP for Candidates training size of "+d.base.size()+" queries= "+avgAP+"\n");
		out.close();
		System.out.println("================= Average NDGC score for a total of "+this.listTestQueries.size()+" = "+totalCount+" queries: "+avgNDCG);
	}
	
	public void populateTrainFiles() throws IOException {
		System.out.println("Size of test collection= "+base.size());
		FileWriter fstream = new FileWriter("src/data/LETOR/forEval/forEval_train.txt");
		BufferedWriter out = new BufferedWriter(fstream);
		Iterator<Query> itr = base.iterator();
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
		System.out.println("forEval_Train file created");
	}

	String[] buildArgsForGenerateBinary()
	{
		String[] args1 = {"--cmd=generate-bin", "--ranking", "--folder", "src/data/LETOR/forEval/", "--file", "forEval_train.txt", "--file", "valid.txt", "--file", "test.txt"};
		return args1;
	}
	
	String[] buildArgsForRanking()
	{
		String[] args1 = {"--cmd=train", "--ranking", "--config-file", "src/data/ranking.properties", "--train-file", "src/data/LETOR/forEval/forEval_train.bin", "--validation-file", "src/data/LETOR/forEval/valid.bin", "--output-model", "src/data/LETOR/forEval/forEval_ensemble.txt"};
		return args1;
	}
	
	String[] buildArgsForPredicting()
	{
		String[] args1 = {"--cmd=predict", "--ranking", "--model-file", "src/data/LETOR/forEval/forEval_ensemble.txt", "--tree-type", "RegressionTree", "--test-file", "src/data/LETOR/forEval/test.bin", "--output-file", "src/data/LETOR/forEval/forEval_predictions.txt"};
		return args1;
	}
	
	void runAlgo(String[] args) throws Exception
	{
		OptionParser parser = new OptionParser();

        parser.accepts("cmd").withRequiredArg();
        parser.accepts("ranking");

        /*
         * Bin generation arguments
         */
        parser.accepts("folder").withRequiredArg();
        parser.accepts("file").withRequiredArg();

        /*
         * Training arguments
         */
        parser.accepts("config-file").withRequiredArg();
        parser.accepts("train-file").withRequiredArg();
        parser.accepts("validation-file").withRequiredArg();
        parser.accepts("output-model").withRequiredArg();

        /*
         * Prediction arguments
         */
        parser.accepts("model-file").withRequiredArg();
        parser.accepts("tree-type").withRequiredArg();
        parser.accepts("test-file").withRequiredArg();
        parser.accepts("output-file").withRequiredArg();

        OptionSet options = parser.parse(args);

        if (!options.has("cmd")) {
                System.err.println("You must specify the command through 'cmd' parameter.");
                return;
        }

        if (options.valueOf("cmd").equals("generate-bin")) {
                generateBin(options);
        } else if (options.valueOf("cmd").equals("train")) {
                train(options);
        } else if (options.valueOf("cmd").equals("predict")) {
                predict(options);
        } else {
                System.err.println("Unknown command: " + options.valueOf("cmd"));
        }
	}
	
	
	@SuppressWarnings("unchecked")
    public static void generateBin(OptionSet options) throws Exception {
            if (!options.has("folder")) {
                    System.err.println("The input folder is not specified.");
                    return;
            }

            if (!options.has("file")) {
                    System.err.println("Input files are not specified.");
                    return;
            }

            String folder = (String) options.valueOf("folder");
            List<String> filesList = (List<String>) options.valuesOf("file");
            String[] files = new String[filesList.size()];
            for (int i = 0; i < files.length; i++) {
                    files[i] = filesList.get(i);
            }

            if (options.has("ranking")) {
                    System.out.println("Generating binary files for ranking data sets...");
                    new RankingRaw2BinConvertor().convert(folder, files);
            } else {
                    System.out.println("Generating binary files...");
                    new Raw2BinConvertor().convert(folder, files);
            }
    }

    public static void train(OptionSet options) throws Exception {
            if (!options.has("config-file")) {
                    System.err.println("The configurations file is not specified.");
                    return;
            }

            InputStream configInputStream = new FileInputStream((String) options.valueOf("config-file"));
            Properties configProperties = new Properties();
            configProperties.load(configInputStream);

            if (options.has("train-file")) {
                    configProperties.put(TrainingConfig.TRAIN_FILENAME, options.valueOf("train-file"));
            }

            if (options.has("validation-file")) {
                    configProperties.put(TrainingConfig.VALID_FILENAME, options.valueOf("validation-file"));
            }

            Ensemble ensemble;

            if (options.has("ranking")) {
                    RankingApp app = new RankingApp();
                    ensemble = app.run(configProperties);
            } else {
                    ClassificationApp app = new ClassificationApp();
                    ensemble = app.run(configProperties);
            }

            /*
             * Dump the output model if requested.
             */
            if (options.has("output-model")) {
                    String outputModelFile = (String) options.valueOf("output-model");
                    File file = new File(outputModelFile);
                    PrintStream ensembleOutput = new PrintStream(file);
                    ensembleOutput.println(ensemble);
                    ensembleOutput.close();
            }

    }

    public static void predict(OptionSet options) throws Exception {

            if (!options.has("model-file")) {
                    System.err.println("Model file is not specified.");
                    return;
            }

            if (!options.has("tree-type")) {
                    System.err.println("Types of trees in the ensemble is not specified.");
                    return;
            }

            if (!options.has("test-file")) {
                    System.err.println("Test file is not specified.");
                    return;
            }

            /*
             * Load the ensemble
             */
            File modelFile = new File((String) options.valueOf("model-file"));
            Ensemble ensemble = new Ensemble();
            if (options.valueOf("tree-type").equals("RegressionTree")) {
                    ensemble.loadFromFile(RegressionTree.class, modelFile);
            } else if (options.valueOf("tree-type").equals("DecisionTree")) {
                    ensemble.loadFromFile(DecisionTree.class, modelFile);
            } else {
                    System.err.println("Unknown tree type: " + options.valueOf("tree-type"));
            }

            /*
             * Load the data set
             */
            InputStream in = new IOUtils().getInputStream((String) options.valueOf("test-file"));
            Sample sample;
            if (options.has("ranking")) {
                    RankingDataset dataset = new RankingDataset();
                    RankingDatasetLoader.load(in, dataset);
                    sample = new RankingSample(dataset);
            } else {
                    Dataset dataset = new Dataset();
                    DatasetLoader.load(in, dataset);
                    sample = new Sample(dataset);
            }
            in.close();
            final long startms = System.currentTimeMillis();
            double[] predictions = new double[sample.size];
            LearningUtils.updateScores(sample, predictions, ensemble);
            final long stopms = System.currentTimeMillis();
            System.err.println(sample.size + " predictions in "+ (stopms - startms) + " ms");

            PrintStream output;
            if (options.has("output-file")) {
                    output = new PrintStream(new File((String) options.valueOf("output-file")));
            } else {
                    output = System.out;
            }
            
            for (int i = 0; i < sample.size; i++) {
                    output.println(predictions[i]);
            }

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

}
