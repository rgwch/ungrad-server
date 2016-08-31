/**
 *  Copyright (c) 2016 by G. Weirich
 */
'use strict'

var fs = vertx.fileSystem()
var config = vertx.getOrCreateContext().config()
var basedir = config["epha"]
var atc = null
var bag = []
var swissmedic=[]

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
  }else{
    callback(bag)
  }
}
var loadSwissmedic=function(callback){
  if(swissmedic.length==0){
    checkFile("swissmedic/swissmedic.min.json", function(result,err){
      if(err==null){
        swissmedic=JSON.parse(result)
        callback(swissmedic)
      }else{
        console.log("error reading swissmedic: "+err)
        callback(null)
      }
    })
  }else{
    callback(swissmedic)
  }
}
module.exports = {
  getATC: function (code, callback) {
    loadATC(function (codes) {
      if (codes == null) {
        callback({})
      } else {
        callback(codes[code])
      }
    })
  },
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
  getSwissmedic: function(pattern,callback){
    var regexp=new RegExp(pattern,"i")
    loadSwissmedic(function(codes){
      if(codes!=null){
        var result= codes.filter(function(item){
          return(regexp.test(item.name))
        })
        callback(result)
      }else{
        callback([])
      }

    })
  }
}
