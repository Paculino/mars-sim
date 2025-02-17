/*
 * Mars Simulation Project
 * NavigatorWindow.java
 * @date 2021-12-22
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.navigator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

import org.mars_sim.mapdata.MapDataUtil;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.environment.Landmark;
import org.mars_sim.msp.core.environment.TerrainElevation;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.JStatusBar;
import org.mars_sim.msp.ui.swing.tool.map.CannedMarsMap;
import org.mars_sim.msp.ui.swing.tool.map.ExploredSiteMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.GeologyMarsMap;
import org.mars_sim.msp.ui.swing.tool.map.LandmarkMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.Map;
import org.mars_sim.msp.ui.swing.tool.map.MapLayer;
import org.mars_sim.msp.ui.swing.tool.map.MapPanel;
import org.mars_sim.msp.ui.swing.tool.map.MineralMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.NavpointMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.ShadingMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.SurfMarsMap;
import org.mars_sim.msp.ui.swing.tool.map.TopoMarsMap;
import org.mars_sim.msp.ui.swing.tool.map.UnitIconMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.UnitLabelMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.VehicleTrailMapLayer;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;

import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.window.PopOverDirection;
import com.alee.extended.window.WebPopOver;
import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.menu.WebCheckBoxMenuItem;
import com.alee.laf.menu.WebPopupMenu;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;

/**
 * The NavigatorWindow is a tool window that displays a map and a globe showing
 * Mars, and various other elements. It is the primary interface component that
 * presents the simulation to the user.
 */
@SuppressWarnings("serial")
public class NavigatorWindow extends ToolWindow implements ActionListener {

	/** Tool name. */
	public static final String NAME = Msg.getString("NavigatorWindow.title"); //$NON-NLS-1$

	public static final int HORIZONTAL_SURFACE_MAP = MapDataUtil.GLOBE_BOX_HEIGHT; 
	public static final int HORIZONTAL_FULL_WDITH = HORIZONTAL_SURFACE_MAP * 2;
	public static final int GLOBAL_MAP_WIDTH = HORIZONTAL_SURFACE_MAP;

	public static final int HEIGHT_BUTTON_PANE = 26;
	public static final int HEIGHT_STATUS_BAR = 20;

	public static final int CB_WIDTH = 120;

	public static final double RAD_PER_DEGREE = Math.PI / 180D;

	public static final String WHITESPACE = " ";
	public static final String COMMA = ", ";
	public static final String THETA = "\u03B8: "; //"Theta: ";
	public static final String PHI = "\u03C6: "; //"Phi: ";
	public static final String CLOSE_P = ")";

	private static final String RGB = "RGB (";
	private static final String HSB = "HSB (";

	private static final String ELEVATION = " h: ";
	private static final String KM = " km";
	
	// Data member
	/** The status bar. */
	private JStatusBar statusBar;
	/** The latitude combox  */
	private JComboBoxMW<?> latCB;
	/** The longitude combox. */
	private JComboBoxMW<?> longCB;
	/** The map panel class for holding all the map layers. */
	private MapPanel mapLayerPanel;
	/** The map pane for holding the map panel . */
	private WebPanel mapPaneInner;
	/** Globe navigation. */
	private GlobeDisplay globeNav;
	/** Compass navigation buttons. */
	// private NavButtonDisplay navButtons;
//	/** Topographical and distance legend. */
//	private LegendDisplay ruler;
//	/** Latitude entry. */
//	private WebTextField latText;
//	/** Longitude entry. */
//	private WebTextField longText;
	/** Latitude direction choice. */
	private JComboBoxMW<?> latDir;
	/** Longitude direction choice. */
	private JComboBoxMW<?> longDir;
	/** Location entry submit button. */
	private WebButton goThere;
	/** Options for map display. */
	private WebButton optionsButton;
	/** Map options menu. */
	private WebPopupMenu optionsMenu;
	/** Minerals button. */
	private WebButton mineralsButton;
	/** The info label on the status bar. */
	private WebStyledLabel heightLabel;
	private WebStyledLabel coordLabel;
	private WebStyledLabel phiLabel;
	private WebStyledLabel thetaLabel;
	private WebStyledLabel rgbLabel;
	private WebStyledLabel hsbLabel;
	
	/** Surface map menu item. */
	private WebCheckBoxMenuItem surfItem;
	/** Topographical map menu item. */
	private WebCheckBoxMenuItem topoItem;
	/** Geological map menu item. */
	private WebCheckBoxMenuItem geoItem;
	/** Show unit labels menu item. */
	private WebCheckBoxMenuItem unitLabelItem;
	/** Day/night tracking menu item. */
	private WebCheckBoxMenuItem dayNightItem;
	/** Show vehicle trails menu item. */
	private WebCheckBoxMenuItem trailItem;
	/** Show landmarks menu item. */
	private WebCheckBoxMenuItem landmarkItem;
	/** Show navpoints menu item. */
	private WebCheckBoxMenuItem navpointItem;
	/** Show explored sites menu item. */
	private WebCheckBoxMenuItem exploredSiteItem;
	/** Show minerals menu item. */
	private WebCheckBoxMenuItem mineralItem;

	private MapLayer unitIconLayer;
	private MapLayer unitLabelLayer;
	private MapLayer shadingLayer;
	private MapLayer mineralLayer;
	private MapLayer trailLayer;
	private MapLayer navpointLayer;
	private MapLayer landmarkLayer;
	private MapLayer exploredSiteLayer;

	private List<Landmark> landmarks;
	private UnitManager unitManager;

	/**
	 * Constructor.
	 * 
	 * @param desktop {@link MainDesktopPane} the desktop pane
	 */
	public NavigatorWindow(MainDesktopPane desktop) {
		// use ToolWindow constructor
		super(NAME, desktop);

		Simulation sim = desktop.getSimulation();
		this.landmarks = sim.getSurfaceFeatures().getLandmarks();
		this.unitManager = sim.getUnitManager();
	
		// Prepare content pane		
		JPanel contentPane = new JPanel(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		// Prepare whole pane
		JPanel wholePane = new JPanel(new GridLayout(1, 2));
		contentPane.add(wholePane, BorderLayout.CENTER);

		JPanel leftPane = new JPanel(new BorderLayout(0, 0));
		leftPane.setMaximumSize(new Dimension(GLOBAL_MAP_WIDTH, HORIZONTAL_SURFACE_MAP));
		wholePane.add(leftPane);
		
		// Prepare globe display
		globeNav = new GlobeDisplay(this);
		WebPanel globePane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		globePane.setMaximumSize(new Dimension(GLOBAL_MAP_WIDTH, HORIZONTAL_SURFACE_MAP));
		globePane.setBackground(Color.black);
		globePane.setOpaque(true);
		globePane.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new LineBorder(Color.gray)));
		globePane.add(globeNav);

		globePane.setAlignmentX(Component.CENTER_ALIGNMENT);
		globePane.setAlignmentY(Component.TOP_ALIGNMENT);
		leftPane.add(globePane, BorderLayout.CENTER);

		
		///////////////////////////////////////////////////////////////////////////

		
		JPanel rightPane = new JPanel(new BorderLayout(0, 0));
		wholePane.add(rightPane);
	
		WebPanel detailPane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		rightPane.add(detailPane, BorderLayout.CENTER);
	
		mapPaneInner = new WebPanel(new BorderLayout(0, 0));
		detailPane.add(mapPaneInner);
	
		mapPaneInner.setBackground(Color.black);
		mapPaneInner.setOpaque(true);
		mapPaneInner.setAlignmentX(CENTER_ALIGNMENT);
		mapPaneInner.setAlignmentY(TOP_ALIGNMENT);

		mapLayerPanel = new MapPanel(desktop, 500L);
		mapLayerPanel.setNavWin(this);
		
		mapLayerPanel.addMouseListener(new MouseListener());
		mapLayerPanel.addMouseMotionListener(new MouseMotionListener());
		// map.setCursor(new Cursor(java.awt.Cursor.DEFAULT_CURSOR));

		// Create map layers.
		unitIconLayer = new UnitIconMapLayer(mapLayerPanel);
		unitLabelLayer = new UnitLabelMapLayer();
		mineralLayer = new MineralMapLayer(mapLayerPanel);
		shadingLayer = new ShadingMapLayer(mapLayerPanel);
		navpointLayer = new NavpointMapLayer(mapLayerPanel);
		trailLayer = new VehicleTrailMapLayer();
		landmarkLayer = new LandmarkMapLayer();
		exploredSiteLayer = new ExploredSiteMapLayer(mapLayerPanel);

		// Add default map layers.
		mapLayerPanel.addMapLayer(shadingLayer, 0);
		// Note: mineralLayer is 1; 
		mapLayerPanel.addMapLayer(unitIconLayer, 2);
		mapLayerPanel.addMapLayer(unitLabelLayer, 3);
		mapLayerPanel.addMapLayer(navpointLayer, 4);
		mapLayerPanel.addMapLayer(trailLayer, 5);
		mapLayerPanel.addMapLayer(landmarkLayer, 6);

		mapLayerPanel.showMap(new Coordinates((Math.PI / 2D), 0D));
		// mapPaneInner.setAlignmentX(Component.CENTER_ALIGNMENT);
		mapPaneInner.add(mapLayerPanel, BorderLayout.CENTER);
		
		// turn on day night layer
		setMapLayer(false, 0, shadingLayer);
//		globeNav.setDayNightTracking(false);
		
		///////////////////////////////////////////////////////////////////////////
		

		// Prepare position coordination entry panel on the left pane
		WebPanel coordPane = new WebPanel(new GridLayout(1, 6, 0, 0));
		coordPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		coordPane.setAlignmentY(Component.BOTTOM_ALIGNMENT);
//		controlPane.add(coordPane);
		leftPane.add(coordPane, BorderLayout.SOUTH);
		
		// coordPane.setBorder(new EmptyBorder(6, 6, 3, 3));
//		coordPane.setMaximumHeight(HEIGHT_BUTTON_PANE);
//		coordPane.setPreferredHeight(HEIGHT_BUTTON_PANE);
//		coordPane.setMaximumSize(new Dimension(300, HEIGHT_BUTTON_PANE));
		coordPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		coordPane.setAlignmentY(Component.TOP_ALIGNMENT);

		// Prepare latitude entry components
		WebLabel latLabel = new WebLabel(" Lat : ", WebLabel.RIGHT);// Msg.getString("NavigatorWindow.latitude")); //$NON-NLS-1$
		// latLabel.setAlignmentY(.5F);
		coordPane.add(latLabel);

		Integer[] lon_degrees = new Integer[361];
		Integer[] lat_degrees = new Integer[91];
		
		// Switch to using ComboBoxMW for latitude
		int size = lon_degrees.length;
		for (int i = 0; i < size; i++) {
			lon_degrees[i] = i;
		}

		int lat_size = lat_degrees.length;
		for (int i = 0; i < lat_size; i++) {
			lat_degrees[i] = i;
		}

		latCB = new JComboBoxMW<Integer>(lat_degrees);
		latCB.setSelectedItem(0);
		latCB.setSize(new Dimension(CB_WIDTH, -1));
		coordPane.add(latCB);

		String[] latStrings = { Msg.getString("direction.degreeSign") + Msg.getString("direction.northShort"), //$NON-NLS-1$ //$NON-NLS-2$
				Msg.getString("direction.degreeSign") + Msg.getString("direction.southShort") //$NON-NLS-1$ //$NON-NLS-2$
		};
		latDir = new JComboBoxMW<Object>(latStrings);
		latDir.setEditable(false);
		latDir.setSize(new Dimension(20, -1));
		coordPane.add(latDir);

		// Prepare longitude entry components
		WebLabel longLabel = new WebLabel("Lon : ", WebLabel.RIGHT);// Msg.getString("NavigatorWindow.longitude")); //$NON-NLS-1$
		// longLabel.setAlignmentY(.5F);
		coordPane.add(longLabel);

		// Switch to using ComboBoxMW for longtitude
		longCB = new JComboBoxMW<Integer>(lon_degrees);
		longCB.setSelectedItem(0);
		longCB.setSize(new Dimension(CB_WIDTH, -1));
		coordPane.add(longCB);

		String[] longStrings = { Msg.getString("direction.degreeSign") + Msg.getString("direction.eastShort"), //$NON-NLS-1$ //$NON-NLS-2$
				Msg.getString("direction.degreeSign") + Msg.getString("direction.westShort") //$NON-NLS-1$ //$NON-NLS-2$
		};
		longDir = new JComboBoxMW<Object>(longStrings);
		longDir.setEditable(false);
		longDir.setSize(new Dimension(20, -1));
		coordPane.add(longDir);

		///////////////////////////////////////////////////////////////////////////
		
		// Prepare options panel on the right pane
		WebPanel optionsPane = new WebPanel(new GridLayout(1, 3));
//		controlPane.add(optionsPane);
		rightPane.add(optionsPane, BorderLayout.SOUTH);
		
//		optionsPane.setPreferredHeight(HEIGHT_BUTTON_PANE);
//		optionsPane.setMaximumSize(new Dimension(300, HEIGHT_BUTTON_PANE));
		optionsPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		optionsPane.setAlignmentY(Component.BOTTOM_ALIGNMENT);
//		rightPane.add(optionsPane, BorderLayout.SOUTH);
		
		// Prepare location entry submit button
		goThere = new WebButton(Msg.getString("NavigatorWindow.button.resetGo")); //$NON-NLS-1$
		goThere.setToolTipText("Go to the location with your specified coordinates");
		goThere.addActionListener(this);

		optionsPane.add(goThere);

		// Prepare options button.
		optionsButton = new WebButton(Msg.getString("NavigatorWindow.button.mapOptions")); //$NON-NLS-1$
		optionsButton.setToolTipText(Msg.getString("NavigatorWindow.tooltip.mapOptions")); //$NON-NLS-1$
		optionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				SwingUtilities.invokeLater(() -> {
//					if (optionsMenu == null)
//						createOptionsMenu();
					optionsMenu.show(optionsButton, 0, optionsButton.getHeight());
				});
			}
		});
		
		optionsPane.add(optionsButton);

		// Prepare minerals button.0
		mineralsButton = new WebButton(Msg.getString("NavigatorWindow.button.mineralOptions")); //$NON-NLS-1$
		mineralsButton.setToolTipText(Msg.getString("NavigatorWindow.tooltip.mineralOptions")); //$NON-NLS-1$
		mineralsButton.setEnabled(false);
		mineralsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				SwingUtilities.invokeLater(() -> {
					WebPopupMenu mineralsMenu = createMineralsMenu();
					mineralsMenu.show(mineralsButton, 0, mineralsButton.getHeight());
				});
			}
		});
		optionsPane.add(mineralsButton);

		// Create the status bar
		statusBar = new JStatusBar(3, 3, 18);
//		statusBar.setPreferredSize(new Dimension(HORIZONTAL_FULL + 10, 20));
		contentPane.add(statusBar, BorderLayout.SOUTH);
		
		Font font = new Font("Times New Roman", Font.PLAIN, 11);
		Font font1 = new Font(Font.DIALOG, Font.PLAIN, 10);
		Font font2 = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
		
		coordLabel = new WebStyledLabel(StyleId.styledlabelShadow);
		coordLabel.setFont(font);
		coordLabel.setForeground(Color.GREEN.darker().darker());
		phiLabel = new WebStyledLabel(StyleId.styledlabelShadow);
		phiLabel.setFont(font);
		phiLabel.setForeground(Color.BLUE.darker());
		thetaLabel = new WebStyledLabel(StyleId.styledlabelShadow);
		thetaLabel.setFont(font);
		thetaLabel.setForeground(Color.BLUE.darker());
		heightLabel = new WebStyledLabel(StyleId.styledlabelShadow);
		heightLabel.setFont(font);
		heightLabel.setForeground(Color.ORANGE.darker());
		rgbLabel = new WebStyledLabel(StyleId.styledlabelShadow);
		rgbLabel.setFont(font1);
		rgbLabel.setForeground(Color.red.darker().darker());
		hsbLabel = new WebStyledLabel(StyleId.styledlabelShadow);
		hsbLabel.setFont(font2);
		hsbLabel.setForeground(Color.MAGENTA.darker());

	    WebPanel p = new WebPanel();
	    p.setPreferredSize(new Dimension(45, HEIGHT_STATUS_BAR));
	    p.add(phiLabel);
	
	    WebPanel t = new WebPanel();
	    t.setPreferredSize(new Dimension(45, HEIGHT_STATUS_BAR));
	    t.add(thetaLabel);
	     
	    WebPanel c = new WebPanel();
	    c.setPreferredSize(new Dimension(135, HEIGHT_STATUS_BAR));
	    c.add(coordLabel);
	    
	    WebPanel e = new WebPanel();
	    e.setPreferredSize(new Dimension(150, HEIGHT_STATUS_BAR));
	    e.add(heightLabel);
	    
	    WebPanel r = new WebPanel();
	    r.setPreferredSize(new Dimension(110, HEIGHT_STATUS_BAR));
	    r.add(rgbLabel);
	    
	    WebPanel hs = new WebPanel();
	    hs.setPreferredSize(new Dimension(130, HEIGHT_STATUS_BAR));
	    hs.add(hsbLabel);
	    
		statusBar.addLeftComponent(c, false);
		statusBar.addLeftComponent(p, false);
		statusBar.addLeftComponent(t, false);
		
		statusBar.addCenterComponent(e, false);

//		statusBar.addRightComponent(e, false);
		statusBar.addRightComponent(r, false);
		statusBar.addRightComponent(hs, false);
		
//		statusBar.addRightCorner();
		
		
		// Create the option menu
		if (optionsMenu == null)
			createOptionsMenu();
		
		setClosable(true);
		setResizable(false);
		setMaximizable(false);

		setVisible(true);
		// Pack window
		pack();
	}

	/**
	 * Updates the labels on the status bar.
	 * 
	 * @param height
	 * @param coord
	 * @param phi
	 * @param theta
	 * @param rgb
	 * @param hsb
	 */
	public void updateStatusBarLabels(String height, String coord, double phi, double theta, String rgb, String hsb) {
		heightLabel.setText(ELEVATION + height);
		coordLabel.setText(WHITESPACE + coord);
		phiLabel.setText(PHI + phi);
		thetaLabel.setText(THETA + theta);
		rgbLabel.setText(rgb);
		hsbLabel.setText(hsb);
	}
	
	private class TransparentPanel extends JPanel {
	    {
	        setOpaque(false);
	    }
	    public void paintComponent(Graphics g) {
	        g.setColor(getBackground());
	        Rectangle r = g.getClipBounds();
	        g.fillRect(r.x, r.y, r.width, r.height);
	        super.paintComponent(g);
	    }
	}
	
	/**
	 * Update coordinates in map, buttons, and globe Redraw map and globe if
	 * necessary
	 * 
	 * @param newCoords the new center location
	 */
	public void updateCoords(Coordinates newCoords) {
		// navButtons.updateCoords(newCoords);
		mapLayerPanel.showMap(newCoords);
		globeNav.showGlobe(newCoords);
	}

	/**
	 * Update coordinates on globe only. Redraw globe if necessary
	 * 
	 * @param newCoords the new center location
	 */
	public void updateGlobeOnly(Coordinates newCoords) {
		globeNav.showGlobe(newCoords);
	}

	public GlobeDisplay getGlobeDisplay() {
		return globeNav;
	}

//	public void showSurfaceMap() {
//		// show surface map
//		mapPanel.setMapType(SurfMarsMap.TYPE);
//		globeNav.showSurf();
//		// ruler.showMap();
//	}
//
//	public void showGeologyMap() {
//		// show geology map
//		mapPanel.setMapType(GeologyMarsMap.TYPE);
//		globeNav.showGeo();
//	}

	/** ActionListener method overridden */
	public void actionPerformed(ActionEvent event) {

		Object source = event.getSource();

		if (source == goThere) {
			// Read longitude and latitude from user input, translate to radians,
			// and recenter globe and surface map on that location.
			try {

				double latitude = 0;
				double longitude = 0;

				latitude = (int) latCB.getSelectedItem();
				longitude = (int) longCB.getSelectedItem();
				
//				if (mainScene != null) {
//					latitude = (int) latCB.getSelectedItem();
//					longitude = (int) longCB.getSelectedItem();
//
//				} else {
//					latText.setCaretPosition(latText.getText().length());
//					longText.setCaretPosition(longText.getText().length());
//
//					latitude = Double.valueOf(latText.getText());// ((Float) new Float(latText.getText())).doubleValue();
//					longitude = Double.valueOf(longText.getText());// ((Float) new Float(longText.getText())).doubleValue();
//				}

				String latDirStr = (String) latDir.getSelectedItem();
				String longDirStr = (String) longDir.getSelectedItem();

				if ((latitude >= 0D) && (latitude <= 90D) && (longitude >= 0D) && (longitude <= 360)) {
					String northString = Msg.getString("direction.degreeSign") + Msg.getString("direction.northShort");
					if (latDirStr.equals(northString)) {
						latitude = 90D - latitude; // $NON-NLS-1$
					} else {
						latitude += 90D;
					}

					String westString = Msg.getString("direction.degreeSign") + Msg.getString("direction.westShort");
					if (longitude > 0D) {
						if (longDirStr.equals(westString)) {
							longitude = 360D - longitude; // $NON-NLS-1$
						}
					}

					double phi = RAD_PER_DEGREE * latitude;
					double theta = RAD_PER_DEGREE * longitude;
					updateCoords(new Coordinates(phi, theta));
				}
			} catch (NumberFormatException e) {
			}
		} else if (source == topoItem) {
			if (topoItem.isSelected()) {
				// show topo map
				mapLayerPanel.setMapType(TopoMarsMap.TYPE);
				globeNav.showTopo();
				// turn off day night layer
				setMapLayer(false, 0, shadingLayer);
//				globeNav.setDayNightTracking(false);
				// turn off mineral layer
				setMapLayer(false, 1, mineralLayer);
				mineralItem.setSelected(false);
				mineralsButton.setEnabled(false);
			}
		}
		else if (source == surfItem) {
			if (surfItem.isSelected()) {
				// show surface map
				mapLayerPanel.setMapType(SurfMarsMap.TYPE);
				globeNav.showSurf();
			}
		}		
		else if (source == geoItem) {
			if (geoItem.isSelected()) {
				// show geology map
				mapLayerPanel.setMapType(GeologyMarsMap.TYPE);
				globeNav.showGeo();
				// turn off day night layer
				setMapLayer(false, 0, shadingLayer);
				// turn off mineral layer
				setMapLayer(false, 1, mineralLayer);
				mineralItem.setSelected(false);
				mineralsButton.setEnabled(false);
			}
		} else if (source == dayNightItem) {
			setMapLayer(dayNightItem.isSelected(), 0, shadingLayer);
//			globeNav.setDayNightTracking(dayNightItem.isSelected());
		} else if (source == unitLabelItem)
			setMapLayer(unitLabelItem.isSelected(), 3, unitLabelLayer);
		else if (source == trailItem)
			setMapLayer(trailItem.isSelected(), 5, trailLayer);
		else if (source == landmarkItem)
			setMapLayer(landmarkItem.isSelected(), 6, landmarkLayer);
		else if (source == navpointItem)
			setMapLayer(navpointItem.isSelected(), 4, navpointLayer);
		else if (source == exploredSiteItem)
			setMapLayer(exploredSiteItem.isSelected(), 7, exploredSiteLayer);
		else if (source == mineralItem) {
			setMapLayer(mineralItem.isSelected(), 1, mineralLayer);
			mineralsButton.setEnabled(mineralItem.isSelected());
			if (mineralItem.isSelected()) {
				surfItem.doClick();
			}
		}
	}

	/**
	 * Sets a map layer on or off.
	 * 
	 * @param setMap   true if map is on and false if off.
	 * @param index    the index order of the map layer.
	 * @param mapLayer the map layer.
	 */
	private void setMapLayer(boolean setMap, int index, MapLayer mapLayer) {
		if (setMap) {
			mapLayerPanel.addMapLayer(mapLayer, index);
		} else {
			mapLayerPanel.removeMapLayer(mapLayer);
		}
	}

	/**
	 * Create the map options menu.
	 */
	private void createOptionsMenu() {
		// Create options menu.
		optionsMenu = new WebPopupMenu();
		optionsMenu.setToolTipText(Msg.getString("NavigatorWindow.menu.mapOptions")); //$NON-NLS-1$

		// Create day/night tracking menu item.
		dayNightItem = new WebCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.daylightTracking"), //$NON-NLS-1$
				mapLayerPanel.hasMapLayer(shadingLayer));
		dayNightItem.addActionListener(this);
		optionsMenu.add(dayNightItem);
		// Unchecked dayNightItem at the start of sim
		// globeNav.setDayNightTracking(false);
		dayNightItem.setSelected(false);

		// Create topographical map menu item.
		surfItem = new WebCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.surf"), //$NON-NLS-1$
				SurfMarsMap.TYPE.equals(mapLayerPanel.getMapType()));
		surfItem.addActionListener(this);
		optionsMenu.add(surfItem);
		
		// Create topographical map menu item.
		topoItem = new WebCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.topo"), //$NON-NLS-1$
				TopoMarsMap.TYPE.equals(mapLayerPanel.getMapType()));
		topoItem.addActionListener(this);
		optionsMenu.add(topoItem);

		// Create topographical map menu item.
		geoItem = new WebCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.geo"), //$NON-NLS-1$
				GeologyMarsMap.TYPE.equals(mapLayerPanel.getMapType()));
		geoItem.addActionListener(this);
		optionsMenu.add(geoItem);
		
	    ButtonGroup group = new ButtonGroup();
		group.add(surfItem);
		group.add(topoItem);
		group.add(geoItem);

//		JMenuItem mapItem = new JMenuItem(Msg.getString("NavigatorWindow.menu.selectMap"));//, KeyEvent.VK_M);
//		mapItem.add(geoItem);
//		mapItem.add(surfItem);
//		mapItem.add(geoItem);
//		optionsMenu.add(mapItem);
		
		// Create unit label menu item.
		unitLabelItem = new WebCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showLabels"), //$NON-NLS-1$
				mapLayerPanel.hasMapLayer(unitLabelLayer));
		unitLabelItem.addActionListener(this);
		optionsMenu.add(unitLabelItem);

		// Create vehicle trails menu item.
		trailItem = new WebCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showVehicleTrails"), //$NON-NLS-1$
				mapLayerPanel.hasMapLayer(trailLayer));
		trailItem.addActionListener(this);
		optionsMenu.add(trailItem);

		// Create landmarks menu item.
		landmarkItem = new WebCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showLandmarks"), //$NON-NLS-1$
				mapLayerPanel.hasMapLayer(landmarkLayer));
		landmarkItem.addActionListener(this);
		optionsMenu.add(landmarkItem);

		// Create navpoints menu item.
		navpointItem = new WebCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showNavPoints"), //$NON-NLS-1$
				mapLayerPanel.hasMapLayer(navpointLayer));
		navpointItem.addActionListener(this);
		optionsMenu.add(navpointItem);

		// Create explored site menu item.
		exploredSiteItem = new WebCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showExploredSites"), //$NON-NLS-1$
				mapLayerPanel.hasMapLayer(exploredSiteLayer));
		exploredSiteItem.addActionListener(this);
		optionsMenu.add(exploredSiteItem);

		// Create minerals menu item.
		mineralItem = new WebCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showMinerals"), //$NON-NLS-1$
				mapLayerPanel.hasMapLayer(mineralLayer));
		mineralItem.addActionListener(this);
		optionsMenu.add(mineralItem);

		optionsMenu.pack();
	}

	/**
	 * Creates the minerals menu.
	 */
	private WebPopupMenu createMineralsMenu() {
		// Create the mineral options menu.
		WebPopupMenu mineralsMenu = new WebPopupMenu();

		// Create each mineral check box item.
		MineralMapLayer mineralMapLayer = (MineralMapLayer) mineralLayer;
		java.util.Map<String, Color> mineralColors = mineralMapLayer.getMineralColors();
		Iterator<String> i = mineralColors.keySet().iterator();
		while (i.hasNext()) {
			String mineralName = i.next();
			Color mineralColor = mineralColors.get(mineralName);
			boolean isMineralDisplayed = mineralMapLayer.isMineralDisplayed(mineralName);
			WebCheckBoxMenuItem mineralItem = new WebCheckBoxMenuItem(mineralName, isMineralDisplayed);
			mineralItem.setIcon(createColorLegendIcon(mineralColor, mineralItem));
			mineralItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					SwingUtilities.invokeLater(() -> {
						WebCheckBoxMenuItem checkboxItem = (WebCheckBoxMenuItem) event.getSource();
						((MineralMapLayer) mineralLayer).setMineralDisplayed(checkboxItem.getText(),
								checkboxItem.isSelected());
					});
				}
			});
			mineralsMenu.add(mineralItem);
		}

		mineralsMenu.pack();
		return mineralsMenu;
	}

	/**
	 * Creates an icon representing a color.
	 * 
	 * @param color            the color for the icon.
	 * @param displayComponent the component to display the icon on.
	 * @return the color icon.
	 */
	private Icon createColorLegendIcon(Color color, Component displayComponent) {
		int[] imageArray = new int[10 * 10];
		Arrays.fill(imageArray, color.getRGB());
		Image image = displayComponent.createImage(new MemoryImageSource(10, 10, imageArray, 0, 10));
		return new ImageIcon(image);
	}

	/**
	 * Opens a unit window on the desktop.
	 *
	 * @param unit the unit the window is for.
	 */
	public void openUnitWindow(Unit unit) {
		desktop.openUnitWindow(unit, false);
	}

	private class MouseListener extends MouseAdapter {
		public void mouseEntered(MouseEvent event) {
			// checkHover(event);
		}
		public void mouseExited(MouseEvent event) {
		}

		public void mouseClicked(MouseEvent event) {
			checkClick(event);
		}
	}

	private class MouseMotionListener extends MouseMotionAdapter {
		public void mouseMoved(MouseEvent event) {
			checkHover(event);
		}
		public void mouseDragged(MouseEvent event) {
		}
	}

	public void checkClick(MouseEvent event) {

		if (mapLayerPanel.getCenterLocation() != null) {
			double rho = CannedMarsMap.PIXEL_RHO;

			double x = (double) (event.getX() - (Map.DISPLAY_WIDTH / 2D) - 1);
			double y = (double) (event.getY() - (Map.DISPLAY_HEIGHT / 2D) - 1);

			Coordinates clickedPosition = mapLayerPanel.getCenterLocation().convertRectToSpherical(x, y, rho);

			Iterator<Unit> i = unitManager.getDisplayUnits().iterator();

			// Open window if unit is clicked on the map
			while (i.hasNext()) {
				Unit unit = i.next();
				
				if (unit.getUnitType() == UnitType.VEHICLE) {
					if (((Vehicle)unit).isOutsideOnMarsMission()) {
						// Proceed to below to set cursor;
					}
					else 
						continue;
				}
				
				UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
				if (displayInfo != null && displayInfo.isMapDisplayed(unit)) {
					Coordinates unitCoords = unit.getCoordinates();
					double clickRange = unitCoords.getDistance(clickedPosition);
					double unitClickRange = displayInfo.getMapClickRange();
					if (clickRange < unitClickRange) {
						mapLayerPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
						openUnitWindow(unit);
					} else
						mapLayerPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
		}
	}

	/**
	 * Checks if the mouse is hovering over a map
	 * 
	 * @param event
	 */
	public void checkHover(MouseEvent event) {

		Coordinates mapCenter = mapLayerPanel.getCenterLocation();
		if (mapCenter != null) {
			double rho = CannedMarsMap.PIXEL_RHO;

			double x = (double) (event.getX() - (Map.DISPLAY_WIDTH / 2D) - 1);
			double y = (double) (event.getY() - (Map.DISPLAY_HEIGHT / 2D) - 1);
			
			Coordinates pos = mapLayerPanel.getCenterLocation().convertRectToSpherical(x, y, rho);
			
			StringBuilder coordSB = new StringBuilder();			
			StringBuilder rgbSB = new StringBuilder();
			StringBuilder hsbSB = new StringBuilder();
			StringBuilder elevSB = new StringBuilder();
			
			double phi = pos.getPhi();
			double theta = pos.getTheta();			
			double h0 = TerrainElevation.getMOLAElevation(phi, theta);
			double h1 = TerrainElevation.getPatchedElevation(pos);
			
			phi = Math.round(phi*1000.0)/1000.0;
			theta = Math.round(theta*1000.0)/1000.0;

			elevSB.append(ELEVATION)
				.append(Math.round(h0*1000.0)/1000.0)
				.append(" / " + Math.round(h1*1000.0)/1000.0)
				.append(KM);
			
			if (topoItem.isSelected()) {
				int[] rgb = TerrainElevation.getRGB(pos);
				float[] hsb = TerrainElevation.getHSB(rgb);
				
				rgbSB.append(RGB).append(rgb[0]).append(COMMA)
				.append(rgb[1]).append(COMMA)
				.append(rgb[2]).append(CLOSE_P);
				
				hsbSB.append(HSB).append(Math.round(hsb[0]*100.0)/100.0).append(COMMA)
					.append(Math.round(hsb[1]*100.0)/100.0).append(COMMA)
					.append(Math.round(hsb[2]*100.0)/100.0).append(CLOSE_P);
			}
			
			coordSB.append(pos.getCoordinateString());
			
			updateStatusBarLabels(elevSB.toString(), coordSB.toString(), phi, theta, rgbSB.toString(), hsbSB.toString());

			boolean onTarget = false;

			Iterator<Unit> i = unitManager.getDisplayUnits().iterator();

			// Change mouse cursor if hovering over an unit on the map
			while (i.hasNext()) {
				Unit unit = i.next();

				if (unit.getUnitType() == UnitType.VEHICLE) {
					if (((Vehicle)unit).isOutsideOnMarsMission()) {
						// Proceed to below to set cursor;
					}
					else 
						continue;
				}
				
				UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
				if (displayInfo != null && displayInfo.isMapDisplayed(unit)) {
					Coordinates unitCoords = unit.getCoordinates();
					double clickRange = Coordinates.computeDistance(unitCoords, pos);
					double unitClickRange = displayInfo.getMapClickRange();
					if (clickRange < unitClickRange) {
						// System.out.println("you're on a settlement or vehicle");
						mapLayerPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
						onTarget = true;
					}
				}
			}

			// TODO: how to avoid overlapping labels ?
			
			// Change mouse cursor if hovering over a landmark on the map
			Iterator<Landmark> j = landmarks.iterator();
			while (j.hasNext()) {
				Landmark landmark = (Landmark) j.next();

				Coordinates unitCoords = landmark.getLandmarkCoord();
				double clickRange = Coordinates.computeDistance(unitCoords, pos);
				double unitClickRange = 40D;

				if (clickRange < unitClickRange) {
					onTarget = true;
					// Open a popover showing additional info on the landmark
//					startPopOver(landmark, (int)x, (int)y, event);
//					mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
				}
			}

			if (!onTarget) {
				mapLayerPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	/**
	 * Pops out a small panel for showing additional info
	 * 
	 * @param landmark
	 * @param event
	 */
	public void startPopOver(Landmark landmark, int x, int y, MouseEvent event) {
//		final WebPopup<?> popup = new WebPopup(desktop.getMainWindow().getMainPane());
//        popup.setPadding(5);
//        popup.setResizable(false);
//        popup.setDraggable(true);
//
//        final WebPanel container = new WebPanel(StyleId.panelTransparent, new BorderLayout(5, 5));
//
//        final WebLabel label = new WebLabel(landmark.getLandmarkLandingLocation(), WebLabel.CENTER);
//        container.add(label, BorderLayout.NORTH);
//
////        final String text = LM.get ( getExampleLanguagePrefix () + "text" );
////        final WebTextField field = new WebTextField ( text, 20 );
////        field.setHorizontalAlignment ( WebTextField.CENTER );
////        container.add ( field, BorderLayout.CENTER );
//
//        popup.add(container);
//
////        popup.pack();
//        popup.showPopup(event.getComponent(), x + 5, y + 5);
        
         final WebPopOver popOver = new WebPopOver(desktop.getMainWindow().getMainPane());
//         popOver.setIconImages ();
         popOver.setCloseOnFocusLoss(true);
         popOver.setPadding(2);
         popOver.setTitle(landmark.getLandmarkName());
         
         final WebPanel c = new WebPanel(StyleId.panelTransparent, new BorderLayout(5, 5));

         final WebLabel l = new WebLabel(landmark.getLandmarkLandingLocation(), WebLabel.CENTER);
         c.add(l, BorderLayout.NORTH);

//         final String text = LM.get ( getExampleLanguagePrefix () + "text" );
//         final WebTextField field = new WebTextField ( text, 20 );
//         field.setHorizontalAlignment ( WebTextField.CENTER );
//         container.add ( field, BorderLayout.CENTER );

         popOver.add(c);
         popOver.show(event.getComponent(), x + 5, y + 5, PopOverDirection.down);
	}

	@Override
	public void destroy() {
		if (mapLayerPanel != null)
			mapLayerPanel.destroy();
		if (globeNav != null)
			globeNav.destroy();

		latCB = null;
		longCB = null;
		mapLayerPanel = null;
		globeNav = null;
//		ruler = null;
//		latText = null;
//		longText = null;
		latDir = null;
		longDir = null;
		goThere = null;
		optionsButton = null;
		optionsMenu = null;
		mineralsButton = null;
		topoItem = null;
		surfItem = null;
		geoItem = null;
		unitLabelItem = null;
		dayNightItem = null;
		trailItem = null;
		landmarkItem = null;
		navpointItem = null;
		exploredSiteItem = null;
		mineralItem = null;
		mapPaneInner = null;
		
		unitIconLayer = null;
		unitLabelLayer = null;
		shadingLayer = null;
		mineralLayer = null;
		trailLayer = null;
		navpointLayer = null;
		landmarkLayer = null;
		exploredSiteLayer = null;
	}
}
