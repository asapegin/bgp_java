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

/**
 * 
 * @author Andrey Sapegin
 * 
 * This class presents a classification result for ONE spike.
 *
 */
public class AdvancedClassificationResult {

	// true if spike was classified as single
	// false - if as correlated (duplicated)
	private boolean single;
	
	// number of visible neighbours for AS sent the given spike / total number
	// of neighbours of this AS
	private Float visibility;
	
	// maximum topology distance between two ASs sent spikes in the group of
	// spikes correlated with the given one
	// 127 == infinity !
	private Byte maxDistance;
	
	// maximum time difference between first and last spikes in the group of
	// spikes correlated with the given one
	private Long maxTimeDifference;
	
	// number of originating ASs for all prefixes in the spike
	private Integer originASs;
	
	// number of prefixes in spike
	private Integer size;
	
	protected AdvancedClassificationResult(){}
	
	public AdvancedClassificationResult(boolean single, int size, Float visibility, Byte maxDistance, Long maxTimeDifference, Integer originASs){
		this.single = single;
		this.size = size;
		this.visibility = visibility;
		this.maxDistance = maxDistance;
		this.maxTimeDifference = maxTimeDifference;
		this.originASs = originASs;
	}

	public boolean isSingle() {
		return single;
	}

	public float getVisibility() {
		return visibility;
	}

	public Byte getMaxDistance() {
		return maxDistance;
	}

	public Long getMaxTimeDifference() {
		return maxTimeDifference;
	}

	public int getOriginASs() {
		return originASs;
	}

	public Integer getSize() {
		return size;
	}
}
