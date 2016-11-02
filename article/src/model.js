/**
 *  Copyright (c) 2016 by G. Weirich
 */
'use strict'

/*
 * This is a very simple data model for medical data. JSON files as created bei epha's cli-robot (http://github.com/epha/cli-robot)
 * are parsed, cached and searched. Of course, this will be a Database (e.g. mongo) in the future.
 * This module is called by a verticle (medical_articles.js) and uses the configuration supplied with the launch of that verticle.
 * The config should contain an "epha" entry with the base directory of cli-robot.
 */
var fs = vertx.fileSystem()
var config = vertx.getOrCreateContext().config()
var basedir = config["epha"]
var atc = null
var bag = []
var swissmedic = []

/*
 * Check and load a file asynchroneously from the data dir of cli-robot
 * @param name name of the file
 * @param handler the handler is called upon completion with two parameters. On success, the first parameter
 * is the contents of the file, the second null. On error, the first parameter will be null and the second an error message.
 */
var checkFile = function (name, handler) {
    if (typeof basedir == 'string') {
        var fname = basedir + "/data/release/" + name
        if (fs.existsBlocking(fname)) {
            fs.readFile(fname, handler)
        } else {
            handler(null, "bad filename or missing file:" + fname)
        }
    }
}
/*
 * Load ATC data into memory. The atc.min.json is parsed and cached in the variable "atc". If it was already parsed,
 * returns immediately the atc variable.
 * @param callback called upon completion with an "atc" object or null if there was an error
 */
var loadATC = function (callback) {
    if (atc == null) {
        checkFile("atc/atc.min.json", function (result, err) {
            if (err == null) {
                atc = JSON.parse(result)
                callback(atc)
            } else {
                console.log("Error reading atc:" + err)
                callback(null)
            }
        })
    } else {
        callback(atc)
    }
}
/*
 * Load BAG data into memory. The bag.min.json is parsed and cached in the variable "bag".If it was already parsed,
 * returns immediately the bag variable.
 * @param callback called upon completion with an "bag" array or null if there was an error
 */
var loadBAG = function (callback) {
    if (bag.length == 0) {
        checkFile("bag/bag.min.json", function (result, err) {
            if (err == null) {
                bag = JSON.parse(result)
                callback(bag)
            } else {
                console.log("error reading bag: " + err)
                callback(null)
            }
        })
    } else {
        callback(bag)
    }
}
/*
 * Load Swissmedic data into memory. The swissmedic.min.json is parsed and cached in the variable "swissmedic".If it was already parsed,
 * returns immediately the swissmedic variable.
 * @param callback called upon completion with a "swissmedic" array or null if there was an error
 */
var loadSwissmedic = function (callback) {
    if (swissmedic.length == 0) {
        checkFile("swissmedic/swissmedic.min.json", function (result, err) {
            if (err == null) {
                swissmedic = JSON.parse(result)
                callback(swissmedic)
            } else {
                console.log("error reading swissmedic: " + err)
                callback(null)
            }
        })
    } else {
        callback(swissmedic)
    }
}
/**
 * public interface
 */
module.exports = {
    /**
     * get an ATC entry by code
     * @param code ATC code (item or group code) to retrieve
     * @param callback contains the entry (which may be an empty object) on completion.
     */
    getATC: function (code, callback) {
        loadATC(function (codes) {
            if (codes == null) {
                callback({})
            } else {
                callback(codes[code])
            }
        })
    },
    /**
     * Find zero or more BAG ("Spezialitätenliste") entries by ATC code
     * @param code the ATC code
     * @param callback result contains an array (which may be empty) of all medical items matching the given ATC code.
     */
    getBAG_from_atc: function (code, callback) {
        loadBAG(function (codes) {
            if (codes != null) {
                var result = codes.filter(function (item) {
                    return (item.atc === code)
                })
                callback(result)
            } else {
                callback([])
            }
        })
    },
    /**
     * Find zero or more entries from the Swissmedic collection of medical articles matching a given expression
     * @param pattern a search expression (text or regexp) to match against the name and the substances of the article.
     * @param callback result contains an array (which may be empty) of all matching articles
     */
    getSwissmedic: function (pattern, callback) {
        var regexp = new RegExp(pattern, "i")
        loadSwissmedic(function (codes) {
            if (codes != null) {
                var result = codes.filter(function (item) {
                    return (regexp.test(item.name) || regexp.test(item['anwendungsgebiet']))
                })
                callback(result)
            } else {
                callback([])
            }

        })
    }
}
