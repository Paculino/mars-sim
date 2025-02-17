/*
 * Mars Simulation Project
 * SurfMarsMap.java
 * @date 2022-08-02
 * @author Scott Davis
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.swing.tool.map;

import javax.swing.JComponent;

import org.mars_sim.mapdata.MapDataUtil;

/**
 * The SurfMarsMap class is a map of the surface of Mars that can be generated
 * for the map display. Map data is retrieved from a data file and stored in
 * memory.
 */
@SuppressWarnings("serial")
public class SurfMarsMap extends CannedMarsMap {

	// The map type.
	public static final String TYPE = "surface map";

	private static MapDataUtil mapDataUtil = MapDataUtil.instance();
	
	/**
	 * Constructor
	 *
	 * @param displayArea the component display area.
	 */
	public SurfMarsMap(JComponent displayArea) {

		// Parent constructor
		super(displayArea, mapDataUtil.getSurfaceMapData());
	}
}
