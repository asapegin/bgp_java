package org.sapegin.bgp.analyse.tests.duplication;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.duplication.DuplicationStats;

public class DuplicationStatsTest {

	DuplicationStats stats;
	
	@Before
	public void createStats(){
		this.stats = new DuplicationStats();
		stats.addDuplicated(5, 4, 0);
		stats.addSingle(3, 0);
		stats.addSingle(3, 0);
		stats.addSingle(5, 0);
		stats.addDuplicated(1, 1, 0);
	}
	
	@Test
	public void testStats() {
		assertEquals(2, stats.getNumberOfDuplicatedSpikes());
		assertEquals(6, stats.getNumberOfPrefixesInDuplicatedSpikes());
		assertEquals(5, stats.getNumberOfDuplicatedPrefixesInDuplicatedSpikes());
		assertEquals(5, stats.getTotalNumberOfSpikes());
		assertEquals(17, stats.getTotalNumberOfPrefixes());
	}
	
	@Test
	public void testWithAllDuplicatedPrefixes(){
		this.stats.addSingle(10, 5);
		this.stats.addDuplicated(10, 7, 5);
		assertEquals(10, stats.getNumberOfAllDuplicatedPrefixesWithAllSpikes());
		assertEquals(12,stats.getNumberOfDuplicatedPrefixesInDuplicatedSpikes());
		assertEquals(7, stats.getTotalNumberOfSpikes());
		assertEquals(4, stats.getNumberOfSingleSpikes());
		assertEquals(21, stats.getNumberOfPrefixesInSingleSpikes());
	}
}
