/*
 * Mars Simulation Project
 * BuildingPanelMaintenance.java
 * @date 2022-08-02
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.resource.MaintenanceScope;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.PartConfig;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.MalfunctionPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

/**
 * The BuildingPanelMaintenance class is a building function panel representing
 * the maintenance state of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelMaintenance extends BuildingFunctionPanel {

	private static final String SPANNER_ICON = Msg.getString("icon.spanner"); //$NON-NLS-1$

	/** Cached value for the wear condition. */
	private int wearConditionCache;
	/** The time since last completed maintenance. */
	private int lastCompletedTime;

	/** The malfunction manager instance. */
	private MalfunctionManager manager;
	
	/** The wear condition label. */
	private WebLabel wearConditionLabel;
	/** The last completed label. */
	private JLabel lastCompletedLabel;
	/** Label for parts. */
	private JLabel partsLabel;

	/** The progress bar model. */
	private BoundedRangeModel progressBarModel;
	/** The parts table model. */
	private PartTableModel tableModel;
	/** The parts table. */
	private JTable table;
	
	/** Parts for maintenance **/
	private Map<Part, List<String>> standardMaintParts;

	private static PartConfig partConfig = SimulationConfig.instance().getPartConfiguration();

	/**
	 * Constructor.
	 * 
	 * @param malfunctionable the malfunctionable building the panel is for.
	 * @param desktop         The main desktop.
	 */
	public BuildingPanelMaintenance(Building malfunctionable, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelMaintenance.title"), 
			ImageLoader.getNewIcon(SPANNER_ICON), 
			malfunctionable, 
			desktop
		);

		// Initialize data members.
		manager = malfunctionable.getMalfunctionManager();
		standardMaintParts = getStandardMaintParts(malfunctionable);
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {
	
		WebPanel labelPanel = new WebPanel(new GridLayout(4, 1, 2, 1));
		add(labelPanel, BorderLayout.NORTH);
		
		// Create wear condition label.
		int wearConditionCache = (int) Math.round(manager.getWearCondition());
		wearConditionLabel = new WebLabel(Msg.getString("BuildingPanelMaintenance.wearCondition", wearConditionCache),
				JLabel.CENTER);
		wearConditionLabel.setToolTipText(Msg.getString("BuildingPanelMaintenance.wear.toolTip"));
		labelPanel.add(wearConditionLabel);

		// Create lastCompletedLabel.
		lastCompletedTime = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
		lastCompletedLabel = new JLabel(Msg.getString("BuildingPanelMaintenance.lastCompleted", lastCompletedTime),
				JLabel.CENTER);
		labelPanel.add(lastCompletedLabel);

		// Create maintenance progress bar panel.
		JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		labelPanel.add(progressPanel);
		progressPanel.setOpaque(false);
		progressPanel.setBackground(new Color(0, 0, 0, 128));

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

		// Prepare maintenance parts label.
		partsLabel = new JLabel(getPartsString(false), JLabel.CENTER);
		partsLabel.setPreferredSize(new Dimension(-1, -1));
		labelPanel.add(partsLabel);
		
		// Create the parts panel
		WebScrollPane partsPane = new WebScrollPane();
		WebPanel tablePanel = new WebPanel();
		tablePanel.add(partsPane);
		center.add(tablePanel, BorderLayout.CENTER);
		addBorder(tablePanel, Msg.getString("BuildingPanelMaintenance.tableBorder"));
		
		// Create the parts table model
		tableModel = new PartTableModel();

		// Create the parts table
		table = new ZebraJTable(tableModel);
		table.setPreferredScrollableViewportSize(new Dimension(220, 125));
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		partsPane.setViewportView(table);

		table.setDefaultRenderer(Double.class, new NumberCellRenderer(2, true));

		table.getColumnModel().getColumn(0).setPreferredWidth(110);
		table.getColumnModel().getColumn(1).setPreferredWidth(90);
		table.getColumnModel().getColumn(2).setPreferredWidth(30);
		table.getColumnModel().getColumn(3).setPreferredWidth(40);

		DefaultTableCellRenderer renderer1 = new DefaultTableCellRenderer();
		renderer1.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(2).setCellRenderer(renderer1);
		table.getColumnModel().getColumn(3).setCellRenderer(renderer1);
		
		// Align the preference score to the center of the cell
		DefaultTableCellRenderer renderer2 = new DefaultTableCellRenderer();
		renderer2.setHorizontalAlignment(SwingConstants.LEFT);
		table.getColumnModel().getColumn(0).setCellRenderer(renderer2);
		table.getColumnModel().getColumn(1).setCellRenderer(renderer2);

		// Added sorting
		table.setAutoCreateRowSorter(true);

		// Added setTableStyle()
		TableStyle.setTableStyle(table);
	}

	/**
	 * Update this panel
	 */
	@Override
	public void update() {

		// Update the wear condition label.
		int wearCondition = (int) Math.round(manager.getWearCondition());
		if (wearCondition != wearConditionCache) {
			wearConditionCache = wearCondition;
			wearConditionLabel.setText(Msg.getString("BuildingPanelMaintenance.wearCondition", wearConditionCache));
		}

		// Update last completed label.
		int lastComplete = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
		if (lastComplete != lastCompletedTime) {
			lastCompletedTime = lastComplete;
			lastCompletedLabel.setText(Msg.getString("BuildingPanelMaintenance.lastCompleted", lastCompletedTime));
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

	}

	/**
	 * Gets the parts string.
	 * 
	 * @return string.
	 */
	private String getPartsString(boolean useHtml) {
		return MalfunctionPanel.getPartsString(manager.getMaintenanceParts(), useHtml).toString();
	}

	/**
	 * Creates multi-line tool tip text.
	 */
	private String getToolTipString() {
		StringBuilder result = new StringBuilder("<html>");
		result.append("The Very Last Maintenance Was Completed ").append(lastCompletedTime).append(" Sols Ago<br>");
		result.append("</html>");
		return result.toString();
	}

	/**
	 * Internal class used as model for the equipment table.
	 */
	private class PartTableModel extends AbstractTableModel {

		private static final String WHITESPACE = " ";

		private int size;
		
		private List<Part> parts = new ArrayList<>();
		private List<String> functions = new ArrayList<>();
		private List<Integer> max = new ArrayList<>();
		private List<Double> probability = new ArrayList<>();

		/**
		 * hidden constructor.
		 * 
		 * @param inventory {@link Inventory}
		 */
		private PartTableModel() {
			
			size = standardMaintParts.size();
			
			for (Part p: standardMaintParts.keySet()) {

				List<String> fList = standardMaintParts.get(p);
				for (MaintenanceScope me: partConfig.getMaintenance(fList, p)) {
					parts.add(p);
					functions.add(Conversion.capitalize(me.getName()));
					max.add(me.getMaxNumber());
					probability.add(me.getProbability());
				}
			}		
		}

		public int getRowCount() {
			return size;
		}

		public int getColumnCount() {
			return 4;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0)
				dataType = String.class;
			else if (columnIndex == 1)
				dataType = String.class;
			else if (columnIndex == 2)
				dataType = Integer.class;
			else if (columnIndex == 3)
				dataType = Double.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return Msg.getString("BuildingPanelMaintenance.header.part"); //$NON-NLS-1$
			else if (columnIndex == 1)
				return Msg.getString("BuildingPanelMaintenance.header.function"); //$NON-NLS-1$
			else if (columnIndex == 2)
				return Msg.getString("BuildingPanelMaintenance.header.max"); //$NON-NLS-1$
			else if (columnIndex == 3)
				return Msg.getString("BuildingPanelMaintenance.header.probability"); //$NON-NLS-1$
			else
				return "unknown";
		}

		public Object getValueAt(int row, int column) {
			if (parts != null && row >= 0 && row < parts.size()) {
				if (column == 0)
					return WHITESPACE + Conversion.capitalize(parts.get(row).getName()) + WHITESPACE;
				else if (column == 1)
					return Conversion.capitalize(functions.get(row));
				else if (column == 2)
					return max.get(row);
				else if (column == 3)
					return probability.get(row);
			}
			return "unknown";
		}
	}
	
	/**
	 * Gets the standard parts to be maintained by this entity
	 * 
	 * @return
	 */
	private static Map<Part, List<String>> getStandardMaintParts(Building building) {
		Set<String> scope = building.getFunctions().stream().map(f -> f.getFunctionType().getName())
										.collect(Collectors.toSet());
		
		Map<Part, List<String>> maint = new LinkedHashMap<>();
	
		for (MaintenanceScope maintenance : partConfig.getMaintenance(scope)) {
			Part part = maintenance.getPart();
			List<String> list = null;
			if (maint.containsKey(part)) {
				list = maint.get(part);
			}
			else {
				list = new CopyOnWriteArrayList<>();
			}			
			list.add(maintenance.getName());
			maint.put(part, list);	
		}
		
		Map<Part, List<String>> sortedMap = new LinkedHashMap<>();
				
		// Sort by the key
		maint.entrySet()
	    .stream()
	    .sorted(Map.Entry.comparingByKey())
	    .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
		
		return sortedMap;
	}
}
