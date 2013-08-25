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

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * 
 * @author Andrey Sapegin
 * 
 * presents an element of AS Path. Consists of list of ASs (usually 1 AS only)
 *
 */
public class ASPathElement {

	private ArrayList<Integer> asElement;

	public ASPathElement(ArrayList<Integer> asElement) {
		this.asElement = asElement;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hashCode = new HashCodeBuilder();
		for (Integer as : asElement) {
			hashCode.append(as.hashCode());
		}
		
		return hashCode.toHashCode();
	}

	@Override
	public boolean equals(Object otherElement) {

		if (otherElement != null && (otherElement instanceof ASPathElement)) {

			if (((ASPathElement) otherElement).getASPathElement().equals(
					asElement)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public ArrayList<Integer> getASPathElement() {
		return this.asElement;
	}

}
