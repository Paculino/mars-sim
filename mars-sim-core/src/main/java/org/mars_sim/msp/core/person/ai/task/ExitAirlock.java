/*
 * Mars Simulation Project
 * ExitAirlock.java
 * @date 2021-10-03
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.InventoryUtil;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.AirlockType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The ExitAirlock class is a Task for EVA egress, namely, exiting an airlock of a settlement or vehicle
 * in order to perform an EVA operation outside.
 */
public class ExitAirlock extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ExitAirlock.class.getName());


	/** Task name */
	private static final String NAME = Msg.getString("Task.description.exitAirlock"); //$NON-NLS-1$
	private static final String PREBREATH_HALF_DONE = "Can't Egress. Other occupant(s) have at least half of pre-breathed.";
	private static final String RESERVATION_NOT_MADE = "Reservation not made.";
	private static final String NOT_FIT = "Not fit";
	private static final String INNER_DOOR_LOCKED = "Inner door was locked.";
	private static final String CHAMBER_FULL = "Chamber was full.";
	
	private static final double MIN_PERFORMANCE = 0.05;

	/** Task phases. */
	private static final TaskPhase REQUEST_EGRESS = new TaskPhase(
		Msg.getString("Task.phase.requestEgress")); //$NON-NLS-1$
	private static final TaskPhase PRESSURIZE_CHAMBER = new TaskPhase(
		Msg.getString("Task.phase.pressurizeChamber")); //$NON-NLS-1$
	private static final TaskPhase ENTER_AIRLOCK = new TaskPhase(
		Msg.getString("Task.phase.enterAirlock")); //$NON-NLS-1$
	private static final TaskPhase WALK_TO_CHAMBER = new TaskPhase(
		Msg.getString("Task.phase.walkToChamber")); //$NON-NLS-1$
	private static final TaskPhase DON_EVA_SUIT = new TaskPhase(
		Msg.getString("Task.phase.donEVASuit")); //$NON-NLS-1$
	private static final TaskPhase PREBREATHE = new TaskPhase(
		Msg.getString("Task.phase.prebreathe")); //$NON-NLS-1$
	private static final TaskPhase DEPRESSURIZE_CHAMBER = new TaskPhase(
		Msg.getString("Task.phase.depressurizeChamber")); //$NON-NLS-1$
	private static final TaskPhase LEAVE_AIRLOCK = new TaskPhase(
		Msg.getString("Task.phase.leaveAirlock")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;
	/** The standard EVA suit donning time. */
	private static final double SUIT_DONNING_TIME = 25;

	// Data members
	/** Is this a building airlock in a settlement? */
	private boolean inSettlement;
	/** True if person has an EVA suit. */
	private boolean hasSuit = false;
	/** The remaining time in donning the EVA suit. */
	private double remainingDonningTime;

	/** The airlock to be used. */
	private Airlock airlock;
	/** The inside airlock position. */
	private LocalPosition insideAirlockPos = null;
	/** The exterior airlock position. */
	private LocalPosition exteriorDoorPos = null;
	/** The interior airlock position. */
	private LocalPosition interiorDoorPos = null;

	/**
	 * Constructor.
	 *
	 * @param person  the person to perform the task
	 * @param airlock the airlock to use.
	 */
	public ExitAirlock(Person person, Airlock airlock) {
		super(NAME, person, false, false, STRESS_MODIFIER, SkillType.EVA_OPERATIONS, 100);

		this.airlock = airlock;

		if (airlock.getAirlockType() == AirlockType.BUILDING_AIRLOCK) {
			inSettlement = true;
		}
		else
			inSettlement = false;

		// Initialize data members
		setDescription(Msg.getString("Task.description.exitAirlock.detail", airlock.getEntityName())); // $NON-NLS-1$
		// Initialize task phase
		addPhase(REQUEST_EGRESS);
		addPhase(PRESSURIZE_CHAMBER);
		addPhase(ENTER_AIRLOCK);
		addPhase(WALK_TO_CHAMBER);
		addPhase(DON_EVA_SUIT);
		addPhase(PREBREATHE);
		addPhase(DEPRESSURIZE_CHAMBER);
		addPhase(LEAVE_AIRLOCK);

		setPhase(REQUEST_EGRESS);

		logger.log((Unit)airlock.getEntity(), person, Level.FINER, 4_000,
				"Starting the EVA egress in " + airlock.getEntityName() + ".");
	}

	/**
	 * Performs the method mapped to the task's current phase.
	 *
	 * @param time the amount of time (millisols) the phase is to be performed.
	 * @return the remaining time (millisols) after the phase has been performed.
	 */
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");

		} else if (REQUEST_EGRESS.equals(getPhase())) {
			return requestEgress(time);
		} else if (PRESSURIZE_CHAMBER.equals(getPhase())) {
			return pressurizeChamber(time);
		} else if (ENTER_AIRLOCK.equals(getPhase())) {
			return enterAirlock(time);
		} else if (WALK_TO_CHAMBER.equals(getPhase())) {
			return walkToChamber(time);
		} else if (DON_EVA_SUIT.equals(getPhase())) {
			return donEVASuit(time);
		} else if (PREBREATHE.equals(getPhase())) {
			return prebreathe(time);
		} else if (DEPRESSURIZE_CHAMBER.equals(getPhase())) {
			return depressurizeChamber(time);
		} else if (LEAVE_AIRLOCK.equals(getPhase())) {
			return leaveAirlock(time);
		} else {
			return time;
		}
	}

	/**
	 * Transitions the person into a particular zone.
	 *
	 * @param zone the destination
	 * @return true if the transition is successful
	 */
	private boolean transitionTo(int zone) {
		
		// Is the person already in this zone ?
		if (isInZone(zone)) {
			return true;
		}
		
		// For egress, a person first arrives at zone 0.
		// Then he progresses via the inner/interior door onto zone 1.
		// At zone 1, he's waiting for an empty chamber to be available.
		// At zone 2, he's at the airlock chamber donning his EVA suit.
		// At zone 3, he's waiting for the outer/exterior door to open.
		// At zone 4, he just stepped outside onto the surface of Mars.
		
		// the previous zone #  a lower numeric #
		int previousZone = zone - 1;
		LocalPosition newPos = fetchNewPos(zone);
		if (newPos != null && airlock.occupy(zone, newPos, id)) {
			if (previousZone >= 0) {
				if (airlock.vacate(previousZone, id)) {
					moveThere(newPos, zone);
					return true;
				}
				else
					return false;
			}
			else {
				moveThere(newPos, zone);
				return true;
			}
		}
			
		return false;
	}


	/**
	 * Checks if the person is already in a particular zone.
	 *
	 * @param zone the zone the person is at
	 * @return true if the person is a particular zone
	 */
	private boolean isInZone(int zone) {
		return airlock.isInZone(person, zone);
	}

	/**
	 * Obtains a new position in the target zone.
	 *
	 * @param zone the destination zone
	 * @return LocalPosition a new location
	 */
	private LocalPosition fetchNewPos(int zone) {
		LocalPosition newPos = null;

		if (zone == 0) {
			newPos = airlock.getAvailableInteriorPosition(false);
		}
		else if (zone == 1) {
			newPos = airlock.getAvailableInteriorPosition(true);
		}
		else if (zone == 2) {
			newPos = airlock.getAvailableAirlockPosition();
		}
		else if (zone == 3) {
			newPos = airlock.getAvailableExteriorPosition(true);
		}
		else if (zone == 4) {
			newPos = airlock.getAvailableExteriorPosition(false);
		}

		return newPos;
	}

	/**
	 * Moves the person to a particular zone.
	 *
	 * @param newPos the target position in that zone
	 * @param zone the destination zone
	 */
	private void moveThere(LocalPosition newPos, int zone) {
		if (zone == 2) {
			walkToEVASpot((Building)airlock.getEntity());
		}

		else if (zone == 4) {
			// Note: Do NOT do obstacle checking because this movement crosses the
			// boundary of Zone 3 in EVA airlock and Zone 4 (which is outside) via
			// the outer door.
			addSubTask(
					new WalkOutside(person, person.getPosition(),
					airlock.getAvailableExteriorPosition(), true));
		}

		else {

			person.setPosition(newPos);
		}

		logger.log((Unit)airlock.getEntity(), person, Level.FINER, 4_000,
				"Arrived at ("
			+ newPos + ") in airlock zone " + zone + ".");
	}

	/**
	 * Checks if a person is tired, too stressful or hungry and need to take break, eat and/or sleep.
	 *
	 * @return true if a person is fit
	 */
	private boolean isFit() {
		// Note: if the person is in the airlock next to the observatory
		// he will always be qualified to leave that place, or else
		// he will get stranded
		if (person.isAdjacentBuilding(FunctionType.ASTRONOMICAL_OBSERVATION)) {
		 	return true;
		}
		
		if (airlock.getEntity() instanceof Building) {
			Building airlockBuilding = (Building)(airlock.getEntity());
			if (person.getSettlement().getBuildingManager().isObservatoryAttached(airlockBuilding)) {
				return true;
			}
		}
		
		// Checks if a person is tired, too stressful or hungry and need
		// to take break, eat and/or sleep
		return person.getPhysicalCondition().computeFitnessLevel() >= 2;
	}

	/**
	 * Walks to another location outside the airlock and ends the egress.
	 *
	 * @param person the person of interest
	 * @param reason the reason for walking away
	 */
	private void walkAway(Person person, String reason) {
		airlock.removeID(person.getIdentifier());

		// This doesn't make sense. The endTask call will be for the current task
		// and if 'person' is the same then the walk subTask will be immediately
		// cancelled by the endTask
//		person.getTaskManager().getTask().walkToRandomLocation(false);
//		endTask();

		logger.log((Unit)airlock.getEntity(), person, Level.WARNING, 4_000, reason);
		
		// Note: For person in a vehicle with high fatigue or hunger,
		// need to call clearAllTasks() to cause a person to quit the task
		// or else it gets stuck forever in the vehicle airlock
		person.getTaskManager().clearAllTasks(reason);
	}
	
	/**
	 * Is at least one occupants already half done with prebreathing ?
	 * 
	 * @return
	 */
	public boolean isOccupantHalfPrebreathed() {
	
		List<Integer> list = new ArrayList<>(airlock.getOccupants());
		for (int id : list) {
			Person p = airlock.getPersonByID(id);
			if (p.getSuit() != null
				&& p.getPhysicalCondition().isAtLeastHalfDonePrebreathing()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Requests the entry of the airlock.
	 *
	 * @param time the pulse
	 * @return the remaining time
	 */
	private double requestEgress(double time) {
		logger.log((Unit)airlock.getEntity(), person, Level.FINE, 4_000,
				"Requesting EVA egress in " + airlock.getEntity().toString() + ".");
		
		double remainingTime = 0;
		
		// If a person is in a vehicle, not needed of checking for reservation
		if (inSettlement && !airlock.addReservation(person.getIdentifier())) {
			walkAway(person, RESERVATION_NOT_MADE);
			return remainingTime;
		}

		if (inSettlement && !isFit()) {
			walkAway(person, NOT_FIT + " for egress.");
			return remainingTime;
		}

		if (person.isOutside()) {
			endTask();
		}

		if (isOccupantHalfPrebreathed()) {
			walkAway(person, PREBREATH_HALF_DONE);
			return remainingTime;
		}
		
		if (!airlock.isActivated()) {
			// Only the airlock operator may activate the airlock
			airlock.setActivated(true);
		}

		boolean canProceed = false;

		if (inSettlement) {
			// Load up the EVA activity spots
			airlock.loadEVAActivitySpots();

			if (!airlock.addAwaitingInnerDoor(id)) {
				walkAway(person, "Cannot get a spot at the inner door of " + airlock.getEntity().toString() + ".");
				return 0;
			}

			if (airlock.isChamberFull() || !airlock.hasSpace()) {
				walkAway(person, "Chamber full.");
				return 0;
			}
			
			if (!transitionTo(0)) {
				walkAway(person, "Cannot transition to zone 0.");
				return 0;
			}
				
			if (!airlock.isInnerDoorLocked() || airlock.isEmpty()) {
				// The inner door will stay locked if the chamber is NOT pressurized
				canProceed = true;
			}
			
			if (airlock.isEmpty()) {
				// If the airlock is empty, it means no one is using it
				logger.log((Unit)airlock.getEntity(), person, Level.FINE, 60_000,
						"No one is at " + airlock.getEntity().toString() + ".");
				// Go to the next phase in order for the inner door to be unlocked.
				// After the pressurization has finished, it should be open.
				canProceed = true;
			}
		}

		else {

	 		if (interiorDoorPos == null) {
	 			interiorDoorPos = airlock.getAvailableInteriorPosition();
			}

//			if (LocalAreaUtil.areLocationsClose(new LocalPosition.Double(person.getXLocation(), person.getYLocation()), interiorDoorPos)) {
				if (airlock.addAwaitingInnerDoor(id)) {
					canProceed = true;
				}
				else {
					walkAway(person, "Cannot wait at " + airlock.getEntity().toString() + " inner door.");
					return remainingTime;
				}
		}

		if (canProceed) {

			if (airlock.isPressurized() && !airlock.isInnerDoorLocked()) {
				// If airlock has already been pressurized,
				// then it's ready for entry

				logger.log((Unit)airlock.getEntity(), person, Level.FINE, 4_000,
						"Chamber already pressurized for entry in "
					+ airlock.getEntity().toString() + ".");

				// Skip PRESSURIZE_CHAMBER phase and go to the ENTER_AIRLOCK phase
				setPhase(ENTER_AIRLOCK);
			}

			else {
				// since it's not pressurized, will need to pressurize the chamber first

				if (!airlock.isActivated()) {
					// Only the airlock operator may activate the airlock
					airlock.setActivated(true);
				}

				if (airlock.isOperator(id)) {

					logger.log((Unit)airlock.getEntity(), person, Level.FINE, 4_000, "Ready to pressurize the chamber.");

					if (!airlock.isPressurized() || !airlock.isPressurizing()) {
						// Get ready for pressurization
						setPhase(PRESSURIZE_CHAMBER);
					}
				}
			}
		}

		return remainingTime;
	}

	/**
	 * Pressurizes the chamber.
	 *
	 * @param time
	 * @return the remaining time
	 */
	private double pressurizeChamber(double time) {

		double remainingTime = 0;

		if (!isFit()) {
			walkAway(person, NOT_FIT + " to pressurize chamber.");
			return remainingTime;
		}

		if (isOccupantHalfPrebreathed()) {
			walkAway(person, PREBREATH_HALF_DONE);
			return remainingTime;
		}
		
		if (airlock.isPressurized() && !airlock.isInnerDoorLocked()) {

			logger.log((Unit)airlock.getEntity(), person, Level.FINE, 4_000,
					"The chamber already pressurized in "
				+ airlock.getEntity().toString() + ".");

			// Add experience
			addExperience(time);

			setPhase(ENTER_AIRLOCK);
		}

		else if (airlock.isPressurizing()) {
			// just wait for pressurizing to finish
		}

		else {

			if (!airlock.isActivated()) {
				// Only the airlock operator may activate the airlock
				airlock.setActivated(true);
			}
			
			if (airlock.isOperator(id)) {
				// Command the airlock state to be transitioned to "pressurized"
				airlock.setTransitioning(true);	
			}
		}

		return remainingTime;
	}

	/**
	 * Enters through the inner door into the chamber of the airlock.
	 *
	 * @param time
	 * @return the remaining time
	 */
	private double enterAirlock(double time) {

		double remainingTime = 0;

		boolean canProceed = false;

		if (!isFit()) {
			walkAway(person, NOT_FIT + " to enter airlock.");
			return remainingTime;
		}

		if (isOccupantHalfPrebreathed()) {
			walkAway(person, PREBREATH_HALF_DONE);
			return remainingTime;
		}
		
		if (!airlock.isPressurized()) {
			// Go back to the previous phase
			setPhase(PRESSURIZE_CHAMBER);
			return remainingTime;
		}

		if (inSettlement) {

			if (airlock.isInnerDoorLocked()) {
				walkAway(person, INNER_DOOR_LOCKED);
				return remainingTime;
			}

			if (airlock.isChamberFull() || !airlock.hasSpace()) {
				walkAway(person, CHAMBER_FULL);
				return remainingTime;
			}
			
			if (!airlock.inAirlock(person)) {
				canProceed = airlock.enterAirlock(person, id, true);
			}
			else {// the person is already inside the airlock from previous cycle
				canProceed = true;
			}
			
            // true if the person is already inside the chamber from previous cycle
            if (canProceed && transitionTo(1)) {
//						logger.log((Unit)airlock.getEntity(), person, Level.INFO, 20_000,
//								"called transitionTo(1) in " + airlock.getEntity().toString() + ".");
				canProceed = true;
			}
			else {
				canProceed = isInZone(2);
			}
		}

		else {

	 		if (interiorDoorPos == null) {
	 			interiorDoorPos = airlock.getAvailableInteriorPosition();
			}

//			if (LocalAreaUtil.areLocationsClose(new LocalPosition.Double(person.getXLocation(), person.getYLocation()), interiorDoorPos)) {
				if (!airlock.isInnerDoorLocked()) {

					if (!airlock.inAirlock(person)) {
						canProceed = airlock.enterAirlock(person, id, true);
					} else // the person is already inside the airlock from previous cycle
						canProceed = true;
				}
				else {
					walkAway(person, airlock.getEntity().toString() + " inner door locked");
					return 0;
				}
//			}

//			else {
//				Rover airlockRover = (Rover) airlock.getEntity();
//
//				logger.log((Unit)airlock.getEntity(), person, Level.FINER, 4_000,
//						"Walked close to the interior door in " + airlockRover);
//			}
		}

		if (canProceed) {
			// Remove person from reservation map
			if (inSettlement)
				airlock.removeReservation(person.getIdentifier());

			logger.log((Unit)airlock.getEntity(), person, Level.FINE, 4_000,
					"Just entered through the inner door into "
					+ airlock.getEntity().toString() + ".");

			// Add experience
			addExperience(time);

			setPhase(WALK_TO_CHAMBER);
		}

		return remainingTime;
	}

	/**
	 * Walks to the chamber.
	 *
	 * @param time
	 * @return canProceed
	 */
	private double walkToChamber(double time) {

		double remainingTime = 0;

		if (!isFit()) {
			walkAway(person, NOT_FIT + " to walk to a chamber.");
			return remainingTime;
		}

		if (isOccupantHalfPrebreathed()) {
			walkAway(person, PREBREATH_HALF_DONE);
			return remainingTime;
		}
		
		if (!airlock.isPressurized()) {
			// Go back to the previous phase
			setPhase(PRESSURIZE_CHAMBER);
			return remainingTime;
		}

		logger.log((Unit)airlock.getEntity(), person, Level.FINE, 4_000,
				"Walking to a chamber in " + airlock.getEntity().toString() + ".");

		boolean canProceed = false;

		if (inSettlement) {

			if (airlock.isChamberFull() || !airlock.hasSpace()) {
				walkAway(person, CHAMBER_FULL);
				return remainingTime;
			}
			
			if (transitionTo(2)) {
				canProceed = true;
			}

			else {
				setPhase(ENTER_AIRLOCK);
				return 0;
			}
		}

		else {

			if (insideAirlockPos == null) {
	 			insideAirlockPos = airlock.getAvailableAirlockPosition();
			}

//	 		if (LocalAreaUtil.areLocationsClose(new LocalPosition.Double(person.getXLocation(), person.getYLocation()), insideAirlockPos)) {
	 			if (!airlock.isInnerDoorLocked()) {
	 				canProceed = true;
				}
	 			else {
					setPhase(ENTER_AIRLOCK);
					return 0;
				}
//			}

//			else {
//				Rover airlockRover = (Rover) airlock.getEntity();
//				logger.log((Unit)airlock.getEntity(), person, Level.FINER, 4_000,
//						"Walked to the reference position.");
//
//		 		// Walk to interior airlock position.
//		 		addSubTask(new WalkRoverInterior(person, airlockRover,
//		 				insideAirlockPos.getX(), insideAirlockPos.getY()));
//			}
		}


		if (canProceed) {

			if (!airlock.isActivated()) {
				// Only the airlock operator may activate the airlock
				airlock.setActivated(true);
			}
			
			if (airlock.isOperator(id)) {
				// Elect an operator to handle this task
				if (!airlock.isPressurized() || !airlock.isPressurizing()) {
					// Get ready for pressurization
					setPhase(PRESSURIZE_CHAMBER);
				}
			}

			if (airlock.isPressurized()) {
				logger.log((Unit)airlock.getEntity(), person, Level.FINE, 4_000,
						"Chamber already pressurized for entry in " + airlock.getEntity().toString() + ".");

		 		// Reset the suit donning time
				remainingDonningTime = SUIT_DONNING_TIME + RandomUtil.getRandomInt(-5, 5);

				setPhase(DON_EVA_SUIT);
			}

			// Add experience
			addExperience(time);
		}


		return remainingTime;
	}

	/**
	 * Selects an EVA suit and don it.
	 *
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time
	 */
	private double donEVASuit(double time) {

		double remainingTime = 0;

		if (!isFit()) {
			walkAway(person, NOT_FIT + " to don an EVA suit.");
			return 0;
		}

		if (isOccupantHalfPrebreathed()) {
			walkAway(person, PREBREATH_HALF_DONE);
			return remainingTime;
		}
		
		if (!airlock.isPressurized()) {
			// Go back to the previous phase
			setPhase(PRESSURIZE_CHAMBER);
			return 0;
		}

		EVASuit suit = null;

		// Check if person already has EVA suit.
		if (alreadyHasEVASuit()) {
			hasSuit = true;
		}

		// Get an EVA suit from entity inventory.
		EquipmentOwner housing = null;
		if (!hasSuit) {
			if (inSettlement)
				housing = ((Building)airlock.getEntity()).getSettlement();
			else
				housing = (Vehicle)airlock.getEntity();

			suit = InventoryUtil.getGoodEVASuitNResource((EquipmentOwner)housing, person);
		}

		if (!hasSuit || suit != null) {

			// if a person hasn't donned the suit yet
			// 0. Remove garment and put on pressure suit
			if (person.unwearGarment(housing)) {
				person.wearPressureSuit(housing);
			}
			// 1. Transfer the EVA suit from settlement/vehicle to person
			suit.transfer(person);
			// 2. Set the person as the owner
			suit.setLastOwner(person);
			// 3. Register the suit the person will take into the airlock to don
			person.registerSuit(suit);
			// Print log
			logger.log((Unit)airlock.getEntity(), person, Level.FINE, 4_000, "Just donned the " + suit.getName() + ".");
			// 4. Loads the resources into the EVA suit
			if (suit.loadResources(housing) < 0.9D) {
				logger.warning(suit, "Being used but not full loaded.");
			}
			// the person has a EVA suit
			hasSuit = true;
		}

		if (hasSuit) {
			remainingDonningTime -= time;

			if (remainingDonningTime <= 0) {
				// Add experience
				addExperience(time - remainingTime);

				logger.log(person, Level.FINER, 4_000,
						"Donned the EVA suit and got ready to do pre-breathing.");
				// Reset the prebreathing time counter to max
				person.getPhysicalCondition().resetRemainingPrebreathingTime();

				setPhase(PREBREATHE);
			}
		}

		else {

			logger.log((Unit)airlock.getEntity(), person, Level.WARNING, 4_000,
					"Could not find a working EVA suit. End this task.");

			// TODO: what to do if person still doesn't have an EVA suit ?
			// Q: why would a person be allowed to initiate this task in the first place 
			//    if there is no known working EVA suit ? Unless there is a sync issue 
			// Q: how do we make an EVA suit pre-assigned to a person prior to starting 
			//    the process of EVA. 
			endTask();

			// Will need to clear the task that create the ExitAirlock sub task
//			person.getMind().getTaskManager().clearAllTasks();

			return 0D;
		}

		return remainingTime;
	}

	/**
	 * Pre-breathes in the EVA suit to prevent the potential occurrence of incapacitating decompression sickness (DCS).
	 * Prebreathing reduces the nitrogen content in the astronaut's body which prevents the formation of nitrogen
	 * bubbles in body tissues when the atmospheric pressure is reduced.
	 *
	 * @param time the pulse
	 * @return the remaining time
	 */
	private double prebreathe(double time) {

		double remainingTime = 0;

		if (!hasSuit || person.getSuit() == null) {

			setPhase(DON_EVA_SUIT);
			
			return time;
		}
		
		boolean canProceed = false;

		PhysicalCondition pc = person.getPhysicalCondition();

		pc.reduceRemainingPrebreathingTime(time);

//		if (pc.isAtLeastThreeQuarterDonePrebreathing()) {
//	
//			List<Integer> list = new ArrayList<>(airlock.getOccupants());
//			for (int id : list) {
//				Person p = airlock.getPersonByID(id);
//				if (p.getSuit() == null) {
//					// Two groups of people having no EVA suits.
//					// (1) Those who egress but just come in to the airlock and haven't donned the suit yet
//					// (2) Those who ingress and have taken off the EVA suits. They are ready to leave.
//					canProceed = false;
//					break;
//				}
//			}
//		}

		if (pc.isDonePrebreathing()) {
			logger.log((Unit)airlock.getEntity(), person, Level.FINER, 4_000,
					"Done pre-breathing.");
			
			canProceed = true;
		}
		
		if (canProceed) {

			if (!airlock.isActivated()) {
				// Only the airlock operator may activate the airlock
				airlock.setActivated(true);
			}
			
			if (airlock.isOperator(id)) {
				// Get ready for depressurization
				setPhase(DEPRESSURIZE_CHAMBER);
			}

			if (airlock.isDepressurized()) {
				logger.log((Unit)airlock.getEntity(), person, Level.FINE, 4_000,
						"Chamber already depressurized for exit in " + airlock.getEntity().toString() + ".");

				setPhase(LEAVE_AIRLOCK);
			}

			// Add experience
			addExperience(time);

		}

		return remainingTime;
	}

	/**
	 * Depressurizes the chamber.
	 *
	 * @param time the pulse
	 * @return the remaining time
	 */
	private double depressurizeChamber(double time) {

		double remainingTime = 0;

		if (airlock.isDepressurizing()) {
			// just wait for depressurization to finish
		}
		
		else if (airlock.isPressurizing()) {
			// just wait for pressurization to finish
		}
		
		else if (airlock.isPressurized()) {
				
			if (!airlock.isActivated()) {
				// Only the airlock operator may activate the airlock
				airlock.setActivated(true);
			}
			
			if (airlock.isOperator(id)) {
				// Command the airlock state to be transitioned to "depressurized"
				airlock.setTransitioning(true);
			}
			
			Set<Person> list = airlock.noEVASuit();
			
			if (!list.isEmpty()) {
				// Everyone has an EVA suit
				logger.log((Unit)airlock.getEntity(), person, Level.WARNING, 4_000,
						"Could not depressurize " + airlock.getEntity().toString()
						+ ". " + list + " inside not wearing EVA suit.");

				for (Person p: list) {
					// Without an EVA suit, one needs to leave the airlock 
					// while the airlock is still being pressurized 
					walkAway(p, "Without EVA Suit.");
					logger.log(p, Level.WARNING, 4_000,
							"Ran out of time to don EVA suit. Cancelling EVA egress.");
				}
			}
		}
		
		else if (airlock.isDepressurized()) {

			logger.log((Unit)airlock.getEntity(), person, Level.FINE, 4_000,
					"Chamber already depressurized for exit in "
				+ airlock.getEntity().toString() + ".");
	
			// Add experience
			addExperience(time);

			setPhase(LEAVE_AIRLOCK);	
		}
		
		return remainingTime;
	}

	/**
	 * Departs the chamber through the outer door of the airlock.
	 *
	 * @param time the pulse
	 * @return the remaining time
	 */
	private double leaveAirlock(double time) {

		double remainingTime = 0;

		boolean canExit = false;

		if (inSettlement) {

			if (!transitionTo(3)) {
				// Wait here
			}
			
			if (airlock.inAirlock(person)
				&& !airlock.isOuterDoorLocked()) {
		
				if (airlock.exitAirlock(person, id, true)) {
					// Move to zone 4
					if (transitionTo(4)) {
						
						canExit = true;
					}
				}
			}
			else {
				canExit = true;
			}
		}

		else {

			if (exteriorDoorPos == null) {
				exteriorDoorPos = airlock.getAvailableExteriorPosition();
			}

			if (airlock.inAirlock(person)) {
				canExit = airlock.exitAirlock(person, id, true);
			}

//			Rover airlockRover = (Rover) airlock.getEntity();
//			logger.log((Unit)airlock.getEntity(), person, Level.FINER, 4_000,
//					"Tried walking close to the exterior door.");
		}

		if (canExit) {
			
			if (inSettlement) {
				// Remove the position at zone 4 before calling endTask()
				if (airlock.vacate(4, id)) {
					// Add experience
			 		addExperience(time);
		
					logger.log((Unit)airlock.getEntity(), person, Level.FINE, 4_000,
							"Leaving " + airlock.getEntity().toString() + ".");
		
					// This completes EVA egress from the airlock
					// End ExitAirlock task
					completeAirlockTask();
				}
				else {
					logger.log((Unit)airlock.getEntity(), person, Level.FINE, 4_000,
							"Can't vacate zone 4 at" + airlock.getEntity().toString() + ".");
				}
			}
			else {
				// Add experience
		 		addExperience(time);
	
				logger.log((Unit)airlock.getEntity(), person, Level.FINE, 4_000,
						"Leaving " + airlock.getEntity().toString() + ".");
	
				// This completes EVA egress from the airlock
				// End ExitAirlock task
				completeAirlockTask();
			}
		}

		return remainingTime;
	}

	/**
	 * Removes the person from airlock and walk away and ends the airlock and walk tasks.
	 */
	public void completeAirlockTask() {
		// Clear the person as the airlock operator if task ended prematurely.
		if (airlock != null && person.getName().equals(airlock.getOperatorName())) {
			if (inSettlement) {
				logger.log((Unit)airlock.getEntity(), person, Level.FINER, 4_000,
						"Concluded the airlock operator task.");
			}
			else {
				logger.log((Unit)airlock.getEntity(), person, Level.FINER, 4_000,
						"Concluded the vehicle airlock operator task.");
			}

			airlock.removeID(id);
		}

		// Ends the sub task 2 within the EnterAirlock task
		endSubTask2();

		// Remove all lingering tasks to avoid any unfinished walking tasks
//		person.getMind().getTaskManager().endSubTask();

		super.endTask();
	}

	/**
	 * Checks if a person can exit an airlock to do an EVA.
	 *
	 * @param person  the person exiting
	 * @param airlock the airlock to be used
	 * @return true if person can exit the entity
	 */
	public static boolean canExitAirlock(Person person, Airlock airlock) {
		boolean result = false;
		// Note: rescueOperation() is more like a hack, rather than a legitimate way 
		// of transferring a person through the airlock into the settlement 
		
		// Check if person is incapacitated.
		if (person.getPerformanceRating() <= MIN_PERFORMANCE) {
			// Prevent the logger statement below from being repeated multiple times
			logger.log((Unit)airlock.getEntity(), person, Level.INFO, 4_000,
					"Could not exit the airlock from " + airlock.getEntityName()
					+ " due to crippling performance rating of " + person.getPerformanceRating() + ".");

			// May need to relocate the following code to a proper place
			try {
				if (person.isInVehicle()) {
					Settlement nearbySettlement = CollectionUtils.findSettlement(person.getVehicle().getCoordinates());
					if (nearbySettlement != null) {				
						// Attempt a rescue operation
						result = person.rescueOperation((Rover) person.getVehicle(), person, nearbySettlement);
					}
				}
				else if (person.isOutside()) {
					Settlement nearbySettlement = CollectionUtils.findSettlement(person.getCoordinates());
					if (nearbySettlement != null)
						// Attempt a rescue operation
						result = person.rescueOperation(null, person, ((Building) (airlock.getEntity())).getSettlement());
				}

			} catch (Exception e) {
				logger.log((Unit)airlock.getEntity(), person, Level.SEVERE, 4_000, "Could not get new action: ", e);
			}

			return result;
		}

		// Check if person is outside.
		if (person.isOutside()) {
			logger.log((Unit)airlock.getEntity(), person, Level.FINER, 4_000,
					"Already outside. No need to exit " + airlock.getEntityName() + ".");

			return false;
		}

		else if (person.isInSettlement()) {

			EVASuit suit = InventoryUtil.getGoodEVASuit(person);
			// Check if EVA suit is available.
			if (suit != null) {
				airlock.resetCheckEVASuit();
				return true;
			}

			else {
				logger.log((Unit)airlock.getEntity(), person, Level.WARNING, 4_000,
						"Could not find a working EVA suit and needed to wait.");

				airlock.addCheckEVASuit();
				return false;

			}
		}

		else if (person.isInVehicle()) {

			EVASuit suit = InventoryUtil.getGoodEVASuit(person);
			// Check if EVA suit is available.
			if (suit != null) {
				airlock.resetCheckEVASuit();
				return true;
			}

			else {
				logger.log((Unit)airlock.getEntity(), person, Level.WARNING, 4_000,
						"Could not find a working EVA suit and needed to wait.");

				Vehicle v = person.getVehicle();
				Mission m = person.getMind().getMission();
				String hasMission = "";
				if (m != null)
					hasMission = " for " + m.getTypeID();
				// Mission m = missionManager.getMission(person);
				logger.log((Unit)airlock.getEntity(), person, Level.WARNING, 20_000,
						v.getName() + hasMission
						+ ". No working EVA suit, awaiting the response for rescue.");

				// TODO: should at least wait for a period of time for the EVA suit to be fixed
				// before calling for rescue
				if (v != null && m != null && !v.isBeaconOn() && !v.isBeingTowed()) {

					// Repair this EVASuit by himself/herself
					logger.log((Unit)airlock.getEntity(), person, Level.WARNING, 20_000,
							v.getName() + hasMission
							+ ". Will try to repair an EVA suit.");

					if (!person.getMind().getTaskManager().getLastTaskName().equalsIgnoreCase(RepairInsideMalfunction.NAME))
						person.getMind().getTaskManager().addTask(new RepairInsideMalfunction(person));

					if (airlock.getCheckEVASuit() > 100)
						// Set the emergency beacon on since no EVA suit is available
						((VehicleMission) m).setEmergencyBeacon(person, v, true, "No good Eva Suit");

				}

				airlock.addCheckEVASuit();
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks if the person already has an EVA suit in their inventory.
	 *
	 * @return true if person already has an EVA suit.
	 */
	private boolean alreadyHasEVASuit() {
		EVASuit suit = person.getSuit();
		if (suit != null) {
			return true;
		}

		return false;
	}



	/**
	 * Release person from the associated Airlock
	 */
	@Override
	protected void clearDown() {
		// Clear the person as the airlock operator if task ended prematurely.
		if (airlock != null && person.getName().equals(airlock.getOperatorName())) {
			if (inSettlement) {
				logger.log(((Building) (airlock.getEntity())).getSettlement(), person, Level.FINE, 1_000,
						"Concluded the airlock operator task.");
			}
			else {
				logger.log(person.getVehicle(), person, Level.FINE, 4_000,
						"Concluded the vehicle airlock operator task.");
			}

			airlock.removeID(id);
		}
	}

	/**
	 * Can these Task be recorded
	 * @return false
	 */
	@Override
	protected boolean canRecord() {
		return false;
	}
}
