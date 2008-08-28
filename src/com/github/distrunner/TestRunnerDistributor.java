package com.github.distrunner;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestRunnerDistributor {

	public static class Task {
		final String className;
		final String path;
		public Task(String className, String path) {
			super();
			this.className = className;
			this.path = path;
		}
	}
	
	private final ExecutorService pool;
	private final ServerSocket serverSocket;
	private String propertyFilename;
	private CommandLineBuilder commandLineBuilder;
	
	private Set<String> testsInFlight = new HashSet<String>();
	
	private int nextInstanceId = 0;
	
	protected int getNextInstanceId() {
		synchronized(this) {
			int v = nextInstanceId;
			nextInstanceId++;
			return v;
		}
	}
	
	public void logStartTest(String testName) {
		String tests;
		synchronized(testsInFlight) {
			testsInFlight.add(testName);
			tests = testsInFlight.toString();
		}
		System.out.println("starting "+testName+" (all tests currently running: "+tests+")");
	}
	
	public void logFinishedTest(String testName) {
		String tests;
		synchronized(testsInFlight) {
			testsInFlight.remove(testName);
			tests = testsInFlight.toString();
		}
		System.out.println("finished "+testName+" (all tests currently running: "+tests+")");
	}

	static class RemoteWorker {
		final DataInputStream in;
		final DataOutputStream out;
		final Process process;

		public RemoteWorker(DataInputStream in, DataOutputStream out,
				Process process) {
			super();
			this.in = in;
			this.out = out;
			this.process = process;
		}

		public void execute(TestRunnerDistributor manager, Task task) {
			manager.logStartTest(task.className);
			
			try {
				out.writeInt(task.className.length());
				out.writeChars(task.className);
				out.writeInt(task.path.length());
				out.writeChars(task.path);

				// block until complete
				in.readBoolean();
			} catch(Exception ex) {
				throw new RuntimeException(ex);
			}

			manager.logFinishedTest(task.className);
		}
	}
	
	private static ThreadLocal<RemoteWorker> remoteWorker = new ThreadLocal<RemoteWorker>();

	public void attachStreamMonitor(final Process p, final InputStream is) {
		final BufferedInputStream bis = new BufferedInputStream(is);
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				byte[] b = new byte[10000];
				
				while(true) {
					try {
						int len = bis.read(b);
						if(len <= 0)
							break;
						String s = new String(b, 0, len);
						System.out.println(p+": "+s);
					} catch (Exception ex) {
						ex.printStackTrace();
						break;
					}
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}
	
	protected RemoteWorker createRemoteWorker() {
		Runtime runtime = Runtime.getRuntime();
		
		try { 
			String [] args = commandLineBuilder.buildCommandLine(TestRunnerWorker.class.getName(), 
					serverSocket.getInetAddress().getHostAddress(), 
					serverSocket.getLocalPort(), 
					Collections.singletonList(propertyFilename),
					getNextInstanceId());
			Process process = runtime.exec( args );
			attachStreamMonitor(process, process.getErrorStream());
			attachStreamMonitor(process, process.getInputStream());
			Socket socket = serverSocket.accept();
			
			RemoteWorker w = new RemoteWorker(new DataInputStream(socket.getInputStream()),
					new DataOutputStream(socket.getOutputStream()),
					process);
			return w;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public RemoteWorker getRemoteWorker() {
		RemoteWorker w = remoteWorker.get();
		if(w == null) {
			w = createRemoteWorker();
			remoteWorker.set(w);
		}
		return w;
	}
	
	
	public TestRunnerDistributor(int poolSize, CommandLineBuilder commandLineBuilder) {
		try {
			serverSocket = new ServerSocket(0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		pool = Executors.newFixedThreadPool(poolSize);
		this.commandLineBuilder = commandLineBuilder;
	}
	
	public List<List<Task>> groupTasks(Collection<Task> tasks) {
		List<Task> ungrouped = new ArrayList<Task>();
		Map<String, List<Task>> groups = new HashMap<String,List<Task>>();
		
		for(Task task : tasks) {
			String group = AnnotationExtractor.getGroupForClass(task.className);
			if(group == null) {
				ungrouped.add(task);
			} else {
				List<Task> groupedTasks;
				if(groups.containsKey(group)) {
					groupedTasks = groups.get(group);
				} else {
					groupedTasks = new ArrayList<Task>();
					groups.put(group, groupedTasks);
				}
				groupedTasks.add(task);
			}
		}
		
		List<List<Task>> allTasks = new ArrayList<List<Task>>();
		for(Task task : ungrouped) {
			allTasks.add(Collections.singletonList(task));
		}
		allTasks.addAll(groups.values());

		// now we want to execute first the largest groups
		Collections.sort(allTasks, new Comparator<List<Task>>() {
			public int compare(List<Task> a, List<Task> b) {
				return b.size() - a.size();
			}
		});
		
		return allTasks;
	}
	
	public void run(Collection<Task> tasks) {
		List<List<Task>> allTasks = groupTasks(tasks);
		
		try { 
			File propertyFile = File.createTempFile("distrunner", "props");
			FileOutputStream os = new FileOutputStream(propertyFile);
			System.getProperties().store(os, "Generated from Manager.java");
			os.close();
			propertyFile.deleteOnExit();
			propertyFilename = propertyFile.getAbsolutePath();
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		}
		
		List<Future<?>> results = new ArrayList<Future<?>>();

		for(List<Task> taskGroup : allTasks) {

			final List<Task> myTaskGroup = taskGroup;
			final TestRunnerDistributor myManager = this;
			
			Future<?> future = pool.submit(new Runnable() {
				public void run() {
					RemoteWorker worker = myManager.getRemoteWorker();
					for(Task task : myTaskGroup) {
						worker.execute(myManager, task);
					}
				}
			});
			results.add(future);
		}
		
		for(Future<?> f : results) {
			// force each of these to execute
			try {
				f.get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		pool.shutdown();
	}
	
	public static void executeTests(int poolSize, Collection<String> classNames, String outputPrefix, CommandLineBuilder commandLineBuilder) {
		List<Task> tasks = new ArrayList<Task>();
		for(String name : classNames) {
			tasks.add(new Task(name, outputPrefix+name+".xml"));
		}

		TestRunnerDistributor mgr = new TestRunnerDistributor(poolSize, commandLineBuilder);
		mgr.run(tasks);
	}
	
}
