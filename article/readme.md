## Article manager

### Summary

This is an example of an ungrad server module written in JavaScript. It maintaints a repository of swiss medical articla data:

* Swissmedic - collection of all medical articles registered in Switzerland
* bag - collection of all medical articles payable by the basic health insurance (Spezialitätenliste BAG der durch die Grundversicherung übernommenen Artikel)
* atc - indication based collection of articles [ATC-Code](https://de.wikipedia.org/wiki/Anatomisch-Therapeutisch-Chemisches_Klassifikationssystem)
* kompendium - Official textual informations of all medical articles found in 'siwssmedoc' 

The source of the data are public available sites, but the retrieval and processing engine (cli-robot) was created by [epha](https://www.epha.ch/#/view=intro/state=default/)

### Preparation

This Verticle needs cli-robot, original copyright by epha. So first

    git clone https://github.com/rgwch/cli-robot.git
    cd cli-robot
    npm install
    mkdir data/public/swissmedic
    mkdir data/public/atc
    
Then enter the path to cli-robot as a configuration parameter for Article Manager 
to the ungrad launcher config, as in:
        
        // user.json
         "launch":[
                // .... other verticles ...
                 
                {
                    "name":"Articles",
                    "url":"file:./article/src/medical_articles.js",
                    "config":{
                        "epha":"/Users/gerry/git/cli-robot"
                    }
                }
            ]

