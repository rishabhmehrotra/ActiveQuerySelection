import java.io.*;
import java.util.*;

public class CalculateErrorBars {

	public static void main(String[] args) throws IOException{
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_minAvgPL_50x10_correctPLFormula.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_onlyNDisagreement_50x10_all committees correct.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_half PL half disagreement_50x10.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_RANDOM_errorBars_minAvgPL_50x10_correctPLFormula.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_onlyLDASim_50x10.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_onlyDisagreement_1000CandidateSize.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_byPL_1000CandidateSize_WrongRandom_CorrectOthers.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_LDASim_WrongRandom_CorrectLDASim_1000Candidates.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_halfPL_halfQBC_CorrectRandom_1000C.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Fold2/RANDOM_Combinedap"));
		//src/data/LETOR/forEval/results/2008MQ/Fold5/NDCG_errorBars_LDA.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Fold4/NDCG_errorBars_PL41.txt"));
		
		BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Combined/COMBINED_LScore10_"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2007MQ/Combined/COMBINED_topic_top10_PL"));
		String line = br.readLine();
		//line = line+line.substring(line.indexOf('.')-1)+line.substring(line.indexOf('.')-1)+line.substring(line.indexOf('.')-1)+line.substring(line.indexOf('.')-1)+line.substring(line.indexOf('.')-1);
		//System.out.println(line);
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
			int c2=0;
			for(int i=1;i<parts.length;i++)
			{
				double diff = Double.parseDouble(parts[i]) - avg;
				//System.out.println("diff: "+Double.parseDouble(parts[i])+" - "+avg+" = "+diff);
				stdDev += (diff*diff);
				c2++;
			}
			stdDev = Math.sqrt(stdDev);
			double error  = stdDev/c;
			//System.out.println(c+"_"+c2+"_"+parts.length);
			//stdDev /= c;
			//System.out.print(stdDev+" ");
			//System.out.print(avg+" ");
			System.out.println((Integer.parseInt(parts[0]))+" "+avg+" "+error);
			line= br.readLine();
			//System.exit(0);
		}
		
	}

}
