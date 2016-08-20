#! /bin/sh
rootdir=`pwd`
console=${rootdir}/console
dispatcher=${rootdir}/dispatcher
webelexis=${rootdir}/webelexis

cd ${console}
rm -rf  ${dispatcher}/src/main/resources/web
mkdir -p ${dispatcher}/src/main/resources/web/scripts
au build
cp index.html ${dispatcher}/src/main/resources/web/index.html
cp -r scripts ${dispatcher}/src/main/resources/web
cd ${webelexis}
mvn clean package
cd ${rootdir}
mvn clean package

