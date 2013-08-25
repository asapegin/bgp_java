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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sapegin.bgp.analyse.Colours;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         This class represents some collection of updates (spikes) from
 *         multiple pairs <monitored AS, monitoring router>
 * 
 */
public class SpikeCollection {

	// Map containing all spikes from all monitored ASs.
	protected Map<MonitoredAS, SingleASspikes> allUpdates = new HashMap<MonitoredAS, SingleASspikes>();

	protected Logger logger = LogManager.getLogger(SpikeCollection.class);

	public SpikeCollection() {
	}

	public SpikeCollection(Map<MonitoredAS, SingleASspikes> spikes) {
		this.allUpdates = spikes;
	}

	// add prefix with originAS to the SpikeCollection
	protected void addDestination(MonitoredAS monitoredAS, long updateTime,
			Destination destination) {
		if (!allUpdates.containsKey(monitoredAS)) {
			// if there is no spikes for this AS yet
			// create all new:

			// create new spike and add prefix
			Spike spike = new Spike();
			spike.addDestination(destination);

			// add new spike
			addSpike(updateTime, spike, monitoredAS);
		} else {
			// if there are already some spikes for this monitored AS
			// add new prefix:

			SingleASspikes spikes = allUpdates.get(monitoredAS);
			if (!spikes.hasSpikeAtTime(updateTime)) {
				// if there is no spike at updateTime
				// create new:-

				// create new spike and add prefix
				Spike spike = new Spike();
				spike.addDestination(destination);

				// put new spike with prefix at update time
				spikes.addSpike(updateTime, spike);
			} else {
				// if there is already a spike at updateTime
				// add new prefix:
				Spike spike = spikes.getSpikeAtTime(updateTime);
				spike.addDestination(destination);
			}
		}
	}

	/**
	 * Adds prefix to collection of spikes
	 * 
	 * @param monitoredAS
	 * @param updateTime
	 * @param prefix
	 */
	protected boolean addPrefix(MonitoredAS monitoredAS, long updateTime,
			InetAddress prefix) {
		boolean warning = false;
		
		if (!allUpdates.containsKey(monitoredAS)) {
			// if there is no spikes for this AS yet
			// create all new:

			// create new spike and add prefix
			Spike spike = new Spike();
			spike.addPrefix(prefix);

			// add new spike
			addSpike(updateTime, spike, monitoredAS);
		} else {
			// if there are already some spikes for this monitored AS
			// add new prefix:

			SingleASspikes spikes = allUpdates.get(monitoredAS);
			if (!spikes.hasSpikeAtTime(updateTime)) {
				// if there is no spike at updateTime
				// create new:-

				// create new spike and add prefix
				Spike spike = new Spike();
				spike.addPrefix(prefix);

				// put new spike with prefix at update time
				spikes.addSpike(updateTime, spike);
			} else {
				// if there is already a spike at updateTime
				// add new prefix:
				Spike spike = spikes.getSpikeAtTime(updateTime);
				
				if (spike.containsPrefix(prefix)) {
					warning = true;
				}
				spike.addPrefix(prefix);
			}
		}
		
		return !warning;
	}

	/**
	 * Adds spike to collection of spikes.
	 * 
	 * @param spikeTime
	 * @param spike
	 * @param as
	 * 
	 * @return false if there is already a spike at this time from this
	 *         {monitored AS, monitoring router} pair
	 */
	public boolean addSpike(long spikeTime, Spike spike, MonitoredAS as) {
		if (allUpdates.containsKey(as)) {
			return allUpdates.get(as).addSpike(spikeTime, spike);
		} else {
			SingleASspikes singleASspikes = new SingleASspikes();
			singleASspikes.addSpike(spikeTime, spike);
			allUpdates.put(as, singleASspikes);
			return true;
		}
	}

	/**
	 * Returns map representation of spike collection.
	 * 
	 * @return
	 */
	public Map<MonitoredAS, SingleASspikes> getUpdateMap() {
		return allUpdates;
	}

	/**
	 * Prepares a chart of all spikes around the given one within given time
	 * interval
	 * 
	 * Creates a folder with files with updates and gnuplot script to make the
	 * chart
	 * 
	 * @param updatesChartFolder
	 *            - folder where the folder with chart data and gnuplot script
	 *            should be written
	 * @param spike
	 *            - spike to print all other spikes around
	 * @param spikeTime
	 *            - need it, as it's not included in the spike
	 * @param timeBuffer
	 *            - time interval to plot around the given spike
	 * @param colours
	 *            - gnuplot colours to try to give separate colour for each
	 *            MonitoredAS (pair {AS, monitoring router})
	 */
	public void printAllAround(String updatesChartFolder, Spike spike,
			long spikeTime, long timeBuffer, Colours colours) {
		colours.clearStats();

		String chartFolder = updatesChartFolder + "/" + spikeTime + "_"
				+ spike.getSpikeSize();
		String gnuplotFile = chartFolder + "/plot.gnu";

		try {

			// create chart folder if needed
			File gFile = new File(chartFolder);
			// empty folder if it was already existed
			if (gFile.exists()) {
				FileUtils.deleteDirectory(gFile);
			}
			gFile.mkdirs();

			// prepare to write a gnuplot script
			FileWriter gfwr = new FileWriter(gnuplotFile);
			BufferedWriter gbwr = new BufferedWriter(gfwr);

			// write header of gnuplot script
			gbwr.write("set term postscript enhanced color" + "\n"
					+ "set style fill solid" + "\n" + "set output '"
					+ spikeTime + "_" + spike.getSpikeSize() + ".eps'" + "\n"
					+ "set xtics format \"%10.0f\"" + "\n" + "set yrange [-1:]"
					+ "\n" + "set xrange [" + (spikeTime - timeBuffer - 5)
					+ ":" + (spikeTime + timeBuffer + 5) + "]" + "\n"
					+ "set key off" + "\n");

			gbwr.write("plot ");

			int dataFiles = 0;

			// go through all spikes in spike interval
			for (long time = spikeTime - timeBuffer; time <= spikeTime
					+ timeBuffer; time++) {

				// check all pairs {monitored AS, monitoring router}
				for (MonitoredAS monitoredAS : allUpdates.keySet()) {

					SingleASspikes spikes = allUpdates.get(monitoredAS);

					// if there is a spike within the time interval
					if (spikes.hasSpikeAtTime(time)) {

						// prepare file with spike sizes and times
						// gnuplot script will take data from these files
						File dump = new File(chartFolder + "/"
								+ monitoredAS.getMonitoredAS() + "_"
								+ monitoredAS.getMonitoringRouter());

						boolean exists = dump.exists();

						// prepare string to write
						String str = time + " "
								+ spikes.getSpikeAtTime(time).getSpikeSize()
								+ "\n";

						FileWriter fwr;
						BufferedWriter bwr;

						// if we have already dumped some spikes for this
						// {monitored AS, monitoring router} pair
						if (exists) {
							fwr = new FileWriter(dump, true); // then append it
							bwr = new BufferedWriter(fwr);
						} else {
							fwr = new FileWriter(dump); // create it
							bwr = new BufferedWriter(fwr);

							// if I create a new data file,
							// then I need to add it to the gnuplot script
							if (dataFiles == 0) { // if this is first data file
								gbwr.write("'" + dump.getName() + "' w i");// lc
																			// rgb
																			// '"
																			// +
																			// colours.getNext()
																			// +
																			// "'");
							} else {
								gbwr.write(", '" + dump.getName() + "' w i");// lc
																				// rgb
																				// '"
																				// +
																				// colours.getNext()
																				// +
																				// "'");
							}

							dataFiles++;
						}

						bwr.write(str);

						bwr.close();
						fwr.close();
					}
				}
			}

			gbwr.close();
			gfwr.close();

		} catch (IOException e) {
			logger.error("Cannot write updates chart data into file.", e);
		}

	}
}
