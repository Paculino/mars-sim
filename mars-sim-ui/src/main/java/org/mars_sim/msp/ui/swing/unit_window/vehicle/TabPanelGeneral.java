/*
 * Mars Simulation Project
 * TabPanelGeneral.java
 * @date 2023-02-25
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;


import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.batik.gvt.GraphicsNode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.svg.SVGGraphicNodeIcon;
import org.mars_sim.msp.ui.swing.tool.svg.SVGMapUtil;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * This tab shows the general details of the Vehicle type.
 */
@SuppressWarnings("serial")
public class TabPanelGeneral extends TabPanel {

	private static final String ID_ICON = "info";
	
	private Vehicle v;

	/**
	 * Constructor.
	 */
	public TabPanelGeneral(Vehicle v, MainDesktopPane desktop) {
		super(
			Msg.getString("BuildingPanelGeneral.title"),
			ImageLoader.getIconByName(ID_ICON), 
			Msg.getString("BuildingPanelGeneral.title"),
			v, desktop);
		this.v = v;
	}

	/**
	 * Build the UI elements
	 */
	@Override
	protected void buildUI(JPanel center) {

		JPanel topPanel = new JPanel(new BorderLayout());
		center.add(topPanel, BorderLayout.NORTH);

		// Add SVG Image loading for the building
		GraphicsNode svg = SVGMapUtil.getVehicleSVG(v.getVehicleType().getName());
		SVGGraphicNodeIcon svgIcon = new SVGGraphicNodeIcon(svg, 128, 64, true);
		JLabel svgLabel = new JLabel(svgIcon);
		JPanel svgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		svgPanel.add(svgLabel);
		topPanel.add(svgPanel, BorderLayout.NORTH);
		
		// Prepare spring layout info panel.
		JPanel infoPanel = new JPanel(new GridLayout(5, 2, 3, 1));
		topPanel.add(infoPanel, BorderLayout.CENTER);

		addTextField(infoPanel, "Type:", v.getVehicleType().getName(), null);
		addTextField(infoPanel, "Cargo Capacity:", StyleManager.DECIMAL_KG.format(v.getCargoCapacity()), null);
		addTextField(infoPanel, "Fuel:", ResourceUtil.findAmountResourceName(v.getFuelType()), null);
		addTextField(infoPanel, "Fuel Capacity:", StyleManager.DECIMAL_KG.format(v.getFuelCapacity()), null);

		// Prepare mass label
		addTextField(infoPanel, "Base Mass:", v.getBaseMass() + " kg", "The base mass of this vehicle");
	}
}