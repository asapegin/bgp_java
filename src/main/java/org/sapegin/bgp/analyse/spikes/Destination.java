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

import java.net.InetAddress;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.sapegin.bgp.analyse.ribs.ASPathElement;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         this class present a BGP destination network prefix the origin AS
 *         could be used to calculate additional statistic
 * 
 */

public class Destination {
	private InetAddress prefix;
	private ASPathElement originAS;

	/**
	 * Adds prefix without originated AS
	 * 
	 * @param prefix
	 */
	public Destination(InetAddress prefix) {
		this.prefix = prefix;
		this.originAS = null;
	}

	public Destination(InetAddress prefix, ASPathElement originAS) {
		this.prefix = prefix;
		this.originAS = originAS;
	}

	@Override
	public int hashCode(){
		if (originAS == null) {
		return new HashCodeBuilder().append(prefix.hashCode()).toHashCode();
		} else{
			return new HashCodeBuilder().append(prefix.hashCode()).append(originAS.hashCode()).toHashCode();
		}
	}
	
	@Override
	public boolean equals(Object anotherDestination) {
		if (anotherDestination != null
				&& (anotherDestination instanceof Destination)) {

			if (((Destination) anotherDestination).getPrefix().equals(prefix)) {

				ASPathElement anotherOrigin = ((Destination) anotherDestination)
						.getOriginAS();

				if ((anotherOrigin == null) && (this.originAS == null)) {
					return true;
				} else {

					if (anotherOrigin.equals(originAS)) {
						return true;
					} else {
						return false;
					}
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public InetAddress getPrefix() {
		return this.prefix;
	}

	public ASPathElement getOriginAS() {
		return this.originAS;
	}
}
