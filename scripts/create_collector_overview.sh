#!/bin/bash
# set variables
result="overview.txt"
files="route-views2.updates_m route-views4.updates_m route-views.eqix.updates_m route-views.isc.updates_m route-views.linx.updates_m route-views.wide.updates_m"

# write header

# 1. first line
# -date
echo -e -n "date" >> $result
# -names
for file in $files
do
echo -n -e " $file" >> $result
done
echo -n -e "\n" >> $result

# 2. second line
echo -e -n "date" >> $result
for file in $files
do
echo -n -e " monitors prefixes" >> $result
done
echo -n -e "\n" >> $result

for folder in `ls -l | grep -e "dr" | awk '{print $9}' | grep "."` # for every folder with data
do
    echo -n -e "$folder" >> $result # write folder name as row name
    for file in $files # for every file with dumps and ases
    do
	echo -n -e " " >> $result
	if [ -f $folder/updates/$file ]
	then
	    active=`wc -l $folder/generated_ases/$file.ases | cut -d " " -f 1`
	    echo -n -e "$active" >> $result # write number of active monitors
	    echo -n -e " " >> $result
	    prefixes=`wc -l $folder/updates/$file | cut -d " " -f 1`
	    echo -n -e "$prefixes" >> $result # write number of prefix updates
	else
	    echo -n -e "n/a n/a" >> $result # no data for the considered collector
	fi
    done
    echo -n -e "\n" >> $result
done