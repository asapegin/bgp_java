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
package org.sapegin.bgp.analyse.spikes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.random.RandomDataImpl;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         This class represents collection of preselected spikes and allows to
 *         get specific spikes (e.g., defined number of random spikes) from this
 *         collection
 * 
 */
public class SelectedSpikes extends SpikeCollection {

	protected SelectedSpikes() {
	}

	public SelectedSpikes(Map<MonitoredAS, SingleASspikes> spikes) {
		this.allUpdates = spikes;
	}

	public SelectedSpikes(SpikeCollection collection) {
		this.allUpdates = collection.allUpdates;
	}

	public int numberOfSpikes() {
		int num = 0;

		for (SingleASspikes spikes : this.allUpdates.values()) {
			num = num + spikes.getNumberOfSpikes();
		}

		return num;
	}
	
	public long numberOfUpdates(){
		long num = 0;

		for (SingleASspikes spikes : this.allUpdates.values()) {
			num = num + spikes.getCurrentUpdateSum();
		}

		return num;
	}

	// get spikes with order number in the defined interval
	public SpikeCollection getSpikes(int min, int max) {
		SpikeCollection result = new SpikeCollection();

		int cursor = 0;

		// go through sets of spikes for each monitored AS
		for (MonitoredAS as : this.allUpdates.keySet()) {

			// calculate starting number for spike set for current monitored AS.
			int asMin = min - cursor;
			if (asMin < 1) { // if cursor position is after min, start from 1st
								// element in set
				asMin = 1;
			}

			// calculate last number for spike set for current monitored AS
			int asMax = max - cursor;

			if (asMax < 1) { // if position of cursor is greater then max - all
								// elements were already selected
				break;
			}

			// set the cursor to the last element in spike set for current
			// monitored AS
			cursor = cursor + this.allUpdates.get(as).getNumberOfSpikes();

			if (cursor >= min) { // if the absolute number of the last spike in
									// set is bigger than min
				if (cursor <= max) { // if the absolute number is less than max
					// select the elements up to the last in the set for current
					// AS
					asMax = this.allUpdates.get(as).getNumberOfSpikes();
				}

				HashMap<Long,Spike> spikes = this.allUpdates.get(
						as).getSpikes(asMin, asMax);
				for (Long time: spikes.keySet()){
				result.addSpike(time, spikes.get(time), as);
				}
			}
		}

		return result;
	}

	/**
	 * get <numberOfRandomSpikesToAnalyse> random spikes from spike collection
	 * 
	 * @param numberOfRandomSpikesToAnalyse
	 * @return
	 */
	public SpikeCollection getRandom(int numberOfRandomSpikesToAnalyse) {

		SpikeCollection randomSpikes = new SpikeCollection();

		RandomDataImpl generator = new RandomDataImpl();

		// find total number of spikes
		int total = 0;
		for (SingleASspikes asSpikes : allUpdates.values()) {
			total = total + asSpikes.getNumberOfSpikes();
		}

		// check if I have enough spikes
		if (numberOfRandomSpikesToAnalyse > total) {
			numberOfRandomSpikesToAnalyse = total;
		}

		// get needed number of random spikes
		int selected = 0;
		ArrayList<MonitoredAS> monitoredASs = new ArrayList<MonitoredAS>(
				allUpdates.keySet());

		while (selected < numberOfRandomSpikesToAnalyse) {
			// get random key
			MonitoredAS as;
			if (monitoredASs.size() > 1) {
				as = monitoredASs.get(generator.nextInt(0,
						monitoredASs.size() - 1));
			} else {
				as = monitoredASs.get(0);
			}

			// get random spike
			SingleASspikes oneASspikes = allUpdates.get(as);
			// ArrayList<Long> seconds = new
			// ArrayList<Long>(oneASspikes.keySet());

			long time;
			long minTime = oneASspikes.getCurrentMinTime();
			long maxTime = oneASspikes.getCurrentMaxTime();
			if (minTime < maxTime) {
				time = generator.nextLong(oneASspikes.getCurrentMinTime(),
						oneASspikes.getCurrentMaxTime());
			} else {
				time = minTime;
			}

			if (oneASspikes.hasSpikeAtTime(time)) {
				Spike randomSpike = new Spike(oneASspikes.getSpikeAtTime(time)
						.copyPrefixSet());

				// add it to map with all random spikes
				if (randomSpikes.addSpike(time, randomSpike, as)) {
					selected++;
				}
			}
		}

		return randomSpikes;
	}
}
