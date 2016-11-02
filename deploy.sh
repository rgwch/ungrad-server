#!/usr/bin/env bash


PRODUCTS=${WEBSPACE}/products/ungrad-server

mkdir -p ${PRODUCTS}/${BUILD_NUMBER}

cp dispatcher/target/ungrad-server-dispatcher*.jar ${PRODUCTS}/${BUILD_NUMBER}

rm ${PRODUCTS}/latest
ln -s ${PRODUCTS}/${BUILD_NUMBER} ${PRODUCTS}/latest
