package com.github.distrunner.test;

import java.io.InputStream;

import org.junit.Test;
import org.objectweb.asm.ClassReader;

import com.github.distrunner.AnnotationExtractor;

public class AnnotationExtractorTest {
	@Test
	public void testExtract() throws Exception {
		InputStream is = AnnotationExtractorTest.class.getClassLoader().getResourceAsStream(IgnoredClass.class.getName().replace(".", "/")+".class");
		ClassReader reader = new ClassReader(is);
		AnnotationExtractor extractor = new AnnotationExtractor();
		reader.accept(extractor, true);
		extractor.getGroup();
	}
	
	@Test
	public void testGrouping() {
		
	}
}
