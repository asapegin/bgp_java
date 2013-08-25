package org.sapegin.bgp.analyse.tests.spikes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import org.sapegin.bgp.analyse.spikes.SingleASspikes;
import org.sapegin.bgp.analyse.spikes.Spike;

public class SingleASspikesTest {

	SingleASspikes spikes;
	
	@Test
	public void testAddSpike() {
		spikes = new SingleASspikes();
		
		spikes.addSpike(1, null);
		assertEquals(spikes.getNumberOfSpikes(), 0);
		
		addSpikes();
	
		Spike spike = new Spike();
		assertFalse(spikes.addSpike(4000, spike));
	}
	
	@Test
	public void testGetNumberOfSpikes() {
		spikes = new SingleASspikes();
		addSpikes();
		
		assertEquals(4, spikes.getNumberOfSpikes());
	}
	
	@Test
	public void testGetCurrentBiggestSpike() throws UnknownHostException {
		spikes = new SingleASspikes();
		addSpikes();
		
		Spike spike = spikes.getCurrentBiggestSpike();
		
		assertEquals(4, spike.copyPrefixSet().size());
		assertTrue(spike.containsPrefix(InetAddress.getByName("1.1.7.0")));
		assertTrue(spike.containsPrefix(InetAddress.getByName("1.1.8.0")));
		assertTrue(spike.containsPrefix(InetAddress.getByName("1.1.9.0")));
		assertTrue(spike.containsPrefix(InetAddress.getByName("1.1.10.0")));
	}

	@Test
	public void testGetCurrentUpdateSum() {
		spikes = new SingleASspikes();
		addSpikes();
		
		assertEquals(10, spikes.getCurrentUpdateSum());
	}
	
	@Test
	public void testGetCurrentTime() {
		spikes = new SingleASspikes();
		addSpikes();
		
		assertEquals(1, spikes.getCurrentMinTime());
		assertEquals(4000, spikes.getCurrentMaxTime());
	}
	
	@Test
	public void testSpikeAtTime() throws UnknownHostException {
		spikes = new SingleASspikes();
		addSpikes();
		
		assertTrue(spikes.hasSpikeAtTime(4000));
		assertTrue(spikes.hasSpikeAtTime(1));
		assertFalse(spikes.hasSpikeAtTime(5));
		assertFalse(spikes.hasSpikeAtTime(0));
		
		Spike spike = spikes.getSpikeAtTime(1);
		assertEquals(1, spike.copyPrefixSet().size());
		assertTrue(spike.containsPrefix(InetAddress.getByName("1.1.1.0")));
	}
	
	@Test
	public void testSynchronise() {
		spikes = new SingleASspikes();
		addSpikes();
		
		spikes.synchronise(2, 10);
		spikes.synchronise(10, 2);
		
		assertEquals(3,spikes.getCurrentMaxTime());
		assertEquals(2,spikes.getCurrentMinTime());
		assertEquals(5,spikes.getCurrentUpdateSum());
		assertEquals(2,spikes.getNumberOfSpikes());
	}
	
	private void addSpikes(){
		Spike spike1 = new Spike();
		spike1.addPrefix("1.1.1.0");
		
		Spike spike2 = new Spike();
		spike2.addPrefix("1.1.2.0");
		spike2.addPrefix("1.1.3.0");
		
		Spike spike3 = new Spike();
		spike3.addPrefix("1.1.4.0");
		spike3.addPrefix("1.1.5.0");
		spike3.addPrefix("1.1.6.0");
		
		Spike spike4 = new Spike();
		spike4.addPrefix("1.1.7.0");
		spike4.addPrefix("1.1.8.0");
		spike4.addPrefix("1.1.9.0");
		spike4.addPrefix("1.1.10.0");
		
		spikes.addSpike(1, spike1);
		spikes.addSpike(2, spike2);
		spikes.addSpike(3, spike3);
		spikes.addSpike(4000, spike4);	
	}
}
