#!/usr/bin/env bash


PRODUCTS=${WEBSPACE}/products/ungrad-server

mkdir -p ${PRODUCTS}/${BUILD_NUMBER}

cp dispatcher/target/ungrad-server-dispatcher*.jar ${PRODUCTS}/${BUILD_NUMBER}
cp webelexis/target/webelexis-*.jar ${PRODUCTS}/${BUILD_NUMBER}
cp lucinda/target/lucinda-*.jar ${PRODUCTS}/${BUILD_NUMBER}
cp tester/target/ungrad-server-tester-*.jar ${PRODUCTS}/${BUILD_NUMBER}
cp -r article ${PRODUCTS}/${BUILD_NUMBER}

rm ${PRODUCTS}/latest
ln -s ${PRODUCTS}/${BUILD_NUMBER} ${PRODUCTS}/latest
