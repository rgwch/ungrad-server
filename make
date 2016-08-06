#! /bin/sh
cd dispatcher/src/client/ungrad-console
rm -rf  ../../main/resources/web
mkdir -p ../../main/resources/web/scripts
au build
cp index.html ../../main/resources/web/index.html
cp -r scripts ../../main/resources/web
cd ../../../../
mvn clean package

