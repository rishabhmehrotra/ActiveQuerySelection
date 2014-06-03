import java.util.*;

public class Query {
	//public ArrayList<String> listOfDocuments;
	public ArrayList<Document> listOfDocuments;
	public int nD;
	public Double disagreement;
	public int qID;
	public double NDCG;
	public double AP;
	public float currentAvgSimilarity;
	public float similarityLDA;
	public float normalizedSimilarity;
	public float normalizedLDASimilarity;
	
	public HashMap<String, Float> termMap;
	public String termString;

	public Double normalizedDisagreement;
	public Double combine;
	public Double combine2;
	public Double combine3;
	public double PL1, PL2, PL3, PL4, avgPL;

	public Query()
	{
		nD = 0;
		this.termString = "";
		this.similarityLDA= (float) 0.0;
		this.disagreement = 0.0;
		this.currentAvgSimilarity = 0;
		this.normalizedDisagreement = 0.0;
		this.PL1 = this.PL2 = this.PL3 = this.PL4 = 0.0;
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
	
	public void setNormalizedSimilarity(float normalizedSimilarity) {
		this.normalizedSimilarity = normalizedSimilarity;
	}
	
	public void setNormalizedLDASimilarity(float normalizedLDASimilarity) {
		this.normalizedLDASimilarity = normalizedLDASimilarity;
	}
	
	public HashMap<String, Float> getTermMap() {
		return termMap;
	}

	public void setTermMap(HashMap<String, Float> termMap) {
		this.termMap = termMap;
	}
	
	public void setTermString(String termString) {
		this.termString = termString;
	}
	
	public void setPL1(double pL1) {
		PL1 = pL1;
	}

	public void setPL2(double pL2) {
		PL2 = pL2;
	}

	public void setPL3(double pL3) {
		PL3 = pL3;
	}

	public void setPL4(double pL4) {
		PL4 = pL4;
	}

	public void setAvgPL(double avgPL) {
		this.avgPL = avgPL;
	}
}
