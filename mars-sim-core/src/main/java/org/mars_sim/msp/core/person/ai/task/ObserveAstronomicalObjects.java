/*
 * Mars Simulation Project
 * ObserveAstronomicalObjects.java
 * @date 2022-07-17
 * @author Sebastien Venot
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
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.AstronomicalObservation;
import org.mars_sim.msp.core.structure.building.function.Computation;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * A task for observing the night sky with an astronomical observatory.
 */
public class ObserveAstronomicalObjects extends Task implements ResearchScientificStudy, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ObserveAstronomicalObjects.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.observeAstronomicalObjects"); //$NON-NLS-1$

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.2D;

	/** Task phases. */
	private static final TaskPhase OBSERVING = new TaskPhase(Msg.getString("Task.phase.observing")); //$NON-NLS-1$

	// Data members.
	/** True if person is active observer. */
	private boolean isActiveObserver = false;
	/** Computing Units needed per millisol. */		
	private double computingNeeded;
	/** The seed value. */
    private double seed = RandomUtil.getRandomDouble(.05, 0.25);
    
	private final double TOTAL_COMPUTING_NEEDED;
	
	/** The scientific study the person is researching for. */
	private ScientificStudy study;
	/** The observatory the person is using. */
	private AstronomicalObservation observatory;
	/** The research assistant. */
	private Person researchAssistant;
	
	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public ObserveAstronomicalObjects(Person person) {
		// Use task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, SkillType.ASTRONOMY, 25D,
			  100D + RandomUtil.getRandomDouble(100D));
		
		TOTAL_COMPUTING_NEEDED = getDuration() * seed;
		computingNeeded = TOTAL_COMPUTING_NEEDED;
		
		setExperienceAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		
		// Determine study.
		study = determineStudy();
		if (study != null) {
			// Determine observatory to use.
			observatory = determineObservatory(person);
			if (observatory != null) {
				// Walk to observatory building.
				walkToTaskSpecificActivitySpotInBuilding(observatory.getBuilding(),
														FunctionType.ASTRONOMICAL_OBSERVATION, false);
				observatory.addObserver();
				isActiveObserver = true;
				
				// Initialize phase
				addPhase(OBSERVING);
				setPhase(OBSERVING);
				
			} else {
				logger.log(person, Level.SEVERE, 5000, 
						"Could not find the observatory.");
				endTask();
			}
		}
		else
			endTask();
	}

	/**
	 * Gets the preferred local astronomical observatory for an observer.
	 * 
	 * @param observer the observer.
	 * @return observatory or null if none found.
	 */
	public static AstronomicalObservation determineObservatory(Person observer) {
		AstronomicalObservation result = null;

		if (observer.isInSettlement()) {

			BuildingManager manager = observer.getSettlement().getBuildingManager();
			List<Building> observatoryBuildings = manager.getBuildings(FunctionType.ASTRONOMICAL_OBSERVATION);
			observatoryBuildings = BuildingManager.getNonMalfunctioningBuildings(observatoryBuildings);
			observatoryBuildings = getObservatoriesWithAvailableSpace(observatoryBuildings);
			observatoryBuildings = BuildingManager.getLeastCrowdedBuildings(observatoryBuildings);

			if (observatoryBuildings.size() > 0) {
				Map<Building, Double> observatoryBuildingProbs = BuildingManager.getBestRelationshipBuildings(observer,
						observatoryBuildings);
				Building building = RandomUtil.getWeightedRandomObject(observatoryBuildingProbs);
				if (building != null) {
					result = building.getAstronomicalObservation();
				}
			}
		}

		return result;
	}

	/**
	 * Gets the crowding modifier for an observer to use a given observatory
	 * building.
	 * 
	 * @param observer    the observer.
	 * @param observatory the astronomical observatory.
	 * @return crowding modifier.
	 */
	public static double getObservatoryCrowdingModifier(Person observer, AstronomicalObservation observatory) {
		double result = 1D;
		if (observer.isInSettlement()) {
			Building observatoryBuilding = observatory.getBuilding();
			if (observatoryBuilding != null) {
				result *= Task.getCrowdingProbabilityModifier(observer, observatoryBuilding);
				result *= Task.getRelationshipModifier(observer, observatoryBuilding);
			}
		}
		return result;
	}

	/**
	 * Gets a list of observatory buildings with available research space from a
	 * list of observatory buildings.
	 * 
	 * @param buildingList list of buildings with astronomical observation function.
	 * @return observatory buildings with available observatory space.
	 */
	private static List<Building> getObservatoriesWithAvailableSpace(List<Building> buildingList) {
		List<Building> result = new ArrayList<Building>();

		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			AstronomicalObservation observatory = building.getAstronomicalObservation();
			if (observatory.getObserverNum() < observatory.getObservatoryCapacity()) {
				result.add(building);
			}
		}

		return result;
	}

	/**
	 * Determines the scientific study for the observations.
	 * 
	 * @return study or null if none available.
	 */
	private ScientificStudy determineStudy() {
		ScientificStudy result = null;

		ScienceType astronomy = ScienceType.ASTRONOMY;
		List<ScientificStudy> possibleStudies = new ArrayList<ScientificStudy>();

		// Add primary study if in research phase.
		ScientificStudy primaryStudy = person.getStudy();
		if (primaryStudy != null) {
			if (ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())
					&& !primaryStudy.isPrimaryResearchCompleted()) {
				if (astronomy == primaryStudy.getScience()) {
					// Primary study added twice to double chance of random selection.
					possibleStudies.add(primaryStudy);
					possibleStudies.add(primaryStudy);
				}
			}
		}

		// Add all collaborative studies in research phase.
		Iterator<ScientificStudy> i = person.getCollabStudies().iterator();
		while (i.hasNext()) {
			ScientificStudy collabStudy = i.next();
			if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())
					&& !collabStudy.isCollaborativeResearchCompleted(person)) {
				if (astronomy == collabStudy.getContribution(person)) {
					possibleStudies.add(collabStudy);
				}
			}
		}

		// Randomly select study.
		if (possibleStudies.size() > 0) {
			int selected = RandomUtil.getRandomInt(possibleStudies.size() - 1);
			result = possibleStudies.get(selected);
		}

		return result;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (OBSERVING.equals(getPhase())) {
			return observingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the observing phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	protected double observingPhase(double time) {

		if (person.getPhysicalCondition().computeFitnessLevel() < 2) {
			logger.log(person, Level.INFO, 0, 
				"Ended observing astronomical objects. Not feeling well.");
			endTask();
		}
		
		// If person is incapacitated, end task.
		if (person.getPerformanceRating() < 0.1) {
			endTask();
		}

		if (isDone()) {
    		logger.info(person, 30_000L, NAME + " - " 
    				+ Math.round((TOTAL_COMPUTING_NEEDED - computingNeeded) * 100.0)/100.0 
    				+ " CUs Used.");
			endTask();
			endTask();
			return time;
		}
		
		// Check for observatory malfunction.
		if (observatory != null && observatory.getBuilding().getMalfunctionManager().hasMalfunction()) {
			endTask();
		}

		// Check sunlight and end the task if sunrise
		double sunlight = surfaceFeatures.getSolarIrradiance(person.getCoordinates());
		if (sunlight > 12) {
			endTask();
		}

        int msol = marsClock.getMillisolInt();
        
        boolean successful = false; 
        
        if (computingNeeded > 0) {
        	double workPerMillisol = 0; 
 
        	if (computingNeeded <= seed) {
        		workPerMillisol = time * computingNeeded;
        	}
        	else {
        		workPerMillisol = time * seed * RandomUtil.getRandomDouble(.9, 1.1);
        	}

        	// Submit request for computing resources
        	Computation center = person.getAssociatedSettlement().getBuildingManager()
        			.getMostFreeComputingNode(workPerMillisol, msol + 1, (int)(msol + getDuration()));
        	if (center != null) {
        		if (computingNeeded <= seed)
        			successful = center.scheduleTask(workPerMillisol, msol + 1, msol + 2);
        		else
        			successful = center.scheduleTask(workPerMillisol, msol + 1, (int)(msol + getDuration()));
        	}
	    	else
	    		logger.info(person, 30_000L, "No computing centers available for " + NAME + ".");
        	
        	if (successful) {
        		if (computingNeeded <= seed)
        			computingNeeded = computingNeeded - workPerMillisol;
        		else
        			computingNeeded = computingNeeded - workPerMillisol * getDuration();
        		if (computingNeeded < 0) {
        			computingNeeded = 0; 
        		}
          	}
	    	else {
	    		logger.info(person, 30_000L, "No computing resources for " + NAME + ".");
	    	}
        }
        else if (computingNeeded <= 0) {
        	// this task has ended
    		logger.log(person, Level.INFO, 30_000L, NAME + " - " 
    				+ Math.round(TOTAL_COMPUTING_NEEDED * 100.0)/100.0 
    				+ " CUs Used.");
        	endTask();
        }
        
		// Add research work time to study.
		double observingTime = getEffectiveObservingTime(time);
		
		boolean isPrimary = study.getPrimaryResearcher().equals(person);
		if (isPrimary) {
			study.addPrimaryResearchWorkTime(observingTime);
		} else {
			study.addCollaborativeResearchWorkTime(person, observingTime);
		}

		// Check if research in study is completed.
		if (isPrimary) {
			if (study.isPrimaryResearchCompleted()) {
				logger.log(person, Level.INFO, 0, 
						"Just spent " 
						+ Math.round(study.getPrimaryResearchWorkTimeCompleted() *10.0)/10.0
						+ " millisols to complete a primary research using " + person.getLocationTag().getImmediateLocation());				
				endTask();
			}
		} else {
			if (study.isCollaborativeResearchCompleted(person)) {
				logger.log(person, Level.INFO, 0, 
						"Just spent " 
						+ Math.round(study.getCollaborativeResearchWorkTimeCompleted(person) *10.0)/10.0
						+ " millisols to complete a collaborative research using " + person.getLocationTag().getImmediateLocation());
				endTask();
			}
		}
		// Add experience
		addExperience(observingTime);

		// Check for lab accident.
		checkForAccident(observatory.getBuilding(), 0.005D, time);

		return 0D;
	}

	/**
	 * Gets the effective observing time based on the person's astronomy skill.
	 * 
	 * @param time the real amount of time (millisol) for observing.
	 * @return the effective amount of time (millisol) for observing.
	 */
	private double getEffectiveObservingTime(double time) {
		// Determine effective observing time based on the astronomy skill.
		double observingTime = time;
		int astronomySkill = getEffectiveSkillLevel();
		if (astronomySkill == 0) {
			observingTime /= 2D;
		}
		if (astronomySkill > 1) {
			observingTime += observingTime * (.2D * astronomySkill);
		}

		// Modify by tech level of observatory.
		int techLevel = observatory.getTechnologyLevel();
		if (techLevel > 0) {
			observingTime *= techLevel;
		}

		// If research assistant, modify by assistant's effective skill.
		if (hasResearchAssistant()) {
			SkillManager manager = researchAssistant.getSkillManager();
			int assistantSkill = manager.getEffectiveSkillLevel(ScienceType.ASTRONOMY.getSkill());
			if (astronomySkill > 0) {
				observingTime *= 1D + ((double) assistantSkill / (double) astronomySkill);
			}
		}

		return observingTime;
	}

	/**
	 * Checks if the sky is dimming and is at dusk
	 * 
	 * @param person
	 * @return
	 */
	public static boolean isGettingDark(Person person) {
        return EVAOperation.isGettingDark(person);
    }
	
	
	/**
	 * Release Observatory
	 */
	@Override
	protected void clearDown() {

		// Remove person from observatory so others can use it.
		try {
			if ((observatory != null) && isActiveObserver) {
				observatory.removeObserver();
				isActiveObserver = false;
			}
		} catch (Exception e) {
		}
	}

	@Override
	public ScienceType getResearchScience() {
		return ScienceType.ASTRONOMY;
	}

	@Override
	public Person getResearcher() {
		return person;
	}

	@Override
	public boolean hasResearchAssistant() {
		return (researchAssistant != null);
	}

	@Override
	public Person getResearchAssistant() {
		return researchAssistant;
	}

	@Override
	public void setResearchAssistant(Person researchAssistant) {
		this.researchAssistant = researchAssistant;
	}
}
