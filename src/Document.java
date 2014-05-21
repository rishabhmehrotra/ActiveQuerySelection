
public class Document {
	public String docFeatures;
	public Double score1;
	public Double score2;
	public Double score3;
	public Double score4;
	public double testScore;
	public int relevance;
	
	public Document(String docFeatures)
	{
		this.docFeatures = docFeatures;
		this.relevance = Integer.parseInt(docFeatures.charAt(0)+"");
		this.score1 = 0.0;
		this.score2 = 0.0;
		this.score3 = 0.0;
		this.score4 = 0.0;
	}
	
	public Document(String docFeatures, Double testScore)
	{
		this.docFeatures = docFeatures;
		this.relevance = Integer.parseInt(docFeatures.charAt(0)+"");
		this.testScore = testScore;
	}

	public Document(String docFeatures, Double score1, Double score2) {
		this.docFeatures = docFeatures;
		this.relevance = Integer.parseInt(docFeatures.charAt(0)+"");
		this.score1 = score1;
		this.score2 = score2;
		//System.out.println("Doc created with scores: "+score1+" "+score2);
	}
	
	public Document(String docFeatures, Double score1, Double score2, Double score3, Double score4) {
		this.docFeatures = docFeatures;
		this.relevance = Integer.parseInt(docFeatures.charAt(0)+"");
		this.score1 = score1;
		this.score2 = score2;
		this.score3 = score3;
		this.score4 = score4;
		//System.out.println("Doc created with scores: "+score1+" "+score2);
	}
}
