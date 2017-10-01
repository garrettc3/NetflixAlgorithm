
public class TvShow {

	private String title;
	private String[] genre;
	private int minAge;
	private double probability;
	private String[] streamService;
	private int yearProduced;
	private int avgEpisodeLength;
	private int rottenTomatoScore;
	private int numSeasons;
	private boolean isStillAiring;
	
	public TvShow(String title, String[] genre, int minAge, String[] ss, int yp, 
			int epLen, int rtScore, int numSeasons, boolean stillAir) {
		this.title = title;
		this.genre = genre;
		this.minAge = minAge;
		this.streamService = ss;
		this.yearProduced = yp;
		this.avgEpisodeLength = epLen;
		this.rottenTomatoScore = rtScore;
		this.numSeasons = numSeasons;
		this.isStillAiring = stillAir;
	}
	
	public void setProbability(double p) {
		this.probability = p;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public String[] getGenres() {
		return this.genre;
	}
	
	public int getMinAge() {
		return this.minAge;
	}
	
	public double getProbability() {
		return this.probability;
	}
	
	public int getYearProduced() {
		return this.yearProduced;
	}
	
	public String[] getStreamServices() {
		return this.streamService;
	}
	
	public int getAverageEpisodeLength() {
		return this.avgEpisodeLength;
	}
	
	public int getRottenTomatoScore() {
		return this.rottenTomatoScore;
	}
	
	public int getNumSeasons() {
		return this.numSeasons;
	}
	
	public boolean getIsStillAiring() {
		return this.isStillAiring;
	}
	
}
