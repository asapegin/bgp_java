package org.sapegin.bgp.analyse.tests.spikes;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import org.sapegin.bgp.analyse.Colours;
import org.sapegin.bgp.analyse.spikes.MonitoredAS;
import org.sapegin.bgp.analyse.spikes.Spike;
import org.sapegin.bgp.analyse.spikes.SpikeCollection;

public class SpikeCollectionTest {

	@Test
	public void test() {
		SpikeCollection collection = new SpikeCollection();

		Spike spike1 = new Spike();
		MonitoredAS as1 = new MonitoredAS("router1", 1);
		collection.addSpike(1111, spike1, as1);

		Spike spike2 = new Spike();
		MonitoredAS as2 = new MonitoredAS("router2", 2);
		collection.addSpike(2222, spike2, as2);

		Spike spike3 = new Spike();
		MonitoredAS as3 = new MonitoredAS("router3", 3);
		collection.addSpike(3333, spike3, as3);

		assertEquals(collection.getUpdateMap().size(), 3);
		assertEquals(collection.getUpdateMap().get(as2).getSpikeAtTime(2222)
				.getSpikeSize(), 0);

		SpikeCollection collection2 = new SpikeCollection(
				collection.getUpdateMap());

		assertEquals(collection2.getUpdateMap().size(), 3);
		assertEquals(collection2.getUpdateMap().get(as1).getSpikeAtTime(1111)
				.getSpikeSize(), 0);
	}

	@Test
	public void testPrintAllAround() throws UnknownHostException {
		SpikeCollection collection = new SpikeCollection();

		Spike spike1 = new Spike();
		spike1.addPrefix(InetAddress.getByName("1.1.1.0"));
		spike1.addPrefix(InetAddress.getByName("2.2.2.0"));
		MonitoredAS as1 = new MonitoredAS("router1", 1);
		collection.addSpike(1111, spike1, as1);

		Spike spike2 = new Spike();
		spike2.addPrefix(InetAddress.getByName("1.1.1.0"));
		spike2.addPrefix(InetAddress.getByName("2.2.2.0"));
		MonitoredAS as2 = new MonitoredAS("router2", 2);
		collection.addSpike(1121, spike2, as2);

		Spike spike3 = new Spike();
		spike3.addPrefix(InetAddress.getByName("1.1.1.0"));
		spike3.addPrefix(InetAddress.getByName("2.2.2.0"));
		MonitoredAS as3 = new MonitoredAS("router3", 3);
		collection.addSpike(3333, spike3, as3);

		Colours colours = new Colours("test/gnuplot_colours.txt");

		collection.printAllAround("test/updates_around_spike", spike1, 1111, 30,
				colours);
	}

}
