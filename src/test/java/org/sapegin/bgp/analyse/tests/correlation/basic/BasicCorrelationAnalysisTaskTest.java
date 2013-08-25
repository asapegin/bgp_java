package org.sapegin.bgp.analyse.tests.correlation.basic;

import org.junit.Test;
import org.sapegin.bgp.analyse.correlation.basic.BasicCorrelationAnalysisTask;
import org.sapegin.bgp.analyse.generics.MonitoredASsFactory;
import org.sapegin.bgp.analyse.tests.BasicTest;
import org.sapegin.bgp.analyse.visibility.MonitoredASs;

public class BasicCorrelationAnalysisTaskTest extends BasicTest{

	@Test
	public void testBasicCorrelationAnalysisTask() throws Exception {
		BasicCorrelationAnalysisTask<?> task = new BasicCorrelationAnalysisTask<MonitoredASs>(
				properties, new MonitoredASsFactory());

		task.selectVisibleDuplicatedSpikes();
		task.writeResults();
	}

}
