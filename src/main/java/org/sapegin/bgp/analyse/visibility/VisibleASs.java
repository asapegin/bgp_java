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
package org.sapegin.bgp.analyse.visibility;

import java.util.concurrent.ConcurrentHashMap;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.InternetMap;

/**
 * Class containing names of visible ASs and methods to find them.
 * 
 * @author Andrey Sapegin
 * 
 */
public class VisibleASs extends ASsToAnalyse {

	/**
	 * Constructor
	 * 
	 * Visible ASs - monitored ASs, with {visibilityPercent}% of neighbours
	 * monitored; i.e. {visibilityPercent}% of neighbours should be also
	 * included into the list of monitored ASs
	 * 
	 * @param iMap
	 *            - map of the Internet. Will be used to find visible ASs.
	 * @param realASsNames
	 *            - List of monitored ASs among which visible ASs could be
	 *            found.
	 * @param visibilityPercent
	 *            - percentage of concerned AS's neighbours which should be
	 *            monitored to assert AS as visible.
	 */
	public VisibleASs(InternetMap iMap, ASsNames realASsNames,
			float visibilityPercent, int threads) {
		this.realASsNames = realASsNames;
		this.iMap = iMap;
		this.visibilityPercent = visibilityPercent;
		this.visibilities = new ConcurrentHashMap<Integer,Float>(threads, 0.75f, threads);

		findVisibleASs();
	}

	/**
	 * This method selects only visible ASs from real ASs names (set of all
	 * monitored ASs).
	 */
	protected void findVisibleASs() {
		graphAS = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);

		// for every real AS name
		for (Integer nameAS : realASsNames.getASsNames()) {
			// check if it is visible
			if (isVisible(nameAS)) {
				// if yes - add to list
				graphAS.addVertex(nameAS);
			}
		}

		// add edges between visible ASs
		for (int visibleAS1 : graphAS.vertexSet()) { // go
			for (int visibleAS2 : graphAS.vertexSet()) { // through
																// all
																// verticies
				if (visibleAS1 != visibleAS2) {
					if (iMap.areConnected(visibleAS1, visibleAS2)) { // if they
																		// are
																		// connected
						graphAS.addEdge(visibleAS1, visibleAS2); // add
																	// edge
					}
				}
			}
		}
	}

}
