package org.sapegin.bgp.analyse.tests.visibility;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.tests.BasicTest;
import org.sapegin.bgp.analyse.visibility.ASsToAnalyse;
import org.sapegin.bgp.analyse.visibility.VisibleASs;

public class ASsToAnalyseTest extends BasicTest {

	private ASsToAnalyse visibleASs;
	private InternetMap map;
	private ASsNames names;

	@Before
	public void initialise() {

		try {
			map = new InternetMap(properties.getProperty("map"),
					properties.getProperty("map_t1"),
					properties.getProperty("map_t2"),
					properties.getProperty("map_t3"));
		} catch (Exception e) {
			e.printStackTrace();
			fail("can't create map");
		}

		names = new ASsNames(inputASsFilenames);
		
		int threads = Integer.parseInt(properties.getProperty("threads","1"));

		visibleASs = new VisibleASs(map, names, 0.99f, threads);
	}

	@Test
	public void testGetASsNamesFromBiggestConnectedGraphComponent() {
		ArrayList<Integer> res = visibleASs
				.getASsNamesFromBiggestConnectedGraphComponent();

		assertTrue(res.contains(13101));
		assertTrue(res.contains(3257));
		assertTrue(res.size() == 2);
	}

	@Test
	public void testGetBiggestConnectedGraphComponent() {
		SimpleGraph<Integer, DefaultEdge> graph = visibleASs
				.getBiggestConnectedGraphComponent();

		assertTrue(graph.containsVertex(13101));
		assertTrue(graph.containsVertex(3257));
		assertTrue(graph.vertexSet().size() == 2);
	}

	@Test
	public void testWriteDOT() {
		visibleASs.writeDOT("test/VisibleASsTEstOutput.dot");
	}

	@Test
	public void testWriteStringDOT() {
		String dot = visibleASs.writeStringDOT();
		assertTrue(dot.contains("3257 -- 13101") || dot.contains("13101 -- 3257"));
		assertFalse(dot.contains("3257 -- 29073"));
	}

}
