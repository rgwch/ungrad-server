## Dispatcher

This is the central class of the Ungrad Server. It manages mpdules and dispatches REST calls accordingly. The system
is built upon the [Vert.x](http://vertx.io) ecosystem and uses its EventBus and Verticle concept.

Ungrad Server is a collection of Verticles. A Verticle is a module of functionality, very similar to a plugin.

On Startup, the Dispatcher runs configured verticles. But additional verticles (or modules) can be launched independently
and connect with the dispatcher.

To register, a verticle must:

 - Send a register message to "ch.elexis.ungrad.server.register" on the EventBus. The message content is a JsonObject
 with the following fields:
    - "rest" -> the rest address. For example: "1.0/someFunc", to register for calls to "https://myhost/api/1.0/someFunc".
     The address can contain variables, as in "1.0/someFunc/:foo/:bar"
    - "method" -> the request method to listen for (get or post)
    - "ebaddress" -> the event bus address this module will listen.
    
 - Listen to the EventBus-address provided when registering ("ebaddress"). The Dispatcher will catch requests to the registered
 address and convert them to EventBus messages, which will be dispatched to the client. The EventBus message content will
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
  
  