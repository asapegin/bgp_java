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
 * 
 * @author Andrey Sapegin
 * 
 *         Class containing graph with all monitored ASs. As all ASs received as
 *         constructor parameter are already monitored, this class just contains
 *         their graph representation.
 * 
 */
public class MonitoredASs extends ASsToAnalyse {

	public MonitoredASs(InternetMap iMap, ASsNames realASsNames, int threads) {
		
		this.realASsNames = realASsNames;
		this.iMap = iMap;
		this.visibilities = new ConcurrentHashMap<Integer,Float>(threads, 0.75f, threads);

		findVisibleASs();
	}	
	

	/**
	 * Creates a graph with all (and only) ASs from realASsNames.
	 */
	@Override
	protected void findVisibleASs() {
		graphAS = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);

		// for every real AS name
		for (Integer nameAS : realASsNames.getASsNames()) {
			// add to list
			graphAS.addVertex(nameAS);
		}

		// add edges between visible ASs
		for (int monitoredAS1 : graphAS.vertexSet()) { // go
			for (int monitoredAS2 : graphAS.vertexSet()) { // through
																// all
																// verticies
				if (monitoredAS1 != monitoredAS2) {
					if (iMap.areConnected(monitoredAS1, monitoredAS2)) { // if
																			// they
																			// are
																			// connected
						graphAS.addEdge(monitoredAS1, monitoredAS2); // add
																		// edge
					}
				}
			}
		}
	}
}
