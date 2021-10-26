/**
 * Mars Simulation Project
 * MarsSurface.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.environment;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * MarsSurface is the object unit that represents the surface of Mars
 */
public class MarsSurface extends Unit implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 123L;
	
	private static final String NAME = "Mars Surface";
	
	private Set<Person> personList = new HashSet<>();

	private Set<Robot> robotList = new HashSet<>();
	
	private Set<Vehicle> vehicleList = new HashSet<>();

	public MarsSurface() {
		super(NAME, null);
		
		setContainerUnit(null);
		
		setContainerID(Unit.OUTER_SPACE_UNIT_ID);

		// This is hack playing on how the identifiers are created
		if (getIdentifier() != Unit.MARS_SURFACE_UNIT_ID) {
			throw new IllegalStateException("MarsSurface has wrong ID: " + getIdentifier());
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public UnitType getUnitType() {
		return UnitType.PLANET;
	}
	
	@Override
	public Settlement getSettlement() {
		return null;
	}
	
	/**
	 * Adds a person
	 * 
	 * @param person
	 * @param true if the person can be added
	 */
	public boolean addPerson(Person person) {
		synchronized (personList) {
			return personList.add(person);
		}
	}
	
	/**
	 * Removes a person
	 * 
	 * @param person
	 * @param true if the person can be removed
	 */
	public boolean removePerson(Person person) {
		synchronized (personList) {
			return personList.remove(person);
		}
	}
	
	/**
	 * Adds a robot
	 * 
	 * @param robot
	 * @param true if the robot can be added
	 */
	public boolean addRobot(Robot robot) {
		synchronized (robotList) {
			return robotList.add(robot);
		}
	}
	
	/**
	 * Removes a robot
	 * 
	 * @param robot
	 * @param true if the robot can be removed
	 */
	public boolean removeRobot(Robot robot) {
		synchronized (robotList) {
			return robotList.remove(robot);
		}
	}
	
	/**
	 * Adds a vehicle
	 * 
	 * @param vehicle
	 * @param true if the vehicle can be added
	 */
	public boolean addVehicle(Vehicle vehicle) {
		synchronized (vehicleList) {
			return vehicleList.add(vehicle);
		}
	}
	
	/**
	 * Removes a vehicle
	 * 
	 * @param vehicle
	 * @param true if the vehicle can be removed
	 */
	public boolean removeVehicle(Vehicle vehicle) {
		synchronized (vehicleList) {
			return vehicleList.remove(vehicle);
		}
	}
	
	/**
	 * Gets the unit's container unit. Returns null if unit has no container unit.
	 * 
	 * @return the unit's container unit
	 */
	@Override
	public Unit getContainerUnit() {
		if (unitManager == null) // for maven test
			return null;
		// Note: there is no outer space unit
		return this;
	}
	
	/**
	 * Gets the hash code for this object.
	 * 
	 * @return hash code.
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
