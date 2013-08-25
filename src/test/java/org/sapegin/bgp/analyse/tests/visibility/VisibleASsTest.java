package org.sapegin.bgp.analyse.tests.visibility;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.tests.BasicTest;
import org.sapegin.bgp.analyse.visibility.VisibleASs;

public class VisibleASsTest extends BasicTest {

	private VisibleASs visibleASs;
	private InternetMap map;
	private ASsNames names;
	private int threads;

	@Before
	public void initialise() throws Exception {

		map = new InternetMap(properties.getProperty("map"),
					properties.getProperty("map_t1"),
					properties.getProperty("map_t2"),
					properties.getProperty("map_t3"));

		names = new ASsNames(inputASsFilenames);
		
		threads = Integer.parseInt(properties.getProperty("threads","1"));
	}
	
	@Test
	public void testGetVisibleASsNames() {
		visibleASs = new VisibleASs(map, names, 0.99f, threads);
		ArrayList<Integer> visible = visibleASs.getVisibleASsNames();

		assertFalse(visible.contains(22548));
		assertTrue(visible.contains(24875));
		assertFalse(visible.contains(1234));
		assertTrue(visible.contains(13101));
		assertFalse(visible.contains(29073));
		assertFalse(visible.contains(5432));
	}
}
