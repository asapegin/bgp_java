package org.sapegin.bgp.analyse.tests.analyse;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sapegin.bgp.analyse.SizeInterval;

public class SizeIntervalTest {
	
	@Test
	public void test() {
		SizeInterval interval = new SizeInterval("-100..200");
		assertEquals(-100, interval.getMinSize());
		assertEquals(200, interval.getMaxSize());
		
		interval = new SizeInterval("12345..12345");
		assertEquals(12345, interval.getMinSize());
		assertEquals(12345, interval.getMaxSize());
	}

}
