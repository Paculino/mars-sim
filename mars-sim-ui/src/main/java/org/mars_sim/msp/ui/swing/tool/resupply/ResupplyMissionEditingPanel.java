/*
 * Mars Simulation Project
 * ResupplyMissionEditingPanel.java
 * @date 2022-07-19
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.ButtonGroup;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.mars_sim.msp.core.BoundedObject;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.interplanetary.transport.TransitState;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MarsClockFormat;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.resupply.SupplyTableModel.SupplyItem;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.radiobutton.WebRadioButton;
import com.alee.laf.scroll.WebScrollPane;


/**
 * A panel for creating or editing a resupply mission.
 */
@SuppressWarnings("serial")
public class ResupplyMissionEditingPanel extends TransportItemEditingPanel {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ResupplyMissionEditingPanel.class.getName());
			
	private static final Integer[] EMPTY_STRING_ARRAY = new Integer[0];
	private static final int MAX_FUTURE_ORBITS = 20;
	private static final int MAX_IMMIGRANTS = 48;
	private static final int MAX_BOTS = 48;
	private static final int MILLISOLS_DELAY = 10;

	// Data members
	private String errorString = new String();
	private boolean validation_result = true;
	private Integer[] solsUntil = new Integer[ResupplyUtil.MAX_NUM_SOLS_PLANNED];
	private Number[] quantity = new Number[100000];
	private Integer[] immigrants = new Integer[MAX_IMMIGRANTS];
	private Integer[] bots = new Integer[MAX_BOTS];
	
	private JComboBoxMW<Settlement> destinationCB;
	private WebRadioButton arrivalDateRB;
	private WebRadioButton solsUntilArrivalRB;
	private MartianSolComboBoxModel martianSolCBModel;
	private WebLabel arrivalDateTitleLabel;
	private WebLabel solsUntilArrivalLabel;
	private WebLabel solLabel;
	private WebLabel monthLabel;
	private WebLabel orbitLabel;
	private WebLabel solInfoLabel;
	private WebLabel errorLabel;
	private JComboBoxMW<?> solsUntilCB, immigrantsCB, botsCB, monthCB, orbitCB, solCB;
	private SupplyTableModel supplyTableModel;
	private JTable supplyTable;
	private WebButton removeSupplyButton;

	private Resupply resupply;
	private NewTransportItemDialog newTransportItemDialog = null;
	private ModifyTransportItemDialog modifyTransportItemDialog = null;
	private ResupplyWindow resupplyWindow;

	private MarsClock marsCurrentTime;

	protected static MarsClock marsClock = Simulation.instance().getMasterClock().getMarsClock();
	protected static UnitManager unitManager = Simulation.instance().getUnitManager();

	/** constructor. */
	public ResupplyMissionEditingPanel(Resupply resupply, ResupplyWindow resupplyWindow,
			ModifyTransportItemDialog modifyTransportItemDialog, NewTransportItemDialog newTransportItemDialog) {
		// User TransportItemEditingPanel constructor.
		super(resupply);

		// Initialize data members.
		this.resupply = resupply;
		this.newTransportItemDialog = newTransportItemDialog;
		this.modifyTransportItemDialog = modifyTransportItemDialog;
		this.resupplyWindow = resupplyWindow;

		setBorder(new MarsPanelBorder());
		setLayout(new BorderLayout(0, 0));

		// Create top edit pane.
		WebPanel topEditPane = new WebPanel(new BorderLayout(10, 10));
		add(topEditPane, BorderLayout.NORTH);

		// Create destination pane.
		WebPanel destinationPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		topEditPane.add(destinationPane, BorderLayout.NORTH);

		// Create destination title label.
		WebLabel destinationTitleLabel = new WebLabel("Destination : ");
		destinationPane.add(destinationTitleLabel);

		// Create destination combo box.
		Vector<Settlement> settlements = new Vector<>(
				unitManager.getSettlements());
		Collections.sort(settlements);
		destinationCB = new JComboBoxMW<>(settlements);
		if (resupply != null) {
			destinationCB.setSelectedItem(resupply.getSettlement());
		} else {
			// this.settlement = (Settlement) destinationCB.getSelectedItem();
		}
		destinationPane.add(destinationCB);

		// Create arrival date pane.
		WebPanel arrivalDatePane = new WebPanel(new GridLayout(4, 1, 10, 10));
		arrivalDatePane.setBorder(new TitledBorder("Arrival"));
		topEditPane.add(arrivalDatePane, BorderLayout.CENTER);

		// Create data type radio button group.
		ButtonGroup dateTypeRBGroup = new ButtonGroup();

		// Create arrival date selection pane.
		WebPanel arrivalDateSelectionPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		arrivalDatePane.add(arrivalDateSelectionPane);

		// Create arrival date radio button.
		arrivalDateRB = new WebRadioButton();
		dateTypeRBGroup.add(arrivalDateRB);
		arrivalDateRB.setSelected(true);
		arrivalDateRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				WebRadioButton rb = (WebRadioButton) evt.getSource();
				setEnableArrivalDatePane(rb.isSelected());
				setEnableTimeUntilArrivalPane(!rb.isSelected());
			}
		});
		arrivalDateSelectionPane.add(arrivalDateRB);

		// Create arrival date title label.
		arrivalDateTitleLabel = new WebLabel("Arrival Date : ");
		arrivalDateSelectionPane.add(arrivalDateTitleLabel);
		
		// Get default resupply Martian time.
		MarsClock resupplyTime = null;
		if (resupply != null) {
			resupplyTime = resupply.getArrivalDate();
		} else {
			resupplyTime = (MarsClock) marsClock.clone();
			resupplyTime.addTime(ResupplyUtil.getAverageTransitTime() * 1000D);
		}

		martianSolCBModel = new MartianSolComboBoxModel(resupplyTime.getMonth(), resupplyTime.getOrbit());

		WebPanel comboBoxPane = new WebPanel(new GridLayout(1, 6, 1, 1));
		comboBoxPane.setSize(150, 20);
		arrivalDateSelectionPane.add(comboBoxPane);
		
		// Create orbit label.
		orbitLabel = new WebLabel("Orbit :", SwingConstants.CENTER);
		comboBoxPane.add(orbitLabel);

		// Create orbit combo box.
		NumberFormat formatter = NumberFormat.getInstance();
		formatter.setMinimumIntegerDigits(2);
		String[] orbitValues = new String[MAX_FUTURE_ORBITS];
		int startOrbit = resupplyTime.getOrbit();
		for (int x = 0; x < MAX_FUTURE_ORBITS; x++) {
			orbitValues[x] = formatter.format(startOrbit + x);
		}
		orbitCB = new JComboBoxMW<>(orbitValues);
		orbitCB.setSelectedItem(formatter.format(startOrbit));
		orbitCB.addActionListener(e -> {
			// Update the solCB based on orbit and month			
			martianSolCBModel.updateSolNumber(monthCB.getSelectedIndex() + 1,
					Integer.parseInt((String) orbitCB.getSelectedItem()));
			// Remove error string
			errorString = null;
			errorLabel.setText(errorString);
			// Reenable Commit/Create button
			enableButton(true);
		});
		comboBoxPane.add(orbitCB);

		// Create month label.
		monthLabel = new WebLabel("Month :", SwingConstants.CENTER);
		comboBoxPane.add(monthLabel);

		// Create month combo box.
		monthCB = new JComboBoxMW<Object>(MarsClockFormat.getMonthNames());
		monthCB.setSelectedItem(resupplyTime.getMonthName());
		monthCB.addActionListener(e -> {
			// Update the solCB based on orbit and month
			martianSolCBModel.updateSolNumber(monthCB.getSelectedIndex() + 1,
						Integer.parseInt((String) orbitCB.getSelectedItem()));
			// Remove error string
			errorString = null;
			errorLabel.setText(errorString);
			// Reenable Commit/Create button
			enableButton(true);
		});
		comboBoxPane.add(monthCB);

		// Create sol label.
		solLabel = new WebLabel("Sol :", SwingConstants.CENTER);
		comboBoxPane.add(solLabel);

		// Create sol combo box.
		solCB = new JComboBoxMW<>(martianSolCBModel);
		solCB.setSelectedItem(resupplyTime.getSolOfMonth());
		comboBoxPane.add(solCB);

		// Create sol until arrival pane.
		WebPanel solsUntilArrivalPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		arrivalDatePane.add(solsUntilArrivalPane);

		// Create sol until arrival radio button.
		solsUntilArrivalRB = new WebRadioButton();
		solsUntilArrivalRB.setSelected(false);
		dateTypeRBGroup.add(solsUntilArrivalRB);
		solsUntilArrivalRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				WebRadioButton rb = (WebRadioButton) evt.getSource();
				setEnableTimeUntilArrivalPane(rb.isSelected());
				setEnableArrivalDatePane(!rb.isSelected());
				// Remove error string
				errorString = null;
				errorLabel.setText(errorString);
				// Reenable Commit/Create button
				enableButton(true);
			}
		});
		solsUntilArrivalPane.add(solsUntilArrivalRB);

		// create the sols until arrival label.
		solsUntilArrivalLabel = new WebLabel("Sols Until Arrival : ");
		solsUntilArrivalLabel.setEnabled(false);
		solsUntilArrivalPane.add(solsUntilArrivalLabel);

		// Create sols text field.
		int solsDiff = (int) Math.round((MarsClock.getTimeDiff(resupplyTime, marsClock) / 1000D));
		
		// Switch to using ComboBoxMW for sols
		int size = solsUntil.length;
		// int max = ResupplyUtil.MAX_NUM_SOLS_PLANNED;
		int t = ResupplyUtil.getAverageTransitTime();
		for (int i = t + 1; i < size + t + 1; i++) {
			if (i > t)
				solsUntil[i - t - 1] = i;
		}

		updateSolsUntilCB();
		solsUntilCB.setSelectedItem(solsDiff);
		solsUntilCB.requestFocus(false);
		solsUntilArrivalPane.add(solsUntilCB);

		// Create sol information label.
		solInfoLabel = new WebLabel("(668 Sols = 1 Martian Orbit for a non-leap year)");
		solInfoLabel.setEnabled(false);
		solsUntilArrivalPane.add(solInfoLabel);

		// Create sol information label.
		WebLabel limitLabel = new WebLabel("  Note : there is a minimum 10-msol delay for a resupply mission to be executed.");
		limitLabel.setEnabled(true);
		limitLabel.setForeground(new Color(139, 69, 19));
		arrivalDatePane.add(limitLabel);

		// Create error pane.
		WebPanel errorPane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		arrivalDatePane.add(errorPane);

		// Create error label
		errorLabel = new WebLabel(new String());
		errorLabel.setForeground(Color.RED);
		errorPane.add(errorLabel);

		////////////////////////////////////////////
		
		WebPanel immigrantsBotsPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		topEditPane.add(immigrantsBotsPane, BorderLayout.SOUTH);
		
		////////////////////////////////////////////
		
		// Create immigrants panel.
		WebPanel immigrantsPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		immigrantsBotsPane.add(immigrantsPane);

		// Create immigrants label.
		WebLabel immigrantsLabel = new WebLabel("Number of Immigrants : ");
		immigrantsPane.add(immigrantsLabel);

		// Create immigrants text field.
		int immigrantsNum = 0;
		if (resupply != null) {
			immigrantsNum = resupply.getNewImmigrantNum();
		}

		// Switch to using ComboBoxMW for immigrants
		int size1 = immigrants.length;
		for (int i = 0; i < size1; i++) {
			immigrants[i] = i;
		}
		immigrantsCB = new JComboBoxMW<>(immigrants);
		immigrantsCB.setSelectedItem(immigrantsNum);
		immigrantsPane.add(immigrantsCB);

		// Create bots panel.
		WebPanel botsPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		immigrantsBotsPane.add(botsPane);
		
		// Create bots label.
		WebLabel botsLabel = new WebLabel("Number of Bots : ");
		botsPane.add(botsLabel);

		// Create bots text field.
		int botsNum = 0;
		if (resupply != null) {
			botsNum = resupply.getNewBotNum();
		}

		// Switch to using ComboBoxMW for bots
		int size2 = bots.length;
		for (int i = 0; i < size2; i++) {
			bots[i] = i;
		}
		botsCB = new JComboBoxMW<>(bots);
		botsCB.setSelectedItem(botsNum);
		botsPane.add(botsCB);
		
		////////////////////////////////////////////
		
		// Create bottom edit pane.
		WebPanel bottomEditPane = new WebPanel(new BorderLayout(0, 0));
		bottomEditPane.setBorder(new TitledBorder("Supplies"));
		add(bottomEditPane, BorderLayout.CENTER);

		// Create supply table.
		supplyTableModel = new SupplyTableModel(resupply);
		supplyTable = new JTable(supplyTableModel);
		TableStyle.setTableStyle(supplyTable);
		supplyTable.getColumnModel().getColumn(0).setMaxWidth(150);
		supplyTable.getColumnModel().getColumn(0).setCellEditor(new CategoryCellEditor());
		supplyTable.getColumnModel().getColumn(1).setMaxWidth(250);
		supplyTable.getColumnModel().getColumn(1).setCellEditor(new TypeCellEditor());
		supplyTable.getColumnModel().getColumn(2).setMaxWidth(100);
		supplyTable.getColumnModel().getColumn(2).setCellEditor(new QuantityCellEditor());
		supplyTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
				if (!evt.getValueIsAdjusting()) {
					// If rows are selected, enable remove supply button.
					boolean hasSelection = supplyTable.getSelectedRow() > -1;
					removeSupplyButton.setEnabled(hasSelection);
				}
			}
		});

		// Align the content to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		supplyTable.getColumnModel().getColumn(2).setCellRenderer(renderer);

		// Create supply scroll pane.
		WebScrollPane supplyScrollPane = new WebScrollPane(supplyTable);
		supplyScrollPane.setPreferredSize(new Dimension(450, 200));
		bottomEditPane.add(supplyScrollPane, BorderLayout.CENTER);

		// Create supply button pane.
		WebPanel supplyButtonPane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		bottomEditPane.add(supplyButtonPane, BorderLayout.SOUTH);

		// Create add supply button.
		WebButton addSupplyButton = new WebButton("Add");
		addSupplyButton.addActionListener(e ->
				// Add new supply row.
				addNewSupplyRow()
		);
		supplyButtonPane.add(addSupplyButton);

		// Create remove supply button.
		removeSupplyButton = new WebButton("Remove");
		removeSupplyButton.addActionListener(e ->
				// Remove selected supply rows.
				removeSelectedSupplyRows()
		);
		removeSupplyButton.setEnabled(false);
		supplyButtonPane.add(removeSupplyButton);
	}

	/**
	 * Set the components of the arrival date pane to be enabled or disabled.
	 *
	 * @param enable true if enable components, false if disable components.
	 */
	private void setEnableArrivalDatePane(boolean enable) {
		arrivalDateTitleLabel.setEnabled(enable);
		solLabel.setEnabled(enable);
		solCB.setEnabled(enable);
		monthLabel.setEnabled(enable);
		monthCB.setEnabled(enable);
		orbitLabel.setEnabled(enable);
		orbitCB.setEnabled(enable);
		errorLabel.setEnabled(!enable);
	}

	/**
	 * Sets the components of the time until arrival pane to be enabled or disabled.
	 *
	 * @param enable true if enable components, false if disable components.
	 */
	private void setEnableTimeUntilArrivalPane(boolean enable) {
		solsUntilArrivalLabel.setEnabled(enable);
		MarsClock resupplyTime = null;
		int solsDiff = 0;
		if (resupply != null) {
			resupplyTime = resupply.getArrivalDate();
			solsDiff = (int) Math.round((MarsClock.getTimeDiff(resupplyTime, marsClock) / 1000D));
		} else {
			getArrivalDate();
		}
		solsUntilCB.setSelectedItem(solsDiff);
		solInfoLabel.setEnabled(enable);
	}

	/**
	 * Adds a new supply row to the supply table.
	 */
	private void addNewSupplyRow() {
		// Add new supply row.
		supplyTableModel.addNewSupplyItem();

		// Select new row.
		int index = supplyTable.getRowCount() - 1;
		supplyTable.setRowSelectionInterval(index, index);

		// Scroll to bottom of table.
		supplyTable.scrollRectToVisible(supplyTable.getCellRect(index, 0, true));
	}

	/**
	 * Removes the selected supply rows.
	 */
	private void removeSelectedSupplyRows() {

		// Get all selected row indexes and remove items from table.
		int[] removedIndexes = supplyTable.getSelectedRows();
		supplyTableModel.removeSupplyItems(removedIndexes);
	}

	/**
	 * Inner class for editing the Category cell with a combo box.
	 */
	private class CategoryCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		// Data members.
		private JComboBoxMW<String> categoryCB;
		private int editingRow;
		private String previousCategory;

		/**
		 * Constructor
		 */
		private CategoryCellEditor() {
			super();
			categoryCB = new JComboBoxMW<>();
			Iterator<String> i = SupplyTableModel.getCategoryList().iterator();
			while (i.hasNext()) {
				categoryCB.addItem(i.next());
			}
			categoryCB.addActionListener(this);
		}

		@Override
		public Object getCellEditorValue() {
			return categoryCB.getSelectedItem();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			editingRow = row;
			previousCategory = (String) table.getValueAt(row, column);
			categoryCB.setSelectedItem(previousCategory);
			return categoryCB;
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			String category = (String) categoryCB.getSelectedItem();
			if ((editingRow > -1) && (!category.equals(previousCategory))) {
				supplyTable.setValueAt(category, editingRow, 0);

				// Update supply type cell in row if category has changed.
				String defaultType = SupplyTableModel.getCategoryTypeMap().get(category).get(0);
				// supplyTable.setValueAt(Conversion.capitalize(defaultType), editingRow, 1);
				supplyTable.setValueAt(defaultType, editingRow, 1);
			}
		}
	}

	/**
	 * Inner class for editing the Type cell with a combo box.
	 */
	private class TypeCellEditor extends AbstractCellEditor implements TableCellEditor {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		// Data members.
		private Map<String, JComboBoxMW<String>> typeCBMap;
		private JComboBoxMW<String> currentCB;

		/**
		 * Constructor
		 */
		private TypeCellEditor() {

			Map<String, List<String>> categoryTypeMap = SupplyTableModel.getCategoryTypeMap();
			typeCBMap = new HashMap<>(categoryTypeMap.keySet().size());
			Iterator<String> i = categoryTypeMap.keySet().iterator();
			while (i.hasNext()) {
				String category = i.next();
				JComboBoxMW<String> categoryCB = new JComboBoxMW<>();
				List<String> types = categoryTypeMap.get(category);
				Iterator<String> j = types.iterator();
				while (j.hasNext()) {
					String type = j.next();
					categoryCB.addItem(Conversion.capitalize(type));
				}
				typeCBMap.put(category, categoryCB);
			}
		}

		@Override
		public Object getCellEditorValue() {
			Object result = null;
			if (currentCB != null)
				result = currentCB.getSelectedItem();
			return result;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {

			// Get type combo box based on first column category value.
			String category = (String) table.getValueAt(row, 0);
			currentCB = typeCBMap.get(category);
			currentCB.setSelectedItem(table.getValueAt(row, column));
			return currentCB;
		}
	}

	/**
	 * Inner class for editing the quantity cell with a combo box.
	 */
	private class QuantityCellEditor extends AbstractCellEditor implements TableCellEditor {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		// Data members.
		private JComboBoxMW<Number> quantityCB;

		/**
		 * Constructor
		 */
		private QuantityCellEditor() {

			int size = quantity.length;
			for (int i = 0; i < size; i++) {
				quantity[i] = i + 1;
			}

			quantityCB = new JComboBoxMW<>(quantity);

		}

		@Override
		public Object getCellEditorValue() {
			Object result = null;
			if (quantityCB != null)
				result = quantityCB.getSelectedItem();
			return result;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			quantityCB.setSelectedItem(table.getValueAt(row, column));
			return quantityCB;
		}
	}

	@Override
	public boolean modifyTransportItem() {
		// Modify resupply mission.
		populateResupplyMission(resupply);
		resupply.commitModification();
		updateSolsUntilCB();
		return true;
	}

	/**
	 * Updates the 'sols until' combo box.
	 */
	public void updateSolsUntilCB() {
		List<Integer> solList = new ArrayList<>();
		Collections.addAll(solList, solsUntil);
		// Remove dates that have been chosen for other resupply missions.
//		solList.removeAll(getMissionSols());

		int t = ResupplyUtil.getAverageTransitTime();
		int missionSol = marsClock.getMissionSol();
		int date = missionSol + t;
		// Gets a list of sols that are to be excluded from solList
		List<Integer> cancelSols = new ArrayList<>();
	    for (int i = 0; i < date; i++) {
	    	cancelSols.add(i);
	    }
	    // Exclude these sols
	    solList.removeAll(cancelSols);
		
		solsUntil = solList.toArray(EMPTY_STRING_ARRAY);
		solsUntilCB = new JComboBoxMW<>(solsUntil);
		solsUntilCB.requestFocus(false);
		solsUntilCB.putClientProperty("JComboBox.isTableCellEditor", Boolean.FALSE);
		solsUntilCB.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
				solsUntilArrivalRB.requestFocus(true);
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				arrivalDateRB.requestFocus(true);
			}

		});

		solsUntilCB.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					solsUntilArrivalRB.requestFocus(true);
				} else if (evt.getStateChange() == ItemEvent.DESELECTED) {
					arrivalDateRB.requestFocus(true);
				}
			}
		});
	}

	@Override
	public boolean createTransportItem() {
		// Create new resupply mission.
		Settlement destination = (Settlement) destinationCB.getSelectedItem();
		MarsClock arrivalDate = null;
		if (marsCurrentTime == null)
			arrivalDate = getArrivalDate();
		else
			arrivalDate = marsCurrentTime;

		if (!validation_result)
			return false;

		else {
			Resupply newResupply = new Resupply(arrivalDate, destination);
			modifyResupplyMission(newResupply, arrivalDate);
			Simulation.instance().getTransportManager().addNewTransportItem(newResupply);
			return true;
		}
	}

	/**
	 * Populates a resupply mission from the dialog info.
	 *
	 * @param resupplyMission the resupply mission to populate.
	 */
	private boolean populateResupplyMission(Resupply resupplyMission) {
		// Set destination settlement.
		Settlement destination = (Settlement) destinationCB.getSelectedItem();
		resupplyMission.setSettlement(destination);
		// Set arrival date.
		MarsClock arrivalDate = null;
		if (marsCurrentTime == null)
			arrivalDate = getArrivalDate();
		else
			arrivalDate = marsCurrentTime;

		if (!validation_result) {
			return false;
		} else {
			modifyResupplyMission(resupplyMission, arrivalDate);
			return true;
		}
	}

	/**
	 * Modifies a resupply mission.
	 * 
	 * @param resupplyMission
	 * @param arrivalDate
	 */
	private void modifyResupplyMission(Resupply resupplyMission, MarsClock arrivalDate) {

		resupplyMission.setArrivalDate(arrivalDate);

		// Determine launch date.
		MarsClock launchDate = (MarsClock) arrivalDate.clone();
		launchDate.addTime(-1D * ResupplyUtil.getAverageTransitTime() * 1000D);
		resupplyMission.setLaunchDate(launchDate);

		// Set resupply state based on launch and arrival time.
		TransitState state = TransitState.PLANNED;
		if (MarsClock.getTimeDiff(marsClock, launchDate) > 0D) {
			state = TransitState.IN_TRANSIT;
			if (MarsClock.getTimeDiff(marsClock, arrivalDate) > 0D) {
				state = TransitState.ARRIVED;
			}
		}
		resupplyMission.setTransitState(state);

		// Set immigrant num.
		int immigrantNum = (Integer) immigrantsCB.getSelectedItem();
		if (immigrantNum < 0)
			immigrantNum = 0;
		resupplyMission.setNewImmigrantNum(immigrantNum);


		// Set bot num.
		int botNum = (Integer) botsCB.getSelectedItem();
		if (botNum < 0)
			botNum = 0;
		resupplyMission.setNewBotNum(botNum);

		
		// Commit any active editing cell in the supply table.
		if (supplyTable.isEditing()) {
			supplyTable.getCellEditor().stopCellEditing();
		}

		List<SupplyItem> supplyItems = supplyTableModel.getSupplyItems();

		// Set new buildings.
		if (resupplyMission.getNewBuildings() != null) {
			// Modify resupply mission buildings from table.
			modifyNewBuildings(resupplyMission, supplyItems);
		} else {
			// Create new buildings from table in resupply mission.
			List<BuildingTemplate> newBuildings = new ArrayList<>();
			Iterator<SupplyItem> i = supplyItems.iterator();
			while (i.hasNext()) {
				SupplyItem item = i.next();
				if (SupplyTableModel.BUILDING.equals(item.category.trim())) {
					int num = item.number.intValue();
					for (int x = 0; x < num; x++) {
						String type = item.type.trim();
						// NOTE: The parameters does NOT mater right now. When a building arrive,
						// the parameters for each building's template will be re-assembled
						newBuildings.add(new BuildingTemplate(null, 0, null, type, type, new BoundedObject(0D, 0D, -1D, -1D, 0D)));
					}
				}
			}
			resupplyMission.setNewBuildings(newBuildings);
		}

		// Set new vehicles.
		List<String> newVehicles = new ArrayList<>();
		Iterator<SupplyItem> j = supplyItems.iterator();
		while (j.hasNext()) {
			SupplyItem item = j.next();
			if (SupplyTableModel.VEHICLE.equals(item.category.trim())) {
				int num = item.number.intValue();
				for (int x = 0; x < num; x++) {
					newVehicles.add(item.type.trim());
				}
			}
		}
		resupplyMission.setNewVehicles(newVehicles);

		// Set new equipment.
		Map<String, Integer> newEquipment = new HashMap<>();
		Iterator<SupplyItem> k = supplyItems.iterator();
		while (k.hasNext()) {
			SupplyItem item = k.next();
			if (SupplyTableModel.EQUIPMENT.equals(item.category.trim())) {
				String type = item.type.trim();
				int num = item.number.intValue();
				if (newEquipment.containsKey(type)) {
					num += newEquipment.get(type);
				}
				newEquipment.put(type, num);
			}
		}
		resupplyMission.setNewEquipment(newEquipment);

		// Set new resources.
		Map<AmountResource, Double> newResources = new HashMap<>();
		Iterator<SupplyItem> l = supplyItems.iterator();
		while (l.hasNext()) {
			SupplyItem item = l.next();
			if (SupplyTableModel.RESOURCE.equals(item.category.trim())) {
				String type = item.type.trim();
				AmountResource resource = ResourceUtil.findAmountResource(type);
				double amount = item.number.doubleValue();
				if (newResources.containsKey(resource)) {
					amount += newResources.get(resource);
				}
				newResources.put(resource, amount);
			}
		}
		resupplyMission.setNewResources(newResources);

		// Set new parts.
		Map<Part, Integer> newParts = new HashMap<>();
		Iterator<SupplyItem> m = supplyItems.iterator();
		while (m.hasNext()) {
			SupplyItem item = m.next();
			if (SupplyTableModel.PART.equals(item.category.trim())) {
				String type = item.type.trim();
				Part part = (Part) ItemResourceUtil.findItemResource(type);
				int num = item.number.intValue();
				if (newParts.containsKey(part)) {
					num += newParts.get(part);
				}
				newParts.put(part, num);
			}
		}
		resupplyMission.setNewParts(newParts);

		// return true;
	}

	/**
	 * Maps a number to an alphabet.
	 *
	 * @param a number
	 * @return a String
	 */
	private String getCharForNumber(int i) {
		// Do note delete. Will use it
		// NOTE: i must be > 1, if i = 0, return null
		return i > 0 && i < 27 ? String.valueOf((char) (i + 'A' - 1)) : null;
	}

	/**
	 * Modifies existing resupply mission new buildings based on supply table.
	 *
	 * @param resupplyMission resupply mission.
	 * @param supplyItems     the supply items from the supply table.
	 */
	private void modifyNewBuildings(Resupply resupplyMission, List<SupplyItem> supplyItems) {

		List<BuildingTemplate> newBuildings = resupplyMission.getNewBuildings();

		// Create map of resupply mission's buildings and numbers.
		Map<String, Integer> oldBuildings = new HashMap<>();
		Iterator<BuildingTemplate> i = newBuildings.iterator();
		while (i.hasNext()) {
			BuildingTemplate template = i.next();
			String type = template.getBuildingType();
			if (oldBuildings.containsKey(type)) {
				int num = oldBuildings.get(type);
				oldBuildings.put(type, num + 1);
			} else {
				oldBuildings.put(type, 1);
			}
		}

		// Go through every building row in the supply table.
		Iterator<SupplyItem> j = supplyItems.iterator();
		while (j.hasNext()) {
			SupplyItem item = j.next();
			if (SupplyTableModel.BUILDING.equals(item.category.trim())) {
				int num = item.number.intValue();
				String type = item.type.trim();

				int existingNum = 0;
				if (oldBuildings.containsKey(type)) {
					existingNum = oldBuildings.get(type);
				}

				if (num > existingNum) {
					// Add new building templates.
					int diff = num - existingNum;
					for (int x = 0; x < diff; x++) {
						// Added a dummy type parameter

						// NOTE: currently building id = 0
						// May need to assemble the buildingNickName
						// by obtaining the next building id and settlement id

						// NOTE: determine why specifying the coordinate below is needed for
						// the Command and Control building to be placed properly

						// NOTE: The parameters does NOT mater right now. When a building arrive,
						// the parameters for each building's template will be re-assembled

						newBuildings.add(new BuildingTemplate(null, 0, null, type, type,
											new BoundedObject(0D, 38D, 7D, 9D, 270D)));
					}
					
				} else if (num < existingNum) {
					// Remove old building templates.
					int diff = existingNum - num;
					for (int x = 0; x < diff; x++) {
						Iterator<BuildingTemplate> k = newBuildings.iterator();
						while (k.hasNext()) {
							BuildingTemplate template = k.next();
							if (template.getBuildingType().equalsIgnoreCase(type)) {
								k.remove();
								break;
							}
						}
					}
				}
			}
		}

		// Go through all of the old buildings in the map to make sure they exist in the
		// supply table.
		Iterator<String> k = oldBuildings.keySet().iterator();
		while (k.hasNext()) {
			String type = k.next();
			boolean exists = false;
			Iterator<SupplyItem> l = supplyItems.iterator();
			while (l.hasNext() && !exists) {
				SupplyItem item = l.next();
				if (SupplyTableModel.BUILDING.equals(item.category.trim())) {
					if (type.equals(item.type.trim())) {
						exists = true;
					}
				}
			}

			// Remove building from new buildings if it doesn't exist in supply table.
			if (!exists) {
				Iterator<BuildingTemplate> m = newBuildings.iterator();
				while (m.hasNext()) {
					BuildingTemplate template = m.next();
					if (template.getBuildingType().equalsIgnoreCase(type)) {
						m.remove();
					}
				}
			}
		}
	}

	/**
	 * Gets the arrival date from the dialog info.
	 *
	 * @return {@link MarsClock} arrival date.
	 */
	private MarsClock getArrivalDate() {
		errorString = null;

		if (arrivalDateRB.isSelected()) {
			marsCurrentTime = (MarsClock) marsClock.clone();

			// Determine arrival date from arrival date combo boxes.
			try {
				int sol = solCB.getSelectedIndex() + 1;
				int month = monthCB.getSelectedIndex() + 1;
				int orbit = Integer.parseInt((String) orbitCB.getSelectedItem());

				// Set millisols to current time plus the delay if resupply is current date, otherwise 0.
				double millisols = 0D;
				
				if (sol < marsClock.getMissionSol()) {
					// if the player selects a sol before today
					marsCurrentTime = null;
					// Remove error string
					errorString = "Cannot pick a sol that's in the past. Try again !";
					errorLabel.setText(errorString);
					logger.severe(errorString);
					enableButton(false);
					validation_result = false;
				}
				else if ((sol == marsClock.getMissionSol()) 
						&& (month == marsClock.getMonth())
						&& (orbit == marsClock.getOrbit())) {
					millisols = marsClock.getMillisol() + MILLISOLS_DELAY;
				}

				marsCurrentTime = new MarsClock(orbit, month, sol, millisols, marsClock.getMissionSol());
				
			} catch (NumberFormatException e) {
				logger.severe("Can't create marsCurrentTime: " + e.getMessage());
			}
		}

		else if (solsUntilArrivalRB.isSelected()) {
			marsCurrentTime = validateSolsUntilArrival();
		}

		return marsCurrentTime;
	}

	/**
	 * Validates the sols until arrival
	 */
	public MarsClock validateSolsUntilArrival() {
		errorString = null;
		solsUntilCB.setEditable(true);
		solsUntilCB.setSelectedIndex(0);

		int inputSol = (Integer) solsUntilCB.getSelectedItem();
		if (inputSol == 0) {
			solsUntilCB.remove(0);
			solsUntilCB.setSelectedIndex(0);
		}

		try {
			boolean good = true;

			if (good) {
				
				// Remove error string
				errorString = null;
				errorLabel.setText(errorString);
				// Reenable Commit/Create button
				enableButton(true);
				
				validation_result = true;

				marsCurrentTime = (MarsClock) marsClock.clone();
				if (inputSol == 0)
					marsCurrentTime.addTime(marsCurrentTime.getMillisol());
				else
					marsCurrentTime.addTime(inputSol * 1000D);
			}

		} catch (NumberFormatException e) {
			errorString = Msg.getString("ArrivingSettlementEditingPanel.error.invalidSols"); //$NON-NLS-1$
			errorLabel.setText(errorString);
			validation_result = false;
			enableButton(false);
			logger.severe("Invalid entry for Sols: " + e.getMessage());
		}

		return marsCurrentTime;
	}

//	public List<Integer> getMissionSols() {
//		List<Integer> solsList = new ArrayList<>();
//		// Check if this particular sol has already been chosen for a resupply
//		// mission
//		JList<?> jList = resupplyWindow.getIncomingListPane().getIncomingList();
//		ListModel<?> model = jList.getModel();
//
//		for (int i = 0; i < model.getSize(); i++) {
//			Transportable transportItem = (Transportable) model.getElementAt(i);
//
//			if ((transportItem != null)) {
//				if (transportItem instanceof Resupply) {
//					// Create modify resupply mission dialog.
//					Resupply newR = (Resupply) transportItem;
//					if (!newR.equals(resupply)) {
//						MarsClock arrivingTime = newR.getArrivalDate();
//						int solsDiff = (int) Math.round((MarsClock.getTimeDiff(arrivingTime, marsClock) / 1000D));
//						solsList.add(solsDiff);
//					}
//				} else if (transportItem instanceof ArrivingSettlement) {
//					// Create modify arriving settlement dialog.
//					ArrivingSettlement settlement = (ArrivingSettlement) transportItem;
//					MarsClock arrivingTime = settlement.getArrivalDate();
//					int solsDiff = (int) Math.round((MarsClock.getTimeDiff(arrivingTime, marsClock) / 1000D));
//					solsList.add(solsDiff);
//				}
//			}
//		}
//
//		return solsList;
//	}

	public void enableButton(boolean value) {
		if (modifyTransportItemDialog != null)
			modifyTransportItemDialog.setCommitButton(value);
		else if (newTransportItemDialog != null)
			newTransportItemDialog.setCreateButton(value);
	}

	/**
	 * Installs a listener to receive notification when the text of any
	 * {@code JTextComponent} is changed. Internally, it installs a
	 * {@link DocumentListener} on the text component's {@link Document},
	 * and a {@link PropertyChangeListener} on the text component to detect
	 * if the {@code Document} itself is replaced.
	 *
	 * @param text any text component, such as a {@link JTextField}
	 *        or {@link JTextArea}
	 * @param changeListener a listener to receieve {@link ChangeEvent}s
	 *        when the text is changed; the source object for the events
	 *        will be the text component
	 * @throws NullPointerException if either parameter is null
	 */
	public static void addChangeListener(JTextComponent text, ChangeListener changeListener) {
		// http://stackoverflow.com/questions/3953208/value-change-listener-to-jtextfield
		Objects.requireNonNull(text);
		Objects.requireNonNull(changeListener);
		DocumentListener dl = new DocumentListener() {
			private int lastChange = 0, lastNotifiedChange = 0;

			@Override
			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			public void changedUpdate(DocumentEvent e) {
				lastChange++;
				SwingUtilities.invokeLater(() -> {
					if (lastNotifiedChange != lastChange) {
						lastNotifiedChange = lastChange;
						changeListener.stateChanged(new ChangeEvent(text));
					}
				});
			}
		};
		text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
			Document d1 = (Document) e.getOldValue();
			Document d2 = (Document) e.getNewValue();
			if (d1 != null)
				d1.removeDocumentListener(dl);
			if (d2 != null)
				d2.addDocumentListener(dl);
			dl.changedUpdate(null);
		});
		Document d = text.getDocument();
		if (d != null)
			d.addDocumentListener(dl);
	}

	/**
	 * Prepares this window for deletion.
	 */
	public void destroy() {

		Arrays.fill(solsUntil, null);
		solsUntil = null;
		Arrays.fill(quantity, null);
		quantity = null;
		Arrays.fill(immigrants, null);
		immigrants = null;
		destinationCB = null;
		arrivalDateRB = null;
		arrivalDateTitleLabel = null;
		solsUntilArrivalRB = null;
		solsUntilArrivalLabel = null;
		martianSolCBModel = null;
		solLabel = null;
		solCB = null;
		solsUntilCB = null;
		immigrantsCB = null;
		monthLabel = null;
		monthCB = null;
		orbitLabel = null;
		orbitCB = null;
		solInfoLabel = null;
		supplyTableModel = null;
		supplyTable = null;
		removeSupplyButton = null;
		errorLabel = null;
		resupply = null;
		newTransportItemDialog = null;
		modifyTransportItemDialog = null;
		resupplyWindow = null;
		marsCurrentTime = null;
	}

}
