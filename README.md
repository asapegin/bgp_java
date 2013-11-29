bgp_java
========

This is console software for correlation analysis of BGP updates.

The example analysis is described in the following paper:

Andrey Sapegin and Steve Uhlig. "On the extent of correlation in BGP updates in the Internet and what it tells us about locality of BGP routing events." Elsevier Computer Communications 36(15–16), 2013, pages 1592–
1605.

The behaviour of the program and analysis modes are controlled in properties.xml in the resources folder (this is a Maven-based project).

To switch between analysis modes, change <entry key="Analysis_type"> in the properties.xml.

1. The 'duplication' mode should be used for BGP analysis described in the Section 3 of the paper.

2. The 'correlated' mode corresponds to Sections 4 and 5 of the paper.

3. The 'graph' mode could be used to plot the spikes and their propagation paths. After the program loads dumps of BGP updates, it selects a defined amount of random spikes. Then, for each spike, it finds a group of spikes correlated with it, and finally prints all spikes together (gnuplot code is automatically generated), as well as prints out the propagation path (graphviz file is automatically generated). This mode is documented in the recently accepted paper "Catch the spike: on the locality of individual BGP update bursts". I will place the link on it here very soon.


As the input, the program accepts dumps of BGP updates in machine-readable ASCII format, that is produced by conversion of raw dumps using the 'route_btoa' tool from MRT 2.2.2a package. The location of dumps is specified in the 'input_names.txt' file.
It supports dumps of BGP RIBs as well, that could be generated using bgpdump tool.
All scripts for downloading and converting the dumps are located in the 'scripts' folder.
Initially, for each file with update dumps, the file with ASs sending the updates from the dumps should be generated. To do it, set <entry key="generate_ases"> to true, and it will be generated automatically.

As this program relates the updates with the Internet topology data, you also need maps with Internet topology from Internet Topology Project, or Cycle-ASlinks dataset from CAIDA. Please notice that it is highly recommended to have the topology maps for the same date, as dumps of BGP updates, as soon as Internet topology changes constantly.

For more details, please read javadoc, or feel free to contact me directly.
