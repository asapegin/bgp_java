package org.sapegin.bgp.analyse.tests.correlation;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.Colours;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.correlation.DuplicatedSpikesGroup;
import org.sapegin.bgp.analyse.spikes.MonitoredAS;
import org.sapegin.bgp.analyse.spikes.Spike;
import org.sapegin.bgp.analyse.tests.BasicTest;
import org.sapegin.bgp.analyse.updates.Updates;
import org.sapegin.bgp.analyse.updates.UpdatesFactory;
import org.sapegin.bgp.analyse.updates.UpdatesFromVisibleASs;
import org.sapegin.bgp.analyse.visibility.MonitoredASs;

public class DuplicatedSpikesGroupTest extends BasicTest {

	ASsNames names;
	Updates updates;
	MonitoredASs visible;
	InternetMap map;
	Colours colours;

	@Before
	public void readFilenames() throws Exception {

		names = new ASsNames(inputASsFilenames);

		map = new InternetMap(properties.getProperty("map"),
				properties.getProperty("map_t1"),
				properties.getProperty("map_t2"),
				properties.getProperty("map_t3"));

		UpdatesFactory<MonitoredASs> updatesFactory = new UpdatesFactory<MonitoredASs>();
		
		int threads = Integer.parseInt(properties.getProperty("threads","1"));

		// I'm going to analyse monitored ASs in this test. So
		// MonitoredASs.getVisibleASNames==names.getASNames!!!
		visible = new MonitoredASs(map, names, threads);

		updates = (UpdatesFromVisibleASs) updatesFactory.createUpdates(false,
				null, false, false, visible, inputUpdatesFilenames, false);

		colours = new Colours("test/gnuplot_colours.txt");

	}

	@Test
	public void test() {

		MonitoredAS as = new MonitoredAS("updates_m_1", 12956);
		long time = 1243814462;
		Spike spike = updates.getUpdateMap().get(as).getSpikeAtTime(time);

		DuplicatedSpikesGroup group = new DuplicatedSpikesGroup(as, spike,
				time, updates, map, 120, 0.99);

		assertEquals(group.getUpdateMap().keySet().size(), 3);
		assertEquals(
				group.getUpdateMap().get(new MonitoredAS("updates_m_2", 22548))
						.getNumberOfSpikes(), 4);

		group.printAllAround("test/group_charts", colours);
		group.printDOTGraph("test/group_graphs", visible, false);
	}
}
