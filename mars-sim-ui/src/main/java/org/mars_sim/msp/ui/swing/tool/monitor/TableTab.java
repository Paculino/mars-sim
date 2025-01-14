/*
 * Mars Simulation Project
 * TableTab.java
 * @date 2021-12-07
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.RowNumberTable;
import org.mars_sim.msp.ui.swing.tool.TableStyle;

import com.alee.laf.scroll.WebScrollPane;

/**
 * This class represents a table view displayed within the Monitor Window. It
 * displays the contents of a UnitTableModel in a WebTable window. It supports
 * the selection and deletion of rows.
 */
@SuppressWarnings("serial")
abstract class TableTab extends MonitorTab {

	private JTableHeader header;
	private TableCellRenderer tableCellRenderer;
	private TableProperties propsWindow;

	// These icons are used to render the sorting images on the column header
	private static Icon ascendingIcon = null;
	private static Icon descendingIcon = null;
	// private final static Icon TABLEICON = ImageLoader.getIcon("Table");

	/** Table component. */
	protected JTable table;
	/** Sortable model proxy. */
	private TableSorter sortedModel;
	/** Constructor will flip this. */
	private boolean sortAscending = true;
	/** Sort column is defined. */
	private int sortedColumn = 0;

	/**
	 * Creates a table within a tab displaying the specified model.
	 *
	 * @param model           The model of Units to display.
	 * @param mandatory       Is this table view mandatory.
	 * @param singleSelection Does this table only allow single selection?
	 */
	public TableTab(final MonitorWindow window, final MonitorModel model, boolean mandatory, boolean singleSelection,
			String icon) {
		super(model, mandatory, ImageLoader.getNewIcon(icon));
	
		// Can not create icons until UIManager is up and running
		if (ascendingIcon == null) {
			Color baseColor = UIManager.getColor("Label.background");

			ascendingIcon = new ColumnSortIcon(false, baseColor);
			descendingIcon = new ColumnSortIcon(true, baseColor);
		}

		// If the model is not ordered, allow user the facility
		if (!model.getOrdered()) {
			// Create a sortable model to act as a proxy
			sortedModel = new TableSorter(model);
			// Create scrollable table window
			table = new JTable(sortedModel) {

				/**
				 * Overriding table change so that selections aren't cleared when rows are
				 * deleted.
				 */
				public void tableChanged(TableModelEvent e) {

					if (e.getType() == TableModelEvent.DELETE) {
						// Store selected row objects.
						List<Object> selected = getSelection();

						// Call super implementation to remove row and clear selection.
						super.tableChanged(e);

						// Reselect rows if row objects still around.
						MonitorModel model = (MonitorModel) getModel();
						Iterator<Object> i = selected.iterator();
						while (i.hasNext()) {
							Object selectedObject = i.next();
							for (int x = 0; x < model.getRowCount(); x++) {
								if (selectedObject.equals(model.getObject(x)))
									addRowSelectionInterval(x, x);
							}
						}
					} else
						super.tableChanged(e);
				}

                /**
                 * Display the cell contents as a tooltip. Useful when cell
                 * contents in wider than the cell
				 */
				@Override
                public String getToolTipText(MouseEvent e) {
                	// Future: Figure out how to create a custom tooltip text for showing the greenhouse crop in Crop tab
                    return getCellText(e);
                };

			};

			// call it a click to display details button when user double clicks the table
			table.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2 && !e.isConsumed()) {
						window.displayDetails();
					}
				}
				@Override
				public void mousePressed(MouseEvent e) {
					// nothing	
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					// nothing	
				}
				@Override
				public void mouseEntered(MouseEvent e) {
					// nothing				
				}
				@Override
				public void mouseExited(MouseEvent e) {
					// nothing			
				}
			});

			sortedModel.addTableModelListener(table);
		
			// Add a mouse listener for the mouse event selecting the sorted column
			// Not the best way but no double click is provided on Header class
			// Get the TableColumn header to display sorted column
			header = (JTableHeader) table.getTableHeader();
			// theRenderer = new TableHeaderRenderer(header.getDefaultRenderer());
			// header.setDefaultRenderer(theRenderer);
			header.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					// Find the column at this point
					int column = header.columnAtPoint(e.getPoint());
					setSortColumn(column);
					header.repaint();
				}
			});

		} else {
			// Simple WebTable
			this.table = new JTable(model) {
				/**
				 * Overriding table change so that selections aren't cleared when rows are
				 * deleted.
				 */
				public void tableChanged(TableModelEvent e) {

					if (e.getType() == TableModelEvent.DELETE) {
						// Store selected row objects.
						List<Object> selected = getSelection();

						// Call super implementation to remove row and clear selection.
						super.tableChanged(e);

						// Reselect rows if row objects still around.
						MonitorModel model = (MonitorModel) getModel();
						Iterator<Object> i = selected.iterator();
						while (i.hasNext()) {
							Object selectedObject = i.next();
							for (int x = 0; x < model.getRowCount(); x++) {
								if (selectedObject.equals(model.getObject(x)))
									addRowSelectionInterval(x, x);
							}
						}
					} else
						super.tableChanged(e);
				}

				/**
				 * Displays the cell contents as a tooltip. Useful when cell contents in wider
				 * than the cell
				 */
				@Override
				public String getToolTipText(MouseEvent e) {
					return getCellText(e);
				};
			};
		}

		// Enable use of RowFilter with Swingbits
		// see https://github.com/eugener/oxbow/wiki/Table-Filtering
		// TableRowFilterSupport.forTable(table).apply();

		// Set single selection mode if necessary.
		if (singleSelection)
			table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Added RowNumberTable
		JTable rowTable = new RowNumberTable(table);

		TableStyle.setTableStyle(rowTable);
		// Add a scrolled window and center it with the table
		JScrollPane scroller = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// scroller.setBorder(new MarsPanelBorder());

		scroller.setRowHeaderView(rowTable);
		scroller.setCorner(WebScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		// setAutoCreateRowSorter() and MultisortTableHeaderCellRenderer would cause no tab to be created
//		table.setAutoCreateRowSorter(true);
		// Apply sorting for multiple columns
//		table.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
		
		TableStyle.setTableStyle(table);

		add(scroller, BorderLayout.CENTER);

		setName(model.getName());
		setSortColumn(0);

		if (table != null) {
			// Use column resizer
			// Note: may need to use SwingUtilities.invokeLater(() -> adjustColumnWidth(table))
			adjustColumnWidth(table);
			// Update the selected row after each sorting
			table.setUpdateSelectionOnSort(true);
		}
	}

	public JTable getTable() {
		return table;
	}

	public void adjustColumnWidth(JTable table) {
		// Gets max width for cells in column as the preferred width
		TableColumnModel columnModel = table.getColumnModel();
		for (int col = 0; col < table.getColumnCount(); col++) {
			TableColumn tableColumn = columnModel.getColumn(col);
		    int preferredWidth = tableColumn.getMinWidth() + 15;
			int w = 50;
		    TableCellRenderer rend = table.getTableHeader().getDefaultRenderer();
			TableCellRenderer rendCol = tableColumn.getHeaderRenderer();
		    if (rendCol == null) rendCol = rend;
		    Component header = rendCol.getTableCellRendererComponent(table, tableColumn.getHeaderValue(), false, false, 0, col);
		    int maxWidth = header.getPreferredSize().width + 15;
		    w = Math.max(w, maxWidth);

			for (int row = 0; row < table.getRowCount(); row++) {
				if (tableCellRenderer == null)
					tableCellRenderer = table.getCellRenderer(row, col);
				Component c = table.prepareRenderer(tableCellRenderer, row, col);
				int width = c.getPreferredSize().width + table.getIntercellSpacing().width + 15;
				preferredWidth = Math.max(width, preferredWidth);

		        if (preferredWidth <= maxWidth){
			        // Exceeded the maximum width, no need to check other rows
		            preferredWidth = maxWidth;
		            break;
		        }
			}

			preferredWidth = Math.max(w, preferredWidth);
			tableColumn.setPreferredWidth(preferredWidth);
		}
	}

	/**
	 * Display property window anchored to a main desktop.
	 *
	 * @param desktop Main desktop owing the properties dialog.
	 */
	public void displayProps(MainDesktopPane desktop) {
		if (propsWindow == null) {
			propsWindow = new TableProperties(getName(), table, desktop);
			propsWindow.show();
		} else {
			if (propsWindow.isClosed()) {
				if (!propsWindow.wasOpened()) {
					propsWindow.setWasOpened(true);
				}
				add(propsWindow, 0);
				try {
					propsWindow.setClosed(false);
				} catch (Exception e) {
					// logger.log(Level.SEVERE,e.toString()); }
				}
			}
			propsWindow.show();
			// bring to front if it overlaps with other propsWindows
			try {
				propsWindow.setSelected(true);
			} catch (PropertyVetoException e) {
				// ignore if setSelected is vetoed
			}
		}
		propsWindow.getContentPane().validate();
		propsWindow.getContentPane().repaint();
		validate();
		repaint();

	}

	/**
	 * This return the selected rows in the model that are current selected in this
	 * view.
	 *
	 * @return array of row indexes.
	 */
	protected List<Object> getSelection() {
		MonitorModel target = (sortedModel != null ? sortedModel : getModel());

		int indexes[] = {};
		if (table != null)
			indexes = table.getSelectedRows();
		List<Object> selectedRows = new ArrayList<>();
		for (int indexe : indexes) {
			Object selected = target.getObject(indexe);
			if (selected != null)
				selectedRows.add(selected);
		}

		return selectedRows;
	}

	/**
	 * Gets the cell contents under the MouseEvent, this will be displayed as a
	 * tooltip.
	 *
	 * @param e MouseEvent triggering tool tip.
	 * @return Tooltip text.
	 */
	private String getCellText(MouseEvent e) {
		Point p = e.getPoint();
		int column = table.columnAtPoint(p);
		int row = table.rowAtPoint(p);
		String result = null;
		if ((column >= 0) && (row >= 0)) {
			Object cell = table.getValueAt(row, column);
			if (cell != null) {
				if (cell instanceof Integer) {
					return ((Integer) cell).intValue()  + "" ;
				}
				else if (cell instanceof Double) {
					return Math.round(((Double) cell).doubleValue() * 10_000.0)/10_000.0 + "" ;
				}
				result = cell.toString();
			}
		}
		return result;
	}

	/**
	 * Removes this tab.
	 */
	public void removeTab() {
		super.removeTab();
		table = null;
		if (sortedModel != null) {
			sortedModel.destroy();
			sortedModel = null;
		}
	}

	/**
	 * Sets this column.
	 * 
	 * @param index
	 */
	private void setSortColumn(int index) {
		if (sortedModel != null) {
			if (sortedColumn == index) {
				sortAscending = !sortAscending;
			}
			sortedColumn = index;
			sortedModel.sortByColumn(sortedColumn, sortAscending);
		}
	}

	public void destroy() {
		// super.destroy();
		header = null;
		tableCellRenderer = null;
		propsWindow = null;

	}

	/**
	 * This internal class provides a fixed image icon that is drawn using a
	 * Graphics object. It represents an arrow Icon that can be other ascending or
	 * or descending.
	 */
	static class ColumnSortIcon implements Icon {

		static final int midw = 4;
		private Color lightShadow;
		private Color darkShadow;
		private boolean downwards;

		/** constructor. */
		public ColumnSortIcon(boolean downwards, Color baseColor) {
			this.downwards = downwards;
			this.lightShadow = baseColor.brighter();
			this.darkShadow = baseColor.darker().darker();
		}

		public void paintIcon(Component c, Graphics g, int xo, int yo) {
			int w = getIconWidth();
			int xw = xo + w - 1;
			int h = getIconHeight();
			int yh = yo + h - 1;

			if (downwards) {
				g.setColor(lightShadow);
				g.drawLine(xo + midw + 1, yo, xw, yh - 1);
				g.drawLine(xo, yh, xw, yh);
				g.setColor(darkShadow);
				g.drawLine(xo + midw - 1, yo, xo, yh - 1);
			} else {
				g.setColor(lightShadow);
				g.drawLine(xw, yo + 1, xo + midw, yh);
				g.setColor(darkShadow);
				g.drawLine(xo + 1, yo + 1, xo + midw - 1, yh);
				g.drawLine(xo, yo, xw, yo);
			}
		}

		public int getIconWidth() {
			return 2 * midw;
		}

		public int getIconHeight() {
			return getIconWidth() - 1;
		}
	}

	/**
	 * This renderer use a delegation software design pattern to delegate this
	 * rendering of the table cell header to the real default render, however this
	 * renderer adds in an icon on the cells which are sorted.
	 **/
	class TableHeaderRenderer implements TableCellRenderer {
		private TableCellRenderer defaultRenderer;

		public TableHeaderRenderer(TableCellRenderer theRenderer) {
			defaultRenderer = theRenderer;
		}

		/**
		 * Renderer the specified Table Header cell
		 **/
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component theResult = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
					column);
			if (theResult instanceof JLabel) {
				// Must clear the icon if not sorted column. This is a renderer
				// class used to render each column heading in turn.
				JLabel cell = (JLabel) theResult;
				Icon icon = null;
				if (column == sortedColumn) {
					if (sortAscending)
						icon = ascendingIcon;
					else
						icon = descendingIcon;
				}
				// cell.setHorizontalAlignment(SwingConstants.CENTER); // not useful
				// cell.setHorizontalAlignment(JLabel.CENTER); // not useful
				cell.setIcon(icon);
				cell.setOpaque(true);
			}
			return theResult;
		}
	}

}
