package dtdataserver

class Player {
	
	String nameFootyWire
	String team 
	double price
	SortedSet scores
	SortedSet positions
	Boolean playing
	String comp
	Boolean inTeam
	Boolean captain
    static hasMany = [ scores : Score,  positions : Position ]		
	
	def getYearAverage(year)
 	{	
		def yearScore = scores.findAll { 
			it.year == year
		}
		
		return yearScore.collect { it.score }.sum() / yearScore.size()
	}	
	 
	def getTrend()
	{
		if (goodPlayerList.contains(nameFootyWire) && price < 100000.0)
		{
			return 5.0;
		}
		
		double trend = 1.0
		double last = getLastFiveScores()[0].score
		getLastFiveScores().each {
			if (it.score < last)
			{
				trend -= 0.1;				
			}
			else if (it.score > last)
			{
				trend += 0.1;
			}
			last = it.score;
		}
		
		return trend.round(1)
	}
	
	def getLastFiveScores()
	{
		def max = scores.asList().size() > 4 ? 4 : scores.asList().size() - 1
		return scores.asList().reverse()[0..max].reverse()
	}
	
	def goodPlayerList = [
	    'Brodie Smith', 'Shaun McKernan', 'Claye Beams', 'Ed Curnow', 'Nicholas Duigan',
	    'Simon Buckley', 'Michael Hibberd', 'Dyson Heppell', 'Nick Lower', 'Viv Michie',
	    'Peter Faulks', 'Brandon Matera', 'Dion Prestia', 'Michael Coad', 'Charlie Dixon',
	    'Isaac Smith', 'Cameron Richardson', 'Robbie Tarrant', 'Cameron O’Shea', 'Byron Sumner',
	    'Jack Darling', 'Tom Liberatore', 'Harley Bennell', 'Josh Fraser', 'Sam Iles'
	];
}
