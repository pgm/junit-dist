package com.github.distrunner.test;

import org.junit.Ignore;
import org.junit.Test;

import com.github.distrunner.RunTestWorker;

public class SampleTest {

	@Test
	public void testStuff() throws Exception {
		Thread.sleep(1000*2);
		System.out.println("stdout");
		System.err.println("stderr");
	}
	
	@Test 
	public void testWrites() throws Exception {
		System.out.println("out to stdout");
		System.err.println("out to stderr");
	}
	
	@Test
	public void testFailure() throws Exception {
		throw new RuntimeException("sample problem");
	}
	
	@Ignore
	public void testTophat() throws Exception {
		throw new RuntimeException("ignored");
	}
	
	public static void main(String[]args) throws Exception {
		RunTestWorker r = new RunTestWorker();
		r.run(IgnoredClass.class.getName(), "sample.xml");
	}
}
