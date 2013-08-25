package org.sapegin.bgp.analyse.tests.spikes;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.spikes.MonitoredAS;

public class MonitoredASTest {

	private MonitoredAS as;
	
	@Before
	public void testMonitoredAS() {
		as = new MonitoredAS("wide",12345);
	}

	@Test
	public void testGetMonitoringRouter() {
		assertTrue(as.getMonitoringRouter().equals("wide"));
	}

	@Test
	public void testGetMonitoredAS() {
		assertTrue(as.getMonitoredAS()==12345);
	}

}
