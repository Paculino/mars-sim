/*
 * Mars Simulation Project
 * TabPanelPopulation.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitListPanel;

import com.alee.laf.panel.WebPanel;

/**
 * This is a tab panel for population information.
 */
@SuppressWarnings("serial")
public class TabPanelPopulation extends TabPanel {

	private static final String POP_ICON = Msg.getString("icon.pop"); //$NON-NLS-1$
	
	/** The Settlement instance. */
	private Settlement settlement;

	private JTextField populationIndoorLabel;
	private JTextField populationCapacityLabel;

	private UnitListPanel<Person> populationList;

	private int populationIndoorCache;
	private int populationCapacityCache;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelPopulation(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getNewIcon(POP_ICON),
			Msg.getString("TabPanelPopulation.title"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;

	}

	@Override
	protected void buildUI(JPanel content) {

		// Prepare count spring layout panel.
		WebPanel countPanel = new WebPanel(new SpringLayout());
		content.add(countPanel, BorderLayout.NORTH);

		// Create population indoor label
		populationIndoorCache = settlement.getIndoorPeopleCount();
		populationIndoorLabel = addTextField(countPanel, Msg.getString("TabPanelPopulation.indoor"),
											 populationIndoorCache, null);

		// Create population capacity label
		populationCapacityCache = settlement.getPopulationCapacity();
		populationCapacityLabel = addTextField(countPanel, Msg.getString("TabPanelPopulation.capacity"),
											   populationCapacityCache, null);

		SpringUtilities.makeCompactGrid(countPanel, 2, 2, // rows, cols
				INITX_DEFAULT, INITY_DEFAULT, // initX, initY
				XPAD_DEFAULT, YPAD_DEFAULT); // xPad, yPad
		
		// Create spring layout population display panel
		JPanel populationDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		addBorder(populationDisplayPanel, Msg.getString("TabPanelPopulation.TitledBorder"));
		content.add(populationDisplayPanel, BorderLayout.CENTER);

		// Create scroll panel for population list. new Dimension(200, 250)
		populationList = new UnitListPanel<>(getDesktop()) {
			@Override
			protected Collection<Person> getData() {
				return settlement.getIndoorPeople();
			}
		};
		populationDisplayPanel.add(populationList);
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {

		int num = settlement.getIndoorPeopleCount();
		// Update indoor num
		if (populationIndoorCache != num) {
			populationIndoorCache = num;
			populationIndoorLabel.setText(populationIndoorCache + "");
		}

		int cap = settlement.getPopulationCapacity();
		// Update capacity
		if (populationCapacityCache != cap) {
			populationCapacityCache = cap;
			populationCapacityLabel.setText(populationCapacityCache + "");
		}

		// Update population list
		populationList.update();
	}

	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		populationIndoorLabel = null;
		populationCapacityLabel = null;
		populationList = null;
	}
}
