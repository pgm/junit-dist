package com.github.distrunner.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.distrunner.TestRunnerDistributor;
import com.github.distrunner.TestRunnerWorker;
import com.github.distrunner.TestRunnerDistributor.Task;
import com.github.distrunner.test.samples.CopyOfSampleTest;
import com.github.distrunner.test.samples.SampleTest;

public class BasicTest {
	@Test
	public void testBasicRun() {
		
		List<Task> tasks = new ArrayList<Task>();
		tasks.add(new Task(SampleTest.class.getName(), "sample1.xml"));
		tasks.add(new Task(CopyOfSampleTest.class.getName(), "sample2.xml"));
		
		TestRunnerDistributor mgr = new TestRunnerDistributor(2, TestRunnerWorker.COMMAND_LINE_BUILDER);
		mgr.run(tasks);
	}

}
