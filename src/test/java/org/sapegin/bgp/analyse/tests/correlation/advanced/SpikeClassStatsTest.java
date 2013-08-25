package org.sapegin.bgp.analyse.tests.correlation.advanced;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.correlation.advanced.SpikeClassStats;

public class SpikeClassStatsTest {

	private SpikeClassStats stats;
	
	@Before
	public void testSpikeClassStats() {
		this.stats = new SpikeClassStats();
		
		stats.addSpikeStats(5, (long) 100, 100);
		stats.addSpikeStats(10, (long) 200, 200);
		stats.addSpikeStats(15, (long) 300, 300);
		stats.addSpikeStats(20, (long) 400, 400);
		stats.addSpikeStats(25, (long) 500, 500);
		stats.addSpikeStats(30, (long) 550, 550);
	}

	@Test
	public void testGetMaxGroupTimes() {
		assertEquals(6, stats.getMaxGroupTimes().size());
		assertTrue(stats.getMaxGroupTimes().contains((long) 300));
	}

	@Test
	public void testGetOriginASsInGroups() {
		assertEquals(6,stats.getOriginASsInGroups().size());
		assertEquals(550, (int) stats.getOriginASsInGroups().get(5));
	}

	@Test
	public void testGetTotalNumberOfSpikes() {
		assertEquals(6,stats.getTotalNumberOfSpikes());
	}

	@Test
	public void testGetTotalNumberOfPrefixes() {
		assertEquals(105,stats.getTotalNumberOfPrefixes());
	}

	@Test
	public void testMergeWith() {
		SpikeClassStats copiedStats = new SpikeClassStats();
		copiedStats.mergeWith(this.stats);
		
		SpikeClassStats stats2 = new SpikeClassStats();
		stats2.addSpikeStats(1, (long) 2, 3);
		stats2.addSpikeStats(2, (long) 3, 4);
		stats2.addSpikeStats(4, (long) 5, 6);
		
		copiedStats.mergeWith(stats2);
		
		assertEquals(9, copiedStats.getTotalNumberOfSpikes());
		assertEquals(112, copiedStats.getTotalNumberOfPrefixes());
	}

	@Test
	public void testGetTimeQuartiles() {
		assertEquals(550,stats.getOriginsQuartiles().getMax());
		assertEquals(100,stats.getOriginsQuartiles().getMin());
		
		// I use SAS Method 5 to calculate quartiles
		assertTrue(200==stats.getOriginsQuartiles().getFirstQuartile());
		
		assertTrue(350==stats.getOriginsQuartiles().getMediana());
		
		assertTrue(500==stats.getOriginsQuartiles().getThirdQuartile());
	}

	@Test
	public void testGetOriginsQuartiles() {
		assertEquals(550,stats.getTimeQuartiles().getMax());
		assertEquals(100,stats.getTimeQuartiles().getMin());
		
		assertTrue(200==stats.getTimeQuartiles().getFirstQuartile());
		assertTrue(350==stats.getTimeQuartiles().getMediana());
		assertTrue(500==stats.getTimeQuartiles().getThirdQuartile());
	}

}
