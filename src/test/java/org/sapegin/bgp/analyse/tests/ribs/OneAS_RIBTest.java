package org.sapegin.bgp.analyse.tests.ribs;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.sapegin.bgp.analyse.ribs.ASPath;
import org.sapegin.bgp.analyse.ribs.ASPathElement;
import org.sapegin.bgp.analyse.ribs.OneAS_RIB;
import org.sapegin.bgp.analyse.spikes.Destination;

public class OneAS_RIBTest {

	@Test
	public void test() throws UnknownHostException {
		OneAS_RIB rib = new OneAS_RIB();

		ASPathElement element1 = new ASPathElement(new ArrayList<Integer>(
				Arrays.asList(1)));
		ASPathElement element2 = new ASPathElement(new ArrayList<Integer>(
				Arrays.asList(2)));
		ASPathElement element3 = new ASPathElement(new ArrayList<Integer>(
				Arrays.asList(3)));
		ASPathElement element4 = new ASPathElement(new ArrayList<Integer>(
				Arrays.asList(4)));
		ASPathElement element5 = new ASPathElement(new ArrayList<Integer>(
				Arrays.asList(5)));
		ASPathElement element6 = new ASPathElement(new ArrayList<Integer>(
				Arrays.asList(6)));
		
		
		rib.announce(new Destination(InetAddress.getByName("1.1.1.0")), new ASPath(new ArrayList<ASPathElement>(
				Arrays.asList(element1, element2, element3))));
		rib.announce(new Destination(InetAddress.getByName("1.2.3.0")), new ASPath(new ArrayList<ASPathElement>(
				Arrays.asList(element4, element5, element6))));
		rib.withdraw(new Destination(InetAddress.getByName("1.2.3.0")));
		
		assertEquals((int) rib.getAS_Path(new Destination(InetAddress.getByName("1.1.1.0"))).getASPath().get(0).getASPathElement().get(0), 1);
		assertEquals((int) rib.getAS_Path(new Destination(InetAddress.getByName("1.1.1.0"))).getASPath().get(1).getASPathElement().get(0), 2);

	}
}
