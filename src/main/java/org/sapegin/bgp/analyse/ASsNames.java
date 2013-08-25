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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Class containing names of ASs from input files
 * 
 * @author Andrey Sapegin
 * 
 */
public class ASsNames {

	// ArrayList containing names of ASs monitored by all routers listed in file
	// got in properties.
	private ArrayList<Integer> asesNames = new ArrayList<Integer>();

	// names of files with ASs lists. Each file contains a list of ASs monitored
	// by 1 monitoring router (vantage point)
	private ArrayList<String> inputASsFilenames = new ArrayList<String>();

	private Logger logger = LogManager.getLogger(ASsNames.class);

	public ASsNames(ArrayList<String> inputASsFilenames) {
		this.inputASsFilenames = inputASsFilenames;

		// read AS names from files got as parameter
		readASsNames();
	}

	public ArrayList<Integer> getASsNames() {
		return asesNames;
	}

	/**
	 * This method reads unique AS names from files into ArrayList
	 */
	private void readASsNames() {
		logger.info("loading ASs names...");
		
		// for every file
		for (String inputASsFilename : inputASsFilenames) {

			try {
				// read file
				FileReader fr = new FileReader(inputASsFilename);
				BufferedReader br = new BufferedReader(fr);

				String str = br.readLine();

				while (str != null) {

					try {
						// parse AS name
						int name = Integer.parseInt(str);

						// add AS name to the array if not already there. It
						// could happen, if the previous file (corresponding to
						// previous monitor router) has the same monitored AS.
						if (!asesNames.contains(name)) {
							asesNames.add(name);
						}

					} catch (NumberFormatException e) {
						logger.warn("Can't parse AS name in "
								+ inputASsFilename + ": " + str
								+ ". AS name will be skipped.");
					}

					// read next line
					str = br.readLine();
				}
				
				br.close();
				
			} catch (FileNotFoundException e) {
				logger.error("Cannot find " + inputASsFilename + " file", e);
			} catch (IOException e) {
				logger.error("Exception while reading " + inputASsFilename
						+ " file", e);
			}
		}
		
		logger.info(asesNames.size() + " ASs loaded.");
	}
}
