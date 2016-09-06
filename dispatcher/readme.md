## Dispatcher

### Core concept 

This is the main module of the Ungrad Server. It manages other modules and dispatches REST calls accordingly. The system
is built upon the [Vert.x](http://vertx.io) ecosystem and uses its EventBus and Verticle concept.

Ungrad Server is ultimately a collection of independently running Verticles. A Verticle is a module of functionality, similar
 to a plugin, but with less dependencies.

On Startup, the Dispatcher runs configured verticles. Additional verticles (or modules) can be launched individually
and connect with the dispatcher. A Verticle usually exposes a REST API for its work, and an EventBus Address for administrative
access.

To register, a verticle must:

 - Send a one or more register messages to "ch.elexis.ungrad.server.register" on the EventBus. The message content is a JsonObject
 with the following fields:
    - "rest" -> the rest address. For example: "1.0/someFunc", to register for calls to "https://myhost/api/1.0/someFunc".
     The address can contain variables, as in "1.0/someFunc/:foo/:bar"
    - "method" -> the request method to listen for (get or post)
    - "ebaddress" -> the event bus address this module will listen.
    - "server" -> A JsonObject describing the Service and its administrative interface.
    
 - Listen to the EventBus-address provided when registering ("ebaddress"). The Dispatcher will catch requests to the registered
 REST address and convert them to EventBus messages, which then are dispatched to the client. The EventBus message content will
 be a JsonObject with the following fields:
        
    - In case of "get" requests: The variables supplied in the request address. Example: If `1.1/someFunc/:foo/:bar` was 
        registered, the a call to `https://myhost/api/1.1/someFunc/hula/hoop` will send a message to the registered address
        with the following message body:
        
            {
               "foo": "hula",
               "bar": "hoop"
            }
            
    - In case of "post" requests: The request body as a JsonObject.
    
  
(You might want to check the submodule "Server Info", `ch.elexis.ungrad.server_test.SelfTest`for an example how to do this)
  
### Access permissions

If the register message contains an entry "role", the the framework will check if the current user has that role before 
proceeding. If not, it will redirect to a login page.

### Administrative interface
  
If the register message contains an entry "server", the framework will provide an administrative
  user interface for that Verticle. Server is a JsonObject with the following fields:
  
  * id - a unique ID for the server
  * name - a descriptive name
  * address - The EventBus Address of the administrative access.
  * params - A JsonArray with all Parameters that can be queried, set or executed via the administrative interface. Each parameter entry is
  a JsonObject with the following fields:
    * name - a unique name for the parameter (unique within this service)
    * caption - a caption for the web interface
    * type - one of 'string', 'boolean', 'number', 'directory', 'file' or 'object'
    * value - value for the parameter
  
 