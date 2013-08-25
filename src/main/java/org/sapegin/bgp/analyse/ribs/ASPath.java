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

import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         This class stores the AS Path and contains methods for parsing it.
 * 
 *         Only one-AS and multi-AS (marked with '[' and ']' ) elements are
 *         supported
 * 
 */
public class ASPath {

	private ArrayList<ASPathElement> asPath;

	// logger
	protected Logger logger = LogManager.getLogger(ASPath.class);

	protected ASPath() {
	}

	public ASPath(ArrayList<ASPathElement> asPath) {
		this.asPath = asPath;
	}

	/**
	 * parse AS PAth from String
	 * 
	 * @param asPath
	 */
	public ASPath(String asPath) {
		// initialize array
		this.asPath = new ArrayList<ASPathElement>();

		// split as path to elements
		String[] elements = asPath.split(" ");

		try {
			int i = 0;
			while (i < elements.length) {
				if (!elements[i].startsWith("[")) { // if AS_PATH element is
													// simple (does not start
													// with '[') - add it to
													// array
					int as = Integer.parseInt(elements[i]); // parse AS number
					// prepare an array with single AS number
					ArrayList<Integer> singleAS = new ArrayList<Integer>();
					singleAS.add(as);
					// create and add element to AS Path
					ASPathElement element = new ASPathElement(singleAS);
					this.asPath.add(element);

					i++;
				} else { // if AS_PATH element consist of multiple ASs

					elements[i] = elements[i].substring(1); // remove "[" from
															// the beginning

					ArrayList<Integer> multiAS = new ArrayList<Integer>();
					while (!elements[i].endsWith("]")) {
						int as = Integer.parseInt(elements[i]);
						multiAS.add(as);

						i++;
					}

					// now parse element with "]"
					elements[i] = elements[i].substring(0,
							elements[i].length() - 1);
					int as = Integer.parseInt(elements[i]);
					multiAS.add(as);

					i++;
				}
			}
		} catch (NumberFormatException e) {
			logger.trace("Can't parse the name of AS in AS Path! Update message will be skipped!");
		} catch (Exception e) {
			logger.warn(
					"Unexpected exception during parsing AS Path! Update message will be skipped!",
					e);
		}
	}

	public ArrayList<ASPathElement> getASPath() {
		return this.asPath;
	}

	/**
	 * get last (from the left) element of AS Path
	 * 
	 * @return
	 */
	public ASPathElement getOriginAS() {
		if (this.asPath.size() > 0) {
			return this.asPath.get(this.asPath.size() - 1);
		} else
			return null;
	}

	/**
	 * get list of all ASs in the AS Path, without dividing them into elements.
	 * 
	 * @return
	 */
	public ArrayList<Integer> getASList() {
		ArrayList<Integer> asList = new ArrayList<Integer>();

		for (ASPathElement element : this.asPath) {
			for (Integer as : element.getASPathElement()) {
				asList.add(as);
			}
		}

		return asList;
	}
}
