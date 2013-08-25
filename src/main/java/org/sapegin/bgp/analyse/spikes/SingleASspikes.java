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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         This class contains map with spikes from one and only one on pair
 *         {monitored AS,monitoring router}
 */
public class SingleASspikes {
	// for every key (timestamp in Unix-time) the Spike (all prefixes for that
	// timestamp) will be stored as Value
	private Map<Long, Spike> oneSecondSpikes;

	public SingleASspikes() {
		this.oneSecondSpikes = new HashMap<Long, Spike>();
	}

	public SingleASspikes(HashMap<Long, Spike> spikes) {
		this.oneSecondSpikes = spikes;
	}

	/**
	 * This method adds new Spike {spike} at time {time} to the Map of one
	 * second spikes.
	 * 
	 * @param spike
	 *            - spike - set of prefixes received from one AS at the time
	 *            {time}
	 * @param time
	 *            - time of spike in Unix format
	 */
	public boolean addSpike(long time, Spike spike) {
		if (spike == null) {
			return false;
		} else {
			if (oneSecondSpikes.containsKey(time)) {
				return false;
			} else {
				oneSecondSpikes.put(time, spike);
				return true;
			}
		}
	}

	public Map<Long, Spike> getAllSpikes() {
		return oneSecondSpikes;
	}

	public HashMap<Long, Spike> getSpikes(int from, int to) {
		HashMap<Long, Spike> result = new HashMap<Long, Spike>();

		if ((from < 1) || (to > oneSecondSpikes.size())) {
			return null;
		}

		ArrayList<Long> keys = new ArrayList<Long>(oneSecondSpikes.keySet());

		for (int i = from; i <= to; i++) {
			long time = keys.get(i - 1);
			Spike spike = new Spike(oneSecondSpikes.get(time).copyPrefixSet());
			result.put(time, spike);
		}

		return result;
	}
	
	public int getNumberOfSpikes() {
		return oneSecondSpikes.size();
	}

	public Spike getCurrentBiggestSpike() {
		Spike biggestSpike = null;
		int biggestSize = 0;

		for (Spike spike : oneSecondSpikes.values()) {
			if (spike.getSpikeSize() > biggestSize) {
				biggestSpike = new Spike(spike.copyPrefixSet());
				biggestSize = spike.getSpikeSize();
			}
		}

		return biggestSpike;
	}

	public long getCurrentUpdateSum() {
		long updatesSum = 0;

		for (Spike spike : oneSecondSpikes.values()) {
			updatesSum += spike.getSpikeSize();
		}

		return updatesSum;
	}

	public long getCurrentMinTime() {
		if (oneSecondSpikes.keySet().size() == 0) {
			return 0;
		} else {
			return Collections.min(oneSecondSpikes.keySet());
		}
	}

	public long getCurrentMaxTime() {
		if (oneSecondSpikes.keySet().size() == 0) {
			return 0;
		} else {
			return Collections.max(oneSecondSpikes.keySet());
		}
	}

	public boolean hasSpikeAtTime(long updateTime) {
		return oneSecondSpikes.containsKey(updateTime);
	}

	public Spike getSpikeAtTime(long updateTime) {
		return oneSecondSpikes.get(updateTime);
	}

	/**
	 * This method DELETES ALL spikes at (time < startTime) or (time > endTime)
	 * 
	 * @param startTime
	 * @param endTime
	 */
	public void synchronise(long startTime, long endTime) {

		if (startTime > endTime) {
			long temp = startTime;
			startTime = endTime;
			endTime = temp;
		}

		// the following code:
		// for (long time : oneSecondSpikes.keySet()) {
		// if ((time < startTime) || (time > endTime)){
		// oneSecondSpikes.remove(time);
		// } }
		// causes ConcurrentModificationException
		// so I rewrite it as:

		Iterator<Entry<Long, Spike>> iterator = oneSecondSpikes.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			Map.Entry<Long, Spike> pair = iterator.next();
			long time = (long) pair.getKey();
			if ((time < startTime) || (time > endTime)) {
				iterator.remove();
			}
		}
	}
}
