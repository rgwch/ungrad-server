//noinspection UnterminatedStatementJS,UnterminatedStatementJS,UnterminatedStatementJS
/**
 * A verticle to handle requests for medical articles.
 *  Copyright (c) 2016 by G. Weirich
 */


'use strict'
var name = "medical_articles.js"

var eb = vertx.eventBus()
var server = require('./params.js')
var base_addr = "ch.elexis.ungrad.articles."
var model = require('./model')

exports.vertxStop=function(){
  console.log("Articles stopped")
}
/*
 * Internal method: Register a REST endpoint for an eventBus address.
 * @param rest REST endpoint to use (will always be preixed with "1.0/articles/"
 * @param ebaddr EventBus Address to register for that REST
 * @param method get or post
 * @param role role needed to access this service
 * @param handler handler for the service
 */
var register = function (rest, ebaddr, method, role, handler) {
  var msg = {
    "rest": "1.0/articles/" + rest,
    "ebaddress": base_addr + ebaddr,
    "method": method
  }
  if (typeof role === 'string') {
    msg['role'] = role
  }
  if (typeof server === 'object') {
    msg['server'] = server.desc
  }

  eb.send("ch.elexis.ungrad.server.register", msg
    , function (ar, ar_err) {
      if (ar_err == null) {
        console.log(name + "received " + JSON.stringify(ar.body()))
        handler(ar.body())
      } else {
        console.log(name + ar_err)
        handler({})
      }
    })
}

/**
 * Landing point for the admin API
 */
eb.consumer(base_addr + "admin", function (message) {
  console.log(name + "got admin " + message.body().command)
  switch (message.body().command) {
    case "getParam":
      message.reply({"status": "ok", "result": "0"})
      break;
    case "setParam":
      message.reply({"status": "ok"})
      break;
    case "exec":
      break;
    default:
      message.reply({"status": "error", "message": "unknown command " + message.body.command})
  }
})


/**
 * Get the entry for an ATC code.
 * Example: http://localhost:2016/api/1.0/articles/getATC/N02CC04
 */
register("getATC/:code", "getATC", "get", "user", function (result) {
  if (result.status === 'ok') {
    eb.consumer(base_addr + "getATC", function (message) {
      var body = message.body()
      var code = body['code']
      model.getATC(code, function (result) {
        message.reply({"status": "ok", "result": result})
      })
    })
  }
})

/**
 * get all BAG Entries for an ATC code.
 * Example: http://localhost:2016/api/1.0/articles/getBAG/N02CC04
 */
register("getBAG/:code", "getBAG", "get", "user", function (result) {
  if (result.status === "ok") {
    eb.consumer(base_addr + "getBAG", function (message) {
      var body = message.body()
      var code = body['code']
      model.getBAG_from_atc(code, function (result) {
        message.reply({"status": "ok", "result": result})
      })
    })
  }
})

/**
 * Get all Swissmedic entries where the name or the substances match a given pattern
 * Example: http://localhost:2016/api/1.0/articles/getSwissmedic/diclofenac
 */
register("getSwissmedic/:pattern", "getSM", "get", "user", function (result) {
  if (result.status == "ok") {
    eb.consumer(base_addr+"getSM",function(message) {
      var body = message.body()
      var pattern = body.pattern
      model.getSwissmedic(pattern, function (result) {
        message.reply({"status": "ok", "result": result})
      })
    })
  }
})


