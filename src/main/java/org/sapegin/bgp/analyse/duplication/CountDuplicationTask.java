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
package org.sapegin.bgp.analyse.duplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.AnalyseSpikesTask;
import org.sapegin.bgp.analyse.generics.ASsFactory;
import org.sapegin.bgp.analyse.updates.Updates;
import org.sapegin.bgp.analyse.updates.UpdatesFactory;
import org.sapegin.bgp.analyse.visibility.ASsToAnalyse;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         This class represents a task of finding duplication rates in BGP
 *         update spikes coming from different monitors
 * 
 * @param <T>
 */
public class CountDuplicationTask<T extends ASsToAnalyse> extends
		AnalyseSpikesTask<T> {

	// separate statistics for every collector
	private Map<String, DuplicationStats> stats = new HashMap<String, DuplicationStats>();
	
	private ASsFactory<T> factoryASs;

	// filename to write results
	private String filenameDuplicated;

	private ArrayList<Float> percentages;
	private ArrayList<Byte> buffers;

	/**
	 * prevent access to the default constructor
	 */
	protected CountDuplicationTask() {
	}

	public CountDuplicationTask(Properties properties, ASsFactory<T> factoryASs)
			throws Exception {

		super(properties, factoryASs);

		// get filename from properties
		this.filenameDuplicated = properties.getProperty(
				"duplication_results_filename",
				"duplicated_spikes_stats_all_collectors");

		// parse percentages
		this.percentages = new ArrayList<Float>();
		String perc = properties.getProperty("duplication_percentages",
				"0.1,0.4,0.7,0.99");
		List<String> percStrArray = Arrays.asList(perc.split(","));
		for (String percent : percStrArray) {
			this.percentages.add(Float.parseFloat(percent));
		}

		// parse time buffers
		this.buffers = new ArrayList<Byte>();
		String buff = properties.getProperty("time-buffers", "5,15,30");
		List<String> buffStrArray = Arrays.asList(buff.split(","));
		for (String buffer : buffStrArray) {
			this.buffers.add(Byte.parseByte(buffer));
		}

		this.factoryASs = factoryASs;

	}

	@Override
	public void selectVisibleDuplicatedSpikes() throws Exception {

		UpdatesFactory<ASsToAnalyse> updatesFactory = new UpdatesFactory<ASsToAnalyse>();

		Iterator<String> updatesIterator = inputUpdatesFilenames.iterator();
		Iterator<String> asesIterator = inputASsFilenames.iterator();

		// for every collector
		while (updatesIterator.hasNext() && asesIterator.hasNext()) {
			String inputUpdates = updatesIterator.next();
			String inputASes = asesIterator.next();

			// read AS names for this collector
			ASsNames ases = new ASsNames(new ArrayList<String>(
					Arrays.asList(inputASes)));

			// prepare AS graph
			T visibleASs = this.factoryASs.create(iMap, ases, 1, 1);

			logger.info("Loading updates from " + inputUpdates + "...");

			// read updates
			Updates updates = updatesFactory.createUpdates(false, null, false,
					false, visibleASs,
					new ArrayList<String>(Arrays.asList(inputUpdates)), false);

			// for every time buffer and duplication percentage
			for (byte buffer : this.buffers) {
				for (float percentage : this.percentages) {

					logger.info("Starting analysis with "
							+ buffer
							+ " seconds time buffer and "
							+ percentage
							* 100
							+ "% of equal prefixes in two spikes to be concerned duplicated");

					// create analyser
					DuplicationAnalyser analyser = new DuplicationAnalyser(
							updates, buffer, percentage);
					analyser.calculateDuplication();

					// find duplicated spikes from different monitors for
					// current
					// collector
					DuplicationStats singleCollectorStats = analyser.getStats();

					// save statistics for the collector to the Map.
					this.stats.put(new File(inputUpdates).getName() + buffer
							+ percentage, singleCollectorStats);
				}
			}
		}
	}

	@Override
	public void writeResults() {
		File file = new File(this.filenameDuplicated.substring(0,
				this.filenameDuplicated.lastIndexOf("/")));
		file.mkdirs();

		try {
			FileWriter fwr = new FileWriter(this.filenameDuplicated);
			BufferedWriter bwr = new BufferedWriter(fwr);

			// write header:
			bwr.write("Collector" + "\t" + "time_buffer" + "\t"
					+ "duplication_percentage" + "\t" + "total_spikes" + "\t"
					+ "total_prefixes" + "\t" + "duplicated_spikes" + "\t"
					+ "prefixes_in_duplicated_spikes" + "\t"
					+ "duplicated_prefixes_in_duplicated_spikes" + "\t" 
					+ "all_duplicated_prefixes" + "\n");

			// write results for every collector
			// 1 line - 1 collector
			for (String collector : inputUpdatesFilenames) {
				collector = new File(collector).getName();
				// write results for every time buffer and duplication
				// percentage
				for (Byte buffer : this.buffers) {
					for (Float percentage : this.percentages) {
						DuplicationStats collectorStats = stats.get(collector
								+ buffer + percentage);
						bwr.write(collector
								+ "\t"
								+ buffer
								+ "\t"
								+ percentage
								+ "\t"
								+ collectorStats.getTotalNumberOfSpikes()
								+ "\t"
								+ collectorStats.getTotalNumberOfPrefixes()
								+ "\t"
								+ collectorStats.getNumberOfDuplicatedSpikes()
								+ "\t"
								+ collectorStats
										.getNumberOfPrefixesInDuplicatedSpikes()
								+ "\t"
								+ collectorStats
										.getNumberOfDuplicatedPrefixesInDuplicatedSpikes()
								+ "\t"
								+ collectorStats.getNumberOfAllDuplicatedPrefixesWithAllSpikes()
								+ "\n");
					}
				}
			}

			bwr.close();
			fwr.close();

		} catch (IOException e) {
			logger.error(
					"Error during writing results for duplication analysis", e);
		}
	}

}
