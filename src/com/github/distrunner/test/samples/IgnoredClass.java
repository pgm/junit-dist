package com.github.distrunner.test.samples;

import org.junit.Ignore;
import org.junit.Test;

import com.github.distrunner.TestGroup;

@TestGroup("goteam")
@Ignore
public class IgnoredClass {
	@Test
	public void testDoNotRun() {
		
	}
}
