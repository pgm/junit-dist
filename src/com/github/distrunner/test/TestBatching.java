package com.github.distrunner.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.github.distrunner.TestRunnerDistributor;
import com.github.distrunner.TestRunnerWorker;
import com.github.distrunner.TestRunnerDistributor.Task;
import com.github.distrunner.test.samples.TestClassAlpha;
import com.github.distrunner.test.samples.TestClassBeta;
import com.github.distrunner.test.samples.TestClassGamma;

public class TestBatching {
	@Test
	public void testBatching() {
		TestRunnerDistributor mgr = new TestRunnerDistributor(3, TestRunnerWorker.COMMAND_LINE_BUILDER);
		Task t1 = new Task(TestClassAlpha.class.getName(), "");
		Task t2 = new Task(TestClassBeta.class.getName(), "");
		Task t3 = new Task(TestClassGamma.class.getName(), "");
		List<Task> tasks = new ArrayList<Task>();
		tasks.add(t1);
		tasks.add(t2);
		tasks.add(t3);
		List<List<Task>> grouped = mgr.groupTasks(tasks);
		Assert.assertEquals(2, grouped.size());
		Assert.assertEquals(2, grouped.get(0).size());
		Assert.assertEquals(1, grouped.get(1).size());
	}
}
