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
package org.sapegin.bgp.analyse.visibility;

import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sapegin.bgp.analyse.Colours;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.SizeInterval;
import org.sapegin.bgp.analyse.correlation.DuplicatedSpikesGroup;
import org.sapegin.bgp.analyse.spikes.MonitoredAS;
import org.sapegin.bgp.analyse.spikes.SelectedSpikes;
import org.sapegin.bgp.analyse.spikes.SingleASspikes;
import org.sapegin.bgp.analyse.spikes.Spike;
import org.sapegin.bgp.analyse.updates.Updates;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         This class selects spikes from Updates (spike collection expected but
 *         not forced to contain only updates from visible or monitored ASs)
 *         received as parameter for analysis and analyses them using concept of
 *         DuplicatedSpikesGroup
 */
public class VisibleDuplicatedSpikes<T extends ASsToAnalyse> {

	private ArrayList<DuplicatedSpikesGroup> groups;
	private Updates allUpdates;
	private Colours colours;
	private T visibleASs;
	private Boolean componentOnly;

	private Logger logger = LogManager.getLogger(VisibleDuplicatedSpikes.class);

	public VisibleDuplicatedSpikes(InternetMap iMap, Updates allUpdates,
			ArrayList<SizeInterval> sizeIntervals,
			int numberOfRandomSpikesToAnalyse, long spikeTime, int spikeAS,
			String spikeMonitor, long timeBuffer, double duplicationPercentage,
			Colours colours, T visibleASs, boolean componentOnly)
			throws Exception {
		groups = new ArrayList<DuplicatedSpikesGroup>();
		this.allUpdates = allUpdates;
		this.colours = colours;
		this.visibleASs = visibleASs;
		this.componentOnly = componentOnly;

		if (numberOfRandomSpikesToAnalyse > 0) {
			for (SizeInterval sizeInterval : sizeIntervals) {
				logger.info("retrieving spikes with size from "
						+ sizeInterval.getMinSize() + " to "
						+ sizeInterval.getMaxSize() + "...");

				// get all spikes with sizes from sizeInterval
				SelectedSpikes selectedSpikes = new SelectedSpikes(
						allUpdates.getSpikesWithPredefinedSize(
								sizeInterval.getMinSize(),
								sizeInterval.getMaxSize(), 2));

				// randomly select several spikes for analysis
				Map<MonitoredAS, SingleASspikes> selectedRandomSpikes = selectedSpikes
						.getRandom(numberOfRandomSpikesToAnalyse)
						.getUpdateMap();

				for (MonitoredAS as : selectedRandomSpikes.keySet()) {
					SingleASspikes oneSecondSpikes = selectedRandomSpikes
							.get(as);
					for (long time = oneSecondSpikes.getCurrentMinTime(); time <= oneSecondSpikes
							.getCurrentMaxTime(); time++) {
						if (oneSecondSpikes.hasSpikeAtTime(time)) {

							Spike spike = oneSecondSpikes.getSpikeAtTime(time);

							// find a group of spikes duplicated with the given
							DuplicatedSpikesGroup dGroup = new DuplicatedSpikesGroup(
									as, spike, time, allUpdates, iMap,
									timeBuffer, duplicationPercentage);

							groups.add(dGroup);
						}
					}
				}
			}
		} else { // analyse specified spike ONLY!

			logger.info("Selecting specified spike for analysis.");

			MonitoredAS spikeMonitoredAS = new MonitoredAS(spikeMonitor,
					spikeAS);
			Spike spike = allUpdates.getUpdateMap().get(spikeMonitoredAS)
					.getSpikeAtTime(spikeTime);

			if (spike == null) {
				logger.fatal("No such spike!");
				throw new Exception("Specified spike not found.");
			}

			logger.info("time: " + spikeTime + " AS: " + spikeAS + " monitor: "
					+ spikeMonitor + " size: " + spike.getSpikeSize());

			DuplicatedSpikesGroup dGroup = new DuplicatedSpikesGroup(
					spikeMonitoredAS, spike, spikeTime, allUpdates, iMap,
					timeBuffer, duplicationPercentage);
			groups.add(dGroup);
		}
	}

	/**
	 * Writes results of the analysis (as graphs in DOT format and text data for
	 * plots).
	 * 
	 * @param time_interval
	 */
	public void writeResults(String updatesChartFolder,
			String groupChartFolder, String groupGraphFolder, long timeBuffer) {
		for (DuplicatedSpikesGroup dGroup : groups) {
			// plot all updates around the given spike
			allUpdates.printAllAround(updatesChartFolder, dGroup.getSpike(),
					dGroup.getSpikeTime(), timeBuffer, colours);

			// plot only duplicated spikes around the given spike
			dGroup.printAllAround(groupChartFolder, colours);

			// create a graph which should show how the given spike has appeared
			dGroup.printDOTGraph(groupGraphFolder, visibleASs, componentOnly);
		}
	}

}
