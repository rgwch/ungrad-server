## Article manager

### Summary

This is an example of an ungrad server module written in JavaScript

### Preparation

This Verticle needs cli-robot (c) by epha. So first

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

