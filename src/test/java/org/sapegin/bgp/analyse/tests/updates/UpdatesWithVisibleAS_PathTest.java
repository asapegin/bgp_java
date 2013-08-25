package org.sapegin.bgp.analyse.tests.updates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.spikes.MonitoredAS;
import org.sapegin.bgp.analyse.tests.GenericTestWithRIB;
import org.sapegin.bgp.analyse.updates.UpdatesFactory;
import org.sapegin.bgp.analyse.updates.UpdatesWithVisibleAS_Path;
import org.sapegin.bgp.analyse.visibility.MonitoredASs;

public class UpdatesWithVisibleAS_PathTest extends GenericTestWithRIB {

	ASsNames names;
	InternetMap iMap;
	int threads;
	
	@Before
	public void loadASsAndRIBs() throws Exception {
		names = new ASsNames(inputASsFilenames);

		iMap = new InternetMap(properties.getProperty("map"),
				properties.getProperty("map_t1"),
				properties.getProperty("map_t2"),
				properties.getProperty("map_t3"));
		
		threads = Integer.parseInt(properties.getProperty("threads","1"));
	}

	@Test
	public void test() throws UnknownHostException {
		UpdatesFactory<MonitoredASs> updatesFactory = new UpdatesFactory<MonitoredASs>();
		MonitoredASs monitoredASs = new MonitoredASs(iMap, names, threads);

		UpdatesWithVisibleAS_Path updates = (UpdatesWithVisibleAS_Path) updatesFactory
				.createUpdates(true, inputRIBsFilenames, false, false, monitoredASs,
						inputUpdatesFilenames, false);

		for (MonitoredAS monitoredAS : updates.getUpdateMap().keySet()) {
			assertFalse(monitoredAS.getMonitoredAS() == 16150); // 16150 is not
																// listed in
																// ases names.
		}

		// there should be only 1 spike with 2 updates with all visible ASs in
		// AS Path!!!
		assertEquals(1, updates.getUpdateMap().size());
		// this AS should be 29073
		assertEquals(updates.getUpdateMap().keySet().iterator().next()
				.getMonitoredAS(), 29073);

		MonitoredAS monitoredAS = new MonitoredAS("updates_m_1", 29073);

		assertEquals(updates.getUpdateMap().get(monitoredAS)
				.getCurrentUpdateSum(), 2);

		assertTrue(updates.getUpdateMap().get(monitoredAS)
				.getSpikeAtTime(1243814456)
				.containsPrefix(InetAddress.getByName("1.1.1.0")));
	}
}
