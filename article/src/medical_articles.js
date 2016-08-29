//noinspection UnterminatedStatementJS,UnterminatedStatementJS,UnterminatedStatementJS
/**
 *  Copyright (c) 2016 by G. Weirich
 */

'use strict'
var name="medical_articles.js "

var eb=vertx.eventBus()
var server=require('./params.js')
var base_addr="ch.elexis.ungrad.articles."
var atc_list=[]

var register=function(rest,ebaddr,method,role,handler){
  var msg={
    "rest":"1.0/articles/"+rest,
    "ebaddress":base_addr+ebaddr,
    "method":method
  }
  if(typeof role ==='string') {
    msg['role'] = role
  }
  if(typeof server === 'object') {
    msg['server'] = server.desc
  }

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

eb.consumer(base_addr+"admin",function(message){
  console.log(name+"got admin "+message.body().command)
  switch(message.body().command){
    case "getParam":

        message.reply({"status":"ok","result":"0"})
      break;
    case "setParam":
        message.reply({"status":"ok"})
      break;
    case "exec":
          val atfile=require('/home/gerry/git/cli-robot/data7release/atc.json')
          message.reply({"status":"ok","executed":message.body().action})
    default:
      message.reply({"status":"error","message":"unknown command "+message.body.command})
  }
})


/**
 * get the entry for an ATC code
 */
register("getATC/:code","getATC","get","user",function(result){
  if(result.status==='ok'){
    eb.consumer(base_addr+"getATC",function(message){
      var body=message.body()
      var code=body['code']
      var result=atc_list.filter(function(item){
        return (item['name']===code)
      })
       message.reply({"status":"ok","result":result})
    })
  }
})
register("find/:pattern","find","get","user",function(result){
  if(result.status==="ok"){
    eb.consumer("ch.elexis.ungrad.articles.find",function(message){
      var body=message.body()

    })
  }
})

