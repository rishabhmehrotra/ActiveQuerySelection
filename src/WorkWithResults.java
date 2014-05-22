import java.io.*;
import java.util.*;
public class WorkWithResults {
	
	public String candidatefile;
	public String prediction1File, prediction2File, prediction3File, prediction4File;
	public int subsetNo;
	public QDataset d;
	//Query[] listOfCandidateQueries;
	ArrayList<Query> listOfCandidates;

	
	public WorkWithResults(String candidatefile, int i, QDataset d, String prediction1File, String prediction2File, String prediction3File, String prediction4File)
	{
		this.candidatefile = candidatefile;
		this.subsetNo = i;
		this.d = d;
		this.prediction1File = prediction1File;
		this.prediction2File = prediction2File;
		this.prediction3File = prediction3File;
		this.prediction4File = prediction4File;
		//this.listOfCandidateQueries = new Query[10000];
		this.listOfCandidates = new ArrayList<Query>();
		populateCandidateScores();
		// so now we have all the test queries populated with their respective scores from each of the models learnt
		computeDisagreement();
		computePLScores();
	}
	
	public void computePLScores()
	{
		
	}

	public void computeDisagreement() {
		int size = d.nCandidateQ;//d.listOfTestQueries.length;
		System.out.println("Size of the listOfCandidateQueries= "+d.nCandidateQ+" to confirm: "+listOfCandidates.size());
		Iterator<Query> itr = listOfCandidates.iterator();
		//for(int k=0;k<size;k++)
		while(itr.hasNext())
		{
			//Query q = listOfCandidates[k];
			Query q = itr.next();
			if(q.nD==0) {System.out.println("SKIPPING...");continue;}
			//System.out.println("Query: "+q.nD+"_"+q.listOfDocuments.get(0).docFeatures);
			// now we want to calculate the disagreement score for query q
			double Vij = 0.0;// Vij denotes the no of votes given by the 2 committee members that agree that doc i is to ranked higher than doc j
			// for Vij we need to have a nested loop for iteration through pairs of docs.
			/*Document[] docs = null;
			docs = (Document[]) q.listOfDocuments.toArray(docs);*/
			
			Document[] docs = getArrayListAsArray(q.listOfDocuments);
			
			
			int sizeDocs = docs.length;
			//System.out.println("sizeDocs= "+sizeDocs);
			double sum = 0.0;
			for(int i=0; i<sizeDocs; i++)
			{
				Vij=0.0;
				for(int j=0;j<sizeDocs;j++)
				{
					Vij = 0.0;
					if(docs[i].score1 > docs[j].score1) Vij+=1;
					if(docs[i].score2 > docs[j].score2) Vij+=1;
					if(docs[i].score3 > docs[j].score3) Vij+=1;
					if(docs[i].score4 > docs[j].score4) Vij+=1;
					
					if(Vij==0) continue;
					
					double fraction = Vij/4.0;
					double fract2 = Math.log(fraction);
					sum += (Vij*fract2);
					//System.out.print("Score1: "+docs[i].score1+" Score2: "+docs[i].score2+" Score1: "+docs[j].score1+" Score2: "+docs[j].score2+" Vij= "+Vij+" Fraction= "+fraction+"_"+fract2+" Sum= "+sum);
				}
				//System.out.println();
			}
			Double disagreement = (-1*sum)/4;
			//System.out.println("disagreement = "+disagreement);
			//if(disagreement < 0) {System.out.println("-ve disagreement value: "+disagreement);System.exit(0);}
			q.setDisagreement(disagreement);
		}
	}

	public Document[] getArrayListAsArray(ArrayList<Document> listOfDocuments) {
		Document[] docs = new Document[listOfDocuments.size()];
		//System.out.println("inside convert ArrayList to Array function, size = "+listOfDocuments.size());
		int count = 0;
		Iterator<Document> itr = listOfDocuments.iterator();
		while(itr.hasNext())
		{
			Document d = itr.next();
			docs[count++] = d;
		}
		return docs;
	}

	public void populateCandidateScores() {
		BufferedReader br, br1, br2, br3, br4;
		try {
			br = new BufferedReader(new FileReader(this.candidatefile));
			br1 = new BufferedReader(new FileReader(this.prediction1File));
			br2 = new BufferedReader(new FileReader(this.prediction2File));
			br3 = new BufferedReader(new FileReader(this.prediction3File));
			br4 = new BufferedReader(new FileReader(this.prediction4File));
			String line = br.readLine();
			String line1 = br1.readLine();
			String line2 = br2.readLine();
			String line3 = br3.readLine();
			String line4 = br4.readLine();
			
			String prevQID = "";
			Query q = new Query();
			int c=0, nCQ=0;
			Double score1 = 0.0, score2 = 0.0, score3 = 0.0, score4 = 0.0;
			
			while(line != null)
			{
				String qID = line.substring(6, line.indexOf(' ', 6));
				//System.out.println("WWR qID= "+qID);
				score1 = Double.parseDouble(line1);
				score2 = Double.parseDouble(line2);
				score3 = Double.parseDouble(line3);
				score4 = Double.parseDouble(line4);
				if(prevQID.compareTo(qID) != 0)
				{
					//System.out.println("new query found:"+prevQID+" "+qID);
					// new query found
					//first add previous query to the list of queries in the dataset
					//listOfCandidateQueries[nTQ++] = q;
					listOfCandidates.add(q);
					nCQ++;
					c+=q.nD;
					q = new Query();
				}
				prevQID = qID;
				q.addDoc(line, score1, score2, score3, score4);
				if(nCQ%1000 == 0) System.out.println(qID);
				
				line = br.readLine();
				line1 = br1.readLine();
				line2 = br2.readLine();
				line3 = br3.readLine();
				line4 = br4.readLine();
			}
			//listOfCandidateQueries[nTQ++] = q;
			listOfCandidates.add(q);
			nCQ++;
			//d.setListOfCandidateQueries(listOfCandidateQueries);
			//d.setnTQ(nTQ);
			d.setCandidates(listOfCandidates);
			d.setnCandidateQ(nCQ);
			c+= q.nD;
			//for(int kk=0;kk<nCQ;kk++) System.out.println(listOfCandidates.get(kk).listOfDocuments.size());
			System.out.println("No of test queries populated with their respective scores nCQ= "+nCQ);
			System.out.println("cT= "+c);
		} catch (FileNotFoundException e) {e.printStackTrace();} catch (IOException e) {e.printStackTrace();}
	}
	
	
}
