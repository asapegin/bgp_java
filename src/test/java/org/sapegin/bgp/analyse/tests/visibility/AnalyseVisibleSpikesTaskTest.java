package org.sapegin.bgp.analyse.tests.visibility;

import org.junit.Test;
import org.sapegin.bgp.analyse.generics.ASsFactory;
import org.sapegin.bgp.analyse.generics.VisibleASsFactory;
import org.sapegin.bgp.analyse.tests.BasicTest;
import org.sapegin.bgp.analyse.visibility.AnalyseVisibleSpikesTask;
import org.sapegin.bgp.analyse.visibility.VisibleASs;

public class AnalyseVisibleSpikesTaskTest extends BasicTest {

	AnalyseVisibleSpikesTask<VisibleASs> task;

	@Test
	public void testAnalyseVisibleSpikesTask() throws Exception {
		
		ASsFactory<VisibleASs> visibleASsFactory = new VisibleASsFactory();
		task = new AnalyseVisibleSpikesTask<VisibleASs>(properties,
				visibleASsFactory);

		task.selectVisibleDuplicatedSpikes();

		task.writeResults();
	}

}
