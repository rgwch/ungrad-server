## Registering modules

For each REST address to register, send a message to "ch.elexis.ungrad.server.register", containing a JsonObject such as:

    {
        "rest":"/my/address/and/function/:param1/:param2",
        "method":"get", 
        "ebaddress":"event.bus.address.to.forward.requests",
        "role":"needed_role_for_this_request",
        "server":{
            "id":"unique.id.for.this.server",
            "name":"human readable name",
            "address":"event.bus.address.for.admin",
            "params":[
                {
                    "name":"name of the parameter",
                    "caption":"Caption for Web UI",
                    "type":"string",
                    "value":"some value",
                    "writable":true
                },
                ...
            ]
        }
    }
    
 The "server" component is needed at least once for each server. So, if a server
  registers more that 1 REST address, it needs only to include the "server" Object in one 
  of the calls. (But it is not an error to send the same server description more than once).
  If a server, identified by its ID, tries to register different server descriptions, there is no 
  guarantee, which one is used.
  
    