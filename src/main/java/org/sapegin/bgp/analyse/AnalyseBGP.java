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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sapegin.bgp.analyse.ases.ASsFileGenerator;
import org.sapegin.bgp.analyse.ases.FixPeerAS;
import org.sapegin.bgp.analyse.correlation.advanced.AnalyseCorrelatedSpikesTask;
import org.sapegin.bgp.analyse.correlation.basic.BasicCorrelationAnalysisTask;
import org.sapegin.bgp.analyse.duplication.CountDuplicationTask;
import org.sapegin.bgp.analyse.generics.ASsFactory;
import org.sapegin.bgp.analyse.generics.MonitoredASsFactory;
import org.sapegin.bgp.analyse.generics.VisibleASsFactory;
import org.sapegin.bgp.analyse.visibility.AnalyseVisibleSpikesTask;
import org.sapegin.bgp.analyse.visibility.MonitoredASs;
import org.sapegin.bgp.analyse.visibility.VisibleASs;

/**
 * Class containing main method for analysis of BGP updates
 * 
 * @version 3.0
 * @author Andrey Sapegin
 * 
 */
public class AnalyseBGP {

	// program properties
	private static Properties properties = new Properties();

	private static Logger logger = LogManager.getLogger(AnalyseBGP.class);

	/**
	 * This procedure loads properties from file
	 */
	private static void loadProperties() {

		logger.info("loading properties...");

		// Load properties (filenames, number of threads, other parameters) from
		// file
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		// System.out.println(loader.getResource("").getPath());
		try {
			InputStream xmlStream = loader
					.getResourceAsStream("analyse.properties.xml");
			if (xmlStream == null) {
				logger.error("Fatal error: xmlStream==null :(");
				System.exit(2);
			}
			properties.loadFromXML(xmlStream);
			xmlStream.close();
		} catch (IOException e) {
			logger.fatal(
					"Fatal error: Exception during loading properties from XML",
					e);
			logger.fatal("Sorry, I can't work without properties. Closing application.");
			System.exit(2);
		}
	}

	/**
	 * This is the main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		loadProperties();

		// fix peerAS in all files with updates
		if (properties.getProperty("fix_peer_as", "false").equals("true")) {
			logger.info("fixing peer AS...");

			FixPeerAS converter = new FixPeerAS(
					properties.getProperty("input_names"),
					Boolean.parseBoolean(properties.getProperty(
							"simulate_fix_peer_as", "true")),
					Boolean.parseBoolean(properties.getProperty("with_ribs",
							"false")));
			try {
				converter.fixAllUpdates();
			} catch (Exception e) {
				logger.fatal(
						"Exception during fixing peerAS. Please check logs. Closing application...",
						e);
				System.exit(2);
			}
		}

		// regenerate ASs names sets if set in properties
		if (properties.getProperty("generate_ases", "false").equals("true")) {
			logger.info("generating AS names...");

			try {
				ASsFileGenerator generator = new ASsFileGenerator(
						properties.getProperty("input_names"),
						properties.getProperty("generate_ases_folder"),
						Boolean.parseBoolean(properties.getProperty(
								"with_ribs", "false")));

				String newInputNames = generator.generate();
				properties.setProperty("input_names", newInputNames);
			} catch (Exception e) {
				logger.fatal(
						"Cannot generate ASs names. Closing application...", e);
				System.exit(2);
			}
		}

		try {

			logger.info("Creating and running task...");

			AnalyseSpikesTask<?> task;

			String analysisType = properties.getProperty("Analysis_type", "");

			if (analysisType.equals("correlated")) {

				// which correlation should be performed
				String correlationType = properties.getProperty(
						"correlation_type", "");

				if (correlationType.equals("basic")) {
					// basic correlation
					if (properties.getProperty("AS_type_to_analyse", "visible")
							.equals("visible")) { // for visible ASs only
						task = new BasicCorrelationAnalysisTask<VisibleASs>(
								properties, new VisibleASsFactory());
					} else {
						// for all monitored ASs
						task = new BasicCorrelationAnalysisTask<MonitoredASs>(
								properties, new MonitoredASsFactory());
					}
				} else {
					// advanced correlation
					if (properties.getProperty("AS_type_to_analyse", "visible")
							.equals("visible")) { // for visible ASs only
						task = new AnalyseCorrelatedSpikesTask<VisibleASs>(
								properties, new VisibleASsFactory());
					} else { // for all monitored ASs
						task = new AnalyseCorrelatedSpikesTask<MonitoredASs>(
								properties, new MonitoredASsFactory());
					}
				}
			} else if (analysisType.equals("duplication")) {

				// count duplication levels
				task = new CountDuplicationTask<MonitoredASs>(properties,
						new MonitoredASsFactory());
			} else {

				// starting with the default analysis type

				// find groups of correlated spikes, build propagation paths and
				// plot spikes

				// create new task, initialise all in the constructor
				// visible AS - monitored AS with all neighbours monitored too
				if (properties.getProperty("AS_type_to_analyse", "visible")
						.equals("visible")) {
					ASsFactory<VisibleASs> visibleASsFactory = new VisibleASsFactory();
					task = new AnalyseVisibleSpikesTask<VisibleASs>(properties,
							visibleASsFactory);
				} else {
					ASsFactory<MonitoredASs> monitoredASsFactory = new MonitoredASsFactory();
					task = new AnalyseVisibleSpikesTask<MonitoredASs>(
							properties, monitoredASsFactory);
				}
			}

			// find duplicated spikes from visible (or monitored) ASs only
			logger.info("selecting duplicated spikes...");
			task.selectVisibleDuplicatedSpikes();

			// print out results
			logger.info("writing results...");
			task.writeResults();
		} catch (Exception e) {
			logger.fatal(
					"Exception during task execution. Closing application.", e);
			System.exit(2);
		}
	}
}
