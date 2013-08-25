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
import org.sapegin.bgp.analyse.tests.BasicTest;
import org.sapegin.bgp.analyse.updates.UpdatesFactory;
import org.sapegin.bgp.analyse.updates.UpdatesFromVisibleASs;
import org.sapegin.bgp.analyse.visibility.MonitoredASs;

public class UpdatesFromVisibleASsTest extends BasicTest {

	ASsNames names;
	InternetMap iMap;
	
	@Before
	public void load() throws Exception {
		names = new ASsNames(inputASsFilenames);
		
		iMap = new InternetMap(properties.getProperty("map"),
				properties.getProperty("map_t1"),
				properties.getProperty("map_t2"),
				properties.getProperty("map_t3"));
	}

	@Test
	public void test() throws UnknownHostException {
		UpdatesFactory<MonitoredASs> updatesFactory = new UpdatesFactory<MonitoredASs>();
		MonitoredASs monitoredASs = new MonitoredASs(iMap, names, 32);
		
		UpdatesFromVisibleASs updates = (UpdatesFromVisibleASs) updatesFactory.createUpdates(false, null, false, false, monitoredASs, inputUpdatesFilenames, false);

		for (MonitoredAS monitoredAS : updates.getUpdateMap().keySet()) {
			assertFalse(monitoredAS.getMonitoredAS() == 16150); //16150 is not listed in ases names.
		}

		MonitoredAS monitoredAS = new MonitoredAS("updates_m_1",
				29073);
		
		assertEquals(7, updates.getUpdateMap().get(monitoredAS)
				.getCurrentUpdateSum());

		assertTrue(updates.getUpdateMap().get(monitoredAS).getSpikeAtTime(1243814456)
				.containsPrefix(InetAddress.getByName("1.1.1.0")));
	}

}
