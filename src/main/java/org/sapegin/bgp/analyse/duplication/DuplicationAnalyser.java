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

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;

import org.sapegin.bgp.analyse.spikes.Destination;
import org.sapegin.bgp.analyse.spikes.MonitoredAS;
import org.sapegin.bgp.analyse.spikes.SingleASspikes;
import org.sapegin.bgp.analyse.spikes.Spike;
import org.sapegin.bgp.analyse.updates.Updates;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         calculates and stores the rates of duplicated one-second spikes in
 *         BGP updates from different monitors. For duplicated spikes it finds
 *         the number of duplicated prefixes in such spike (less than number of
 *         all prefixes in spike concerned duplicated)
 * 
 */
public class DuplicationAnalyser {

	private Updates updates;
	private long timeBuffer;
	private double duplicationPercentage;
	private DuplicationStats stats;

	protected DuplicationAnalyser(){}
	
	public DuplicationAnalyser(Updates updates, long timeBuffer,
			double duplicationPercentage) {
		this.updates = updates;
		this.timeBuffer = timeBuffer;
		this.duplicationPercentage = duplicationPercentage;
		this.stats = new DuplicationStats();
	}

	/**
	 * This procedure calculate rates of duplicated spikes from different
	 * monitors (monitoredASs)
	 */
	public void calculateDuplication() {

		Map<MonitoredAS, SingleASspikes> allUpdates = updates.getUpdateMap();

		// for every monitor
		for (MonitoredAS monitoredAS : allUpdates.keySet()) {
			// get all spikes
			Map<Long, Spike> spikes = allUpdates.get(monitoredAS)
					.getAllSpikes();

			// for every spike
			for (Long time : spikes.keySet()) {
				Spike spike = spikes.get(time);

				// get number of unique duplicated (only with DUPLICATED spikes from other
				// monitors) prefixes in this spike
				Set<InetAddress> uniqueDuplicatedPrefixes = this.updates
						.getDuplicatedPrefixesInSpikeIfSpikeIsDuplicated(spike,
								time, timeBuffer, monitoredAS,
								duplicationPercentage);
				
				
				
				// get number of unique duplicated (only with spikes from other
				// monitors) prefixes in this spike
				Set<InetAddress> allUniqueDuplicatedPrefixes = this.updates
						.getAllDuplicatedPrefixesInSpike(spike,
								time, timeBuffer, monitoredAS,
								duplicationPercentage);
				
				// find number of duplicated prefixes
				int allDuplicatedPrefixes = 0;
				for (Destination destination : spike.copyPrefixSet()) {
					if (allUniqueDuplicatedPrefixes.contains(destination
							.getPrefix())) {
						allDuplicatedPrefixes++;
					}
				}

					
					
				// if spike was classified as duplicated, the number of unique
				// duplicated will be > 0.
				if (uniqueDuplicatedPrefixes.size() == 0) {
					// spike is single
					this.stats.addSingle(spike.getSpikeSize(),allDuplicatedPrefixes);
				} else {
					// spike is duplicated

					// find number of duplicated prefixes
					int duplicatedPrefixes = 0;
					for (Destination destination : spike.copyPrefixSet()) {
						if (uniqueDuplicatedPrefixes.contains(destination
								.getPrefix())) {
							duplicatedPrefixes++;
						}
					}

					// update stats
					this.stats.addDuplicated(spike.getSpikeSize(),
							duplicatedPrefixes, allDuplicatedPrefixes);
				}
			}
		}

	}

	/**
	 * Get rates of duplicated spikes from different monitors
	 * 
	 * @return
	 */
	public DuplicationStats getStats() {
		return this.stats;
	}

}
