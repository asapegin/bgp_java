package org.sapegin.bgp.analyse.tests.correlation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.correlation.DuplicatedSpikesGroup;
import org.sapegin.bgp.analyse.correlation.advanced.AdvancedClassificationResult;
import org.sapegin.bgp.analyse.spikes.MonitoredAS;
import org.sapegin.bgp.analyse.spikes.Spike;
import org.sapegin.bgp.analyse.tests.BasicTest;
import org.sapegin.bgp.analyse.updates.Updates;
import org.sapegin.bgp.analyse.updates.UpdatesFactory;
import org.sapegin.bgp.analyse.updates.UpdatesFromVisibleASs;
import org.sapegin.bgp.analyse.visibility.MonitoredASs;

public class DuplicatedSpikesGroupClassificationTest extends BasicTest {

	@Test
	public void testClassification() throws Exception {

		ASsNames names = new ASsNames(new ArrayList<String>(
				Arrays.asList(inputASsFilenames.get(0))));

		InternetMap map = new InternetMap(properties.getProperty("map"), null,
				null, null);

		int threads = Integer.parseInt(properties.getProperty("threads","1"));
		
		MonitoredASs monitoredASs = new MonitoredASs(map, names, threads);

		// load updates only from the first test file
		Updates updates1 = (UpdatesFromVisibleASs) new UpdatesFactory<MonitoredASs>()
				.createUpdates(
						false,
						null,
						false,
						false,
						monitoredASs,
						new ArrayList<String>(Arrays
								.asList(inputUpdatesFilenames.get(0))), false);

		MonitoredAS as = new MonitoredAS(
				new File(inputUpdatesFilenames.get(0)).getName(), 29073);

		Spike spike = updates1.getUpdateMap().get(as).getAllSpikes()
				.get((long) 1243814456);

		DuplicatedSpikesGroup group = new DuplicatedSpikesGroup(as, spike,
				(long) 1243814456, updates1, map, 120, 0.99);

		AdvancedClassificationResult result = group.classify(monitoredASs);

		assertEquals(7, (int) result.getSize());
		// AS 29686 will not be counted, as it is too small and will be filtered
		// with a threshold
		assertEquals(1, (byte) result.getMaxDistance());
		assertEquals(8, (long) result.getMaxTimeDifference());
		assertFalse(result.isSingle());

		map = new InternetMap(properties.getProperty("map"),
				properties.getProperty("map_t1"),
				properties.getProperty("map_t2"),
				properties.getProperty("map_t3"));

		group = new DuplicatedSpikesGroup(as, spike, (long) 1243814456,
				updates1, map, 120, 0.99);

		result = group.classify(monitoredASs);
		assertTrue(result.getMaxDistance() == null);
	}

}
