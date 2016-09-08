#! /bin/sh
rootdir=`pwd`
console=${rootdir}/admin-ui
dispatcher=${rootdir}/dispatcher

cd ${console}
rm -rf  ${dispatcher}/src/main/resources/web
mkdir -p ${dispatcher}/src/main/resources/web/dist
gulp export
cp -r export/* ${dispatcher}/src/main/resources/web/
cd ${rootdir}
mvn clean package

