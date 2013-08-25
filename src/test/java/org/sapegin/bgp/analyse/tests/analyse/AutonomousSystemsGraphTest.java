package org.sapegin.bgp.analyse.tests.analyse;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.AutonomousSystemsGraph;

public class AutonomousSystemsGraphTest extends AutonomousSystemsGraph{

	@Before
	public void prepare(){
		for (int i=1; i<12; i++){
		this.graphAS.addVertex(i);
		}
		
		this.graphAS.addEdge(1, 5);
		this.graphAS.addEdge(2, 6);
		this.graphAS.addEdge(3, 4);
		this.graphAS.addEdge(7, 10);
		this.graphAS.addEdge(8, 3);
		this.graphAS.addEdge(2, 9);
		this.graphAS.addEdge(3, 10);
	}

	@Test
	public void testAreConnected() {
		assertTrue(this.areConnected(1, 5));
		assertTrue(this.areConnected(10, 7));
		assertTrue(this.areConnected(10, 3));
		assertTrue(this.areConnected(3, 8));
		assertFalse(this.areConnected(2, 10));
		assertFalse(this.areConnected(10, 4));
		assertFalse(this.areConnected(2, 7));
		assertFalse(this.areConnected(9, 1));
	}

	@Test
	public void testGetNeighbours() {
		ArrayList<Integer> neighbours = this.getNeighbours(3);
		
		assertTrue(neighbours.contains(8));
		assertTrue(neighbours.contains(10));
		assertTrue(neighbours.contains(4));
		assertTrue(neighbours.size()==3);
		
		neighbours = this.getNeighbours(11);
		
		assertTrue(neighbours.size()==0);
		
		neighbours = this.getNeighbours(1);
		
		assertTrue(neighbours.size()==1);
		assertTrue(neighbours.get(0)==5);
	}

}
