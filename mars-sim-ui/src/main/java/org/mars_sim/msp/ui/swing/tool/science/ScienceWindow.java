/**
 * Mars Simulation Project
 * ScienceWindow.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.science;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;

import com.alee.laf.panel.WebPanel;

/**
 * Window for the science tool.
 */
public class ScienceWindow
extends ToolWindow {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Tool name. */
	public static final String NAME = Msg.getString("ScienceWindow.title"); //$NON-NLS-1$

	// Data members
	private AbstractStudyListPanel ongoingStudyListPane;
	private AbstractStudyListPanel finishedStudyListPane;
	private StudyDetailPanel studyDetailPane;
	private ScientificStudy selectedStudy;

	/**
	 * Constructor
	 * @param desktop the main desktop panel.
	 */
	@SuppressWarnings("serial")
	public ScienceWindow(MainDesktopPane desktop) {

		// Use ToolWindow constructor
		super(NAME, desktop);

		selectedStudy = null;

		// Create content panel.
		WebPanel mainPane = new WebPanel(new BorderLayout());
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mainPane);

		// Create lists panel.
		WebPanel listsPane = new WebPanel(new GridLayout(2, 1));
		mainPane.add(listsPane, BorderLayout.WEST);

		ScientificStudyManager mgr = Simulation.instance().getScientificStudyManager();
		
		// Create ongoing study list panel.
		ongoingStudyListPane = new AbstractStudyListPanel(this, "OngoingStudyListPanel") {		
			@Override
			protected List<ScientificStudy> getStudies() {
				return mgr.getOngoingStudies();
			}
		};
		listsPane.add(ongoingStudyListPane);

		// Create finished study list panel.
		finishedStudyListPane = new AbstractStudyListPanel(this, "FinishedStudyListPanel") {		
			@Override
			protected List<ScientificStudy> getStudies() {
				return mgr.getCompletedStudies();
			}
		};
		listsPane.add(finishedStudyListPane);

		// Create study detail panel.
		studyDetailPane = new StudyDetailPanel(this);
		mainPane.add(studyDetailPane, BorderLayout.CENTER);

		//if (desktop.getMainScene() != null)
			//setClosable(false);

		setMinimumSize(new Dimension(480, 480));
		setMaximizable(true);
		setResizable(false);
		setVisible(true);
		
		// Pack window.
		pack();
	}

	/**
	 * Sets the scientific study to display in the science window.
	 * @param study the scientific study to display.
	 */
	public void setScientificStudy(ScientificStudy study) {
		selectedStudy = study;
		if (studyDetailPane.displayScientificStudy(study)) {
			ongoingStudyListPane.selectScientificStudy(study, true);
			finishedStudyListPane.selectScientificStudy(study, true);
		}
	}

	/**
	 * Gets the displayed scientific study.
	 * @return study or null if none displayed.
	 */
	public ScientificStudy getScientificStudy() {
		return selectedStudy;
	}

	/**
	 * Update the window.
	 */
	@Override
	public void update() {
		// Update all of the panels.
		ongoingStudyListPane.update();
		finishedStudyListPane.update();
		studyDetailPane.update();
	}

	/**
	 * Opens an info window for researcher.
	 * @param researcher the researcher.
	 */
	void openResearcherWindow(Person researcher) {
		desktop.openUnitWindow(researcher, false);
	}
}
