package org.sapegin.bgp.analyse.tests.ases;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.ases.FixPeerAS;

public class FixPeerASTest {

	private FixPeerAS converter;

	@Before
	public void testFixPeerAS() {
		converter = new FixPeerAS("test/input_names_for_peer_as_fix", false,
				false);
	}

	@Test
	public void testFixAllUpdates() throws Exception {
		converter.fixAllUpdates();

		File file = new File("test/updates/updates_m_pas_1.old");
		assertTrue(file.exists());
	}

	@After
	public void renameUpdatesBack() throws IOException {
		FileReader inputNames = new FileReader(
				"test/input_names_for_peer_as_fix");
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

			String updatesFilename;
			if (inStr.indexOf(":") != -1) {
				updatesFilename = inStr.substring(0, inStr.indexOf(":"));
			} else {
				updatesFilename = inStr;
			}
			File update = new File(updatesFilename + ".old");

			if (update.exists()) {
				// rename files back
				File converted = new File(updatesFilename);
				converted.delete();
				update.renameTo(converted);
			}

			// read next update filename
			inStr = inputNamesBR.readLine();
		}
		
		inputNamesBR.close();
		inputNames.close();
	}

}
