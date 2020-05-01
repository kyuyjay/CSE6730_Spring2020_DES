#!/bin/bash
javac *.java -d test
cd test
java Driver 1 0 1 
java Driver 2 0 1 
cd ..
