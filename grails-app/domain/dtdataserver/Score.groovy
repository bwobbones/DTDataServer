package dtdataserver

class Score implements Comparable <Score> {

    static belongsTo = [ player : Player ]
	
	int year
	int round
	double score
	
	int compareTo(obj) {		
		if (year.compareTo(obj.year) != 0)
		{
			return year.compareTo(obj.year)
		}
		
		return round.compareTo(obj.round)
	}
}
