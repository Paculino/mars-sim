/**
 * Mars Simulation Project
 * VirtualMars.java
 * @version 2.73 2001-11-29
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import org.mars_sim.msp.simulation.task.MissionManager;
import java.util.*;

/** VirtualMars represents Mars in the simulation. It contains all the
 *  units, a master clock, and access to the topography data.
 */
public class VirtualMars {

    // Data members
    private SimulationProperties properties; // The user-defined simulation properties
    private OrbitInfo orbitInfo; // Orbital information
    private SurfaceFeatures surfaceFeatures; // Surface features
    private UnitManager units; // Unit controller
    private MissionManager missionManager; // Mission controller
    private MasterClock masterClock; // Master clock for virtual world

    /** Constructs a VirtualMars object */
    public VirtualMars() {

        // Initialize simulation properties
        properties = new SimulationProperties();

        // Initialize orbit info
        orbitInfo = new OrbitInfo();
 
        // Initialize surface features
        surfaceFeatures = new SurfaceFeatures(this);

        // Initialize mission manager
        missionManager = new MissionManager(this);

        // Initialize all units
        units = new UnitManager(this);

        // Initialize and start master clock
        masterClock = new MasterClock(this);
        masterClock.start();
    }

    /** Clock pulse from master clock 
     *  @param time amount of time passing (in millisols)
     */
    void clockPulse(double time) {
        orbitInfo.addTime(MarsClock.convertMillisolsToSeconds(time));
        units.timePassing(time);
    }

    /** Returns the simulation properties
     *  @return simulation properties
     */
    public SimulationProperties getSimulationProperties() {
        return properties;
    }

    /** Returns the orbital information
     *  @return orbital information
     */
    public OrbitInfo getOrbitInfo() {
        return orbitInfo;
    }

    /** Returns surface features
     *  @return surfaces features
     */
    public SurfaceFeatures getSurfaceFeatures() {
        return surfaceFeatures;
    }

    /** Returns the unit manager 
     *  @return unit manager for virtual Mars
     */
    public UnitManager getUnitManager() {
        return units;
    }

    /** Returns the mission manager
     *  @return mission manager for virtual Mars
     */
    public MissionManager getMissionManager() {
        return missionManager;
    }

    /** Returns the master clock
     *  @return master clock instance
     */
    public MasterClock getMasterClock() {
        return masterClock;
    }
}

