package org.sapegin.bgp.analyse.tests.ases;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.ases.ASsFileGenerator;

public class ASsFileGeneratorTest {
	
	private ASsFileGenerator generator;
		
	@Before
	public void createASsFileGenerator() {
		this.generator = new ASsFileGenerator("test/input_names_generator_test","test/generated_ases",false);
	}

	@Test
	public void testGenerate() throws Exception {
		assertTrue(generator.generate().equals("test/input_names_generator_test.new"));
	}

}
