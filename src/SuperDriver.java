import java.io.*;

public class SuperDriver {

	public static void main(String[] args) throws Exception {
		int numIterations = 1;
		for(int i=0;i<numIterations;i++)
		{
			clean();
			//try
			{
				new Driver();
			} //catch (Exception e) {System.err.println(e.toString());continue;}
		}
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
