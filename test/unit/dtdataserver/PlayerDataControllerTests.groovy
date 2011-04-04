package dtdataserver

import grails.test.*
import grails.converters.*
import org.ccil.cowan.tagsoup.*;

class PlayerDataControllerTests extends ControllerUnitTestCase {
	
	def pdc
	def slurper
	
    protected void setUp() {
		pdc = new PlayerDataController()
		
		def tagsoupParser = new Parser()
		slurper = new XmlSlurper(tagsoupParser)
		http://localhost:9090/DTDataServer/rest/playerName/Steele
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testGetPlayerByNameFromWS() {
		
		def jsonPlayer = slurper.parse("http://localhost:9090/DTDataServer/rest/playerName/Steele")
		
		def playerArray = JSON.parse(jsonPlayer)
		
		playerArray.each { println "Value: ${it}" }					
    }
}
