import java.io.*;

public class CombineErrorBars {

	public static void main(String[] args) throws IOException{
		//moveFile("src/data/LETOR/", "test.txt", "src/data/LETOR/forEval/results/");
		//System.exit(0);
		
		//String algo = "topic_top10_PL";
		//String algo = "FScoreWithNormalizedLR5-5_";
		String algo = "FScoreWithNormalizedLR5-5_";
		/*
		BufferedReader br1 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/NDCG_errorBars_B10_"+algo+"11.txt"));
		BufferedReader br2 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/NDCG_errorBars_B10_"+algo+"21.txt"));
		BufferedReader br3 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/NDCG_errorBars_B10_"+algo+"31.txt"));
		BufferedReader br4 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/NDCG_errorBars_B10_"+algo+"41.txt"));
		BufferedReader br5 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/NDCG_errorBars_B10_"+algo+"51.txt"));
		*/
		BufferedReader br1 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/NDCG_errorBars_"+algo+"11.txt"));
		BufferedReader br2 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/NDCG_errorBars_"+algo+"21.txt"));
		BufferedReader br3 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/NDCG_errorBars_"+algo+"31.txt"));
		BufferedReader br4 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/NDCG_errorBars_"+algo+"41.txt"));
		BufferedReader br5 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/NDCG_errorBars_"+algo+"51.txt"));
		
		/*BufferedReader br1 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/NDCG_RANDOM_errorBars_B10_"+algo+"1.txt"));
		BufferedReader br2 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/NDCG_RANDOM_errorBars_B10_"+algo+"2.txt"));
		BufferedReader br3 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/NDCG_RANDOM_errorBars_B10_"+algo+"3.txt"));
		BufferedReader br4 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/NDCG_RANDOM_errorBars_B10_"+algo+"4.txt"));
		BufferedReader br5 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/NDCG_RANDOM_errorBars_B10_"+algo+"5.txt"));
		*/
		/*
		BufferedReader br1 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/RANDOM_Combined1"));
		BufferedReader br2 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/RANDOM_Combined2"));
		BufferedReader br3 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/RANDOM_Combined3"));
		BufferedReader br4 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/RANDOM_Combined4"));
		BufferedReader br5 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/RANDOM_Combined5"));
		*/
		/*
		BufferedReader br1 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/NDCG_errorBars_"+algo+"11.txt"));
		//BufferedReader br12 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/NDCG_errorBars_"+algo+"12.txt"));
		//BufferedReader br13 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/NDCG_errorBars_"+algo+"13.txt"));
		BufferedReader br2 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/NDCG_errorBars_"+algo+"21.txt"));
		BufferedReader br3 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/NDCG_errorBars_"+algo+"31.txt"));
		BufferedReader br4 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/NDCG_errorBars_"+algo+"41.txt"));
		BufferedReader br5 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/NDCG_errorBars_"+algo+"51.txt"));
		*/
		
		/*BufferedReader br1 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/AP_errorBars_"+algo+"1.txt"));
		BufferedReader br2 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/AP_errorBars_"+algo+"2.txt"));
		BufferedReader br3 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/AP_errorBars_"+algo+"3.txt"));
		BufferedReader br4 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/AP_errorBars_"+algo+"4.txt"));
		BufferedReader br5 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/AP_errorBars_"+algo+"5.txt"));
		
		BufferedReader br12=null;
		if(algo.compareTo("LDA")==0) br12 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/AP_errorBars_"+algo+"12.txt"));
		BufferedReader br13=null;
		if(algo.compareTo("LDA")==0) br13 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/AP_errorBars_"+algo+"13.txt"));
		*/
		
		/*BufferedReader br1 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/RANDOM_Combined1"));
		BufferedReader br2 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/RANDOM_Combined2"));
		BufferedReader br3 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/RANDOM_Combined3"));
		BufferedReader br4 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/RANDOM_Combined4"));
		BufferedReader br5 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/RANDOM_Combined5"));
		BufferedReader br12 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Fold1/NDCG_RANDOM_errorBars_LDA12.txt"));
		BufferedReader br13 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Fold1/NDCG_RANDOM_errorBars_LDA13.txt"));
		*/
		
		/*BufferedReader br1 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/AP_RANDOM_errorBars_"+algo+"1.txt"));
		BufferedReader br2 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/AP_RANDOM_errorBars_"+algo+"2.txt"));
		BufferedReader br3 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/AP_RANDOM_errorBars_"+algo+"3.txt"));
		BufferedReader br4 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/AP_RANDOM_errorBars_"+algo+"4.txt"));
		BufferedReader br5 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/AP_RANDOM_errorBars_"+algo+"5.txt"));
		
		BufferedReader br12=null;
		if(algo.compareTo("LDA")==0) br12 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/AP_RANDOM_errorBars_"+algo+"12.txt"));
		BufferedReader br13=null;
		if(algo.compareTo("LDA")==0) br13 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/AP_RANDOM_errorBars_"+algo+"13.txt"));
		*/
		
		String line1 = br1.readLine();
		String line2 = br2.readLine();
		String line3 = br3.readLine();
		String line4 = br4.readLine();
		String line5 = br5.readLine();
		
		String line12=null;
		//if(algo.compareTo("LDA")==0) line12 = br12.readLine();
		String line13=null;
		//if(algo.compareTo("LDA")==0) line13 = br13.readLine();
		
		//FileWriter fstream = new FileWriter("src/data/LETOR/forEval/results/2008MQ/Combined/COMBINED_"+algo);
		//FileWriter fstream = new FileWriter("src/data/LETOR/forEval/results/2007MQ/Combined/COMBINED_RANDOM");
		FileWriter fstream = new FileWriter("src/data/LETOR/forEval/results/2007MQ/Combined/COMBINED_"+algo);
		//FileWriter fstream = new FileWriter("src/data/LETOR/forEval/results/2008MQ/Combined/AP_COMBINED_RANDOM");
		BufferedWriter out = new BufferedWriter(fstream);
		int cc = 0;
		while(line1!=null || line2!=null || line3!=null || line4!=null || line5!=null)
		{
			String combine = "";
			String parts1[] = line1.split("\t");
			if(line1!=null) combine += line1;
			if(line2!=null) combine += line2.substring(line2.indexOf('.')-1);
			if(line3!=null) combine += line3.substring(line3.indexOf('.')-1);
			if(line4!=null) combine += line4.substring(line4.indexOf('.')-1);
			if(line5!=null) combine += line5.substring(line5.indexOf('.')-1);
			
			//if(algo.compareTo("LDA")==0) combine += line12.substring(line12.indexOf('.')-1);
			//if(algo.compareTo("LDA")==0) combine += line13.substring(line13.indexOf('.')-1);
			
			out.write(combine);
			out.write("\n");
			System.out.println("Successfully printed: "+parts1[0]);
			line1 = br1.readLine();
			//if(algo.compareTo("LDA")==0) line12 = br12.readLine();
			//if(algo.compareTo("LDA")==0) line13 = br13.readLine();
			line2 = br2.readLine();
			line3 = br3.readLine();
			line4 = br4.readLine();
			line5 = br5.readLine();
		}
		out.close();
	}
	
	public static void moveFile(String inputPath, String inputFile, String outputPath) {

	    InputStream in = null;
	    OutputStream out = null;
	    try {

	        //create output directory if it doesn't exist
	        File dir = new File (outputPath); 
	        if (!dir.exists())
	        {
	            dir.mkdirs();
	        }


	        in = new FileInputStream(inputPath + inputFile);        
	        out = new FileOutputStream(outputPath + inputFile);

	        byte[] buffer = new byte[1024];
	        int read;
	        while ((read = in.read(buffer)) != -1) {
	            out.write(buffer, 0, read);
	        }
	        in.close();
	        in = null;

	            // write the output file
	            out.flush();
	        out.close();
	        out = null;

	        // delete the original file
	        new File(inputPath + inputFile).delete();  


	    } 

	         catch (FileNotFoundException fnfe1) {
	        	 System.err.print(fnfe1.getMessage());
	    }
	          catch (Exception e) {
	        	  System.err.print(e.getMessage());
	    }

	}
	

}
