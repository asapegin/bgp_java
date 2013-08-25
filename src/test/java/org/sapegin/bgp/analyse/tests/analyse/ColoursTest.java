package org.sapegin.bgp.analyse.tests.analyse;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sapegin.bgp.analyse.Colours;

public class ColoursTest {

	@Test
	public void test() {
		Colours colours = new Colours("test/gnuplot_colours.txt");
		
		assertEquals("white", colours.getNext());
		assertEquals("gray100", colours.getNext());
		assertFalse("".equals(colours.getNext()));
	}

}
