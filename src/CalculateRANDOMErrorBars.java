import java.io.*;
import java.util.*;

public class CalculateRANDOMErrorBars {

	public static void main(String[] args) throws IOException{
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_minAvgPL_50x10_correctPLFormula.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/Fold3/B10/NDCG_RANDOM_errorBars_B10_QBC-LDA-PL.txt"));
		
		BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/NDCG_RANDOM_errorBars.txt"));
		
		
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_half PL half disagreement_50x10.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_RANDOM_errorBars_minAvgPL_50x10_correctPLFormula.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_onlyLDASim_50x10.txt"));
		String line = br.readLine();
		int prev = 0;
		while(line!= null)
		{
			String[] parts = line.split("\t");
			/*int numQ = Integer.parseInt(parts[0]);
			int flag = 0;
			if(numQ == prev) ;//{line = br.readLine();continue;}
			else prev = numQ;*/
			int repeat = 10;
			double avg=0;
			int c=0;
			while(repeat>0)
			{	if(repeat != 10)
				{
					line = br.readLine();
					parts = line.split("\t");
				}
				double avg1 = 0;
				c=0;
				for(int i=1;i<parts.length;i++)
				{
					double ndcg = Double.parseDouble(parts[i]);
					//System.out.println(ndcg);
					//if(ndcg == 0) continue;
					avg1+=ndcg;c++;
				}
				//System.exit(0);
				avg1 = avg1/c;
				//System.out.print(avg1+" ");
				avg += avg1;
				repeat--;
				
			}
			avg = avg/10;
			//System.out.println("avgNDCG: "+avg+" = "+avg);
			// System.out.println("Calculating standard deviation now:");
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
			//System.out.println(avg);
			line= br.readLine();
		}
		
	}

}
