

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.MarginalProbEstimator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class BuildLDAModel {
	private static final String[] INPUT_FILES = {
		    	"src/data/queriesForLDA"
	};

	private static final String STOP_WORDS_FILE = "stoplists/en.txt";
	private static int NUM_TOPICS;
	private static final int NUM_ITERATIONS = 1000;
	private static final int NUM_THREADS = 1;

	private static final int OPTIMIZE_INTERVAL = 0;
	private static final int BURN_IN_PERIOD = 0;

	private static final double ALPHA = 50.0;
	private static final double BETA = 0.01;
	
	public List<Pipe> pipeList;
	public InstanceList instances;
	public ParallelTopicModel model;

	public BuildLDAModel(QDataset d) throws IOException
	{
		
		this.NUM_TOPICS = d.numTopics;
		
		System.out.println(NUM_ITERATIONS + " Iterations; " 
		+ NUM_TOPICS + " topics; optimize interval = " + OPTIMIZE_INTERVAL 
		+ "; burn in period = " + BURN_IN_PERIOD
		+ "; initial alpha = " + (new DecimalFormat("##.#####")).format(ALPHA/NUM_TOPICS) + "; "
		+ "initial beta = " + BETA);
		System.out.println("starting...");

		for(int inputIdx = 0; inputIdx < INPUT_FILES.length; inputIdx++) {
			System.out.println("Processing file " + INPUT_FILES[inputIdx]);
			System.out.println("[Topic No.]  [Topic Proportion]  [Alpha]  [Topic Words]");
			this.pipeList = new ArrayList<Pipe>();
			this.pipeList.add(new CharSequenceLowercase());
			this.pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
			this.pipeList.add(new TokenSequenceRemoveStopwords(new File(STOP_WORDS_FILE), "UTF-8", false, false, false));
			this.pipeList.add(new TokenSequence2FeatureSequence());

			this.instances = new InstanceList(new SerialPipes(this.pipeList));            
			Reader fileReader = new InputStreamReader(new FileInputStream(new File(INPUT_FILES[inputIdx])));
			instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(.*)$"), 1, 0, 0));

			/*ParallelTopicModel*/  model = new ParallelTopicModel(NUM_TOPICS, ALPHA, BETA);
			this.model.setRandomSeed(412321);
			this.model.addInstances(instances);
			this.model.setNumThreads(NUM_THREADS);
			this.model.setNumIterations(NUM_ITERATIONS);
			this.model.setTopicDisplay(NUM_ITERATIONS, 15);
			this.model.setBurninPeriod(BURN_IN_PERIOD);
			this.model.setOptimizeInterval(OPTIMIZE_INTERVAL);
			
			

			long start = System.currentTimeMillis();
			this.model.estimate();
			long end = System.currentTimeMillis();
			System.out.println("Topic Modelling finished in " + ((end-start)/1000) + " seconds.");
			Object [][]res = this.model.getTopWords(10);
			for(int i=0;i<NUM_TOPICS;i++)
			{
				for(int j=0;j<10;j++)
				{
					System.out.print(res[i][j]+"\t");
				}
				System.out.println("\n");
			}
			System.out.println(instances.size());
			
			/*InstanceList testing = new InstanceList(this.instances.getPipe());
			testing.addThruPipe(new Instance("china in health issu", null, "test instance", null));		
			TopicInferencer inferencer = d.model.getInferencer();
			System.out.println(testing.size());
			double[] testProb = inferencer.getSampledDistribution(testing.get(0), NUM_TOPICS, 1, 5);
			for(int j=0;j<NUM_TOPICS;j++)
        	{
				System.out.print(testProb[j]+" ");
        	}*/
			System.out.println("DONE!");
		}
	}
}