class UrlMappings {
    static mappings = {
      "/$controller/$action?/$id?"{
	      constraints {
			 // apply constraints here
		  }
	  }
	  "/rest/playerData/updateFW"(controller:"playerData",action:"updateFootyWire")
	  "/rest/playerData/updateDT"(controller:"playerData",action:"updateDreamTeam")
	  "/rest/playerData/updateFT"(controller:"playerData",action:"updateFootyTips")
	  "/rest/playerData/updateCurrent"(controller:"playerData",action:"updateCurrentTeam")
	  "/rest/position/$pos?"(controller:"playerData",action:"position")
	  "/rest/playerName/$playerName?"(controller:"playerData",action:"playerName")
	  "/rest/playerId/$playerId?"(controller:"playerData",action:"playerId")
	  "/rest/allPlayers/$year?/$round?"(controller:"playerData",action:"allPlayers")
      "/"(view:"/index")
	  "500"(view:'/error')
	}
}
