package org.sapegin.bgp.analyse.tests.visibility;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.Colours;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.SizeInterval;
import org.sapegin.bgp.analyse.tests.BasicTest;
import org.sapegin.bgp.analyse.updates.Updates;
import org.sapegin.bgp.analyse.updates.UpdatesFactory;
import org.sapegin.bgp.analyse.visibility.MonitoredASs;
import org.sapegin.bgp.analyse.visibility.VisibleDuplicatedSpikes;

public class VisibleDuplicatedSpikesTest extends BasicTest {

	private VisibleDuplicatedSpikes<MonitoredASs> duplicated;
	InternetMap map;
	Updates updates;
	MonitoredASs monitored;
	ArrayList<SizeInterval> intervals;
	Colours colours;

	@Before
	public void prepare() throws Exception {

		// load AS names
		ASsNames names = new ASsNames(inputASsFilenames);

		map = new InternetMap(properties.getProperty("map"), null, null, null);
		
		int threads = Integer.parseInt(properties.getProperty("threads","1"));
		
		monitored = new MonitoredASs(map, names, threads);
		
		UpdatesFactory<MonitoredASs> updatesFactory = new UpdatesFactory<MonitoredASs>();
		
		updates = updatesFactory.createUpdates(false, null, false, false, monitored, inputUpdatesFilenames, false);

		SizeInterval interval = new SizeInterval("1..5");
		intervals = new ArrayList<SizeInterval>();
		intervals.add(interval);

		colours = new Colours("test/gnuplot_colours.txt");
	}

	@Test
	public void testVisibleDuplicatedSpikes() throws Exception {
		
		duplicated = new VisibleDuplicatedSpikes<MonitoredASs>(map, updates,
				intervals, 5, 0, 0, "", 120, 0.99, colours, monitored, false);

		duplicated.writeResults("test/updateCharts", "test/groupCharts",
				"test/groupGraphs", 120);
	}

}
