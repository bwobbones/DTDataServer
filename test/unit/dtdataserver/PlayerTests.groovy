package dtdataserver

import grails.test.*

class PlayerTests extends GrailsUnitTestCase {
	
    protected void setUp() {
        super.setUp()			
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testAverage() {
		def s1 = [
				new Score(year:2010, round:1, score:20.0),
				new Score(year:2010, round:2, score:20.0),
				new Score(year:2010, round:3, score:30.0),
				new Score(year:2010, round:4, score:40.0),
				new Score(year:2010, round:5, score:40.0)
				] as SortedSet
		
		def p = new Player(nameFootyWire:'Greg', scores:s1)
		
		assert p.getYearAverage(2010) == 30.0
    }
	
	void testAverageSpecificYear() {
		def s1 = [
		new Score(year:2010, round:1, score:20.0),
		new Score(year:2010, round:2, score:20.0),
		new Score(year:2010, round:3, score:30.0),
		new Score(year:2010, round:4, score:40.0),
		new Score(year:2010, round:5, score:40.0),
		new Score(year:2011, round:1, score:400.0),
		new Score(year:2011, round:2, score:400.0)
		] as SortedSet
		
		def p = new Player(nameFootyWire:'Greg', scores:s1)
		
		assert p.getYearAverage(2010) == 30.0
	}
	
	void testTrend()
	{
		def s1 = [
				new Score(year:2010, round:1, score:20.0),
				new Score(year:2010, round:2, score:20.0),
				new Score(year:2010, round:3, score:30.0),
				new Score(year:2010, round:4, score:40.0),
				new Score(year:2010, round:5, score:40.0)				
				] as SortedSet
		
		def p = new Player(nameFootyWire:'Greg', scores:s1)
		
		println "got: " + p.getTrend() + " needed 1.2"
		
		assert p.getTrend().equals(1.2 as double)
	}
	
	void testAcrossYearTrend()
	{
		def s1 = [
				new Score(year:2010, round:1, score:20.0),
				new Score(year:2010, round:2, score:20.0),
				new Score(year:2010, round:3, score:30.0),
				new Score(year:2010, round:4, score:30.0),
				new Score(year:2010, round:5, score:35.0),
				new Score(year:2011, round:1, score:40.0),
				new Score(year:2011, round:2, score:45.0)				
				] as SortedSet
		
		def p = new Player(nameFootyWire:'Greg', scores:s1)	
		
		assert p.getTrend().equals(1.3 as double)
	}
	
	void testSkipRoundTrend()
	{
		def s1 = [
			new Score(year:2010, round:1, score:20.0),
			new Score(year:2010, round:2, score:25.0),
			new Score(year:2010, round:4, score:30.0),
			new Score(year:2010, round:5, score:35.0),
			new Score(year:2010, round:6, score:40.0)			
		] as SortedSet
		
		def p = new Player(nameFootyWire:'Greg', scores:s1)	
		
		assert p.getTrend().equals(1.4 as double)
	}
	
	void testNegativeTrend()
	{
		def s1 = [
			new Score(year:2010, round:1, score:40.0),
			new Score(year:2010, round:2, score:35.0),
			new Score(year:2010, round:4, score:30.0),
			new Score(year:2010, round:5, score:25.0),
			new Score(year:2010, round:6, score:20.0)			
		] as SortedSet
		
		def p = new Player(nameFootyWire:'Greg', scores:s1)	
		
		assert p.getTrend().equals(0.6 as double)
	}
	
	
}
