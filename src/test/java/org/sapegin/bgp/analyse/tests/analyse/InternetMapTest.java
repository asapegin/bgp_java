package org.sapegin.bgp.analyse.tests.analyse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.tests.BasicTest;

public class InternetMapTest extends BasicTest{

	@Test
	public void testInternetMap() {
		boolean success = false;
				
		try {
			InternetMap map = new InternetMap(properties.getProperty("map"),
					properties.getProperty("map_t1"),
					properties.getProperty("map_t2"),
					properties.getProperty("map_t3"));
			
			assertTrue(map.areConnected(22548, 29686));
			assertTrue(map.areConnected(22548, 14444));
			assertFalse(map.areConnected(1526, 1525));
			assertFalse(map.areConnected(22548, 13101));
			assertFalse(map.areConnected(5432, 2222));
			
			InternetMap map1 = new InternetMap(null,
					properties.getProperty("map_t1"),
					properties.getProperty("map_t2"),
					properties.getProperty("map_t3"));
			
			assertTrue(map1.areConnected(22548, 29686));
			assertTrue(map1.areConnected(22548, 14444));
			assertTrue(map1.areConnected(1526, 1525));
			assertFalse(map1.areConnected(22548, 13101));
			assertFalse(map1.areConnected(5432, 2222));
			
			InternetMap map2 = new InternetMap(properties.getProperty("map"),
					null,
					null,
					null);
			
			assertTrue(map2.areConnected(22548, 29686));
			assertTrue(map2.areConnected(22548, 14444));
			assertFalse(map2.areConnected(1526, 1525));
			assertTrue(map2.areConnected(22548, 13101));
			assertFalse(map2.areConnected(5432, 2222));
			assertFalse(map2.areConnected(1001, 2222));
			
			InternetMap map3 = new InternetMap(null,
					properties.getProperty("map_t1"),
					null,
					null);
			
			assertTrue(map3.areConnected(22548, 29686));
			assertTrue(map3.areConnected(22548, 14444));
			assertTrue(map3.areConnected(1526, 1525));
			assertFalse(map3.areConnected(22548, 13101));
			assertFalse(map3.areConnected(5432, 2222));
			assertTrue(map3.areConnected(1001, 2222));
			
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertTrue(success);
	}
	
	public void testDistanceCalculation() throws Exception{
		InternetMap map = new InternetMap(properties.getProperty("map"), null, null, null);
		
		assertEquals((byte) 2, (byte) map.getInternetDistanceBFS(1001, 22548));
		assertEquals((byte) 2, (byte) map.getInternetDistanceBFS(22548, 1001));
		assertEquals((byte) 3, (byte) map.getInternetDistanceBFS(1001, 24875));
		assertEquals((byte) 127, (byte) map.getInternetDistanceBFS(12985, 14444));
		assertEquals((byte) 5, (byte) map.getInternetDistanceBFS(16445, 14444));
		assertEquals((byte) 2, (byte) map.getInternetDistanceBFS(1525, 13101));
	}
}
