package org.sapegin.bgp.analyse.tests.duplication;

import org.junit.Test;
import org.sapegin.bgp.analyse.AnalyseSpikesTask;
import org.sapegin.bgp.analyse.duplication.CountDuplicationTask;
import org.sapegin.bgp.analyse.generics.ASsFactory;
import org.sapegin.bgp.analyse.generics.MonitoredASsFactory;
import org.sapegin.bgp.analyse.tests.BasicTest;
import org.sapegin.bgp.analyse.visibility.MonitoredASs;

public class CountDuplicationTaskTest extends BasicTest{

	@Test
	public void test() throws Exception {
		ASsFactory<MonitoredASs> monitoredASsFactory = new MonitoredASsFactory();
		AnalyseSpikesTask<?> task = new CountDuplicationTask<MonitoredASs>(properties,
				monitoredASsFactory);
		
		task.selectVisibleDuplicatedSpikes();
		
		task.writeResults();
	}

}
