/*
 * Mars Simulation Project
 * CropCategoryType.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function.farming;

public enum CropCategoryType {
	
	BULBS("bulbs"),
	CORMS("corms"),
	FLOWERS("flowers"),
	FRUITS("fruits"),
	
	FUNGI("fungi"),
	GRAINS("grains"),
	GRASSES("grasses"),
	LEAVES("leaves"),
	
	LEGUMES("legumes"),
	ROOTS("roots"),
	SEEDS("seeds"),
	//SPICES("spices"),
	
	STEMS("stems"),
	TUBERS("tubers");
	

	private String name;

	private CropCategoryType(String name) {
		this.name = name;
	}	

	public String getName() {
		// TODO change all names to i18n-keys for accessing messages.properties
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
