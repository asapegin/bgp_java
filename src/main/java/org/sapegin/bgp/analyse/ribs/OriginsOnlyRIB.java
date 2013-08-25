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
package org.sapegin.bgp.analyse.ribs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sapegin.bgp.analyse.spikes.Destination;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         this class represents Routing Information Base of one (and only one!)
 *         monitoring router. I'm going to use it only to get AS Path of
 *         withdrawal prefixes, so I will not include all RIB fields here, just
 *         AS Path and some other which I will need for my analysis.
 * 
 *         This RIB should be loaded from ASCII machine-readable (-m) file
 *         created using converter of MRT-format RIBs.
 * 
 */
public class OriginsOnlyRIB {

	// logger
	private Logger logger = LogManager.getLogger(OriginsOnlyRIB.class);

	private HashMap<Integer, OriginsOnlyOneAS_RIB> rib = new HashMap<Integer, OriginsOnlyOneAS_RIB>();

	private ArrayList<Integer> inputASs;

	public OriginsOnlyRIB(String ribFilename, ArrayList<Integer> inputASs) {
		this.inputASs = inputASs;
		loadRIBFromFile(ribFilename);
	}

	/**
	 * This method reads routes from file 'ribFilename'. However, only routes
	 * from ASs listed in 'inputASs' will be imported.
	 * 
	 * @param ribFilename
	 */
	private void loadRIBFromFile(String ribFilename) {
		try {

			logger.info("loading RIB from " + ribFilename);

			FileReader fr = new FileReader(ribFilename);
			BufferedReader br = new BufferedReader(fr);

			String str = br.readLine();

			int routes_total = 0;
			int routes_read = 0;

			while (str != null) {

				routes_total++;

				int message_type_size;

				// check message format
				if (str.startsWith("TABLE_DUMP2|")) {
					message_type_size = 12;
				} else if (str.startsWith("TABLE_DUMP|")) {
					message_type_size = 11;
				} else {
					logger.trace("Update message does not start with 'TABLE_DUMP2|' or 'TABLE_DUMP|'. Skipping message...");
					str = br.readLine();
					continue;
				}

				// delete "TABLE_DUMP2|" or "TABLE_DUMP|" 
				str = str.substring(message_type_size);

				// get name (number) of AS sent the update message
				for (int i = 0; i < 3; i++) { // skip '|' 3 times
					str = str.substring(str.indexOf("|") + 1);
				}
				int nameAS;
				try { // parse AS's name
					nameAS = Integer
							.parseInt(str.substring(0, str.indexOf("|")));
				} catch (NumberFormatException e) {
					logger.trace(
							"Can't parse the name of AS, which has sent the update message! Message will be skipped.",
							e);
					str = br.readLine();
					continue;
				}

				if (!inputASs.contains(nameAS)) {
					logger.trace("Update from AS "
							+ nameAS
							+ ", not contained in inputASs list found. Update message will be skipped.");
					str = br.readLine();
					continue;
				}

				// read prefix
				Destination prefix;
				String sprefix = str.substring(str.indexOf("|") + 1,
						str.indexOf("/"));
				try { // parse prefix
					prefix = new Destination(Inet4Address.getByName(sprefix));
				} catch (UnknownHostException e) {
					logger.error(
							"Can't parse prefix: "
									+ sprefix
									+ ". Update message will be skipped. It's normal if the prefix is IPv6, as I analyse only IPv4 prefixes.",
							e);
					str = br.readLine();
					continue;
				}

				// read AS path
				str = str.substring(str.indexOf("|") + 1); // delete AS name
				str = str.substring(str.indexOf("|") + 1); // delete prefix
				String ases = str.substring(0, str.indexOf("|"));

				ASPath asPath = new ASPath(ases);

				// add prefix with AS Path to the RIB!
				announceOrigin(nameAS, prefix, asPath.getOriginAS());

				routes_read++;

				str = br.readLine();
			}

			br.close();
			fr.close();

			logger.info(routes_read + " of " + routes_total
					+ " loaded from RIB.");

		} catch (FileNotFoundException e) {
			logger.fatal("FileNotFound exception during reading RIB", e);
		} catch (IOException e) {
			logger.fatal("IO exception during reading RIB", e);
		}
	}

	/**
	 * This method adds route to the HashMap representation of RIB
	 * 
	 * @param nameAS
	 * @param prefix
	 * @param asPath
	 */
	public void announceOrigin(int nameAS, Destination prefix, ASPathElement originAS) {
		if (rib.containsKey(nameAS)) {
			rib.get(nameAS).announceOrigin(prefix, originAS);
		} else {
			OriginsOnlyOneAS_RIB asRIB = new OriginsOnlyOneAS_RIB();
			asRIB.announceOrigin(prefix, originAS);
			rib.put(nameAS, asRIB);
		}
	}

	/**
	 * Withdraws prefix from RIB.
	 * 
	 * @param nameAS
	 * @param prefix
	 */
	public void withdrawOrigin(int nameAS, Destination prefix) {
		if (rib.containsKey(nameAS)) {
			rib.get(nameAS).withdrawOrigin(prefix);
		} else {
			logger.warn("No routes in RIB to prefix "
					+ prefix.getPrefix().getHostAddress() + " from AS "
					+ nameAS);
		}
	}

	/**
	 * This method returns list of ASs in AS Path of route to prefix, for which
	 * a withdrawal message was received.
	 * 
	 * @param nameAS
	 * @param prefix
	 * @return
	 */
	public ASPathElement getWithdwalOrigin(int nameAS, Destination prefix) {
		if (rib.containsKey(nameAS)) {
			return rib.get(nameAS).getOriginAS(prefix);
		} else {
			logger.trace("No routes in RIB to prefix "
					+ prefix.getPrefix().getHostAddress() + " from AS "
					+ nameAS);
			return null;
		}
	}

}
