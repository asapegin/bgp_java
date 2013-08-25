package org.sapegin.bgp.analyse.tests.correlation.advanced;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.correlation.advanced.AdvancedClassificationResult;

public class AdvancedClassificationResultTest {

	AdvancedClassificationResult result_single;
	AdvancedClassificationResult result_correlated;
	
	@Before
	public void testClassificationResult() {
		this.result_single = new AdvancedClassificationResult(true, 50, 0.33f, null, null, 20);
		this.result_correlated = new AdvancedClassificationResult(false, 30, 0.33f, (byte) 3, (long) 211, 5);
	}

	@Test
	public void testIsSingle() {
		assertTrue(this.result_single.isSingle());
		assertFalse(this.result_correlated.isSingle());
	}

	@Test
	public void testGetVisibility() {
		assertTrue(0.33f == this.result_correlated.getVisibility());
	}

	@Test
	public void testGetMaxDistance() {
		assertTrue(this.result_single.getMaxDistance() == null);
		assertTrue(this.result_correlated.getMaxDistance() == 3);
	}

	@Test
	public void testGetMaxTimeDifference() {
		assertTrue(this.result_single.getMaxTimeDifference() == null);
		assertTrue(this.result_correlated.getMaxTimeDifference() == 211);
	}

	@Test
	public void testGetOriginASs() {
		assertTrue(this.result_correlated.getOriginASs() == 5);
	}

	@Test
	public void testGetSize() {
		assertTrue(this.result_correlated.getSize() == 30);
	}

}
