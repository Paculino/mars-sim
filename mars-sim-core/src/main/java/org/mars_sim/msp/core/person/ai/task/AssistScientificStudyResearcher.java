/*
 * Mars Simulation Project
 * AssistScientificStudyResearcher.java
 * @date 2022-06-11
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.social.RelationshipUtil;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * Task for assisting a scientific study researcher.
 */
public class AssistScientificStudyResearcher extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(AssistScientificStudyResearcher.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.assistScientificStudyResearcher"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase ASSISTING = new TaskPhase(Msg.getString("Task.phase.assisting")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = 0D;

	/**
	 * The improvement in relationship opinion of the assistant from the researcher
	 * per millisol.
	 */
	private static final double BASE_RELATIONSHIP_MODIFIER = .2D;

	// Data members
	private ResearchScientificStudy researchTask;
	private Person researcher;

	/**
	 * Constructor.
	 *
	 * @param person the person performing the task.
	 */
	public AssistScientificStudyResearcher(Person person) {
		// Use Task constructor. Skill determined later based on study
		super(NAME, person, true, false, STRESS_MODIFIER, null, 50D);
		setExperienceAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);

		// Determine researcher
		researcher = determineResearcher();
		if (researcher != null) {
			researchTask = (ResearchScientificStudy) researcher.getMind().getTaskManager().getTask();
			if (researchTask != null) {
				addAdditionSkill(researchTask.getResearchScience().getSkill());
				researchTask.setResearchAssistant(person);
				setDescription(
						Msg.getString("Task.description.assistScientificStudyResearcher.detail", researcher.getName())); // $NON-NLS-1$

				// If in settlement, move assistant to building researcher is in.
				if (person.isInSettlement()) {

					Building researcherBuilding = BuildingManager.getAvailableBuilding(person.getStudy(), person);
					if (researcherBuilding != null) {
						// Walk to researcher
						walkToResearchSpotInBuilding(researcherBuilding, false);
					}
				} else if (person.isInVehicle()) {
					// If person is in rover, walk to passenger activity spot.
					if (person.getVehicle() instanceof Rover) {
						walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), false);
					}
				} else {
					// Walk to random location.
					walkToRandomLocation(true);
				}
			} else {
				logger.severe(person, "Unable to start assisting Researcher task.");
				endTask();
			}
		} else {
			logger.severe(person, "Cannot find researcher");
			endTask();
		}

		// Initialize phase
		addPhase(ASSISTING);
		setPhase(ASSISTING);
	}

	/**
	 * Determines a researcher to assist.
	 *
	 * @return researcher or null if none found.
	 */
	private Person determineResearcher() {
		Person result = null;

		Collection<Person> researchers = getBestResearchers(person);
		if (researchers.size() > 0) {
			int rand = RandomUtil.getRandomInt(researchers.size() - 1);
			result = (Person) researchers.toArray()[rand];
		}

		return result;
	}

	/**
	 * Gets a list of the most preferred researchers to assist.
	 *
	 * @return collection of preferred researchers, empty of none available.
	 */
	public static Collection<Person> getBestResearchers(Person assistant) {
		Collection<Person> result = null;

		// Get all available researchers.
		Collection<Person> researchers = getAvailableResearchers(assistant);

		// If assistant is in a settlement, best researchers are in least crowded
		// buildings.
		Collection<Person> leastCrowded = new ConcurrentLinkedQueue<Person>();

		if (assistant.isInSettlement()) {
			// Find the least crowded buildings that researchers are in.
			int crowding = Integer.MAX_VALUE;
			Iterator<Person> i = researchers.iterator();
			while (i.hasNext()) {
				Person researcher = i.next();
				Building building = BuildingManager.getBuilding(researcher);
				if (building != null) {
					LifeSupport lifeSupport = building.getLifeSupport();
					int buildingCrowding = lifeSupport.getOccupantNumber() - lifeSupport.getOccupantCapacity() + 1;
					if (buildingCrowding < -1)
						buildingCrowding = -1;
					if (buildingCrowding < crowding)
						crowding = buildingCrowding;
				}
			}

			// Add researchers in least crowded buildings to result.
			Iterator<Person> j = researchers.iterator();
			while (j.hasNext()) {
				Person researcher = j.next();
				Building building = BuildingManager.getBuilding(researcher);
				if (building != null) {
					LifeSupport lifeSupport = building.getLifeSupport();
					int buildingCrowding = lifeSupport.getOccupantNumber() - lifeSupport.getOccupantCapacity() + 1;
					if (buildingCrowding < -1)
						buildingCrowding = -1;
					if (buildingCrowding == crowding)
						leastCrowded.add(researcher);
				}
			}
		} else
			leastCrowded = researchers;

		// Get the assistant's favorite researchers.
//		RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
		Collection<Person> favoriteResearchers = new ConcurrentLinkedQueue<Person>();

		// Find favorite opinion.
		double favorite = Double.NEGATIVE_INFINITY;
		Iterator<Person> k = leastCrowded.iterator();
		while (k.hasNext()) {
			Person researcher = k.next();
			double opinion = RelationshipUtil.getOpinionOfPerson(assistant, researcher);
			if (opinion > favorite)
				favorite = opinion;
		}

		// Get list of favorite researchers.
		k = leastCrowded.iterator();
		while (k.hasNext()) {
			Person researcher = k.next();
			double opinion = RelationshipUtil.getOpinionOfPerson(assistant, researcher);
			if (opinion == favorite)
				favoriteResearchers.add(researcher);
		}

		result = favoriteResearchers;

		return result;
	}

	/**
	 * Get a list of all available researchers to assist.
	 *
	 * @param assistant the research assistant.
	 * @return list of researchers.
	 */
	private static Collection<Person> getAvailableResearchers(Person assistant) {
		Collection<Person> result = new ConcurrentLinkedQueue<Person>();

		Iterator<Person> i = getLocalPeople(assistant).iterator();
		while (i.hasNext()) {
			Person person = i.next();
			Task personsTask = person.getMind().getTaskManager().getTask();
			if ((personsTask != null) && (personsTask instanceof ResearchScientificStudy)) {
				ResearchScientificStudy researchTask = (ResearchScientificStudy) personsTask;
				if (!researchTask.hasResearchAssistant() && researchTask.getResearchScience() != null) {
					SkillType scienceSkill = researchTask.getResearchScience().getSkill();
					int personSkill = person.getSkillManager().getEffectiveSkillLevel(scienceSkill);
					int assistantSkill = assistant.getSkillManager().getEffectiveSkillLevel(scienceSkill);
					if (assistantSkill < personSkill)
						result.add(person);
				}
			}
		}

		return result;
	}

	/**
	 * Gets a collection of people in a person's settlement or rover. The resulting
	 * collection doesn't include the given person.
	 *
	 * @param person the person checking
	 * @return collection of people
	 */
	private static Collection<Person> getLocalPeople(Person person) {
		Collection<Person> people = new ConcurrentLinkedQueue<Person>();

		if (person.isInSettlement()) {
			Iterator<Person> i = person.getSettlement().getIndoorPeople().iterator();
			while (i.hasNext()) {
				Person inhabitant = i.next();
				if (person != inhabitant)
					people.add(inhabitant);
			}
		} else if (person.isInVehicle()) {
			Crewable rover = (Crewable) person.getVehicle();
			Iterator<Person> i = rover.getCrew().iterator();
			while (i.hasNext()) {
				Person crewmember = i.next();
				if (person != crewmember)
					people.add(crewmember);
			}
		}

		return people;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (ASSISTING.equals(getPhase())) {
			return assistingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Perform the assisting phase of the task.
	 *
	 * @param time the amount (millisols) of time to perform the phase.
	 * @return the amount (millisols) of time remaining after performing the phase.
	 * @throws Exception
	 */
	private double assistingPhase(double time) {
	
        // If person is incapacitated, end task.
        if (person.getPerformanceRating() <= .2) {
            endTask();
            return time;
        }

		if (person.getPhysicalCondition().computeFitnessLevel() < 2) {
			logger.log(person, Level.FINE, 10_000, "Ended assisting researcher. Not feeling well.");
			endTask();
			return time;
		}

	      // Check if task is finished.
        if (((Task) researchTask).isDone()) {
            endTask();
            return 0;
        }

		// Check if researcher is in a different location situation than the assistant.
        // Remotely assisting a researcher is allowed
//		if (!researcher.getLocationSituation().equals(person.getLocationSituation())) {
//			endTask();
//		}

		// Add experience
		addExperience(time);

		// Add relationship modifier for opinion of assistant from the researcher.
		addRelationshipModifier(time);

		return 0;
	}

	/**
	 * Adds a relationship modifier for the researcher's opinion of the assistant.
	 *
	 * @param time the time assisting.
	 */
	private void addRelationshipModifier(double time) {
        RelationshipUtil.changeOpinion(researcher, person, BASE_RELATIONSHIP_MODIFIER * time);
	}
}
