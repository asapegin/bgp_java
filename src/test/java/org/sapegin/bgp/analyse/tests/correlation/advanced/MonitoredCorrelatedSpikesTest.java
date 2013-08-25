package org.sapegin.bgp.analyse.tests.correlation.advanced;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.sapegin.bgp.analyse.ASsNames;
import org.sapegin.bgp.analyse.InternetMap;
import org.sapegin.bgp.analyse.correlation.advanced.MonitoredCorrelatedSpikes;
import org.sapegin.bgp.analyse.tests.GenericTestWithRIB;
import org.sapegin.bgp.analyse.updates.Updates;
import org.sapegin.bgp.analyse.updates.UpdatesFactory;
import org.sapegin.bgp.analyse.visibility.ASsToAnalyse;
import org.sapegin.bgp.analyse.visibility.MonitoredASs;

public class MonitoredCorrelatedSpikesTest extends GenericTestWithRIB{

	private MonitoredCorrelatedSpikes<MonitoredASs> mocos;
	private String classificationResultsFilename;
	private String timeQuartilesFilename;
	private String originsQuartilesFilename;
	
	@Before
	public void testMonitoredCorrelatedSpikes() throws Exception {
		classificationResultsFilename = properties.getProperty("classification_results_filename");
		timeQuartilesFilename = properties.getProperty("time_quartiles_filename");
		originsQuartilesFilename = properties.getProperty("origins_quartiles_filename");
		
		InternetMap map = new InternetMap(properties.getProperty("map"), null, null, null);

		ASsNames names = new ASsNames(inputASsFilenames);
		int threads = Integer.parseInt(properties.getProperty("threads","1"));
		MonitoredASs monitoredASs = new MonitoredASs(map, names, threads);

		UpdatesFactory<ASsToAnalyse> updatesFactory = new UpdatesFactory<ASsToAnalyse>();

		// with ribs; ribFilenames; NOT componentOnly; correlated; from all
		// monitored ASs; filenames with updates; sync time
		Updates allUpdates = updatesFactory.createUpdates(true, inputRIBsFilenames,
				false, true, monitoredASs, inputUpdatesFilenames, true);
		
		mocos = new MonitoredCorrelatedSpikes<MonitoredASs>(2, 1, 100, map, allUpdates, null, 120, 0.99, monitoredASs, false, null);
	}

	@Test
	public void testStartThreads() throws InterruptedException, ExecutionException, IOException {
		mocos.startThreads();
		mocos.writeResults(classificationResultsFilename, timeQuartilesFilename, originsQuartilesFilename);
	}
}
