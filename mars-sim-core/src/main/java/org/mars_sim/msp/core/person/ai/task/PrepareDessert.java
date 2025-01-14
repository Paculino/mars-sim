/*
 * Mars Simulation Project
 * PrepareDessert.java
 * @date 2021-08-29
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The PrepareDessert class is a task for making dessert
 */
public class PrepareDessert extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(PrepareDessert.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.prepareDessert"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase PREPARING_DESSERT = new TaskPhase(Msg.getString("Task.phase.prepareDessert")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.2D;

	// Data members
	/** The kitchen the person is making dessert. */
	private PreparingDessert kitchen;

	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 * @throws Exception if error constructing task.
	 */
	public PrepareDessert(Person person) {
		// Use Task constructor
		super(NAME, person, true, false, STRESS_MODIFIER, SkillType.COOKING, 25D);

		// Get an available dessert preparing kitchen.
		Building kitchenBuilding = getAvailableKitchen(person);
		if (kitchenBuilding != null) {
			initDessert(kitchenBuilding);
		}
		else {
			// No dessert preparing kitchen available.
			endTask();
		}
	}
	
	private void initDessert(Building kitchenBuilding) {

		kitchen = kitchenBuilding.getPreparingDessert();
		// Walk to kitchen building.
		walkToTaskSpecificActivitySpotInBuilding(kitchenBuilding, FunctionType.PREPARING_DESSERT, false);

		boolean isAvailable = kitchen.getListDessertsToMake().size() > 0;
		// Check if enough desserts have been prepared at the kitchen for this meal
		// time.
		boolean enoughDessert = kitchen.getMakeNoMoreDessert();

		if (isAvailable && !enoughDessert) {
			// Set the chef name at the kitchen.
			// kitchen.setChef(person.getName());

			// Add task phase
			addPhase(PREPARING_DESSERT);
			setPhase(PREPARING_DESSERT);
			// String jobName = person.getMind().getJob().getName(person.getGender());

			// String newLog = jobName + " " + person.getName() + " prepared desserts in " +
			// kitchen.getBuilding().getNickName() +
			// " at " + person.getSettlement();
			// LogConsolidated.log(logger, Level.INFO, 5000, sourceName, newLog, null);
		} else {
			// No dessert available or enough desserts have been prepared for now.
			endTask();
		}

	}

	public PrepareDessert(Robot robot) {
		// Use Task constructor
		super(NAME, robot, true, false, STRESS_MODIFIER, SkillType.COOKING, 25D);

		// Get available kitchen if any.
		Building kitchenBuilding = getAvailableKitchen(robot);

		if (kitchenBuilding != null) {
			initDessert(kitchenBuilding);
		} else
			endTask();
	}

	/**
	 * Performs the method mapped to the task's current phase.
	 * 
	 * @param time the amount of time the phase is to be performed.
	 * @return the remaining time after the phase has been performed.
	 */
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("The Preparing Desert task phase is null");
		} else if (PREPARING_DESSERT.equals(getPhase())) {
			return preparingDessertPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the dessert making phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) left after performing the phase.
	 */
	private double preparingDessertPhase(double time) {
		// If kitchen has malfunction, end task.
		if (kitchen.getBuilding().getMalfunctionManager().hasMalfunction()) {
			endTask();
			return time;
		}

		// Add this work to the kitchen.
		String nameOfDessert = null;
		double workTime = time;

		// If meal time is over, end task.
		if (!CookMeal.isLocalMealTime(worker.getCoordinates(), 10)) {
			logger.log(worker, Level.FINE, 0, "ended preparing desserts : meal time was over.");
			endTask();
			return time;
		}

		// If enough desserts have been prepared for this meal time, end task.
		if (kitchen.getMakeNoMoreDessert()) {
			logger.log(worker, Level.FINE, 0, "ended preparing desserts : enough desserts prepared.");
			endTask();
			return time;
		}

		if (worker instanceof Robot) {
			// A robot moves slower than a person and incurs penalty on workTime
			workTime = time / 2;
		}
		
		// Add this work to the kitchen.
		nameOfDessert = kitchen.addWork(workTime, worker);

		// Determine amount of effective work time based on Cooking skill.
		int dessertMakingSkill = getEffectiveSkillLevel();
		if (dessertMakingSkill == 0) {
			workTime /= 2;
		}

		else {
			workTime += workTime * (.2D * (double) dessertMakingSkill);
		}

		if (nameOfDessert != null)
			setDescription(Msg.getString("Task.description.prepareDessert.detail.finish", nameOfDessert)); // $NON-NLS-1$
		else {
			endTask();
			return time;
		}

		// Add experience
		addExperience(time);

		// Check for accident in kitchen.
		checkForAccident(kitchen.getBuilding(), time, 0.005D);

		return 0D;
	}

	/**
	 * Gets the kitchen the person is making desserts.
	 * 
	 * @return kitchen
	 */
	public PreparingDessert getKitchen() {
		return kitchen;
	}

	/**
	 * Gets an available kitchen building at the person's settlement.
	 * 
	 * @param person the person to check for.
	 * @return kitchen building or null if none available.
	 */
	public static Building getAvailableKitchen(Person person) {
		Building result = null;

		if (person.isInSettlement()) {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			List<Building> kitchenBuildings = manager.getBuildings(FunctionType.PREPARING_DESSERT);
			kitchenBuildings = BuildingManager.getNonMalfunctioningBuildings(kitchenBuildings);
			kitchenBuildings = getKitchensNeedingCooks(kitchenBuildings);
			kitchenBuildings = BuildingManager.getLeastCrowdedBuildings(kitchenBuildings);

			if (kitchenBuildings.size() > 0) {

				Map<Building, Double> kitchenBuildingProbs = BuildingManager.getBestRelationshipBuildings(person,
						kitchenBuildings);

				result = RandomUtil.getWeightedRandomObject(kitchenBuildingProbs);
			}
		}

		return result;
	}

	public static Building getAvailableKitchen(Robot robot) {
		Building result = null;

		if (robot.isInSettlement()) {
			BuildingManager manager = robot.getSettlement().getBuildingManager();
			List<Building> kitchenBuildings = manager.getBuildings(FunctionType.PREPARING_DESSERT);
			kitchenBuildings = BuildingManager.getNonMalfunctioningBuildings(kitchenBuildings);
			kitchenBuildings = getKitchensNeedingCooks(kitchenBuildings);
			if (RandomUtil.getRandomInt(2) == 0) // robot is not as inclined to move around
				kitchenBuildings = BuildingManager.getLeastCrowded4BotBuildings(kitchenBuildings);

			if (kitchenBuildings.size() > 0) {
				int selected = RandomUtil.getRandomInt(kitchenBuildings.size() - 1);
				result = kitchenBuildings.get(selected);
			}
		}

		return result;
	}

	/**
	 * Gets a list of kitchen buildings that have room for more cooks.
	 * 
	 * @param kitchenBuildings list of kitchen buildings
	 * @return list of kitchen buildings
	 * @throws BuildingException if error
	 */
	private static List<Building> getKitchensNeedingCooks(List<Building> kitchenBuildings) {
		List<Building> result = new ArrayList<Building>();

		if (kitchenBuildings != null) {
			Iterator<Building> i = kitchenBuildings.iterator();
			while (i.hasNext()) {
				Building building = i.next();
				PreparingDessert kitchen = building.getPreparingDessert();
				if (kitchen.getNumCooks() < kitchen.getCookCapacity()) {
					result.add(building);
				}
			}
		}

		return result;
	}
}
