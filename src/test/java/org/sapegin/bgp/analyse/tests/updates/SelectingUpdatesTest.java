package org.sapegin.bgp.analyse.tests.updates;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.generics.MonitoredASsFactory;
import org.sapegin.bgp.analyse.spikes.SelectedSpikes;
import org.sapegin.bgp.analyse.updates.Updates;
import org.sapegin.bgp.analyse.updates.UpdatesFactory;
import org.sapegin.bgp.analyse.updates.UpdatesFromVisibleASs;
import org.sapegin.bgp.analyse.visibility.MonitoredASs;

public class SelectingUpdatesTest {
	
	protected ArrayList<String> inputUpdatesFilenames = new ArrayList<String>();
	protected ArrayList<String> inputASsFilenames = new ArrayList<String>();
	
	@Test
	public void test() throws Exception {
		loadFilenames("test/test_selecting_spikes/input_names_java");
		
		ASsNames names = new ASsNames(inputASsFilenames);
		
		InternetMap iMap = new InternetMap("test/test_selecting_spikes/map.20090601",null,null,null);
		
		UpdatesFactory<MonitoredASs> updatesFactory = new UpdatesFactory<MonitoredASs>();
		
		int threads = 3;
		
		MonitoredASs monitoredASs = new MonitoredASsFactory().create(iMap, names, 1, threads);
		
		Updates updates = (UpdatesFromVisibleASs) updatesFactory.createUpdates(false,
				null, false, false, monitoredASs, inputUpdatesFilenames, false);
		
		assertEquals(26546,new SelectedSpikes(updates.getSpikesWithPredefinedSize(0, 100, threads)).numberOfSpikes());
	}
	
	/**
	 * This procedure reads names of files with updates and names of ASes
	 * 
	 * These preparations are STRICTLY needed for reading data from files with
	 * updates!
	 * 
	 * @throws IOException
	 */
	private void loadFilenames(String inputFilenamesFilename)
			throws IOException {

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

			// read filenames
			inputUpdatesFilenames.add(inStr.substring(0, inStr.indexOf(":")));
			inputASsFilenames.add(inStr.substring(inStr.lastIndexOf(":") + 1));

			inStr = inputNamesBR.readLine();
		}

		inputNames.close();
	}

}
