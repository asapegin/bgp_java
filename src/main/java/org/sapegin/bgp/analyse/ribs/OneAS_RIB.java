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
package org.sapegin.bgp.analyse.ribs;

import java.util.HashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sapegin.bgp.analyse.spikes.Destination;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         This class contains a part of monitoring router RIB, which contains
 *         only prefixes received from 1 monitored AS
 * 
 */
public class OneAS_RIB {

	// logger
	private Logger logger = LogManager.getLogger(OneAS_RIB.class);

	// part of RIB with prefixes received from 1 monitored (remote) AS
	private HashMap<Destination, ASPath> routesAS;

	public OneAS_RIB() {
		// prefix , AS Path
		routesAS = new HashMap<Destination, ASPath>();
	}

	/**
	 * Adds route to RIB
	 * 
	 * @param prefix
	 * @param asPath
	 */
	public void announce(Destination prefix, ASPath asPath) {
		if (routesAS.get(prefix) != null) {
			logger.trace("AS Path for prefix " + prefix.getPrefix().getHostAddress()
					+ " is already in RIB!");
		}
		routesAS.put(prefix, asPath);
	}

	/**
	 * Removes route from RIB
	 * 
	 * @param prefix
	 */
	public void withdraw(Destination prefix) {
		if (routesAS.get(prefix) == null) {
			logger.trace("AS Path for prefix " + prefix.getPrefix().getHostAddress()
					+ " does not exist in RIB!");
		}
		routesAS.remove(prefix);
	}

	/**
	 * Find AS Path (which is normally not known for withdrawals) for prefix in
	 * RIB
	 * 
	 * @param prefix
	 * @return
	 */
	public ASPath getAS_Path(Destination prefix) {
		return routesAS.get(prefix);
	}

}
