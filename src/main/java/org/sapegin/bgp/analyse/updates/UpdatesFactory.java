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
package org.sapegin.bgp.analyse.updates;

import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sapegin.bgp.analyse.ribs.OriginsOnlyRIB;
import org.sapegin.bgp.analyse.ribs.RIB;
import org.sapegin.bgp.analyse.spikes.SingleASspikes;
import org.sapegin.bgp.analyse.visibility.ASsToAnalyse;

/**
 * 
 * @author Andrey Sapegin
 * 
 * @param <T>
 *            - type of Ass to analyse
 * 
 *            creates an instance of Updates subclass depending on parameters
 *            (analysis type)
 */
public class UpdatesFactory<T extends ASsToAnalyse> {

	private Logger logger = LogManager.getLogger(UpdatesFactory.class);

	/**
	 * This method returns instance of Updates subclass selected based on
	 * parameters
	 * 
	 * @param useRIBs
	 * @param inputRIBsFilenames
	 * @param componentOnly
	 *            - load updates only from ASs in biggest connected component of
	 *            visibleASs
	 * @param visibleASs
	 * @param inputASsFilenames
	 * @param synchronise
	 *            -
	 * @return
	 */
	public Updates createUpdates(boolean useRIBs,
			ArrayList<String> inputRIBsFilenames, boolean componentOnly,
			boolean correlated, T visibleASs,
			ArrayList<String> inputUpdatesFilenames, boolean synchronise) {

		Updates updates;

		// read Updates from visible/monitored ASs only
		// ////// ?????????????? And what with neighbours of visible ASs???????
		// And also with AS path including ONLY visible/monitored ASs
		// I want to find a root cause, so I want to analyse only prefixes
		// located in available graph of visible/monitored ASs
		if (useRIBs) { // if monitoring routers' RIBs are available
						// only updates with AS Path including only VisibleASs
						// will be loaded and analysed

			// prepare updates
			if (correlated) {
				// load OriginsASOnlyRIB for every monitor router
				logger.info("loading RIBs with origins ASes only...");
				ArrayList<OriginsOnlyRIB> allRIBs = new ArrayList<OriginsOnlyRIB>();
				for (String inputRIBfilename : inputRIBsFilenames) {
					allRIBs.add(new OriginsOnlyRIB(inputRIBfilename, visibleASs
							.getVisibleASsNames()));
				}
				logger.info("RIBs loaded.");
				
				updates = new UpdatesFromVisibleASsWithOriginAS(inputUpdatesFilenames,
						allRIBs, visibleASs.getVisibleASsNames(),
						synchronise);
			} else {
				
				// load RIB with full AS Path for every monitor router
				logger.info("loading RIBs...");
				ArrayList<RIB> allRIBs = new ArrayList<RIB>();
				for (String inputRIBfilename : inputRIBsFilenames) {
					allRIBs.add(new RIB(inputRIBfilename, visibleASs
							.getVisibleASsNames()));
				}
				logger.info("RIBs loaded.");
				
				if (componentOnly) {
					updates = new UpdatesWithVisibleAS_Path(
							inputUpdatesFilenames, allRIBs,
							new ArrayList<Integer>(visibleASs
									.getBiggestConnectedGraphComponent()
									.vertexSet()), synchronise);
				} else {
					updates = new UpdatesWithVisibleAS_Path(
							inputUpdatesFilenames, allRIBs,
							visibleASs.getVisibleASsNames(), synchronise);
				}
			}
		} else { // if RIBs are not available
					// currently I'm forced to use all updates coming from
					// Visible ASs,
					// without AS Path filtering
					//
					// The problem is to determine AS Path for prefix
					// withdrawals.
					// This problem could be solved without having a RIB,
					// just recreating one by tracing of all previous updates
					// for long enough time period.
					// However, at this step I'm not going to implement this
					// idea as it needs time which I think I don't have now.
			if (componentOnly) {
				updates = new UpdatesFromVisibleASs(inputUpdatesFilenames,
						new ArrayList<Integer>(visibleASs
								.getBiggestConnectedGraphComponent()
								.vertexSet()), synchronise);
			} else {
				updates = new UpdatesFromVisibleASs(inputUpdatesFilenames,
						visibleASs.getVisibleASsNames(), synchronise);
			}
		}
		
		// load updates
		updates.readUpdates();

		if (synchronise) {
			// print out stats
			int total = 0;
			for (SingleASspikes spikes : updates.getUpdateMap().values()) {
				total += spikes.getCurrentUpdateSum();
			}

			logger.info(total
					+ " update messages from "
					+ updates.getUpdateMap().keySet().size()
					+ " monitored ASs loaded before synchronisation. Syncing time...");

			updates.synchronise();
		}

		// print out stats
		int total = 0;
		for (SingleASspikes spikes : updates.getUpdateMap().values()) {
			total += spikes.getCurrentUpdateSum();
		}

		logger.info(total + " update messages from "
				+ updates.getUpdateMap().keySet().size()
				+ " monitored ASs loaded");

		return updates;
	}
}
