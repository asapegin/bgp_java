package org.sapegin.bgp.analyse.tests.visibility;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.tests.BasicTest;
import org.sapegin.bgp.analyse.visibility.MonitoredASs;

public class MonitoredASsTest extends BasicTest {

	private MonitoredASs monitoredASs;
	private InternetMap map;
	private ASsNames names;
	private int threads;

	@Before
	public void initialise() {

		try {
			map = new InternetMap(properties.getProperty("map"),
					properties.getProperty("map_t1"),
					properties.getProperty("map_t2"),
					properties.getProperty("map_t3"));
		} catch (Exception e) {
			e.printStackTrace();
			fail("can't create map");
		}

		names = new ASsNames(inputASsFilenames);
		
		threads = Integer.parseInt(properties.getProperty("threads","1"));

		monitoredASs = new MonitoredASs(map, names, threads);
	}

	@Test
	public void testVisibleASs() {
		monitoredASs = new MonitoredASs(map, names, threads);
	}

	@Test
	public void testGetVisibleASsNames() {
		ArrayList<Integer> visible = monitoredASs.getVisibleASsNames();

		assertTrue(visible.contains(22548));
		assertTrue(visible.contains(24875));
		assertTrue(visible.contains(1234));
		assertTrue(visible.contains(13101));
		assertTrue(visible.contains(29073));
		assertFalse(visible.contains(5432));
		assertFalse(visible.contains(43210));
		assertFalse(visible.contains(12345));
	}

}
