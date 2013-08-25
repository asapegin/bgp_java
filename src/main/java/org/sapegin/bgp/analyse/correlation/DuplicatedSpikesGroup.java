/*
	 * Copyright 2013 Andrey Sapegin
	 * 
	 * Licensed under the "Attribution-NonCommercial-ShareAlike" Vizsage
	 * Public License (the "License"). You may not use this file except
	 * in compliance with the License. Roughly speaking, non-commercial
	 * users may share and modify this code, but must give credit and 
	 * share improvements. However, for proper details please 
	 * read the full License, available at
	 *  	http://vizsage.com/license/Vizsage-License-BY-NC-SA.html 
	 * and the handy reference for understanding the full license at 
	 *  	http://vizsage.com/license/Vizsage-Deed-BY-NC-SA.html
	 *
	 * Please contact the author for any other kinds of use.
	 * 
	 * Unless required by applicable law or agreed to in writing, any
	 * software distributed under the License is distributed on an 
	 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
	 * either express or implied. See the License for the specific 
	 * language governing permissions and limitations under the License.
	 *
	 */
package org.sapegin.bgp.analyse.correlation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Set;

import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.DefaultEdge;
import org.sapegin.bgp.analyse.Colours;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.MyDirectIntegerVertexNameProvider;
import org.sapegin.bgp.analyse.correlation.advanced.AdvancedClassificationResult;
import org.sapegin.bgp.analyse.correlation.basic.BasicClassificationResult;
import org.sapegin.bgp.analyse.spikes.Destination;
import org.sapegin.bgp.analyse.spikes.MonitoredAS;
import org.sapegin.bgp.analyse.spikes.SingleASspikes;
import org.sapegin.bgp.analyse.spikes.Spike;
import org.sapegin.bgp.analyse.spikes.SpikeCollection;
import org.sapegin.bgp.analyse.updates.Updates;
import org.sapegin.bgp.analyse.visibility.ASsToAnalyse;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         class containing a group of spikes duplicated with given. And methods
 *         to analyse and represent (print) it.
 * 
 *         !!! Please pay attention to the obvious fact, that 'Updates' given as
 *         the parameter to the constructor should be loaded from files with BGP
 *         updates dump in accordance with (using) THE SAME 'visible/monitored
 *         ASs' graph given as the parameter to the 'printDOTgraph' method. !!!
 * 
 *         !!! Please also, for the same reasons and in the same manner,
 *         preserve the 'componentOnly' boolean parameter. !!!
 */
public class DuplicatedSpikesGroup extends SpikeCollection {

	private Spike spike;
	private long spikeTime;
	private long timeBuffer;
	private MonitoredAS spikeAS;

	// total number of prefix updates in the group of spikes duplicated with the
	// given one, including given spike
	private long groupUpdateSum;

	// number of prefix updates received by all monitoring routers from the AS
	// of the given spike
	private long givenSpikeASUpdateSum;

	// maximum number of prefix updates in the spike among spikes received by
	// all monitoring routers from the AS of the given spike
	private long givenSpikeASmaxSpikeUpdateSum;

	// maximum number of prefix updates in the spike among spikes received by
	// all monitoring routers from the AS, different from AS of the given spike
	private long otherASmaxSpikeUpdateSum;

	// number of <monitoredAS,monitoringRouter> pairs from which the spikes in
	// the group of spikes, correlated (duplicated) with the given one, were
	// received
	private int totalPairs;

	// number of <monitoredAS,monitoringRouter> pairs with the same AS, as the
	// given spike, and from which the spikes in the group of spikes, correlated
	// (duplicated) with the given one, were received
	private int givenSpikeASpairs;

	// in contrast with allUpdates, where all updates of this particular
	// DuplicatedSpikeGroup are stores, preloadedUpdates REALLY LINKS TO THE ALL
	// updates loaded by this program from files.
	private Updates preloadedUpdates;

	double duplicationPercentage;

	// all prefixes IN THE GIVEN SPIKE, that are ACTUALLY MARKED as duplicated
	// with any other spike FROM DIFFERENT AS
	int allDuplicatedPrefixesInSpike;

	private InternetMap iMap;

	public DuplicatedSpikesGroup(MonitoredAS as, Spike spike, long time,
			Updates updates, InternetMap iMap, long timeBuffer,
			double duplicationPercentage) {
		this.preloadedUpdates = updates;
		this.duplicationPercentage = duplicationPercentage;

		this.spike = spike;
		this.spikeTime = time;
		this.spikeAS = as;
		this.timeBuffer = timeBuffer;

		this.iMap = iMap;

		logger.trace("analysing spike at " + this.spikeTime
				+ " time with size " + this.spike.getSpikeSize()
				+ " prefixes from " + this.spikeAS.getMonitoredAS()
				+ " AS monitored by router "
				+ this.spikeAS.getMonitoringRouter());

		// find spikes, duplicated with the given one, and add them to the group
		logger.trace("searching for spikes duplicated with the given one");
		this.allUpdates.putAll(updates.findDuplicatedSpikes(spike, spikeTime,
				timeBuffer, spikeAS, duplicationPercentage).getUpdateMap());
		int count = 0;
		for (SingleASspikes spikes : this.allUpdates.values()) {
			count += spikes.getNumberOfSpikes();
		}
		logger.trace(count + " spikes duplicated with the given one were found");

		// add the given spike to the group. I need to do it after the previous
		// operation, as
		// HashMap.putAll will overwrite SingleASspikes for the same key
		if (allUpdates.get(spikeAS) != null) {
			allUpdates.get(spikeAS).addSpike(spikeTime, spike);
		} else {
			SingleASspikes tmpSpikes = new SingleASspikes();
			tmpSpikes.addSpike(spikeTime, spike);
			allUpdates.put(spikeAS, tmpSpikes);
		}
		logger.trace("given spike added to the group.");

		// count the total number of pairs
		this.totalPairs = allUpdates.keySet().size();

		// count the number of pairs <monitoredAS,monitoring router>, where
		// monitoredAS is the AS sent the given spike
		this.givenSpikeASpairs = 0;
		for (MonitoredAS monitoredAS : allUpdates.keySet()) {
			if (monitoredAS.getMonitoredAS() == spikeAS.getMonitoredAS()) { // compare
																			// monitored
																			// ASs
				this.givenSpikeASpairs++;
			}
		}

		// count number of updates in the group
		groupUpdateSum = 0;
		for (SingleASspikes spikes : allUpdates.values()) {
			groupUpdateSum += spikes.getCurrentUpdateSum();
		}

		// 1. count sum of updates from the AS of the given spike
		// 2. count pairs statistics

		givenSpikeASUpdateSum = 0;
		givenSpikeASmaxSpikeUpdateSum = 0;
		otherASmaxSpikeUpdateSum = 0;

		for (MonitoredAS monitoredAS : allUpdates.keySet()) { // for every pair

			// SingleASSpikes pairSpikes = allUpdates.get(monitoredAS);
			//
			// PairStats pairStats = new PairStats(pairSpikes
			// .getCurrentUpdateSum(),pairSpikes
			// .getCurrentBiggestSpike().getSpikeSize(),allUpdates.get(monitoredAS).getCurrentMinTime())
			//
			// pairStatistics.put(monitoredAS, pairStats);

			if (monitoredAS.getMonitoredAS() == spikeAS.getMonitoredAS()) { // if
																			// monitoredAS
																			// is
																			// the
																			// same
																			// as
																			// for
																			// given
																			// spike
				// count sum
				givenSpikeASUpdateSum += allUpdates.get(monitoredAS)
						.getCurrentUpdateSum();

				// update maximum
				if (givenSpikeASmaxSpikeUpdateSum < allUpdates.get(monitoredAS)
						.getCurrentBiggestSpike().getSpikeSize()) {
					givenSpikeASmaxSpikeUpdateSum = allUpdates.get(monitoredAS)
							.getCurrentBiggestSpike().getSpikeSize();
				}
			} else { // if monitored AS is not the same as for given spike
				// update maximum
				if (otherASmaxSpikeUpdateSum < allUpdates.get(monitoredAS)
						.getCurrentBiggestSpike().getSpikeSize()) {
					otherASmaxSpikeUpdateSum = allUpdates.get(monitoredAS)
							.getCurrentBiggestSpike().getSpikeSize();
				}
			}
		}

		// identify number of prefixes, actually marked as duplicated in this
		// spike.
		Set<InetAddress> allUniqueDuplicatedPrefixes = preloadedUpdates
				.getAllDuplicatedPrefixesInSpike(this.spike, this.spikeTime,
						this.timeBuffer, this.spikeAS,
						this.duplicationPercentage);
		// find number of duplicated prefixes
		this.allDuplicatedPrefixesInSpike = 0;
		for (Destination destination : spike.copyPrefixSet()) {
			if (allUniqueDuplicatedPrefixes.contains(destination.getPrefix())) {
				this.allDuplicatedPrefixesInSpike++;
			}
		}
	}

	public Spike getSpike() {
		return spike;
	}

	public long getSpikeTime() {
		return spikeTime;
	}

	public MonitoredAS getSpikeAS() {
		return spikeAS;
	}

	/**
	 * Writes a graph of visible/monitored/other ASs received as a parameter
	 * where verticies (ASs) from the group of spikes duplicated with the given
	 * one, are marked with red. If 2 connected ASs are marked (spikes,
	 * duplicated with given, were received from both ASs), this method will
	 * find AS, which has sent duplicated updates earlier. Then the edge between
	 * these 2 ASs will be converted to directed, from AS, which have sent
	 * duplicated updates earlier to AS which have sent them later.
	 * 
	 * @param groupGraphFolder
	 *            - folder to save the DOT file
	 * @param visibleASs
	 *            - graph with ASs (visible or monitored) that were used during
	 *            loading of updates from file with BGP updates dumps !!! !!!
	 *            !!!
	 * @param componentOnly
	 *            - if true, use only biggest connected component of visibleASs
	 *            graph. Should be the same value as during loading updates by
	 *            Updates class (parameter of class constructor of this method)
	 */
	public void printDOTGraph(String groupGraphFolder, ASsToAnalyse visibleASs,
			boolean componentOnly) {
		// create a partly (?) directed graph from visibleASs (if componentOnly
		// - using BiggestConnectedGraphComponent only)
		//
		// as soon as jGraphT does not provide documentation/implementation on
		// how the multigraphs (having both directed and undirected (bidirected)
		// edges) are exported to DOT format, I will export it now as undirected
		// graph and then change direction and other parameters like vertex
		// colour manually
		String exportedGraph;

		if (componentOnly) {
			DOTExporter<Integer, DefaultEdge> dotExporter = new DOTExporter<Integer, DefaultEdge>(
					new MyDirectIntegerVertexNameProvider(), null, null);

			StringWriter swr = new StringWriter();

			// export biggest connected component
			dotExporter.export(swr,
					visibleASs.getBiggestConnectedGraphComponent());

			exportedGraph = swr.toString();
		} else {
			// export all visible/monitored ASs
			exportedGraph = visibleASs.writeStringDOT();
		}

		// mark ASs satisfying threshold conditions in the newly created graph
		double threshold = 1.0 / (allUpdates.keySet().size());
		threshold = threshold / 2.0;

		ArrayList<Integer> markedASs = new ArrayList<Integer>();

		for (MonitoredAS monitoredAS : allUpdates.keySet()) {
			if ((allUpdates.get(monitoredAS).getCurrentUpdateSum() > threshold
					* groupUpdateSum)
					|| (allUpdates.get(monitoredAS).getCurrentBiggestSpike()
							.getSpikeSize() > 0.33 * givenSpikeASmaxSpikeUpdateSum)
					|| monitoredAS == spikeAS) {

				// mark as with red if not already marked
				if (exportedGraph.contains("  " + monitoredAS.getMonitoredAS()
						+ ";")) {
					exportedGraph = exportedGraph.replaceFirst("  "
							+ monitoredAS.getMonitoredAS() + ";", "  "
							+ monitoredAS.getMonitoredAS()
							+ " [fillcolor=red, style=\"filled\"]" + ";");
				}

				if (!markedASs.contains(monitoredAS.getMonitoredAS())) {
					markedASs.add(monitoredAS.getMonitoredAS());
				}
			}
		}

		// for all marked ASs - check if they are connected
		// if yes, set an edge direction based on time of spike arrival from
		// those ASs, also taking into account a spike size
		for (int as1 : markedASs) {
			for (int as2 : markedASs) {
				if (as1 == as2) {
					continue;
				}

				if (exportedGraph.contains(as1 + " -- " + as2)
						|| exportedGraph.contains(as2 + " -- " + as1)) {

					// find which AS has sent duplicated spikes earlier
					long as1MinTime = Long.MAX_VALUE;
					long as2MinTime = Long.MAX_VALUE;
					for (MonitoredAS monitoredAS : allUpdates.keySet()) {
						if ((monitoredAS.getMonitoredAS() == as1)
								|| allUpdates.get(monitoredAS)
										.getCurrentMinTime() < as1MinTime) {
							as1MinTime = allUpdates.get(monitoredAS)
									.getCurrentMinTime();
						}
						if ((monitoredAS.getMonitoredAS() == as2)
								|| allUpdates.get(monitoredAS)
										.getCurrentMinTime() < as2MinTime) {
							as2MinTime = allUpdates.get(monitoredAS)
									.getCurrentMinTime();
						}
					}

					// set edge direction, if one of connected ASs sent a
					// duplicated spike earlier
					if (as1MinTime < as2MinTime) {

						exportedGraph = exportedGraph.replaceFirst(as1 + " -- "
								+ as2, as1 + " -- " + as2 + " [dir=forward]");

					} else if (as2MinTime < as1MinTime) {

						exportedGraph = exportedGraph.replaceFirst(as1 + " -- "
								+ as2, as2 + " -- " + as1 + " [dir=forward]");
					} else {
						logger.warn(as1 + " and " + as2
								+ " have the same MinTime.");
					}
				}
			}
		}

		// write DOT file
		try {
			File dir = new File(groupGraphFolder);
			dir.mkdirs();
			FileWriter fwr = new FileWriter(groupGraphFolder + "/"
					+ spikeAS.getMonitoredAS() + "_"
					+ spikeAS.getMonitoringRouter() + "_" + spikeTime + ".dot");
			fwr.write(exportedGraph);
			fwr.close();
		} catch (IOException e) {
			logger.error(
					"Error during dumping graph for group of duplicated spikes to file",
					e);
		}
	}

	/**
	 * Plots a chart with updates from the spikes in the group of spike
	 * duplicated with the given, within timeBuffer seconds before and after the
	 * given spike.
	 * 
	 * @param groupChartFolder
	 *            - folder to save a file with chart and gnuplot script
	 * @param colours
	 *            - gnuplot colours to prepare gnuplot script
	 */
	public void printAllAround(String groupChartFolder, Colours colours) {
		super.printAllAround(groupChartFolder, spike, spikeTime, timeBuffer,
				colours);
	}

	public AdvancedClassificationResult classify(ASsToAnalyse visibleASs) {

		logger.trace("Starting classification for the one given spike");

		float threshold = (float) (totalPairs - givenSpikeASpairs)
				/ (float) totalPairs;
		threshold = threshold / (float) 2;

		boolean single;

		if ((threshold == 0)
				|| (((groupUpdateSum - givenSpikeASUpdateSum) < (threshold * givenSpikeASUpdateSum)) && (otherASmaxSpikeUpdateSum < 0.33 * givenSpikeASmaxSpikeUpdateSum))) {
			// spike is single
			single = true;

			return new AdvancedClassificationResult(single, this.spike.getSpikeSize(),
					visibleASs.visibility(spikeAS.getMonitoredAS()), null,
					null, spike.getNumberOfOriginASs());
		} else {
			// spike is correlated (duplicated)
			single = false;

			threshold = 1 / (float) totalPairs;
			threshold = threshold / (float) 2;

			// 1. count max distance here.
			// 2. count max time difference here
			long minTime = 2000000000; // infinity (UTC)
			long maxTime = 0;

			// distance of 127 is infinity!
			Byte maxDistance = 0;
			boolean infinite = false; // infinite distance was never found

			for (MonitoredAS pair : allUpdates.keySet()) {
				SingleASspikes pairSpikes = allUpdates.get(pair);

				if ((pairSpikes.getCurrentUpdateSum() > (threshold * this.groupUpdateSum))
						|| (pairSpikes.getCurrentBiggestSpike().getSpikeSize() > (0.33 * this.givenSpikeASmaxSpikeUpdateSum))) {
					// 1. finding maximum time difference part
					if (minTime > pairSpikes.getCurrentMinTime()) {
						minTime = pairSpikes.getCurrentMinTime();
					}
					if (maxTime < pairSpikes.getCurrentMaxTime()) {
						maxTime = pairSpikes.getCurrentMaxTime();
					}

					// 2. finding maximum topological distance between ASs of 2
					// spikes in the group
					for (MonitoredAS secondPair : allUpdates.keySet()) {

						SingleASspikes secondPairSpikes = allUpdates
								.get(secondPair);
						if ((secondPairSpikes.getCurrentUpdateSum() > (threshold * this.groupUpdateSum))
								|| (secondPairSpikes.getCurrentBiggestSpike()
										.getSpikeSize() > (0.33 * this.givenSpikeASmaxSpikeUpdateSum))) {
							byte distance = iMap.getInternetDistanceBFS(
									pair.getMonitoredAS(),
									secondPair.getMonitoredAS());
							if (distance != 127) { // if distance is not
													// infinite
								if (distance > maxDistance) {
									maxDistance = distance;
								}
							} else {
								infinite = true; // mark that infinite distance
													// was found
							}
						}
					}
				}
			}

			if (maxDistance == 0) { // check if maximum distance is still 0
				if (!infinite) { // if there were 2 ASs and no path in the map
									// for them
									// log error!
					logger.error("Logical error. Max distance found is 0 and no infinite distance was found.");
				}
				maxDistance = null; // mark distance as not reachable here
			}

			if (single) {
				logger.trace("Spike classified as single");
			} else {
				logger.trace("Spike classified as correlated");
			}

			return new AdvancedClassificationResult(single, this.spike.getSpikeSize(),
					visibleASs.visibility(spikeAS.getMonitoredAS()),
					maxDistance, (maxTime - minTime),
					spike.getNumberOfOriginASs());
		}
	}
	
	/**
	 * Performs basic classification of the given spike.
	 * 
	 * @param visibleASs
	 * @param basicThreshold
	 * @return
	 */
	public BasicClassificationResult classifyBasic(double basicThreshold) {

		boolean single;

		if (((groupUpdateSum - givenSpikeASUpdateSum) < (basicThreshold * givenSpikeASUpdateSum))
				&& (otherASmaxSpikeUpdateSum < (basicThreshold * givenSpikeASmaxSpikeUpdateSum))) {
			// spike is single
			single = true;
		} else {
			// spike is duplicated
			if (this.allDuplicatedPrefixesInSpike > (basicThreshold * this.spike
					.getSpikeSize())) {
				single = false;
			} else {
				// sorry, but the spike is actually single
				single = true;
			}
		}

		return new BasicClassificationResult(single, this.spike.getSpikeSize(),
				null, this.allDuplicatedPrefixesInSpike);
	}
}
