## Dispatcher

### Core concept 

This is the central class of the Ungrad Server. It manages modules and dispatches REST calls accordingly. The system
is built upon the [Vert.x](http://vertx.io) ecosystem and uses its EventBus and Verticle concept.

Ungrad Server is a collection of Verticles. A Verticle is a module of functionality, very similar to a plugin.

On Startup, the Dispatcher runs configured verticles. Additional verticles (or modules) can be launched independently
and connect with the dispatcher.

To register, a verticle must:

 - Send a register message to "ch.elexis.ungrad.server.register" on the EventBus. The message content is a JsonObject
 with the following fields:
    - "rest" -> the rest address. For example: "1.0/someFunc", to register for calls to "https://myhost/api/1.0/someFunc".
     The address can contain variables, as in "1.0/someFunc/:foo/:bar"
    - "method" -> the request method to listen for (get or post)
    - "ebaddress" -> the event bus address this module will listen.
    
 - Listen to the EventBus-address provided when registering ("ebaddress"). The Dispatcher will catch requests to the registered
 address and convert them to EventBus messages, which then are dispatched to the client. The EventBus message content will
 be a JsonObject with the following fields:
        
    - In case of "get" requests: The variables supplied in the request address. Example: If `1.1/someFunc/:foo/:bar` was 
        registered, the a call to `https://myhost/api/1.1/someFunc/hula/hoop` will send a message to the registered adress
        with the following message body:
        
            {
               "foo": "hula",
               "bar": "hoop"
            }
            
    - In case of "post" requests: The request body as a JsonObject.
    
  
(You might want to check the submodule "tester", `ch.elexis.ungrad.server_test.SelfTest`for an example how to do this)
  
### Access permissions

If the register message contains an entry "role", the the framework will check if the current user has that role before 
proceeding. If not, it will redirect to a login page.

### Administrative interface
  
If the register message contains entries for "server id" and "server control", the framework will provide an administrative
  user interface for that Verticle. "server id" should be a unique identifier and "server control" should be an EventBus address
  the Verticle listens to.
  
  Eventually, the framework will send one of the following messages to that address:
  
* getName -> the answer should be a human readable name for this Plugin
* getParams -> the answer contains the parameters for the user interface to 
present for modifying and should be a JsonObject with a content similar to the following:
  
        {
            caption: {  
                type: <one of: boolean, string, number>
                default: <defaultvalue>
                range: <allowed range. For String parameters: regexp>
            },
            otherCaption{
            }
            ...
        }
       
* setParam({name: caption, value: newVal})
* start
* stop

It is the responsibility of the Verticle to perform Actions for each call. 
