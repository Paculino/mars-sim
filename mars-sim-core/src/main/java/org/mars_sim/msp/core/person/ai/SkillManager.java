/*
 * Mars Simulation Project
 * SkillManager.java
 * @date 2022-07-16
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.TrainingType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.tool.RandomUtil;


/**
 * The SkillManager class manages skills for a given person. Each person has one
 * skill manager.
 */
public class SkillManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** The person's instance. */
	private Person person;
	/** The robot's instance. */
	private Robot robot;
//	private CoreMind coreMind;
	
	/** A list of skills keyed by skill type enum. */
	private Map<SkillType, Skill> skills;

	/** Constructor. */
	public SkillManager(Unit unit) {
	
		if (unit instanceof Person) {
			person = (Person)unit;
		} else if (unit instanceof Robot) {
			robot = (Robot)unit;
		}

		skills = new ConcurrentHashMap<SkillType, Skill>();
	}

	/**
	 * Sets some random skills for a bot.
	 * 
	 * @param t
	 */
	public void setRandomBotSkills(RobotType t) {
		// Add starting skills randomly for a bot.
		List<SkillType> skills = new ArrayList<>();
		if (t == RobotType.MAKERBOT) {
			skills.add(SkillType.MATERIALS_SCIENCE);
			skills.add(SkillType.PHYSICS);
		}
		else if (t == RobotType.GARDENBOT) {
			skills.add(SkillType.BOTANY);
			skills.add(SkillType.BIOLOGY);
		}
		else if (t == RobotType.REPAIRBOT) {
			skills.add(SkillType.MATERIALS_SCIENCE);
			skills.add(SkillType.MECHANICS);
		}
		else if (t == RobotType.CHEFBOT) {
			skills.add(SkillType.CHEMISTRY);
			skills.add(SkillType.COOKING);
		}
		else if (t == RobotType.MEDICBOT) {
			skills.add(SkillType.CHEMISTRY);
			skills.add(SkillType.MEDICINE);
			skills.add(SkillType.PSYCHOLOGY);
		}
		else if (t == RobotType.DELIVERYBOT) {
			skills.add(SkillType.PILOTING);
			skills.add(SkillType.TRADING);
		}
		else if (t == RobotType.CONSTRUCTIONBOT) {
			skills.add(SkillType.AREOLOGY);
			skills.add(SkillType.CONSTRUCTION);
		}
		
		for (SkillType startingSkill : skills) {
			int skillLevel = 1;
			addNewSkillNExperience(startingSkill, skillLevel);
		}

	}
	
	/**
	 * Sets some random skills for a person.
	 */
	public void setRandomSkills() {
		int ageFactor = getPerson().getAge();
		// Add starting skills randomly for a person.
		for (SkillType startingSkill : SkillType.values()) {
			int skillLevel = 0;
			
			if (startingSkill == SkillType.PILOTING) {
				// Checks to see if a person has a pilot license/certification
				if (getPerson().getTrainings().contains(TrainingType.AVIATION_CERTIFICATION)) {
					skillLevel = getInitialSkillLevel(1, 35);
					int exp = RandomUtil.getRandomInt(0, 24);
					this.addExperience(startingSkill, exp, 0);
				}
			}
			
			// Medicine skill is highly needed for diagnosing sickness and prescribing medication 
			if (startingSkill == SkillType.MEDICINE) {
					skillLevel = getInitialSkillLevel(0, 35);
					int exp = RandomUtil.getRandomInt(0, 24);
					this.addExperience(startingSkill, exp, 0);
			}

			// psychology skill is sought after for living in confined environment
			else if (startingSkill == SkillType.PSYCHOLOGY) {
				skillLevel = getInitialSkillLevel(0, 35);
				int exp = RandomUtil.getRandomInt(0, 24);
				this.addExperience(startingSkill, exp, 0);		
			}
			
			// Mechanics skill is sought after for repairing malfunctions
			else if (startingSkill == SkillType.MATERIALS_SCIENCE
				 || startingSkill == SkillType.MECHANICS) {
				skillLevel = getInitialSkillLevel(0, 45);
				int exp = RandomUtil.getRandomInt(0, 24);
				this.addExperience(startingSkill, exp, 0);
			}
			
			else {
				int rand = RandomUtil.getRandomInt(0, 3);
				
				if (rand == 0) {
					skillLevel = getInitialSkillLevel(0, (int)(10 + ageFactor/10.0));
					addNewSkillNExperience(startingSkill, skillLevel);
				}
				else if (rand == 1) {
					skillLevel = getInitialSkillLevel(1, (int)(5 + ageFactor/8.0));
					addNewSkillNExperience(startingSkill, skillLevel);
				}
				else if (rand == 2) {
					skillLevel = getInitialSkillLevel(2, (int)(2.5 + ageFactor/6.0));
					addNewSkillNExperience(startingSkill, skillLevel);
				}
//				else if (rand == 3) {
//					skillLevel = getInitialSkillLevel(3, (int)(1.25 + ageFactor/4));
//					addNewSkillNExperience(startingSkill, skillLevel);
//				}
			}
		}
	}
	
	/**
	 * Adds a new skill at the prescribed level.
	 * 
	 * @param startingSkill
	 * @param skillLevel
	 */
	public void addNewSkillNExperience(SkillType startingSkill, int skillLevel) {
		Skill newSkill = new Skill(startingSkill);
		newSkill.setLevel(skillLevel);
		addNewSkill(newSkill);
		// Add some initial experience points
		int exp = RandomUtil.getRandomInt(0, (int)(Skill.BASE * Math.pow(2, skillLevel)) - 1);
		this.addExperience(startingSkill, exp, 0);
	}
	
	/**
	 * Returns an initial skill level.
	 * 
	 * @param level  lowest possible skill level
	 * @param chance the chance that the skill will be greater
	 * @return the initial skill level
	 */
	private int getInitialSkillLevel(int level, int chance) {
		if (RandomUtil.lessThanRandPercent(chance))
			return getInitialSkillLevel(level + 1, chance / 2);
		else
			return level;
	}

	/**
	 * Returns the number of skills.
	 * 
	 * @return the number of skills
	 */
	public int getSkillNum() {
		return skills.size();
	}

	/**
	 * Returns a random skill type.
	 * 
	 * @return a skill type
	 */
	public SkillType getARandomSkillType() {
		int rand = RandomUtil.getRandomInt(getSkillNum() - 1);
		return getKeys()[rand];
	}
	
	/**
	 * Returns an array of skill types.
	 * 
	 * @return an array of skill types
	 */
	public SkillType[] getKeys() {
		return skills.keySet().toArray(new SkillType[] {});
	}

	/**
	 * Returns an array of the skill strings.
	 * 
	 * @return an array of the skill strings
	 */
	public List<String> getKeyStrings() {
		return new ArrayList<>(skills.keySet()).stream()
				   .map(o -> o.getName())
				   .collect(Collectors.toList());
	}
	
	/**
	 * Returns true if the SkillManager has the named skill, false otherwise.
	 * 
	 * @param skill {@link SkillType} the skill's name
	 * @return true if the manager has the named skill
	 */
	public boolean hasSkill(SkillType skill) {
		return skills.containsKey(skill);
	}

	/**
	 * Returns the integer skill level from a named skill if it exists in the
	 * SkillManager. Returns 0 otherwise.
	 * 
	 * @param skillType {@link SkillType}
	 * @return {@link Integer} >= 0
	 */
	public int getSkillLevel(SkillType skillType) {
		int result = 0;
		if (skills.containsKey(skillType)) {
			result = skills.get(skillType).getLevel();
		}
		return result;
	}

	/**
	 * Returns the skill instance if it exists in the
	 * SkillManager. Returns null otherwise.
	 * 
	 * @param skillType {@link SkillType}
	 * @return {@link Skill}
	 */
	public Skill getSkill(SkillType skillType) {
		if (skills.containsKey(skillType)) {
			return skills.get(skillType);
		}
		return null;
	}
	
	/**
	 * Gets the cumulative experience points of the skill.
	 * 
	 * @param skillType {@link SkillType}
	 * @return the cumulative experience points
	 */
	public double getCumuativeExperience(SkillType skillType) {
		Skill skill = getSkill(skillType);
		if (skill != null) {
			// Calculate exp points at the current level
			double pts = skill.getExperience();
			int level = skill.getLevel();
			// Calculate the exp points at previous levels
			for (int i=0; i<level; i++) {
				pts += Skill.BASE * Math.pow(2D, level);
			}
			return pts;
		}
		else
			return 0;

	}
	
	/**
	 * Returns the integer skill experiences at the current level.
	 * 
	 * @param skill {@link SkillType}
	 * @return {@link Integer} >= 0
	 */
	public int getSkillExp(SkillType skill) {
		int result = 0;
		if (skills.containsKey(skill)) {
			result = (int) skills.get(skill).getExperience();
		}
		return result;
	}
	
	/**
	 * Returns the integer skill experiences needed to promote to the next level
	 * 
	 * @param skill {@link SkillType}
	 * @return {@link Integer} >= 0
	 */
	public int getSkillDeltaExp(SkillType skill) {
		int result = 0;
		if (skills.containsKey(skill)) {
			result = (int) skills.get(skill).getNeededExp();
		}
		return result;
	}
	
	/**
	 * Returns the integer labor time from a named skill if it exists in the
	 * SkillManager. Returns 0 otherwise.
	 * 
	 * @param skill {@link SkillType}
	 * @return {@link Integer} >= 0
	 */
	public int getSkillTime(SkillType skill) {
		int result = 0;
		if (skills.containsKey(skill)) {
			result = (int) skills.get(skill).getTime();
		}
		return result;
	}
	
	/**
	 * Returns the effective integer skill level from a named skill based on
	 * additional modifiers such as performance.
	 * 
	 * @param skillType the skill's type
	 * @return the skill's effective level
	 */
	public int getEffectiveSkillLevel(SkillType skillType) {
		int skill = getSkillLevel(skillType);
		double performance = 0;
		if (person != null) { 
			performance = getPerson().getPerformanceRating();
		}
		else if (robot != null) {
			performance = getRobot().getPerformanceRating();
		}
		return (int) Math.round(performance * skill);
	}

	/**
	 * Adds a new skill to the SkillManager and indexes it under its name.
	 * 
	 * @param newSkill the skill to be added
	 */
	public void addNewSkill(Skill newSkill) {
		SkillType skillType = newSkill.getSkill();
		if (hasSkill(skillType))
			skills.get(skillType).setLevel(newSkill.getLevel());
		else {
			skills.put(skillType, newSkill);
		}
		
//		// Set up the core mind
//		String skillEnumString = skillType.ordinal() + "";
//		String name = "";
//		if (person != null) 
//			name = person.getName();
//		else 
//			name = robot.getName();
//		LogConsolidated.log(Level.SEVERE, 5_000, sourceName,
//				name + " is acquiring the " + skillType.getName() + " skill (id " + skillEnumString + ")");
//		coreMind.create(skillEnumString);
	}

	/**
	 * Adds given experience points to a named skill if it exists in the
	 * SkillManager. If it doesn't exist, create a skill of that name in the
	 * SkillManager and add the experience points to it.
	 * 
	 * @param skillType        {@link SkillType} the skill's type
	 * @param experiencePoints the experience points to be added
	 */
	public void addExperience(SkillType skillType, double experiencePoints, double time) {
		if (hasSkill(skillType)) {
			skills.get(skillType).addExperience(experiencePoints);
			skills.get(skillType).addTime(time);
		}
		else {
			addNewSkill(new Skill(skillType));
			addExperience(skillType, experiencePoints, time);
		}
	}

	public Map<String, Integer> getSkillLevelMap() {
		SkillType[] keys = getKeys();
		Map<String, Integer> skillLevelMap = new ConcurrentHashMap<>();
		for (SkillType skill : keys) {
			int level = getSkillLevel(skill);
			skillLevelMap.put(skill.getName(), level);
		}
		return skillLevelMap;
	}
	
	public Map<String, Integer> getSkillExpMap() {
		SkillType[] keys = getKeys();
		Map<String, Integer> skillExpMap = new ConcurrentHashMap<>();
		for (SkillType skill : keys) {
			int exp = getSkillExp(skill);
			skillExpMap.put(skill.getName(), exp);
		}
		return skillExpMap;
	}
	
	public Map<String, Integer> getSkillDeltaExpMap() {
		SkillType[] keys = getKeys();
		Map<String, Integer> skillDeltaExpMap = new ConcurrentHashMap<>();
		for (SkillType skill : keys) {
			int exp = getSkillDeltaExp(skill);
			skillDeltaExpMap.put(skill.getName(), exp);
		}
		return skillDeltaExpMap;
	}
	
	public Map<String, Integer> getSkillTimeMap() {
		SkillType[] keys = getKeys();
		Map<String, Integer> skillTimeMap = new ConcurrentHashMap<>();
		for (SkillType skill : keys) {
			int exp = getSkillTime(skill);
			skillTimeMap.put(skill.getName(), exp);
		}
		return skillTimeMap;
	}
	
	/**
	 * Gets the person's reference.
	 * 
	 * @return {@link Person}
	 */
	public Person getPerson() {
		return person;
	}
	
	/**
	 * Gets the robot's reference.
	 * 
	 * @return {@link Robot}
	 */
	public Robot getRobot() {
		return robot;
	}
	
	/**
	 * Loads instances.
	 */
	public static void initializeInstances() {
	}
			
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		skills.clear();
		skills = null;
		person = null;
		robot = null;
	}
}
