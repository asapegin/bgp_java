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
package org.sapegin.bgp.analyse.correlation.basic;

import java.io.IOException;
import java.util.Properties;

import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.AnalyseSpikesTask;
import org.sapegin.bgp.analyse.correlation.advanced.MonitoredCorrelatedSpikes;
import org.sapegin.bgp.analyse.generics.ASsFactory;
import org.sapegin.bgp.analyse.generics.MonitoredASsFactory;
import org.sapegin.bgp.analyse.updates.Updates;
import org.sapegin.bgp.analyse.updates.UpdatesFactory;
import org.sapegin.bgp.analyse.visibility.ASsToAnalyse;
import org.sapegin.bgp.analyse.visibility.MonitoredASs;

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
public class BasicCorrelationAnalysisTask<T extends ASsToAnalyse> extends
		AnalyseSpikesTask<T> {

	private MonitoredCorrelatedSpikes<T> correlatedSpikes;

	private T visibleASs;

	private String classificationResultsFilename;

	private Updates allUpdates;

	private int threads;
	private double threshold;
	private int startInterval;
	private int endInterval;

	protected BasicCorrelationAnalysisTask() {
	}

	/*
	 * This constructor could receive different factories, which produce
	 * different number of updates to analyse. E.g., updated from all
	 * (monitored) or only from visible ASs. However, the correlation will be
	 * always calculated against all monitored updates.
	 * 
	 * For example, if there are 10 spikes from visibles ASs and 100 spikes
	 * (including those 10) from all monitored ASs, then only 10 spikes will be
	 * classified, but during the classification the group of spikes correlated
	 * with the given one will be selected from all 100 spikes.
	 */
	public BasicCorrelationAnalysisTask(Properties properties,
			ASsFactory<T> factoryASs) throws Exception {
		// initialise super class and read common properties
		super(properties, factoryASs);

		this.threads = Integer.parseInt(properties.getProperty("threads", "1"));
		this.startInterval = Integer.parseInt(properties.getProperty(
				"start_interval", "1"));
		this.endInterval = Integer.parseInt(properties.getProperty(
				"end_interval", "203"));
		this.threshold = Double.parseDouble(properties.getProperty(
				"basic_threshold", "0.1"));

		// read all names of monitored ASs
		logger.info("loading AS names...");
		ASsNames allASs = new ASsNames(inputASsFilenames);

		// select visible ASs with visibilityPercent
		// I don't know if i will get visible or monitored ASs from generic
		// factory, so I just call all of them visible
		visibleASs = factoryASs.create(iMap, allASs, 1, threads);

		logger.info(visibleASs.getVisibleASsNames().size()
				+ " ASs left after calling visibleASs factory.");

		this.classificationResultsFilename = properties.getProperty(
				"basic_classification_results_filename",
				"basic_classification_" + timeBuffer + "_"
						+ duplicationPercentage + "_" + threshold);

		UpdatesFactory<ASsToAnalyse> updatesFactory = new UpdatesFactory<ASsToAnalyse>();

		logger.info("loading visible RIBs and updates...");
		Boolean correlated = properties.getProperty("Analysis_type").equals(
				"correlated");
		// updates to be classified
		allUpdates = updatesFactory.createUpdates(useRIBs, inputRIBsFilenames,
				componentOnly, correlated, visibleASs, inputUpdatesFilenames,
				synchronise);

		// updates for searching of correlated spikes, in visible mode only
		Updates allUpdatesFromAllASs = null;
		if (properties.getProperty("AS_type_to_analyse", "visible").equals(
				"visible")) {
			logger.info("Loading all updates from all ASs. This is needed to find spikes, correlated with spikes from fully visible ASs.");

			MonitoredASs allMonitoredASs = new MonitoredASsFactory().create(
					iMap, allASs, 1, threads);

			UpdatesFactory<MonitoredASs> allUpdatesFactory = new UpdatesFactory<MonitoredASs>();

			// all updates from all monitored ASs to check for correlation with
			// previous updates
			allUpdatesFromAllASs = allUpdatesFactory.createUpdates(useRIBs,
					inputRIBsFilenames, componentOnly, correlated,
					allMonitoredASs, inputUpdatesFilenames, synchronise);
		}

		correlatedSpikes = new MonitoredCorrelatedSpikes<T>(this.threads,
				this.startInterval, this.endInterval, iMap, allUpdates,
				allUpdatesFromAllASs, timeBuffer, duplicationPercentage,
				visibleASs, true, threshold);
	}

	@Override
	public void selectVisibleDuplicatedSpikes() throws Exception {
		correlatedSpikes.startThreads();
	}

	@Override
	public void writeResults() {
		try {
			correlatedSpikes.writeResults(this.classificationResultsFilename,
					null, null);
		} catch (IOException e) {
			logger.error("Exception during writing results", e);
		}
	}

}
