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

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Andrey Sapegin
 * 
 * this class contains a dynamic array with distances from one AS (not known for this class) to all ther ASs in Internet.
 *
 */
public class Distances {
	
	private Map<Integer,Byte> distances = new HashMap<Integer,Byte>();
	
	public void setDistance(int destinationASnumber, byte distance){
		distances.put(destinationASnumber, distance);
	}
	
	public Byte getDistance(int destinationASnumber){
		return distances.get(destinationASnumber);
	}
}
