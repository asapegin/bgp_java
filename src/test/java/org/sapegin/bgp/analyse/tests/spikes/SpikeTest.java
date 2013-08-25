package org.sapegin.bgp.analyse.tests.spikes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.sapegin.bgp.analyse.ribs.ASPathElement;
import org.sapegin.bgp.analyse.spikes.Destination;
import org.sapegin.bgp.analyse.spikes.Spike;

public class SpikeTest {

	@Test
	public void testAddPrefixString() {
		Spike spike = new Spike();
		spike.addPrefix("1.2.3.4");
	}

	@Test
	public void testAddPrefixInetAddress() throws UnknownHostException {
		Spike spike = new Spike();
		InetAddress address = InetAddress.getByName("4.3.2.1");
		spike.addPrefix(address);
	}
	
	@Test
	public void testContainsPrefix() throws UnknownHostException {
		Spike spike = new Spike();
		InetAddress address = InetAddress.getByName("4.3.2.1");
		spike.addPrefix(address);
		assertTrue(spike.containsPrefix(address));
		
		Spike spike2 = new Spike();
		spike2.addDestination(new Destination(address,new ASPathElement(new ArrayList<Integer>(Arrays.asList(1,2,3)))));
		assertTrue(spike2.containsPrefix(address));
	}
	
	@Test
	public void testIsDuplicated(){
		Spike spike = new Spike();
		spike.addPrefix("1.1.1.0");
		spike.addPrefix("2.2.2.0");
		spike.addPrefix("3.3.3.0");
		
		Spike duplicated = new Spike();
		duplicated.addPrefix("1.1.1.0");
		duplicated.addPrefix("1.1.1.0");
		duplicated.addPrefix("1.1.1.0");
		duplicated.addPrefix("1.1.1.0");
		
		assertTrue(spike.isDuplicatedWith(duplicated, 0.33));
		assertFalse(spike.isDuplicatedWith(duplicated, 0.34));
	}

}
