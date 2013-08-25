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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sapegin.bgp.analyse.ribs.ASPathElement;

/**
 * 
 * Class containing prefixes (IP-addresses) for one and only one single spike.
 * It should mean 'updates received from one of ASs by one of monitoring routers
 * (vantage points) at one specific second'
 * 
 * @author Andrey Sapegin
 * 
 */
public class Spike {
	// list containing all prefixes of this spike
	private ArrayList<Destination> destinations;
	// list containing unique (!!!) origin ASs for all destinations
	private Set<ASPathElement> origins;

	// logger
	private Logger logger = LogManager.getLogger(Spike.class);

	public Spike() {
		destinations = new ArrayList<Destination>();
		origins = new HashSet<ASPathElement>();
	}

	public Spike(ArrayList<Destination> prefixes) {
		this.destinations = new ArrayList<Destination>(prefixes);
		this.origins = new HashSet<ASPathElement>();
		
		for (Destination destination: destinations){
			this.origins.add(destination.getOriginAS());
		}
	}
	
	public void addDestination(Destination destination){
		this.destinations.add(destination);
		this.origins.add(destination.getOriginAS());
	}

	/**
	 * This method adds IPv4 prefix (network address) to the spike
	 * 
	 * @param ip
	 *            - IPv4 network address in String format (aaa.bbb.ccc.ddd)
	 */
	public void addPrefix(String ip) {
		try {
			InetAddress prefixWithoutOrigin = InetAddress.getByName(ip);
			Destination destination = new Destination(prefixWithoutOrigin);
			destinations.add(destination);
		} catch (UnknownHostException e) {
			logger.error(
					"Can't convert String "
							+ ip
							+ " into ip address. This ip will not be added to the AS spike.",
					e);
		}
	}

	/**
	 * This method adds IPv4 prefix (network address) to the spike
	 * 
	 * @param ip
	 *            - IPv4 network address as InetAddress
	 */
	public void addPrefix(InetAddress ip) {
		Destination destination = new Destination(ip);
		this.destinations.add(destination);
	}
	
	public int getNumberOfOriginASs(){
		return origins.size();
	}

	public int getSpikeSize() {
		return destinations.size();
	}

	public Boolean containsPrefix(InetAddress ip) {
		for (Destination destination: destinations){
			if (destination.getPrefix().equals(ip)){
				return true;
			}
		}
			return false;
	}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! //	
	/**
	 * Possible mistake here
	 * @return
	 */
	public ArrayList<Destination> copyPrefixSet() {
		return new ArrayList<Destination>(destinations);
	}
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! //
	
	/**
	 * This method determines which of 2 spikes (this spike and <spike> received
	 * as parameter) has less number of prefix updates. Then the procedure will
	 * calculate how many prefixes (%) of first (smaller) spike are also
	 * included in the 2nd (bigger) spike
	 * 
	 * E.g., if first spike has 100 prefixes and 20 of them are included into
	 * updates of second spike, the calculated value will be 20/100=0.2
	 * 
	 * If this value is bigger than or equals <duplicationPercentage>, both
	 * spikes are concerned duplicated and this method will return true.
	 * Otherwise it will return false.
	 * 
	 * @param spike
	 * @param duplicationPercentage
	 * @return
	 */
	public boolean isDuplicatedWith(Spike spike, double duplicationPercentage) {
		if (destinations.size() > spike.getSpikeSize()) {
			// change order of parameters
			return spike.isDuplicatedWith(this, duplicationPercentage);
		} else {
			
			// check if spikes are duplicated
			int duplicated = 0;
			for (Destination destination : destinations) {
				if (spike.containsPrefix(destination.getPrefix())) {
					duplicated++;
				}
			}
			
			if ((duplicated / (float) destinations.size()) >= duplicationPercentage) {
					return true;
				} else {
					return false;
				}
		}
	}

	public Set<InetAddress> getPrefixesDuplicatedWith(
			Spike duplicatedSpike) {
		
		Set<InetAddress> duplicatedPrefixes = new HashSet<InetAddress>();
		
		for (Destination destination: this.destinations){
			if (duplicatedSpike.containsPrefix(destination.getPrefix())){
				duplicatedPrefixes.add(destination.getPrefix());
			}
		}
		
		return duplicatedPrefixes;
	}
}
