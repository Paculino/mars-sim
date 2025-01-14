/*
 * Mars Simulation Project
 * MissionSubAgenda.java
 * @date 2022-07-15
 * @author Barry Evans
 */
package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.science.ScienceType;

/**
 * This class represents a sub-agenda of reporting.
 */
public class MissionSubAgenda implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String description;
	private Map<MissionType, Integer> missionModifiers;
	private Map<ScienceType, Integer> scienceModifiers;

	public MissionSubAgenda(String description, Map<MissionType, Integer> missionModifiers,
							Map<ScienceType, Integer> scienceModifiers) {
		super();
		this.description = description;
		this.missionModifiers = Collections.unmodifiableMap(missionModifiers);
		this.scienceModifiers = Collections.unmodifiableMap(scienceModifiers);

	}
	
	public String getDescription() {
		return description;
	}
	
	public Map<MissionType, Integer> getMissionModifiers() {
		return missionModifiers;
	}
	
	public Map<ScienceType, Integer> getScienceModifiers() {
		return scienceModifiers;
	}
	
	public String toString() {
		return description;
	}
}
