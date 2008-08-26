package com.github.distrunner;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class XmlLoggingRunListener extends RunListener {
	final String filename;
	final Document doc;

	public XmlLoggingRunListener(String filename) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		this.filename = filename;
		try {
			this.doc = factory.newDocumentBuilder().getDOMImplementation().createDocument(null, null, null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	Properties properties;
	long allStartMils;
	long curStartMils;
	int testCount = 0;
	int failureCount = 0;
	int errorCount = 0;
	boolean curTestFailed;
	String curClassName;
	String curDescription;
	String hostname = "unknown";
	byte [] outOutput;
	byte [] errOutput;
	
	List<Element> testCaseElements = new ArrayList<Element>();

	public void outputComplete(byte[] out, byte[] err) {
		this.outOutput = out;
		this.errOutput = err;
	}
	
	protected Element createTestCaseElement() {
		double time = (double)(-curStartMils+System.currentTimeMillis())/1000.0;
		
		Element node = doc.createElement("testcase");
		node.setAttribute("classname", curClassName);
		node.setAttribute("name", curDescription);
		node.setAttribute("time", String.format("%.2f", time));
	
		testCaseElements.add(node);
		
		return node;
	}
	
	public void writeXml() throws Exception {
		// if no tests were run, do nothing
		if(testCount == 0) {
			return;
		}
		
		double time = (double)(-allStartMils+System.currentTimeMillis())/1000.0;

		Element rootElement;
		
		rootElement = doc.createElement("testsuite");
		rootElement.setAttribute("errors", Integer.toString(errorCount));
		rootElement.setAttribute("failures", Integer.toString(failureCount));
		rootElement.setAttribute("hostname", hostname);
		rootElement.setAttribute("name", curClassName);
		rootElement.setAttribute("tests", Integer.toString(testCount));
		rootElement.setAttribute("time", String.format("%.2f", time));
		rootElement.setAttribute("timestamp", new Date().toString());
		
		Element propertiesElement = doc.createElement("properties");
		for(Map.Entry<Object, Object> property : this.properties.entrySet()) {
			Element propertyElement = doc.createElement("property");
			propertyElement.setAttribute("name", property.getKey().toString());
			propertyElement.setAttribute("value", property.getValue().toString());
			propertiesElement.appendChild(propertyElement);
		}

		rootElement.appendChild(propertiesElement);
		
		for(Element element : testCaseElements) {
			rootElement.appendChild(element);
		}
		
		Element systemOut = doc.createElement("system-out");
		Text systemOutText = doc.createTextNode(new String(outOutput));
		systemOut.appendChild(systemOutText);
		
		Element systemErr = doc.createElement("system-err");
		Text systemErrText = doc.createTextNode(new String(errOutput));
		systemErr.appendChild(systemErrText);
		
		rootElement.appendChild(systemOut);
		rootElement.appendChild(systemErr);
		
		doc.appendChild(rootElement);

		FileWriter fw = new FileWriter(filename);

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(fw);
		
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(source, result); 

		fw.close();
	}

	@Override
	public void testFailure(Failure failure) throws Exception {
		curTestFailed = true;
		
		this.failureCount ++;
		Element elm = createTestCaseElement();
		elm.setAttribute("message", failure.getMessage());
		elm.setAttribute("type", failure.getException().getClass().getName());
		Text trace = doc.createTextNode(failure.getTrace());
		elm.appendChild(trace);
		super.testFailure(failure);
	}
	
	@Override
	public void testFinished(Description description) throws Exception {
		if(!curTestFailed)
			createTestCaseElement();

		super.testFinished(description);
	}

	@Override
	public void testIgnored(Description description) throws Exception {
		super.testIgnored(description);
	}

	@Override
	public void testRunFinished(Result result) throws Exception {
		super.testRunFinished(result);
	}

	@Override
	public void testRunStarted(Description description) throws Exception {
		this.curClassName = description.getDisplayName();
		this.allStartMils = System.currentTimeMillis();
		
		super.testRunStarted(description);
	}

	Pattern testName = Pattern.compile("([^(]+)\\(([^)]+)\\)");
	
	@Override
	public void testStarted(Description description) throws Exception {
		this.properties = new Properties();
		this.properties.putAll(System.getProperties());
		
		this.curStartMils = System.currentTimeMillis();
		String name = description.getDisplayName();
		this.curDescription = name;
		
		Matcher m = testName.matcher(name);
		if(m.matches()) {
			this.curClassName = m.group(2);
			this.curDescription = m.group(1);
		}
		this.testCount ++;

		curTestFailed = false;
		
		super.testStarted(description);
	}
}
