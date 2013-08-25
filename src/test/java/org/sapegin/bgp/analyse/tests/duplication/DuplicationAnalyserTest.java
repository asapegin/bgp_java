package org.sapegin.bgp.analyse.tests.duplication;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.duplication.DuplicationAnalyser;
import org.sapegin.bgp.analyse.duplication.DuplicationStats;
import org.sapegin.bgp.analyse.generics.MonitoredASsFactory;
import org.sapegin.bgp.analyse.tests.BasicTest;
import org.sapegin.bgp.analyse.updates.Updates;
import org.sapegin.bgp.analyse.updates.UpdatesFactory;
import org.sapegin.bgp.analyse.visibility.MonitoredASs;

public class DuplicationAnalyserTest extends BasicTest {

	Updates updates1;
	Updates updates2;

	@Before
	public void test() throws Exception {
		InternetMap map = new InternetMap(properties.getProperty("map"),
				null,null,null);
		
		// read data from the first test file
		ASsNames ases1 = new ASsNames(new ArrayList<String>(
				Arrays.asList(this.inputASsFilenames.get(0))));
		MonitoredASs monitoredASs1 = new MonitoredASsFactory().create(map, ases1, 1, 1);
		this.updates1 = new UpdatesFactory<MonitoredASs>().createUpdates(false, null, false,
				false, monitoredASs1,
				new ArrayList<String>(Arrays.asList(this.inputUpdatesFilenames.get(0))), false);
		
		// read data from the second test file
		ASsNames ases2 = new ASsNames(new ArrayList<String>(
				Arrays.asList(this.inputASsFilenames.get(1))));
		MonitoredASs monitoredASs2 = new MonitoredASsFactory().create(map, ases2, 1, 1);
		this.updates2 = new UpdatesFactory<MonitoredASs>().createUpdates(false, null, false,
				false, monitoredASs2,
				new ArrayList<String>(Arrays.asList(this.inputUpdatesFilenames.get(1))), false);
	}
	
	@Test
	public void testAnalyser(){

		DuplicationAnalyser analyser = new DuplicationAnalyser(updates1,
				6, 0.99);
		
		analyser.calculateDuplication();

		DuplicationStats singleCollectorStats = analyser.getStats();

		assertEquals(4, singleCollectorStats.getNumberOfDuplicatedSpikes());
		assertEquals(15, singleCollectorStats.getNumberOfPrefixesInDuplicatedSpikes());
		assertEquals(15, singleCollectorStats.getNumberOfDuplicatedPrefixesInDuplicatedSpikes());
		assertEquals(7,singleCollectorStats.getTotalNumberOfSpikes());
		assertEquals(20,singleCollectorStats.getTotalNumberOfPrefixes());
		
		analyser = new DuplicationAnalyser(updates2,
				5, 0.66);
		
		analyser.calculateDuplication();

		singleCollectorStats = analyser.getStats();

		assertEquals(0, singleCollectorStats.getNumberOfDuplicatedSpikes());
		assertEquals(0, singleCollectorStats.getNumberOfPrefixesInDuplicatedSpikes());
		assertEquals(0, singleCollectorStats.getNumberOfDuplicatedPrefixesInDuplicatedSpikes());
		assertEquals(7,singleCollectorStats.getTotalNumberOfSpikes());
		assertEquals(20,singleCollectorStats.getTotalNumberOfPrefixes());
	}

}
