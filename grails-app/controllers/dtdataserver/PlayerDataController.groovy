package dtdataserver

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;	   

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import groovy.util.slurpersupport.GPathResult;
import groovy.xml.StreamingMarkupBuilder;

import sun.font.LayoutPathImpl.SegmentPath.Mapper;

import grails.converters.*;

import org.ccil.cowan.tagsoup.*;

class PlayerDataController {
	
	def index = { 
		render "index"
	}
	
	def updateFootyWire = {
		
		def tagsoupParser = new Parser()
		def slurper = new XmlSlurper(tagsoupParser)
		
		render "Workin - grabbing from Footywire.\n"
		
		(1..22).each { rnd ->				
			
			println "doing: " + rnd
		
			Position.values().each { position ->
				
				if (Score.findByRound(rnd) == null)
				{
				
					println "position: " + position
					
					def htmlParser = slurper.parse("http://www.footywire.com/afl/footy/dream_team_round?round=${rnd}&p=${position}")
				  
					def rows = htmlParser.body.div.table.tr.td.table.tr.td.form.table.tr.td.table.tr.td.table.tr.collect {it}
					
					println "found " + rows.size() + " rows"
					
					rows.each { 
						def playerName = it.td[1].text().indexOf("(") > 0 ? it.td[1].text().substring(0, it.td[1].text().indexOf("(") - 2) : it.td[1].text()
						
						if (!playerName.startsWith("Player") && !playerName.startsWith("Dream"))
						{
							//render "Lookin for player: ${playerName}\n"
							
							def p = Player.findByNameFootyWire(playerName)
			
							if (p == null)
							{
								p = new Player(scores: [], positions: [position], nameFootyWire: playerName, team: it.td[2].text(), price: it.td[3].text().replace('$', '').replace(',', ''))					
								
								savePlayer(p)											
							}
							else
							{
								// found him so add the new position to his list of positions							
								p.positions << position
							}
							
							def thisScore = p.scores.find { s -> s.year == '2010' && s.round == rnd }
							
							if (thisScore == null)
							{
								def sc = it.td[4].text()
								
								def newScore = new Score(player: p, year: '2010', round: rnd, score: sc) 
								
								if (!newScore.validate())
								{
									newScore.errors.allErrors.each {
										render "error: " + it + "\n"
									}
								}
								
								p.scores << newScore
								p.save()
							}			
							
							//render "Here he is: ${p.id}: ${p.nameFootyWire}\n"					
						}
					}					
				}										
				else
				{
					println "Dun him already";
				}
				
			}
			
		
		}
		
		response.status = 200
		render "All done\n"

	}
	
	def nameMap = [
	    'Matt' : ['Matthew'],
	    'Ricky' : ['Rick'],
		'Chris' : ['Christopher'],
		'Heritier' : ['Harry'],
		'Nic' : ['Nicholas'],
		'Nick' : ['Nicholas'],
		'Sam' : ['Samuel'],
		'Ben' : ['Benjamin'],
		'Brad' : ['Bradley'],
		'Lachie' : ['Lachlan'],
		'James' : ['Jamie'],
		'Zac' : ['Zachary'],
		'Robbie' : ['Robert'],
	
		'de Boer' : ['DeBoer']
	]
		
	def posMap = [
	    'MID' : Position.MI,
	    'DEF' : Position.DE,
	    'FWD' : Position.FO,
	    'RUC' : Position.RU,
	    '[C]' : Position.MI,
	    '[B]' : Position.DE,
	    '[F]' : Position.FO,
	    '[R]' : Position.RU,
		'1' : Position.FO,
		'2' : Position.DE,
		'3' : Position.MI,
	    '4' : Position.RU	    
	]
	
	def updateDreamTeam = {
		render "Workin - grabbing from Dreamteam direct.\n"
		
		WebDriver driver = new FirefoxDriver();
		driver.get("http://dreamteam.afl.com.au")
		WebElement username = driver.findElementByXPath("//body[@id='VS_index']/div[2]/div[2]/div[1]/div[1]/div[1]/div[8]/div/form/table/tbody/tr[1]/td[2]/a/input")
		username.sendKeys("bwobbones@gmail.com")

		WebElement password = driver.findElementByXPath("//html/body/div[2]/div[2]/div/div/div/div[8]/div/form/table/tbody/tr[2]/td[2]/a/input")
		password.sendKeys("nirvana")

		WebElement playButton = driver.findElementByXPath("//html/body/div[2]/div[2]/div/div/div/div[8]/div/form/table/tbody/tr/td[3]/a/input")
		playButton.click()

		WebElement tradeButton = driver.findElementByXPath("//html/body/div[2]/div[2]/div[7]/div/div/div/div/div/div/table/tbody/tr[2]/td[2]/div[3]/div[4]/a[2]")
		tradeButton.click()
		
		def page = driver.getPageSource()
		String[] lines = page.split("\n")

		def playerJSON 
		lines.each { if (it.contains("var players_object")) { playerJSON = it }}
		
		playerJSON = playerJSON.minus("var players_object = ")
		playerJSON = playerJSON.minus(";");
		
		JsonFactory factory = new JsonFactory()
		ObjectMapper mapper = new ObjectMapper(factory) 
		HashMap<String,Object> o = mapper.readValue(playerJSON, HashMap.class); 
		ArrayList<HashMap<String,Object>> players = o.players
		
		def found = 0
		def notfound = 0
		def foundnonclean = 0
		
		players.each { player -> 								
			def p = Player.findByNameFootyWire(player.first_name + " " + player.last_name)
			if (p == null)
			{
				nameMap[player.first_name].each { println "Looking now for: " + it + " " + player.last_name; p = Player.findByNameFootyWire(it + " " + player.last_name) }
				nameMap[player.last_name].each { println "Looking now for: " + player.first_name + " " + it; p = Player.findByNameFootyWire(player.first_name + " " + it) }
				if (p == null)
				{
					notfound++;
					println "didn't get him: " + player.first_name + " " + player.last_name + " team: " + player.team + "\n"
				
					p = new Player(scores: [], positions: [gatherPos(player)], nameFootyWire: player.first_name + " " + player.last_name, 
							team: player.team, price: player.price)
					def s = new Score(player: p, year: '2010', round: 22, score: 20.0)
					p.getScores().add(s)
					savePlayer(p)								
				}
				else
				{
					foundnonclean++;
					updatePlayer(p, player)
				}
			}
			else
			{
				found++;
				updatePlayer(p, player)
			}
		}

		render "found: " + found + "\n"
		render "found non clean: " + foundnonclean + "\n"
		render "not found: " + notfound + "\n"		
		
		driver.close();
	}
	
	def updateCurrentTeam = {
		render "Workin - updating current team.\n"
		def tagsoupParser = new Parser()
		def slurper = new XmlSlurper(tagsoupParser)
		
		// reset all players
//		Player.findByComp("FT").each() {			
//			it.inTeam = false
//			it.captain = false
//			savePlayer(it)
//		}
		
		// go through and update all current players
		WebDriver driver = new FirefoxDriver();
		loginFT(driver)
		driver.get("http://www.footytips.com.au/fantasy/afl/manageteam/?teamid=2653")
		def page = driver.getPageSource()
		String[] lines = page.split(";")
		def teamJSON 
		lines.each { if (it.contains("var currentTeam")) { teamJSON = it }}		
		teamJSON = teamJSON.trim().minus("var currentTeam = ")

		render teamJSON + "\n"
		
		JsonFactory factory = new JsonFactory()		
		ObjectMapper mapper = new ObjectMapper(factory)
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true); 
		HashMap<String,Object> o = mapper.readValue(teamJSON, HashMap.class); 
		ArrayList<HashMap<String,Object>> positions = o.position
		
		positions.each { pos -> 
			ArrayList<HashMap<String,Object>> players = pos.players
			players.each { player -> 
				def p = Player.findByNameFootyWire(player.name);
				if (p == null)
				{
					render "name: " + player.name + " couldn't find him\n";
					
					p = new Player(scores: [], positions: [posMap[pos]], nameFootyWire: name, 
							team: team, price: price, playing: playing, comp: "FT", inTeam: false, captain: false)
					def s = new Score(player: p, year: '2011', round: 1, score: score)
					p.getScores().add(s)
					savePlayer(p)
					
				}
				else
				{
					render "found: " + player.name
				}
			}
			
		}
		
		response.status = 200
		
	}
	
	def loginFT = {	driver ->	
		driver.get("http://www.footytips.com.au/competitions")
		
		WebElement username = driver.findElementByXPath("//html/body/div[5]/table/tbody/tr[2]/td/div/table/tbody/tr/td/table/tbody/tr/td[2]/div/form/input")
		username.sendKeys("bwobbones")
		
		WebElement password = driver.findElementByXPath("//html/body/div[5]/table/tbody/tr[2]/td/div/table/tbody/tr/td/table/tbody/tr/td[2]/div/form/input[2]")
		password.sendKeys("nirvana")
		
		WebElement loginButton = driver.findElementByXPath("//html/body/div[5]/table/tbody/tr[2]/td/div/table/tbody/tr/td/table/tbody/tr/td[2]/div/form/input[3]")
		loginButton.click()
	}
	
	def updateFootyTips = {
		render "Workin - grabbing from FootyTips.\n"
		
		WebDriver driver = new FirefoxDriver();
		loginFT(driver)

		driver.get("http://www.footytips.com.au/fantasy/afl/manageteam/?teamid=2653&bnav1=trade")

		def tagsoupParser = new Parser()
		def slurper = new XmlSlurper(tagsoupParser)

		def prevCount = 1
		(1..46).each  { i ->
			driver.get("http://www.footytips.com.au/fantasy/afl/manageteam/?teamid=2653&page=" + i + "&psr=" + prevCount + "&ddstat=pointsHighest&ddprice=0&ddposition=p_0&ddclub=0&ddfilter=1")
			prevCount += 17;					
			
			def htmlParser = slurper.parseText(driver.getPageSource())	

			
			(0..1).each { 
				
				def cls = "ftdatatable" + it + "row"
				
				def rows = htmlParser.body.div.table.tbody.tr.td.table.tbody.tr.td.div.table.tbody.tr.td.div.div.div.table.tbody.tr.findAll {it.@class == cls} 
						
				rows.each {
					def tds = it.td.collect {it}
					def pos
					def name
					def price
					def score
					def team
					def playing
					tds.each {
						
						if (it.text().trim().startsWith("[")) 
						{
							pos = it.text().trim()
						} else 
						if (it.div.a.@name =~ 'FT_JS_LINK')
						{
							name = it.div.a.text().trim() 
						} else 					
						if (it.img.@title == 'This player value')
						{
							price = it.text().trim() 
						} else
						if (it.text().trim().contains("avg"))				
						{
							score = it.text().trim().substring(0, it.text().trim().indexOf("a"))
							
							def p = new Player(scores: [], positions: [posMap[pos]], nameFootyWire: name, 
									team: team, price: price, playing: playing, comp: "FT", inTeam: false, captain: false)
							def s = new Score(player: p, year: '2011', round: 1, score: score)
							p.getScores().add(s)
							savePlayer(p)
							
							render "saved: " + p.nameFootyWire + ": " + score + "\n"
							
						}
						
						if (!it.div.img.@src.text().isEmpty()) 
						{										
							def str = it.div.img.@src.text().trim()
							
							team = str.substring(str.indexOf("AFL") + 4, str.indexOf("AFL") + 7) 
							playing = (str.charAt(str.indexOf("AFL") + 7) == ".") 				
						}
					}
				}
				
				render "\ndone: " + rows.size() + "\n"
			}
							
		}
						
		driver.close()	
		
		response.status = 200			
		render "OK"
	}
	
	public static String createString(GPathResult root) { 
		return new StreamingMarkupBuilder().bind { out << root } 
	} 
	
	def updatePlayer = { p, player ->
		
		p.positions = gatherPos(player)
		p.team = player.team
		p.price = player.price as Double
		
		println "updating: " + p.nameFootyWire + " team: " + p.team + " price: " + p.price + " positions: " + p.positions
		
		if (!p.validate())
		{
			p.errors.allErrors.each {
				render "error: " + it + "\n"
			}
		}				
		
		p.save()
	}
	
	def savePlayer = { p -> 
		if (!p.validate())
		{
			p.errors.allErrors.each {
				render "error: " + it + "\n"
			}
		}
		
		p.save()	
	}
	
	def gatherPos = { player ->
		def pos = [] as SortedSet
		pos << posMap[player.pos1]
		if (player.pos2 != null)
		{
			pos << posMap[player.pos2]
		}	
		return pos
	}
	
	def position = {       
		if (params.pos) {
			
			def playersMap = ['players':[[:]]]
			
			println "pos: " + Enum.valueOf(Position.class, params.pos)
			
			def results = Player.executeQuery(
					'select p from Player p left join p.positions pp where pp=:position',
					[position: Enum.valueOf(Position.class, params.pos)])
						

			results.each {
				
				def pMap = [:]
				pMap.id = it.id
				pMap.nameFootyWire = it.nameFootyWire
				pMap.price = it.price 
				pMap.team = it.team
				pMap.average = it.getYearAverage(2010)
				pMap.trend = it.getTrend() 
				playersMap.players << pMap
			}

			if (results)
			{
				response.status = 200			
				render playersMap as JSON
			}
		}	
	}
	
	def playerName = {       
		if (params.playerName) {
			def results = Player.findByNameFootyWireLike("%${params.playerName}%")

			if (results)
			{
				response.status = 200
				render results as JSON
			}
		}	
	}
	
	def playerId = {       
		if (params.playerId) {
			def results = Player.get(params.playerId)
			
			if (results)
			{
				response.status = 200
				render results as JSON
			}
		}	
	}
	
}
