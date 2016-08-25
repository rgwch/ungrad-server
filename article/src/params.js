module.exports = {
  desc:{
    "id":"ch.elexis.ungrad,articles",
    "name":"Medical Articles Switzerland",
    "address":"ch.elexis.ungrad.articles.admin",
    "params": [
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
