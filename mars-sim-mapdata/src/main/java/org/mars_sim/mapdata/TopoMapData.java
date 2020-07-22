 /**
  * Mars Simulation Project
  * TopoMapData.java
  * @version 3.1.1 2020-07-22
  * @author Scott Davis
  */

 package org.mars_sim.mapdata;

 /**
  * Topographical map data.
  */
 public class TopoMapData extends IntegerMapData {

     // Static members.
     private static final String MAP_FILE = "/maps/topo2880x1440.jpg";
     
     /**
      * Constructor
      */
     public TopoMapData() {
         super(MAP_FILE);
     }
 }
