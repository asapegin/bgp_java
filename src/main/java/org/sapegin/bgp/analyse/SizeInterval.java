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
package org.sapegin.bgp.analyse;


/**
 * 
 * @author Andrey Sapegin
 * 
 *         This class represents the selected for analysis interval of spike
 *         sizes.
 * 
 */
public class SizeInterval {
	
	//private static Logger logger = LogManager.getLogger(SizeInterval.class);
	
	private int minSize;
	private int maxSize;

	public int getMinSize() {
		return minSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * Constructor. It accepts string representation of integer interval, which
	 * should look like 123..234 or 0..15.
	 * 
	 * @param interval
	 */
	public SizeInterval(String interval) {
		String[] strs = interval.split("\\.");
		//logger.trace("Size interval: "+strs.toString());
		// first is min
		minSize = Integer.parseInt(strs[0]);
		// last is max
		maxSize = Integer.parseInt(strs[strs.length - 1]);
	}

}
