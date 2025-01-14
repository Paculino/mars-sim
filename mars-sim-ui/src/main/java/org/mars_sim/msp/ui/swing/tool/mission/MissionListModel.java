/*
 * Mars Simulation Project
 * MissionListModel.java
 * @date 2021-12-03
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractListModel;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionManagerListener;

/**
 * List model for the mission list.
 */
@SuppressWarnings("serial")
public class MissionListModel extends AbstractListModel<Mission>
implements MissionManagerListener, MissionListener {

	// Private members.
	private List<Mission> missions;

	private MissionWindow missionWindow;
	private static MissionManager missionManager;


	/**
	 * Constructor.
	 */
	public MissionListModel(MissionWindow missionWindow) {
		this.missionWindow = missionWindow;

		missions = new CopyOnWriteArrayList<>();

		missionManager = Simulation.instance().getMissionManager();

		// Add list as mission manager listener.
		missionManager.addListener(this);
	}


	/**
	 * Populates the mission list
	 *
	 * @param settlement
	 */
	public void populateMissions() {
		// Check for null, needed when exiting the sim while Mission Tool is still open.
		if (missions == null)
			return;

		Iterator<Mission> i = missions.iterator();
		while (i.hasNext()) {
			removeMission(i.next());
		}

		// Add all current missions.
		Iterator<Mission> ii = missionManager.getMissions().iterator();
		while (ii.hasNext()) {
			Mission mission = ii.next();
			if (!missions.contains(mission)) {
				addMission(mission);
			}
		}
	}

	/**
	 * Adds a mission to this list.
	 *
	 * @param mission {@link Mission} the mission to add.
	 */
	@Override
	public void addMission(Mission mission) {
		if (!missions.contains(mission)
				&& missionWindow.getSettlement() != null
				&& missionWindow.getSettlement().equals(mission.getAssociatedSettlement())) {
			missions.add(mission);
			mission.addMissionListener(this);
			SwingUtilities.invokeLater(new MissionListUpdater(MissionListUpdater.ADD, this, missions.size() - 1));
		}
	}

	/**
	 * Removes a mission from this list.
	 *
	 * @param mission {@link Mission} mission to remove.
	 */
	@Override
	public void removeMission(Mission mission) {
		if (missions.contains(mission)
				&& missionWindow.getSettlement() != null
				// Make sure the mission of this settlement will NOT be deleted
				&& !missionWindow.getSettlement().equals(mission.getAssociatedSettlement())) {
			int index = missions.indexOf(mission);
			missions.remove(mission);
			mission.removeMissionListener(this);
			SwingUtilities.invokeLater(new MissionListUpdater(MissionListUpdater.REMOVE, this, index));
		}
	}

	/**
	 * Catch mission update event.
	 *
	 * @param event the mission event.
	 */
	@Override
	public void missionUpdate(MissionEvent event) {
		MissionEventType eventType = event.getType();
		if (eventType == MissionEventType.DESIGNATION_EVENT
				|| eventType == MissionEventType.PHASE_EVENT
				|| eventType == MissionEventType.PHASE_DESCRIPTION_EVENT) {
			int index = missions.indexOf(event.getSource());
			if ((index > -1) && (index < missions.size())) {
				SwingUtilities.invokeLater(new MissionListUpdater(MissionListUpdater.CHANGE, this, index));
			}
		}
	}

	/**
	 * Gets the list size.
	 *
	 * @return size.
	 */
	@Override
	public int getSize() {
		return missions.size();
	}

	/**
	 * Gets the list element at a given index.
	 *
	 * @param index the index.
	 * @return the object at the index or null if one.
	 */
	@Override
	public Mission getElementAt(int index) {
		try {
			return missions.get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * Checks if the list contains a given mission.
	 *
	 * @param mission the mission to check for.
	 * @return true if list contains the mission.
	 */
	public boolean containsMission(Mission mission) {
		return (missions != null) && missions.contains(mission);
	}

	/**
	 * Gets the index a given mission is at.
	 *
	 * @param mission the mission to check for.
	 * @return the index for the mission or -1 if not in list.
	 */
	public int getMissionIndex(Mission mission) {
		if (containsMission(mission))
			return missions.indexOf(mission);
		else
			return -1;
	}

	/**
	 * Prepares the list for deletion.
	 */
	public void destroy() {
		missions.clear();
		missions = null;
		missionManager.removeListener(this);
		missionManager = null;
	}

	/**
	 * Inner class for updating the mission list.
	 */
	private class MissionListUpdater implements Runnable {

		private static final int ADD = 0;
		private static final int REMOVE = 1;
		private static final int CHANGE = 2;

		private int mode;
		private MissionListModel model;
		private int row;

		private MissionListUpdater(int mode, MissionListModel model, int row) {
			this.mode = mode;
			this.model = model;
			this.row = row;
		}

		public void run() {
			switch (mode) {
			case ADD: {
				fireIntervalAdded(model, row, row);
			}
				break;
			case REMOVE: {
				fireIntervalRemoved(model, row, row);
			}
				break;
			case CHANGE: {
				fireContentsChanged(model, row, row);
			}
				break;
			}
		}
	}
}
