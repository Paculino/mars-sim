/**
 * Mars Simulation Project
 * Administration.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * An administration building function. The building facilitates report writing
 * and other administrative paperwork.
 */
public class Administration extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(Administration.class.getName());
	
	// Data members
	private int populationSupport;
	private int staff;
	private int staffCapacity;


	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 */
	public Administration(Building building) {
		// Use Function constructor.
		super(FunctionType.ADMINISTRATION, building);

		String buildingType = building.getBuildingType();

		populationSupport = buildingConfig.getAdministrationPopulationSupport(buildingType);

		staffCapacity = buildingConfig.getFunctionCapacity(buildingType, FunctionType.ADMINISTRATION);
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param buildingName the building name.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding, Settlement settlement) {

		// Settlements need enough administration buildings to support population.
		double demand = settlement.getNumCitizens();

		// Supply based on wear condition of buildings.
		double supply = 0D;
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.ADMINISTRATION).iterator();
		while (i.hasNext()) {
			Building adminBuilding = i.next();
			Administration admin = adminBuilding.getAdministration();// adminBuilding.getFunction(FUNCTION);
			double populationSupport = admin.getPopulationSupport();
			double wearFactor = ((adminBuilding.getMalfunctionManager().getWearCondition() / 100D) * .75D) + .25D;
			supply += populationSupport * wearFactor;
		}

		if (!newBuilding) {
			supply -= buildingConfig.getFunctionCapacity(buildingName, FunctionType.ADMINISTRATION);
			if (supply < 0D)
				supply = 0D;
		}

		return demand / (supply + 1D);
	}

	/**
	 * Gets an available building with the administration function.
	 * 
	 * @param person the person looking for the office.
	 * @return an available office space or null if none found.
	 */
	public static Building getAvailableOffice(Person person) {
		Building result = null;
		
		// If person is in a settlement, try to find a building with )an office.
		if (person.isInSettlement()) {
			BuildingManager buildingManager = person.getSettlement().getBuildingManager();
			List<Building> offices = buildingManager.getBuildings(FunctionType.ADMINISTRATION);
			offices = BuildingManager.getNonMalfunctioningBuildings(offices);
			
			List<Building> comfortOffices = BuildingManager.getLeastCrowdedBuildings(offices);

			if (!comfortOffices.isEmpty()) {				
				offices = comfortOffices;			
			}
			
			// skip filtering the crowded offices
			Map<Building, Double> selectedOffices = BuildingManager.getBestRelationshipBuildings(person, offices);
			result = RandomUtil.getWeightedRandomObject(selectedOffices);
		}

		return result;
	}

	/**
	 * Gets the number of people this administration facility can support.
	 * 
	 * @return population that can be supported.
	 */
	public int getPopulationSupport() {
		return populationSupport;
	}

	/**
	 * Gets the number of people this administration facility can be used all at a
	 * time.
	 * 
	 * @return population that can be supported.
	 */
	public int getStaffCapacity() {
		return staffCapacity;
	}

	/**
	 * Gets the current number of people using the office space.
	 * 
	 * @return number of people.
	 */
	public int getNumStaff() {
		return staff;
	}

	public boolean isFull() {
		if (staff >= staffCapacity)
			return true;
		else
			return false;
	}

	/**
	 * Adds a person to the office space.
	 * 
	 * @throws BuildingException if person would exceed office space capacity.
	 */
	public void addStaff() {
		if (staff >= staffCapacity) {
			logger.log(building, Level.INFO, 10_000, "The office space is full.");
		}
		else
			staff++;
	}

	/**
	 * Removes a person from the office space.
	 * 
	 * @throws BuildingException if nobody is using the office space.
	 */
	public void removeStaff() {
		staff--;
		if (staff < 0) {
			staff = 0;
			logger.log(building, Level.SEVERE, 10_000, "Miscalculating the office space occupancy");
		}
	}

	@Override
	public double getMaintenanceTime() {
		return populationSupport * 1D;
	}
}
