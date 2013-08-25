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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         This class is created to prepare ("fix") those files with BGP updates
 *         (in machine readable format generated with route_btoa) where peerAS
 *         is not the first AS in the AS path. For example, all BGP update dumps
 *         from Abilene have 1 peerAS - 11537, while it's not even mentioned in
 *         the AS path. In such situations, methods of this class will replace
 *         peerAS with first AS in the AS path. Such preparations are needed to
 *         simplify implementation of other algorithms for BGP analysis.
 * 
 */
public class FixPeerAS {

	private static Logger logger = LogManager.getLogger(FixPeerAS.class);

	private String inputFilenamesFilename;
	private boolean simulate;
	private boolean ribs;

	public FixPeerAS(String inputFilenamesFilename, boolean simulate,
			boolean withRIBs) {
		this.inputFilenamesFilename = inputFilenamesFilename;
		this.simulate = simulate;
		this.ribs = withRIBs;
	}

	/**
	 * this method reads input_names file with all updates and checks every
	 * updates file from it
	 * 
	 * @throws Exception
	 */
	public void fixAllUpdates() throws Exception {

		// I implement here (once again) the algorithm of reading of update
		// filenames because I want this class to be stand-alone as much as
		// possible because I plan to use it in very rare cases.

		// read file with input names line by line
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

				String updatesFilename;

				if (inStr.indexOf(":") != -1) {
					if (!ribs || inStr.lastIndexOf(":") != inStr.indexOf(":")) { // if
																					// ribs
																					// not
																					// presented,
																					// or
																					// presented,
																					// but
																					// ASs
																					// names
																					// are
																					// presented
																					// too
						logger.error("Attention!!! File with ASs corresponding to file with updates presented. If this file with updates will be fixed for peerAS, new AS list generation will be needed.");
					}
					updatesFilename = inStr.substring(0, inStr.indexOf(":"));
				} else {
					updatesFilename = inStr;
				}

				// check file with updates and fix if needed
				checkAndFixUpdateFile(updatesFilename);

				// read next update filename
				inStr = inputNamesBR.readLine();
			}

			inputNames.close();
		} catch (IOException e) {
			throw (new Exception("IO Exception during modifying peer AS", e));
		}
	}

	/**
	 * This method checks file with BGP updates in machine readable format and,
	 * if peerAS != firstASFromASPath, fixes it
	 * 
	 * @param updatesFilename
	 * @throws IOException
	 */
	private void checkAndFixUpdateFile(String updatesFilename) throws Exception {
		FileReader updatesFR = new FileReader(updatesFilename);
		BufferedReader updatesBR = new BufferedReader(updatesFR);

		String update = updatesBR.readLine();

		while (update != null) {
			String str = update;
			for (int i = 1; i <= 2; i++) { // go to peer AS
				str = str.substring(str.indexOf("|") + 1);
			}
			String updateType = str.substring(0, str.indexOf("|"));
			if (updateType.equals("W")) { // there is no way to determine
											// correct AS for prefix withdrawals
											// as there is no AS Path
				update = updatesBR.readLine();
				continue;
			}
			for (int i = 1; i <= 2; i++) { // go to peer AS
				str = str.substring(str.indexOf("|") + 1);
			}

			try {
				// convert peerAS name to integer
				int peerAS = Integer
						.parseInt(str.substring(0, str.indexOf("|")));

				// go to AS Path
				str = str.substring(str.indexOf("|") + 1);
				str = str.substring(str.indexOf("|") + 1);

				int firstASinASPath;
				if ((str.indexOf(" ") == -1) || (str.indexOf(" ") > 5)) { // in
																			// case
																			// when
																			// there
																			// is
																			// only
																			// 1
																			// AS
																			// in
																			// AS
																			// path
					firstASinASPath = Integer.parseInt(str.substring(0,
							str.indexOf("|")));
				} else {
					firstASinASPath = Integer.parseInt(str.substring(0,
							str.indexOf(" ")));
				}

				// compare peerAS with first AS in AS Path
				if (peerAS != firstASinASPath) {
					logger.info("Update file with peerAS different from first AS in AS Path found: "
							+ updatesFilename);
					logger.info("file before conversion will be copied as filename.old");

					if (!simulate) { // in some cases I only want to
										// know which files need peerAS
										// fix, without fixing them
						fixUpdateFile(updatesFilename);
					}

					// file was converted, so stop searching for peerASes, that
					// needed to be fixed
					break;
				}

			} catch (Exception e) {
				logger.fatal("Cannot parse update: " + updatesFilename + ":"
						+ update, e);
				updatesBR.close();
				updatesFR.close();
				throw (new Exception("Cannot parse update: " + updatesFilename
						+ ":" + str, e));
			}

			update = updatesBR.readLine();
		}

		updatesBR.close();
		updatesFR.close();
	}

	/**
	 * This method finds all situations when peerAS is not equal to first AS in
	 * the AS Path and writes firstAS in ASPath as peerAS. It copies original
	 * file to filename.old
	 * 
	 * @param updatesFilename
	 * @throws IOException
	 */
	private void fixUpdateFile(String updatesFilename) throws IOException {
		File original = new File(updatesFilename);
		FileReader fr = new FileReader(original);
		BufferedReader br = new BufferedReader(fr);

		File converted = new File(updatesFilename + ".converted");
		FileWriter fwr = new FileWriter(converted);
		BufferedWriter bwr = new BufferedWriter(fwr);

		String update = br.readLine();

		while (update != null) {
			String str = update;

			for (int i = 1; i <= 2; i++) { // go to peer AS
				str = str.substring(str.indexOf("|") + 1);
			}
			String updateType = str.substring(0, str.indexOf("|"));
			if (updateType.equals("W")) { // there is no way to determine
				// correct AS for prefix withdrawals
				// as there is no AS Path
				bwr.write(update);
				bwr.newLine();
				update = br.readLine();
				continue;
			}
			for (int i = 1; i <= 2; i++) { // go to peer AS
				str = str.substring(str.indexOf("|") + 1);
			}

			// convert peerAS name to integer
			int peerAS = Integer.parseInt(str.substring(0, str.indexOf("|")));

			// go to AS Path
			str = str.substring(str.indexOf("|") + 1);
			str = str.substring(str.indexOf("|") + 1);

			int firstASinASPath;
			if ((str.indexOf(" ") == -1) || (str.indexOf(" ") > 5)) { // in case
																		// when
																		// there
																		// is
																		// only
																		// 1 AS
																		// in AS
																		// path
				firstASinASPath = Integer.parseInt(str.substring(0,
						str.indexOf("|")));
			} else {
				firstASinASPath = Integer.parseInt(str.substring(0,
						str.indexOf(" ")));
			}

			// compare peerAS with first AS in AS Path
			if (peerAS != firstASinASPath) {
				update = update.replaceFirst(String.valueOf(peerAS),
						String.valueOf(firstASinASPath));
			}

			// write fixed update
			bwr.write(update);
			bwr.newLine();

			// read next line
			update = br.readLine();
		}

		fr.close();
		bwr.close();
		fwr.close();

		// rename original file to .old
		File old = new File(updatesFilename + ".old");
		if (old.exists()) {
			throw new IOException("file " + updatesFilename
					+ ".old already exists!");
		} else {
			original.renameTo(old);

			// and then rename .converted to original
			converted.renameTo(original);
		}
	}
}
