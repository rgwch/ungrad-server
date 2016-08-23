/**
 *  Copyright (c) 2016 by G. Weirich
 */

'use strict'
var name="medical_articles.js "

var eb=vertx.eventBus()

var register=function(rest,ebaddr,method,role,handler){
  eb.send("ch.elexis.ungrad.server.register",{
    "rest":"1.0/articles/"+rest,
    "ebaddress":"ch.elexis.ungrad.articles."+ebaddr,
    "method":method,
    "role":role,
    "server-id":"ch.elexis.ungrad.articles",
    "server-control":"ch.elexis.ungrad.articles.admin"
  },function(ar,ar_err){
    if(ar_err==null){
      console.log(name+"received "+JSON.stringify(ar.body()))
      handler(ar.body())
    }else{
      console.log(name+ar_err)
      handler({})
    }
  })
}

eb.consumer("ch.elexis.ungrad.articles.admin",function(message){
  console.log(name+"got admin")
  switch(message.body().command){
    case "getParams":
      var parm=require('./params').params
        message.reply(parm)
      break;
    case "getName":
      message.reply({"status":"ok","name":"Medical Articles Switzerland"})
      break;
    default:
      message.reply({"status":"error","message":"unknown command "+message.body.command})
  }
})

eb.send("ch.elexis.ungrad.server.register",{
  "rest":"1.0/articles/update",
  "ebaddress":"ch.elexis.ungrad.articles.update",
  "method":"get",
  "role":"admin",
  "server-id":"ch.elexis.ungrad.articles",
  "server-control":"ch.elexis.ungrad.articles.admin"
},function(ar,ar_err){
    if(ar_err==null){
      console.log(name+"received "+JSON.stringify(ar.body()))
    }else{
      console.log(name+ar_err)
    }
})

register("find/:pattern","find","get","user",function(result){
  if(result.status==="ok"){
    eb.consumer("ch.elexis.ungrad.articles.find",function(message){
      var body=message.body()

    })
  }
})

