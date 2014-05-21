import java.util.*;

public class Query {
	//public ArrayList<String> listOfDocuments;
	public ArrayList<Document> listOfDocuments;
	public int nD;
	public Double disagreement;
	public int qID;
	public double NDCG;
	public float currentAvgSimilarity;
	public Double normalizedDisagreement;
	public Double combine;

	public Query()
	{
		nD = 0;
		this.disagreement = 0.0;
		this.currentAvgSimilarity = 0;
		//listOfDocuments = new ArrayList<String>();
		listOfDocuments = new ArrayList<Document>();
	}
	
	public void addDoc(String line)
	{
		String qID = line.substring(6, line.indexOf(' ', 6));
		this.qID = Integer.parseInt(qID);
		Document d = new Document(line);
		listOfDocuments.add(d);
		nD++;
	}
	
	public void addDoc(String line, Double testScore)
	{
		String qID = line.substring(6, line.indexOf(' ', 6));
		this.qID = Integer.parseInt(qID);
		Document d = new Document(line, testScore);
		this.listOfDocuments.add(d);
		nD++;
		//System.out.println("Doc added for query, testScore:"+qID+" "+testScore);
	}
	
	public void addDoc(String line, Double score1, Double score2)
	{
		String qID = line.substring(6, line.indexOf(' ', 6));
		this.qID = Integer.parseInt(qID);
		Document d = new Document(line, score1, score2);
		this.listOfDocuments.add(d);
		nD++;
	}
	
	public void addDoc(String line, Double score1, Double score2, Double score3, Double score4)
	{
		String qID = line.substring(6, line.indexOf(' ', 6));
		this.qID = Integer.parseInt(qID);
		Document d = new Document(line, score1, score2, score3, score4);
		this.listOfDocuments.add(d);
		nD++;
	}
	
	public Double getDisagreement() {
		return disagreement;
	}

	public void setDisagreement(Double disagreement) {
		this.disagreement = disagreement;
	}
	
	public void setNormalizedDisagreement(Double normalizedDisagreement) {
		this.normalizedDisagreement = normalizedDisagreement;
	}
}
