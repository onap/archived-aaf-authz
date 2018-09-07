#!/bin/bash
rm -Rf WEB-INF/classes/*
cp -Rf target/test-classes/* WEB-INF/classes
jar -cvf caditest.war META-INF WEB-INF index.html
