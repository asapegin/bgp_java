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
package org.sapegin.bgp.analyse.generics;

import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.visibility.MonitoredASs;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         This is one of factories needed to be able to instantiate generic
 *         ASsToAnalyse inside other classes.
 * 
 */
public class MonitoredASsFactory implements ASsFactory<MonitoredASs> {

	@Override
	public MonitoredASs create(InternetMap iMap, ASsNames names, float visibilityPercent, int threads) {
		// i need visibility percent for compatibility only
		// i don't use it, because I just get monitored ASs only
		return new MonitoredASs(iMap, names, threads);
	}


}
