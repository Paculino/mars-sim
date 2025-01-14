/*
 * Mars Simulation Project
 * MainWindow.java
 * @date 2021-08-15
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.mars.sim.console.InteractiveTerm;
import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationFiles;
import org.mars_sim.msp.core.SimulationListener;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.astroarts.OrbitViewer;
import org.mars_sim.msp.ui.swing.tool.JStatusBar;
import org.mars_sim.msp.ui.swing.tool.WaitLayerUIPanel;

import com.alee.api.resource.ClassResource;
import com.alee.extended.button.WebSwitch;
import com.alee.extended.date.WebDateField;
import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.memorybar.WebMemoryBar;
import com.alee.extended.overlay.FillOverlay;
import com.alee.extended.overlay.WebOverlay;
import com.alee.extended.svg.SvgIconSource;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.button.WebButton;
import com.alee.laf.text.WebTextField;
import com.alee.managers.UIManagers;
import com.alee.managers.icon.IconManager;
import com.alee.managers.icon.LazyIcon;
import com.alee.managers.icon.set.IconSet;
import com.alee.managers.icon.set.RuntimeIconSet;
import com.alee.managers.language.LanguageManager;
import com.alee.managers.style.StyleId;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;
import com.alee.utils.swing.NoOpKeyListener;
import com.alee.utils.swing.NoOpMouseListener;

/**
 * The MainWindow class is the primary UI frame for the project. It contains the
 * main desktop pane window are, status bar and tool bars.
 */
public class MainWindow
extends JComponent implements ClockListener {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(MainWindow.class.getName());

	/** Icon image filename for frame */
	public static final String LANDER_PNG = "landerhab16.png";//"/images/LanderHab.png";
	public static final String LANDER_SVG = "/svg/icons/lander_hab.svg";

	public static final String INFO_RED_SVG = "/svg/icons/info_red.svg";
	public static final String PAUSE_ORANGE_SVG = "/svg/icons/pause_orange.svg";
	public static final String MARS_CALENDAR_SVG = "/svg/icons/calendar_mars.svg";

	public static final String INFO_SVG = "/svg/icons/info.svg";
	public static final String EDIT_SVG = "/svg/icons/edit.svg";
	public static final String LEFT_SVG = "/svg/icons/left_rotate.svg";
	public static final String RIGHT_SVG = "/svg/icons/right_rotate.svg";
	public static final String CENTER_SVG = "/svg/icons/center.svg";
	public static final String STACK_SVG = "/svg/icons/stack.svg";

	public static final String SAND_SVG = Msg.getString("img.svg.sand");//$NON-NLS-1$
	public static final String HAZY_SVG = Msg.getString("img.svg.hazy");//$NON-NLS-1$

	public static final String SANDSTORM_SVG = Msg.getString("img.svg.sandstorm"); //$NON-NLS-1$
	public static final String DUST_DEVIL_SVG = Msg.getString("img.svg.dust_devil");//$NON-NLS-1$

	public static final String COLD_WIND_SVG = Msg.getString("img.svg.cold_wind");//$NON-NLS-1$
	public static final String FROST_WIND_SVG = Msg.getString("img.svg.frost_wind");//$NON-NLS-1$

	public static final String SUN_SVG = Msg.getString("img.svg.sun"); //$NON-NLS-1$
	public static final String DESERT_SUN_SVG = Msg.getString("img.svg.desert_sun");//$NON-NLS-1$
	public static final String CLOUDY_SVG = Msg.getString("img.svg.cloudy");//$NON-NLS-1$
	public static final String SNOWFLAKE_SVG = Msg.getString("img.svg.snowflake");//$NON-NLS-1$
	public static final String ICE_SVG = Msg.getString("img.svg.ice");//$NON-NLS-1$
	public static final String MARS_SVG = Msg.getString("img.svg.mars");//$NON-NLS-1$
	public static final String TELESCOPE_SVG = Msg.getString("img.svg.telescope");//$NON-NLS-1$
	public static final String OS = System.getProperty("os.name").toLowerCase(); // e.g. 'linux', 'mac os x'

	private static final String SOL = "   Sol ";
	private static final String WHITESPACES = "   ";

	/** The size of the weather icons */
	public static final int WEATHER_ICON_SIZE = 64;
	/** The timer for update the status bar labels. */
	private static final int TIME_DELAY = 2_000;
	/** Keeps track of whether icons have been added to the IconManager . */
	private static boolean iconsConfigured = false;

	private boolean isIconified = false;

	/** The main window frame. */
	private static JFrame frame;
	/** The lander hab icon. */
	private static Icon landerIcon;
	/** The Telescope icon. */
	private static Icon telescopeIcon;

	/** The four types of theme types. */
	public enum ThemeType {
		SYSTEM, NIMBUS, NIMROD, WEBLAF, METAL
	}
	/** The default ThemeType enum. */
	public ThemeType defaultThemeType = ThemeType.NIMBUS;

	// Data members
	private boolean useDefault = true;

	private int solCache = 0;

	private String lookAndFeelTheme;

	/** The unit tool bar. */
	private UnitToolBar unitToolbar;
	/** The tool bar. */
	private ToolToolBar toolToolbar;
	/** The main desktop. */
	private MainDesktopPane desktop;

	private MainWindowMenu mainWindowMenu;

	private Timer delayTimer;
	private Timer delayTimer1;

	private OrbitViewer orbitViewer;

	private javax.swing.Timer earthTimer;

	private JStatusBar statusBar;

	/** WebSwitch for the control of play or pause the simulation*/
	private WebSwitch pauseSwitch;
	private WebButton increaseSpeed;
	private WebButton decreaseSpeed;

	private JCheckBox overlayCheckBox;

	private WebOverlay overlay;

	private WebStyledLabel blockingOverlay;
	private WebStyledLabel solLabel;

	private WebTextField marsTimeTF;
	private WebDateField earthDateField;

	private WebMemoryBar memoryBar;

	private JPanel bottomPane;
	private JPanel mainPane;

	private Font FONT_SANS_SERIF_1 = new Font(Font.SANS_SERIF, Font.BOLD, 13);

	/** Arial font. */
	private Font ARIAL_FONT = new Font("Arial", Font.PLAIN, 14);

	private JLayer<JPanel> jlayer;
	private WaitLayerUIPanel layerUI = new WaitLayerUIPanel();

	private Dimension selectedSize;

	private static SplashWindow splashWindow;

	private Simulation sim;
	private MasterClock masterClock;
	private EarthClock earthClock;
	private MarsClock marsClock;

	private static InteractiveTerm interactiveTerm;

	/**
	 * Constructor 1.
	 *
	 * @param cleanUI true if window should display a clean UI.
	 */
	public MainWindow(boolean cleanUI, Simulation sim) {
		this.sim = sim;
				
		if (GameManager.getGameMode() == GameMode.COMMAND) {
			logger.log(Level.CONFIG, "Running mars-sim in Command Mode.");
		} else {
			logger.log(Level.CONFIG, "Running mars-sim in Sandbox Mode.");
		}

		// Start the wait layer
		layerUI.start();

		// this.cleanUI = cleanUI;
		// Set up the look and feel library to be used
		initializeTheme();

		// Set up the frame
		frame = new JFrame();
		frame.setResizable(true);
		frame.setMinimumSize(new Dimension(640, 640));
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		GraphicsDevice graphicsDevice = null;

		if (gs.length == 1) {
			logger.log(Level.CONFIG, "Detecting only one screen.");
			logger.config("1 screen detected.");	
		}
		else if (gs.length == 0) {
			throw new RuntimeException("No Screens Found.");
			// NOTE: what about the future server version of mars-sim in which no screen is needed.
		}
		else {
			logger.config(gs.length + " screens detected.");	
		}
		
		graphicsDevice = gs[0];
		int screenWidth = graphicsDevice.getDisplayMode().getWidth();
		int screenHeight = graphicsDevice.getDisplayMode().getHeight();

		if (cleanUI) {
			askScreenConfig(screenWidth, screenHeight);
		}
		else {
			useDefaultScreenConfig(screenWidth, screenHeight);
		}	

		// Set up MainDesktopPane
		desktop = new MainDesktopPane(this, sim);

		// Set up timers for use on the status bar
		setupDelayTimer();

		// Set up other elements
		masterClock = sim.getMasterClock();
		earthClock = masterClock.getEarthClock();
		marsClock = masterClock.getMarsClock();
		init();

		// Show frame
		frame.setVisible(true);

		// Stop the wait indicator layer
		layerUI.stop();

		// Dispose the Splash Window
		disposeSplash();

		// Open all initial windows.
		desktop.openInitialWindows();
	}

	/**
	 * Asks if the player wants to use last saved screen configuration.
	 * 
	 * @param screenWidth
	 * @param screenHeight
	 */
	private void askScreenConfig(int screenWidth, int screenHeight) {

		logger.config("Do you want to use the last saved screen configuration ?");
		logger.config("To proceed, please choose 'Yes' or 'No' button in the dialog box.");
		
		int reply = JOptionPane.showConfirmDialog(frame,
				"Do you want to use the last saved screen configuration", 
				"Screen Configuration", 
				JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
        	
			logger.config("You choose Yes. Loading last saved screen configuration.");	
			
    		// Load previous UI configuration.
			UIConfig.INSTANCE.parseFile();
			
			// Set the UI configuration
			useDefault = UIConfig.INSTANCE.useUIDefault();
//			logger.config("useDefault is: " + useDefault);
			
			if (useDefault) {
//				selectedSize = calculatedScreenSize(screenWidth, screenHeight);
	    		selectedSize = new Dimension(screenWidth, screenHeight);
	    		
				// Set frame size
				frame.setSize(selectedSize);
				logger.config("The default window dimension is "
						+ selectedSize.width
						+ " x "
						+ selectedSize.height
						+ ".");

				frame.setLocation(
					((screenWidth - selectedSize.width) / 2),
					((screenHeight - selectedSize.height) / 2)
				);
				
				logger.config("Use default configuration to set frame to the center of the screen.");	
				logger.config("The window frame is centered and starts at (" 
						+ (screenWidth - selectedSize.width) / 2 
						+ ", "
						+ (screenHeight - selectedSize.height) / 2
						+ ").");
			}
			else {
	    		selectedSize = UIConfig.INSTANCE.getMainWindowDimension();
				
				// Set frame size
				frame.setSize(selectedSize);
				logger.config("The last saved window dimension is "	
					+ selectedSize.width
					+ " x "
					+ selectedSize.height
					+ ".");
				
				// Display screen at a certain location
				frame.setLocation(UIConfig.INSTANCE.getMainWindowLocation());
				logger.config("The last saved frame starts at (" 
						+ UIConfig.INSTANCE.getMainWindowLocation().x
						+ ", "
						+ UIConfig.INSTANCE.getMainWindowLocation().y
						+ ").");
			}
        }
        
        // No. use the new default setting
        else {
        	useDefaultScreenConfig(screenWidth, screenHeight);
        }
	}
	
	/**
	 * Uses the default screen config.
	 * 
	 * @param screenWidth
	 * @param screenHeight
	 */
	private void useDefaultScreenConfig(int screenWidth, int screenHeight) {
	   	selectedSize = interactiveTerm.getSelectedScreen();
		
		logger.config("You choose No. Loading default screen dimension "
				+ selectedSize.width
				+ " x "
				+ selectedSize.height
				+ ".");
		
		// Set frame size
		frame.setSize(selectedSize);
		
		// Center frame on screen
		frame.setLocation(
			((screenWidth - selectedSize.width) / 2),
			((screenHeight - selectedSize.height) / 2)
		);
		
		logger.config("Use default configuration to set frame to the center of the screen.");
		logger.config("The window frame is centered and starts at (" 
				+ (screenWidth - selectedSize.width) / 2 
				+ ", "
				+ (screenHeight - selectedSize.height) / 2
				+ ").");	
	}
	
	/**
	 * Calculates the screen size.
	 * 
	 * @return
	 */
	private Dimension calculatedScreenSize(int screenWidth, int screenHeight) {
		logger.config("Current screen size is " + screenWidth + " x " + screenHeight);
		
		Dimension frameSize = interactiveTerm.getSelectedScreen();
		if (frameSize != null) {
			logger.config("Selected screen size is " + frameSize.width + " x " + frameSize.height);
		}
		else {
			if (useDefault) {
				logger.config("useDefault is: " + useDefault);
				logger.config("Use default screen configuration.");
			}
			else {
				// Use any stored size
				frameSize = UIConfig.INSTANCE.getMainWindowDimension();
				logger.config("Use last saved window size " + frameSize.width + " x " + frameSize.height);	
			}
		}
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if (screenSize != null) {
			logger.config("Current toolkit screen size is " + screenSize.width + " x " + screenSize.height);
		}
				
		if (frameSize != null) {
			// Check selected is not bigger than the screen
			if (frameSize.width > screenSize.width
					|| frameSize.height > screenSize.height) {
				logger.warning("Selected screen size cannot be larger than physical screen size.");
				frameSize = null;
			}
			else {
				// proceed to the next
			}
		}
 
		if (frameSize == null) {
			// Make frame size 80% of screen size.
			if (screenSize.width > 800) {
				frameSize = new Dimension(
					(int) Math.round(screenSize.getWidth() * .8),
					(int) Math.round(screenSize.getHeight() * .8)
				);
				logger.config("New window size is " + frameSize.width + " x " + frameSize.height);
			}
			else {
				frameSize = new Dimension(screenSize);
				logger.config("New window size is " + frameSize.width + " x " + frameSize.height);

			}
		}
		
		return frameSize;
	}

	/**
	 * Get the selected screen size for the main window.
	 * @return
	 */
	public Dimension getSelectedSize() {
		return selectedSize;
	}

	public void stopLayerUI() {
		layerUI.stop();
	}

	public static void initIconManager() {
		iconsConfigured = true;
		// Set up an icon set for use throughout mars-sim
		IconSet iconSet = new RuntimeIconSet("mars-sim-set");

		int size = 24;

		iconSet.addIcon(new SvgIconSource (
		        "info_red",
		        new ClassResource(MainWindow.class, INFO_RED_SVG),
		        new Dimension(12, 12)));

		iconSet.addIcon(new SvgIconSource (
		        "pause_orange",
		        new ClassResource(MainWindow.class, PAUSE_ORANGE_SVG),
		        new Dimension(300, 300)));

		iconSet.addIcon(new SvgIconSource (
		        "calendar_mars",
		        new ClassResource(MainWindow.class, MARS_CALENDAR_SVG),
		        new Dimension(16, 16)));

		iconSet.addIcon(new SvgIconSource (
		        "lander",
		        new ClassResource(MainWindow.class, LANDER_SVG),
		        new Dimension(16, 16)));

		iconSet.addIcon(new SvgIconSource (
		        "info",
		        new ClassResource(MainWindow.class, INFO_SVG),
		        new Dimension(size, size)));

		iconSet.addIcon(new SvgIconSource (
		        "edit",
		        new ClassResource(MainWindow.class, EDIT_SVG),
		        new Dimension(size, size)));

		iconSet.addIcon(new SvgIconSource (
		        "left",
		        new ClassResource(MainWindow.class, LEFT_SVG),
		        new Dimension(size, size)));

		iconSet.addIcon(new SvgIconSource (
		        "right",
		        new ClassResource(MainWindow.class, RIGHT_SVG),
		        new Dimension(size, size)));

		iconSet.addIcon(new SvgIconSource (
		        "center",
		        new ClassResource(MainWindow.class, CENTER_SVG),
		        new Dimension(size, size)));

		iconSet.addIcon(new SvgIconSource (
		        "stack",
		        new ClassResource(MainWindow.class, STACK_SVG),
		        new Dimension(size, size)));

		/////////////////////////////////////////////////////////

		iconSet.addIcon(new SvgIconSource (
		        "sandstorm",
		        new ClassResource(MainWindow.class, SANDSTORM_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));

		iconSet.addIcon(new SvgIconSource (
		        "dustDevil",
		        new ClassResource(MainWindow.class, DUST_DEVIL_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));

		////////////////////

		iconSet.addIcon(new SvgIconSource (
		        "frost_wind",
		        new ClassResource(MainWindow.class, FROST_WIND_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));

		iconSet.addIcon(new SvgIconSource (
		        "cold_wind",
		        new ClassResource(MainWindow.class, COLD_WIND_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));

		////////////////////

		iconSet.addIcon(new SvgIconSource (
		        "sun",
		        new ClassResource(MainWindow.class, SUN_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));

		iconSet.addIcon(new SvgIconSource (
		        "desert_sun",
		        new ClassResource(MainWindow.class, DESERT_SUN_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));

		iconSet.addIcon(new SvgIconSource (
		        "cloudy",
		        new ClassResource(MainWindow.class, CLOUDY_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));

		iconSet.addIcon(new SvgIconSource (
		        "snowflake",
		        new ClassResource(MainWindow.class, SNOWFLAKE_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));

		iconSet.addIcon(new SvgIconSource (
		        "ice",
		        new ClassResource(MainWindow.class, ICE_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));

		////////////////////

		iconSet.addIcon(new SvgIconSource (
		        "sand",
		        new ClassResource(MainWindow.class, SAND_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));

		iconSet.addIcon(new SvgIconSource (
		        "hazy",
		        new ClassResource(MainWindow.class, HAZY_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));

		////////////////////

		iconSet.addIcon(new SvgIconSource (
		      "mars",
		      new ClassResource(MainWindow.class, MARS_SVG),
		      new Dimension(18, 18)));

		iconSet.addIcon(new SvgIconSource (
			      "telescope",
			      new ClassResource(MainWindow.class, TELESCOPE_SVG),
			      new Dimension(14, 14)));

		// Add the icon set to the icon manager
		IconManager.addIconSet(iconSet);

		landerIcon = new LazyIcon("lander").getIcon();
	}

	/**
	 * Initializes UI elements for the frame
	 */
	@SuppressWarnings("serial")
	private void init() {

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				// Save simulation and UI configuration when window is closed.
				exitSimulation();
			}
		});

		frame.addWindowStateListener(new WindowStateListener() {
			   public void windowStateChanged(WindowEvent e) {
				   int state = e.getNewState();
                   isIconified = (state == Frame.ICONIFIED);
				   if (state == Frame.MAXIMIZED_HORIZ
						   || state == Frame.MAXIMIZED_VERT)
//					   frame.update(getGraphics());
						logger.log(Level.CONFIG, "MainWindow set to maximum."); //$NON-NLS-1$
					repaint();
			   }
		});

    	frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		changeTitle(false);

		frame.setIconImage(getIconImage());

		// Set up the main pane
		mainPane = new JPanel(new BorderLayout());
		frame.add(mainPane);

		// Set up the jlayer pane
		JPanel jlayerPane = new JPanel(new BorderLayout());
		jlayerPane.add(desktop);
		// Set up the glassy wait layer for pausing
		jlayer = new JLayer<>(jlayerPane, layerUI);

		// Set up the overlay pane
		JPanel overlayPane = new JPanel(new BorderLayout());

		// Create a pause overlay
		createOverlay(overlayPane);

		// Add desktop to the overlay pane
		overlayPane.add(jlayer, BorderLayout.CENTER);

		// Add overlay
		mainPane.add(overlay, BorderLayout.CENTER);

		// Add this class to the master clock's listener
		masterClock.addClockListener(this, 1000L);

		// Create Earth date text field
		createEarthDate();

		// Create sol label
		createSolLabel();

		// Create Mars date text field
		createMarsDate();

		// Prepare tool toolbar
		toolToolbar = new ToolToolBar(this);

		// Add toolToolbar to mainPane
		overlayPane.add(toolToolbar, BorderLayout.NORTH);

		// Add bottomPane for holding unitToolbar and statusBar
		bottomPane = new JPanel(new BorderLayout());

		// Prepare unit toolbar
		unitToolbar = new UnitToolBar(this) {
			@Override
			protected JButton createActionComponent(Action a) {
				return super.createActionComponent(a);
			}
		};

		unitToolbar.setBorder(new MarsPanelBorder());
		// Remove the toolbar border, to blend into figure contents
		unitToolbar.setBorderPainted(true);

		mainPane.add(bottomPane, BorderLayout.SOUTH);
		bottomPane.add(unitToolbar, BorderLayout.CENTER);

		// set the visibility of tool and unit bars from preferences
		unitToolbar.setVisible(UIConfig.INSTANCE.showUnitBar());
		toolToolbar.setVisible(UIConfig.INSTANCE.showToolBar());

		// Prepare menu
		mainWindowMenu = new MainWindowMenu(this, desktop);
		frame.setJMenuBar(mainWindowMenu);

		// Close the unit bar when starting up
		unitToolbar.setVisible(false);

		// Create the status bar
		statusBar = new JStatusBar(1, 1, 28);

		// Create speed buttons
		createSpeedButtons();
		// Add the decrease speed button
		statusBar.addLeftComponent(decreaseSpeed, false);

		// Create pause switch
		createPauseSwitch();
		statusBar.addLeftComponent(pauseSwitch, false);

		// Add the increase speed button
		statusBar.addLeftComponent(increaseSpeed, false);

		// Create overlay button
		createOverlayCheckBox();
		statusBar.addLeftComponent(overlayCheckBox, false);

		// Create memory bar
		createMemoryBar();
		statusBar.addRightComponent(memoryBar, true);

		statusBar.addRightCorner();

		bottomPane.add(statusBar, BorderLayout.SOUTH);
	}

	/**
	 * Sets up the pause overlay
	 *
	 * @param overlayPane
	 */
	public void createOverlay(JPanel overlayPane) {
		// Add overlayPane to overlay
		overlay = new WebOverlay(StyleId.overlay, overlayPane);

		Icon pauseIcon = new LazyIcon("pause_orange").getIcon();

        blockingOverlay = new WebStyledLabel(
        		pauseIcon,
                SwingConstants.CENTER
        );
        NoOpMouseListener.install(blockingOverlay);
        NoOpKeyListener.install(blockingOverlay);
	}

	public void createOverlayCheckBox() {
		overlayCheckBox = new JCheckBox("{Pause Overlay On/Off:b}", false);
		overlayCheckBox.putClientProperty(StyleId.STYLE_PROPERTY, StyleId.checkboxLink);
		TooltipManager.setTooltip(overlayCheckBox, "Turn on/off pause overlay in desktop", TooltipWay.up);

		overlayCheckBox.addItemListener(new ItemListener() {
		    @Override
		    public void itemStateChanged(ItemEvent e) {
		        if(e.getStateChange() == ItemEvent.SELECTED) {
		        	// Checkbox has been selected
		        	overlay.addOverlay(new FillOverlay(blockingOverlay));
		        } else {
		        	// Checkbox has been unselected
	                if (blockingOverlay.isShowing()) {
	                    overlay.removeOverlay(blockingOverlay);
	                }
		        };
		    }
		});
		// Disable the overlay check box at start of the sim
		overlayCheckBox.setEnabled(false);
	}

	public void createPauseSwitch() {
		pauseSwitch = new WebSwitch(true);
		pauseSwitch.setSwitchComponents(
				ImageLoader.getIcon(Msg.getString("img.speed.play")),
				ImageLoader.getIcon(Msg.getString("img.speed.pause")));
		TooltipManager.setTooltip(pauseSwitch, "Pause or Resume the Simulation", TooltipWay.up);

		pauseSwitch.addActionListener(e -> 
                masterClock.setPaused(!pauseSwitch.isSelected(), false)
		);
	}

	/**
	 * Open orbit viewer
	 */
	public void openOrbitViewer() {
		if (orbitViewer == null) {
			orbitViewer = new OrbitViewer(desktop);
			return;
		}
        orbitViewer.setVisible(!orbitViewer.isVisible());
	}


	public void setOrbitViewer(OrbitViewer orbitViewer) {
		this.orbitViewer = orbitViewer;
	}
	
	public Icon getTelescopeIcon() {
		if (telescopeIcon == null) {
			telescopeIcon = new LazyIcon("telescope").getIcon();
			telescopeIcon = ImageLoader.getNewIcon(Msg.getString("icon.telescope"));
		}
		return telescopeIcon;
	}

	public void createSpeedButtons() {
		increaseSpeed = new WebButton();
		increaseSpeed.setIcon(ImageLoader.getIcon(Msg.getString("img.speed.increase"))); //$NON-NLS-1$
		TooltipManager.setTooltip(increaseSpeed, "Increase the sim speed (aka time ratio)", TooltipWay.up);

		increaseSpeed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!masterClock.isPaused()) {
					masterClock.increaseSpeed();
				}
			};
		});

		decreaseSpeed = new WebButton();
		decreaseSpeed.setIcon(ImageLoader.getIcon(Msg.getString("img.speed.decrease"))); //$NON-NLS-1$
		TooltipManager.setTooltip(decreaseSpeed, "Decrease the sim speed (aka time ratio)", TooltipWay.up);

		decreaseSpeed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!masterClock.isPaused()) {
					masterClock.decreaseSpeed();
				}
			};
		});

	}

	private void createSolLabel() {
		solLabel = new WebStyledLabel(StyleId.styledlabelShadow);
		solLabel.setFont(FONT_SANS_SERIF_1);
		solLabel.setForeground(Color.DARK_GRAY);
		solLabel.setText(SOL + "1" + WHITESPACES);
		solLabel.setHorizontalAlignment(JLabel.CENTER);
		solLabel.setVerticalAlignment(JLabel.CENTER);
		TooltipManager.setTooltip(solLabel, "# of sols since the beginning of the sim", TooltipWay.up);
	}

	public WebStyledLabel getSolLabel() {
		return solLabel;
	}

	public void createMemoryBar() {
		memoryBar = new WebMemoryBar();
		memoryBar.setPreferredWidth(180);
		memoryBar.setRefreshRate(3000);
		memoryBar.setFont(ARIAL_FONT);
		memoryBar.setForeground(Color.DARK_GRAY);
	}

	public WebMemoryBar getMemoryBar() {
		return memoryBar;
	}

	public void createEarthDate() {
		earthDateField = new WebDateField(StyleId.datefield);
		TooltipManager.setTooltip(earthDateField, "Earth Timestamp in Greenwich Mean Time (GMT)", TooltipWay.up);
		earthDateField.setAllowUserInput(false);
		earthDateField.setFont(ARIAL_FONT);
		earthDateField.setForeground(new Color(0, 69, 165));
		earthDateField.setPadding(0, 5, 0, 5);
		earthDateField.setMargin(0, 0, 0, 0);
		// Note: May use "yyyy-MMM-dd EEE HH:mm a '['z']'"
		DateFormat d = new SimpleDateFormat("yyyy-MMM-dd EEE HH:mm a  ", LanguageManager.getLocale());
		d.setTimeZone(TimeZone.getTimeZone("GMT"));
		earthDateField.setDateFormat(d);

		if (earthClock.getInstant() != null) {
			earthDateField.setDate(new Date(earthClock.getInstant().toEpochMilli()));
		}
	}

	public WebDateField getEarthDate() {
		return earthDateField;
	}

	public void createMarsDate() {
		marsTimeTF = new WebTextField(StyleId.formattedtextfieldNoFocus);
		marsTimeTF.setEditable(false);
		marsTimeTF.setFont(ARIAL_FONT);
		marsTimeTF.setForeground(new Color(150,96,0));
		marsTimeTF.setPadding(0, 10, 0, 10);
		marsTimeTF.setMargin(0, 0, 0, 0);
		TooltipManager.setTooltip(marsTimeTF, "Mars Timestamp in Universal Mean Time (UMT)", TooltipWay.up);
	}

	public WebTextField getMarsTime() {
		return marsTimeTF;
	}

	/**
	 * Set up the timer for status bar
	 */
	public void setupDelayTimer() {
		if (delayTimer == null) {
			delayTimer = new Timer();
			delayTimer.schedule(new DelayTimer(), 300);
		}
	}

	/**
	 * Defines the delay timer class
	 */
	class DelayTimer extends TimerTask {
		public void run() {
			runStatusTimer();
		}
	}

	/**
	 * Set up the timer for caching settlement windows
	 */
	public void setupSettlementWindowTimer() {
		delayTimer1 = new Timer();
		delayTimer1.schedule(new CacheLoadingSettlementTimerTask(), 2000);
	}

	/**
	 * Defines the delay timer class
	 */
	class CacheLoadingSettlementTimerTask extends TimerTask {
		public void run() {
			// Cache each settlement unit window
			SwingUtilities.invokeLater(() -> desktop.cacheSettlementUnitWindow());
		}
	}

	public JPanel getBottomPane() {
		return bottomPane;
	}

	/**
	 * Start the earth timer
	 */
	public void runStatusTimer() {
		earthTimer = new javax.swing.Timer(TIME_DELAY, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// Increment both the earth and mars clocks
				incrementClocks();

				if (solLabel != null) {
					// Track mission sol
					int sol = marsClock.getMissionSol();
					if (solCache != sol) {
						solCache = sol;
						solLabel.setText(SOL + sol + WHITESPACES);
					}
				}
				// Check if the music track should be played
				desktop.getSoundPlayer().playRandomMusicTrack();
			}
		});

		earthTimer.start();
	}

	/**
	 * Get the window's frame.
	 *
	 * @return the frame.
	 */
	public JFrame getFrame() {
		return frame;
	}

	/**
	 * Gets the main desktop panel.
	 *
	 * @return desktop
	 */
	public MainDesktopPane getDesktop() {
		return desktop;
	}

	/**
	 * Gets the Main Window Menu.
	 *
	 * @return mainWindowMenu
	 */
	public MainWindowMenu getMainWindowMenu() {
		return mainWindowMenu;
	}

	/**
	 * Performs the process of saving a simulation.
	 * Note: if defaultFile is false, displays a FileChooser to select the
	 * location and new filename to save the simulation.
	 *
	 * @param defaultFile is the default.sim file be used
	 */
	public void saveSimulation(boolean defaultFile) {
		File fileLocn = null;
		if (!defaultFile) {
			JFileChooser chooser = new JFileChooser(SimulationFiles.getSaveDir());
			chooser.setDialogTitle(Msg.getString("MainWindow.dialogSaveSim")); //$NON-NLS-1$
			if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
				fileLocn = chooser.getSelectedFile();
			}
			else {
				return;
			}
		}

		// Request the save
		sim.requestSave(fileLocn, action -> {
			if (SimulationListener.SAVE_COMPLETED.equals(action)) {
				// Save the current main window ui config
				UIConfig.INSTANCE.saveFile(this);

				logger.log(Level.CONFIG, "Done calling saveSimulation().");
			}
		});

		logger.log(Level.CONFIG, "Save requested"); 
	}

	/**
	 * Pauses the simulation and opens an announcement window.
	 */
	public void pauseSimulation() {
		desktop.openAnnouncementWindow("  " + Msg.getString("MainWindow.pausingSim") + "  "); //$NON-NLS-1$
		masterClock.setPaused(true, false);
	}

	/**
	 * Closes the announcement window and unpauses the simulation.
	 */
	public void unpauseSimulation() {
		masterClock.setPaused(false, false);
		desktop.disposeAnnouncementWindow();
	}

	/**
	 * Create a new unit button in toolbar.
	 *
	 * @param unit the unit the button is for.
	 */
	public void createUnitButton(Unit unit) {
		unitToolbar.createUnitButton(unit);
	}

	/**
	 * Disposes a unit button in toolbar.
	 *
	 * @param unit the unit to dispose.
	 */
	public void disposeUnitButton(Unit unit) {
		unitToolbar.disposeUnitButton(unit);
	}

	/**
	 * Exit the simulation for running and exit.
	 */
	public void exitSimulation() {
		if (!masterClock.isPaused() && !sim.isSavePending()) {
			int reply = JOptionPane.showConfirmDialog(frame,
					"Are you sure you want to exit?", "Exiting the Simulation", JOptionPane.YES_NO_CANCEL_OPTION);
	        if (reply == JOptionPane.YES_OPTION) {

	        	frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

	        	endSimulation();
	    		// Save the UI configuration.
	    		UIConfig.INSTANCE.saveFile(this);
	    		masterClock.exitProgram();
	    		frame.dispose();
	    		destroy();
	    		System.exit(0);
	        }

	        else {
	        	frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	        }
		}
	}

	/**
	 * Ends the current simulation, closes the JavaFX stage of MainScene but leaves
	 * the main menu running
	 */
	private void endSimulation() {
		sim.endSimulation();
	}

	/**
	 * Sets the theme skin after calling stage.show() at the start of the sim
	 */
	public void initializeTheme() {
		setLookAndFeel(defaultThemeType);
	}

	/**
	 * Initialize weblaf them
	 */
	public void initializeWeblaf() {

		try {
			// use the weblaf skin
			WebLookAndFeel.install();
			UIManagers.initialize();
			// Start the weblaf icon manager
			if (!iconsConfigured)
				initIconManager();
		} catch (Exception e) {
			logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
		}

	}

	/**
	 * Sets the look and feel of the UI
	 *
	 * @param choice
	 */
	public void setLookAndFeel(ThemeType choice1) {
		boolean changed = false;

		if (choice1 == ThemeType.METAL) {

			try {
				UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
				changed = true;
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}

			initializeTheme();

		}

		else if (choice1 == ThemeType.SYSTEM) {
			try {

				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

				changed = true;
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}

			initializeWeblaf();
		}

		else if (choice1 == ThemeType.NIMBUS) {

			try {
				boolean foundNimbus = false;
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if (info.getName().equals("Nimbus")) {
						// Set Nimbus look & feel if found in JVM.

						// see https://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/color.html
						UIManager.setLookAndFeel(info.getClassName());
						foundNimbus = true;
						// themeSkin = "nimbus";
						changed = true;
						 break;
					}
				}

				// Metal Look & Feel fallback if Nimbus not present.
				if (!foundNimbus) {
					logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
					UIManager.setLookAndFeel(new MetalLookAndFeel());

					changed = true;
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}

			initializeWeblaf();
		}

		if (changed && (desktop != null)) {
			desktop.updateToolWindowLF();
			desktop.updateUnitWindowLF();
		}
	}

	/**
	 * Gets the unit toolbar.
	 *
	 * @return unit toolbar.
	 */
	public UnitToolBar getUnitToolBar() {
		return unitToolbar;
	}

	/**
	 * Gets the tool toolbar.
	 *
	 * @return tool toolbar.
	 */
	public ToolToolBar getToolToolBar() {
		return toolToolbar;
	}

	public String getLookAndFeelTheme() {
		return lookAndFeelTheme;
	}

	/**
	 * Gets the main pane instance
	 *
	 * @return
	 */
	public JPanel getMainPane() {
		return mainPane;
	}

	/**
	 * Gets the lander hab icon instance
	 *
	 * @return
	 */
	public static Icon getLanderIcon() {
		return landerIcon;
	}

	public static Image getIconImage() {
		return ((ImageIcon)landerIcon).getImage();
	}

	/**
	 * Increment the label of both the earth and mars clocks
	 */
	public void incrementClocks() {
		if (earthDateField != null && earthClock != null && earthClock.getInstant() != null) {
			earthDateField.setDate(new Date(earthClock.getInstant().toEpochMilli()));
		}

		if (marsTimeTF != null && marsClock != null) {
			marsTimeTF.setText(marsClock.getDisplayTruncatedTimeStamp());
		}
	}

	/**
	 * Starts the splash window frame
	 */
	public static void startSplash() {
        // Create a splash window
		if (splashWindow == null) {
			splashWindow = new SplashWindow();
		}

		splashWindow.setIconImage();
        splashWindow.display();
        splashWindow.getJFrame().setCursor(new Cursor(java.awt.Cursor.WAIT_CURSOR));
        //SwingUtilities.windowForComponent(splashWindow);
        
//        splashWindow.getJFrame().setAlwaysOnTop(true);
	}

	/**
	 * Disposes the splash window frame
	 */
	public void disposeSplash() {
		if (splashWindow != null) {
			splashWindow.remove();
		}
		splashWindow = null;
	}

	private void changeTitle(boolean isPaused) {
		if (GameManager.getGameMode() == GameMode.COMMAND) {
			if (isPaused) {
				frame.setTitle(Simulation.title + "  -  Command Mode" + "  -  [ P A U S E ]");
			} else {
				frame.setTitle(Simulation.title + "  -  Command Mode");
			}
		} else {
			if (isPaused) {
				frame.setTitle(Simulation.title + "  -  Sandbox Mode" + "  -  [ P A U S E ]");
			} else {
				frame.setTitle(Simulation.title + "  -  Sandbox Mode");
			}
		}
	}

	public boolean isIconified() {
		return isIconified;
	}

	@Override
	public void clockPulse(ClockPulse pulse) {
		if (pulse.getElapsed() > 0 && !isIconified) {
			// Increments the Earth and Mars clock labels.
			incrementClocks();
		}
	}

	/**
	 * Change the pause status. Called by Masterclock's firePauseChange() since
	 * TimeWindow is on clocklistener.
	 *
	 * @param isPaused true if set to pause
	 * @param showPane true if the pane will show up
	 */
	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		changeTitle(isPaused);
		// Update pause/resume webswitch buttons, based on masterclock's pause state.
		if (isPaused) { // if it needs to pause
			// if the web switch is at the play position
			if (pauseSwitch.isSelected()) {
				// then switch it to the pause position and animate the change
				pauseSwitch.setSelected(false, true);
			}
			// Enable the overlay check box
			overlayCheckBox.setEnabled(true);
			overlayCheckBox.setSelected(true);
		}

		else { // if it needs to resume playing
			// if the web switch is at the pause position
			if (!pauseSwitch.isSelected()) {
				// then switch it to the play position and animate the change
				pauseSwitch.setSelected(true, true);
			}
			// Disable the overlay check box
			overlayCheckBox.setSelected(false);
			overlayCheckBox.setEnabled(false);
		}
	}

	public static void setInteractiveTerm(InteractiveTerm i) {
		interactiveTerm = i;
	}

	/**
	 * Prepares the panel for deletion.
	 */
	public void destroy() {
		frame = null;
		unitToolbar = null;
		toolToolbar = null;
		desktop.destroy();
		desktop = null;
		mainWindowMenu = null;
		delayTimer = null;
		earthTimer = null;
		statusBar = null;
		solLabel = null;
		bottomPane = null;
		mainPane = null;
	}
}
