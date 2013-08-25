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
package org.sapegin.bgp.analyse.ases;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         This class generates files with names of ASs from files with updates
 *         Such files are needed to optimise algorithms in other parts of the
 *         program, e.g., to perform topology analysis without parsing files
 *         with BGP updates
 * 
 */
public class ASsFileGenerator {

	// logger
	private static Logger logger = LogManager.getLogger(ASsFileGenerator.class);

	private String inputFilenamesFilename;
	private String asesFolderName;
	// true if file with input filenames contains RIB filenames too
	// (update_filename:rib_filename)
	private Boolean withRIBs;

	public ASsFileGenerator(String inputFilenamesFilename,
			String asesFolderName, Boolean withRIBs) {
		this.inputFilenamesFilename = inputFilenamesFilename;
		this.asesFolderName = asesFolderName;
		this.withRIBs = withRIBs;
	}

	/**
	 * This method goes throw all files with updates. For each file it creates a
	 * file with names of ASs (peerASs) which sent those updates. Also it
	 * creates new input_names file where for each string with name of file with
	 * updates this method adds
	 * :<name_of_file_with_ASs_names_for_file_with_updates_in_this_string>
	 * 
	 * if withRIBs parameter was set, the
	 * <name_of_file_with_ASs_names_for_file_with_updates_in_this_string> will
	 * be inserted between filename for updates and filename for RIB.
	 * 
	 * @return String - name of new input_updates file with names of files with
	 *         names of ASs added.
	 */
	public String generate() throws Exception {

		// read file line by line
		try {
			// source file
			FileReader inputNames = new FileReader(inputFilenamesFilename);
			BufferedReader inputNamesBR = new BufferedReader(inputNames);

			// target file
			FileWriter newInputNames = new FileWriter(inputFilenamesFilename
					+ ".new");
			BufferedWriter newInputNamesBWR = new BufferedWriter(newInputNames);

			// parsing source...
			// read first line
			String updatesFilename;
			String input = inputNamesBR.readLine();
			if (!withRIBs) {
				updatesFilename = input;
			} else {
				updatesFilename = input.substring(0, input.indexOf(":"));
			}

			// while not EndOfFile
			while (updatesFilename != null) {
				if (updatesFilename.startsWith("#")) {
					// if line is commented
					newInputNamesBWR.write(updatesFilename + "\n");
					updatesFilename = inputNamesBR.readLine();
					continue;
				}

				HashSet<Integer> names = new HashSet<Integer>(); // clear list
																	// with
																	// unique AS
																	// names

				// read file with updates
				FileReader updates = new FileReader(updatesFilename);
				BufferedReader updatesBR = new BufferedReader(updates);
				// go throw file with updates
				String str = updatesBR.readLine();
				while (str != null) {

					for (int i = 1; i <= 4; i++) { // go to the peerAS
						str = str.substring(str.indexOf("|") + 1);
					}

					try {
						// convert peerAS name to integer
						int nameAS = Integer.parseInt(str.substring(0,
								str.indexOf("|")));
						names.add(nameAS); // add name if not already present
					} catch (NumberFormatException e) {
						logger.error("Cannot parse peer AS name: "
								+ updatesFilename + ":" + str, e);
					}

					// read next string
					str = updatesBR.readLine();
				}
				updatesBR.close();

				// write all AS names found to file
				String relativeUpdatesFilename = updatesFilename;

				while (relativeUpdatesFilename.indexOf("/") > -1) {
					relativeUpdatesFilename = relativeUpdatesFilename
							.substring(relativeUpdatesFilename.indexOf("/") + 1);
				}

				// target file
				File target = new File(asesFolderName + "/"
						+ relativeUpdatesFilename + ".ases");
				// create path if not exists
				target.getParentFile().mkdirs();

				FileWriter asesNames = new FileWriter(target);
				BufferedWriter asesNamesBWR = new BufferedWriter(asesNames);

				for (Integer name : names) {
					asesNamesBWR.write(name + "\n");
				}
				asesNamesBWR.close(); // close file with names of ASs
				asesNames.close();

				// write name of file with ases to the new input_names file
				// together with name of file with updates
				newInputNamesBWR.write(input + ":" + asesFolderName + "/"
						+ relativeUpdatesFilename + ".ases" + "\n");

				// read next updates filename
				input = inputNamesBR.readLine();
				if (withRIBs && input!=null) {
					updatesFilename = input.substring(0, input.indexOf(":"));
				} else {
					updatesFilename = input;
				}
			}
			// close files
			inputNames.close();
			newInputNamesBWR.close();
			newInputNames.close();
		} catch (IOException e) {
			logger.fatal(
					"Cannot load filenames with updates and ASs from file.", e);
			throw (new Exception(
					"Cannot load filenames with updates and ASs from file.", e));
		}

		return inputFilenamesFilename + ".new";
	}
}
