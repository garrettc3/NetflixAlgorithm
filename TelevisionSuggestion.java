import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Scanner;

public class TelevisionSuggestion {

	
	public static void main(String [] args) {
		
		Scanner in = new Scanner(System.in);
		//loop through program until user says to stop
		while(true) {
			//welcoming message/instructions
			System.out.println("Welcome to Suggest a TV Show!");
			System.out.println("For answers where multiple options apply, please enter "
					+ "your selections, separated by only commas.\n");
			//initialize list of suggestions
			ArrayList<TvShow> suggestions = initializeSuggestions();
			
			//find out user's current streaming options, filter out shows not available to them
			System.out.println("Which streaming services do you currently have? (Netflix, Amazon Prime, Hulu, HBO)");
			String[] streamServices = in.nextLine().split(",");
			suggestions = findAvailibleShows(suggestions, streamServices);
			
			//find out if user wants family friendly shows, filter accordingly
			System.out.println("Does the show need to be family friendly? (Y/N)");
			String ff = in.nextLine();
			if (ff.equals("Y") || ff.equals("y"))
				suggestions = parentalControl(suggestions);
			
			//calculate probability of user interest in show based on rotten tomatoes score and their
			//level of trust in Rotten Tomatoes ratings
			System.out.println("On a scale from 1-10, 1 meaning that you almost never agree with them"
					+ "and 10 meaning you almost always agree with them, ");
			System.out.println("how frequently do you agree with Rotten Tomatoes movie or tv show "
					+ "review scores?");
			int rottenTrust = in.nextInt();
			suggestions = calculateProbWithReviews(suggestions, rottenTrust);
			
			//skip blank input line for Java's weird Scanner class
			in.nextLine();
			//ask user's preference on whether they want an older or newer show
			System.out.println("Would you prefer an older, or newer show?(O/N)");
			String oldNew = in.nextLine();
			suggestions = calculateProbWithAge(suggestions, oldNew);
			
			//ask user if they prefer longer or shorter episodes
			System.out.println("Do you prefer longer, or shorter episodes?(L/S)");
			String longShort = in.nextLine();
			suggestions = calculateProbWithRuntime(suggestions, longShort);
			
			//ask user if they prefer a show with more or less seasons
			System.out.println("Do you want a show with many, or few seasons?(M/F)");
			String manyFew = in.nextLine();
			suggestions = calculateProbWithSeasons(suggestions, manyFew);
			
			//ask user what genre they prefer
			System.out.println("Please select, from the following, which topics interest you: "
					+ "Comedy, Drama, Psychology, Politics, Fantasy/Sci-Fi, Crime");
			String genreString = in.nextLine();
			String[] genres = genreString.split(",");
			suggestions = calculateProbWithGenres(suggestions, genres);
			
			//ask user if they want a show that is still producing new episodes
			System.out.println("Do you want a show that is already over, or still in production?(Over/Ongoing)");
			String overOngoing = in.nextLine();
			suggestions = calculateProbWithOverOngoing(suggestions, overOngoing);

			//sort shows by probability, display top 5 results to user
			DecimalFormat d2 = new DecimalFormat(".##");
			Comparator<TvShow> c = new probSort();
			PriorityQueue<TvShow> finalList = new PriorityQueue<TvShow>(suggestions.size(), c);
			for (TvShow t : suggestions)
				finalList.add(t);
			System.out.println("Your top 5 recommended shows are:");
			for (int i = 0; i < 5; i++) {
				if (finalList.peek() != null) {
					TvShow show = finalList.poll();
					String title = show.getTitle();
					double prob = show.getProbability();
					System.out.println(title + " - with " + d2.format(prob * 100) + "% certainty");
				}
			}
			
			//ask if user wants to go again
			System.out.println("Would you like a new suggestion?");
			String goAgain = in.nextLine();
			if (goAgain.equals("No") || goAgain.equals("no") || goAgain.equals("N") 
					|| goAgain.equals("n"))
				break;
		}
		System.out.println("Thank you for using Suggest a TV Show!");
		in.close();
	}
	
	//function to perform bayes formula
	public static double bayesFormula(double evidence, double denominator, double hypo) {
		
		return (evidence * hypo) / denominator;
	}
	
	//Function to initialize list of suggestions with data from .csv file
	public static ArrayList<TvShow> initializeSuggestions() {
		
		//initialize variables for csv file parsing
		String csvFile = "tvshows.csv";
		BufferedReader br = null;
		String line = "";
		String csvSplitBy = ",";
		ArrayList<TvShow> shows = new ArrayList<TvShow>();
		
		try {
			//set up buffered reader to read csv file
			br = new BufferedReader(new FileReader(csvFile));
			br.readLine();
			while ((line = br.readLine()) != null) {
				
				//read line-by-line
				String[] show = line.split(csvSplitBy);
				//get title
				String title = show[0];
				//get list of genres, split by semicolons
				String[] genre = show[1].split(";");
				//get minimum age recommendation
				int minAge = Integer.parseInt(show[2]);
				//get list of possible streaming services, split by semicolons
				String[] streamService = show[3].split(";");
				//get year show was debuted
				int yearProduced = Integer.parseInt(show[4]);
				//get average episode length of show
				int avgEpisodeLength = Integer.parseInt(show[5]);
				//get rotten tomatoes score for show
				int rottenTomatoScore = Integer.parseInt(show[6]);
				//get number of seasons
				int numSeasons = Integer.parseInt(show[7]);
				//is the show still running new episodes?
				boolean stillAir = false;
				if (show[8].equals("Y"))
					stillAir = true;
				//create show object, add to initial arraylist
				TvShow s = new TvShow(title, genre, minAge, streamService, 
						yearProduced, avgEpisodeLength, rottenTomatoScore, numSeasons, 
						stillAir);
				shows.add(s);
			}
		}
		//catch possible exceptions
		catch(FileNotFoundException e) {
			System.out.println("Error finding file");
		}
		
		catch(IOException e) {
			System.out.println("IO Error");
		}
		finally {
			//close file
			if (br != null) {
				try {
					br.close();
				}
				catch(IOException e) {
					System.out.println("IO Error");
				}
			}
			/*for (TvShow s : shows) {
				String[] g = s.getStreamServices();
				for (String ge : g)
					System.out.print(ge + ", ");
				System.out.println();
				System.out.println(s.getTitle());
			}*/
		}
		return shows;
	}

	//comparator class for sorting by probability
	public static class probSort implements Comparator<TvShow> {
		
		public int compare(TvShow tvShow1, TvShow tvShow2) {
			return (int)((tvShow2.getProbability() * 10000) - (tvShow1.getProbability() * 10000));
		}
	}
	
	//Function to eliminate tv shows the user does not have access to
	public static ArrayList<TvShow> findAvailibleShows(ArrayList<TvShow> ss, String[] streamServices) {
		
		ArrayList<TvShow> applicable = new ArrayList<TvShow>();
		//for each tv show
		for (TvShow t : ss) {
			boolean isApplicable = false;
			//get streams availible for this show
			String[] streams = t.getStreamServices();
			//see if any of the show's services match user's specified services
			for (int i = 0; i < streamServices.length; i++) {
				for (int j = 0; j < streams.length; j++) {
					if (streamServices[i].equals(streams[j]))
						isApplicable = true;
				}
			}
			if (isApplicable)
				applicable.add(t);
		}
		return applicable;
	}
	
	//Function to filter out non-family friendly shows if user wants
	public static ArrayList<TvShow> parentalControl(ArrayList<TvShow> sugg) {
		
		ArrayList<TvShow> applicable = new ArrayList<TvShow>();
		for (TvShow t : sugg) {
			if (t.getMinAge() <= 13)
				applicable.add(t);
		}
		return applicable;
	}
	
	//Function to calculate probabilities factoring their rotten tomato score, as well as the user's
	//trust in rotten tomatoes
	public static ArrayList<TvShow> calculateProbWithReviews(ArrayList<TvShow> sugg, int rTrust) {
		
		//use their trust on a scale of 1-10 as a scaling factor
		double trustFactor = (double)(rTrust) / 10.0;
		double denominator = 0.0;
		//calculate the denominator for the Bayesian formula
		for (TvShow t : sugg) {
			denominator += trustFactor * ((double)t.getRottenTomatoScore() / 100.0);
		}
		//calculate the probability for each show
		for(TvShow t : sugg) {
			double prob = bayesFormula((double)t.getRottenTomatoScore() / 100.0, denominator, trustFactor);
			t.setProbability(prob);
		}
		return sugg;
	}

	//comparator object for sorting shows by age
	public static class ageSort implements Comparator<TvShow> {
		
		public int compare(TvShow tvShow1, TvShow tvShow2) {
			return tvShow1.getYearProduced() - tvShow2.getYearProduced();
		}
	}
	
	//Function to calculate probabilities factoring their age, and whether the user wants older or newer
	//shows
	public static ArrayList<TvShow> calculateProbWithAge(ArrayList<TvShow> sugg, String oldNew) {
		
		//data structures needed to sort and compare shows
		PriorityQueue<TvShow> sorted;
		Comparator<TvShow> compare;
		ArrayList<TvShow> fin = new ArrayList<TvShow>();
		//reverse the comparator based on user preference
		if (oldNew.equals("N") || oldNew.equals("n")) {
			compare = new ageSort().reversed();
		}
		else {
			compare = new ageSort();
		}
		//sort shows by age
		sorted = new PriorityQueue<TvShow>(sugg.size(), compare);
		for (TvShow t : sugg)
			sorted.offer(t);
		int pqSize = sorted.size();
		ArrayList<tempObj> temp = new ArrayList<tempObj>();
		double factor = .25;
		//start the probability factor at .5, every time a newer show appears, 
		//cut by half
		for (int i = 0; i < pqSize; i++) {
			TvShow show = sorted.poll();
			//use an array of temporary objects to find the age factor for each show
			temp.add(new tempObj(show, factor));
			if (sorted.peek() != null) {
				if (sorted.peek().getYearProduced() != show.getYearProduced())
					factor = factor / 2.0;
			}
		}
		//calculate the denominator for Bayesian probabilities
		double denominator = 0.0;
		for (tempObj t : temp)
			denominator += t.getTempProb() * t.getShow().getProbability();
		//calculate new Bayesian probability for each show, based on previous probability and new 
		//one determined by age
		for (tempObj t : temp) {
			TvShow show = t.getShow();
			double hypo = show.getProbability();
			double evidence = t.getTempProb();
			double newProb = bayesFormula(evidence, denominator, hypo);
			show.setProbability(newProb);
			fin.add(show);
		}
		return fin;
	}
	
	//comparator object for sorting shows by runtime
	public static class lenSort implements Comparator<TvShow> {
			
		public int compare(TvShow tvShow1, TvShow tvShow2) {
			return tvShow1.getAverageEpisodeLength() - tvShow2.getAverageEpisodeLength();
		}
	}
	
	//calculate probabilities based on the length of episode the user specifies as their preference
	public static ArrayList<TvShow> calculateProbWithRuntime(ArrayList<TvShow> sugg, String longShort) {
		
		//data structures needed to sort and compare shows
		PriorityQueue<TvShow> sorted;
		Comparator<TvShow> compare;
		ArrayList<TvShow> fin = new ArrayList<TvShow>();
		//reverse the comparator based on user preference
		if (longShort.equals("L") || longShort.equals("l")) {
			compare = new lenSort().reversed();
		}
		else {
			compare = new lenSort();
		}
		//sort shows by runtime
		sorted = new PriorityQueue<TvShow>(sugg.size(), compare);
		for (TvShow t : sugg)
			sorted.offer(t);
		int pqSize = sorted.size();
		ArrayList<tempObj> temp = new ArrayList<tempObj>();
		double factor = .5;
		//start the probability factor at .5, cut by half each time
		for (int i = 0; i < pqSize; i++) {
			TvShow show = sorted.poll();
			//use an array of temporary objects to find the length factor for each show
			temp.add(new tempObj(show, factor));
			if (sorted.peek() != null) {
				if (sorted.peek().getAverageEpisodeLength() != show.getAverageEpisodeLength())
					factor = factor / 2.0;
			}
			//System.out.println(factor);
		}
		//calculate the denominator for Bayesian probabilities
		double denominator = 0.0;
		for (tempObj t : temp)
			denominator += t.getTempProb() * t.getShow().getProbability();
		//calculate new Bayesian probability for each show, based on previous probability and new 
		//one determined by age
		for (tempObj t : temp) {
			TvShow show = t.getShow();
			double hypo = show.getProbability();
			double evidence = t.getTempProb();
			double newProb = bayesFormula(evidence, denominator, hypo);
			show.setProbability(newProb);
			fin.add(show);
		}
		return fin;
	}
	
	//comparator object for sorting shows by number of seasons
	public static class seasonSort implements Comparator<TvShow> {
		
		public int compare(TvShow tvShow1, TvShow tvShow2) {
			return tvShow1.getNumSeasons() - tvShow2.getNumSeasons();
		}
	}
	
	//calculate probabilities based on number of seasons that user prefers
	public static ArrayList<TvShow> calculateProbWithSeasons(ArrayList<TvShow> sugg, String manyFew) {
		
		//data structures needed to sort and compare shows
		PriorityQueue<TvShow> sorted;
		Comparator<TvShow> compare;
		ArrayList<TvShow> fin = new ArrayList<TvShow>();
		//reverse the comparator based on user preference
		if (manyFew.equals("M") || manyFew.equals("m")) {
			compare = new seasonSort().reversed();
		}
		else {
			compare = new seasonSort();
		}
		//sort shows by number of seasons
		sorted = new PriorityQueue<TvShow>(sugg.size(), compare);
		for (TvShow t : sugg)
			sorted.offer(t);
		int pqSize = sorted.size();
		ArrayList<tempObj> temp = new ArrayList<tempObj>();
		double factor = .125;
		//start the probability factor at .5, cut by half each time
		for (int i = 0; i < pqSize; i++) {
			TvShow show = sorted.poll();
			//use an array of temporary objects to find the season factor for each show
			temp.add(new tempObj(show, factor));
			if (sorted.peek() != null) {
				if (sorted.peek().getNumSeasons() != show.getNumSeasons())
					factor = factor / 2.0;
			}
		}
		//calculate the denominator for Bayesian probabilities
		double denominator = 0.0;
		for (tempObj t : temp)
			denominator += t.getTempProb() * t.getShow().getProbability();
		//calculate new Bayesian probability for each show, based on previous probability and new 
		//one determined by number of seasons
		for (tempObj t : temp) {
			TvShow show = t.getShow();
			double hypo = show.getProbability();
			double evidence = t.getTempProb();
			double newProb = bayesFormula(evidence, denominator, hypo);
			show.setProbability(newProb);
			fin.add(show);
		}
		return fin;
	}

	//calculate probabilities based on whether the show is ongoing or not
	public static ArrayList<TvShow> calculateProbWithOverOngoing(ArrayList<TvShow> sugg, String overOngoing) {
		
		//initialize data structures
		ArrayList<tempObj> temp = new ArrayList<tempObj>();
		ArrayList<TvShow> fin = new ArrayList<TvShow>();
		boolean isOver = false;
		if (overOngoing.equals("Over") || overOngoing.equals("over"))
			isOver = true;
		//factor probabilities based on user input
		for (TvShow t : sugg) {
			//relatively important factor on final decision, make disparity a factor of 4
			if (isOver) {
				if (!t.getIsStillAiring())
					temp.add(new tempObj(t, .75 * t.getProbability()));
				else
					temp.add(new tempObj(t, .125 * t.getProbability()));
			}
			else {
				if (t.getIsStillAiring())
					temp.add(new tempObj(t, .75 * t.getProbability()));
				else
					temp.add(new tempObj(t, .125 * t.getProbability()));
			}
		}
		double denominator = 0.0;
		for (tempObj t : temp)
			denominator += t.getTempProb() * t.getShow().getProbability();
		//set new probabilities for each show with Bayes' formula
		for (tempObj t : temp) {
			double evidence = t.getTempProb();
			TvShow show = t.getShow();
			double hypo = show.getProbability();
			double newProb = bayesFormula(evidence, denominator, hypo);
			show.setProbability(newProb);
			fin.add(show);
		}
		return fin;
	}

	//calculate probabilities based on genres user prefers
	public static ArrayList<TvShow> calculateProbWithGenres(ArrayList<TvShow> sugg, String[] genres) {
		
		//initialize data structures
		ArrayList<tempObj> temp = new ArrayList<tempObj>();
		ArrayList<TvShow> fin = new ArrayList<TvShow>();
		for (TvShow t : sugg) {
			double newProb = .1;
			//each time a user input genre matches a show's genre, increment probability by .33
			for (int i = 0; i < t.getGenres().length; i++) {
				for (int j = 0; j < genres.length; j++) {
					if (genres[j].equals(t.getGenres()[i])) {
						newProb += .33;
					}
				}
			}
			//create temporary object to hold show with its factor
			temp.add(new tempObj(t, newProb));
		}
		//calculate denominator for Bayes' Formula
		double denominator = 0.0;
		for (tempObj t : temp) 
			denominator += t.getTempProb() * t.getShow().getProbability();
		//use Bayes' formula to calculate new probability for each show
		for (tempObj t : temp) {
			TvShow show = t.getShow();
			double evidence = t.getTempProb();
			double hypo = show.getProbability();
			double newProb = bayesFormula(evidence, denominator, hypo);
			show.setProbability(newProb);
			fin.add(show);
		}
		return fin;
	}
}