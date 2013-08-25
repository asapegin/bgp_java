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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * This class reads from file and stores Internet Map (connection between ASs)
 * 
 * @author Andrey Sapegin
 */
public class InternetMap extends AutonomousSystemsGraph {

	// map filenames
	private String mapFilenameITP;
	private String mapFilenameCAIDA1;
	private String mapFilenameCAIDA2;
	private String mapFilenameCAIDA3;

	// logger
	private static Logger logger = LogManager.getLogger(InternetMap.class);

	private Map<Integer, Distances> distances = new HashMap<Integer, Distances>();

	protected InternetMap() {
	}

	/**
	 * Protected cunstructor for cloning the Map
	 * 
	 * @param graphAS
	 */
	protected InternetMap(SimpleGraph<Integer, DefaultEdge> graphAS) {
		this.graphAS = graphAS;
	}

	/**
	 * This constructor creates Internet Graph using intersection of all map
	 * files (not null) in parameters
	 * 
	 * @param mapFilenameITP
	 * @param mapFilenameCAIDA1
	 * @param mapFilenameCAIDA2
	 * @param mapFilenameCAIDA3
	 * @throws Exception
	 *             in case filenames are not set or IO Exception.
	 */
	public InternetMap(String mapFilenameITP, String mapFilenameCAIDA1,
			String mapFilenameCAIDA2, String mapFilenameCAIDA3)
			throws Exception {
		this.mapFilenameITP = mapFilenameITP;
		this.mapFilenameCAIDA1 = mapFilenameCAIDA1;
		this.mapFilenameCAIDA2 = mapFilenameCAIDA2;
		this.mapFilenameCAIDA3 = mapFilenameCAIDA3;

		// first of all, read initial map
		if (this.mapFilenameITP != null) {
			readMapITP();
			logger.info("ITP map " + this.mapFilenameITP
					+ " loaded. Number of edges: " + graphAS.edgeSet().size());
		} else if (this.mapFilenameCAIDA1 != null) {
			readCAIDAmap();
			logger.info("CAIDA map " + this.mapFilenameCAIDA1
					+ " loaded. Number of edges: " + graphAS.edgeSet().size());
		} else {
			logger.fatal("Both ITP and CAIDA1 filenames are null, I do not have filename to read map!");
			throw new Exception("No map filename");
		}

		// second, add (find intersection) other maps
		if (this.mapFilenameITP != null && this.mapFilenameCAIDA1 != null) {
			graphAS = addCAIDAmap(this.mapFilenameCAIDA1);
			logger.info("Graph merged with CAIDA map " + this.mapFilenameCAIDA1
					+ " Number of edges: " + graphAS.edgeSet().size());
		}
		if (this.mapFilenameCAIDA2 != null) {
			graphAS = addCAIDAmap(this.mapFilenameCAIDA2);
			logger.info("Graph merged with CAIDA map " + this.mapFilenameCAIDA2
					+ " Number of edges: " + graphAS.edgeSet().size());
		}
		if (this.mapFilenameCAIDA3 != null) {
			graphAS = addCAIDAmap(this.mapFilenameCAIDA3);
			logger.info("Graph merged with CAIDA map " + this.mapFilenameCAIDA3
					+ " Number of edges: " + graphAS.edgeSet().size());
		}
	}

	/**
	 * clones the graph inside the Map and returns new Map based on cloned graph
	 * 
	 * @return
	 */
	public InternetMap copyMap() {
		logger.debug("copying Internet Map...");
		logger.debug(this.graphAS.edgeSet().size() + " edges in source");
		logger.debug(this.graphAS.vertexSet().size() + " vertices in source");

		SimpleGraph<Integer, DefaultEdge> copiedGraph = new SimpleGraph<Integer, DefaultEdge>(
				DefaultEdge.class);
		
		//copy vertices
		for (Integer vertex : this.graphAS.vertexSet()) {
			copiedGraph.addVertex(vertex.intValue());
		}

		// copy edges
		for (DefaultEdge edge : this.graphAS.edgeSet()) {
			copiedGraph.addEdge(this.graphAS.getEdgeSource(edge).intValue(),
					this.graphAS.getEdgeTarget(edge).intValue());
		}

		logger.debug(copiedGraph.edgeSet().size() + " edges copied");
		logger.debug(copiedGraph.vertexSet().size() + " vertices copied");

		return new InternetMap(copiedGraph);
	}

	/**
	 * This method reads 'map' file (from Internet Topology Collection) set in
	 * the properties (private field).
	 */
	private void readMapITP() throws Exception {
		try {
			FileReader fr = new FileReader(mapFilenameITP);
			BufferedReader br = new BufferedReader(fr);

			String str = br.readLine();

			while (str != null) {

				int as1;
				int as2;

				try {
					as1 = Integer.parseInt(str.substring(0, str.indexOf("\t")));
					str = str.substring(str.indexOf("\t") + 1);
					as2 = Integer.parseInt(str.substring(0, str.indexOf("\t")));
				} catch (NumberFormatException e) {
					logger.error("Cannot parse AS number. Line will be ignored.");
					str = br.readLine();
					continue;
				}

				if ((as1 > 65536) || (as2 > 65536)) {
					logger.error("Logical error reading map file - as1 or as2 > 65536. Line will be ignored.");
					str = br.readLine();
					continue;
				}

				if (as1 > as2) {
					logger.warn("Logical error reading map file (as1 > as2). Problem will be ignored...");
				}

				// add vertices if not already present
				graphAS.addVertex(as1);
				graphAS.addVertex(as2);
				// add edge
				graphAS.addEdge(as1, as2);

				// read next line
				str = br.readLine();
			}

			br.close();

		} catch (FileNotFoundException e) {
			logger.fatal("Error during reading map file", e);
			throw new Exception("Error during reading map file");
		} catch (IOException e) {
			logger.fatal("Error during reading map file", e);
			throw new Exception("Error during reading map file");
		}
	}

	/**
	 * This method reads 'map' file (from CAIDA) set in the properties (private
	 * field).
	 * 
	 * @throws Exception
	 */
	private void readCAIDAmap() throws Exception {
		try {
			FileReader fr = new FileReader(this.mapFilenameCAIDA1);
			BufferedReader br = new BufferedReader(fr);

			String str = br.readLine();

			while (str != null) {

				// if the string does not start with 'D' (direct link) or 'I'
				// (indirect link), skip this string
				if (!str.startsWith("D") && !str.startsWith("I")) {
					str = br.readLine();
					continue;
				}

				// get first AS (or AS set of multihomed ASs)
				str = str.substring(str.indexOf("\t") + 1);
				String as1_str = str.substring(0, str.indexOf("\t"));

				// get second AS (or AS set of multihomed ASs)
				str = str.substring(str.indexOf("\t") + 1);
				String as2_str = str.substring(0, str.indexOf("\t"));

				// prepare arrays for ASs
				ArrayList<Integer> as1 = parseASsetCAIDA(as1_str);
				ArrayList<Integer> as2 = parseASsetCAIDA(as2_str);

				// add all to the graph
				for (Integer first : as1) {
					for (Integer second : as2) {
						// add vertices if not already present
						graphAS.addVertex(first);
						graphAS.addVertex(second);
						// add edge
						try {
							graphAS.addEdge(first, second);
						} catch (Exception e) { // stupid library could throw an
												// exception like
												// "loops not allowed"
												// I will catch them all!
							logger.error(
									"Error during adding link to the graph. Link will be ignored.",
									e);
							continue;
						}
					}
				}

				// read next line
				str = br.readLine();
			}

			br.close();

		} catch (FileNotFoundException e) {
			logger.error("Error during reading map file", e);
			throw new Exception("Error during reading map file");
		} catch (IOException e) {
			logger.error("Error during reading map file", e);
			throw new Exception("Error during reading map file");
		}
	}

	/**
	 * This method parses substring containing 1 of 2 AS sets from CAIDA
	 * topology file
	 * 
	 * @param str
	 *            - CAIDA AS set
	 * @return ArrayList<Integer> list with AS numbers extracted from CAIDA AS
	 *         set (usually 1 AS only)
	 */
	private ArrayList<Integer> parseASsetCAIDA(String str) {

		ArrayList<Integer> ases = new ArrayList<Integer>();

		str = str.replaceAll("_", ","); // treat multi-homed ASs like AS sets
										// and
		// vice versa

		// parse AS set
		while (str.contains(",")) {
			try {
				// parse AS from AS set or set of multi-homed ASs
				int as = Integer.parseInt(str.substring(0, str.indexOf(",")));
				if (as < 65536 && as > 0) {
					ases.add(as);
				} else {
					logger.error("Logical error reading map file - AS number > 65536 or < 0. AS will be ignored.");
				}
			} catch (NumberFormatException e) {
				logger.error("Cannot parse AS number. AS will be ignored.", e);
				str = str.substring(str.indexOf(",") + 1); // delete part of the
															// string which
															// cannot be parsed
				continue;
			}
			// delete parsed part of the string
			str = str.substring(str.indexOf(",") + 1);
		}

		// parse the rest of the string
		try {
			int as = Integer.parseInt(str);
			if (as < 65536 && as > 0) {
				ases.add(as);
			} else {
				logger.error("Logical error reading map file - AS number > 65536 or < 0. AS will be ignored.");
			}
		} catch (NumberFormatException e) {
			logger.error(
					"Cannot parse AS number. AS will be ignored. If this AS is not part of AS set or not multi-homed, the line will be ignored too.",
					e);
		}

		return ases;
	}

	/**
	 * This method reads CAIDA map from file and merges it with current
	 * internetMap. After this operation, internetMap graph will contain only
	 * intersection of previous internetMap and CAIDA map received as parameter
	 * In other words, internetMap will contain only links contained in both
	 * internetMap and map being read Again, in other words, only links,
	 * presented both in map file received as parameter and in the class field
	 * internetMap, will be saved in that class field
	 * 
	 * @param mapFilenameCAIDA
	 * @throws Exception
	 *             in case IO Error
	 */
	private SimpleGraph<Integer, DefaultEdge> addCAIDAmap(
			String mapFilenameCAIDA) throws Exception {

		SimpleGraph<Integer, DefaultEdge> mergedInternetGraph = new SimpleGraph<Integer, DefaultEdge>(
				DefaultEdge.class);

		try {
			FileReader fr = new FileReader(mapFilenameCAIDA);
			BufferedReader br = new BufferedReader(fr);

			String str = br.readLine();

			while (str != null) {

				// if the string does not start with 'D' (direct link) or 'I'
				// (indirect link), skip this string
				if (!str.startsWith("D") && !str.startsWith("I")) {
					str = br.readLine();
					continue;
				}

				// get first AS (or AS set of multihomed ASs)
				str = str.substring(str.indexOf("\t") + 1);
				String as1_str = str.substring(0, str.indexOf("\t"));

				// get second AS (or AS set of multihomed ASs)
				str = str.substring(str.indexOf("\t") + 1);
				String as2_str = str.substring(0, str.indexOf("\t"));

				// prepare arrays for ASs
				ArrayList<Integer> as1 = parseASsetCAIDA(as1_str);
				ArrayList<Integer> as2 = parseASsetCAIDA(as2_str);

				// add vertices and edge to the merged graph, if presented both
				// in file and graphAS
				for (Integer first : as1) {
					for (Integer second : as2) {
						if (graphAS.containsEdge(first, second)) {
							// add vertices (ASs names)
							mergedInternetGraph.addVertex(first);
							mergedInternetGraph.addVertex(second);
							// add edge (link betweeen ASs)
							mergedInternetGraph.addEdge(first, second);
						}
					}
				}

				// read next line
				str = br.readLine();
			}

			br.close();

		} catch (FileNotFoundException e) {
			logger.error("Error during reading map file", e);
			throw new Exception("Error during reading map file");
		} catch (IOException e) {
			logger.error("Error during reading map file", e);
			throw new Exception("Error during reading map file");
		}

		return mergedInternetGraph;
	}

	// 127 is infinity, null - not calculated before
	public Byte getInternetDistanceBFS(int as1, int as2) {

		// if Distances for AS1 are not created yet - create them
		if (distances.get(as1) == null) {
			distances.put(as1, new Distances());
		}

		if (distances.get(as1).getDistance(as2) != null) { // if distance was
															// already found,
															// return it.
			return distances.get(as1).getDistance(as2);
		} else { // start Breadth-First-Search

			// array where I save vertices found
			Set<Integer> foundVerticies = new HashSet<Integer>();

			// FIFO queue where I put new vertices
			Queue<Integer> queue = new LinkedList<Integer>();

			// add first AS to the graph and add it to the queue
			foundVerticies.add(as1);
			queue.add(as1);

			// set distance to itself for first vertex
			distances.get(as1).setDistance(as1, (byte) 0);

			while (!queue.isEmpty()) {
				// get and remove first element from the queue
				int currentVertex = queue.poll();

				// get all neighbours of current vertex, mark them and add to
				// the queue if not found earlier
				if (this.getNeighbours(currentVertex) != null) {
					for (Integer neighbour : this.getNeighbours(currentVertex)) {
						if (!foundVerticies.contains(neighbour)) {
							// mark as found and add to the queue
							foundVerticies.add(neighbour);
							queue.add(neighbour);

							// set distance for neighbour found
							if (distances.get(as1).getDistance(currentVertex) < 127) { // check
																						// if
																						// distance
																						// to
																						// currentVertex
																						// is
																						// less
																						// than
																						// infinity
								distances.get(as1).setDistance(
										// set distance
										neighbour,
										(byte) (distances.get(as1).getDistance(
												currentVertex) + 1));
							} else {
								distances.get(as1).setDistance(neighbour, // set
																			// infinite
																			// distance
																			// (127)
										(byte) 127);
							}

							if (neighbour == as2) { // if as2 was reached -
													// return
													// result
								return distances.get(as1)
										.getDistance(neighbour);
							}
						}
					}
				}
			}

			// If I'm here, the as2 was not reached
			distances.get(as1).setDistance(as2, (byte) 127); // set distance to
																// infinity
			return (byte) 127;

		}
	}
}
