#!/bin/bash
for f in `ls -l | grep -e "dr" | awk '{print $9}'` #for every folder
do
    cd $f
    echo "I'm in $f"
    for archive in *
    do
	filename=$(basename "$archive")
	extension="${filename##*.}"
	#echo "Looking at: $archive"
	if [ "$extension" = "gz" ] 
	then
	    gunzip $archive
	    #echo "filename: $filename extension: $extension type: gzip"
	else 
	    if [ "$extension" = "bz2" ] 
	    then
		bunzip2 $archive
		#echo "filename: $filename extension: $extension type: bzip2"
	    fi
	fi
    done
    cd ..
done