#!/bin/bash

./s-compile-all-uberjar.sh
java -jar ./target/uberjar/jbreakpoint-0.1.0-SNAPSHOT-standalone.jar
