import java.io.*;
import java.util.*;

public class CalculateErrorBars {

	public static void main(String[] args) throws IOException{
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_minAvgPL_50x10_correctPLFormula.txt"));
		BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_onlyNDisagreement_50x10_all committees correct.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_half PL half disagreement_50x10.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_RANDOM_errorBars_minAvgPL_50x10_correctPLFormula.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_onlyLDASim_50x10.txt"));
		String line = br.readLine();
		int prev = 0;
		while(line!= null)
		{
			String[] parts = line.split("\t");
			int numQ = Integer.parseInt(parts[0]);
			if(numQ == prev) {line = br.readLine();continue;}
			else prev = numQ;
			double avg=0;int c=0;
			for(int i=1;i<parts.length;i++)
			{
				double ndcg = Double.parseDouble(parts[i]);
				//if(ndcg == 0) continue;
				avg+=ndcg;c++;
			}
			avg = avg/c;
			//System.out.println("avgNDCG: "+avg+" = "+avg);
			// System.out.println("Calculatign standard deviation now:");
			double stdDev=0;
			for(int i=1;i<parts.length;i++)
			{
				double diff = Double.parseDouble(parts[i]) - avg;
				//System.out.println("diff: "+Double.parseDouble(parts[i])+" - "+avg+" = "+diff);
				stdDev += (diff*diff);
			}
			stdDev = Math.sqrt(stdDev);
			double error  = stdDev/c;
			//stdDev /= c;
			//System.out.print(stdDev+" ");
			//System.out.print(avg+" ");
			System.out.println((Integer.parseInt(parts[0]))+" "+avg+" "+error);
			line= br.readLine();
		}
		
	}

}
