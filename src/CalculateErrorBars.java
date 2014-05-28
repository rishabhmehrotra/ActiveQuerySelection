import java.io.*;
import java.util.*;

public class CalculateErrorBars {

	public static void main(String[] args) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_minAvgPL_50x10_correctPLFormula.txt"));
		String line = br.readLine();
		while(line!= null)
		{
			String[] parts = line.split("\t");
			int numQ = Integer.parseInt(parts[0]);
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
			stdDev /= c;
			//System.out.print(stdDev+" ");
			//System.out.print(avg+" ");
			System.out.print(parts[0]+" ");
			line= br.readLine();
		}
		
	}

}
