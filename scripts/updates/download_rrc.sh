#!/bin/bash
while read line
do
mkdir ripe.$line
cd ripe.$line
wget -r -A bview.20120715.0000.gz -np -nd http://data.ris.ripe.net/$line/2012.07/
cd ..
done < "ripe.txt"