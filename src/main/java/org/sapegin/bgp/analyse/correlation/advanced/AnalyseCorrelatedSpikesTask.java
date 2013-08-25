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

import java.io.IOException;
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
 *         this class defines a task of classifying spikes into correlated and
 *         not correlated
 * 
 *         correlated will be classified into groups by maximum distance between
 *         monitors in the group of spikes correlated with the given one
 * 
 *         not correlated or single will be classified into groups depending on
 *         the visibility of monitors (number of monitored neighbours)
 * 
 *         Additionally, the distributions of maximum inter-arrival time and
 *         number of origin ASs for every group of spikes correlated with the
 *         given one will be analysed
 * 
 * @param <T>
 */
public class AnalyseCorrelatedSpikesTask<T extends ASsToAnalyse> extends
		AnalyseSpikesTask<T> {

	private MonitoredCorrelatedSpikes<T> correlatedSpikes;

	private T visibleASs;

	private String classificationResultsFilename;
	private String timeQuartilesFilename;
	private String originsQuartilesFilename;

	private Updates allUpdates;
	
	private int threads;
	private int startInterval;
	private int endInterval;

	protected AnalyseCorrelatedSpikesTask() {
	}

	public AnalyseCorrelatedSpikesTask(Properties properties,
			ASsFactory<T> factoryASs) throws Exception {
		// initialise super class and read common properties
		super(properties, factoryASs);

		this.threads = Integer.parseInt(properties.getProperty("threads", "1"));
		this.startInterval = Integer.parseInt(properties.getProperty(
				"start_interval", "1"));
		this.endInterval = Integer.parseInt(properties.getProperty(
				"end_interval", "203"));

		// read all names of monitored ASs
		logger.info("loading AS names...");
		ASsNames allASs = new ASsNames(inputASsFilenames);

		// select visible ASs with visibilityPercent
		// I don't know if i will get visible or monitored ASs from generic
		// factory,
		// so I just call all of them visible
		visibleASs = factoryASs.create(iMap, allASs, 1, threads);

		this.classificationResultsFilename = properties.getProperty(
				"classification_results_filename", "classification_"
						+ timeBuffer + "_" + duplicationPercentage);
		this.timeQuartilesFilename = properties.getProperty(
				"time_quartiles_filename", "classification_" + timeBuffer + "_"
						+ duplicationPercentage + "_time_quartiles_all_hops");
		this.originsQuartilesFilename = properties.getProperty(
				"origins_quartiles_filename", "classification_" + timeBuffer
						+ "_" + duplicationPercentage
						+ "_origins_quartiles_all_hops");

		UpdatesFactory<ASsToAnalyse> updatesFactory = new UpdatesFactory<ASsToAnalyse>();

		logger.info("loading RIBs and updates...");
		Boolean correlated = properties.getProperty("Analysis_type").equals(
				"correlated");
		allUpdates = updatesFactory.createUpdates(useRIBs, inputRIBsFilenames,
				componentOnly, correlated, visibleASs, inputUpdatesFilenames,
				synchronise);

		correlatedSpikes = new MonitoredCorrelatedSpikes<T>(this.threads,
				this.startInterval, this.endInterval, iMap, allUpdates, null,
				timeBuffer, duplicationPercentage, visibleASs, false, null);
	}

	@Override
	public void selectVisibleDuplicatedSpikes() throws Exception {
		correlatedSpikes.startThreads();
	}

	@Override
	public void writeResults() {
		try {
			correlatedSpikes.writeResults(this.classificationResultsFilename,
					this.timeQuartilesFilename, this.originsQuartilesFilename);
		} catch (IOException e) {
			logger.error("Exception during writing results", e);
		}
	}

}
