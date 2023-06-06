/*
 * Mars Simulation Project
 * OuterSpace.java
 * @date 2023-06-05
 * @author Manny Kung
 */

package org.mars_sim.msp.core.environment;

import java.util.Set;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.data.UnitSet;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * OuterSpace is the object unit that represents the outer space
 */
public class OuterSpace extends Unit {

	/** default serial id. */
	private static final long serialVersionUID = 123L;

	private static final String NAME = "Outer Space";

	private Set<Unit> unitList;

	public OuterSpace() {
		super(NAME, Unit.OUTER_SPACE_UNIT_ID, Unit.OUTER_SPACE_UNIT_ID);

		unitList = new UnitSet<>();
		
		// Set currentStateType to OUTER_SPACE
		currentStateType = LocationStateType.OUTER_SPACE;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public UnitType getUnitType() {
		return UnitType.OUTER_SPACE;
	}

	public Settlement getSettlement() {
		return null;
	}

	/**
	 * Adds a person.
	 *
	 * @param person
	 * @param true if the person can be added
	 */
	public boolean addPerson(Person person) {
		synchronized (unitList) {
			return unitList.add(person);
		}
	}

	/**
	 * Removes a person.
	 *
	 * @param person
	 * @param true if the person can be removed
	 */
	public boolean removePerson(Person person) {
		synchronized (unitList) {
			return unitList.remove(person);
		}
	}

	/**
	 * Adds a robot.
	 *
	 * @param robot
	 * @param true if the robot can be added
	 */
	public boolean addRobot(Robot robot) {
		synchronized (unitList) {
			return unitList.add(robot);
		}
	}

	/**
	 * Removes a robot.
	 *
	 * @param robot
	 * @param true if the robot can be removed
	 */
	public boolean removeRobot(Robot robot) {
		synchronized (unitList) {
			return unitList.remove(robot);
		}
	}

	/**
	 * Adds a vehicle.
	 *
	 * @param vehicle
	 * @param true if the vehicle can be added
	 */
	public boolean addVehicle(Vehicle vehicle) {
		synchronized (unitList) {
			// There is a bug somewhere because Drones in delivery remains on the Surface
			if (unitList.contains(vehicle)) {
				return true;
			}
			return unitList.add(vehicle);
		}
	}

	/**
	 * Removes a vehicle.
	 *
	 * @param vehicle
	 * @param true if the vehicle can be removed
	 */
	public boolean removeVehicle(Vehicle vehicle) {
		synchronized (unitList) {
			return unitList.remove(vehicle);
		}
	}

	/**
	 * Gets the unit's container unit.
	 *
	 * @return the unit's container unit
	 */
	@Override
	public Unit getContainerUnit() {
		if (unitManager == null) // for maven test
			return null;
		// Should it return itself or return null ?
		return null;
	}

	/**
	 * Is this unit inside a settlement ?
	 *
	 * @return true if the unit is inside a settlement
	 */
	@Override
	public boolean isInSettlement() {
		return false;
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
