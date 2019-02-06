/**
 * Mars Simulation Project
 * NavigatorWindow.java
 * @version 3.1.0 2016-10-26
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.navigator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.mars.Landmark;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.map.CannedMarsMap;
import org.mars_sim.msp.ui.swing.tool.map.ExploredSiteMapLayer;
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
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.menu.WebCheckBoxMenuItem;
import com.alee.laf.menu.WebPopupMenu;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextField;

/**
 * The NavigatorWindow is a tool window that displays a map and a globe showing
 * Mars, and various other elements. It is the primary interface component that
 * presents the simulation to the user.
 */
public class NavigatorWindow extends ToolWindow implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Tool name. */
	public static final String NAME = Msg.getString("NavigatorWindow.title"); //$NON-NLS-1$

	public static final int HORIZONTAL = 635;
	public static final int VERTICAL = 435;

	public static final int HORIZONTAL_MINIMAP = 300;// 274
	public static final int VERTICAL_MINIMAP = 700;// 695;

	// Data members
	private Integer[] lon_degrees = new Integer[361];
	private Integer[] lat_degrees = new Integer[91];

	private JComboBoxMW<?> latCB;
	private JComboBoxMW<?> longCB;
	/** map navigation. */
	private MapPanel mapPanel;
	/** Globe navigation. */
	private GlobeDisplay globeNav;
	/** Compass navigation buttons. */
	// private NavButtonDisplay navButtons;
	/** Topographical and distance legend. */
	private LegendDisplay ruler;
	/** Latitude entry. */
	private WebTextField latText;
	/** Longitude entry. */
	private WebTextField longText;
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
	/** Topographical map menu item. */
	private WebCheckBoxMenuItem topoItem;
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

	private WebPanel mapPaneInner;

	private MapLayer unitIconLayer;
	private MapLayer unitLabelLayer;
	private MapLayer shadingLayer;
	private MapLayer mineralLayer;
	private MapLayer trailLayer;
	private MapLayer navpointLayer;
	private MapLayer landmarkLayer;
	private MapLayer exploredSiteLayer;

	private static List<Landmark> landmarks;
	
	private static Simulation sim = Simulation.instance();
	private static UnitManager unitManager = sim.getUnitManager();

	/**
	 * Constructor.
	 * 
	 * @param desktop {@link MainDesktopPane} the desktop pane
	 */
	public NavigatorWindow(MainDesktopPane desktop) {
		// use ToolWindow constructor
		super(NAME, desktop);
		this.desktop = desktop;

		landmarks = sim.getMars().getSurfaceFeatures().getLandmarks();

//		if (desktop.getMainScene() != null) {

			// setTitleName(null);
			// ...
//			putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
//			getRootPane().setWindowDecorationStyle(JRootPane.NONE);
//			// Remove title bar
//			BasicInternalFrameUI bi = (BasicInternalFrameUI) super.getUI();
//			bi.setNorthPane(null);
//			// Remove border (not working)
//			setBorder(null);

			// Prepare content pane
			JPanel wholePane = new JPanel();
			// mainPane.setLayout(new BorderLayout());
			wholePane.setLayout(new BoxLayout(wholePane, BoxLayout.Y_AXIS));
			// mainPane.setBorder(new MarsPanelBorder());
			setContentPane(wholePane);

			// Prepare globe display
			globeNav = new GlobeDisplay(this);
			WebPanel globePane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			// globePane.setMaximumSize(new Dimension(HORIZONTAL_MINIMAP,
			// HORIZONTAL_MINIMAP));
			globePane.setBackground(Color.black);
			globePane.setOpaque(true);
			globePane.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new LineBorder(Color.gray)));
			globePane.add(globeNav);

			globePane.setAlignmentX(Component.CENTER_ALIGNMENT);
			wholePane.add(globePane);// , BorderLayout.NORTH);

			///////////////////////////////////////////////////////////////////////////

			mapPaneInner = new WebPanel(new BorderLayout(0, 0)); // FlowLayout(FlowLayout.LEFT, 0, 0));
			mapPaneInner
					.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new LineBorder(Color.gray)));
			mapPaneInner.setBackground(Color.black);
			// mapPaneInner.setMaximumSize(new Dimension(HORIZONTAL_MINIMAP,
			// HORIZONTAL_MINIMAP));
			mapPaneInner.setOpaque(true);
			mapPaneInner.setAlignmentX(CENTER_ALIGNMENT);
			mapPaneInner.setAlignmentY(CENTER_ALIGNMENT);
			// mapPaneInner.setCursor(new Cursor(java.awt.Cursor.DEFAULT_CURSOR));

			WebPanel detailPane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			detailPane.setMaximumHeight(HORIZONTAL_MINIMAP - 10);
			// detailPane.setBorder( new CompoundBorder(new
			// BevelBorder(BevelBorder.LOWERED),
			// new LineBorder(Color.gray)));
			detailPane.setBackground(Color.black);
			detailPane.setOpaque(true);
			detailPane.add(mapPaneInner);

			mapPanel = new MapPanel(desktop, 500L);
			// map.setMaximumSize(new Dimension(HORIZONTAL_MINIMAP, HORIZONTAL_MINIMAP));
			mapPanel.setNavWin(this);
			mapPanel.addMouseListener(new mapListener());
			mapPanel.addMouseMotionListener(new mouseMotionListener());
			// map.setCursor(new Cursor(java.awt.Cursor.DEFAULT_CURSOR));

			// Create map layers.
			unitIconLayer = new UnitIconMapLayer(mapPanel);
			unitLabelLayer = new UnitLabelMapLayer();
			mineralLayer = new MineralMapLayer(mapPanel);
			shadingLayer = new ShadingMapLayer(mapPanel);
			navpointLayer = new NavpointMapLayer(mapPanel);
			trailLayer = new VehicleTrailMapLayer();
			landmarkLayer = new LandmarkMapLayer();
			exploredSiteLayer = new ExploredSiteMapLayer(mapPanel);

			// Add default map layers.
			mapPanel.addMapLayer(shadingLayer, 0);
			mapPanel.addMapLayer(unitIconLayer, 2);
			mapPanel.addMapLayer(unitLabelLayer, 3);
			mapPanel.addMapLayer(navpointLayer, 4);
			mapPanel.addMapLayer(trailLayer, 5);
			mapPanel.addMapLayer(landmarkLayer, 6);

			mapPanel.showMap(new Coordinates((Math.PI / 2D), 0D));
			// mapPaneInner.setAlignmentX(Component.CENTER_ALIGNMENT);
			mapPaneInner.add(mapPanel, BorderLayout.CENTER);
			// mapPane.add(mapPaneInner, BorderLayout.CENTER);
			wholePane.add(detailPane);// , BorderLayout.CENTER);

			///////////////////////////////////////////////////////////////////////////

			WebPanel bottomPane = new WebPanel();// new BorderLayout(0,0));//FlowLayout(FlowLayout.CENTER, 0, 0));/
			bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.Y_AXIS));
			// bottomPane.setBorder(new EmptyBorder(0, 3, 0, 0));
			bottomPane.setAlignmentX(Component.CENTER_ALIGNMENT);
			// bottomPane.setMaximumSize(new Dimension(HORIZONTAL_MINIMAP, 15));
			bottomPane.setMaximumHeight(35);
			wholePane.add(bottomPane);// , BorderLayout.SOUTH);
			// mapPaneInner.add(bottomPane, BorderLayout.SOUTH);

			///////////////////////////////////////////////////////////////////////////
			// Prepare position entry panel
			WebPanel coordPane = new WebPanel(new GridLayout(1, 6, 0, 0));// FlowLayout(FlowLayout.LEFT));//
			// coordPane.setBorder(new EmptyBorder(6, 6, 3, 3));
			coordPane.setMaximumHeight(18);
			coordPane.setAlignmentX(Component.CENTER_ALIGNMENT);
			coordPane.setAlignmentY(Component.TOP_ALIGNMENT);
			bottomPane.add(coordPane);// , BorderLayout.NORTH);

			// Prepare latitude entry components
			WebLabel latLabel = new WebLabel(" Lat : ", WebLabel.RIGHT);// Msg.getString("NavigatorWindow.latitude")); //$NON-NLS-1$
			// latLabel.setAlignmentY(.5F);
			coordPane.add(latLabel);

			// latText = new WebTextField(5);
			// latText.setCaretPosition(latText.getText().length());
			// latText.setHorizontalAlignment(WebTextField.CENTER);
			// coordPane.add(latText);

			// 2016-11-24 Switch to using ComboBoxMW for latitude
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
			latCB.setPreferredSize(new Dimension(80, -1));
			coordPane.add(latCB);

			String[] latStrings = { Msg.getString("direction.degreeSign") + Msg.getString("direction.northShort"), //$NON-NLS-1$ //$NON-NLS-2$
					Msg.getString("direction.degreeSign") + Msg.getString("direction.southShort") //$NON-NLS-1$ //$NON-NLS-2$
			};
			latDir = new JComboBoxMW<Object>(latStrings);
			latDir.setEditable(false);
			latDir.setPreferredSize(new Dimension(20, -1));
			coordPane.add(latDir);

			// Put glue and strut spacers in
			// positionPane.add(Box.createHorizontalGlue());
			// positionPane.add(Box.createHorizontalStrut(5));

			// Prepare longitude entry components
			WebLabel longLabel = new WebLabel("Lon : ", WebLabel.RIGHT);// Msg.getString("NavigatorWindow.longitude")); //$NON-NLS-1$
			// longLabel.setAlignmentY(.5F);
			coordPane.add(longLabel);

			// longText = new WebTextField(5);
			// longText.setCaretPosition(longText.getText().length());
			// longText.setHorizontalAlignment(WebTextField.CENTER);
			// coordPane.add(longText);

			// 2016-11-24 Switch to using ComboBoxMW for longtitude
			longCB = new JComboBoxMW<Integer>(lon_degrees);
			longCB.setSelectedItem(0);
			longCB.setPreferredSize(new Dimension(80, -1));
			coordPane.add(longCB);

			String[] longStrings = { Msg.getString("direction.degreeSign") + Msg.getString("direction.eastShort"), //$NON-NLS-1$ //$NON-NLS-2$
					Msg.getString("direction.degreeSign") + Msg.getString("direction.westShort") //$NON-NLS-1$ //$NON-NLS-2$
			};
			longDir = new JComboBoxMW<Object>(longStrings);
			longDir.setEditable(false);
			longDir.setPreferredSize(new Dimension(20, -1));
			coordPane.add(longDir);

			// Put glue and strut spacers in
			// positionPane.add(Box.createHorizontalGlue());
			// positionPane.add(Box.createHorizontalStrut(5));

			// Prepare location entry submit button
			// goThere = new WebButton(Msg.getString("NavigatorWindow.button.goThere"));
			// //$NON-NLS-1$
			// goThere.addActionListener(this);
			// goThere.setAlignmentY(.5F);
			// positionPane.add(goThere);
			// bottomPane.add(goThere, BorderLayout.SOUTH);

			///////////////////////////////////////////////////////////////////////////

			// Prepare topographical panel
			// WebPanel buttonsPane = new WebPanel(new BorderLayout());
			// buttonsPane.setBorder(new EmptyBorder(0, 3, 0, 0));
			// bottomPane.add(buttonsPane, BorderLayout.NORTH);

			// Prepare options panel
			WebPanel optionsPane = new WebPanel(new GridLayout(1, 3));
			optionsPane.setMaximumHeight(15);
			optionsPane.setAlignmentX(Component.CENTER_ALIGNMENT);
			bottomPane.add(optionsPane);// , BorderLayout.CENTER);

			// Prepare location entry submit button
			goThere = new WebButton(Msg.getString("NavigatorWindow.button.resetGo")); //$NON-NLS-1$
			goThere.setToolTipText("Go to the location with your specified coordinates");
			goThere.addActionListener(this);
			// goThere.setAlignmentY(.5F);
			// positionPane.add(goThere);
			optionsPane.add(goThere);

			// Prepare options button.
			optionsButton = new WebButton(Msg.getString("NavigatorWindow.button.mapOptions")); //$NON-NLS-1$
			optionsButton.setToolTipText(Msg.getString("NavigatorWindow.tooltip.mapOptions")); //$NON-NLS-1$
			optionsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					SwingUtilities.invokeLater(() -> {
						if (optionsMenu == null)
							createOptionsMenu();
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

			// Prepare legend icon
			// ruler = new LegendDisplay();
			// ruler.setBorder( new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
			// new LineBorder(Color.gray)));
			// WebPanel legendPanel = new WebPanel(new BorderLayout(0, 0));
			// legendPanel.add(ruler, BorderLayout.NORTH);
			// buttonsPane.add(legendPanel, BorderLayout.NORTH);
			optionsPane.setSize(new Dimension(HORIZONTAL_MINIMAP, 30));

//			setMaximumSize(new Dimension(HORIZONTAL_MINIMAP, VERTICAL_MINIMAP));
//			setSize(new Dimension(HORIZONTAL_MINIMAP, VERTICAL_MINIMAP));
			setPreferredSize(new Dimension(HORIZONTAL_MINIMAP, VERTICAL_MINIMAP));

//		}
//
//		else {
//
//			// Prepare content pane
//			WebPanel mainPane = new WebPanel();
//			mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
//			mainPane.setBorder(new MarsPanelBorder());
//			setContentPane(mainPane);
//
//			// Prepare top layout panes
//			WebPanel topMainPane = new WebPanel();
//			topMainPane.setLayout(new BoxLayout(topMainPane, BoxLayout.X_AXIS));
//			mainPane.add(topMainPane);
//
//			WebPanel leftTopPane = new WebPanel();
//			leftTopPane.setLayout(new BoxLayout(leftTopPane, BoxLayout.Y_AXIS));
//			topMainPane.add(leftTopPane);
//
//			// Prepare globe display
//			// globeNav = new GlobeDisplay(this, 150, 150);
//			globeNav = new GlobeDisplay(this);// , GlobeDisplay.GLOBE_MAP_WIDTH, GlobeDisplay.GLOBE_MAP_HEIGHT);
//			WebPanel globePane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
//			globePane.setBackground(Color.black);
//			globePane.setOpaque(true);
//			globePane.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new LineBorder(Color.gray)));
//			globePane.add(globeNav);
//			leftTopPane.add(globePane);
//
//			// Prepare navigation buttons display
////			navButtons = new NavButtonDisplay(this);
////			WebPanel navPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
////			navPane.setBorder( new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
////					new LineBorder(Color.gray)));
////			navPane.add(navButtons);
////			leftTopPane.add(navPane);
//
//			// Put strut spacer in
//			topMainPane.add(Box.createHorizontalStrut(5));
//
//			WebPanel rightTopPane = new WebPanel();
//			rightTopPane.setLayout(new BoxLayout(rightTopPane, BoxLayout.Y_AXIS));
//			topMainPane.add(rightTopPane);
//
//			// Prepare surface map display
//			WebPanel mapPane = new WebPanel(new BorderLayout(0, 0));
//			mapPane.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new LineBorder(Color.gray)));
//			rightTopPane.add(mapPane);
//			mapPaneInner = new WebPanel(new BorderLayout(0, 0));
//			mapPaneInner.setBackground(Color.black);
//			mapPaneInner.setOpaque(true);
//			// mapPaneInner.setCursor(new Cursor(java.awt.Cursor.DEFAULT_CURSOR));
//
//			map = new MapPanel(desktop, 500L);
//			map.setNavWin(this);
//			map.addMouseListener(new mapListener());
//			map.addMouseMotionListener(new mouseMotionListener());
//			map.setCursor(new Cursor(java.awt.Cursor.DEFAULT_CURSOR));
//
//			// Create map layers.
//			unitIconLayer = new UnitIconMapLayer(map);
//			unitLabelLayer = new UnitLabelMapLayer();
//			mineralLayer = new MineralMapLayer(map);
//			shadingLayer = new ShadingMapLayer(map);
//			navpointLayer = new NavpointMapLayer(map);
//			trailLayer = new VehicleTrailMapLayer();
//			landmarkLayer = new LandmarkMapLayer();
//			exploredSiteLayer = new ExploredSiteMapLayer(map);
//
//			// Add default map layers.
//			map.addMapLayer(shadingLayer, 0);
//			map.addMapLayer(unitIconLayer, 2);
//			map.addMapLayer(unitLabelLayer, 3);
//			map.addMapLayer(navpointLayer, 4);
//			map.addMapLayer(trailLayer, 5);
//			map.addMapLayer(landmarkLayer, 6);
//
//			map.showMap(new Coordinates((Math.PI / 2D), 0D));
//			mapPaneInner.add(map, BorderLayout.CENTER);
//			mapPane.add(mapPaneInner, BorderLayout.CENTER);
//
//			// Put some glue in to fill in extra space
//			rightTopPane.add(Box.createVerticalStrut(5));
//
//			// Prepare topographical panel
//			WebPanel topoPane = new WebPanel(new BorderLayout());
//			topoPane.setBorder(new EmptyBorder(0, 3, 0, 0));
//			mainPane.add(topoPane);
//
//			// Prepare options panel
//			WebPanel optionsPane = new WebPanel(new GridLayout(2, 1));
//			topoPane.add(optionsPane, BorderLayout.CENTER);
//
//			// Prepare options button.
//			optionsButton = new WebButton(Msg.getString("NavigatorWindow.button.mapOptions")); //$NON-NLS-1$
//			optionsButton.setToolTipText(Msg.getString("NavigatorWindow.tooltip.mapOptions")); //$NON-NLS-1$
//			optionsButton.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent event) {
//					SwingUtilities.invokeLater(() -> {
//						if (optionsMenu == null)
//							createOptionsMenu();
//						optionsMenu.show(optionsButton, 0, optionsButton.getHeight());
//					});
//				}
//			});
//			optionsPane.add(optionsButton);
//
//			// Prepare minerals button.0
//			mineralsButton = new WebButton(Msg.getString("NavigatorWindow.button.mineralOptions")); //$NON-NLS-1$
//			mineralsButton.setToolTipText(Msg.getString("NavigatorWindow.tooltip.mineralOptions")); //$NON-NLS-1$
//			mineralsButton.setEnabled(false);
//			mineralsButton.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent event) {
//					SwingUtilities.invokeLater(() -> {
//						WebPopupMenu mineralsMenu = createMineralsMenu();
//						mineralsMenu.show(mineralsButton, 0, mineralsButton.getHeight());
//					});
//				}
//			});
//			optionsPane.add(mineralsButton);
//
//			// Prepare legend icon
//			ruler = new LegendDisplay();
//			ruler.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new LineBorder(Color.gray)));
//			WebPanel legendPanel = new WebPanel(new BorderLayout(0, 0));
//			legendPanel.add(ruler, BorderLayout.NORTH);
//			topoPane.add(legendPanel, BorderLayout.EAST);
//
//			// Prepare position entry panel
//			WebPanel positionPane = new WebPanel();
//			positionPane.setLayout(new BoxLayout(positionPane, BoxLayout.X_AXIS));
//			positionPane.setBorder(new EmptyBorder(6, 6, 3, 3));
//			mainPane.add(positionPane);
//
//			// Prepare latitude entry components
//			WebLabel latLabel = new WebLabel(Msg.getString("NavigatorWindow.latitude")); //$NON-NLS-1$
//			latLabel.setAlignmentY(.5F);
//			positionPane.add(latLabel);
//
//			latText = new WebTextField(5);
//			positionPane.add(latText);
//
//			String[] latStrings = { Msg.getString("direction.degreeSign") + Msg.getString("direction.northShort"), //$NON-NLS-1$ //$NON-NLS-2$
//					Msg.getString("direction.degreeSign") + Msg.getString("direction.southShort") //$NON-NLS-1$ //$NON-NLS-2$
//			};
//			latDir = new JComboBoxMW<Object>(latStrings);
//			latDir.setEditable(false);
//			latDir.setPreferredSize(new Dimension(50, -1));
//			positionPane.add(latDir);
//
//			// Put glue and strut spacers in
//			positionPane.add(Box.createHorizontalGlue());
//			positionPane.add(Box.createHorizontalStrut(5));
//
//			// Prepare longitude entry components
//			WebLabel longLabel = new WebLabel(Msg.getString("NavigatorWindow.longitude")); //$NON-NLS-1$
//			longLabel.setAlignmentY(.5F);
//			positionPane.add(longLabel);
//
//			longText = new WebTextField(5);
//			positionPane.add(longText);
//
//			String[] longStrings = { Msg.getString("direction.degreeSign") + Msg.getString("direction.eastShort"), //$NON-NLS-1$ //$NON-NLS-2$
//					Msg.getString("direction.degreeSign") + Msg.getString("direction.westShort") //$NON-NLS-1$ //$NON-NLS-2$
//			};
//			longDir = new JComboBoxMW<Object>(longStrings);
//			longDir.setEditable(false);
//			longDir.setPreferredSize(new Dimension(50, -1));
//			positionPane.add(longDir);
//
//			// Put glue and strut spacers in
//			positionPane.add(Box.createHorizontalGlue());
//			positionPane.add(Box.createHorizontalStrut(5));
//
//			// Prepare location entry submit button
//			goThere = new WebButton(Msg.getString("NavigatorWindow.button.resetGo")); //$NON-NLS-1$
//			goThere.addActionListener(this);
//			goThere.setAlignmentY(.5F);
//			positionPane.add(goThere);
//
//			setMaximumSize(new Dimension(HORIZONTAL, VERTICAL));
//			// setSize(new Dimension(HORIZONTAL, VERTICAL));
//			// setPreferredSize(new Dimension(HORIZONTAL, VERTICAL));
//
//		}

//		if (desktop.getMainScene() != null) {
//			setClosable(false);
//			setResizable(false);
//			setMaximizable(false);
//		} else {
			setClosable(true);
			setResizable(false);
			setMaximizable(false);
//		}

		setVisible(true);
		// Pack window
		pack();
	}

	/**
	 * Update coordinates in map, buttons, and globe Redraw map and globe if
	 * necessary
	 * 
	 * @param newCoords the new center location
	 */
	public void updateCoords(Coordinates newCoords) {
		// navButtons.updateCoords(newCoords);
		mapPanel.showMap(newCoords);
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

	public void showSurfaceMap() {
		// show surface map
		mapPanel.setMapType(SurfMarsMap.TYPE);
		globeNav.showSurf();
		// ruler.showMap();
	}

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

					double phi = Math.PI * (latitude / 180D);
					double theta = (2 * Math.PI) * (longitude / 360D);
					updateCoords(new Coordinates(phi, theta));
				}
			} catch (NumberFormatException e) {
			}
		} else if (source == topoItem) {
			if (topoItem.isSelected()) {
				// turn off day night layer
				setMapLayer(dayNightItem.isSelected(), 0, shadingLayer);
				globeNav.setDayNightTracking(dayNightItem.isSelected());
				// show topo map
				mapPanel.setMapType(TopoMarsMap.TYPE);
				globeNav.showTopo();
				// ruler.showColor();
			}

			else {
				// show surface map
				mapPanel.setMapType(SurfMarsMap.TYPE);
				globeNav.showSurf();
				// ruler.showMap();
			}
		} else if (source == dayNightItem) {
			setMapLayer(dayNightItem.isSelected(), 0, shadingLayer);
			globeNav.setDayNightTracking(dayNightItem.isSelected());
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
			mapPanel.addMapLayer(mapLayer, index);
		} else {
			mapPanel.removeMapLayer(mapLayer);
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
				mapPanel.hasMapLayer(shadingLayer));
		dayNightItem.addActionListener(this);
		optionsMenu.add(dayNightItem);
		// 2016-06-08 Unchecked dayNightItem at the start of sim
		// globeNav.setDayNightTracking(false);
		// dayNightItem.setSelected(false);

		// Create topographical map menu item.
		topoItem = new WebCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.topo"), //$NON-NLS-1$
				TopoMarsMap.TYPE.equals(mapPanel.getMapType()));
		topoItem.addActionListener(this);
		optionsMenu.add(topoItem);

		// Create unit label menu item.
		unitLabelItem = new WebCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showLabels"), //$NON-NLS-1$
				mapPanel.hasMapLayer(unitLabelLayer));
		unitLabelItem.addActionListener(this);
		optionsMenu.add(unitLabelItem);

		// Create vehicle trails menu item.
		trailItem = new WebCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showVehicleTrails"), //$NON-NLS-1$
				mapPanel.hasMapLayer(trailLayer));
		trailItem.addActionListener(this);
		optionsMenu.add(trailItem);

		// Create landmarks menu item.
		landmarkItem = new WebCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showLandmarks"), //$NON-NLS-1$
				mapPanel.hasMapLayer(landmarkLayer));
		landmarkItem.addActionListener(this);
		optionsMenu.add(landmarkItem);

		// Create navpoints menu item.
		navpointItem = new WebCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showNavPoints"), //$NON-NLS-1$
				mapPanel.hasMapLayer(navpointLayer));
		navpointItem.addActionListener(this);
		optionsMenu.add(navpointItem);

		// Create explored site menu item.
		exploredSiteItem = new WebCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showExploredSites"), //$NON-NLS-1$
				mapPanel.hasMapLayer(exploredSiteLayer));
		exploredSiteItem.addActionListener(this);
		optionsMenu.add(exploredSiteItem);

		// Create minerals menu item.
		mineralItem = new WebCheckBoxMenuItem(Msg.getString("NavigatorWindow.menu.map.showMinerals"), //$NON-NLS-1$
				mapPanel.hasMapLayer(mineralLayer));
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

	private class mapListener extends MouseAdapter {
		public void mouseEntered(MouseEvent event) {
			// checkHover(event);
		}
		public void mouseExited(MouseEvent event) {
		}

		public void mouseClicked(MouseEvent event) {
			checkClick(event);
		}
	}

	private class mouseMotionListener extends MouseMotionAdapter {
		public void mouseMoved(MouseEvent event) {
			checkHover(event);
		}
		public void mouseDragged(MouseEvent event) {
		}
	}

	public void checkClick(MouseEvent event) {

		if (mapPanel.getCenterLocation() != null) {
			double rho = CannedMarsMap.PIXEL_RHO;

			double x = (double) (event.getX() - (Map.DISPLAY_WIDTH / 2D) - 1);
			double y = (double) (event.getY() - (Map.DISPLAY_HEIGHT / 2D) - 1);

			Coordinates clickedPosition = mapPanel.getCenterLocation().convertRectToSpherical(x, y, rho);

			Iterator<Unit> i = unitManager.computeUnits().iterator();

			// Open window if unit is clicked on the map
			while (i.hasNext()) {
				Unit unit = i.next();
				UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
				if (displayInfo != null && displayInfo.isMapDisplayed(unit)) {
					Coordinates unitCoords = unit.getCoordinates();
					double clickRange = unitCoords.getDistance(clickedPosition);
					double unitClickRange = displayInfo.getMapClickRange();
					if (clickRange < unitClickRange) {
						mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
						openUnitWindow(unit);
					} else
						mapPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
		}
	}

	public void checkHover(MouseEvent event) {

		Coordinates mapCenter = mapPanel.getCenterLocation();
		if (mapCenter != null) {
			double rho = CannedMarsMap.PIXEL_RHO;

			double x = (double) (event.getX() - (Map.DISPLAY_WIDTH / 2D) - 1);
			double y = (double) (event.getY() - (Map.DISPLAY_HEIGHT / 2D) - 1);
			// System.out.println("x is " + x + " y is " + y);
			Coordinates mousePos = mapPanel.getCenterLocation().convertRectToSpherical(x, y, rho);
			boolean onTarget = false;

			Iterator<Unit> i = unitManager.computeUnits().iterator();

			// Change mouse cursor if hovering over an unit on the map
			while (i.hasNext()) {
				Unit unit = i.next();
				UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
				if (displayInfo != null && displayInfo.isMapDisplayed(unit)) {
					Coordinates unitCoords = unit.getCoordinates();
					double clickRange = unitCoords.getDistance(mousePos);
					double unitClickRange = displayInfo.getMapClickRange();
					if (clickRange < unitClickRange) {
						// System.out.println("you're on a settlement or vehicle");
						mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
						onTarget = true;
					}
				}
			}

			// Change mouse cursor if hovering over a landmark on the map
			Iterator<Landmark> j = landmarks.iterator();
			while (j.hasNext()) {
				Landmark landmark = (Landmark) j.next();

				Coordinates unitCoords = landmark.getLandmarkLocation();
				double clickRange = unitCoords.getDistance(mousePos);
				double unitClickRange = 40D;

				if (clickRange < unitClickRange) {
					onTarget = true;
					// System.out.println("you're on a landmark");
					// TODO: may open a panel showing any special items at that landmark
					mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
					// System.out.println("right on landmark");
				}
			}

			if (!onTarget) {
				mapPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	public MainDesktopPane getDesktop() {
		return desktop;
	}

	public void destroy() {
		mapPanel.destroy();
		globeNav.destroy();

		latCB = null;
		longCB = null;
		mapPanel = null;
		globeNav = null;
		ruler = null;
		latText = null;
		longText = null;
		latDir = null;
		longDir = null;
		goThere = null;
		optionsButton = null;
		optionsMenu = null;
		mineralsButton = null;
		topoItem = null;
		unitLabelItem = null;
		dayNightItem = null;
		trailItem = null;
		landmarkItem = null;
		navpointItem = null;
		exploredSiteItem = null;
		mineralItem = null;
		mapPaneInner = null;
		
		unitManager = null;
		landmarks = null;
		
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