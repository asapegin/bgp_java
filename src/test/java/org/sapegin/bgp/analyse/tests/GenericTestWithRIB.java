package org.sapegin.bgp.analyse.tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.junit.Before;

public abstract class GenericTestWithRIB {
	
	protected Properties properties;
	protected ArrayList<String> inputUpdatesFilenames = new ArrayList<String>();
	protected ArrayList<String> inputRIBsFilenames = new ArrayList<String>();
	protected ArrayList<String> inputASsFilenames = new ArrayList<String>();

	@Before
	public void SetUp() throws InvalidPropertiesFormatException, IOException {

		ClassLoader loader = ClassLoader.getSystemClassLoader();
		InputStream stream = loader
				.getResourceAsStream("analyse.properties.rib.xml");
		properties = new Properties();
		properties.loadFromXML(stream);

		loadFilenames(properties.getProperty("input_names"));
	}
	
	/**
	 * This procedure reads names of files with updates and names of ASes
	 * 
	 * These preparations are STRICTLY needed for reading data from files with
	 * updates!
	 * @throws IOException 
	 */
	private void loadFilenames(String inputFilenamesFilename) throws IOException {

		// read file line by line
		FileReader inputNames = new FileReader(inputFilenamesFilename);
		BufferedReader inputNamesBR = new BufferedReader(inputNames);

		// read first line
		String inStr = inputNamesBR.readLine();

		// while not EndOfFile
		while (inStr != null) {
			// skip commented lines
			if (inStr.startsWith("#")) {
				inStr = inputNamesBR.readLine();
				continue;
			}

			// read filenames
			inputUpdatesFilenames.add(inStr.substring(0, inStr.indexOf(":")));
			inStr = inStr.substring(inStr.indexOf(":")+1);
			inputRIBsFilenames.add(inStr.substring(0, inStr.indexOf(":")));
			inputASsFilenames.add(inStr.substring(inStr.indexOf(":") + 1));

			inStr = inputNamesBR.readLine();
		}

		inputNamesBR.close();
		inputNames.close();
		
	}
}
