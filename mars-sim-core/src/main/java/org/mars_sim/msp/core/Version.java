/*
 * Mars Simulation Project
 * Version.java
 * @date 2021-012-05
 * @author Barry Evans
 */
package org.mars_sim.msp.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import org.mars_sim.msp.core.structure.building.function.ResourceProcess;

/**
 * This class will read the properties file automatically created by the Maven build.
 * See the pom.xml plugin git-commit-id-maven-plugin for details.
 * The implementation of this class mut be synchronised with the plugin.
 */
public final class Version {

	/** default logger. */
	private static final Logger logger = Logger.getLogger(Version.class.getName());

	// Properties of the version file generated by the Maven plugin
	// "git-commit-id-maven-plugin"
	private static final String VERSION_PROPERTY = "git.build.version";
	private static final String DIRTY_PROPERTY = "git.dirty";
	private static final String BUILD_PROPERTY = "git.commit.id.abbrev";

	// Name of the file generated by the Maven build
	private static final String VERSION_PROPERTIES = "version.properties";

	private static String build = "Unknown";
	private static String versionTag = "Unknwon";

	// Load the properties file
	static {
		ClassLoader loader = Version.class.getClassLoader();
		Properties props = new Properties();
		InputStream stream = loader.getResourceAsStream(VERSION_PROPERTIES);
		if (stream != null) {
			try {
				props.load(stream);

				// Pick out key settings
				build = props.getProperty(BUILD_PROPERTY, "Not Specified");
				if (!props.getProperty(DIRTY_PROPERTY, "false").equalsIgnoreCase("false")) {
					// Dirty build with local changed files
					build += "-dirty";
				}

				versionTag = props.getProperty(VERSION_PROPERTY, "Not Specified");
			} catch (IOException e) {
				logger.severe("Version file found but not read.");
				e.printStackTrace();
			}
		}
	}

	// Private constructor to stop instantiation
	private Version() {
	}

	/**
	 * Get the build number which is the abbreviated Git commit. If there
	 * are dirty files in the build then a suffix will be added.
	 * @return
	 */
	public static String getBuild() {
		return build;
	}

	/**
	 * Get the version number the Maven project.
	 * @return
	 */
	public static String getVersion() {
		return versionTag;
	}
}