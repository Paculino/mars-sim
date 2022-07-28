/**
 * Mars Simulation Project
 * VehicleWindow.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import javax.swing.event.ChangeEvent;

import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.InventoryTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.LocationTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.MaintenanceTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.NotesTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.SalvageTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

/**
 * The VehicleWindow is the window for displaying a vehicle.
 */
@SuppressWarnings("serial")
public class VehicleWindow extends UnitWindow {

	// Data members
	private boolean salvaged;

	private Vehicle vehicle;

	/**
	 * Constructor
	 *
	 * @param desktop the main desktop panel.
	 * @param vehicle the vehicle for this window.
	 */
	public VehicleWindow(MainDesktopPane desktop, Vehicle vehicle) {
		// Use UnitWindow constructor
		super(desktop, vehicle, vehicle.getAssociatedSettlement().getName() + " - " + vehicle.getNickName(), true);
		this.vehicle = vehicle;

		// Add tab panels
		if (vehicle instanceof Crewable) {
			Crewable crewableVehicle = (Crewable) vehicle;
			if (crewableVehicle.getCrewCapacity() > 0)
				addTabPanel(new TabPanelCrew(vehicle, desktop));
			else if (crewableVehicle.getRobotCrewCapacity() > 0)
				addTabPanel(new TabPanelBots(vehicle, desktop));
		}

		addTabPanel(new InventoryTabPanel(vehicle, desktop));

		if (vehicle instanceof Rover) {

			addTabPanel(new TabPanelEVA(vehicle, desktop));
			addTabPanel(new NavigationTabPanel(vehicle, desktop));
						
			Rover rover = (Rover) vehicle;
			if (rover.hasLab())
				addTabPanel(new LaboratoryTabPanel(rover, desktop));		
			// Future: Add sickbay tab panel.
		}		
		else {
			addTabPanel(new NavigationTabPanel(vehicle, desktop));
		}

		addTopPanel(new LocationTabPanel(vehicle, desktop));
		addTopPanel(new TabPanelLog(vehicle, desktop));
		addTabPanel(new MaintenanceTabPanel(vehicle, desktop));
		addTabPanel(new TabPanelMission(vehicle, desktop));	
		addTabPanel(new NotesTabPanel(vehicle, desktop));

		salvaged = vehicle.isSalvaged();
		if (salvaged)
			addTabPanel(new SalvageTabPanel(vehicle, desktop));

		addTabPanel(new TabPanelTow(vehicle, desktop));

//		sortTabPanels();
		// Add to tab panels. 
		addTabIconPanels();
	}

	/**
	 * Updates this window.
	 */
	@Override
	public void update() {
		super.update();
		// Check if equipment has been salvaged.
		// Vehicle vehicle = (Vehicle) getUnit();
		if (!salvaged && vehicle.isSalvaged()) {
			addTabPanel(new SalvageTabPanel(vehicle, desktop));
			salvaged = true;
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		vehicle = null;
	}

    @Override
	public void stateChanged(ChangeEvent e) {
//		// SwingUtilities.updateComponentTreeUI(this);
//		TabPanel newTab = getSelected();
//
//		if (oldTab == null || newTab != oldTab) {
//			oldTab = newTab;
//			
//			if (!newTab.isUIDone());
//				newTab.initializeUI();		
//		}
	}
}
