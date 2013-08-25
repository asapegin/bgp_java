#!/bin/bash
for f in `ls -l | grep -e "dr" | awk '{print $9}'` #for every folder
do
    echo "Converting $f"
    for dump in $f/*
    do
	#echo $dump
	#./bgpdump -m -i $dump >> /media/truecrypt3/episode_6/ribs/$f.rib_m
	echo "$f.m" #>> /media/truecrypt3/episode_6/rib_names
    done
done