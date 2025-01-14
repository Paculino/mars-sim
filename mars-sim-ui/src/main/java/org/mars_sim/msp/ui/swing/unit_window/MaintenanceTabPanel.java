/*
 * Mars Simulation Project
 * MaintenanceTabPanel.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.Conversion;

/**
 * The MaintenanceTabPanel is a tab panel for unit maintenance information.
 */
@SuppressWarnings("serial")
public class MaintenanceTabPanel extends TabPanel {
	
	private static final String SPANNER_ICON = Msg.getString("icon.spanner"); //$NON-NLS-1$

    private int wearConditionCache; // The cached value for the wear condition.
    private int lastCompletedTime; // The time since last completed maintenance.
    
    private JLabel wearConditionLabel; // The wear condition label.
    private JLabel lastCompletedLabel; // The last completed label.
    private JLabel partsLabel; // Label for showing maintenance parts list.
    private JPanel malfunctionListPanel; // Malfunction list panel.
   
    private BoundedRangeModel progressBarModel; // The progress bar model.
    
    private Collection<MalfunctionPanel> malfunctionPanels; // List of malfunction panels.
    private Collection<Malfunction> malfunctionCache; // List of malfunctions.

    /**
     * Constructor.
     *
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public MaintenanceTabPanel(Unit unit, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super(
        	Msg.getString("MaintenanceTabPanel.title"),
        	Msg.getString("MaintenanceTabPanel.title"), 
        	ImageLoader.getNewIcon(SPANNER_ICON), 
        	Msg.getString("MaintenanceTabPanel.title"),
        	unit, desktop
        );
	}
	
    @Override
    protected void buildUI(JPanel content) {
        Malfunctionable malfunctionable = (Malfunctionable) getUnit();
        MalfunctionManager manager = malfunctionable.getMalfunctionManager();

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

        // Create maintenance panel
        JPanel maintenancePanel = new JPanel(new GridLayout(6, 1, 0, 0));
        northPanel.add(maintenancePanel);
        content.add(northPanel, BorderLayout.NORTH);

        // Create wear condition label.
        wearConditionCache = (int) Math.round(manager.getWearCondition());
        wearConditionLabel = new JLabel("Condition: " + wearConditionCache +
                "%", JLabel.CENTER);
        wearConditionLabel.setToolTipText("The health condition due to wear & tear : 100% = new; 0% = worn out");
        maintenancePanel.add(wearConditionLabel);

        // Create lastCompletedLabel.
        lastCompletedTime = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
        lastCompletedLabel = new JLabel("Last Completed: " + lastCompletedTime +
            " sols", JLabel.CENTER);
        maintenancePanel.add(lastCompletedLabel);

        // Create maintenance progress bar panel.
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        maintenancePanel.add(progressPanel);

        // Prepare maintenance parts label.
        partsLabel = new JLabel(getPartsString(false), JLabel.CENTER);
        partsLabel.setPreferredSize(new Dimension(-1, -1));
        maintenancePanel.add(partsLabel);

        // Prepare progress bar.
        JProgressBar progressBar = new JProgressBar();
        progressBarModel = progressBar.getModel();
        progressBar.setStringPainted(true);
        progressPanel.add(progressBar);

        // Set initial value for progress bar.
        double completed = manager.getMaintenanceWorkTimeCompleted();
        double total = manager.getMaintenanceWorkTime();
        int percentDone = (int) (100D * (completed / total));
        progressBarModel.setValue(percentDone);

        // Prepare malfunction panel
        JPanel malfunctionPanel = new JPanel(new BorderLayout(0, 0));
        content.add(malfunctionPanel, BorderLayout.CENTER);

        // Create malfunctions label
        JLabel malfunctionsLabel = new JLabel("Malfunctions", JLabel.CENTER);
        malfunctionPanel.add(malfunctionsLabel, BorderLayout.NORTH);

        // Create scroll panel for malfunction list
        JScrollPane malfunctionScrollPanel = new JScrollPane();
        malfunctionScrollPanel.setPreferredSize(new Dimension(170, 120));
        malfunctionPanel.add(malfunctionScrollPanel, BorderLayout.CENTER);

        // Create malfunction list main panel.
        JPanel malfunctionListMainPanel = new JPanel(new BorderLayout(0, 0));
        malfunctionScrollPanel.setViewportView(malfunctionListMainPanel);

        // Create malfunction list panel
        malfunctionListPanel = new JPanel();
        malfunctionListPanel.setLayout(new BoxLayout(malfunctionListPanel, BoxLayout.Y_AXIS));
        malfunctionListMainPanel.add(malfunctionListPanel, BorderLayout.NORTH);

        // Create malfunction panels
        malfunctionCache = malfunctionable.getMalfunctionManager().getMalfunctions();
        malfunctionPanels = new ArrayList<>();
        Iterator<Malfunction> i = malfunctionCache.iterator();
        while (i.hasNext()) {
            MalfunctionPanel panel = new MalfunctionPanel(i.next(), null);
            malfunctionListPanel.add(panel);
            malfunctionPanels.add(panel);
        }
    }

    /**
     * Updates this panel.
     */
    @Override
    public void update() {
        Malfunctionable malfunctionable = (Malfunctionable) getUnit();
        MalfunctionManager manager = malfunctionable.getMalfunctionManager();

        // Update the wear condition label.
        int wearCondition = (int) Math.round(manager.getWearCondition());
        if (wearCondition != wearConditionCache) {
            wearConditionCache = wearCondition;
            wearConditionLabel.setText("Wear Condition: " + wearCondition + "%");
        }

        // Update last completed label.
        int lastComplete = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
        if (lastComplete != lastCompletedTime) {
            lastCompletedTime = lastComplete;
            lastCompletedLabel.setText("Last Completed: " + lastCompletedTime + " Sols");
        }

		// Update tool tip.
		lastCompletedLabel.setToolTipText(getToolTipString());

        // Update progress bar.
        double completed = manager.getMaintenanceWorkTimeCompleted();
        double total = manager.getMaintenanceWorkTime();
        int percentDone = (int) (100D * (completed / total));
        progressBarModel.setValue(percentDone);

        // Update parts label.
        partsLabel.setText(getPartsString(false));
        // Update tool tip.
		partsLabel.setToolTipText("<html>" + getPartsString(true) + "</html>");


        // Get list of malfunctions.
        Collection<Malfunction> malfunctions = manager.getMalfunctions();

        // Update malfunction panels if necessary.
        if (!malfunctionCache.equals(malfunctions)) {
            // Add malfunction panels for new malfunctions.
            Iterator<Malfunction> iter1 = malfunctions.iterator();
            while (iter1.hasNext()) {
                Malfunction malfunction = iter1.next();
                if (!malfunctionCache.contains(malfunction)) {
                    MalfunctionPanel panel = new MalfunctionPanel(malfunction, null);
                    malfunctionPanels.add(panel);
                    malfunctionListPanel.add(panel);
                }
            }

            // Remove malfunction panels for repaired malfunctions.
            Iterator<Malfunction> iter2 = malfunctionCache.iterator();
            while (iter2.hasNext()) {
                Malfunction malfunction = iter2.next();
                if (!malfunctions.contains(malfunction)) {
                    MalfunctionPanel panel = getMalfunctionPanel(malfunction);
                    if (panel != null) {
                        malfunctionPanels.remove(panel);
                        malfunctionListPanel.remove(panel);
                    }
                }
            }

            // Update malfunction cache.
            malfunctionCache = malfunctions;
        }

        // Have each malfunction panel update.
        Iterator<MalfunctionPanel> i = malfunctionPanels.iterator();
        while (i.hasNext()) i.next().updateMalfunctionPanel();
    }

    /**
     * Gets the parts string.
     * 
     * @return string.
     */
    private String getPartsString(boolean useHtml) {
    	Malfunctionable malfunctionable = (Malfunctionable) getUnit();
        StringBuilder buf = new StringBuilder("Needed Parts: ");

    	Map<Integer, Integer> parts = malfunctionable.getMalfunctionManager().getMaintenanceParts();
    	if (parts.size() > 0) {
    		Iterator<Integer> i = parts.keySet().iterator();
    		while (i.hasNext()) {
    			Integer id = i.next();
    			int number = parts.get(id);
    			Part p = ItemResourceUtil.findItemResource(id);
				if (useHtml) 
					buf.append("<br>");
				buf.append(number).append(" ").append(Conversion.capitalize(p.getName()));
				if (i.hasNext()) 
					buf.append(", ");
				else {
					buf.append(".");
					if (useHtml) buf.append("<br>");
				}
      		}
    	}
    	else buf.append("None.");

    	return buf.toString();
    }

	/**
	 * Creates multi-line tool tip text.
	 */
	private String getToolTipString() {
		StringBuilder result = new StringBuilder("<html>");
		result.append("The Last Complete Maintenance Was Done ")
			.append(lastCompletedTime)
			.append(" Sols Ago<br>")
			.append("</html>");
		return result.toString();
	}

    /**
     * Gets an existing malfunction panel for a given malfunction.
     *
     * @param malfunction the given malfunction
     * @return malfunction panel or null if none.
     */
    private MalfunctionPanel getMalfunctionPanel(Malfunction malfunction) {
        MalfunctionPanel result = null;

        Iterator<MalfunctionPanel> i = malfunctionPanels.iterator();
        while (i.hasNext()) {
            MalfunctionPanel panel = i.next();
            if (panel.getMalfunction() == malfunction) 
            	result = panel;
        }

        return result;
    }
}
