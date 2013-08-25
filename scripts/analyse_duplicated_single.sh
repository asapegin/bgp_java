#!/bin/bash

for filename in input_names_single/*
do
extension="${filename##*.}"
./analyse --input-names=$filename --map=map.20110602 --duplicated=results/duplicated.$extension.result --short --buffers=30 --percentages=99 >> analyse.log
done