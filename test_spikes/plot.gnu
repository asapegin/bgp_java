set term postscript enhanced color
set style fill solid
set output 'all_updates.eps'
set xtics format "%10.0f"
set yrange [-10:]
set key off
plot 'updates_m_2_22548' w i, 'updates_m_1_24875' w i, 'updates_m_1_29073' w i, 'updates_m_2_1916' w i, 'updates_m_1_12956' w i