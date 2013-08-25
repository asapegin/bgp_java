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
package org.sapegin.bgp.analyse.correlation.advanced;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.correlation.ClassificationResults;
import org.sapegin.bgp.analyse.duplication.DuplicationStats;
import org.sapegin.bgp.analyse.spikes.SelectedSpikes;
import org.sapegin.bgp.analyse.updates.Updates;
import org.sapegin.bgp.analyse.visibility.ASsToAnalyse;

/**
 * 
 * @author Andrey Sapegin
 * 
 * 
 * 
 * @param <T>
 */
public class MonitoredCorrelatedSpikes<T extends ASsToAnalyse> {

	private int threads;
	private int firstInterval;
	private int lastInterval;
	private InternetMap iMap;
	private Updates allUpdates;
	private Updates allUpdatesFromAllASs;
	private long timeBuffer;
	private double duplicationPercentage;
	private T visibleASs;
	private Boolean basic;
	private Double basicThreshold;

	private Map<Integer, ClassificationResults> results = new HashMap<Integer, ClassificationResults>();

	private Logger logger = LogManager
			.getLogger(MonitoredCorrelatedSpikes.class);

	public MonitoredCorrelatedSpikes(int threads, int startInterval,
			int endInterval, InternetMap iMap, Updates allUpdates,
			Updates allUpdatesFromAllASs, long timeBuffer,
			double duplicationPercentage, T visibleASs, Boolean basic,
			Double basicThreshold) {
		this.threads = threads;
		this.iMap = iMap;
		this.allUpdates = allUpdates;
		this.allUpdatesFromAllASs = allUpdatesFromAllASs;
		this.timeBuffer = timeBuffer;
		this.duplicationPercentage = duplicationPercentage;
		this.visibleASs = visibleASs;
		this.firstInterval = startInterval;
		this.lastInterval = endInterval;
		this.basic = basic;
		this.basicThreshold = basicThreshold;
	}

	/**
	 * divide one-second spikes into groups by size (0..99, 100..199, ...) for
	 * each group divide spikes into computation blocks depending on the number
	 * of classifier threads.
	 * 
	 * Runs threads and merges results
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IOException
	 */
	public void startThreads() throws InterruptedException, ExecutionException,
			IOException {

		for (int interval = this.firstInterval; interval < this.lastInterval; interval++) {
			// get all spikes with sizes from sizeInterval
			SelectedSpikes selectedSpikes = new SelectedSpikes(
					allUpdates.getSpikesWithPredefinedSize(
							interval * 100 - 100, interval * 100, threads));

			logger.info("" + selectedSpikes.numberOfSpikes() + " spikes with "
					+ selectedSpikes.numberOfUpdates()
					+ " updates and size from " + (interval * 100 - 100)
					+ " to " + (interval * 100 - 1) + " found.");

			int computationBlockSize = selectedSpikes.numberOfSpikes()
					/ threads;
			int classifierThreads = threads;

			if (computationBlockSize == 0) {
				// run only one thread to do all the job
				classifierThreads = 1;
			}

			ArrayList<ClassifierThread> threadArray = new ArrayList<ClassifierThread>();

			for (int i = 0; i < classifierThreads; i++) {
				// divide task for threads
				int startIndex = i * computationBlockSize + 1;
				int endIndex = (i + 1) * computationBlockSize;
				if (i == (classifierThreads - 1)) { // last thread finishes the
													// job
					endIndex = selectedSpikes.numberOfSpikes();
				}

				ClassifierThread classifier;
				if (allUpdatesFromAllASs != null) { // if I need to perform
													// basic
													// classification of visible
													// ASs only
					// find correlated spikes for selected spikes only, but
					// among all spikes from all monitored ASs
					classifier = new ClassifierThread(duplicationPercentage,
							selectedSpikes.getSpikes(startIndex, endIndex),
							visibleASs, allUpdatesFromAllASs, timeBuffer,
							iMap.copyMap(), basic, basicThreshold);
				} else {
					// otherwise, perform usual classification on the same
					// updates dataset with selected spikes
					classifier = new ClassifierThread(duplicationPercentage,
							selectedSpikes.getSpikes(startIndex, endIndex),
							visibleASs, allUpdates, timeBuffer, iMap.copyMap(),
							basic, basicThreshold);
				}

				threadArray.add(classifier);
			}

			logger.info("Executing threads...");

			ExecutorService executor = Executors.newCachedThreadPool();

			ClassificationResults intervalResults = new ClassificationResults();

			// run all threads and get list of results
			List<Future<ClassificationResults>> results = executor
					.invokeAll(threadArray);

			logger.info("threads execution finished. Merging results...");

			// merge results list
			for (Future<ClassificationResults> futureResultsFromOneThread : results) {
				ClassificationResults resultsFromOneThread = futureResultsFromOneThread
						.get();
				intervalResults.merge(resultsFromOneThread);
			}

			logger.info("results merged into interval results");

			this.results.put(interval, intervalResults);
			// dump results for current interval to file
			writeCurrentClassificationResultsForOneInterval(
					"temp_classification_results", interval);

			executor.shutdown();
		}
	}

	/**
	 * Writes classification results to files.
	 * 
	 * @param classificationResultsFilename
	 * @param timeQuartilesFilename
	 * @param originsQuartilesFilename
	 * @throws IOException
	 */
	public void writeResults(String classificationResultsFilename,
			String timeQuartilesFilename, String originsQuartilesFilename)
			throws IOException {

		if (this.basic) {
			// write results for basic classification and return
			this.writeBasicClassificationResults(classificationResultsFilename);
		} else {

			// find max hop to prepare file header
			byte maxHop = 0;
			for (int interval = this.firstInterval; interval < this.lastInterval; interval++) {
				for (byte hop : this.results.get(interval)
						.getCorrelatedSpikesStats().keySet()) {
					if (maxHop < hop) {
						maxHop = hop;
					}
				}
			}

			this.writeClassificationResults(classificationResultsFilename,
					maxHop);

			// merge time and origins
			ClassificationResults allClassificationResults = new ClassificationResults();

			for (ClassificationResults intervalResults : this.results.values()) {
				allClassificationResults.merge(intervalResults);
			}

			this.writeTimeQuartiles(timeQuartilesFilename,
					allClassificationResults.getCorrelatedSpikesStats(), maxHop);
			this.writeOriginsQuartiles(originsQuartilesFilename,
					allClassificationResults.getCorrelatedSpikesStats(),
					allClassificationResults.getSingleSpikesMax033Visible(),
					allClassificationResults.getSingleSpikesMax066Visible(),
					allClassificationResults.getSingleSpikes100Visible(),
					maxHop);

		}
	}

	/**
	 * Writes the results of basic classification for all intervals to file
	 */
	private void writeBasicClassificationResults(
			String basicClassificationResultsFilename) throws IOException {
		// create folders if needed
		if (basicClassificationResultsFilename.lastIndexOf("/") != -1) {
			File file = new File(basicClassificationResultsFilename.substring(
					0, basicClassificationResultsFilename.lastIndexOf("/")));
			file.mkdirs();
		}

		// writing classification results
		// prepare file
		FileWriter bcfwr = new FileWriter(basicClassificationResultsFilename);
		BufferedWriter bcbwr = new BufferedWriter(bcfwr);

		// write header
		bcbwr.write("Spike_size" + "\t" + "TotalSpikes" + "\t" + "TotalUpdates"
				+ "\t" + "Single" + "\t" + "SingleUpdates" + "\t"
				+ "Correlated" + "\t" + "CorrelatedUpdates" + "\t"
				+ "AllCorrelatedPrefixes");

		// write classification results for each spike size interval
		for (int interval = this.firstInterval; interval < this.lastInterval; interval++) {
			// start a new line
			bcbwr.write("\n");

			// write spike sizes
			bcbwr.write("" + (interval * 100 - 100) + ".."
					+ (interval * 100 - 1));

			// write values
			DuplicationStats intervalBasicStats = this.results.get(interval)
					.getBasicStats();
			bcbwr.write("\t"
					+ intervalBasicStats.getTotalNumberOfSpikes()
					+ "\t"
					+ intervalBasicStats.getTotalNumberOfPrefixes()
					+ "\t"
					+ intervalBasicStats.getNumberOfSingleSpikes()
					+ "\t"
					+ intervalBasicStats.getNumberOfPrefixesInSingleSpikes()
					+ "\t"
					+ intervalBasicStats.getNumberOfDuplicatedSpikes()
					+ "\t"
					+ intervalBasicStats
							.getNumberOfPrefixesInDuplicatedSpikes()
					+ "\t"
					+ intervalBasicStats
							.getNumberOfAllDuplicatedPrefixesWithAllSpikes());
		}

		bcbwr.close();
		bcfwr.close();

	}

	/**
	 * Writes quartiles for distribution of number of origin ASs to file
	 * 
	 * @param originsQuartilesFilename
	 * @param stats
	 * @param maxHop
	 * @throws IOException
	 */
	private void writeOriginsQuartiles(String originsQuartilesFilename,
			Map<Byte, SpikeClassStats> stats, SpikeClassStats single33,
			SpikeClassStats single66, SpikeClassStats single100, byte maxHop)
			throws IOException {

		// create folders if needed
		if (originsQuartilesFilename.lastIndexOf("/") != -1) {
			File file = new File(originsQuartilesFilename.substring(0,
					originsQuartilesFilename.lastIndexOf("/")));
			file.mkdirs();
		}

		FileWriter ofwr = new FileWriter(originsQuartilesFilename);
		BufferedWriter obwr = new BufferedWriter(ofwr);

		// write origins for correlated
		for (byte hop = 1; hop <= maxHop; hop++) {

			// write current hop
			if (hop == 1) {
				// start first line
				obwr.write(hop + " hop");
			} else {
				// make new line and write "hopS"
				obwr.write("\n" + hop + " hops");
			}

			// write xtic
			obwr.write("\t" + hop * 10);

			if (stats.get(hop) != null) {
				Quartiles quartiles = stats.get(hop).getOriginsQuartiles();

				obwr.write("\t" + quartiles.getFirstQuartile() + "\t"
						+ quartiles.getMediana() + "\t"
						+ quartiles.getThirdQuartile() + "\t"
						+ quartiles.getMin() + "\t" + quartiles.getMax());
			} else {
				obwr.write("\t0\t0\t0\t0\t0");
			}
		}

		// write origins for single
		Quartiles quartiles;

		// 33
		quartiles = single33.getOriginsQuartiles();
		if (quartiles != null) {
			obwr.write("single33\t" + (maxHop + 1) * 10);
			obwr.write("\t" + quartiles.getFirstQuartile() + "\t"
					+ quartiles.getMediana() + "\t"
					+ quartiles.getThirdQuartile() + "\t" + quartiles.getMin()
					+ "\t" + quartiles.getMax());
		}
		// 66
		quartiles = single66.getOriginsQuartiles();
		if (quartiles != null) {
			obwr.write("single66\t" + (maxHop + 2) * 10);
			obwr.write("\t" + quartiles.getFirstQuartile() + "\t"
					+ quartiles.getMediana() + "\t"
					+ quartiles.getThirdQuartile() + "\t" + quartiles.getMin()
					+ "\t" + quartiles.getMax());
		}
		// 100
		quartiles = single100.getOriginsQuartiles();
		if (quartiles != null) {
			obwr.write("single100\t" + (maxHop + 3) * 10);
			obwr.write("\t" + quartiles.getFirstQuartile() + "\t"
					+ quartiles.getMediana() + "\t"
					+ quartiles.getThirdQuartile() + "\t" + quartiles.getMin()
					+ "\t" + quartiles.getMax());
		}

		obwr.close();
		ofwr.close();
	}

	/**
	 * Writes quartiles for distribution of maximum inter-arrival times to file
	 * 
	 * @param timeQuartilesFilename
	 * @param stats
	 * @param maxHop
	 * @throws IOException
	 */
	private void writeTimeQuartiles(String timeQuartilesFilename,
			Map<Byte, SpikeClassStats> stats, byte maxHop) throws IOException {

		// create folders if needed
		if (timeQuartilesFilename.lastIndexOf("/") != -1) {
			File file = new File(timeQuartilesFilename.substring(0,
					timeQuartilesFilename.lastIndexOf("/")));
			file.mkdirs();
		}

		FileWriter tfwr = new FileWriter(timeQuartilesFilename);
		BufferedWriter tbwr = new BufferedWriter(tfwr);

		for (byte hop = 1; hop <= maxHop; hop++) {

			// write current hop
			if (hop == 1) {
				// start first line
				tbwr.write(hop + " hop");
			} else {
				// make new line and write "hopS"
				tbwr.write("\n" + hop + " hops");
			}

			// write xtic
			tbwr.write("\t" + hop * 10);

			if (stats.get(hop) != null) {
				Quartiles quartiles = stats.get(hop).getTimeQuartiles();

				tbwr.write("\t" + quartiles.getFirstQuartile() + "\t"
						+ quartiles.getMediana() + "\t"
						+ quartiles.getThirdQuartile() + "\t"
						+ quartiles.getMin() + "\t" + quartiles.getMax());
			} else {
				tbwr.write("\t0\t0\t0\t0\t0");
			}
		}

		tbwr.close();
		tfwr.close();
	}

	/**
	 * Writes classification results to file
	 * 
	 * @param classificationResultsFilename
	 * @param maxHop
	 * @throws IOException
	 */
	private void writeClassificationResults(
			String classificationResultsFilename, byte maxHop)
			throws IOException {

		// create folders if needed
		if (classificationResultsFilename.lastIndexOf("/") != -1) {
			File file = new File(classificationResultsFilename.substring(0,
					classificationResultsFilename.lastIndexOf("/")));
			file.mkdirs();
		}

		// writing classification results
		// prepare file
		FileWriter cfwr = new FileWriter(classificationResultsFilename);
		BufferedWriter cbwr = new BufferedWriter(cfwr);

		// write header
		cbwr.write("Spike_size\tSingle_0.33\tSingleUpdates_0.33\tSingle_0.66\tSingleUpdates_0.66\tSingle_1\tSingleUpdates_1");
		for (byte hop = 1; hop <= maxHop; hop++) {
			cbwr.write("\tDuplicated_" + hop + "_hop\tDuplicatedUpdates_" + hop
					+ "_hop");
		}

		// write classification results for each spike size interval
		for (int interval = this.firstInterval; interval < this.lastInterval; interval++) {
			// start a new line
			cbwr.write("\n");

			// write spike sizes
			cbwr.write("" + (interval * 100 - 100) + ".."
					+ (interval * 100 - 1));

			// write stats for single spikes with visibility <= 0.33
			cbwr.write("\t"
					+ this.results.get(interval).getSingleSpikesMax033Visible()
							.getTotalNumberOfSpikes()
					+ "\t"
					+ this.results.get(interval).getSingleSpikesMax033Visible()
							.getTotalNumberOfPrefixes());
			// write stats for single spikes with (0.33 < visibility <= 0.66)
			cbwr.write("\t"
					+ this.results.get(interval).getSingleSpikesMax066Visible()
							.getTotalNumberOfSpikes()
					+ "\t"
					+ this.results.get(interval).getSingleSpikesMax066Visible()
							.getTotalNumberOfPrefixes());
			// write stats for single spikes with (0.66 < visibility <= 1)
			cbwr.write("\t"
					+ this.results.get(interval).getSingleSpikes100Visible()
							.getTotalNumberOfSpikes()
					+ "\t"
					+ this.results.get(interval).getSingleSpikes100Visible()
							.getTotalNumberOfPrefixes());

			// write statistics for correlated spikes for every hop
			for (byte hop = 1; hop <= maxHop; hop++) {
				SpikeClassStats stats = this.results.get(interval)
						.getCorrelatedSpikesStats().get(hop);
				if (stats != null) {
					cbwr.write("\t" + stats.getTotalNumberOfSpikes() + "\t"
							+ stats.getTotalNumberOfPrefixes());
				} else {
					cbwr.write("\t" + 0 + "\t" + 0);
				}
			}
		}

		cbwr.close();
		cfwr.close();
	}

	/**
	 * Writes results for 1 interval into file
	 * 
	 * @param classificationResultsFilename
	 * @throws IOException
	 */
	private void writeCurrentClassificationResultsForOneInterval(
			String classificationResultsFilename, int interval)
			throws IOException {

		// find maxHop
		byte maxHop = 0;
		for (byte hop : this.results.get(interval).getCorrelatedSpikesStats()
				.keySet()) {
			if (maxHop < hop) {
				maxHop = hop;
			}
		}

		// create folders if needed
		if (classificationResultsFilename.lastIndexOf("/") != -1) {
			File file = new File(classificationResultsFilename.substring(0,
					classificationResultsFilename.lastIndexOf("/")));
			file.mkdirs();
		}

		// prepare file
		File file = new File(classificationResultsFilename);
		FileWriter fwr = new FileWriter(file, true);
		BufferedWriter bwr = new BufferedWriter(fwr);

		if (!file.exists()) {
			// write header
			bwr.write("Spike_size\tSingle_0.33\tSingleUpdates_0.33\tSingle_0.66\tSingleUpdates_0.66\tSingle_1\tSingleUpdates_1");
			for (byte hop = 1; hop <= maxHop; hop++) {
				bwr.write("\tDuplicated_" + hop + "_hop\tDuplicatedUpdates_"
						+ hop + "_hop");
			}
		}

		// start a new line
		bwr.write("\n");

		// write spike sizes
		bwr.write("" + (interval * 100 - 100) + ".." + (interval * 100 - 1));

		// write stats for single spikes with visibility <= 0.33
		bwr.write("\t"
				+ this.results.get(interval).getSingleSpikesMax033Visible()
						.getTotalNumberOfSpikes()
				+ "\t"
				+ this.results.get(interval).getSingleSpikesMax033Visible()
						.getTotalNumberOfPrefixes());
		// write stats for single spikes with (0.33 < visibility <= 0.66)
		bwr.write("\t"
				+ this.results.get(interval).getSingleSpikesMax066Visible()
						.getTotalNumberOfSpikes()
				+ "\t"
				+ this.results.get(interval).getSingleSpikesMax066Visible()
						.getTotalNumberOfPrefixes());
		// write stats for single spikes with (0.66 < visibility <= 1)
		bwr.write("\t"
				+ this.results.get(interval).getSingleSpikes100Visible()
						.getTotalNumberOfSpikes()
				+ "\t"
				+ this.results.get(interval).getSingleSpikes100Visible()
						.getTotalNumberOfPrefixes());

		// write statistics for correlated spikes for every hop
		for (byte hop = 1; hop <= maxHop; hop++) {
			SpikeClassStats stats = this.results.get(interval)
					.getCorrelatedSpikesStats().get(hop);
			if (stats != null) {
				bwr.write("\t" + stats.getTotalNumberOfSpikes() + "\t"
						+ stats.getTotalNumberOfPrefixes());
			} else {
				bwr.write("\t" + 0 + "\t" + 0);
			}
		}

		bwr.close();
		fwr.close();
	}
}
