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
package org.sapegin.bgp.analyse.updates;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.sapegin.bgp.analyse.spikes.MonitoredAS;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         This class reads and stores BGP updates (to be honest, prefixes and
 *         monitoredAS information only) from input files. Only updates from
 *         specified AS names will be loaded from file.
 * 
 */
public class UpdatesFromVisibleASs extends Updates {

	protected UpdatesFromVisibleASs() {
	}

	// "package private" constructor
	UpdatesFromVisibleASs(ArrayList<String> inputUpdatesFilenames,
			ArrayList<Integer> visibleASsNames, boolean synchronise) {
		super(inputUpdatesFilenames, visibleASsNames);
	}

	/**
	 * This method reads updates from files in 'inputUpdatesFilenames'. However,
	 * only updates from ASs listed in 'inputASs' will be imported
	 */
	@Override
	protected void readUpdates() {
		logger.info("loading updates from visible/monitored ASs ("
				+ inputASs.size() + " ASs are used)...");

		// for every filename
		for (String inputUpdates : inputUpdatesFilenames) {
			try {
				File file = new File(inputUpdates);
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);

				boolean warning = false;
				int prefixes = 0;
				int samePrefixes = 0;

				String str = br.readLine();

				while (str != null) {

					// check message format
					if (!str.startsWith("BGP4MP|")) {
						logger.warn("Update message does not start with 'BGP4MP|'. Skipping message...");
						str = br.readLine();
						continue;
					}

					// delete "BGP4MP|"
					str = str.substring(7);

					// get time
					long updateTime;
					try {
						updateTime = Long.parseLong(str.substring(0,
								str.indexOf("|")));
					} catch (NumberFormatException e) {
						logger.warn(
								"Can't parse the time of update message! Message will be skipped.",
								e);
						str = br.readLine();
						continue;
					}

					// skip message if time < minStartTime to synchronise files
					if (updateTime < this.minStartTime) {
						str = br.readLine();
						continue;
					}

					// get name (number) of AS sent the update message
					for (int i = 0; i < 3; i++) { // skip '|' 3 times
						str = str.substring(str.indexOf("|") + 1);
					}
					int nameAS;
					try { // parse AS's name
						nameAS = Integer.parseInt(str.substring(0,
								str.indexOf("|")));
					} catch (NumberFormatException e) {
						logger.warn(
								"Can't parse the name of AS, which has sent the update message! Message will be skipped.",
								e);
						str = br.readLine();
						continue;
					}

					if (!inputASs.contains(nameAS)) {
						logger.trace("Update from AS " + nameAS
								+ ", not contained in inputASs list found in "
								+ inputUpdates
								+ ". Update message will be skipped.");
						str = br.readLine();
						continue;
					}

					// add update to the map! //
					MonitoredAS monitoredAS = new MonitoredAS(file.getName(),
							nameAS);

					InetAddress prefix;
					try { // parse prefix
						prefix = readPrefix(str);
					} catch (UnknownHostException e) {
						logger.error(
								"Can't parse prefix. Update message will be skipped",
								e);
						str = br.readLine();
						continue;
					}

					// add prefix to the map!
					if (!addPrefix(monitoredAS, updateTime, prefix)) { //if prefix already contained at this second from this AS.
						if (!warning) { // if this warning is not yet issued for
										// this file
							logger.warn("this prefix is already presented in this spike. This could be normal if the update dump contains several prefix updates (withdrawals/updates) from 1 AS at the same second");
							logger.warn("this warning will be issued only once for this file");
							warning = true;
						}
						samePrefixes++;
					}
					prefixes++;

					str = br.readLine();
				}

				logger.info("File "+inputUpdates+" readed. "+prefixes+" prefixes loaded including "+samePrefixes+" equal prefixes appeared the same second");
				
				fr.close();

			} catch (FileNotFoundException e) {
				logger.fatal(
						"FileNotFound exception during reading files with updates",
						e);
			} catch (IOException e) {
				logger.fatal(
						"IO exception during reading files with updates during",
						e);
			}
		}
	}
}
