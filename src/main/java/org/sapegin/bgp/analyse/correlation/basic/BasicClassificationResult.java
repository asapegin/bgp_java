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


/**
 * 
 * @author Andrey Sapegin
 * 
 *         This class presents a result of the basic classification for ONE
 *         spike.
 * 
 */
public class BasicClassificationResult {

	// true if spike was classified as single
	// false - if as correlated (duplicated)
	private boolean single;

	// number of visible neighbours for AS sent the given spike / total number
	// of neighbours of this AS
	private Float visibility;

	// number of prefixes in spike
	private Integer size;

	// number of prefixes in spike, that were ACTUALLY MARKED as correlated
	private Integer correlatedPrefixesPortion;

	protected BasicClassificationResult() {
	}

	public BasicClassificationResult(boolean single, int size,
			Float visibility, Integer correlatedPrefixesPortion) {
		this.single = single;
		this.size = size;
		this.visibility = visibility;
		this.correlatedPrefixesPortion = correlatedPrefixesPortion;
	}

	public boolean isSingle() {
		return single;
	}

	public float getVisibility() {
		return visibility;
	}

	/**
	 * Returns number of prefixes actually marked as correlated during the
	 * analysis of this spike.
	 * 
	 * @return
	 */
	public Integer getCorrelatedPortion() {
		return correlatedPrefixesPortion;
	}

	public Integer getSize() {
		return size;
	}

}
