/*
 * Mars Simulation Project
 * TabPanelMission.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextArea;

/**
 * Tab panel displaying vehicle mission info.
 */
@SuppressWarnings("serial")
public class TabPanelMission
extends TabPanel {
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TabPanelMission.class.getName());

	private static final String FLAG_ICON = Msg.getString("icon.flag"); //$NON-NLS-1$
	
	private WebTextArea missionTextArea;
	private WebTextArea missionPhaseTextArea;
	private DefaultListModel<Worker> memberListModel;
	private JList<Worker> memberList;
	private WebButton missionButton;
	private WebButton monitorButton;

	// Cache
	private String missionCache = null;
	private String missionPhaseCache = null;
	private Collection<Worker> memberCache;

	/** The Vehicle instance. */
	private Vehicle vehicle;

	/**
	 * Constructor.
	 * 
	 * @param vehicle the vehicle.
	 * @param desktop the main desktop.
	 */
	public TabPanelMission(Vehicle vehicle, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelMission.title"), //$NON-NLS-1$
			ImageLoader.getNewIcon(FLAG_ICON),
			Msg.getString("TabPanelMission.title"), //$NON-NLS-1$
			vehicle, desktop
		);

      this.vehicle = vehicle;

	}

	@Override
	protected void buildUI(JPanel topContentPanel) {
		MissionManager missionManager = getSimulation().getMissionManager();

		Mission mission = missionManager.getMissionForVehicle(vehicle);

		// Prepare mission top panel
		WebPanel missionTopPanel = new WebPanel(new GridLayout(2, 1, 0, 0));
		addBorder(missionTopPanel, "missionTopPanel");
		missionTopPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		topContentPanel.add(missionTopPanel, BorderLayout.NORTH);

		// Prepare mission panel
		WebPanel missionPanel = new WebPanel(new BorderLayout(0, 0));
		missionPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		missionTopPanel.add(missionPanel);

		// Prepare mission text area
		if (mission != null) missionCache = mission.getDescription();
		missionTextArea = new WebTextArea(2, 20);
		if (missionCache != null) missionTextArea.setText(missionCache);
		missionTextArea.setLineWrap(true);
		missionTextArea.setEditable(false);
		missionPanel.add(new WebScrollPane(missionTextArea), BorderLayout.NORTH);

		// Prepare mission phase panel
		WebPanel missionPhasePanel = new WebPanel(new BorderLayout(0, 0));
		missionPhasePanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		missionTopPanel.add(missionPhasePanel);

		// Prepare mission phase label
		WebLabel missionPhaseLabel = new WebLabel(Msg.getString("TabPanelMission.missionPhase"), SwingConstants.CENTER); //$NON-NLS-1$
		missionPhaseLabel.setFont(TITLE_FONT);
		missionPhasePanel.add(missionPhaseLabel, BorderLayout.NORTH);
		missionTopPanel.add(missionPhasePanel);
		
		// Prepare mission phase text area
		if (mission != null) missionPhaseCache = mission.getPhaseDescription();
		missionPhaseTextArea = new WebTextArea(2, 20);
		if (missionPhaseCache != null) missionPhaseTextArea.setText(missionPhaseCache);
		missionPhaseTextArea.setLineWrap(true);
		missionPhaseTextArea.setEditable(false);
		missionPhasePanel.add(new WebScrollPane(missionPhaseTextArea), BorderLayout.CENTER);

		// Prepare mission bottom panel
		WebPanel missionBottomPanel = new WebPanel(new BorderLayout(0, 0));
		missionBottomPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		topContentPanel.add(missionBottomPanel, BorderLayout.CENTER);

		// Prepare member label
		WebLabel memberLabel = new WebLabel(Msg.getString("TabPanelMission.members"), SwingConstants.CENTER); //$NON-NLS-1$
		memberLabel.setFont(new Font("Serif", Font.BOLD, 14));
		missionBottomPanel.add(memberLabel, BorderLayout.NORTH);

		// Prepare member list panel
		WebPanel memberListPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT));
		missionBottomPanel.add(memberListPanel, BorderLayout.CENTER);

		// Create scroll panel for member list.
		WebScrollPane memberScrollPanel = new WebScrollPane();
		memberScrollPanel.setPreferredSize(new Dimension(225, 100));
		memberListPanel.add(memberScrollPanel);

		// Create member list model
		memberListModel = new DefaultListModel<>();
		if (mission != null) memberCache = mission.getMembers();
		else memberCache = new ConcurrentLinkedQueue<>();
		Iterator<Worker> i = memberCache.iterator();
		while (i.hasNext()) memberListModel.addElement(i.next());

		// Create member list
		memberList = new JList<>(memberListModel);
		// memberList.addMouseListener(this);
		memberList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				// If double-click, open person dialog.
				if (arg0.getClickCount() >= 2)
					getDesktop().openUnitWindow((Unit) memberList.getSelectedValue(), false);
			}
		});
		memberScrollPanel.setViewportView(memberList);

		WebPanel buttonPanel = new WebPanel(new GridLayout(2, 1, 5, 5));
//		buttonPanel.setBorder(new MarsPanelBorder());
		memberListPanel.add(buttonPanel);

		Unit unit = getUnit();
		
		// Create mission tool button
		missionButton = new WebButton(ImageLoader.getIcon(Msg.getString("img.mission"))); //$NON-NLS-1$
		missionButton.setMargin(new Insets(2, 2, 2, 2));
		missionButton.setToolTipText(Msg.getString("TabPanelMission.tooltip.mission")); //$NON-NLS-1$
		missionButton.addActionListener(e -> {
				Vehicle vehicle = (Vehicle) unit;
				Mission m = missionManager.getMissionForVehicle(vehicle);
				if (m != null) {
					getDesktop().openToolWindow(MissionWindow.NAME, m);
				}
		});
		missionButton.setEnabled(mission != null);
		buttonPanel.add(missionButton);

		// Create member monitor button
		monitorButton = new WebButton(ImageLoader.getIcon(Msg.getString("img.monitor"))); //$NON-NLS-1$
		monitorButton.setMargin(new Insets(2, 2, 2, 2));
		monitorButton.setToolTipText(Msg.getString("TabPanelMission.tooltip.monitor")); //$NON-NLS-1$
		monitorButton.addActionListener(e -> {
				Vehicle vehicle = (Vehicle) unit;
				Mission m = missionManager.getMissionForVehicle(vehicle);
				if (m != null) {
					try {
						getDesktop().addModel(new PersonTableModel(m));
					} catch (Exception ex) {
						logger.severe("PersonTableModel cannot be added.");
					}
				}
		});
		monitorButton.setEnabled(mission != null);
		buttonPanel.add(monitorButton);
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		Vehicle vehicle = (Vehicle) getUnit();
		Mission mission = getSimulation().getMissionManager().getMissionForVehicle(vehicle);

		if (mission != null) {
		    missionCache = mission.getDescription();
		}
		else {
		    missionCache = null;
		}
		if (!missionTextArea.getText().equals(missionCache)) {
			missionTextArea.setText(missionCache);
		}

		if (mission != null) {
		    missionPhaseCache = mission.getPhaseDescription();
		}
		else {
		    missionPhaseCache = null;
		}
		if (!missionPhaseTextArea.getText().equals(missionPhaseCache)) {
			missionPhaseTextArea.setText(missionPhaseCache);
		}

		// Update member list
		Collection<Worker> tempCollection = null;
		if (mission != null) {
		    tempCollection = mission.getMembers();
		}
		else {
		    tempCollection = new ConcurrentLinkedQueue<>();
		}
		if (!Arrays.equals(memberCache.toArray(), tempCollection.toArray())) {
			memberCache = tempCollection;
			memberListModel.clear();
			Iterator<Worker> i = memberCache.iterator();
			while (i.hasNext()) {
			    memberListModel.addElement(i.next());
			}
		}

		// Update mission and monitor buttons.
		missionButton.setEnabled(mission != null);
		monitorButton.setEnabled(mission != null);
	}

	@Override
	public void destroy() {
		super.destroy();
		
		missionTextArea = null;
		missionPhaseTextArea = null;
		memberListModel = null;
		memberList = null;
		missionButton = null;
		monitorButton = null;
		memberCache = null;
	}
}
