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

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * 
 * This class just represents a pair {monitoringRouter,monitoredAS}
 * 
 * @author Andrey Sapegin
 * 
 */
public class MonitoredAS {
	private String monitoringRouter;
	private int monitoredAS;

	public MonitoredAS(String monitoringRouter, int monitoredAS) {
		this.monitoringRouter = monitoringRouter;
		this.monitoredAS = monitoredAS;
	}

	public String getMonitoringRouter() {
		return monitoringRouter;
	}

	public int getMonitoredAS() {
		return monitoredAS;
	}

	/**
	 * I override equals() as I want to use MonitoredAS as key in a HashMap
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		
		MonitoredAS as = (MonitoredAS) obj;
		
		return ((as.getMonitoredAS() == monitoredAS) && (as
				.getMonitoringRouter().equals(monitoringRouter)));
	}
	
	/**
	 * I override hashCode() as I want to use MonitoredAS as key in a HashMap
	 */
	@Override
	public int hashCode(){
		HashCodeBuilder hash = new HashCodeBuilder();
		hash.append(monitoredAS);
		hash.append(monitoringRouter);
		return hash.toHashCode();
	}
}
