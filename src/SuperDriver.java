import java.io.*;

import org.apache.commons.io.FileUtils;

public class SuperDriver {

	public static String suffix = "FScoreWithNormalizedLR";
	//public static String suffix = "LDA_AGAIN";

	public static int nFolds = 5;
	public static double wt1=0.1, wt2=0.9;
	public static int f1=1, f2=9;
	
	public static void main(String[] args) throws Exception {
		

		while(wt2>wt1)
		{
			for(int j=1;j<=nFolds;j++)
			{
				// bring in the files
				cleanBeforeFoldStart();
				moveFilesBeforeStartOfFold(j);

				int numIterations = 1;
				for(int i=0;i<numIterations;i++)
				{
					clean();
					//try
					{
						new Driver(wt1, wt2);
					} //catch (Exception e) {System.err.println(e.toString());continue;}
				}

				moveFilesAfterEndOfFold(j);
				
			}
			wt1+=0.1;wt2-=0.1;
			f1++;f2--;
		}
	}

	private static void cleanBeforeFoldStart() {
		File dir = new File("src/data/LETOR/");
		for(File files: dir.listFiles()) {if(files.getName().equalsIgnoreCase("forEval") || files.getName().equalsIgnoreCase("queryTerms")); else files.delete();}

		dir = new File("src/data/LETOR/forEval/");
		for(File files: dir.listFiles()) {if(files.getName().equalsIgnoreCase("randomQ") || files.getName().equalsIgnoreCase("results")); else files.delete();}
	}

	private static void moveFilesAfterEndOfFold(int j) throws IOException {
		String sFile1 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/NDCG_errorBars.txt";
		String sFile2 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/AP_errorBars.txt";
		String sFile3 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/forEval/resultsAT10.txt";

		int t1 = (int)wt1*10, t2=(int)wt2*10;
		/*String dFile1 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/forEval/results/2008MQ/Fold"+j+"/NDCG_errorBars_"+suffix+f1+"-"+f2+"_"+j+"1.txt";
		String dFile2 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/forEval/results/2008MQ/Fold"+j+"/AP_errorBars_"+suffix+f1+"-"+f2+"_"+j+"1.txt";
		String dFile3 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/forEval/results/2008MQ/Fold"+j+"/resultsAT10_"+suffix+f1+"-"+f2+"_"+j+"1.txt";
		*/
		/*
		String dFile1 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/forEval/results/2008MQ/Fold"+j+"/NDCG_errorBars_"+suffix+j+"1.txt";
		String dFile2 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/forEval/results/2008MQ/Fold"+j+"/AP_errorBars_"+suffix+j+"1.txt";
		String dFile3 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/forEval/results/2008MQ/Fold"+j+"/resultsAT10_"+suffix+j+"1.txt";
		*/
		String dFile1 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/forEval/results/2007MQ/Fold"+j+"/NDCG_errorBars_"+suffix+f1+"-"+f2+"_"+j+"1.txt";
		String dFile2 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/forEval/results/2007MQ/Fold"+j+"/AP_errorBars_"+suffix+f1+"-"+f2+"_"+j+"1.txt";
		String dFile3 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/forEval/results/2007MQ/Fold"+j+"/resultsAT10_"+suffix+f1+"-"+f2+"_"+j+"1.txt";
		File source, dest;
		source = new File(sFile1);
		dest = new File(dFile1);
		FileUtils.copyFile(source, dest);


		source = new File(sFile2);
		dest = new File(dFile2);
		FileUtils.copyFile(source, dest);

		source = new File(sFile3);
		dest = new File(dFile3);
		FileUtils.copyFile(source, dest);
	}

	private static void moveFilesBeforeStartOfFold(int j) throws IOException{
		String sFile1 = "/Users/rishabhmehrotra/dev/UCL/ActiveQuerySelection/LETOR_Dataset/MQ2007/Fold"+j+"/train.txt";
		String sFile2 = "/Users/rishabhmehrotra/dev/UCL/ActiveQuerySelection/LETOR_Dataset/MQ2007/Fold"+j+"/test.txt";
		String sFile3 = "/Users/rishabhmehrotra/dev/UCL/ActiveQuerySelection/LETOR_Dataset/MQ2007/Fold"+j+"/valid.txt";

		String dFile11 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/train.txt";
		String dFile21 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/test.txt";
		String dFile31 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/valid.txt";
		String dFile12 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/forEval/train.txt";
		String dFile22 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/forEval/test.txt";
		String dFile32 = "/Users/rishabhmehrotra/dev/workspace/ActiveQuerySelection/src/data/LETOR/forEval/valid.txt";

		File source, dest1, dest2;
		source = new File(sFile1);
		dest1 = new File(dFile11);
		dest2 = new File(dFile12);
		FileUtils.copyFile(source, dest1);
		FileUtils.copyFile(source, dest2);

		source = new File(sFile2);
		dest1 = new File(dFile21);
		dest2 = new File(dFile22);
		FileUtils.copyFile(source, dest1);
		FileUtils.copyFile(source, dest2);

		source = new File(sFile3);
		dest1 = new File(dFile31);
		dest2 = new File(dFile32);
		FileUtils.copyFile(source, dest1);
		FileUtils.copyFile(source, dest2);

	}

	public static void clean()
	{
		File dir = new File("src/data/LETOR/");
		for(File files: dir.listFiles()) {if(files.getName().equalsIgnoreCase("test.txt") || files.getName().contains("errorBars") || files.getName().equalsIgnoreCase("valid.txt") || files.getName().equalsIgnoreCase("train.txt") || files.getName().equalsIgnoreCase("forEval") || files.getName().equalsIgnoreCase("queryTerms")); else files.delete();}

		dir = new File("src/data/LETOR/forEval/");
		for(File files: dir.listFiles()) {if(files.getName().equalsIgnoreCase("test.txt") || files.getName().equalsIgnoreCase("valid.txt") || files.getName().equalsIgnoreCase("resultsAT10.txt") || files.getName().equalsIgnoreCase("randomQ") || files.getName().equalsIgnoreCase("results")); else files.delete();}

		dir = new File("src/data/LETOR/forEval/randomQ/");
		for(File files: dir.listFiles()) files.delete();

	}

}
