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

/**
 * 
 * @author Andrey Sapegin
 * 
 * this class stores statistics for duplication in spikes from different monitors
 *
 */
public class DuplicationStats {

	private int singleSpikes = 0;
	private int prefixesInSingleSpikes = 0;
	private int duplicatedSpikes = 0;
	private int prefixesInDuplicatedSpikes = 0;
	private int duplicatedPrefixesInDuplicatedSpikes = 0;
	private int allDuplicatedPrefixes = 0;
	
	
	public int getTotalNumberOfSpikes() {
		return this.singleSpikes+this.duplicatedSpikes;
	}

	public int getTotalNumberOfPrefixes() {
		return this.prefixesInSingleSpikes+this.prefixesInDuplicatedSpikes;
	}

	public int getNumberOfDuplicatedSpikes() {
		return this.duplicatedSpikes;
	}
	
	public int getNumberOfSingleSpikes() {
		return this.singleSpikes;
	}

	public int getNumberOfPrefixesInDuplicatedSpikes() {
		return this.prefixesInDuplicatedSpikes;
	}
	
	public int getNumberOfPrefixesInSingleSpikes() {
		return this.prefixesInSingleSpikes;
	}

	public int getNumberOfDuplicatedPrefixesInDuplicatedSpikes() {
		return this.duplicatedPrefixesInDuplicatedSpikes;
	}
	
	public int getNumberOfAllDuplicatedPrefixesWithAllSpikes() {
		return this.allDuplicatedPrefixes;
	}

	public void addSingle(int spikeSize, int allDuplicatedPrefixes) {
		this.singleSpikes++;
		this.prefixesInSingleSpikes+=spikeSize;
		this.allDuplicatedPrefixes+=allDuplicatedPrefixes;
	}

	public void addDuplicated(int spikeSize, int duplicatedPrefixes, int allDuplicatedPrefixes) {
		this.duplicatedSpikes++;
		this.prefixesInDuplicatedSpikes+=spikeSize;
		this.duplicatedPrefixesInDuplicatedSpikes+=duplicatedPrefixes;
		this.allDuplicatedPrefixes+=allDuplicatedPrefixes;
	}

	public void mergeWith(DuplicationStats stats) {
		
		this.allDuplicatedPrefixes += stats.getNumberOfAllDuplicatedPrefixesWithAllSpikes();
		this.duplicatedPrefixesInDuplicatedSpikes += stats.getNumberOfDuplicatedPrefixesInDuplicatedSpikes();
		
		this.duplicatedSpikes += stats.getNumberOfDuplicatedSpikes();
		this.prefixesInDuplicatedSpikes += stats.getNumberOfPrefixesInDuplicatedSpikes();
		
		this.singleSpikes += stats.getNumberOfSingleSpikes();
		this.prefixesInSingleSpikes += stats.getNumberOfPrefixesInSingleSpikes();
		
	}

}
