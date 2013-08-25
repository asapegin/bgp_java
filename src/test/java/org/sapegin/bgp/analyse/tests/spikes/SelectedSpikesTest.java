package org.sapegin.bgp.analyse.tests.spikes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.spikes.MonitoredAS;
import org.sapegin.bgp.analyse.spikes.SelectedSpikes;
import org.sapegin.bgp.analyse.spikes.Spike;
import org.sapegin.bgp.analyse.spikes.SpikeCollection;

public class SelectedSpikesTest {

	private SpikeCollection collection;

	@Before
	public void prepare() {
		collection = new SpikeCollection();

		Spike spike1 = new Spike();
		MonitoredAS as1 = new MonitoredAS("router1", 1);
		collection.addSpike(1111, spike1, as1);

		Spike spike2 = new Spike();
		MonitoredAS as2 = new MonitoredAS("router2", 2);
		collection.addSpike(2222, spike2, as2);

		Spike spike3 = new Spike();
		MonitoredAS as3 = new MonitoredAS("router3", 3);
		collection.addSpike(3333, spike3, as3);
	}

	@Test
	public void test() {

		SelectedSpikes spikes = new SelectedSpikes(collection);

		Spike spike4 = new Spike();
		MonitoredAS as4 = new MonitoredAS("router4", 4);
		assertTrue(collection.addSpike(4444, spike4, as4));

		assertFalse(spikes.addSpike(4444, spike4, as4));

		assertEquals(4, spikes.getRandom(10).getUpdateMap().size());
		assertEquals(0, spikes.getRandom(10).getUpdateMap().get(as4)
				.getSpikeAtTime(4444).getSpikeSize());
		assertEquals(0, spikes.getRandom(0).getUpdateMap().size());
	}

	@Test
	public void testGetSpikes() {
		collection = new SpikeCollection();

		Spike spike1 = new Spike();
		MonitoredAS as1 = new MonitoredAS("router1", 1);
		collection.addSpike(1111, spike1, as1);

		Spike spike2 = new Spike();
		MonitoredAS as2 = new MonitoredAS("router2", 2);
		collection.addSpike(2222, spike2, as2);
		
		collection.addSpike(2223, spike2, as2);
		collection.addSpike(2224, spike2, as2);

		Spike spike3 = new Spike();
		MonitoredAS as3 = new MonitoredAS("router3", 3);
		collection.addSpike(3333, spike3, as3);
		
		SelectedSpikes spikes = new SelectedSpikes(collection);
		assertEquals(5,new SelectedSpikes(spikes.getUpdateMap()).numberOfSpikes());
		
		// order: router3, router1, router2
		assertEquals(0,spikes.getSpikes(1, 4).getUpdateMap().get(as2).getCurrentUpdateSum());
		assertEquals(2,spikes.getSpikes(1, 4).getUpdateMap().get(as2).getNumberOfSpikes());
		assertEquals(2,spikes.getSpikes(4, 5).getUpdateMap().get(as2).getNumberOfSpikes());
		assertEquals(1,spikes.getSpikes(1, 3).getUpdateMap().get(as1).getNumberOfSpikes());
		assertEquals(1,spikes.getSpikes(2, 3).getUpdateMap().get(as1).getNumberOfSpikes());
		assertEquals(1,spikes.getSpikes(2, 3).getUpdateMap().get(as2).getNumberOfSpikes());
		assertEquals(null,spikes.getSpikes(2, 3).getUpdateMap().get(as3));
	}

}
