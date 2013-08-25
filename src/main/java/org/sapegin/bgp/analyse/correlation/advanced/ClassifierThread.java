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

import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.correlation.ClassificationResults;
import org.sapegin.bgp.analyse.correlation.DuplicatedSpikesGroup;
import org.sapegin.bgp.analyse.correlation.basic.BasicClassificationResult;
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
 *         contains a single thread for classifying a subset of spikes into
 *         correlated and not correlated
 * 
 */
public class ClassifierThread implements Callable<ClassificationResults> {

	private Logger logger = LogManager.getLogger(ClassifierThread.class);
	private double duplicationPercentage;
	private Map<MonitoredAS, SingleASspikes> selectedSpikes;
	private Updates allUpdates;
	private long timeBuffer;
	private InternetMap iMap;
	private ASsToAnalyse visibleASs;
	// should be only the basic classification performed?
	private Boolean basic;
	private Double basicThreshold;

	public ClassifierThread(double duplicationPercentage,
			SpikeCollection spikes, ASsToAnalyse visibleASs,
			Updates allUpdates, long timeBuffer, InternetMap iMap,
			Boolean basic, Double basicThreshold) {
		this.duplicationPercentage = duplicationPercentage;
		this.selectedSpikes = spikes.getUpdateMap();
		this.allUpdates = allUpdates;
		this.timeBuffer = timeBuffer;
		this.iMap = iMap;
		this.visibleASs = visibleASs;
		this.basic = basic;
		this.basicThreshold = basicThreshold;
	}

	@Override
	public ClassificationResults call() throws Exception {

		ClassificationResults results = new ClassificationResults();

		logger.info("Starting classification of the subset of selected spikes");
		int count = 0;
		for (SingleASspikes spikes : selectedSpikes.values()) {
			count += spikes.getNumberOfSpikes();
		}
		logger.info(count + " spikes will be analysed");

		// for every monitor
		for (MonitoredAS monitoredAS : selectedSpikes.keySet()) {
			Map<Long, Spike> spikes = selectedSpikes.get(monitoredAS)
					.getAllSpikes();

			// for every spike
			for (Long time : spikes.keySet()) {

				// create a group of spike, correlated with the given one, if
				// any
				DuplicatedSpikesGroup dgroup = new DuplicatedSpikesGroup(
						monitoredAS, spikes.get(time), time, allUpdates, iMap,
						timeBuffer, duplicationPercentage);

				// classify spike
				if (!this.basic) {
					// perform advanced classification
					AdvancedClassificationResult result = dgroup
							.classify(visibleASs);

					// save classification results
					results.addResult(result);
				} else {
					// perform basic classification
					BasicClassificationResult result = dgroup
							.classifyBasic(basicThreshold);

					// save classification results
					results.addBasicResult(result);
				}

			}
		}

		logger.info("thread finished. returning results...");

		// when ready, return results of classification for the subset of spikes
		return results;
	}

}
