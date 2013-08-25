package org.sapegin.bgp.analyse.tests.analyse;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.ASsNames;

public class ASsNamesTest {
	
	private ArrayList<String> filenames;
	
	@Before
	public void prepareTest(){
		filenames = new ArrayList<String>();
		
		filenames.add("test/ases/ases1");
		filenames.add("test/ases/ases2");
		filenames.add("test/ases/ases3");
	}
	
	@Test
	public void testASsNames() {
		ASsNames names = new ASsNames(filenames);
		
		ArrayList<Integer> results = names.getASsNames();
		
		assertTrue(results.contains(13101));
		assertTrue(results.contains(22548));
		assertTrue(results.size()==12);
	}

}
