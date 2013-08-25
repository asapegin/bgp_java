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
package org.sapegin.bgp.analyse.visibility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.DefaultEdge;
import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.AnalyseSpikesTask;
import org.sapegin.bgp.analyse.Colours;
import org.sapegin.bgp.analyse.MyDirectIntegerVertexNameProvider;
import org.sapegin.bgp.analyse.generics.ASsFactory;
import org.sapegin.bgp.analyse.updates.Updates;
import org.sapegin.bgp.analyse.updates.UpdatesFactory;

/**
 * 
 * @author Andrey Sapegin
 * 
 *         This class represents a task to analyse visible or monitored spikes.
 *         These spikes are represented by generic factory ASsToAnalyse. I'm
 *         forced to use factory to be able to instantiate generic classes
 *         inside this class.
 * 
 */
public class AnalyseVisibleSpikesTask<T extends ASsToAnalyse> extends
		AnalyseSpikesTask<T> {

	String filenameDOT;
	String updatesChartFolder;
	String groupChartFolder;
	String groupGraphFolder;
	
	private T visibleASs;

	private int numberOfSpikesToAnalyse;
	private long spikeTime;
	private int spikeAS;
	private String spikeMonitor;

	private Updates allUpdates;

	private Colours colours;

	protected AnalyseVisibleSpikesTask() {
	}

	public AnalyseVisibleSpikesTask(Properties properties,
			ASsFactory<T> factoryASs) throws Exception {

		super(properties, factoryASs);

		// read all names of monitored ASs
		logger.info("loading AS names...");
		ASsNames allASs = new ASsNames(inputASsFilenames);

		// select visible ASs with visibilityPercent
		// I don't know if i will get visible or monitored ASs from generic
		// factory,
		// so I just call all of them visible
		visibleASs = factoryASs.create(iMap, allASs, 1, 1);

		this.filenameDOT = properties.getProperty("dot_filename", "dotfile");

		this.updatesChartFolder = properties.getProperty(
				"all_selected_updates_chart_folder",
				"all_selected_visible_updates");
		this.groupChartFolder = properties.getProperty("group_charts_folder",
				"duplicated_selected_visible_updates");
		this.groupGraphFolder = properties.getProperty("group_graphs_folder",
				"groups_graphs");
		this.colours = new Colours(properties.getProperty("gnuplot_colours"));

		// load updates
		UpdatesFactory<ASsToAnalyse> updatesFactory = new UpdatesFactory<ASsToAnalyse>();

		logger.info("loading updates...");

		this.allUpdates = updatesFactory.createUpdates(useRIBs,
				inputRIBsFilenames, componentOnly, correlated, visibleASs,
				inputUpdatesFilenames, synchronise);

		// not equals 0 if the task is to analyse some random spikes
		this.numberOfSpikesToAnalyse = Integer.parseInt(properties.getProperty(
				"number_of_random_spikes_to_analyse_for_each_size_interval",
				"10"));
		// specified if numberOfSpikesToAnalyse==0
		if (this.numberOfSpikesToAnalyse == 0) {
			this.spikeTime = Long.parseLong(properties.getProperty(
					"spike_time", "0"));
			this.spikeAS = Integer.parseInt(properties.getProperty("spike_as",
					"0"));
			this.spikeMonitor = properties.getProperty("spike_monitor", "");
		}
	}

	/**
	 * This method calls selection of duplicated spikes among updates from
	 * visible (or monitored, depending on what generic class was used during
	 * reading of allUpdates) ASs.
	 * 
	 * @throws Exception
	 */
	public void selectVisibleDuplicatedSpikes() throws Exception {

		visibleDuplicatedSpikes = new VisibleDuplicatedSpikes<T>(iMap,
				allUpdates, sizeIntervals, numberOfSpikesToAnalyse, spikeTime,
				spikeAS, spikeMonitor, timeBuffer, duplicationPercentage,
				colours, visibleASs, componentOnly);
	}

	/**
	 * This method produce results of the analysis (graphs in DOT format and
	 * text data for charts)
	 */
	public void writeResults() {
		visibleDuplicatedSpikes.writeResults(updatesChartFolder,
				groupChartFolder, groupGraphFolder, timeBuffer);

		allUpdates.printAll(updatesChartFolder);

		// create DOT file for visible ASs graph
		visibleASs.writeDOT(filenameDOT + "_all_visible.dot");

		// create DOT file for biggest connected component of visible ASs graph
		DOTExporter<Integer, DefaultEdge> dotExporter = new DOTExporter<Integer, DefaultEdge>(
				new MyDirectIntegerVertexNameProvider(), null, null);
		try {
			dotExporter.export(new BufferedWriter(new FileWriter(filenameDOT
					+ "_biggest_visible_component.dot")),
					visibleASs.getBiggestConnectedGraphComponent());
		} catch (IOException e) {
			logger.error(
					"Can't write biggest connected component of visible ASs graph into DOT file",
					e);
		}

	}

}
