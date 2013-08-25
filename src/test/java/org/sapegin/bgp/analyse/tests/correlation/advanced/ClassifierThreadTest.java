package org.sapegin.bgp.analyse.tests.correlation.advanced;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.correlation.ClassificationResults;
import org.sapegin.bgp.analyse.correlation.advanced.ClassifierThread;
import org.sapegin.bgp.analyse.tests.GenericTestWithRIB;
import org.sapegin.bgp.analyse.updates.Updates;
import org.sapegin.bgp.analyse.updates.UpdatesFactory;
import org.sapegin.bgp.analyse.visibility.ASsToAnalyse;
import org.sapegin.bgp.analyse.visibility.MonitoredASs;

public class ClassifierThreadTest extends GenericTestWithRIB {

	private ClassifierThread classifier_1map;
	private ClassifierThread classifier_allMaps;

	@Before
	public void testClassifierThread() throws Exception {

		UpdatesFactory<ASsToAnalyse> updatesFactory = new UpdatesFactory<ASsToAnalyse>();

		ASsNames names = new ASsNames(inputASsFilenames);

		// prepare classifier with single map
		InternetMap map = new InternetMap(properties.getProperty("map"), null,
				null, null);
		
		int threads = Integer.parseInt(properties.getProperty("threads","1"));
		MonitoredASs monitoredASs = new MonitoredASs(map, names, threads);
		// with ribs; ribFilenames; NOT componentOnly; correlated; from all
		// monitored ASs; filenames with updates; sync time
		Updates allUpdates = updatesFactory.createUpdates(true,
				inputRIBsFilenames, false, true, monitoredASs,
				inputUpdatesFilenames, true);
		classifier_1map = new ClassifierThread(0.99,
				allUpdates.getSpikesWithPredefinedSize(0, 1000, 2), monitoredASs,
				allUpdates, 120, map, false, null);

		// prepare classifier with all maps
		InternetMap allMaps = new InternetMap(properties.getProperty("map"),
				properties.getProperty("map_t1"),
				properties.getProperty("map_t2"),
				properties.getProperty("map_t3"));
		MonitoredASs monitoredASsAllMaps = new MonitoredASs(allMaps, names,threads);
		// with ribs; ribFilenames; NOT componentOnly; correlated; from all
		// monitored ASs; filenames with updates; sync time
		Updates allUpdatesAllMaps = updatesFactory.createUpdates(true,
				inputRIBsFilenames, false, true, monitoredASs,
				inputUpdatesFilenames, true);
		classifier_allMaps = new ClassifierThread(0.33,
				allUpdates.getSpikesWithPredefinedSize(0, 1000, 2),
				monitoredASsAllMaps, allUpdatesAllMaps, 120, allMaps, false, null);
	}

	@Test
	public void testCall_1map_99() throws InterruptedException,
			ExecutionException {

		// results for classification with 1 map and 0.99 correlation:
		//
		// 4 single spikes
		// 14 prefixes in total
		//
		// 1 single spike with (0.33 < visibility <= 0.66) and 3 prefixes
		// 3 single spikes with (visibility > 0.66) and 12 prefixes
		//
		// 5 duplicated spikes with 2 hops distance
		// 17 prefixes in total

		// run classifier and get results
		ExecutorService executor = Executors.newSingleThreadExecutor();
		ArrayList<ClassifierThread> threads = new ArrayList<ClassifierThread>();
		threads.add(classifier_1map);
		List<Future<ClassificationResults>> futureResults = executor
				.invokeAll(threads);
		ClassificationResults results = futureResults.get(0).get();

		assertEquals(17, results.getCorrelatedSpikesStats().get((byte) 2)
				.getTotalNumberOfPrefixes());
		assertEquals(5, results.getCorrelatedSpikesStats().get((byte) 2)
				.getTotalNumberOfSpikes());
		assertEquals(7, results.getCorrelatedSpikesStats().get((byte) 2)
				.getTimeQuartiles().getMax());
		assertEquals(true, results.getCorrelatedSpikesStats().get((byte) 2)
				.getOriginASsInGroups().contains(4)); // there is a spike with 4 different origin ASs
		assertEquals(false, results.getCorrelatedSpikesStats().get((byte) 2)
				.getOriginASsInGroups().contains(1234));
		assertEquals(4, results.getCorrelatedSpikesStats().get((byte) 2)
				.getOriginsQuartiles().getMax());

		assertEquals(0, results.getOtherCorrelatedSpikesStats()
				.getTotalNumberOfSpikes());

		assertEquals(3, results.getSingleSpikes100Visible()
				.getTotalNumberOfSpikes());
		assertEquals(12, results.getSingleSpikes100Visible()
				.getTotalNumberOfPrefixes());

		assertEquals(1, results.getSingleSpikesMax066Visible()
				.getTotalNumberOfSpikes());
		assertEquals(2, results.getSingleSpikesMax066Visible()
				.getTotalNumberOfPrefixes());
	}

	@Test
	public void testCall_allMaps_33() throws InterruptedException,
			ExecutionException {

		// results for classification with all maps and 0.33 correlation:
		//
		// 3 single spikes
		// 11 prefixes in total
		//
		// 1 single spike (from AS 1916) with (visibility <= 0.33) and 2
		// prefixes
		// 2 single spikes (from AS 22548 and 24875) with (visibility > 0.66)
		// and 9 prefixes
		//
		// 6 duplicated spikes classified as other
		// 20 prefixes in total

		// run classifier and get results
		ExecutorService executor = Executors.newSingleThreadExecutor();
		ArrayList<ClassifierThread> threads = new ArrayList<ClassifierThread>();
		threads.add(classifier_allMaps);
		List<Future<ClassificationResults>> futureResults = executor
				.invokeAll(threads);
		ClassificationResults results = futureResults.get(0).get();

		assertTrue(results.getCorrelatedSpikesStats().get((byte) 2) == null);

		assertEquals(20, results.getOtherCorrelatedSpikesStats()
				.getTotalNumberOfPrefixes());
		assertEquals(6, results.getOtherCorrelatedSpikesStats()
				.getTotalNumberOfSpikes());

		assertEquals(2, results.getSingleSpikes100Visible()
				.getTotalNumberOfSpikes());
		assertEquals(9, results.getSingleSpikes100Visible()
				.getTotalNumberOfPrefixes());

		assertEquals(0, results.getSingleSpikesMax066Visible()
				.getTotalNumberOfSpikes());

		assertEquals(1, results.getSingleSpikesMax033Visible()
				.getTotalNumberOfSpikes());
		assertEquals(2, results.getSingleSpikesMax033Visible()
				.getTotalNumberOfPrefixes());
	}
}
