#!/bin/bash

ROOT=$(dirname $0)
cd $ROOT
ROOT=$(pwd)

java -jar target/playcrawl.jar $@
