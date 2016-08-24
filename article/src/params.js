module.exports = {
  params:{
    status:"ok",
    result:[
      {
        "name":"source",
        "caption":"Data source",
        "type":"String",
        "value":"/Users/gerry/cli-robot",
        "writeable":false
      },
      {
        "name":"has_atc",
        "caption":"ATC loaded",
        "type":"boolean",
        "value":true,
        writable:false
      },
      {
        "name":"load:atc",
        "caption":"reload ATC",
        "type":"action",
        "value":"off",
        "writable":true
      }
    ]
  }
}
