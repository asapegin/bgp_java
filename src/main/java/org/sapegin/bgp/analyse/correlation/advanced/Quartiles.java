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
 * container for quartiles' values for some distribution
 *
 */
public class Quartiles {
	private long min;
	private long max;
	private float mediana;
	private float firstQuartile;
	private float thirdQuartile;
	
	public Quartiles(long min, float firstQuartile, float mediana, float thirdQuartile, long max){
		this.min = min;
		this.firstQuartile = firstQuartile;
		this.mediana = mediana;
		this.thirdQuartile = thirdQuartile;
		this.max = max;
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	public float getMediana() {
		return mediana;
	}

	public float getFirstQuartile() {
		return firstQuartile;
	}

	public float getThirdQuartile() {
		return thirdQuartile;
	}
}
