package dtdataserver

import grails.test.*

class PlayerDataTestTests extends GrailsUnitTestCase {
	
	def pdc
	
    protected void setUp() {
        super.setUp()
		pdc = new PlayerDataController()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testDataInDB() {
		
		
//		pdc.updateData
//		
//		def p = Player.findByNameFootyWire("Brent Stanton")
//		
//		assertNotNull(p);
//		
//		assertEquals p.getTeam(), "Essendon"
    }
}
