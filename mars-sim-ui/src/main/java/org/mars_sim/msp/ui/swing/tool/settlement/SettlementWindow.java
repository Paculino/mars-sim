/*
 * Mars Simulation Project
 * SettlementWindow.java
 * @date 2021-12-06
 * @author Lars Naesbye Christensen
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;

import javax.swing.JLayer;
import javax.swing.WindowConstants;
import javax.swing.plaf.LayerUI;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpotlightLayerUI;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;

import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.statusbar.WebStatusBar;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;

/**
 * The SettlementWindow is a tool window that displays the Settlement Map Tool.
 */
@SuppressWarnings("serial")
public class SettlementWindow extends ToolWindow {

	// default logger.
	// private static final Logger logger = Logger.getLogger(SettlementWindow.class.getName());

	/** Tool name. */
	public static final String NAME = Msg.getString("SettlementWindow.title"); //$NON-NLS-1$

	public static String css_file = MainDesktopPane.BLUE_CSS;

	public static final String SOL = " Sol : ";
	public static final String POPULATION = "  Population : ";
	public static final String CAP = "  Capacity : ";
	public static final String WHITESPACES_2 = "  ";
	public static final String COMMA = ", ";
	public static final String CLOSE_PARENT = ")  ";
	public static final String WITHIN_BLDG = "  Building : (";
	public static final String SETTLEMENT_MAP = "  Map : (";
	public static final String PIXEL_MAP = "  Window : (";

	public static final int HORIZONTAL = 800;// 630;
	public static final int VERTICAL = 800;// 590;

	private WebStyledLabel buildingXYLabel;
	private WebStyledLabel mapXYLabel;
	private WebStyledLabel pixelXYLabel;
	private WebStyledLabel popLabel;
	private WebPanel subPanel;

	/** The status bar. */
	private WebStatusBar statusBar;
	/** Map panel. */
	private SettlementMapPanel mapPanel;

	private Font sansSerif12Plain = new Font("SansSerif", Font.PLAIN, 12);
	private Font sansSerif13Bold = new Font("SansSerif", Font.BOLD, 13);
	
	/**
	 * Constructor.
	 *
	 * @param desktop the main desktop panel.
	 */
	public SettlementWindow(MainDesktopPane desktop) {
		// Use ToolWindow constructor
		super(NAME, desktop);

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		setBackground(Color.BLACK);

		WebPanel mainPanel = new WebPanel(new BorderLayout());
		setContentPane(mainPanel);

		// Creates the status bar for showing the x/y coordinates and population
        statusBar = new WebStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        popLabel = new WebStyledLabel(StyleId.styledlabelShadow);
        popLabel.setFont(sansSerif13Bold);
        popLabel.setForeground(Color.DARK_GRAY);
	    buildingXYLabel = new WebStyledLabel(StyleId.styledlabelShadow);
	    buildingXYLabel.setFont(sansSerif12Plain);
	    buildingXYLabel.setForeground(Color.GREEN.darker().darker().darker());
	    mapXYLabel = new WebStyledLabel(StyleId.styledlabelShadow);
	    mapXYLabel.setFont(sansSerif12Plain);
	    mapXYLabel.setForeground(Color.ORANGE.darker());
	    pixelXYLabel = new WebStyledLabel(StyleId.styledlabelShadow);
	    pixelXYLabel.setFont(sansSerif12Plain);
	    pixelXYLabel.setForeground(Color.GRAY);

	    WebPanel emptyPanel = new WebPanel();
	    emptyPanel.setPreferredSize(new Dimension(145, 20));
	    emptyPanel.add(new WebLabel(""));

	    WebPanel w0 = new WebPanel();
	    w0.setPreferredSize(new Dimension(125, 20));
	    w0.add(pixelXYLabel);

	    WebPanel w1 = new WebPanel();
	    w1.setPreferredSize(new Dimension(115, 20));
	    w1.add(popLabel);

	    WebPanel w2 = new WebPanel();
	    w2.setPreferredSize(new Dimension(145, 20));
	    w2.add(buildingXYLabel);

	    WebPanel w3 = new WebPanel();
	    w3.setPreferredSize(new Dimension(135, 20));
	    w3.add(mapXYLabel);

        statusBar.add(w0);
        statusBar.add(emptyPanel);
        statusBar.addToMiddle(w1);
        statusBar.addToEnd(w2);
        statusBar.addToEnd(w3);

        // Create subPanel for housing the settlement map
		subPanel = new WebPanel(new BorderLayout());
		mainPanel.add(subPanel, BorderLayout.CENTER);
		subPanel.setBackground(Color.BLACK);

		mapPanel = new SettlementMapPanel(desktop, this);
		mapPanel.createUI();

		// Added SpotlightLayerUI
		LayerUI<WebPanel> layerUI = new SpotlightLayerUI(mapPanel);
		JLayer<WebPanel> jlayer = new JLayer<WebPanel>(mapPanel, layerUI);
		subPanel.add(jlayer, BorderLayout.CENTER);

		setSize(new Dimension(HORIZONTAL, VERTICAL));
		setPreferredSize(new Dimension(HORIZONTAL, VERTICAL));
		setMinimumSize(new Dimension(HORIZONTAL / 2, VERTICAL / 2));
		setClosable(true);
		setResizable(false);
		setMaximizable(true);

		setVisible(true);
	}

	/**
	 * Gets the settlement map panel.
	 *
	 * @return the settlement map panel.
	 */
	public SettlementMapPanel getMapPanel() {
		return mapPanel;
	}

	public String format0(double x, double y) {
//		return String.format("%6.2f,%6.2f", x, y);
		return Math.round(x*100.00)/100.00 + ", " + Math.round(y*100.00)/100.00;
	}

	public String format1(double x, double y) {
//		return String.format("%6.2f,%6.2f", x, y);
		return (int)x + ", " + (int)y;
	}

	/**
	 * Sets the label of the coordinates within a building
	 *
	 * @param x
	 * @param y
	 * @param blank
	 */
	public void setBuildingXYCoord(double x, double y, boolean blank) {
		if (blank) {
			buildingXYLabel.setText("");
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append(WITHIN_BLDG).append(format0(x, y)).append(CLOSE_PARENT);
			buildingXYLabel.setText(sb.toString());
		}
	}

	/**
	 * Sets the x/y pixel label of the settlement window
	 *
	 * @param point
	 * @param blank
	 */
	public void setPixelXYCoord(double x, double y, boolean blank) {
		if (blank) {
			pixelXYLabel.setText("");
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append(PIXEL_MAP).append(format1(x, y)).append(CLOSE_PARENT);
			pixelXYLabel.setText(sb.toString());
		}
	}

	/**
	 * Sets the label of the settlement map coordinates
	 *
	 * @param point
	 * @param blank
	 */
	public void setMapXYCoord(Point.Double point, boolean blank) {
		if (blank) {
			mapXYLabel.setText("");
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append(SETTLEMENT_MAP).append(format0(point.getX(), point.getY())).append(CLOSE_PARENT);
			mapXYLabel.setText(sb.toString());
		}
	}

	/**
	 * Sets the population label
	 *
	 * @param pop
	 */
	public void setPop(int pop) {
        popLabel.setText(POPULATION + pop + WHITESPACES_2);
	}

	@Override
	public void destroy() {
		buildingXYLabel = null;
		pixelXYLabel = null;
		mapXYLabel = null;
		popLabel = null;
		statusBar = null;
		mapPanel.destroy();
		mapPanel = null;
		desktop = null;

	}

}
