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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.AutonomousSystemsGraph;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.MyDirectIntegerVertexNameProvider;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         Abstract class representing Graph of specific (visible or monitored
 *         or some other, depending on extending class) ASs
 * 
 */
public abstract class ASsToAnalyse extends AutonomousSystemsGraph {

	protected ASsNames realASsNames;
	
	protected ConcurrentHashMap<Integer,Float> visibilities;

	protected InternetMap iMap;

	protected float visibilityPercent;		

	// logger
	private Logger logger = LogManager.getLogger(VisibleASs.class);

	abstract protected void findVisibleASs();

	public float visibility(int nameAS){
		if (visibilities.get(nameAS)!=null){
			return visibilities.get(nameAS);
		} else{
			// get nameAS neighbours
			ArrayList<Integer> neighbours = iMap.getNeighbours(nameAS);

			// check if at least one neighbour was found
			if (neighbours == null) {
				logger.warn("AS "
						+ nameAS
						+ " not found in the map. AS will be marked as not visible.");

				visibilities.put(nameAS, (float) 0);
				return 0;
			}

			int monitoredNeighbours = 0;

			// for every neighbour of concerned AS
			for (Integer neighbour : neighbours) {
				// check if the neighbour is monitored
				if (realASsNames.getASsNames().contains(neighbour)) {
					monitoredNeighbours++; // increment number of monitored
											// neighbours
				}
			}

			// if more than visibilityPercent of AS neighbours is also monitored
			// than concerned AS is visible
			float visibility = monitoredNeighbours / (float) neighbours.size();
			visibilities.put(nameAS, visibility);
			return visibility;
		}
	}
	
	/**
	 * This method determines if concerned AS is visible or not
	 * 
	 * @param nameAS
	 * @return boolean - true if AS is visible (if more than visibilityPercent
	 *         of AS neighbours is also monitored)
	 */
	protected boolean isVisible(Integer nameAS) {
		// get nameAS neighbours
		ArrayList<Integer> neighbours = iMap.getNeighbours(nameAS);

		// check if at least one neighbour was found
		if (neighbours == null) {
			logger.warn("AS "
					+ nameAS
					+ " not found in the map. AS will be marked as not visible.");

			visibilities.put(nameAS, (float) 0);
			
			return false;
		}

		int monitoredNeighbours = 0;

		// for every neighbour of concerned AS
		for (Integer neighbour : neighbours) {
			// check if the neighbour is monitored
			if (realASsNames.getASsNames().contains(neighbour)) {
				monitoredNeighbours++; // increment number of monitored
										// neighbours
			}
		}

		// if more than visibilityPercent of AS neighbours is also monitored
		// than concerned AS is visible
		float visibility = monitoredNeighbours / (float) neighbours.size();
		visibilities.put(nameAS, visibility);
		if (visibility >= visibilityPercent) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Getter returning visible AS names
	 * 
	 * @return ArrayList&lt;Integer&gt; - array list with names (integer
	 *         numbers) of visible (according to visibilityPercent) ASs
	 */
	public ArrayList<Integer> getVisibleASsNames() {
		ArrayList<Integer> vASs = new ArrayList<Integer>();
		for (Integer name : graphAS.vertexSet()) {
			vASs.add(name);
		}

		return vASs;
	}

	/**
	 * finds biggest connected component of AS graph and returns list of ASs in
	 * this component.
	 */
	public ArrayList<Integer> getASsNamesFromBiggestConnectedGraphComponent() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		result.addAll(getBiggestConnectedGraphComponent().vertexSet());
		return result;
	}

	/**
	 * 
	 * @return SimpleGraph representation of the biggest connected graph
	 *         component.
	 */
	public SimpleGraph<Integer, DefaultEdge> getBiggestConnectedGraphComponent() {

		int maxVerticies = 0;
		int maxVertex = 0;

		// get the longest path after breadth-first search for each vertex
		for (Integer visibleAS : graphAS.vertexSet()) {
			int verticies = getNumberOfConnectedVerticiesBFS(visibleAS);
			if (maxVerticies < verticies) {
				// increase maxLongestPath
				maxVerticies = verticies;
				// save name of AS
				maxVertex = visibleAS;
			}
		}

		// find and return connected component of graphAS graph with vertex
		// maxLongestPathVertex
		if (maxVerticies > 0) {
			return getConnectedComponentBFS(maxVertex);
		} else {
			logger.error("Max. longest path in the graph with visible ASs is 0 !");
			return null;
		}
	}

	/**
	 * This method performs breadth-first search (BFS) in {@link VisibleASs}
	 * starting from {visibleAS} and then calculates the longest path found
	 * during BFS.
	 * 
	 * I use algorithm from
	 * "Stefan Hougardy: Graphen und Algorithmen 1, WS2005/2006", Page 20
	 * 
	 * @param visibleAS
	 *            - vertex name to start from
	 * @return int - longest path found during BFS started from {visibleAS}
	 */
	protected byte getLongestPathBFS(Integer visibleAS) {
		byte longestPath = 0; // path should not be longer than 127

		// array where I mark found verticies as true
		List<Integer> foundVerticies = new ArrayList<Integer>();

		// array where I store distances to all verticies
		Map<Integer, Byte> distanceToVertice = new HashMap<Integer, Byte>();

		// FIFO queue where I put new verticies
		Queue<Integer> queue = new LinkedList<Integer>();

		// mark first AS as found and add it to the queue
		foundVerticies.add(visibleAS);
		queue.add(visibleAS);

		// set distance to itself for first vertex
		distanceToVertice.put(visibleAS, (byte) 0);

		while (!queue.isEmpty()) {
			// get and remove first element from the queue
			int currentVertex = queue.poll();
			// get all neighbours of current vertex, mark them and add to the
			// queue if not found earlier
			for (Integer neighbour : getNeighbours(currentVertex)) {
				if (!foundVerticies.contains(neighbour)) {
					// mark as found and add to the queue
					foundVerticies.add(neighbour);
					queue.add(neighbour);

					// set distance for neighbour found
					distanceToVertice.put(neighbour,
							(byte) (distanceToVertice.get(currentVertex) + 1));
					if (longestPath < distanceToVertice.get(neighbour)) {
						longestPath = distanceToVertice.get(neighbour);
					}
				}
			}
		}

		return longestPath;
	}

	/**
	 * This method performs breadth-first search (BFS) in {@link VisibleASs}
	 * starting from {visibleAS} and then calculates the number of verticies
	 * found during BFS.
	 * 
	 * I use algorithm from
	 * "Stefan Hougardy: Graphen und Algorithmen 1, WS2005/2006", Page 20
	 * 
	 * @param visibleAS
	 *            - vertex name to start from
	 * @return int - number of verticies found during BFS starting in
	 *         {visibleAS}
	 */
	private int getNumberOfConnectedVerticiesBFS(Integer visibleAS) {

		// array where I mark found verticies as true
		List<Integer> foundVerticies = new ArrayList<Integer>();

		// FIFO queue where I put new verticies
		Queue<Integer> queue = new LinkedList<Integer>();

		// mark first AS as found and add it to the queue
		foundVerticies.add(visibleAS);
		queue.add(visibleAS);

		while (!queue.isEmpty()) {
			// get and remove first element from the queue
			int currentVertex = queue.poll();
			// get all neighbours of current vertex, mark them and add to the
			// queue if not found earlier
			for (Integer neighbour : getNeighbours(currentVertex)) {
				if (!foundVerticies.contains(neighbour)) {
					// mark as found and add to the queue
					foundVerticies.add(neighbour);
					queue.add(neighbour);
				}
			}
		}

		// return result
		return foundVerticies.size();
	}

	/**
	 * this method uses BFS to return the connected graph component containing
	 * {visibleAS}
	 * 
	 * I use algorithm from
	 * "Stefan Hougardy: Graphen und Algorithmen 1, WS2005/2006", Page 20
	 * 
	 * @param visibleAS
	 *            - vertex name to start from
	 * @return SimpleGraph<Integer, DefaultEdge> - connected component of
	 *         visible ASs graph found during BFS starting in {visibleAS}
	 */
	private SimpleGraph<Integer, DefaultEdge> getConnectedComponentBFS(
			Integer visibleAS) {

		SimpleGraph<Integer, DefaultEdge> connectedComponent = new SimpleGraph<Integer, DefaultEdge>(
				DefaultEdge.class);

		// FIFO queue where I put new verticies
		Queue<Integer> queue = new LinkedList<Integer>();

		// add first AS to the graph and add it to the queue
		connectedComponent.addVertex(visibleAS);
		queue.add(visibleAS);

		while (!queue.isEmpty()) {
			// get and remove first element from the queue
			int currentVertex = queue.poll();
			// get all neighbours of current vertex, add them to the graph and
			// add to the
			// queue if not found earlier
			for (Integer neighbour : getNeighbours(currentVertex)) {
				if (!connectedComponent.containsVertex(neighbour)) {
					// add to the graph and add to the queue
					connectedComponent.addVertex(neighbour);
					connectedComponent.addEdge(currentVertex, neighbour);
					queue.add(neighbour);
				} else if (!connectedComponent.containsEdge(currentVertex,
						neighbour)) { // if neighbour was already added, check if the new edge was discovered
					connectedComponent.addEdge(currentVertex, neighbour);
				}
			}
		}

		return connectedComponent;
	}

	/**
	 * This method dumps graph with ALL visibleASs to the {filenameDOT} file in
	 * DOT format
	 * 
	 * @param filenameDOT
	 *            - filename to write
	 */
	public void writeDOT(String filenameDOT) {
		DOTExporter<Integer, DefaultEdge> dotExporter = new DOTExporter<Integer, DefaultEdge>(
				new MyDirectIntegerVertexNameProvider(), null, null);
		try {
			dotExporter.export(new BufferedWriter(new FileWriter(filenameDOT)),
					graphAS);
		} catch (IOException e) {
			logger.error("Can't write visible ASs graph into DOT file", e);
		}

	}

	/**
	 * This method dumps graph with ALL visibleASs to the String in DOT format
	 * 
	 * @return - String with DOT representation of graph.
	 */
	public String writeStringDOT() {
		DOTExporter<Integer, DefaultEdge> dotExporter = new DOTExporter<Integer, DefaultEdge>(
				new MyDirectIntegerVertexNameProvider(), null, null);

		StringWriter swr = new StringWriter();
		dotExporter.export(swr, graphAS);
		return swr.toString();
	}
}
