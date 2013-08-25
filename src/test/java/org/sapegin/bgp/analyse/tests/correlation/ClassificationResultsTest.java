package org.sapegin.bgp.analyse.tests.correlation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.correlation.ClassificationResults;
import org.sapegin.bgp.analyse.correlation.advanced.AdvancedClassificationResult;

public class ClassificationResultsTest{

	ClassificationResults results;
	
	@Before
	public void testClassificationResults() {
		this.results = new ClassificationResults();
		
		AdvancedClassificationResult result = new AdvancedClassificationResult(false, 100, 0.98f, (byte) 5, (long) 201, 50);
		this.results.addResult(result);
		
		result = new AdvancedClassificationResult(false, 101, 0.98f, (byte) 5, (long) 201, 50);
		this.results.addResult(result);
		
		result = new AdvancedClassificationResult(true, 101, 0.25f, null, null, 50);
		this.results.addResult(result);
		
		result = new AdvancedClassificationResult(true, 101, 1f, null, null, 50);
		this.results.addResult(result);
	}	

	@Test
	public void testGetSingleSpikesMax033Visible() {
		assertEquals(1,this.results.getSingleSpikesMax033Visible().getTotalNumberOfSpikes());
	}

	@Test
	public void testGetSingleSpikesMax066Visible() {
		assertEquals(0,this.results.getSingleSpikesMax066Visible().getTotalNumberOfSpikes());
	}

	@Test
	public void testGetSingleSpikes100Visible() {
		assertEquals(1,this.results.getSingleSpikes100Visible().getTotalNumberOfSpikes());
	}

	@Test
	public void testGetCorrelatedSpikesStats() {
		assertEquals(2,this.results.getCorrelatedSpikesStats().get((byte) 5).getTotalNumberOfSpikes());
		assertEquals(201,this.results.getCorrelatedSpikesStats().get((byte) 5).getTotalNumberOfPrefixes());
	}

	@Test
	public void testMerge() {
		ClassificationResults results2 = new ClassificationResults();
		AdvancedClassificationResult result = new AdvancedClassificationResult(false, 100, 0.98f, (byte) 5, (long) 201, 50);
		results2.addResult(result);
		
		this.results.merge(results2);
		assertEquals(3,this.results.getCorrelatedSpikesStats().get((byte) 5).getTotalNumberOfSpikes());
		assertEquals(301,this.results.getCorrelatedSpikesStats().get((byte) 5).getTotalNumberOfPrefixes());
	}

}
