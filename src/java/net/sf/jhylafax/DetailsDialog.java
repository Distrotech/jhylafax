/**
 * JHylaFax - A java client for HylaFAX.
 *
 * Copyright (C) 2005 by Steffen Pingel <steffenp@gmx.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package net.sf.jhylafax;

import static net.sf.jhylafax.JHylaFAX.i18n;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xnap.commons.gui.ColoredTable;
import org.xnap.commons.gui.DefaultDialog;
import org.xnap.commons.gui.table.TableLayout;
import org.xnap.commons.gui.table.TableSorter;

public class DetailsDialog extends DefaultDialog {

	private final static Log logger = LogFactory.getLog(DetailsDialog.class);
	private net.sf.jhylafax.DetailsDialog.KeyValueTableModel jobTableModel;
	private ColoredTable jobTable;
	private TableLayout jobTableLayout;
	private List<Property> data;
	
	public DetailsDialog(JFrame owner, List<Property> data) {
		super(owner, BUTTON_CLOSE);

		initialize();
		
		setData(data);
		revert();
		
		updateLabels();
		pack();
	}
	
	public void setData(List<Property> data) {
		this.data = data;	
	}

	public List<Property> getData() {
		return data;
	}
	
	private void initialize() {
		jobTableModel = new KeyValueTableModel();
		TableSorter sorter = new TableSorter(jobTableModel);
		jobTable = new ColoredTable(sorter);
		jobTable.setShowVerticalLines(true);
		jobTable.setShowHorizontalLines(false);
		jobTable.setAutoCreateColumnsFromModel(true);
		jobTable.setIntercellSpacing(new java.awt.Dimension(2, 1));
		jobTable.setDefaultRenderer(Object.class, new ValueTableCellRenderer());
		jobTableLayout = new TableLayout(jobTable);
		jobTableLayout.setColumnProperties(0, "key", 200);
		jobTableLayout.setColumnProperties(1, "value", 150);
		jobTableLayout.setMaintainSortOrder(true);
		jobTableLayout.sortByColumn(0, TableSorter.Order.ASCENDING, false);
		setMainComponent(new JScrollPane(jobTable));
	}

	public void revert()
	{
		jobTableModel.setJob(data);
	}
	
	public void updateLabels() {
		setTitle(i18n.tr("JHylaFAX Job Details"));
		
		jobTableLayout.setColumnNames(new String[] {
				i18n.tr("Property"),
				i18n.tr("Value"),
		});
	}

	private static class ValueTableCellRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			// TODO Auto-generated method stub
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
		}
	}
	
	public static class Property {
		private String key;
		private Object value;
		
		public Property(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}
		
		
	}
	
	private static class KeyValueTableModel extends AbstractTableModel {

		private List<Property> data = new ArrayList<Property>();
		
		private static final Class[] columnClasses= {
			String.class,
			Object.class,
		};
		
		public KeyValueTableModel() 
		{
		}
		
		public void setJob(List<Property> data)
		{
			this.data = data;
			fireTableDataChanged();
		}
	
		public Object getValueAt(int row, int column)
		{
			Property property = data.get(row);
			switch (column) {
			case 0:
				return property.getKey();
			case 1:
				return property.getValue();
			default:
				return null;
			}
		}

		public int getRowCount()
		{
			return data.size();
		}

		public int getColumnCount()
		{
			return columnClasses.length;
		}
		
	    public Class<?> getColumnClass(int column) {
	        return columnClasses[column];
	    }
		
		public Property getProperty(int row) {
			return data.get(row);
		}

	}
}
