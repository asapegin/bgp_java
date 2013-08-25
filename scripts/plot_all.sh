#! /bin/bash
for f in `ls -l | grep -e "dr" | awk '{print $9}'`; do
cd $f
gnuplot plot.gnu
cd ..
done