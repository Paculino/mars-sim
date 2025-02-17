/*
 * Mars Simulation Project
 * Computation.java
 * @date 2022-07-17
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.HashMap;
import java.util.Map;

import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.computing.ComputingTask;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.structure.building.SourceSpec;
import org.mars_sim.msp.core.time.ClockPulse;

/**
 * The Computation class is a building function for generating computational power.
 */
public class Computation extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Computation.class.getName());
	
	// Configuration properties
	private static final String COMPUTING_UNIT = "computing-unit";
	private static final String POWER_DEMAND = "power-demand";
	private static final String COOLING_DEMAND = "cooling-demand";
	
	/** The peak amount of computing resources [in CUs]. */
	private double peakCU;
	/** The amount of computing resources currently available [in CUs]. */
	private double computingUnit;
	/** The power load in kW for each running CU [in kW/CU]. */
	private double powerDemand;
	/** The power load in kW needed for cooling each running CU [in kW/CU]. */
	private double coolingDemand;
	/** The combined power demand for each running CU [in kW/CU]. */
	private double combinedkW;
	/** The combined power demand for each non-load CU [in kW/CU] - Assume 10% of full load. */
	private double NON_LOAD_KW = 0.1;
	/** The schedule demand [in CUs] for the current mission sol. */
	private Map<Integer, Double> todayDemand;

	private Building building;
	
	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 * @param spec Specification of the Computing Function
	 * @throws BuildingException if error in constructing function.
	 */
	public Computation(Building building, FunctionSpec spec) {
		// Call Function constructor.
		super(FunctionType.COMPUTATION, spec, building);
		
		this.building = building;
		
		peakCU = spec.getDoubleProperty(COMPUTING_UNIT);
		computingUnit = peakCU; 
		powerDemand = spec.getDoubleProperty(POWER_DEMAND);
		coolingDemand = spec.getDoubleProperty(COOLING_DEMAND);	
		
		combinedkW = coolingDemand + powerDemand;
		todayDemand = new HashMap<>();
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param type the building type.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String type, boolean newBuilding, Settlement settlement) {

		double demand = settlement.getPowerGrid().getRequiredPower();

		double supply = 0D;
		boolean removedBuilding = false;
		for (Building building : settlement.getBuildingManager().getBuildings(FunctionType.COMPUTATION)) {
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(type) && !removedBuilding) {
				removedBuilding = true;
			} else {
				Computation com = building.getComputation();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += com.getComputingUnit() * wearModifier;
			}
		}

		double existingPowerValue = demand / (supply + 1D);

		double powerSupply = buildingConfig.getHeatSources(type).stream()
								.mapToDouble(SourceSpec::getCapacity).sum();

		return powerSupply * existingPowerValue;
	}

	@Override
	public String[] getMalfunctionScopeStrings() {
		return new String[] {"Computation"};
	}

	/**
	 * Gets the computing units [in CU].
	 * 
	 * @return
	 */
	public double getComputingUnit() {
		return computingUnit;
	}

	/**
	 * Gets the peak computing units [in CU].
	 * 
	 * @return
	 */
	public double getPeakComputingUnit() {
		return peakCU;
	}
	
	
	/**
	 * Gets the power demand [in kW].
	 * 
	 * @return
	 */
	public double getPowerDemand() {
		return powerDemand;
	}

	/**
	 * Gets the cooling demand [in kW].
	 * 
	 * @return
	 */
	public double getCoolingDemand() {;
		return coolingDemand;
	}
	
	
	/**
	 * Schedules for a computing tasks.
	 * 
	 * @param computingTask
	 * @return
	 */
	public boolean scheduleTask(ComputingTask computingTask) {
		double demand = computingTask.getComputingPower();
		int beginningMSol = computingTask.getStartTime();
		int endMSol = beginningMSol + computingTask.getDuration();
		return scheduleTask(demand, beginningMSol, endMSol);
	}
	
	/**
	 * Schedules for a computing task.
	 * 
	 * @param needed the CUs needed per msol
	 * @param beginningMSol the start msol
	 * @param endMSol the end msol
	 * @return
	 */
	public boolean scheduleTask(double needed, int beginningMSol, int endMSol) {
		int duration = endMSol - beginningMSol;
		if (duration < 0)
			duration = endMSol + 1000 - beginningMSol;
		double existing = 0;
		// Test to see if the assigned duration has enough resources
		for (int i = 0; i < duration; i++) {
			int sol = i + beginningMSol;
			if (sol > 999) {
				sol = sol - 1000;
			}
			if (todayDemand.containsKey(sol)) {
				existing = todayDemand.get(sol);
			}
			double available = peakCU - existing - needed;
			if (available < 0) {
				logger.info(getBuilding(), 30_000L, "scheduleTask::available: " + available);
				// Need to make sure each msol has enough resources
				return false;
			}
		}

		// Now the actual scheduling
		for (int i = 0; i < duration; i++) {
			int sol = i + beginningMSol;
			if (sol > 999) {
				sol = sol - 1000;
			}
			if (todayDemand.containsKey(sol)) {
				existing = todayDemand.get(sol);
			}
			todayDemand.put(sol, existing + needed);
		}

		return true;
	}
	
	/**
	 * Does this computing center have the resources to schedule for a computing task ?
	 * 
	 * @param needed
	 * @param beginningMSol
	 * @param endMSol
	 * @return
	 */
	public boolean canScheduleTask(double needed, int beginningMSol, int endMSol) {
		int duration = endMSol - beginningMSol;
		if (duration < 0)
			duration = endMSol + 1000 - beginningMSol;
		double existing = 0;
		double available = 0;
		// Test to see if the assigned duration has enough resources
		for (int i = 0; i < duration; i++) {
			int sol = i + beginningMSol;
			if (sol > 999) {
				sol = sol - 1000;
			}
			if (todayDemand.containsKey(sol)) {
				existing = todayDemand.get(sol);
			}
			available = peakCU - existing - needed;
		}
		if (available < 0) {
			logger.info(getBuilding(), 30_000L, "canScheduleTask::available: " + available);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Returns the evaluation score if scheduling for a computing task for a prescribed period of time. 
	 * 
	 * @param needed CU(s) per millisol
	 * @param beginningMSol
	 * @param endMSol
	 * @return
	 */
	public double evaluateScheduleTask(double needed, int beginningMSol, int endMSol) {
		int duration = endMSol - beginningMSol;
		if (duration < 0)
			duration = endMSol + 1000 - beginningMSol;
		double score = 0;
		double existing = 0;
		// Test to see if the assigned duration has enough resources
		for (int i = 0; i < duration; i++) {
			int sol = i + beginningMSol;
			if (sol > 999) {
				sol = sol - 1000;
			}
			if (todayDemand.containsKey(sol)) {
				existing = todayDemand.get(sol);
			}
			double available = peakCU - existing - needed;
			if (available < 0) {
				logger.info(getBuilding(), 30_000L, "evaluateScheduleTask::available: " + available);
				return 0;
			}
			
			score += available;
		}
		return score;
	}
	
	/**
	 * Sets the computing resource to a new value and fires the unit event type alert.
	 * 
	 * @param value
	 */
	public void setComputingResource(double value) {
		double cu = Math.round(value * 100_000.0) / 100_000.0;
		if (computingUnit != cu) {
			computingUnit = cu;
			building.getSettlement().fireUnitUpdate(UnitEventType.CONSUMING_COMPUTING_EVENT);
		}
	}

	/**
	 * Time passing for the building.
	 * 
	 * @param deltaTime amount of time passing (in millisols)
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
	
			int msol = pulse.getMarsTime().getMillisolInt();
			double newDemand = 0;
			
			// Delete past demand on previous sol
			if (msol - 1 > 0 && todayDemand.containsKey(msol - 1)) {
				todayDemand.remove(msol - 1);
			}
			// Delete past demand on the sol before yestersol 
			if (msol - 2 > 0 && todayDemand.containsKey(msol - 2)) {
				todayDemand.remove(msol - 2);
			}
			
			if (todayDemand.containsKey(msol)) {
				newDemand = todayDemand.get(msol);
			}
			if (newDemand > 0) {
				// Updates the CUs
				setComputingResource(peakCU - newDemand); 
			}
			else {
				setComputingResource(peakCU);
			}

			// Notes: 
			// if it falls below 10%, flash yellow
			// if it falls below 0%, flash red
		}
		return valid;
	}
	
	/**
	 * Returns the percent of usage of computing resources.
	 * 
	 * @return
	 */
	public double getUsagePercent() {
		return (peakCU - computingUnit)/peakCU * 100.0;
	}
	
	/**
	 * Gets the amount of power required, based on the current load.
	 *
	 * @return power (kW) default zero
	 */
	@Override
	public double getFullPowerRequired() {
		double load = peakCU - computingUnit;
		double nonLoad = computingUnit;
		return (load + NON_LOAD_KW * nonLoad) * combinedkW;
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}

}
