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
package org.sapegin.bgp.analyse.updates;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sapegin.bgp.analyse.spikes.MonitoredAS;
import org.sapegin.bgp.analyse.spikes.SingleASspikes;
import org.sapegin.bgp.analyse.spikes.Spike;
import org.sapegin.bgp.analyse.spikes.SpikeCollection;

/**
 * 
 * This class reads from files and stores BGP update messages.
 * 
 * @author Andrey Sapegin
 * 
 */
public abstract class Updates extends SpikeCollection {

	// It's a main map with updates
	// For each key (pair {monitoringRouter,monitoredAS}) all spikes will be
	// stored as value
	// private Map<MonitoredAS, SingleASspikes> allASsUpdates;

	// time in Unix-time format - the moment in time, from which this class has
	// started importing of updates from files
	// it's needed for time synchronisation
	// E.g., if there are 2 files with updates: in the first file first update
	// timestamp is X; and in the second file - X+5 - minStartTime will be X+5.
	// Thus all updates from input files before X+5 timestamp will be skipped
	// and all imported updates will start from the same time moment.
	protected long minStartTime;

	protected ArrayList<String> inputUpdatesFilenames;

	// only updates from ASs listed here will be imported from files
	protected ArrayList<Integer> inputASs;

	// logger
	protected Logger logger = LogManager.getLogger(Updates.class);

	protected Updates() {
	}

	/**
	 * Constructor
	 * 
	 * This constructor will call methods to read files from
	 * {inputUpdatesFilenames} and import updates ONLY for ASs in {inputASs}
	 * list
	 * 
	 * @param inputASsFilenames
	 * @param inputASs
	 */
	public Updates(ArrayList<String> inputUpdatesFilenames,
			ArrayList<Integer> inputASs) {
		this.inputUpdatesFilenames = inputUpdatesFilenames;
		this.inputASs = inputASs;

		findStartTime();

		allUpdates = new HashMap<MonitoredAS, SingleASspikes>();
	}

	/**
	 * Synchronises hash map with updates by deleting spikes with time, when
	 * other pairs (monitored AS, monitoring router) have no spikes, but only if
	 * the time is less than time of first spike of any other pair or if the
	 * time is more than time of last spike of any other pair
	 */
	protected void synchronise() {
		long startTime = 0;
		long endTime = Long.MAX_VALUE;

		// find endTime and startTime
		for (SingleASspikes pairSpikes : allUpdates.values()) {
			if (pairSpikes.getCurrentMinTime() > startTime) {
				startTime = pairSpikes.getCurrentMinTime();
			}

			if (pairSpikes.getCurrentMaxTime() < endTime) {
				endTime = pairSpikes.getCurrentMaxTime();
			}
		}

		// synchronise
		for (SingleASspikes pairSpikes : allUpdates.values()) {
			pairSpikes.synchronise(startTime, endTime);
		}
	}

	protected abstract void readUpdates();

	/**
	 * This method extracts prefix (network IPv4 address) from update message in
	 * machine format created with route_btoa and prepared by readUpdates()
	 * method (message should start with AS name (number))
	 * 
	 * @param str
	 *            - update message in machine format starting from AS name
	 * @return InetAddress - extracted prefix (network address)
	 * @throws UnknownHostException
	 */
	protected InetAddress readPrefix(String str) throws UnknownHostException {
		String prefix = str.substring(str.indexOf("|") + 1, str.indexOf("/"));

		return InetAddress.getByName(prefix);
	}

	/**
	 * This functions looks into every file with BGP updates loaded before and
	 * finds maximum time among starting (first message) time in the files
	 * 
	 */
	private void findStartTime() {
		// at the beginning maximum is 0
		minStartTime = 0;

		// go through all files with BGP updates
		for (int i = 0; i < inputUpdatesFilenames.size(); i++) {
			try {
				FileReader fr = new FileReader(inputUpdatesFilenames.get(i));
				BufferedReader br = new BufferedReader(fr);

				// read first line
				String str = br.readLine();

				if (str == null) { // if file with updates is empty
					br.close();
					fr.close();
					continue;
				}

				// check if message is ok
				if (str.indexOf("|") != 6) {
					logger.fatal("Unexpected update message found! Filename: "
							+ inputUpdatesFilenames.get(i));
				}

				str = str.substring(str.indexOf("|") + 1);

				// check if time is correct
				if (str.indexOf("|") != 10) {
					logger.fatal("Unexpected time length in update message found! Filename: "
							+ inputUpdatesFilenames.get(i));
				}

				// parse time
				int time = Integer.parseInt(str.substring(0, str.indexOf("|")));

				// refresh maximum
				if (time > minStartTime) {
					minStartTime = time;
				}

				br.close();
				fr.close();

			} catch (FileNotFoundException e) {
				logger.fatal(
						"FileNotFound exception during reading files with updates during determining max start time",
						e);
			} catch (IOException e) {
				logger.fatal(
						"IO exception during reading files with updates during determining max start time",
						e);
			}
		}
	}

	/**
	 * 
	 * Selects spikes with predefined size. Order of min- and maxSize parameters
	 * is not significant.
	 * 
	 * @param minSize
	 * @param maxSize
	 * @return spikes with size greater than or equals to minSize and strictly
	 *         less than maxSize
	 * @throws InterruptedException
	 */
	public SpikeCollection getSpikesWithPredefinedSize(int minSize,
			int maxSize, int threads) throws InterruptedException {

		// check sizes
		if (maxSize < minSize) {
			int tmp = maxSize;
			maxSize = minSize;
			minSize = tmp;
		}

		ConcurrentHashMap<MonitoredAS, SingleASspikes> spikesWithPredefinedSize = new ConcurrentHashMap<MonitoredAS, SingleASspikes>(
				threads, (float) 0.75, threads);

		int computationBlockSize = allUpdates.size() / threads;

		ArrayList<UpdatesSelector> selectors = new ArrayList<UpdatesSelector>();

		for (int i = 0; i < threads; i++) {
			// divide task for threads
			int startIndex = i * computationBlockSize + 1;
			int endIndex = (i + 1) * computationBlockSize;
			if (i == (threads - 1)) { // last thread finishes the job
				endIndex = allUpdates.size();
			}

			UpdatesSelector selector = new UpdatesSelector(startIndex,
					endIndex, minSize, maxSize, allUpdates,
					spikesWithPredefinedSize);

			// start thread and add it to the array
			selector.start();
			selectors.add(selector);
		}

		// wait until threads will finish their job
		for (UpdatesSelector selector : selectors) {
			selector.join();
		}

		// convert ConcurrentHashMap to usual HashMap and return result
		return new SpikeCollection(new HashMap<MonitoredAS, SingleASspikes>(
				spikesWithPredefinedSize));
	}

	/**
	 * Dump spike sizes into <updatesChartFolder> folder. This method creates
	 * separate file for each pair {monitored AS, monitoring router}. Spike
	 * sizes for this pair is dumped in the two columns:
	 * 
	 * 1) time of spike (in Unix time format).
	 * 
	 * 2) number of BGP updates received at this time by monitoring router from
	 * monitored AS.
	 * 
	 * @param updatesChartFolder
	 */
	public void printAll(String updatesChartFolder) {

		try {

			String gnuplotFile = updatesChartFolder + "/plot.gnu";

			// prepare to write a gnuplot script
			FileWriter gfwr = new FileWriter(gnuplotFile);
			BufferedWriter gbwr = new BufferedWriter(gfwr);

			// write header of gnuplot script
			gbwr.write("set term postscript enhanced color" + "\n"
					+ "set style fill solid" + "\n" + "set output '"
					+ "all_updates.eps'" + "\n" + "set xtics format \"%10.0f\""
					+ "\n" + "set yrange [-10:]" + "\n" + "set key off" + "\n");

			ArrayList<MonitoredAS> monitoredASs = new ArrayList<MonitoredAS>(
					allUpdates.keySet());

			gbwr.write("plot '" + monitoredASs.get(0).getMonitoringRouter()
					+ "_" + monitoredASs.get(0).getMonitoredAS() + "' w i");

			boolean first = true;

			for (MonitoredAS monitoredAS : monitoredASs) {

				// add filename to gnuplot script
				if (!first) {
					gbwr.write(", '" + monitoredAS.getMonitoringRouter() + "_"
							+ monitoredAS.getMonitoredAS() + "' w i");
				}
				first = false;

				// create folders if needed
				File file = new File(updatesChartFolder);
				file.mkdirs();

				FileWriter fwr = new FileWriter(updatesChartFolder + "/"
						+ monitoredAS.getMonitoringRouter() + "_"
						+ monitoredAS.getMonitoredAS());
				BufferedWriter bwr = new BufferedWriter(fwr);

				SingleASspikes spikes = allUpdates.get(monitoredAS);

				for (long second = spikes.getCurrentMinTime(); second <= spikes
						.getCurrentMaxTime(); second++) {
					if (spikes.hasSpikeAtTime(second)) {
						bwr.write(second + "\t"
								+ spikes.getSpikeAtTime(second).getSpikeSize()
								+ "\n");
					}
				}

				bwr.close();
				fwr.close();

			}

			gbwr.close();
			gfwr.close();

		} catch (IOException e) {
			logger.error(
					"IO error during writing chart data (dumping spike sizes into files)",
					e);
		}
	}

	/**
	 * Selects spikes duplicated with the given spike within the given time
	 * interval.
	 * 
	 * @param spike
	 * @param time
	 * @param timeBuffer
	 * @param as
	 * @param duplicationPercentage
	 * @return spike collection of duplicated spikes
	 */
	public SpikeCollection findDuplicatedSpikes(Spike spike, long time,
			long timeBuffer, MonitoredAS as, double duplicationPercentage) {
		SpikeCollection duplicatedSpikes = new SpikeCollection();

		for (MonitoredAS pair : allUpdates.keySet()) {

			SingleASspikes asSpikes = allUpdates.get(pair);

			for (long second = time - timeBuffer; second <= time + timeBuffer; second++) {

				if ((pair.getMonitoredAS() == as.getMonitoredAS())
						&& (pair.getMonitoringRouter().equals(as
								.getMonitoringRouter())) && (second == time)) {
					// skip the given spike, as I don't want to check if spike
					// is duplicated with itself
					continue;
				}

				if (asSpikes.hasSpikeAtTime(second)) {
					if (asSpikes.getSpikeAtTime(second).isDuplicatedWith(spike,
							duplicationPercentage)) {
						duplicatedSpikes.addSpike(second,
								asSpikes.getSpikeAtTime(second), pair);
					}
				}
			}
		}

		return duplicatedSpikes;
	}

	/**
	 * This function find duplication in spikes from different monitors
	 * (MonitoredASs)! The procedure never checks duplication in spikes from the
	 * same monitor!
	 * 
	 * It returns the set of prefixes marked duplicated. However, the set only
	 * contains UNIQUE prefixes. The situation when the analysed spike has
	 * several equal prefixes (with same network addresses) is not taken into
	 * account.
	 * 
	 * @param spike
	 * @param time
	 * @param timeBuffer
	 * @param as
	 * @param duplicationPercentage
	 * @return set of unique prefixes in original spike that were ACTUALLY
	 *         marked as duplicated IN DUPLICATED SPIKES
	 */
	public Set<InetAddress> getDuplicatedPrefixesInSpikeIfSpikeIsDuplicated(
			Spike spike, long time, long timeBuffer, MonitoredAS as,
			double duplicationPercentage) {

		Set<InetAddress> duplicatedPrefixes = new HashSet<InetAddress>();

		for (MonitoredAS pair : allUpdates.keySet()) {

			if (pair.getMonitoredAS() == as.getMonitoredAS()) {
				// skip all spikes from the same AS
				continue;
			}

			SingleASspikes asSpikes = allUpdates.get(pair);

			for (long second = time - timeBuffer; second <= time + timeBuffer; second++) {

				if (asSpikes.hasSpikeAtTime(second)) {
					if (asSpikes.getSpikeAtTime(second).isDuplicatedWith(spike,
							duplicationPercentage)) { // if spikes are
														// duplicated
						// find number of duplicated prefixes in spike

						duplicatedPrefixes.addAll(spike
								.getPrefixesDuplicatedWith(asSpikes
										.getSpikeAtTime(second)));
					}
				}
			}
		}

		return duplicatedPrefixes;
	}

	/**
	 * This function find duplication in spikes from different monitors
	 * (MonitoredASs)! The procedure never checks duplication in spikes from the
	 * same monitor!
	 * 
	 * It returns the set of prefixes marked duplicated. However, the set only
	 * contains UNIQUE prefixes. The situation when the analysed spike has
	 * several equal prefixes (with same network addresses) is not taken into
	 * account.
	 * 
	 * @param spike
	 * @param time
	 * @param timeBuffer
	 * @param as
	 * @param duplicationPercentage
	 * @return set of unique prefixes in original spike that were ACTUALLY
	 *         marked as duplicated WITH ALL OTHER SPIKES WITHIN TIME INTERVAL
	 */
	public Set<InetAddress> getAllDuplicatedPrefixesInSpike(Spike spike,
			long time, long timeBuffer, MonitoredAS as,
			double duplicationPercentage) {

		Set<InetAddress> duplicatedPrefixes = new HashSet<InetAddress>();

		for (MonitoredAS pair : allUpdates.keySet()) {

			if (pair.getMonitoredAS() == as.getMonitoredAS()) {
				// skip all spikes from the same AS
				continue;
			}

			SingleASspikes asSpikes = allUpdates.get(pair);

			for (long second = time - timeBuffer; second <= time + timeBuffer; second++) {

				if (asSpikes.hasSpikeAtTime(second)) {
					// find number of duplicated prefixes in spike
					duplicatedPrefixes.addAll(spike
							.getPrefixesDuplicatedWith(asSpikes
									.getSpikeAtTime(second)));
				}
			}
		}

		return duplicatedPrefixes;
	}
}
