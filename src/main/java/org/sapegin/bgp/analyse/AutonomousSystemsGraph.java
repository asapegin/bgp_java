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

import java.util.ArrayList;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * This class is a base class for all objects representing maps and/or graphs
 * of ASs and connections between them
 * 
 * @author Andrey Sapegin
 */
public class AutonomousSystemsGraph {
	// I will store map as SimpleGraph with integer vertices (names of
	// ASs).
	protected SimpleGraph<Integer, DefaultEdge> graphAS = new SimpleGraph<Integer, DefaultEdge>(
			DefaultEdge.class);
	
	/**
	 * This class should be used only as Parent, so let's hide
	 * the constructor
	 */
	protected AutonomousSystemsGraph(){}
	
	/**
	 * This method checks if 2 verticies are connected
	 * @param vertex1
	 * @param vertex2
	 * @return boolean - true if verticies are connected, false if not.
	 */
	public boolean areConnected(Integer vertex1, Integer vertex2) {
		return graphAS.containsEdge(vertex1, vertex2);
	}
	
	/**
	 * This method
	 * 
	 * @param nameAS - name of AS for which a list of neighbours will be found and returned
	 * @return ArrayList&lt;Integer&gt; - list of names of neighbouring ASs for
	 *         {nameAS} AS according to the current map. if {nameAS} will not be
	 *         found in the map, null will be returned
	 */
	public ArrayList<Integer> getNeighbours(int nameAS) {
		// check if nameAS is in the map
		if (graphAS.containsVertex(nameAS)) {

			// prepare array for neighbours
			ArrayList<Integer> neighbours = new ArrayList<Integer>();

			// for every edge of <nameAS> AS
			for (DefaultEdge edge : graphAS.edgesOf(nameAS)) {

				// get 2 vertices | I'm not sure which vertice is nameAS here |
				// can't find clear explanation in javadoc for JGraphT for it
				// so I check both...
				int as1 = graphAS.getEdgeSource(edge);
				int as2 = graphAS.getEdgeTarget(edge);

				// add vertice if not <nameAS> and not already in the array
				// (this check is explicit, but let it be:)
				if (as1 != nameAS && !neighbours.contains(as1)) {
					neighbours.add(as1);
				}
				// the same for second vertice.
				if (as2 != nameAS && !neighbours.contains(as2)) {
					neighbours.add(as2);
				}
			}
			// return neighbours found
			return neighbours;
		} else {
			return null; // return null if nameAS not found
		}
	}	
}
