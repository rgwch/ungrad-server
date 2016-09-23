# Article manager

## Summary

This is an example of an ungrad server module written in JavaScript. It maintaints a repository of swiss medical articla data:

* swissmedic - collection of all medical articles registered in Switzerland
* bag - collection of all medical articles payable by the basic health insurance (Spezialitätenliste BAG der durch die Grundversicherung übernommenen Artikel)
* atc - indication based collection of articles [ATC-Code](https://de.wikipedia.org/wiki/Anatomisch-Therapeutisch-Chemisches_Klassifikationssystem)
* kompendium - Official textual informations of all medical articles found in 'swissmedic' 

The source of the data are public available sites, but the retrieval and processing engine (cli-robot) was created by [epha](https://www.epha.ch/#/view=intro/state=default/)

## Preparation

This Verticle needs cli-robot, original copyright by epha. So first

    git clone https://github.com/rgwch/cli-robot.git
    cd cli-robot
    npm install
    mkdir data/public/swissmedic
    mkdir data/public/atc
    npm run all
    
Then enter the path to cli-robot as a configuration parameter for Article Manager 
to the ungrad launcher config, as in:
        
        // user.json
         "launch":[
                // .... other verticles ...
                 
                {
                    "name":"Articles",
                    "url":"file:./article/src/medical_articles.js",
                    "config":{
                        "epha":"/some/path/to/cli-robot"
                    }
                }
            ]

## Usage

This module is called via `/api/1.0/articles/xxx`

where xxx is one of:

* `getATC/<code>` - retrieve an ATC entry via its code. Example: `http://localhost:2016/api/1.0/articles/getATC/N02CC04`
* `getBAG/<code>` - retrive an Array of all "Spezialitätenliste"-Articles with a given ATC code. Example: `http://localhost:2016/api/1.0/articles/getBAG/N02CC04`
* `getSwissmedic/<pattern>` - retrieve an Array of all "Swissmedic" Articles, where the name or one of the substances matches 'pattern'. Example: `http://localhost:2016/api/1.0/articles/getSwissmedic/diclofenac` 

All methods return a JsonObject with an entry "status". If status is "ok", there will be an Entry "result" with the result. If the status is "error", there will be an 
entry "message" with an explaining text.
