#!/bin/bash
while read line
do
mkdir $line
cd $line
wget -r -A *b.20120715.0000.bz2 -np -nd ftp://archive.routeviews.org/$line/bgpdata/2012.07/RIBS/
cd ..
done < "routeviews.txt"