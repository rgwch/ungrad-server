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

