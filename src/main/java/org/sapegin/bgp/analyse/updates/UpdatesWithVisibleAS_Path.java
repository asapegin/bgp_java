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
import java.util.Iterator;

import org.sapegin.bgp.analyse.ribs.ASPath;
import org.sapegin.bgp.analyse.ribs.RIB;
import org.sapegin.bgp.analyse.spikes.Destination;
import org.sapegin.bgp.analyse.spikes.MonitoredAS;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         This class reads and stores BGP updates (to be honest, prefixes and
 *         monitoredAS information only) from input files. Only updates from
 *         specified AS names and with AS Path including only specified AS names
 *         will be loaded from file.
 * 
 */
public class UpdatesWithVisibleAS_Path extends Updates {

	private ArrayList<RIB> monitorRIBs;

	protected UpdatesWithVisibleAS_Path(){}
	
	// "package private" constructor
	UpdatesWithVisibleAS_Path(ArrayList<String> inputUpdatesFilenames,
			ArrayList<RIB> monitorRIBs, ArrayList<Integer> visibleASsNames, boolean synchronise) {
		super(inputUpdatesFilenames, visibleASsNames); // read updates
		
		this.monitorRIBs = monitorRIBs;
	}

	/**
	 * This method reads updates from files in 'inputUpdatesFilenames'. However,
	 * only updates from ASs listed in 'inputASs' and with all ASs in AS_Path
	 * listed in 'inputASs' will be imported.
	 */
	@Override
	protected void readUpdates() {
		logger.info("loading updates from only visible/monitored ASs in AS Path (" +inputASs.size()+  " ASs are used)...");
		
		// for every filename
		// and for every router RIB
		Iterator<String> filenames = inputUpdatesFilenames.iterator();
		Iterator<RIB> ribs = monitorRIBs.iterator();
		while (filenames.hasNext() && ribs.hasNext()) {
			String inputUpdates = filenames.next();
			RIB rib = ribs.next();

			try {
				File file = new File(inputUpdates);
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);

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

					// get message time (announcement / withdrawal)
					str = str.substring(str.indexOf("|") + 1); // delete time
					String type = str.substring(0, str.indexOf("|"));

					// get name (number) of AS sent the update message
					str = str.substring(str.indexOf("|") + 1); // delete type
					str = str.substring(str.indexOf("|") + 1); // delete AS IP
																// address
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
						logger.trace("Update from AS "
								+ nameAS
								+ ", not contained in inputASs list found in "+ inputUpdates +". Update message will be skipped.");
						str = br.readLine();
						continue;
					}

					// parse prefix
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

					// read AS path
					ASPath asPath;
					if (type.equals("A")) { // if update type is announcement,
											// there should be AS Path
						str = str.substring(str.indexOf("|") + 1); // delete AS
						// name
						str = str.substring(str.indexOf("|") + 1); // delete
						// prefix
						String ases = str.substring(0, str.indexOf("|"));
						
						asPath = new ASPath(ases);
					} else if (type.equals("W")) { // if it's a withdrawal - try
													// to get AS Path from RIB
						asPath = rib.getWithdwalASPath(nameAS, new Destination(prefix));
					} else {
						logger.warn("Update message type is not W or A!. Message will be skipped!");
						str = br.readLine();
						continue;
					}

					// check if AS Path is not empty
					if (asPath == null) {
						logger.trace("Empty AS Path loaded. Message will be skipped!");
						str = br.readLine();
						continue;
					}

					// check if AS Path consists ONLY from VisibleASs
					if (inputASs.containsAll(asPath.getASList())) { // if yes, then
						// add prefix to the map!!!
						MonitoredAS monitoredAS = new MonitoredAS(file.getName(),
								nameAS);
						addPrefix(monitoredAS, updateTime, prefix);
					}

					// apply update to RIB
					if (type.equals("W")) {
						rib.withdraw(nameAS, new Destination(prefix));
					} else if (type.equals("A")) {
						rib.announce(nameAS, new Destination(prefix), asPath);
					}

					str = br.readLine();
				}

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
