# Elexis ungrad Server

## What is it?

Elexis Ungrad Server is a collection of server components for the Elexis electronic medical record system.
Much like Elexis itself, Ungrad Server is highly modular and customizable. One can easily include and exclude features, and it is
quite simple to add custom plugins. I'll explain this below.

Ungrad server has two API's: (1) a REST-Api for programmatic access, found in `<server>:2016/api/1.0/xxx`, and a graphical UI residing at `<server>:2016/ui/index.html`. Both share the same code base and the same authentication and authorization modalities.

## Build

### Prerequisites

* Java 8
* Node.js and NPM
* Maven 3.x

### Preparation

    git clone https://github.com/rgwch/ungrad-server
    cd ungrad-server/dispatcher/src/client/ungrad-console

    sudo npm install -g aurelia-cli
    npm install
    cd ../../../../

### Build

* Full build: `./make`
* Server only: `mvn clean package`
* client only: `cd dispatcher/src/client/ungrad-console && au build`

## Launching and usage:

After successful build, the server is in dispatcher/target. Run it from there or copy to somewhere else (there are no external runtime dependencies except Java 8). Then create a `user.json` file (use `default.json` as a reference) and launch the server with:

    java -jar ungrad-server-dispatcher-x.y.z-SNAPSHOT.jar

Launch your favorite web browser and navigate to `http://localhost:2016/index.hml`. You'll (hopefully) see a login screen. Enter uisername and password as defined in `user.json`, and there you are.

## Extending functionality

The basic units of functionality are *Verticles*. A Verticle is a independently running piece of software, invented by vert.x. It features are:

* Normally single threaded, no deadlocks. Multicore- Capability is achieved by launching a verticle multiple times in parallel.
* Has a communication system to talk to other verticles, the EventBus.
* Can be written in Java, Ruby, JavaScript, Python, Scala and others. Verticles of different languages can play peacefully together and communicate via the eventBus
* Interconnected Verticles can reside in the same VM, or in different VMs on the same Machine, or on different Machines within the same network.

In Ungrad-Server, Verticles are added and launched in a number of ways:

* Some Verticles are built-in and Launched, whenever the main program launches.
* The Launcher (ch.elexis.ungrad.server.Launcher) checks its configuration on startup and launches external Verticles configured there.
* A program can be launched completely independent, launch its ow verticles and connect to ungrad-server via the EventBus.



## Technologies

* Server-side APIs, Verticle technology and communication: [Vert.x](http://vertx.io)
* Client UI: [Aurelia](http://aurelia.io)
