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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.runner.JUnitCore;

public class TestRunnerWorker {
	public static CommandLineBuilder COMMAND_LINE_BUILDER = new CommandLineBuilder() {
		public String[] buildCommandLine(String className, String hostname,
				int port, List<String> propertyFiles, int instance) {
			List<String> args = new ArrayList<String>();
			
			args.add(System.getProperty("java.home")+"/bin/java");
			args.add("-cp");
			args.add(System.getProperty("java.class.path"));
			args.add(className);
			args.add(hostname);
			args.add(Integer.toString(port));
			args.addAll(propertyFiles);

			return args.toArray(new String[args.size()]);
		}
	};

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

	public static Properties readProperties(List<String> filenames) throws Exception {
		Properties props = new Properties();
		
		for(String propertyFile : filenames)
		{
			FileInputStream fi = new FileInputStream(propertyFile);
			props.load(fi);
			fi.close();
		}

		return props;
	}
	
	public static void main(String args[]) throws Exception{
		TestRunnerWorker r = new TestRunnerWorker();
		
		String target = args[0];
		int port = Integer.parseInt(args[1]);

		List<String> filenames = new ArrayList<String>();
		for(int i=2;i<args.length;i++) {
			filenames.add(args[i]);
		}
		
		// replace the current properties with those from the manager
		Properties props = readProperties(filenames);
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
