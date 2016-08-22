# Elexis ungrad Server

## What is it?

Elexis Ungrad Server is a collection of server components for the Elexis electronic medical practice system.
Much like Elexis itself, Ungrad Server is highly modular and customizable. One can easily include and exclude features, and it is
quite simple to add custom plugins. I'll explain this below.

Ungrad server has two API's: (1) a REST-Api for programmatic access, found in `<server>:2016/api/1.0/xxx`,
 and a graphical UI residing at `<server>:2016/ui/index.html`. Both share the same code base and the same authentication and authorization modalities.

## Build

### Prerequisites

* Java 1.8.0_45 or higher (please note: At least revision 45 to be able to launch Parts written in JavaScript) 
* Node.js >= 6.0, and NPM >= 3.0
* Maven >= 3.x

### Preparation

    git clone https://github.com/rgwch/ungrad-server
    cd ungrad-server/console

    sudo npm install -g aurelia-cli
    npm install
    cd ..

### Build

* Full build: `./make`
* Server only: `mvn clean package`
* client only: `cd console && au build`

## Launching and usage:

After successful build, the server is in dispatcher/target. 
Run it from there or copy to somewhere else (there are no external runtime dependencies except Java 8). 
Then create a configuration file (use `dispatcher/src/main/resources/default.json` as a reference) and launch the server with:

    java -jar dispatcher/target/ungrad-server-dispatcher-x.y.z-SNAPSHOT.jar --config=<your-configuration.json>

(Please note that relative paths in the configuration file are relative to the current directory when launching).
Launch your favorite web browser and navigate to `http://localhost:2016/ui/index.hml`. You'll (hopefully) see a login screen. Enter uisername and password as defined in `user.json`, and there you are.

## Extending functionality

The basic units of functionality are *Verticles*. A Verticle is an independently running piece of software, invented by vert.x. 
Its features are:

* No concurrency issues by design, similar to NodeJS: Single threaded, no deadlocks. Multicore capability is achieved by launching a verticle multiple times in parallel.
* Has a communication system to talk to other verticles, the EventBus.
* Can be written in Java, Ruby, Groovy, JavaScript, Python, Scala and others. Verticles of different languages can play peacefully together and communicate via the eventBus
* Interconnected Verticles can reside in the same VM, or in different VMs on the same Machine, or on different Machines within the same network.

In Ungrad-Server, Verticles are added and launched in a number of ways:

* Some Verticles are built-in and launched whenever the main program launches. (e.g. dispatcher/Restpoint)
* The Launcher (ch.elexis.ungrad.server.Launcher) checks its configuration on startup and launches external Verticles configured there. 
* A program can be launched completely independent, launch its own verticles and connect to ungrad-server via the EventBus.

## Configuration

If a --config parameter was given at launch, the launcher uses this file as configuration. If no --config was given, the launcher searches the classpath 
for a file named "default.json" and then for a file called "user.json". If both exist, user.json will overlay default.json.
    

## Technologies

* Server-side APIs, Verticle technology and communication: [Vert.x](http://vertx.io)
* Client UI: [Aurelia](http://aurelia.io)
