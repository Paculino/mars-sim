/*
 * Mars Simulation Project
 * MarsProject.java
 * @date 2022-08-08
 * @author Scott Davis
 */
package org.mars_sim.main;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.mars.sim.console.InteractiveTerm;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationBuilder;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.SimulationFiles;
import org.mars_sim.msp.core.configuration.Scenario;
import org.mars_sim.msp.core.configuration.UserConfigurableConfig;
import org.mars_sim.msp.core.person.Crew;
import org.mars_sim.msp.ui.helpGenerator.HelpGenerator;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.configeditor.SimulationConfigEditor;
import org.mars_sim.msp.ui.swing.sound.AudioPlayer;

/**
* MarsProject is the main class for the application. It creates both the
* simulation and the user interface.
*/
public class MarsProject {
	/** initialized logger for this class. */
	private static final Logger logger = Logger.getLogger(MarsProject.class.getName());

	private static final String LOGGING_PROPERTIES = "/logging.properties";
	private static final String NOAUDIO = "noaudio";
	private static final String NOGUI = "nogui";
	private static final String DISPLAY_HELP = "help";
	private static final String GENERATE_HELP = "html";
	private static final String NEW = "new";
	private static final String CLEANUI = "cleanui";
	private static final String SANDBOX = "sandbox";
	private static final String SITE_EDITOR = "site";
	private static final String PROFILE = "profile";
	
	private static final String SANDBOX_MODE_Q = "Do you want to bypass the console menu and start a new default simulation in Sandbox Mode ?";
	
	/** true if displaying graphic user interface. */
	private boolean useGUI = true;
	
	private boolean useNew = false;
	
	private boolean useCleanUI = false;
	
	private boolean useSiteEditor;
	
	private boolean useProfile = false;
	
	private boolean isSandbox = false;

	private InteractiveTerm interactiveTerm = new InteractiveTerm(false);

	private Simulation sim;

	private SimulationConfig simulationConfig = SimulationConfig.instance();

	/**
	 * Constructor
	 */
	public MarsProject() {
		logger.config("Starting " + Simulation.title);
		// Set the InteractionTerm instance
		MainWindow.setInteractiveTerm(interactiveTerm);
	}

	/**
	 * Checks for confirmation of bypassing the text console main menu via a dialog box.
	 * 
	 * @return
	 */
	public boolean bypassConsoleMenuDialog() {
		logger.config(SANDBOX_MODE_Q);
		logger.config("To proceed, please choose 'Yes' or 'No' button in the dialog.");
		// Ask the player if wanting to do a 'Quick Start'
		int reply = JOptionPane.showConfirmDialog(interactiveTerm.getTerminal().getFrame(),
				SANDBOX_MODE_Q, 
				"Quick Start", 
				JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
			logger.config("You choose Yes. Go straight to starting a new default simulation in Sandbox Mode.");	
			return true;
        }
        
        return false;
	}

	/**
	 * Parses the argument and start the simulation.
	 * 
	 * @param args
	 */
	public void parseArgs(String[] args) {
		String s = Arrays.toString(args);
		
		logger.config("List of input args : " + s);
		
		SimulationBuilder builder = new SimulationBuilder();

		checkOptions(builder, args);

		// Do it
		try {
			if (useGUI) {
				// Start the splash window
				if (!useSiteEditor) {
					SwingUtilities.invokeLater(MainWindow::startSplash);
				}
				
				// Use opengl
				// Question: How compatible are linux and macos with opengl ?
				// System.setProperty("sun.java2d.opengl", "true"); // not compatible with
				if (!MainWindow.OS.contains("linux")) {
					System.setProperty("sun.java2d.ddforcevram", "true");
				}
				
			}

			// Preload the Config
			simulationConfig.loadConfig();
			
			if (useSiteEditor) {
				logger.config("Start the Scenario Editor...");
				startScenarioEditor(builder);
			}
			else if (useProfile) {
				setupProfile();
			}
		
			// Go to console main menu if there is no template well-defined in the startup string
			else if (!builder.isFullyDefined() && useNew) {
		
				// Ask if running in standard Sandbox mode or Go to Console Menu
				if (!isSandbox && !bypassConsoleMenuDialog()) {
					logger.config("Please go to the Console Main Menu to choose an option.");
					int type = interactiveTerm.startConsoleMainMenu();
					if (type == 1) {
						logger.config("Start the Scenario Editor...");
						startScenarioEditor(builder);
					}

					else if (type == 2) {
						// Load simulation
						logger.config("Load the sim...");
						String filePath = selectSimFile(false);
						if (filePath != null) {
							builder.setSimFile(filePath);
						}
					}
					else {
						// Check out crew flag
						builder.setUseCrews(interactiveTerm.getUseCrew());
					}
				}
			}

			// Build and run the simulator
			sim = builder.start();

			// Start the wait layer
			InteractiveTerm.startLayer();

			// Start beryx console
			startConsoleThread();

			if (useGUI) {
				setupMainWindow(useCleanUI);
			}
		}
		catch(Exception e) {
			// Catch everything
			exitWithError("Problem starting " + e.getMessage(), e);
		}
	}

	/**
	 * Checks what switches or arguments have been provided
	 * 
	 * @param builder
	 * @param args
	 */
	private void checkOptions(SimulationBuilder builder, String[] args) {

		Options options = new Options();
		for(Option o : builder.getCmdLineOptions()) {
			options.addOption(o);
		}

		options.addOption(Option.builder(DISPLAY_HELP)
				.desc("Display help options").build());
		options.addOption(Option.builder(NOAUDIO)
				.desc("Disable the audio").build());
		options.addOption(Option.builder(NOGUI)
				.desc("Disable the main UI").build());
		options.addOption(Option.builder(CLEANUI)
				.desc("Disable loading stored UI configurations").build());
		options.addOption(Option.builder(SANDBOX)
				.desc("Start in Sandbox Mode").build());
		options.addOption(Option.builder(NEW)
				.desc("Enable quick start").build());
		options.addOption(Option.builder(GENERATE_HELP)
				.desc("Generate HTML help").build());
		options.addOption(Option.builder(SITE_EDITOR)
				.desc("Start the Scenario Editor").build());
		options.addOption(Option.builder(PROFILE)
				.desc("Set up the Commander Profile").build());
		
		CommandLineParser commandline = new DefaultParser();
		try {
			CommandLine line = commandline.parse(options, args);

			builder.parseCommandLine(line);

			if (line.hasOption(NOAUDIO)) {
				// Disable all audio not just the volume
				AudioPlayer.disableVolume();
			}
			if (line.hasOption(DISPLAY_HELP)) {
				usage("See available options below", options);
			}
			if (line.hasOption(NOGUI)) {
				useGUI = false;
			}
			if (line.hasOption(NEW)) {
				useNew = true;
			}
			if (line.hasOption(CLEANUI)) {
				useCleanUI = true;
			}
			if (line.hasOption(SITE_EDITOR)) {
				useSiteEditor = true;
			}
			if (line.hasOption(GENERATE_HELP)) {
				generateHelp();
			}
			if (line.hasOption(PROFILE)) {
				useProfile = true;
			}
			if (line.hasOption(SANDBOX)) {
				isSandbox = true;
			}
			

		}
		catch (Exception e1) {
			usage("Problem with arguments: " + e1.getMessage(), options);
		}
	}

	/**
	 * Starts the scenario editor
	 * 
	 * @param builder
	 */
	private void startScenarioEditor(SimulationBuilder builder) {
		MainWindow.setInteractiveTerm(interactiveTerm);
		// Start sim config editor
		SimulationConfigEditor editor = new SimulationConfigEditor(SimulationConfig.instance());
		logger.config("Starting the Scenario Editor...");
		editor.waitForCompletion();

		UserConfigurableConfig<Crew> crew = editor.getCrewConfig();
		if (crew != null) {
			// Set the actual CrewConfig as it has editted entries
			builder.setCrewConfig(crew);
		}

		Scenario scenario = editor.getScenario();
		if (scenario != null) {
			builder.setScenario(scenario);
		}
	}


	/**
	 * Prints the help options.
	 * 
	 * @param message
	 * @param options
	 */
	private void usage(String message, Options options) {
		HelpFormatter format = new HelpFormatter();
		logger.config(message);
		format.printHelp("", options);
		System.exit(1);
	}

	/**
	 * Performs the process of loading a simulation.
	 *
	 * @param autosave
	 */
	protected static String selectSimFile(boolean autosave) {

		String dir = null;
		String title = null;

		// Add autosave
		if (autosave) {
			dir = SimulationFiles.getAutoSaveDir();
			title = Msg.getString("MainWindow.dialogLoadAutosaveSim");
		} else {
			dir = SimulationFiles.getSaveDir();
			title = Msg.getString("MainWindow.dialogLoadSavedSim");
		}

		JFileChooser chooser = new JFileChooser(dir);
		chooser.setDialogTitle(title); // $NON-NLS-1$
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().getAbsolutePath();
		}

		return null;
	}

	/**
	 * Sets up the commander profile 
	 */
	private void setupProfile() {
		logger.config("Note: direct setup of the Commander Profile has NOT been implemented.");
		logger.config("Choose 'No' to start the Console Main Menu");
		logger.config("Select '1. Start a new Sim'");
		logger.config("Select '1. Command Mode'");
		logger.config("Select '5. Set up a new commander profile'");
		logger.config("When done, select '6. Load an exiting commander profile'");
	}
	
	/**
	 * Generates the html help files
	 */
	private void generateHelp() {
		logger.config("Generating help files in headless mode in " + Simulation.OS + ".");

		try {
			simulationConfig.loadConfig();
			// this will generate html files for in-game help
			HelpGenerator.generateHtmlHelpFiles();
			logger.config("Done creating help files.");
			System.exit(1);

		} catch (Exception e) {
			exitWithError("Could not generate help files ", e);
		}
	}

	/**
	 * Exits the simulation with an error message.
	 *
	 * @param message the error message.
	 * @param e       the thrown exception or null if none.
	 */
	private void exitWithError(String message, Exception e) {
		if (e != null) {
			logger.log(Level.SEVERE, message, e);
		} else {
			logger.log(Level.SEVERE, message);
		}

		if (useGUI) {
			JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
		}
		System.exit(1);
	}


	/**
	 * Runs the main window 
	 * 
	 * @param cleanUI
	 */
	public void setupMainWindow(boolean cleanUI) {
		while (true) {
	        try {
				TimeUnit.MILLISECONDS.sleep(250);
				if (!sim.isUpdating()) {
					logger.config("Starting the Main Window...");
					new MainWindow(cleanUI, sim).stopLayerUI();
					break;
				}
	        } catch (InterruptedException e) {
				logger.log(Level.WARNING, "Trouble starting Main Window. ", e); //$NON-NLS-1$
	        }
		}
	}


	/**
	 * Starts the simulation instance.
	 */
	private void startConsoleThread() {
		Thread consoleThread = new Thread(new ConsoleTask());
		consoleThread.setName("ConsoleThread");
		consoleThread.start();
	}

	/**
	 * The ConsoleTask allows running the beryx console in a thread
	 */
	class ConsoleTask implements Runnable {

		ConsoleTask() {
		}

		public void run() {
			// Load the menu choice
			InteractiveTerm.loadTerminalMenu();
		}
	}

	/**
	 * The main starting method for the application
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
		// Note: Read the logging configuration from the classloader to make it webstart compatible
		new File(System.getProperty("user.home"), ".mars-sim" + File.separator + "logs").mkdirs();

		try {
			LogManager.getLogManager().readConfiguration(MarsProject.class.getResourceAsStream(LOGGING_PROPERTIES));
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not load logging properties", e);
			try {
				LogManager.getLogManager().readConfiguration();
			} catch (IOException e1) {
				logger.log(Level.WARNING, "Could read logging default config", e);
			}
		}

		// Sets text antialiasing
		System.setProperty("swing.aatext", "true");
		System.setProperty("awt.useSystemAAFontSettings", "lcd"); // for newer VMs

		// Starts the simulation
		MarsProject project = new MarsProject();
		
		// Processes the arguments
		project.parseArgs(args);
		
		logger.config("Finish processing MarsProject.");
	}

}
