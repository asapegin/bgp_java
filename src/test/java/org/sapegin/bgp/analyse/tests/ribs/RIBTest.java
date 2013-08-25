package org.sapegin.bgp.analyse.tests.ribs;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.ribs.ASPath;
import org.sapegin.bgp.analyse.ribs.RIB;
import org.sapegin.bgp.analyse.spikes.Destination;
import org.sapegin.bgp.analyse.tests.BasicTest;

public class RIBTest extends BasicTest {

	private ASsNames names;

	@Before
	public void prepare() {

		names = new ASsNames(inputASsFilenames);
	}

	@Test
	public void test() throws UnknownHostException {

		RIB rib = new RIB("test/ribs/1.rib", names.getASsNames());

		// 1299 should be in RIB file, but not in ases file! So not loaded...
		assertEquals(
				rib.getWithdwalASPath(1299, new Destination(InetAddress.getByName("1.0.0.0"))),
				null);
		System.out.println(rib.getWithdwalASPath(3257,
				new Destination(InetAddress.getByName("1.0.0.0"))).getASList().size());
		assertEquals(
				(int) rib.getWithdwalASPath(3257,
						new Destination(InetAddress.getByName("1.0.0.0"))).getASList().get(1), 15169);

		rib.announce(12345, new Destination(InetAddress.getByName("1.2.3.0")),
				new ASPath("4 5 6"));

		assertEquals(
				rib.getWithdwalASPath(12345, new Destination(InetAddress.getByName("1.2.3.0"))).getASList()
						.size(), 3);
	}
}
