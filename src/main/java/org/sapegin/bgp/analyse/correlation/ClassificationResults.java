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
package org.sapegin.bgp.analyse.correlation;

import java.util.HashMap;
import java.util.Map;

import org.sapegin.bgp.analyse.correlation.advanced.AdvancedClassificationResult;
import org.sapegin.bgp.analyse.correlation.advanced.SpikeClassStats;
import org.sapegin.bgp.analyse.correlation.basic.BasicClassificationResult;
import org.sapegin.bgp.analyse.duplication.DuplicationStats;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         class presents a container for classification results. It could
 *         represent both results calculated by 1 thread and merged results for
 *         all threads
 * 
 *         This class should have a method merge to merge results into one
 *         Result class And a method writeResults too.
 * 
 */
public class ClassificationResults {
	
	// fields for basic classification:
	private DuplicationStats basicStats = new DuplicationStats();
	
	// fields for advanced classification:

	// Statistics for single spikes with different visibility
	private SpikeClassStats singleSpikesMax033Visible = new SpikeClassStats();
	private SpikeClassStats singleSpikesMax066Visible = new SpikeClassStats();
	private SpikeClassStats singleSpikes100Visible = new SpikeClassStats();

	// Statistics for correlated spikes with different maxDistance in hops
	// key presents a distance in hops
	private Map<Byte, SpikeClassStats> correlatedSpikesStats = new HashMap<Byte, SpikeClassStats>();

	// Statistics for correlated spikes with infinite maxDistance
	private SpikeClassStats otherCorrelatedSpikesStats = new SpikeClassStats();

	public ClassificationResults() {
	}

	/**
	 * Adds a result for one spike to the stored statistics
	 * 
	 * @param result
	 */
	public void addResult(AdvancedClassificationResult result) {
		if (result.isSingle()) {
			if (result.getVisibility() <= 0.33) {
				singleSpikesMax033Visible.addSpikeStats(result.getSize(), null,
						result.getOriginASs());
			} else if (result.getVisibility() <= 0.66) {
				singleSpikesMax066Visible.addSpikeStats(result.getSize(), null,
						result.getOriginASs());
			} else {
				singleSpikes100Visible.addSpikeStats(result.getSize(), null,
						result.getOriginASs());
			}
		} else {
			if (result.getMaxDistance() != null) {
				if (correlatedSpikesStats.get(result.getMaxDistance()) != null) {
					correlatedSpikesStats.get(result.getMaxDistance())
							.addSpikeStats(result.getSize(),
									result.getMaxTimeDifference(),
									result.getOriginASs());
				} else {
					SpikeClassStats initStats = new SpikeClassStats();
					initStats.addSpikeStats(result.getSize(),
							result.getMaxTimeDifference(),
							result.getOriginASs());
					correlatedSpikesStats.put(result.getMaxDistance(),
							initStats);
				}
			} else {
				otherCorrelatedSpikesStats.addSpikeStats(result.getSize(),
						result.getMaxTimeDifference(), result.getOriginASs());
			}
		}
	}

	/**
	 * 
	 * @return statistics stored for single spikes with visibility <= 0.33
	 */
	public SpikeClassStats getSingleSpikesMax033Visible() {
		return singleSpikesMax033Visible;
	}

	/**
	 * 
	 * @return statistics stored for single spikes with 0.33 < visibility <=
	 *         0.66
	 */
	public SpikeClassStats getSingleSpikesMax066Visible() {
		return singleSpikesMax066Visible;
	}

	/**
	 * 
	 * @return statistics stored for single spikes with visibility > 0.66
	 */
	public SpikeClassStats getSingleSpikes100Visible() {
		return singleSpikes100Visible;
	}

	/**
	 * 
	 * @return statistics stored for correlated spikes
	 */
	public Map<Byte, SpikeClassStats> getCorrelatedSpikesStats() {
		return correlatedSpikesStats;
	}

	/**
	 * 
	 * @return statistics stored for correlated spikes with infinite maxDistance
	 */
	public SpikeClassStats getOtherCorrelatedSpikesStats() {
		return otherCorrelatedSpikesStats;
	}

	/**
	 * Adds all statistics from results received as parameter to the already
	 * stored statistics
	 * 
	 * @param results
	 */
	public void merge(ClassificationResults results) {
		
		// merge results of basic classification
		this.basicStats.mergeWith(results.getBasicStats());
		
		// merge results of advanced classification
		this.singleSpikesMax033Visible.mergeWith(results
				.getSingleSpikesMax033Visible());
		this.singleSpikesMax066Visible.mergeWith(results
				.getSingleSpikesMax066Visible());
		this.singleSpikes100Visible.mergeWith(results
				.getSingleSpikes100Visible());

		Map<Byte, SpikeClassStats> statsToMerge = results
				.getCorrelatedSpikesStats();

		for (byte hop : statsToMerge.keySet()) {
			if (this.correlatedSpikesStats.get(hop) == null) {
				this.correlatedSpikesStats.put(hop, statsToMerge.get(hop));
			} else {
				this.correlatedSpikesStats.get(hop).mergeWith(
						statsToMerge.get(hop));
			}
		}

		this.otherCorrelatedSpikesStats.mergeWith(results
				.getOtherCorrelatedSpikesStats());
	}
	
	/**
	 * @return statistics collected during basic correlation analysis 
	 */
	public DuplicationStats getBasicStats() {
		return basicStats;
	}

	/**
	 * Adds results of basic correlation analysis for 1 (ONE) spike
	 * @param result
	 */
	public void addBasicResult(BasicClassificationResult result) {
				if (result.isSingle()){
					this.basicStats.addSingle(result.getSize(), result.getCorrelatedPortion());
				} else{
					this.basicStats.addDuplicated(result.getSize(), 0, result.getCorrelatedPortion());
				}
	}
}
