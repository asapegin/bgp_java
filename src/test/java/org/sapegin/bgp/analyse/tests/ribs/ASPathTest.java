package org.sapegin.bgp.analyse.tests.ribs;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.ribs.ASPath;
import org.sapegin.bgp.analyse.ribs.ASPathElement;

public class ASPathTest {

	private ASPath path;
	
	@Before
	public void testASPathArrayListOfASPathElement() {
		path = new ASPath("1 2 3 4 5 6 7");
	}

	@Test
	public void testASPathString() {
		ArrayList<ASPathElement> elements = new ArrayList<ASPathElement>();
		ASPathElement element = new ASPathElement(new ArrayList<Integer>(Arrays.asList(1,2,3)));
		elements.add(element);
		ASPath path1 = new ASPath(elements);
		
		assertEquals(2, (int) path1.getASList().get(1));
		assertEquals(3, path1.getASList().size());
		assertEquals(1, path1.getASPath().size());
		assertEquals(3, path1.getASPath().get(0).getASPathElement().size());
		assertEquals(3, (int) path1.getASPath().get(0).getASPathElement().get(2));
	}

	@Test
	public void testGetASPath() {
		assertEquals(1, (int) path.getASPath().get(0).getASPathElement().get(0));
		assertEquals(2, (int) path.getASPath().get(1).getASPathElement().get(0));
		assertEquals(7, (int) path.getASPath().get(6).getASPathElement().get(0));
		assertEquals(7,path.getASPath().size());
	}

	@Test
	public void testGetOriginAS() {
		assertEquals(7, (int) path.getOriginAS().getASPathElement().get(0));
		assertEquals(1, (int) path.getOriginAS().getASPathElement().size());
	}

	@Test
	public void testGetASList() {
		assertEquals(7, path.getASList().size());
		assertEquals(1, (int) path.getASList().get(0));
		assertEquals(6, (int) path.getASList().get(5));
	}

}
