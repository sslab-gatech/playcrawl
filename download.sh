#!/bin/bash

if [ $# -eq 1 ]
then
    apps_folder=$1
else
    apps_folder=/data/playcrawl/apps
fi

./multiplay.py download ACCOUNT/apps.json $apps_folder
