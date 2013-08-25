package org.sapegin.bgp.analyse.tests.correlation.advanced;

import org.junit.Test;
import org.sapegin.bgp.analyse.correlation.advanced.AnalyseCorrelatedSpikesTask;
import org.sapegin.bgp.analyse.generics.MonitoredASsFactory;
import org.sapegin.bgp.analyse.tests.BasicTest;
import org.sapegin.bgp.analyse.visibility.MonitoredASs;

public class AnalyseCorrelatedSpikesTaskTest extends BasicTest{
	
	@Test
	public void testAnalyseCorrelatedSpikesTask() throws Exception {
		AnalyseCorrelatedSpikesTask<?> task = new AnalyseCorrelatedSpikesTask<MonitoredASs>(
				properties, new MonitoredASsFactory());
		
		task.selectVisibleDuplicatedSpikes();
		task.writeResults();
	}
}
