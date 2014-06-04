import java.io.*;
import java.util.*;

public class CalculateRANDOMErrorBars {

	public static void main(String[] args) throws IOException{
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_minAvgPL_50x10_correctPLFormula.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/Fold3/B10/NDCG_RANDOM_errorBars_B10_QBC-LDA-PL.txt"));
		
		
		
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_half PL half disagreement_50x10.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_RANDOM_errorBars_minAvgPL_50x10_correctPLFormula.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_onlyLDASim_50x10.txt"));
		
		BufferedReader br1 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Fold1/NDCG_RANDOM_errorBars_QBC.txt"));
		BufferedReader br2 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Fold1/NDCG_RANDOM_errorBars_LDA.txt"));
		FileWriter fstream = new FileWriter("src/data/LETOR/forEval/results/2008MQ/Fold1/RANDOM_Combined");
		BufferedWriter out = new BufferedWriter(fstream);
		String line1 = br1.readLine(), line2 = br2.readLine();
		int cc=0;
		while(line1!=null && line2!= null)
		{
			String single="";
			int flag = 0;
			int repeat = 10;
			while(repeat>0)
			{
				cc=0;
				String parts1[] = line1.split("\t");
				if(flag == 0) {single += (parts1[0]+"\t"); flag = 1;}
				for(int i=0;i<parts1.length;i++)
				{
					if(parts1[i].length()==0) continue;
					double ndcg = Double.parseDouble(parts1[i]);
					if(ndcg<2)
					{
						single += (ndcg+"\t");cc++;
						//System.out.println(ndcg);
					}
				}
				String parts2[] = line2.split("\t");
				//System.out.println(parts1.length+"_"+parts2.length);
				//System.out.println(line1);
				//System.out.println(line2);
				for(int i=0;i<parts2.length;i++)
				{
					if(parts2[i].length()==0) continue;
					double ndcg = Double.parseDouble(parts2[i]);
					if(ndcg<2)
					{
						single += (ndcg+"\t");cc++;
						//System.out.println(ndcg);
					}
				}
				//single = single.trim();
				//single += (line1+"\t"+line2+"\t");
				repeat--;
				line1 = br1.readLine();
				line2 = br2.readLine();
			}
			//System.out.println(single);
			//System.out.println(cc);
			out.write(single);
			out.write("\n");
			//out.write(line2+"\n");
			
		}
		
		BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Fold1/RANDOM_Combined"));
		String line = br.readLine();
		int prev = 0;
		while(line!= null)
		{
			String[] parts = line.split("\t");
			//System.out.println(parts.length);
			int useless=0;
			double avg=0, avgError=0;
			int c=0;
				double avg1 = 0;
				c=0;
				for(int i=1;i<parts.length;i++)
				{
					parts[i] = parts[i].trim();
					if(parts[i].length()==0) {useless++;System.out.println("tab");continue;}
					//System.out.println(parts[i]);
					double ndcg = Double.parseDouble(parts[i]);
					//System.out.println(ndcg);
					//if(ndcg == 0) continue;
					if(ndcg>2) {useless++;System.out.println(ndcg);}
					else {avg1+=ndcg;c++;}
				}
				avg1 = avg1/c;
				int c2=0;
				double stdDev=0;
				for(int i=1;i<parts.length;i++)
				{
					if(parts[i].length()==0) {continue;}
					double ndcg = Double.parseDouble(parts[i]);
					double diff = ndcg - avg1;
					if(ndcg>2) {System.out.println(ndcg);}
					else {stdDev += (diff*diff);c2++;}
				}
				stdDev = Math.sqrt(stdDev);
				double error  = stdDev/c;
			
			//System.out.println("c= "+c+"_c2= "+c2+" parts size:"+parts.length);
			System.out.println((Integer.parseInt(parts[0]))+" "+avg1+" "+error/*+" c= "+c+" useless= "+useless*/);
			line= br.readLine();
			//System.exit(0);
		}
		
	}

}

/*import java.io.*;
import java.util.*;

public class CalculateRANDOMErrorBars {

	public static void main(String[] args) throws IOException{
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_minAvgPL_50x10_correctPLFormula.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/Fold3/B10/NDCG_RANDOM_errorBars_B10_QBC-LDA-PL.txt"));
		
		
		
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_half PL half disagreement_50x10.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_RANDOM_errorBars_minAvgPL_50x10_correctPLFormula.txt"));
		//BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/NDCG_errorBars_onlyLDASim_50x10.txt"));
		
		BufferedReader br1 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Fold1/NDCG_RANDOM_errorBars_QBC.txt"));
		BufferedReader br2 = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Fold1/NDCG_RANDOM_errorBars_LDA.txt"));
		FileWriter fstream = new FileWriter("src/data/LETOR/forEval/results/2008MQ/Fold1/RANDOM_Combined", true);
		BufferedWriter out = new BufferedWriter(fstream);
		String line1 = br1.readLine(), line2 = br2.readLine();
		while(line1!=null && line2!= null)
		{
			out.write(line1+"\n");
			out.write(line2+"\n");
			line1 = br1.readLine();
			line2 = br2.readLine();
		}
		
		
		BufferedReader br = new BufferedReader(new FileReader("src/data/LETOR/forEval/results/2008MQ/Fold1/NDCG_RANDOM_errorBars_QBC.txt"));
		String line = br.readLine();
		int prev = 0;
		while(line!= null)
		{
			String[] parts = line.split("\t");
			
			int repeat = 20;
			double avg=0;
			int c=0;
			while(repeat>0)
			{	if(repeat != 20)
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
				avg1 = avg1/c;
				avg += avg1;
				repeat--;
			}
			avg = avg/20;
			double stdDev=0;
			for(int i=1;i<parts.length;i++)
			{
				double diff = Double.parseDouble(parts[i]) - avg;
				stdDev += (diff*diff);
			}
			stdDev = Math.sqrt(stdDev);
			double error  = stdDev/c;
			System.out.println((Integer.parseInt(parts[0]))+" "+avg+" "+error);
			line= br.readLine();
		}
		
	}

}*/
