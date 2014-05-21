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

public class RunLearningAlgorithm {
	
	ArrayList<Query> subset;
	Runner r;
	String[] args;
	
	public RunLearningAlgorithm(ArrayList<Query> subset, int i)
	{
		this.subset = subset;
		deleteBinFiles();
		try {
			runAlgo(buildArgsForGenerateBinary(i));
			runAlgo(buildArgsForRanking(i));
			runAlgo(buildArgsForPredicting(i));
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public static void deleteBinFiles() {
		File dir = new File("src/data/LETOR/forEval/");
		for(File files: dir.listFiles()) {if((files.getName().contains(".bin") || files.getName().contains("jforest")) && (!files.getName().equalsIgnoreCase("test.bin"))) files.delete();}
		
		dir = new File("src/data/LETOR/");
		for(File files: dir.listFiles()) {if(files.getName().contains(".bin") || files.getName().contains("jforest")) files.delete();}
		
		dir = new File("src/data/LETOR/forEval/randomQ/");
		for(File files: dir.listFiles()) {if(files.getName().contains(".bin") || files.getName().contains("jforest")) files.delete();}
	}

	String[] buildArgsForGenerateBinary(int i)
	{
		String[] args1 = {"--cmd=generate-bin", "--ranking", "--folder", "src/data/LETOR/", "--file", "train"+i+".txt", "--file", "valid.txt", "--file", "candidate.txt"};
		return args1;
	}
	
	String[] buildArgsForRanking(int i)
	{
		String[] args1 = {"--cmd=train", "--ranking", "--config-file", "src/data/ranking.properties", "--train-file", "src/data/LETOR/train"+i+".bin", "--validation-file", "src/data/LETOR/valid.bin", "--output-model", "src/data/LETOR/ensemble"+i+".txt"};
		return args1;
	}
	
	String[] buildArgsForPredicting(int i)
	{
		String[] args1 = {"--cmd=predict", "--ranking", "--model-file", "src/data/LETOR/ensemble"+i+".txt", "--tree-type", "RegressionTree", "--test-file", "src/data/LETOR/candidate.bin", "--output-file", "src/data/LETOR/predictions"+i+".txt"};
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

}
