/*
 * Mars Simulation Project
 * MapDataUtil.java
 * @date 2022-08-02
 * @author Scott Davis
 */

 package org.mars_sim.mapdata;

/**
  * Static utility class for accessing Mars map data.
  */
 public final class MapDataUtil {
 	
	/** Note: Make sure GLOBE_BOX_HEIGHT matches the number of vertical pixels of the globe surface map. */ 
 	public static final int GLOBE_BOX_HEIGHT = 300;
 	public static final int GLOBE_BOX_WIDTH = GLOBE_BOX_HEIGHT;
 	public static final int MAP_BOX_HEIGHT = GLOBE_BOX_HEIGHT;
 	public static final int MAP_BOX_WIDTH = GLOBE_BOX_WIDTH;
 	
	/** Note: Make sure MAP_HEIGHT matches the number of vertical pixels of the surface source map. */ 
	public static final int MAP_HEIGHT = 1440; //1024; 1440; 2048; 2880 
	/** Note: Make sure MAP_HEIGHT matches the number of horizontal pixels of the surface source map. */ 
	public static final int MAP_WIDTH = 2880; //2048; 2880; 4096; 5760
	
 	public static final double RATIO = GLOBE_BOX_HEIGHT / 300 * MAP_WIDTH / 2880;
 	
 	private static final int ELEVATION_MAP_HEIGHT = MEGDRMapReader.HEIGHT;
 	private static final int ELEVATION_MAP_WIDTH = MEGDRMapReader.WIDTH;
 	
 	private static final double PI = Math.PI;
 	private static final double TWO_PI = Math.PI * 2D;

     // Singleton instance.
     private static MapDataUtil instance;
     private static MapDataFactory mapDataFactory;
     private static MEGDRMapReader reader;

 	private static int[] elevationArray;
 	
     /**
      * Private constructor for static utility class.
      */
     private MapDataUtil() {
         mapDataFactory = new MapDataFactory();
         reader = new MEGDRMapReader();
     }
     
     public int[] getElevationArray() {
     	if (elevationArray == null)	
     		elevationArray = reader.loadElevation();
  
 		return elevationArray;
 	}
 	
     /**
 	 * Gets the elevation as an integer at a given location.
 	 * 
 	 * @param phi   the phi location.
 	 * @param theta the theta location.
 	 * @return the elevation as an integer.
 	 */
 	public int getElevationInt(double phi, double theta) {
// 		// Make sure phi is between 0 and PI.
// 		while (phi > PI)
// 			phi -= PI;
// 		while (phi < 0)
// 			phi += PI;
//
// 		// Adjust theta with PI for the map offset.
// 		// Note: the center of the map is when theta = 0
// 		if (theta > PI)
// 			theta -= PI;
// 		else
// 			theta += PI;
// 		
// 		// Make sure theta is between 0 and 2 PI.
// 		while (theta > TWO_PI)
// 			theta -= TWO_PI;
// 		while (theta < 0)
// 			theta += TWO_PI;

 		int row = (int) Math.round(phi * ELEVATION_MAP_HEIGHT / PI);
 		
 		if (row == ELEVATION_MAP_HEIGHT) 
 			row--;
 		
 		int column = ELEVATION_MAP_WIDTH /2 + (int) Math.round(theta * ELEVATION_MAP_WIDTH / TWO_PI);

 		if (column == ELEVATION_MAP_WIDTH)
 			column--;

 		int index = row * ELEVATION_MAP_WIDTH + column;
 		
 		if (index > ELEVATION_MAP_HEIGHT * ELEVATION_MAP_WIDTH)
 			index = ELEVATION_MAP_HEIGHT * ELEVATION_MAP_WIDTH - 1;
 		
 		return getElevationArray()[index];
 	}
 	
     /**
      * Gets the singleton instance of MapData.
      * 
      * @return instance.
      */
     public static MapDataUtil instance() {
         if (instance == null) {
             instance = new MapDataUtil();
         }
         return instance;
     }
     
     /**
      * Gets the surface map data.
      * 
      * @return surface map data.
      */
     public MapData getSurfaceMapData() {
         return mapDataFactory.getMapData(MapDataFactory.SURFACE_MAP_DATA);
     }
     
     /**
      * Gets the topographical map data.
      * 
      * @return topographical map data.
      */
     public MapData getTopoMapData() {
         return mapDataFactory.getMapData(MapDataFactory.TOPO_MAP_DATA);
     }
     
     /**
      * Gets the geology map data.
      * 
      * @return geology map data.
      */
     public MapData getGeologyMapData() {
         return mapDataFactory.getMapData(MapDataFactory.GEOLOGY_MAP_DATA);
     }
     
 }
