/*
 * Mars Simulation Project
 * Skill.java
 * @date 2022-07-16
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai;

import java.io.Serializable;

/**
 * The Skill class represents a single skill that a person has. The skill must
 * have a name unique among the person's collection of skills.
 */
public class Skill implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** The base factor. */
	static final int BASE = 75;
	
	// Data members
	/** The skill level (0 to infinity). */
	private int level;
	
	/** The experience points towards the next skill level. */
	private double experiencePoints;
	/** The threshold of the experience points needed to reach the next skill level. */
	private double threshold;
	/** The labor time of the skill. */
	private double time;
	
	/** The unique (for each person) skill. */
	private SkillType skill;

	/**
	 * Constructor, starting at level 0.
	 * 
	 * @param skill {@link SkillType}
	 */
	public Skill(SkillType skill) {
		this.skill = skill;
		level = 0;
		experiencePoints = 0D;
		threshold = BASE;
	}

	/**
	 * Constructor with level.
	 * 
	 * @param skill {@link SkillType} the skill's name.
	 * @param level the skill's initial level.
	 */
	public Skill(SkillType skill, int level) {
		this(skill);
		setLevel(level);
	}

	/**
	 * Returns the name of the skill.
	 * 
	 * @return the skill's name
	 */
	public SkillType getSkill() {
		return skill;
	}

	/**
	 * Returns the level of the skill.
	 * 
	 * @return the skill's level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Sets the level of the skill.
	 * 
	 * @param newLevel the new level of the skill
	 */
	void setLevel(int newLevel) {
		level = newLevel;
		// Reset the exp points back to 0
		experiencePoints = 0;
		// Set the upper limit of exp points
		threshold = BASE * Math.pow(2D, newLevel);
	}

	/**
	 * Gets the experience needed to promote to the next level of the skill.
	 * 
	 * @return the delta experience points
	 */
	public double getNeededExp() {
		return threshold - experiencePoints;
	}
	
	
	/**
	 * Gets the experience points of the skill.
	 * 
	 * @return the experience points
	 */
	public double getExperience() {
		return experiencePoints;
	}
	
	/**
	 * Gets the cumulative experience points of the skill.
	 * 
	 * @return the cumulative experience points
	 */
	public double getCumuativeExperience() {
		// Calculate exp points at the current level
		double pts = experiencePoints;
		// Calculate the exp points at previous levels
		for (int i=0; i<level; i++) {
			pts += BASE * Math.pow(2D, level);
		}
		return pts;
	}
	
	/**
	 * Adds to the experience points of the skill.
	 * 
	 * @param newPoints the experience points to be added
	 */
	void addExperience(double newPoints) {
		experiencePoints += newPoints;
		// Check if it has reached the next level.		
		if (experiencePoints >= threshold) {
			experiencePoints -= threshold;
			threshold *= 2D;
			level++;
		}
	}
	
	/**
	 * Gets the labor time one has put in.
	 * 
	 * @return the labor time
	 */
	public double getTime() {
		return time;
	}
	
	/**
	 * Adds to the labor time.
	 * 
	 * @param time the labor tme
	 */
	void addTime(double time) {
		this.time += time; 
	}
}
