

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
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
	private static final int NUM_TOPICS = 4;
	private static final int NUM_ITERATIONS = 1000;
	private static final int NUM_THREADS = 1;

	private static final int OPTIMIZE_INTERVAL = 0;
	private static final int BURN_IN_PERIOD = 0;

	private static final double ALPHA = 50.0;
	private static final double BETA = 0.01;

	public static void main(String[] args) throws IOException {
		System.out.println(NUM_ITERATIONS + " Iterations; " 
		+ NUM_TOPICS + " topics; optimize interval = " + OPTIMIZE_INTERVAL 
		+ "; burn in period = " + BURN_IN_PERIOD
		+ "; initial alpha = " + (new DecimalFormat("##.#####")).format(ALPHA/NUM_TOPICS) + "; "
		+ "initial beta = " + BETA);
		System.out.println("starting...");

		for(int inputIdx = 0; inputIdx < INPUT_FILES.length; inputIdx++) {
			System.out.println("Processing file " + INPUT_FILES[inputIdx]);
			System.out.println("[Topic No.]  [Topic Proportion]  [Alpha]  [Topic Words]");
			List<Pipe> pipeList = new ArrayList<Pipe>();
			pipeList.add(new CharSequenceLowercase());
			pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
			pipeList.add(new TokenSequenceRemoveStopwords(new File(STOP_WORDS_FILE), "UTF-8", false, false, false));
			pipeList.add(new TokenSequence2FeatureSequence());

			InstanceList instances = new InstanceList(new SerialPipes(pipeList));            
			Reader fileReader = new InputStreamReader(new FileInputStream(new File(INPUT_FILES[inputIdx])));
			instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(.*)$"), 1, 0, 0));

			ParallelTopicModel model = new ParallelTopicModel(NUM_TOPICS, ALPHA, BETA);
			model.setRandomSeed(412321);
			model.addInstances(instances);
			model.setNumThreads(NUM_THREADS);
			model.setNumIterations(NUM_ITERATIONS);
			model.setTopicDisplay(NUM_ITERATIONS, 15);
			model.setBurninPeriod(BURN_IN_PERIOD);
			model.setOptimizeInterval(OPTIMIZE_INTERVAL);
			
			

			long start = System.currentTimeMillis();
			model.estimate();
			long end = System.currentTimeMillis();
			System.out.println("Topic Modelling finished in " + ((end-start)/1000) + " seconds.");
			Object [][]res = model.getTopWords(10);
			for(int i=0;i<NUM_TOPICS;i++)
			{
				for(int j=0;j<10;j++)
				{
					System.out.print(res[i][j]+"\t");
				}
				System.out.println("\n");
			}
			
			InstanceList testing = new InstanceList(instances.getPipe());
			testing.addThruPipe(new Instance("china in health issu", null, "test instance", null));
			TopicInferencer inferencer = model.getInferencer();
			double[] testProb = inferencer.getSampledDistribution(testing.get(0), NUM_TOPICS, 1, 5);
			for(int j=0;j<NUM_TOPICS;j++)
        	{
				System.out.print(testProb[j]+" ");
        	}

			/*            //--------------------------------------------------------------------------------------------------------------------
			            
			            //write inference code here using "model" variable
			            
			            
			            String line="";
			            String filename = "sample-data/forLDAdataset1/GfilEspecific.txt";

			            int ID=0;
			            
			            System.out.println("INFERENCING NOW.....");
			            
			            
			            InstanceList testing = new InstanceList(instances.getPipe());
			            BufferedReader brd = new BufferedReader(new FileReader(filename));
			            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1 FIlename = "+filename);
			            line=brd.readLine();
			            int twcount=0,i=0;
			            //HashMap<Integer,String> twid=new HashMap<Integer,String>();
			            String twid[]=new String[1000000];
			            while(line!=null)
			            {
			            	//if(!twid.containsKey(line)) twid.put(new Integer(twcount),line);
			            	twid[twcount]=line;
			            	testing.addThruPipe(new Instance(line, null, "test instance", null));
			            	line=brd.readLine();twcount++;
			            }
			            brd.close();
			            System.out.println("Added instances...going todo inference now...");
			            TopicInferencer inferencer = model.getInferencer();
			            System.out.println("Inference done...now going to populate t_is");
			            
			            
			            //testing the evaluation module
			            System.out.println("-------------------------------------------------------------------------------");
			            System.out.println("EVALUATION module...");
			            
			            
			            double[] testProb;
			            	testProb = inferencer.getSampledDistribution(testing.get(i), 10, 1, 5);
			            	double max=0,maxtopic=0;
			            	for(int j=0;j<10;j++)
			            	{
			            		if(testProb[j]>max){max=testProb[j];maxtopic=j;}
			            	}
*/
			System.out.println("DONE!");
		}
	}

}