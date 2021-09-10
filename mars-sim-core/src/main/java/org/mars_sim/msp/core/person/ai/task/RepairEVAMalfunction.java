/*
 * Mars Simulation Project
 * RepairEVAMalfunction.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.environment.MarsSurface;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.MalfunctionRepairWork;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;

/**
 * The RepairEVAMalfunction class is a task to repair a malfunction requiring an
 * EVA.
 */
public class RepairEVAMalfunction extends EVAOperation implements Repair, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(RepairEVAMalfunction.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.repairEVAMalfunction"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase REPAIRING = new TaskPhase(Msg.getString("Task.phase.repairing")); //$NON-NLS-1$

	// Data members
	/** The malfunctionable entity being repaired. */
	private Malfunctionable entity;

	/** The malfunction to be repaired. */
	private Malfunction malfunction;

	/** The container unit the person started the mission in. */
	private Unit containerUnit;

	public RepairEVAMalfunction(Person person) {
		super(NAME, person, true, 25, SkillType.MECHANICS);

		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        	return;
		}
		
		containerUnit = person.getTopContainerUnit();

		if (!(containerUnit instanceof MarsSurface)) {
			// Get the malfunctioning entity.
			entity = getEVAMalfunctionEntity(person);
			
			if (entity != null) {
				malfunction = getMalfunction(person, entity);
				
				if (malfunction != null) {
					setDescription(Msg.getString("Task.description.repairEVAMalfunction.detail", malfunction.getName(),
							entity.getNickName())); // $NON-NLS-1$
		
					// Determine location for repairing malfunction.
					Point2D malfunctionLoc = determineMalfunctionLocation();
					setOutsideSiteLocation(malfunctionLoc.getX(), malfunctionLoc.getY());
					
					if (!isDone()) {
			            if (person.isInside()) {
			            	setPhase(WALK_TO_OUTSIDE_SITE);
			            }
					}				
			
					String chief = malfunction.getChiefRepairer(MalfunctionRepairWork.EVA);
					String deputy = malfunction.getDeputyRepairer(MalfunctionRepairWork.EVA);

					if (chief == null || chief.equals("")) {
						logger.info(person, "Was appointed as the chief repairer handling the EVA for "
								+ malfunction.getName() + "' on "
								+ entity.getNickName() + ".");
						 malfunction.setChiefRepairer(MalfunctionRepairWork.EVA, person.getName());						
					}
					else if (deputy == null || deputy.equals("")) {
						logger.info(person, "Was appointed as the deputy repairer handling the EVA for " 
								+ malfunction.getName() + "' on "
								+ entity.getNickName() + ".");
						malfunction.setDeputyRepairer(MalfunctionRepairWork.EVA, person.getName());
					}
					
					// Record I will work on repair
					malfunction.addWorkTime(MalfunctionRepairWork.EVA, 0, person.getName());
					
					// Initialize phase
					addPhase(REPAIRING);
					
				}
				else {
		        	if (person.isOutside())
		        		setPhase(WALK_BACK_INSIDE);
		        	else
		        		endTask();
				}
				
			} 
			else {
	        	if (person.isOutside())
	        		setPhase(WALK_BACK_INSIDE);
	        	else
	        		endTask();
			}
		}
		else 
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
	}


	public static Malfunctionable getEVAMalfunctionEntity(Person person) {
		Malfunctionable result = null;

		Iterator<Malfunctionable> i = MalfunctionFactory.getLocalMalfunctionables(person).iterator();
		while (i.hasNext() && (result == null)) {
			Malfunctionable entity = i.next();
			if (getMalfunction(person, entity) != null) {
				result = entity;
			}
			MalfunctionManager manager = entity.getMalfunctionManager();

			// Check if entity has any EVA malfunctions.
			Iterator<Malfunction> j = manager.getAllEVAMalfunctions().iterator();
			while (j.hasNext() && (result == null)) {
				Malfunction malfunction = j.next();
				try {
					if (hasRepairPartsForMalfunction(person, person.getTopContainerUnit(),
							malfunction)) {
						result = entity;
					}
				} catch (Exception e) {
		          	logger.severe("Problems calling RepairEVAMalfunction's hasRepairPartsForMalfunction(): "+ e.getMessage());
				}
			}
		}

		return result;
	}
	
	/**
	 * Gets a reparable malfunction requiring an EVA for a given entity.
	 * 
	 * @param person the person to repair.
	 * @param entity the entity with a malfunction.
	 * @return malfunction requiring an EVA repair or null if none found.
	 */
	public static Malfunction getMalfunction(Person person, Malfunctionable entity) {

		Malfunction result = null;

		MalfunctionManager manager = entity.getMalfunctionManager();

		// Check if entity has any EVA malfunctions.
		Iterator<Malfunction> j = manager.getAllEVAMalfunctions().iterator();
		while (j.hasNext() && (result == null)) {
			Malfunction malfunction = j.next();
			try {
				if (RepairEVAMalfunction.hasRepairPartsForMalfunction(person, person.getTopContainerUnit(),
						malfunction)) {
					result = malfunction;
				}
			} catch (Exception e) {
	          	logger.log(Level.SEVERE, "Problems calling RepairEVAMalfunction's hasRepairPartsForMalfunction(): "+ e.getMessage());
			}
		}
		return result;
	}

	/**
	 * Checks if there are enough repair parts at person's location to fix the
	 * malfunction.
	 * 
	 * @param person        the person checking.
	 * @param containerUnit the unit the person is doing an EVA from.
	 * @param malfunction   the malfunction.
	 * @return true if enough repair parts to fix malfunction.
	 */
	public static boolean hasRepairPartsForMalfunction(Person person, Unit containerUnit, Malfunction malfunction) {

		if (person == null)
			throw new IllegalArgumentException("person is null");

		return hasRepairParts(containerUnit, malfunction);
	}

	/**
	 * Does a container have the parts required in stock ?
	 * @param containerUnit
	 * @param malfunction
	 * @return
	 */
	public static boolean hasRepairParts(Unit containerUnit, Malfunction malfunction) {

		boolean result = true;

		if (containerUnit == null)
			throw new IllegalArgumentException("containerUnit is null");

		if (malfunction == null)
			throw new IllegalArgumentException("malfunction is null");

		Inventory inv = containerUnit.getInventory();

		Map<Integer, Integer> repairParts = malfunction.getRepairParts();
		Iterator<Integer> i = repairParts.keySet().iterator();
		while (i.hasNext() && result) {
			Integer part = i.next();
			int number = repairParts.get(part);
			if (inv.getItemResourceNum(part) < number) {
				inv.addItemDemand(part, number);
				result = false;
			}
		}

		return result;
	}

	/**
	 * Determine location to repair malfunction.
	 * 
	 * @return location.
	 */
	private Point2D determineMalfunctionLocation() {

		Point2D.Double newLocation = new Point2D.Double(0D, 0D);

		if (entity instanceof LocalBoundedObject) {
			LocalBoundedObject bounds = (LocalBoundedObject) entity;
			boolean goodLocation = false;
			for (int x = 0; (x < 50) && !goodLocation; x++) {
				Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(bounds, 1D);
				newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), boundedLocalPoint.getY(),
						bounds);
				goodLocation = LocalAreaUtil.isLocationCollisionFree(newLocation.getX(), newLocation.getY(),
						worker.getCoordinates());
			}
		}

		return newLocation;
	}

	@Override
	protected TaskPhase getOutsideSitePhase() {
		return REPAIRING;
	}

	@Override
	protected double performMappedPhase(double time) {

		time = super.performMappedPhase(time);
		if (!isDone()) {
			if (getPhase() == null) {
				throw new IllegalArgumentException("Task phase is null");
			}
			else if (REPAIRING.equals(getPhase())) {
				time = repairMalfunctionPhase(time);
			}
		}
		return time;
	}

	/**
	 * Gets a malfunctional entity with a normal malfunction for a user.
	 * 
	 * @return malfunctional entity.
	 */
	private static boolean hasMalfunction(Person person, Malfunctionable entity) {
		boolean result = false;

		if (entity.getMalfunctionManager().hasMalfunction())
			return true;
		
		return result;
	}

	
	/**
	 * Perform the repair malfunction phase of the task.
	 * 
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 */
	private double repairMalfunctionPhase(double time) {
	
		if (isDone()) {
            if (person.isOutside())
            	setPhase(WALK_BACK_INSIDE);
            else //if (person.isInside())
        		endTask();
            return time;
		}
		
		// Check for radiation exposure during the EVA operation.
		if (isRadiationDetected(time)) {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
		}

		if (person != null) {
			// Check if there are no more malfunctions.
			if (!hasMalfunction(person, entity)) {
	            if (person.isOutside())
	            	setPhase(WALK_BACK_INSIDE);
	            else //if (person.isInside())
	        		endTask();
	            return time;
			}
		}
		
		if (shouldEndEVAOperation() || addTimeOnSite(time)) {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
		}

		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
		}
		
		double workTime = 0;

		if (person != null) {
			workTime = time;
		} else if (robot != null) {
			// A robot moves slower than a person and incurs penalty on workTime
			workTime = time / 2;
		}

		// Determine effective work time based on "Mechanic" skill.
		int mechanicSkill = worker.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);

		if (mechanicSkill == 0)
			workTime /= 2;
		if (mechanicSkill > 1)
			workTime += workTime * (.2D * mechanicSkill);
		
		if (person != null) {
			if (hasRepairPartsForMalfunction(person, containerUnit, malfunction)) {
				Map<Integer, Integer> parts = new HashMap<>(malfunction.getRepairParts());
				Iterator<Integer> j = parts.keySet().iterator();
				// Add repair parts if necessary.
				Inventory inv = containerUnit.getInventory();
				for( Entry<Integer, Integer> part : malfunction.getRepairParts().entrySet()) {
					Integer id = part.getKey();
					int number = part.getValue();
					inv.retrieveItemResources(id, number);
					malfunction.repairWithParts(id, number, inv);
				}
			} else {
	            if (person.isOutside())
	            	setPhase(WALK_BACK_INSIDE);
	            else //if (person.isInside())
	        		endTask();
	            return time;
			}

		}

		// Add EVA work to malfunction.
		double workTimeLeft = 0D;
		if (!malfunction.isWorkDone(MalfunctionRepairWork.EVA) ) {
			workTimeLeft = malfunction.addWorkTime(MalfunctionRepairWork.EVA, workTime, worker.getName());
		}
		
		// Add experience points
		addExperience(time);

		// Check if an accident happens during repair.
		checkForAccident(time);

		// Check if there are no more malfunctions.
		if (malfunction.isWorkDone(MalfunctionRepairWork.EVA)) {
			logger.info(person, "Wrapped up the EVA of " + malfunction.getName() 
					+ " in "+ entity + " ("
					+ Math.round(malfunction.getCompletedWorkTime(MalfunctionRepairWork.EVA)*10.0)/10.0 + " millisols spent).");
            if (worker.isOutside()) {
            	setPhase(WALK_BACK_INSIDE);
            	return workTimeLeft;
            }
            else if (person.isInside()) {
        		endTask();
            }
		}
			
		return workTimeLeft;
	}

	@Override
	public Malfunctionable getEntity() {
		return entity;
	}


	/**
	 * Worker leaves the malfunction effort
	 */
	@Override
	protected void clearDown() {
		// Leaving the repair effort
		if (malfunction != null) {
			malfunction.leaveWork(MalfunctionRepairWork.EVA, worker.getName());
		}
	}
}
