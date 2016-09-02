#! /bin/sh
rootdir=`pwd`
console=${rootdir}/admin-ui
dispatcher=${rootdir}/dispatcher

cd ${console}
rm -rf  ${dispatcher}/src/main/resources/web
mkdir -p ${dispatcher}/src/main/resources/web/dist
gulp build
cp index.html ${dispatcher}/src/main/resources/web/index.html
cp -r dist ${dispatcher}/src/main/resources/web
cd ${rootdir}
mvn clean package

