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

import java.util.ArrayList;
import java.util.Collections;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         class containing statistics for one class of classified spikes
 * 
 */
public class SpikeClassStats {

	// array with the maximum time difference between two correlated spikes for
	// every group of correlated spikes for this spike class
	private ArrayList<Long> maxGroupTimes = new ArrayList<Long>();

	// array with the number of origin ASs for every group of
	// correlated spikes for this spike class
	private ArrayList<Integer> originASsInGroups = new ArrayList<Integer>();

	private int totalNumberOfSpikes;

	private int totalNumberOfPrefixes;

	public SpikeClassStats() {
	}

	public void addSpikeStats(int size, Long maxTimeDifference,
			Integer originASs) {
		this.totalNumberOfSpikes++;
		this.totalNumberOfPrefixes += size;

		if (maxTimeDifference != null) {
			this.maxGroupTimes.add(maxTimeDifference);
		}

		if (originASs != null) {
			this.originASsInGroups.add(originASs);
		}
	}

	public ArrayList<Long> getMaxGroupTimes() {
		return maxGroupTimes;
	}

	public ArrayList<Integer> getOriginASsInGroups() {
		return originASsInGroups;
	}

	public int getTotalNumberOfSpikes() {
		return totalNumberOfSpikes;
	}

	public int getTotalNumberOfPrefixes() {
		return totalNumberOfPrefixes;
	}

	public void mergeWith(SpikeClassStats stats) {
		this.maxGroupTimes.addAll(stats.getMaxGroupTimes());
		this.originASsInGroups.addAll(stats.getOriginASsInGroups());
		this.totalNumberOfPrefixes += stats.getTotalNumberOfPrefixes();
		this.totalNumberOfSpikes += stats.getTotalNumberOfSpikes();
	}

	/**
	 * calculates and returns quartiles for maximum inter-arriveal times
	 * 
	 * @return
	 */
	public Quartiles getTimeQuartiles() {
		Collections.sort(this.maxGroupTimes);

		// first quartile
		double np = this.maxGroupTimes.size() * 0.25;

		int j = (int) np;
		double g = np - j;
		j = j - 1; // numeration in the array starts at 0. So if j=1, in need to
					// get element number 0.

		float firstQuartile;
		if (g == 0) {
			firstQuartile = (this.maxGroupTimes.get(j) + this.maxGroupTimes
					.get(j + 1)) / (float) 2;
		} else {
			firstQuartile = this.maxGroupTimes.get(j + 1);
		}

		// mediana
		np = this.maxGroupTimes.size() * 0.5;

		j = (int) np;
		g = np - j;
		j = j - 1; // numeration in the array starts at 0. So if j=1, in need to
					// get element number 0.

		float mediana;
		if (g == 0) {
			mediana = (this.maxGroupTimes.get(j) + this.maxGroupTimes
					.get(j + 1)) / (float) 2;
		} else {
			mediana = this.maxGroupTimes.get(j + 1);
		}

		// third quartile
		np = this.maxGroupTimes.size() * 0.75;

		j = (int) np;
		g = np - j;
		j = j - 1; // numeration in the array starts at 0. So if j=1, in need to
					// get element number 0.

		float thirdQuartile;
		if (g == 0) {
			thirdQuartile = (this.maxGroupTimes.get(j) + this.maxGroupTimes
					.get(j + 1)) / (float) 2;
		} else {
			thirdQuartile = this.maxGroupTimes.get(j + 1);
		}

		// min
		long min = this.maxGroupTimes.get(0);

		// max
		long max = this.maxGroupTimes.get(this.maxGroupTimes.size() - 1);

		return new Quartiles(min, firstQuartile, mediana, thirdQuartile, max);

	}

	/**
	 * Calculates and returns quartiles for number of origin ASs
	 * 
	 * @return
	 */
	public Quartiles getOriginsQuartiles() {
		if(originASsInGroups.size()==0){
			return null;
		}
		
		Collections.sort(this.originASsInGroups);

		// first quartile
		double np = this.originASsInGroups.size() * 0.25;

		int j = ((int) np);
		double g = np - j;
		j = j - 1; // numeration in the array starts at 0. So if j=1, in need to
					// get element number 0.

		float firstQuartile;
		if (g == 0) {
			firstQuartile = (this.originASsInGroups.get(j) + this.originASsInGroups
					.get(j + 1)) / (float) 2;
		} else {
			firstQuartile = this.originASsInGroups.get(j + 1);
		}

		// mediana
		np = this.originASsInGroups.size() * 0.5;

		j = (int) np;
		g = np - j;
		j = j - 1; // numeration in the array starts at 0. So if j=1, in need to
					// get element number 0.

		float mediana;
		if (g == 0) {
			mediana = (this.originASsInGroups.get(j) + this.originASsInGroups
					.get(j + 1)) / (float) 2;
		} else {
			mediana = this.originASsInGroups.get(j + 1);
		}

		// third quartile
		np = this.originASsInGroups.size() * 0.75;

		j = (int) np;
		g = np - j;
		j = j - 1; // numeration in the array starts at 0. So if j=1, in need to
					// get element number 0.

		float thirdQuartile;
		if (g == 0) {
			thirdQuartile = (this.originASsInGroups.get(j) + this.originASsInGroups
					.get(j + 1)) / (float) 2;
		} else {
			thirdQuartile = this.originASsInGroups.get(j + 1);
		}

		// min
		long min = this.originASsInGroups.get(0);

		// max
		long max = this.originASsInGroups
				.get(this.originASsInGroups.size() - 1);

		return new Quartiles(min, firstQuartile, mediana, thirdQuartile, max);
	}
}
