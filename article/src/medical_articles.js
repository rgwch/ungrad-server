//noinspection UnterminatedStatementJS,UnterminatedStatementJS,UnterminatedStatementJS
/**
 *  Copyright (c) 2016 by G. Weirich
 */

'use strict'
var name="medical_articles.js "

var eb=vertx.eventBus()
var server=require('./params.js')

var register=function(rest,ebaddr,method,role,handler){
  var msg={}
  msg['rest']="1.0/articles/"+rest
  msg['ebaddress']="ch.elexis.ungrad.articles."+ebaddr
  msg['method']=method
  msg['role']=role
  msg['server']=server.desc
  eb.send("ch.elexis.ungrad.server.register",msg
  ,function(ar,ar_err){
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
    case "getParam":

        message.reply({"status":"ok","result":"0"})
      break;
    case "setParam":
        message.reply({"status:ok"})
      break;
    default:
      message.reply({"status":"error","message":"unknown command "+message.body.command})
  }
})


register("find/:pattern","find","get","user",function(result){
  if(result.status==="ok"){
    eb.consumer("ch.elexis.ungrad.articles.find",function(message){
      var body=message.body()

    })
  }
})

