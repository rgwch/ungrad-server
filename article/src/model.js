/**
 *  Copyright (c) 2016 by G. Weirich
 */
'use strict'

    var fs = vertx.fileSystem
    var config = vertx.getOrCreateContext().config()

    var atc = null
    var loadATC=function (callback) {
        if(atc == null) {
            var fname = config["epha"]
            if (typeof fname == String) {
                fs.readFile(fname,function(result,err){
                    if(err==null){
                        atc=JSON.parse(result)
                        callback(atc)
                    }else{
                        console.log("Error reading atc:"+ err)
                        callback(null)
                    }
                })
            }else{
                console.log("bad or missing argument for epha in config")
                callback(null)
            }
        }else{
            callback(atc)
        }
    }
    module.exports= {
        getATC: function(code, callback){
            loadATC(function(codes){
                callback(codes[code])
            })
        }
    }
