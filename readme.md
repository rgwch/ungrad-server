# Elexis ungrad Server

## What is it?

Elexis Ungrad Server is a collection of server components for the Elexis electronic medical practice system.
Much like Elexis itself, Ungrad Server is highly modular and customizable. One can easily include and exclude features, and it is
quite simple to add custom plugins. I'll explain this below.

Ungrad server has two API's: (1) a REST-Api for programmatic access, found in `<server>:2016/api/1.0/xxx`,
 and a graphical UI residing at `<server>:2016/ui/index.html`. Both share the same code base and the same authentication and authorization modalities.

## Build

### Prerequisites

* Java >= 1.8.0_45  (please note: At least revision 45 to be able to launch Parts written in JavaScript) 
* Node.js >= 4.4.7, and NPM >= 3.10.7
* Maven >= 3.x

### Preparation

    git clone https://github.com/rgwch/ungrad-server
    cd ungrad-server/admin-ui

    sudo npm install -g gulp-cli jspm
    npm install
    jspm install -y
    cd ..

### Build

* Full build: `./make`
* Server only: `mvn clean package`
* client only: `cd admin-ui && gulp watch`

## Launching and usage:

After successful build, the server is in dispatcher/target. 
Run it from there or copy to somewhere else (there are no external runtime dependencies except Java 8). 
Then create a configuration file (use `dispatcher/src/main/resources/default.json` as a reference) and launch the server with:

    java -jar dispatcher/target/ungrad-server-dispatcher-x.y.z-SNAPSHOT.jar --config=<your-configuration.json>

(Please note that relative paths in the configuration file are relative to the current directory when launching).
Launch your favorite web browser and navigate to `http://localhost:2016/ui/index.hml`. You'll (hopefully) see a login screen. Enter username and password as defined in `user.json`, and there you are.

## Extending functionality

The basic units of functionality are *Verticles*. A Verticle is an independently running piece of software, invented by vert.x. 
Its features are:

* No concurrency issues by design, similar to NodeJS: Single threaded, no deadlocks. Multicore capability is achieved by launching a verticle multiple times in parallel.
* Has a communication system to talk to other verticles, the EventBus.
* Can be written in Java, Kotlin, Ruby, Groovy, JavaScript, Python, Scala and others. Verticles of different languages can play peacefully together and communicate via the eventBus.
* Interconnected Verticles can reside in the same VM, or in different VMs on the same Machine, or on different Machines within the same network.

if you like fancy buzzwords, you might call such verticles ['microservices'](http://martinfowler.com/articles/microservices.html) - Vert.x promoted microservises before it was cool ;)

In Ungrad-Server, Verticles are added and launched in a number of ways:

* Some Verticles are built-in and launched whenever the main program launches. (e.g. dispatcher/Restpoint)
* The Launcher (dispatcher->ch.elexis.ungrad.server.Launcher) checks its configuration on startup and launches external Verticles configured there. 
* A program can be launched completely independent, launch its own verticles and connect to ungrad-server via the EventBus.

## Configuration

If a --config parameter was given at launch, the launcher uses this file as configuration. If no --config was given, the launcher searches the classpath 
for a file named "default.json" and then for a file called "user.json". If both exist, user.json will overlay default.json.
    
## Further reading
    
You might like to have a look at some of the included subprojects as examples:

* dispatcher - the main entry model; more explanation on the interaction modell provided in its "readme.md".
* article - a verticle implemented in JavaScript, presenting epha's cli-robot data as REST services
* lucinda - a document indexing and retrieving Verticle
* tester - a simple SelfTest and system information Verticle
* webelexis - some Verticles to access Elexis data

* admin-ui - Web UI for admin purposes.

## Technologies

* Server-side APIs, Verticle technology and communication: [Vert.x](http://vertx.io).
* Client UI: [Aurelia](http://aurelia.io), [Materialize css](http://materializecss.com/) and [aurelia-materialize-bridge](http://aurelia-ui-toolkits.github.io/demo-materialize/).
* Built with: [Idea Ultimate 2015](https://www.jetbrains.com/idea/), [Oracle JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html), [npm](https://www.npmjs.com/), [jspm](http://jspm.io/), and [gulp](http://gulpjs.com/). 

## License

Copyright (c) 2016 by G. Weirich, Licensed under the [Eclipse public License V1.0](https://www.eclipse.org/legal/epl-v10.html)

## tl;dr

The following creates the ungrad-server from scratch in a freshly installed Ubuntu 16.04.1:

    sudo apt-get install npm nodejs-legacy openjdk-8-jdk maven git
    sudo npm install -g gulp-cli jspm
    git clone https://github.com/rgwch/ungrad-server.git
    cd ungrad-server/admin-ui
    npm install
    jspm install
    cd ..
    ./make
    java -jar dispatcher/target/ungrad-server-dispatcher-0.1.0-SNAPSHOT.jar
    
(It will take some time and there will be some warnings which you can ignore for now)
    
Then, launch your favourite browser and navigate to `http://localhost:2016/ui/index.html`. Login as user `admin` with
    the password `secret`. After playing around, create a `user.json`, based on `dispatcher/src/main/respurces/default.json` and
    set correct data for your own system.