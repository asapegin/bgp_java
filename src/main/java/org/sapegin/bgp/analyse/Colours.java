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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         Class containing gnuplot colours. Needed for preparing gnuplot
 *         scripts. Methods of this class assume that 2 colours with the most
 *         far from each other POSITION IN THE FILE are most different for the
 *         human eye.
 * 
 */
public class Colours {
	private ArrayList<String> colours;
	private ArrayList<Integer> selectedColours = new ArrayList<Integer>();

	protected Logger logger = LogManager.getLogger(Colours.class);

	public Colours(String gnuplotColoursFilename) {
		this.colours = readColoursFromFile(gnuplotColoursFilename);
	}

	/**
	 * Read colour names from file. 1 string - 1 colour name, no columns
	 */
	private ArrayList<String> readColoursFromFile(String gnuplotColoursFilename) {
		ArrayList<String> colours = new ArrayList<String>();

		try {
			FileReader fr = new FileReader(gnuplotColoursFilename);
			BufferedReader br = new BufferedReader(fr);

			String str = br.readLine();

			while (str != null) {
				if (str.length() > 2) {
					colours.add(str);
				} else {
					logger.error("Colour name loaded is too short! Please check if "
							+ gnuplotColoursFilename
							+ " contains valid gnuplot colour names!");
				}
				str = br.readLine();
			}
			
			br.close();
			
		} catch (IOException e) {
			logger.error("Error during reading gnuplot colour names from file",
					e);
		}

		return colours;
	}

	/**
	 * 
	 * @return gnuplot colour, with the biggest distance (position in the
	 *         ArrayList) from previously returned (by the class instance)
	 *         colours.
	 */
	public String getNext() {
		if (colours.size() == 0) {
			logger.fatal("No colours available. Returning null...");
			return null;
		}

		if (selectedColours.size() == 0) {
			selectedColours.add(0);
			return colours.get(0);
		} else {
			// select the most distant colour in the array (from other selected
			// colours)
			int mostDistant = 0;
			int distance = 0;

			for (int i = 0; i < colours.size(); i++) {
				int minDistance = 1000; // infinity

				// find the distance to the closest previously selected colour
				for (Integer position : selectedColours) {
					if (Math.abs(i - position) < minDistance) {
						minDistance = Math.abs(i - position);
					}
				}

				// check if this colour has a position with the biggest
				// minDistance to previously selected colour
				if (minDistance > distance) {
					distance = minDistance;
					mostDistant = i;
				}
			}

			// return this element
			selectedColours.add(mostDistant);
			return colours.get(mostDistant);
		}
	}

	/**
	 * Clear saved list of previously returned colours.
	 */
	public void clearStats() {
		this.selectedColours.clear();
	}
}
