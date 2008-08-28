package com.github.distrunner;

import java.util.List;

public interface CommandLineBuilder {
	public String[] buildCommandLine(String className, String hostname, int port, List<String> propertyFiles, int instance);
}
