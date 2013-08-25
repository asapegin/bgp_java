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

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sapegin.bgp.analyse.spikes.Destination;
import org.sapegin.bgp.analyse.spikes.MonitoredAS;
import org.sapegin.bgp.analyse.spikes.SingleASspikes;
import org.sapegin.bgp.analyse.spikes.Spike;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         Thread for selecting updates with predefined size from update map
 * 
 *         Attention! this thread "contains a sequence of calls to a concurrent"
 *         hash map. However, it should not be a problem since different threads
 *         work with different monitoredASs.
 * 
 */
public class UpdatesSelector extends Thread {

	// logger
	protected Logger logger = LogManager.getLogger(UpdatesSelector.class);

	private Map<MonitoredAS, SingleASspikes> allUpdates;
	private ConcurrentHashMap<MonitoredAS, SingleASspikes> selectedSpikesWithPredefinedSize;
	private int minSize;
	private int maxSize;
	private int start;
	private int end;

	protected UpdatesSelector() {
	}

	public UpdatesSelector(
			int start,
			int end,
			int minSize,
			int maxSize,
			Map<MonitoredAS, SingleASspikes> allUpdates,
			ConcurrentHashMap<MonitoredAS, SingleASspikes> selectedSpikesWithPredefinedSize) {
		this.allUpdates = allUpdates;
		this.selectedSpikesWithPredefinedSize = selectedSpikesWithPredefinedSize;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.start = start;
		this.end = end;
	}

	@Override
	public void run() {
		int selected = 0;

		ArrayList<MonitoredAS> monitoredASs = new ArrayList<MonitoredAS>(
				allUpdates.keySet());

		for (int i = start; i <= end; i++) {

			MonitoredAS monitoredAS = monitoredASs.get(i - 1);

			SingleASspikes spikes = allUpdates.get(monitoredAS);

			if (spikes.getCurrentUpdateSum() == 0) {
				continue;
			}

			for (Long time = spikes.getCurrentMinTime(); time <= spikes
					.getCurrentMaxTime(); time++) {
				if (spikes.hasSpikeAtTime(time)) {

					int size = spikes.getSpikeAtTime(time).getSpikeSize();
					if ((size < maxSize) && (size >= minSize)) {// select spike

						// Attention! this part of code
						// "contains a sequence of calls to a concurrent" hash
						// map. However, it should not be a problem since
						// different threads work with different monitoredASs

						// check if there are already selected spikes from this
						// monitoredAS
						if (!selectedSpikesWithPredefinedSize
								.containsKey(monitoredAS)) {
							// create hash map entry for monitoredAS with empty
							// spikes
							// I will add spikes at the code below
							selectedSpikesWithPredefinedSize.put(monitoredAS,
									new SingleASspikes());// need
							// to
							// copy
							// monitoredAS?
						}

						// copy spike object
						ArrayList<Destination> destinations = spikes
								.getSpikeAtTime(time).copyPrefixSet();
						Spike selectedSpike = new Spike(destinations);

						// add spike to selected (entry with monitored AS is
						// already created)
						selectedSpikesWithPredefinedSize.get(monitoredAS)
								.addSpike(time, selectedSpike);

						selected++;
					}
				}
			}
		}

		logger.info(selected + " spikes with size from " + minSize + " to "
				+ (maxSize - 1) + " selected by current thread.");
	}
}
