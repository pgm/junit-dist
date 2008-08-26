package com.github.distrunner;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Properties;

import org.junit.runner.JUnitCore;

public class RunTestWorker {

	public void run(String className, String filename) throws Exception {
		ClassLoader loader = this.getClass().getClassLoader();

		PrintStream originalOut = System.out;
		PrintStream originalErr = System.err;

		try {
			ByteArrayOutputStream newOutBuffer = new ByteArrayOutputStream();
			PrintStream newOut = new PrintStream(newOutBuffer);
			System.setOut(newOut);
	
			ByteArrayOutputStream newErrBuffer = new ByteArrayOutputStream();
			PrintStream newErr = new PrintStream(newErrBuffer);
			System.setErr(newErr);
			
			Class <?> clazz = loader.loadClass(className);
			JUnitCore core = new JUnitCore();
			XmlLoggingRunListener listener = new XmlLoggingRunListener(filename);
			core.addListener(listener);
			core.run(clazz);
			listener.outputComplete(newOutBuffer.toByteArray(), newErrBuffer.toByteArray());
			listener.writeXml();
		} finally {
			System.setOut(originalOut);
			System.setErr(originalErr);
		}
	}
	
	public static void main(String args[]) throws Exception{
		RunTestWorker r = new RunTestWorker();
		String target = args[0];
		int port = Integer.parseInt(args[1]);
		String propertyFile = args[2];

		// replace the current properties with those from the manager
		FileInputStream fi = new FileInputStream(propertyFile);
		Properties props = new Properties();
		props.load(fi);
		fi.close();
		System.setProperties(props);
		
		System.out.println("connecting to "+target+":"+port);
		Socket socket = new Socket(target, port);
		System.out.println("getting streams");
		
		InputStream is = socket.getInputStream();
		OutputStream os = socket.getOutputStream();
		DataInputStream ios = new DataInputStream(is);
		DataOutputStream oos = new DataOutputStream(os);
		while(true) {
			String className = readString(ios);
			String filename = readString(ios);
			r.run(className, filename);
			oos.writeBoolean(true);
		}
		
	}
	
	public static String readString(DataInputStream s) throws IOException {
		int len = s.readInt();
		char [] buf = new char[len];
		for(int i = 0;i<len;i++)
			buf[i] = s.readChar();
		return new String(buf);
	}
}
