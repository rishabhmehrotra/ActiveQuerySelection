import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import ciir.umass.edu.features.FeatureManager;
import ciir.umass.edu.features.Normalizer;
import ciir.umass.edu.features.SumNormalizor;
import ciir.umass.edu.features.ZScoreNormalizor;
import ciir.umass.edu.learning.*;
import ciir.umass.edu.learning.boosting.*;
import ciir.umass.edu.learning.neuralnet.*;
import ciir.umass.edu.learning.tree.LambdaMART;
import ciir.umass.edu.learning.tree.RFRanker;
import ciir.umass.edu.learning.Evaluator;
import ciir.umass.edu.learning.RANKER_TYPE;
import ciir.umass.edu.learning.RankList;
import ciir.umass.edu.learning.Ranker;
import ciir.umass.edu.metric.ERRScorer;
import ciir.umass.edu.metric.METRIC;
import ciir.umass.edu.metric.MetricScorer;
import ciir.umass.edu.metric.MetricScorerFactory;
import ciir.umass.edu.utilities.FileUtils;
import ciir.umass.edu.utilities.LinearComputer;
import ciir.umass.edu.utilities.MergeSorter;
import ciir.umass.edu.utilities.MyThreadPool;
import ciir.umass.edu.utilities.SimpleMath;
import ciir.umass.edu.utilities.Sorter;


public class RunLearningAlgorithmRankLib {
	
	ArrayList<Query> subset;
	String[] args;
	
	public RunLearningAlgorithmRankLib(ArrayList<Query> subset, int i)
	{
		this.subset = subset;
		//String[] args1 = {"--cmd=generate-bin", "--ranking", "--folder", "src/data/LETOR/", "--file", "train"+i+".txt", "--file", "valid.txt", "--file", "candidate.txt"};
		String args1[] = {"-silent","-train", "src/data/LETOR/train"+i+".txt", "-test", "src/data/LETOR/candidate.txt", "-validate", "src/data/LETOR/valid.txt", "-ranker", "3", "-metric2t", "MAP"/*"NDCG@10"*/, "-metric2T", "NDCG@10", "-save", "src/data/LETOR/mymodel"+i+".txt"};
		runAlgo(args1);
		String args2[] = {"-silent","-load", "src/data/LETOR/mymodel"+i+".txt", "-rank", "src/data/LETOR/candidate.txt", "-score", "src/data/LETOR/predictions"+i+".txt"};
		runAlgo(args2);
		deleteBinFiles();
	}
	
	public static void deleteBinFiles() {
		File dir = new File("src/data/LETOR/forEval/");
		for(File files: dir.listFiles()) {if((files.getName().contains(".bin") || files.getName().contains("jforest")) && (!files.getName().equalsIgnoreCase("test.bin"))) files.delete();}
		
		dir = new File("src/data/LETOR/");
		for(File files: dir.listFiles()) {if(files.getName().contains(".bin") || files.getName().contains("jforest")) files.delete();}
		
		dir = new File("src/data/LETOR/forEval/randomQ/");
		for(File files: dir.listFiles()) {if(files.getName().contains(".bin") || files.getName().contains("jforest")) files.delete();}
	}
	
public static void runAlgo(String[] args) {
		
		String[] rType = new String[]{"MART", "RankNet", "RankBoost", "AdaRank", "Coordinate Ascent", "LambdaRank", "LambdaMART", "ListNet", "Random Forests"};
		RANKER_TYPE[] rType2 = new RANKER_TYPE[]{RANKER_TYPE.MART, RANKER_TYPE.RANKNET, RANKER_TYPE.RANKBOOST, RANKER_TYPE.ADARANK, RANKER_TYPE.COOR_ASCENT, RANKER_TYPE.LAMBDARANK, RANKER_TYPE.LAMBDAMART, RANKER_TYPE.LISTNET, RANKER_TYPE.RANDOM_FOREST};
		
		String trainFile = "";
		String featureDescriptionFile = "";
		double ttSplit = 0.0;//train-test split
		double tvSplit = 0.0;//train-validation split
		int foldCV = -1;
		String validationFile = "";
		String testFile = "";
		int rankerType = 4;
		String trainMetric = "ERR@10";
		String testMetric = "";
		Evaluator.normalize = false;
		String savedModelFile = "";
		String rankFile = "";
		boolean printIndividual = false;
		
		//for my personal use
		String indriRankingFile = "";
		String scoreFile = "";
		
		if(args.length < 2)
		{
			System.out.println("Usage: java -jar RankLib.jar <Params>");
			System.out.println("Params:");
			System.out.println("  [+] Training (+ tuning and evaluation)");
			System.out.println("\t-train <file>\t\tTraining data");
			System.out.println("\t-ranker <type>\t\tSpecify which ranking algorithm to use");
			System.out.println("\t\t\t\t0: MART (gradient boosted regression tree)");
			System.out.println("\t\t\t\t1: RankNet");
			System.out.println("\t\t\t\t2: RankBoost");
			System.out.println("\t\t\t\t3: AdaRank");
			System.out.println("\t\t\t\t4: Coordinate Ascent");
			System.out.println("\t\t\t\t6: LambdaMART");
			System.out.println("\t\t\t\t7: ListNet");
			System.out.println("\t\t\t\t8: Random Forests");
			System.out.println("\t[ -feature <file> ]\tFeature description file: list features to be considered by the learner, each on a separate line");
			System.out.println("\t\t\t\tIf not specified, all features will be used.");
			//System.out.println("\t[ -metric2t <metric> ]\tMetric to optimize on the training data. Supported: MAP, NDCG@k, DCG@k, P@k, RR@k, BEST@k, ERR@k (default=" + trainMetric + ")");
			System.out.println("\t[ -metric2t <metric> ]\tMetric to optimize on the training data. Supported: MAP, NDCG@k, DCG@k, P@k, RR@k, ERR@k (default=" + trainMetric + ")");
			System.out.println("\t[ -metric2T <metric> ]\tMetric to evaluate on the test data (default to the same as specified for -metric2t)");
			System.out.println("\t[ -gmax <label> ]\tHighest judged relevance label. It affects the calculation of ERR (default=" + (int)SimpleMath.logBase2(ERRScorer.MAX) + ", i.e. 5-point scale {0,1,2,3,4})");
			//System.out.println("\t[ -qrel <file> ]\tTREC-style relevance judgment file. It only affects MAP and NDCG (default=unspecified)");

			System.out.println("\t[ -test <file> ]\tSpecify if you want to evaluate the trained model on this data (default=unspecified)");
			System.out.println("\t[ -validate <file> ]\tSpecify if you want to tune your system on the validation data (default=unspecified)");
			System.out.println("\t\t\t\tIf specified, the final model will be the one that performs best on the validation data");
			System.out.println("\t[ -tvs <x \\in [0..1]> ]\tSet train-validation split to be (x)(1.0-x)");
			System.out.println("\t[ -tts <x \\in [0..1]> ]\tSet train-test split to be (x)(1.0-x). -tts will override -tvs");
			System.out.println("\t[ -kcv <k> ]\t\tSpecify if you want to perform k-fold cross validation using ONLY the specified training data (default=NoCV)");
			
			System.out.println("\t[ -norm <method>]\tNormalize feature vectors (default=no-normalization). Method can be:");
			System.out.println("\t\t\t\tsum: normalize each feature by the sum of all its values");
			System.out.println("\t\t\t\tzscore: normalize each feature by its mean/standard deviation");
			
			System.out.println("\t[ -save <model> ]\tSave the learned model to the specified file (default=not-save)");
			
			System.out.println("\t[ -silent ]\t\tDo not print progress messages (which are printed by default)");
			
			System.out.println("");
			System.out.println("    [-] RankNet-specific parameters");
			System.out.println("\t[ -epoch <T> ]\t\tThe number of epochs to train (default=" + RankNet.nIteration + ")");
			System.out.println("\t[ -layer <layer> ]\tThe number of hidden layers (default=" + RankNet.nHiddenLayer + ")");
			System.out.println("\t[ -node <node> ]\tThe number of hidden nodes per layer (default=" + RankNet.nHiddenNodePerLayer + ")");
			System.out.println("\t[ -lr <rate> ]\t\tLearning rate (default=" + (new DecimalFormat("###.########")).format(RankNet.learningRate) + ")");
			
			System.out.println("");
			System.out.println("    [-] RankBoost-specific parameters");
			System.out.println("\t[ -round <T> ]\t\tThe number of rounds to train (default=" + RankBoost.nIteration + ")");
			System.out.println("\t[ -tc <k> ]\t\tNumber of threshold candidates to search. -1 to use all feature values (default=" + RankBoost.nThreshold + ")");
			
			System.out.println("");
			System.out.println("    [-] AdaRank-specific parameters");
			System.out.println("\t[ -round <T> ]\t\tThe number of rounds to train (default=" + AdaRank.nIteration + ")");
			System.out.println("\t[ -noeq ]\t\tTrain without enqueuing too-strong features (default=unspecified)");
			System.out.println("\t[ -tolerance <t> ]\tTolerance between two consecutive rounds of learning (default=" + AdaRank.tolerance + ")");
			System.out.println("\t[ -max <times> ]\tThe maximum number of times can a feature be consecutively selected without changing performance (default=" + AdaRank.maxSelCount + ")");

			System.out.println("");
			System.out.println("    [-] Coordinate Ascent-specific parameters");
			System.out.println("\t[ -r <k> ]\t\tThe number of random restarts (default=" + CoorAscent.nRestart + ")");
			System.out.println("\t[ -i <iteration> ]\tThe number of iterations to search in each dimension (default=" + CoorAscent.nMaxIteration + ")");
			System.out.println("\t[ -tolerance <t> ]\tPerformance tolerance between two solutions (default=" + CoorAscent.tolerance + ")");
			System.out.println("\t[ -reg <slack> ]\tRegularization parameter (default=no-regularization)");

			System.out.println("");
			System.out.println("    [-] {MART, LambdaMART}-specific parameters");
			System.out.println("\t[ -tree <t> ]\t\tNumber of trees (default=" + LambdaMART.nTrees + ")");
			System.out.println("\t[ -leaf <l> ]\t\tNumber of leaves for each tree (default=" + LambdaMART.nTreeLeaves + ")");
			System.out.println("\t[ -shrinkage <factor> ]\tShrinkage, or learning rate (default=" + LambdaMART.learningRate + ")");
			System.out.println("\t[ -tc <k> ]\t\tNumber of threshold candidates for tree spliting. -1 to use all feature values (default=" + LambdaMART.nThreshold + ")");
			System.out.println("\t[ -mls <n> ]\t\tMin leaf support -- minimum #samples each leaf has to contain (default=" + LambdaMART.minLeafSupport + ")");
			System.out.println("\t[ -estop <e> ]\t\tStop early when no improvement is observed on validaton data in e consecutive rounds (default=" + LambdaMART.nRoundToStopEarly + ")");

			System.out.println("");
			System.out.println("    [-] ListNet-specific parameters");
			System.out.println("\t[ -epoch <T> ]\t\tThe number of epochs to train (default=" + ListNet.nIteration + ")");
			System.out.println("\t[ -lr <rate> ]\t\tLearning rate (default=" + (new DecimalFormat("###.########")).format(ListNet.learningRate) + ")");

			System.out.println("");
			System.out.println("    [-] Random Forests-specific parameters");
			System.out.println("\t[ -bag <r> ]\t\tNumber of bags (default=" + RFRanker.nBag + ")");
			System.out.println("\t[ -srate <r> ]\t\tSub-sampling rate (default=" + RFRanker.subSamplingRate + ")");
			System.out.println("\t[ -frate <r> ]\t\tFeature sampling rate (default=" + RFRanker.featureSamplingRate + ")");
			int type = (RFRanker.rType.ordinal()-RANKER_TYPE.MART.ordinal());
			System.out.println("\t[ -rtype <type> ]\tRanker to bag (default=" + type + ", i.e. " + rType[type] + ")");
			System.out.println("\t[ -tree <t> ]\t\tNumber of trees in each bag (default=" + RFRanker.nTrees + ")");
			System.out.println("\t[ -leaf <l> ]\t\tNumber of leaves for each tree (default=" + RFRanker.nTreeLeaves + ")");
			System.out.println("\t[ -shrinkage <factor> ]\tShrinkage, or learning rate (default=" + RFRanker.learningRate + ")");
			System.out.println("\t[ -tc <k> ]\t\tNumber of threshold candidates for tree spliting. -1 to use all feature values (default=" + RFRanker.nThreshold + ")");
			System.out.println("\t[ -mls <n> ]\t\tMin leaf support -- minimum #samples each leaf has to contain (default=" + RFRanker.minLeafSupport + ")");

			System.out.println("");
			System.out.println("  [+] Testing previously saved models");
			System.out.println("\t-load <model>\t\tThe model to load");
			System.out.println("\t-test <file>\t\tTest data to evaluate the model (specify either this or -rank but not both)");
			System.out.println("\t-rank <file>\t\tRank the samples in the specified file (specify either this or -test but not both)");
			System.out.println("\t[ -metric2T <metric> ]\tMetric to evaluate on the test data (default=" + trainMetric + ")");
			System.out.println("\t[ -gmax <label> ]\tHighest judged relevance label. It affects the calculation of ERR (default=" + (int)SimpleMath.logBase2(ERRScorer.MAX) + ", i.e. 5-point scale {0,1,2,3,4})");
			System.out.println("\t[ -score <file>]\tStore ranker's score for each object being ranked (has to be used with -rank)");
			//System.out.println("\t[ -qrel <file> ]\tTREC-style relevance judgment file. It only affects MAP and NDCG (default=unspecified)");
			System.out.println("\t[ -idv ]\t\tPrint model performance (in test metric) on individual ranked lists (has to be used with -test)");
			System.out.println("\t[ -norm ]\t\tNormalize feature vectors (similar to -norm for training/tuning)");

/*			System.out.println("");
			System.out.println("  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			System.out.println("  + NOTE: ALWAYS include -letor if you're doing experiments on LETOR 4.0 dataset.       +");
			System.out.println("  +       The reason is a relevance degree of 2 in the dataset is actually counted as 3 +");
			System.out.println("  +       (this is based on the evaluation script they provided). To be consistent      +");
			System.out.println("  +       with their numbers, this program will change 2 to 3 when it loads the data    +");
			System.out.println("  +       into memory if the -letor flag is specified.                                  +");
			System.out.println("  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
*/
			System.out.println("");
			return;
		}
		
		MyThreadPool.init(Runtime.getRuntime().availableProcessors());
		//MyThreadPool.init(2);
		
		for(int i=0;i<args.length;i++)
		{
			if(args[i].compareTo("-train")==0)
				trainFile = args[++i];
			else if(args[i].compareTo("-ranker")==0)
				rankerType = Integer.parseInt(args[++i]);
			else if(args[i].compareTo("-feature")==0)
				featureDescriptionFile = args[++i];
			else if(args[i].compareTo("-metric2t")==0)
				trainMetric = args[++i];
			else if(args[i].compareTo("-metric2T")==0)
				testMetric = args[++i];
			else if(args[i].compareTo("-gmax")==0)
				ERRScorer.MAX = Math.pow(2, Double.parseDouble(args[++i]));			
			else if(args[i].compareTo("-qrel")==0)
				qrelFile = args[++i];			
			else if(args[i].compareTo("-tts")==0)
				ttSplit = Double.parseDouble(args[++i]);
			else if(args[i].compareTo("-tvs")==0)
				tvSplit = Double.parseDouble(args[++i]);
			else if(args[i].compareTo("-kcv")==0)
				foldCV = Integer.parseInt(args[++i]);
			else if(args[i].compareTo("-validate")==0)
				validationFile = args[++i];
			else if(args[i].compareTo("-test")==0)
				testFile = args[++i];
			else if(args[i].compareTo("-norm")==0)
			{
				Evaluator.normalize = true;
				String n = args[++i];
				if(n.compareTo("sum") == 0)
					Evaluator.nml = new SumNormalizor();
				else if(n.compareTo("zscore") == 0)
					Evaluator.nml = new ZScoreNormalizor();
				else
				{
					System.out.println("Unknown normalizor: " + n);
					System.out.println("System will now exit.");
					System.exit(1);
				}
			}
			else if(args[i].compareTo("-save")==0)
				Evaluator.modelFile = args[++i];
			else if(args[i].compareTo("-silent")==0)
				Ranker.verbose = false;

			else if(args[i].compareTo("-load")==0)
			{
				savedModelFile = args[++i];
				modelToLoad = args[i];
			}
			else if(args[i].compareTo("-idv")==0)
				printIndividual = true;
			else if(args[i].compareTo("-rank")==0)
				rankFile = args[++i];
			else if(args[i].compareTo("-score")==0)
				scoreFile = args[++i];			

			//Ranker-specific parameters
			//RankNet
			else if(args[i].compareTo("-epoch")==0)
			{
				RankNet.nIteration = Integer.parseInt(args[++i]);
				ListNet.nIteration = Integer.parseInt(args[i]);
			}
			else if(args[i].compareTo("-layer")==0)
				RankNet.nHiddenLayer = Integer.parseInt(args[++i]);
			else if(args[i].compareTo("-node")==0)
				RankNet.nHiddenNodePerLayer = Integer.parseInt(args[++i]);
			else if(args[i].compareTo("-lr")==0)
			{
				RankNet.learningRate = Double.parseDouble(args[++i]);
				ListNet.learningRate = Neuron.learningRate; 
			}
			
			//RankBoost
			else if(args[i].compareTo("-tc")==0)
			{
				RankBoost.nThreshold = Integer.parseInt(args[++i]);
				LambdaMART.nThreshold = Integer.parseInt(args[i]);
			}
			
			//AdaRank
			else if(args[i].compareTo("-noeq")==0)
				AdaRank.trainWithEnqueue = false;
			else if(args[i].compareTo("-max")==0)
				AdaRank.maxSelCount = Integer.parseInt(args[++i]);
			
			//COORDINATE ASCENT
			else if(args[i].compareTo("-r")==0)
				CoorAscent.nRestart = Integer.parseInt(args[++i]);
			else if(args[i].compareTo("-i")==0)
				CoorAscent.nMaxIteration = Integer.parseInt(args[++i]);
			
			//ranker-shared parameters
			else if(args[i].compareTo("-round")==0)
			{
				RankBoost.nIteration = Integer.parseInt(args[++i]);
				AdaRank.nIteration = Integer.parseInt(args[i]);
			}
			else if(args[i].compareTo("-reg")==0)
			{
				CoorAscent.slack = Double.parseDouble(args[++i]);
				CoorAscent.regularized = true;
			}
			else if(args[i].compareTo("-tolerance")==0)
			{
				AdaRank.tolerance = Double.parseDouble(args[++i]);
				CoorAscent.tolerance = Double.parseDouble(args[i]);
			}
			
			//MART / LambdaMART / Random forest
			else if(args[i].compareTo("-tree")==0)
			{
				LambdaMART.nTrees = Integer.parseInt(args[++i]);
				RFRanker.nTrees = Integer.parseInt(args[i]);
			}
			else if(args[i].compareTo("-leaf")==0)
			{
				LambdaMART.nTreeLeaves = Integer.parseInt(args[++i]);
				RFRanker.nTreeLeaves = Integer.parseInt(args[i]);
			}
			else if(args[i].compareTo("-shrinkage")==0)
			{
				LambdaMART.learningRate = Float.parseFloat(args[++i]);
				RFRanker.learningRate = Float.parseFloat(args[i]);
			}
			else if(args[i].compareTo("-mls")==0)
			{
				LambdaMART.minLeafSupport = Integer.parseInt(args[++i]);
				RFRanker.minLeafSupport = Integer.parseInt(args[i]);
			}
			else if(args[i].compareTo("-estop")==0)
				LambdaMART.nRoundToStopEarly = Integer.parseInt(args[++i]);
			
			//Random forest
			else if(args[i].compareTo("-bag")==0)
				RFRanker.nBag = Integer.parseInt(args[++i]);
			else if(args[i].compareTo("-srate")==0)
				RFRanker.subSamplingRate = Float.parseFloat(args[++i]);
			else if(args[i].compareTo("-frate")==0)
				RFRanker.featureSamplingRate = Float.parseFloat(args[++i]);
			
			else if(args[i].compareTo("-letor")==0)
				letor = true;
			
			/////////////////////////////////////////////////////
			// These parameters are *ONLY* for my personal use
			/////////////////////////////////////////////////////
			else if(args[i].compareTo("-nf")==0)
				newFeatureFile = args[++i];
			else if(args[i].compareTo("-keep")==0)
				keepOrigFeatures = true;
			else if(args[i].compareTo("-t")==0)
				topNew = Integer.parseInt(args[++i]);
			else if(args[i].compareTo("-indri")==0)
				indriRankingFile = args[++i];
			else if(args[i].compareTo("-hr")==0)
				mustHaveRelDoc = true;
			else
			{
				System.out.println("Unknown command-line parameter: " + args[i]);
				System.out.println("System will now exit.");
				System.exit(1);
			}
		}
		
		if(testMetric.compareTo("")==0)
			testMetric = trainMetric;
		
		System.out.println("");
		//System.out.println((keepOrigFeatures)?"Keep orig. features":"Discard orig. features");
		System.out.println("[+] General Parameters:");
		System.out.println("LETOR 4.0 dataset: " + (letor?"Yes":"No"));
		Evaluator e = new Evaluator(rType2[rankerType], trainMetric, testMetric);
		if(trainFile.compareTo("")!=0)
		{
			System.out.println("Training data:\t" + trainFile);
			
			if(foldCV != -1)
			{
				System.out.println("Cross validation: " + foldCV + " folds.");
			}
			else
			{
				if(testFile.compareTo("")!=0)
					System.out.println("Test data:\t" + testFile);
				else if(ttSplit > 0.0)//choose to split training data into train and test
					System.out.println("Train-Test split: " + ttSplit);
				
				if(validationFile.compareTo("")!=0)//the user has specified the validation set 
					System.out.println("Validation data:\t" + validationFile);
				else if(ttSplit <= 0.0 && tvSplit > 0.0)
					System.out.println("Train-Validation split: " + tvSplit);
			}
			System.out.println("Ranking method:\t" + rType[rankerType]);
			if(featureDescriptionFile.compareTo("")!=0)
				System.out.println("Feature description file:\t" + featureDescriptionFile);
			else
				System.out.println("Feature description file:\tUnspecified. All features will be used.");
			System.out.println("Train metric:\t" + trainMetric);
			System.out.println("Test metric:\t" + testMetric);
			if(trainMetric.toUpperCase().startsWith("ERR") || testMetric.toUpperCase().startsWith("ERR"))
				System.out.println("Highest relevance label (to compute ERR): " + (int)SimpleMath.logBase2(ERRScorer.MAX));
			if(qrelFile.compareTo("") != 0)
				System.out.println("TREC-format relevance judgment (only affects MAP and NDCG scores): " + qrelFile);
			System.out.println("Feature normalization: " + ((Evaluator.normalize)?Evaluator.nml.name():"No"));
			if(modelFile.compareTo("")!=0)
				System.out.println("Model file: " + modelFile);
			
			System.out.println("");
			System.out.println("[+] " + rType[rankerType] + "'s Parameters:");
			RankerFactory rf = new RankerFactory();
			
			rf.createRanker(rType2[rankerType]).printParameters();
			System.out.println("");
			
			//starting to do some work
			if(foldCV != -1)
				e.evaluate(trainFile, featureDescriptionFile, foldCV);
			else
			{
				if(ttSplit > 0.0)//we should use a held-out portion of the training data for testing?
					e.evaluate(trainFile, validationFile, featureDescriptionFile, ttSplit);
				else if(tvSplit > 0.0)//should we use a portion of the training data for validation?
					e.evaluate(trainFile, tvSplit, testFile, featureDescriptionFile);
				else
					e.evaluate(trainFile, validationFile, testFile, featureDescriptionFile);
			}
		}
		else //scenario: test a saved model
		{
			System.out.println("Model file:\t" + savedModelFile);
			System.out.println("Feature normalization: " + ((Evaluator.normalize)?Evaluator.nml.name():"No"));
			if(rankFile.compareTo("")!=0)
			{
				if(scoreFile.compareTo("") != 0)
					e.score(savedModelFile, rankFile, scoreFile);
				else if(indriRankingFile.compareTo("") != 0)
					e.rank(savedModelFile, rankFile, indriRankingFile);
				else
					e.rank(savedModelFile, rankFile);
			}
			else
			{
				System.out.println("Test metric:\t" + testMetric);
				if(testMetric.startsWith("ERR"))
					System.out.println("Highest relevance label (to compute ERR): " + (int)SimpleMath.logBase2(ERRScorer.MAX));
				if(savedModelFile.compareTo("") != 0)
					e.test(savedModelFile, testFile, printIndividual);
				//This is *ONLY* for my personal use. It is *NOT* exposed via cmd-line
				//It will evaluate the input ranking (without being reranked by any model) using any measure specified via metric2T
				else
					e.test(testFile);
			}
		}
		MyThreadPool.getInstance().shutdown();
	}

//main settings
	public static boolean letor = false;
	public static boolean mustHaveRelDoc = false;
	public static boolean normalize = false;
	public static Normalizer nml = new SumNormalizor();
	public static String modelFile = "";
	public static String modelToLoad = "";
	
	public static String qrelFile = "";//measure such as NDCG and MAP requires "complete" judgment.
	//The relevance labels attached to our samples might be only a subset of it.
	//If we're working on datasets like Letor/Web10K or Yahoo! LTR, we can totally ignore this parameter.
	//However, if we sample top-K documents from baseline run (e.g. query-likelihood) to create training data for TREC collections,
	//there's a high chance some relevant document (the in qrel file TREC provides) does not appear in our top-K list -- thus the calculation of
	//MAP and NDCG is no longer precise.
	
	//tmp settings, for personal use
	public static String newFeatureFile = "";
	public static boolean keepOrigFeatures = false;
	public static int topNew = 2000;
}
