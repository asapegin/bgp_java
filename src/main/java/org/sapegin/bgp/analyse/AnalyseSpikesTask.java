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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sapegin.bgp.analyse.generics.ASsFactory;
import org.sapegin.bgp.analyse.visibility.ASsToAnalyse;
import org.sapegin.bgp.analyse.visibility.VisibleDuplicatedSpikes;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         abstract class presenting generic task for analysis of BGP updates it
 *         contains generic operations and fields like reading properties and
 *         parameters for analysis
 * 
 * @param <T>
 */
public abstract class AnalyseSpikesTask<T extends ASsToAnalyse> {

	protected Logger logger = LogManager.getLogger(AnalyseSpikesTask.class);

	protected VisibleDuplicatedSpikes<T> visibleDuplicatedSpikes;

	private String mapFilenameITP;
	private String mapFilenameCAIDA1;
	private String mapFilenameCAIDA2;
	private String mapFilenameCAIDA3;

	private String inputFilenamesFilename;
	protected ArrayList<String> inputRIBsFilenames = new ArrayList<String>();

	protected Boolean useRIBs;
	protected Boolean synchronise;
	protected Boolean componentOnly;
	protected Boolean correlated;

	protected long timeBuffer;
	protected double duplicationPercentage;

	protected InternetMap iMap;

	protected ArrayList<String> inputUpdatesFilenames = new ArrayList<String>();
	protected ArrayList<String> inputASsFilenames = new ArrayList<String>();

	protected ArrayList<SizeInterval> sizeIntervals;

	protected AnalyseSpikesTask() {
	}

	protected AnalyseSpikesTask(Properties properties, ASsFactory<T> factoryASs)
			throws Exception {
		// load properties
		logger.info("parsing properties...");

		this.inputFilenamesFilename = properties.getProperty("input_names",
				"input_names");
		this.mapFilenameITP = properties.getProperty("map", null);
		this.mapFilenameCAIDA1 = properties.getProperty("map_t1", null);
		this.mapFilenameCAIDA2 = properties.getProperty("map_t2", null);
		this.mapFilenameCAIDA3 = properties.getProperty("map_t3", null);

		this.useRIBs = Boolean.parseBoolean(properties.getProperty("with_ribs",
				"false"));

		this.synchronise = Boolean.parseBoolean(properties.getProperty(
				"sync_time", "false"));
		
		this.correlated = properties.getProperty("Analysis_type").equals(
				"correlated");

		this.componentOnly = Boolean.parseBoolean(properties.getProperty(
				"analyse_biggest_connected_subcomponent_only", "true"));

		this.timeBuffer = Long.parseLong(properties.getProperty("time-buffer",
				"120"));
		this.duplicationPercentage = Double.parseDouble(properties.getProperty(
				"duplication-percentage", "0.99"));

		// load size intervals from properties
		String intervals = properties.getProperty("spike_size_intervals",
				"950..1050,1950..2050");
		sizeIntervals = new ArrayList<SizeInterval>();
		for (String interval : intervals.split(",")) {
			SizeInterval sizeInterval = new SizeInterval(interval);
			sizeIntervals.add(sizeInterval);
		}

		// load Internet topology
		logger.info("loading map...");
		iMap = new InternetMap(mapFilenameITP, mapFilenameCAIDA1,
				mapFilenameCAIDA2, mapFilenameCAIDA3);

		// load inputUpdatesFilenames and inputASsFilenames and, if used,
		// inputRIBsFilenames
		loadFilenames();

	}

	public abstract void selectVisibleDuplicatedSpikes() throws Exception;

	public abstract void writeResults();

	/**
	 * This procedure reads names of files with updates and names of ASes
	 * 
	 * These preparations are STRICTLY needed for reading data from files with
	 * updates!
	 */
	private void loadFilenames() throws Exception {

		logger.info("loading filenames...");

		// read file line by line
		try {
			FileReader inputNames = new FileReader(inputFilenamesFilename);
			BufferedReader inputNamesBR = new BufferedReader(inputNames);

			// read first line
			String inStr = inputNamesBR.readLine();

			// while not EndOfFile
			while (inStr != null) {
				// skip commented lines
				if (inStr.startsWith("#")) {
					inStr = inputNamesBR.readLine();
					continue;
				}

				// read filenames
				try {
					inputUpdatesFilenames.add(inStr.substring(0,
							inStr.indexOf(":")));
					if (useRIBs) {
						inStr = inStr.substring(inStr.indexOf(":")+1);
						inputRIBsFilenames.add(inStr.substring(0,
								inStr.indexOf(":")));
					}
					inputASsFilenames.add(inStr.substring(inStr
							.lastIndexOf(":") + 1));
				} catch (Exception e) {
					inputNamesBR.close();
					inputNames.close();
					throw new Exception(
							"Cannot parse filenames with updates and / or ASs. Please check file format");
				}

				inStr = inputNamesBR.readLine();
			}
			
			inputNamesBR.close();
			inputNames.close();

		} catch (IOException e) {
			throw new IOException(
					"Cannot load filenames with updates and ASs from file.", e);
		}
	}
}
