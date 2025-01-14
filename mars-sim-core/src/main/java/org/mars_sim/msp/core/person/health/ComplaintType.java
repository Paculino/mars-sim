/**
 * Mars Simulation Project
 * ComplaintType.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 *
 */
package org.mars_sim.msp.core.person.health;

import org.mars_sim.msp.core.Msg;

public enum ComplaintType {

	// Environmentally Induced Illnesses
	DECOMPRESSION  					(Msg.getString("ComplaintType.decompression")), //$NON-NLS-1$
	DEHYDRATION  					(Msg.getString("ComplaintType.dehydration")), //$NON-NLS-1$
	FREEZING  						(Msg.getString("ComplaintType.freezing")), //$NON-NLS-1$
	HEAT_STROKE  					(Msg.getString("ComplaintType.heatStroke")), //$NON-NLS-1$
	STARVATION  					(Msg.getString("ComplaintType.starvation")), //$NON-NLS-1$
	SUFFOCATION  					(Msg.getString("ComplaintType.suffocation")), //$NON-NLS-1$

	// Medical Illnesses
	APPENDICITIS					(Msg.getString("ComplaintType.appendicitis")), //$NON-NLS-1$
	BROKEN_BONE						(Msg.getString("ComplaintType.brokenBone")), //$NON-NLS-1$
	BURNS							(Msg.getString("ComplaintType.burns")), //$NON-NLS-1$
	COLD							(Msg.getString("ComplaintType.cold")), //$NON-NLS-1$
	DEPRESSION						(Msg.getString("ComplaintType.depression")), //$NON-NLS-1$
	FEVER							(Msg.getString("ComplaintType.fever")), //$NON-NLS-1$
	FLU								(Msg.getString("ComplaintType.flu")), //$NON-NLS-1$
	FOOD_POISONING					(Msg.getString("ComplaintType.foodPoisoning")), //$NON-NLS-1$
	FROSTNIP						(Msg.getString("ComplaintType.frostnip")), //$NON-NLS-1$
	FROSTBITE						(Msg.getString("ComplaintType.frostbite")), //$NON-NLS-1$
	GANGRENE						(Msg.getString("ComplaintType.gangrene")), //$NON-NLS-1$
	HEARTBURN						(Msg.getString("ComplaintType.heartburn")), //$NON-NLS-1$
	HIGH_FATIGUE_COLLAPSE			(Msg.getString("ComplaintType.highFatigueCollapse")), //$NON-NLS-1$
	HYPOXEMIA  						(Msg.getString("ComplaintType.hypoxemia")), //$NON-NLS-1$
	LACERATION						(Msg.getString("ComplaintType.laceration")), //$NON-NLS-1$
	MAJOR_BURNS						(Msg.getString("ComplaintType.majorBurns")), //$NON-NLS-1$
	MENINGITIS						(Msg.getString("ComplaintType.meningitis")), //$NON-NLS-1$
	MINOR_BURNS						(Msg.getString("ComplaintType.minorBurns")), //$NON-NLS-1$
	PANIC_ATTACK					(Msg.getString("ComplaintType.panicAttack")), //$NON-NLS-1$
	PULLED_MUSCLE_TENDON			(Msg.getString("ComplaintType.pulledMuscleTendon")), //$NON-NLS-1$
	RADIATION_SICKNESS				(Msg.getString("ComplaintType.radiationSickness")), //$NON-NLS-1$
	RUPTURED_APPENDIX				(Msg.getString("ComplaintType.rupturedAppendix")), //$NON-NLS-1$
	SUICIDE							(Msg.getString("ComplaintType.suicide")) //$NON-NLS-1$
	;

	private String name;

	private ComplaintType(String name) {
		this.name = name;
	}

	public String getName() {
		// TODO change all names to i18n-keys for accessing messages.properties
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
