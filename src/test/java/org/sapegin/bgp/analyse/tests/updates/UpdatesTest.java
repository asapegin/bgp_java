package org.sapegin.bgp.analyse.tests.updates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.spikes.Destination;
import org.sapegin.bgp.analyse.spikes.MonitoredAS;
import org.sapegin.bgp.analyse.spikes.SelectedSpikes;
import org.sapegin.bgp.analyse.spikes.Spike;
import org.sapegin.bgp.analyse.spikes.SpikeCollection;
import org.sapegin.bgp.analyse.tests.BasicTest;
import org.sapegin.bgp.analyse.updates.Updates;
import org.sapegin.bgp.analyse.updates.UpdatesFactory;
import org.sapegin.bgp.analyse.updates.UpdatesFromVisibleASs;
import org.sapegin.bgp.analyse.visibility.MonitoredASs;

public class UpdatesTest extends BasicTest {

	ASsNames names;
	Updates updates;
	InternetMap iMap;

	@Before
	public void load() throws Exception {

		names = new ASsNames(inputASsFilenames);
		
		iMap = new InternetMap(properties.getProperty("map"),
				properties.getProperty("map_t1"),
				properties.getProperty("map_t2"),
				properties.getProperty("map_t3"));

		UpdatesFactory<MonitoredASs> updatesFactory = new UpdatesFactory<MonitoredASs>();
		
		int threads = Integer.parseInt(properties.getProperty("threads","1"));
		
		MonitoredASs monitoredASs = new MonitoredASs(iMap, names, threads);
		
		updates = (UpdatesFromVisibleASs) updatesFactory.createUpdates(false,
				null, false, false, monitoredASs, inputUpdatesFilenames, false);
	}

	@Test
	public void testUpdates() throws UnknownHostException {

		for (MonitoredAS monitoredAS : updates.getUpdateMap().keySet()) {
			assertFalse(monitoredAS.getMonitoredAS() == 16150);
		}

		MonitoredAS monitoredAS = new MonitoredAS("updates_m_1",
				29073);
		assertEquals(updates.getUpdateMap().get(monitoredAS)
				.getCurrentUpdateSum(), 7);

		assertTrue(updates.getUpdateMap().get(monitoredAS).getSpikeAtTime(1243814456)
				.containsPrefix(InetAddress.getByName("1.1.1.0")));
		
//		updates.printAll("test_spikes");
	}

	@Test
	public void testGetWithPredefindSize() throws InterruptedException {
								 // 2 < spikes.size <=5
		assertEquals(3, updates.getSpikesWithPredefinedSize(2, 5, 4).getUpdateMap().size());
		assertEquals(16, new SelectedSpikes(updates.getSpikesWithPredefinedSize(2, 5, 4)).numberOfUpdates());
//		System.out.println(new SelectedSpikes(updates.getSpikesWithPredefinedSize(1, 3, 4)).numberOfSpikes());
		assertEquals(2, updates.getSpikesWithPredefinedSize(1, 3, 4).getUpdateMap().size());
		assertEquals(7, new SelectedSpikes(updates.getSpikesWithPredefinedSize(1, 3, 4)).numberOfUpdates());
	}

	@Test
	public void testFindDuplicated() {

		MonitoredAS as = new MonitoredAS("updates_m_1", 12956);
		long time = 1243814462;
		Spike spike = updates.getUpdateMap().get(as).getSpikeAtTime(time);

		SpikeCollection collection = updates.findDuplicatedSpikes(spike, time,
				120, as, 0.99);

		assertEquals(2, collection.getUpdateMap().keySet().size()); // AS 12956
																	// will not
																	// be
																	// included,
																	// as
																	// findDuplicatedSpikes
																	// does not
																	// return
																	// the given
																	// spike
																	// itself
																	// and there
																	// should be
																	// no more
																	// spikes
																	// from AS
																	// 12956
		assertEquals(
				collection.getUpdateMap()
						.get(new MonitoredAS("updates_m_2", 22548))
						.getNumberOfSpikes(), 4); // without second part of time sync
	}

	@Test
	public void test() {
		updates.printAll("test/all_updates_chart");
	}
	
	@Test
	/*
	 * test if the procedure correctly calculates the number of spikes actually marked as correlated
	 */
	public void testgetDuplicatedPrefixesInSpikeIfSpikeIsDuplicated() throws UnknownHostException{
		ArrayList<Destination> prefixes = new ArrayList<Destination>();
		prefixes.add(new Destination(InetAddress.getByName("1.1.1.0")));
		prefixes.add(new Destination(InetAddress.getByName("1.1.1.0")));
		prefixes.add(new Destination(InetAddress.getByName("1.11.111.0")));
		Spike spike = new Spike(prefixes);
		
		// get random as
		MonitoredAS as = updates.getUpdateMap().keySet().iterator().next();
		
		Set<InetAddress> markedPrefixes = updates.getDuplicatedPrefixesInSpikeIfSpikeIsDuplicated(spike, updates.getUpdateMap().get(as).getCurrentMinTime(), 1000, as, 0.1);
		
		//System.out.println(markedPrefixes.size());
		assertEquals(1, markedPrefixes.size());
		assertEquals(InetAddress.getByName("1.1.1.0"),markedPrefixes.iterator().next());
	}
}
