set term postscript color enhanced
set output 'Historical.eps'
#
set size nosquare
set size noratio
#
set key outside right
#
set timefmt "%d.%m.%Y"
set xdata time
set xrange [ "01.06.2003":"01.06.2013" ]
set xtics font "Helvetica,20" rotate by -45
set xtics ("01.06.2004","01.06.2005","01.06.2006","01.06.2007","01.06.2008","01.06.2009","02.06.2010","01.06.2011","28.05.2012")
#
set ytics font "Helvetica,20"
set ytics format "%g%%"
set ytics (0,10,20,30,40,50,60,70,80,90,100)
set yrange [0:100]
#
set xlabel "date" font "Helvetica,20" offset 0,-3
set ylabel "Prefix updates in correlated spikes, \n % from total \n \n" font "Helvetica,20"
#
set format x "%d.%m.%y"
#
plot 'duplicated.eqix.result' every ::1 using 1:($8>0?$8/$5*100:1/0) with linespoints linetype 3 title 'eqix', \
'duplicated.isc.result' every::1 u 1:($8>0?$8/$5*100:1/0) w lp lt 1 title 'isc', \
'duplicated.linx.result' every::1 u 1:($8>0?$8/$5*100:1/0) w lp lt 2 title 'linx', \
'duplicated.rv2.result' every::1 u 1:($8>0?$8/$5*100:1/0) w lp lt 25 title 'rv2', \
'duplicated.rv4.result' every::1 u 1:($8>0?$8/$5*100:1/0) w lp lt 4 title 'rv4', \
'duplicated.wide.result' every::1 u 1:($8>0?$8/$5*100:1/0) w lp lt 5 title 'wide'
