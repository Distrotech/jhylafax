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
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import net.sf.jhylafax.JobHelper.FileStat;
import net.sf.jhylafax.fax.FaxJob;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xnap.commons.gui.ColoredTable;
import org.xnap.commons.gui.Dialogs;
import org.xnap.commons.gui.action.AbstractXNapAction;
import org.xnap.commons.gui.table.FilesizeCellRenderer;
import org.xnap.commons.gui.table.StringCellRenderer;
import org.xnap.commons.gui.table.TableLayout;
import org.xnap.commons.gui.table.TableSorter;
import org.xnap.commons.gui.util.GUIHelper;
import org.xnap.commons.gui.util.PopupListener;
import org.xnap.commons.settings.SettingStore;
import org.xnap.commons.util.StringHelper;

/**
 * A generic panel that displays a list of {@link net.sf.jhylafax.fax.FaxJob} 
 * objects in a table.
 *  
 * @author Steffen Pingel
 */
public abstract class AbstractQueuePanel extends JPanel implements ListSelectionListener, LocaleChangeListener {
	
	private final static Log logger = LogFactory.getLog(AbstractQueuePanel.class);
	
	private String[] defaultColumns;
	private String queueName;
	private ColoredTable queueTable;
	private TableLayout queueTableLayout;
	private ResetQueueTableAction resetQueueTableAction;
	private JPanel buttonPanel;
	private JPopupMenu tablePopupMenu;

	public AbstractQueuePanel(String queueName) {
		this.queueName = queueName;
		
		setLayout(new BorderLayout());
		setBorder(GUIHelper.createEmptyBorder(10));
				
		resetQueueTableAction = new ResetQueueTableAction();
		
		tablePopupMenu = new JPopupMenu();
		
		TableSorter sorter = new TableSorter(getTableModel());
		queueTable = new ColoredTable(sorter);
		queueTableLayout = new TableLayout(queueTable);
		initializeTableLayout();
		queueTableLayout.getHeaderPopupMenu().add(new JMenuItem(resetQueueTableAction));
		add(new JScrollPane(queueTable), BorderLayout.CENTER);

		queueTable.setShowVerticalLines(true);
		queueTable.setShowHorizontalLines(false);
		queueTable.setAutoCreateColumnsFromModel(true);
		queueTable.setIntercellSpacing(new java.awt.Dimension(2, 1));
		queueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		queueTable.getSelectionModel().addListSelectionListener(this);
		queueTable.addMouseListener(new PopupListener(tablePopupMenu));
		
		queueTable.setDefaultRenderer(Long.class, new FilesizeCellRenderer());
		queueTable.setDefaultRenderer(String.class, new StringCellRenderer());
		queueTable.setDefaultRenderer(Date.class, new TimeCellRenderer());
		queueTable.setDefaultRenderer(FaxJob.State.class, new StateCellRenderer());
		
		buttonPanel = new JPanel(new FlowLayout());
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	protected File createTempFile(String filename)
	{
		try {
			// TODO should preserve extension of downloaded file
			File tempFile = File.createTempFile("jhylafax", null);
			tempFile.deleteOnExit();
			return tempFile;
		}
		catch (IOException e) {
			logger.debug("Error creating temporary file", e);
			JHylaFAX.getInstance().showError(i18n.tr("Error creating temporary file"), e);
			return null;				
		}			
	}
	
	public String getAbsolutePath(String filename) {
		return getQueueName() + "/" + filename;
	}	

	public String getQueueName() {
		return queueName;
	}
	
	public abstract FileStat getSelectedFile();
	
	protected abstract TableModel getTableModel();
	
	protected TableLayout getTableLayout() {
		return queueTableLayout;
	}

	protected JTable getTable() {
		return queueTable;
	}
	
	protected JPopupMenu getTablePopupMenu() {
		return tablePopupMenu;
	}
	
	protected JPanel getButtonPanel() {
		return buttonPanel;
	}

	protected int getSelectedRow() {
		int row = queueTable.getSelectedRow();
		return (row == -1) ? -1 : ((TableSorter)queueTable.getModel()).mapToIndex(row);
	}

	protected abstract void initializeTableLayout();

	public void restoreLayout(SettingStore store, String[] defaultColumns) {
		this.defaultColumns = defaultColumns;
		
		store.restoreTable(getQueueName(), defaultColumns, queueTableLayout);
	}	

	public void saveLayout(SettingStore store) {
		store.saveTable(getQueueName(), queueTableLayout);
	}
	
	public abstract void updateActions();

	public void updateLabels() {
		resetQueueTableAction.putValue(Action.NAME, i18n.tr("Reset to Defaults"));
	}

	public void resetTable() {
		initializeTableLayout();
		getTableLayout().setColumnsVisible(defaultColumns);	
		queueTable.getTableHeader().revalidate();
	}
	
	public void valueChanged(ListSelectionEvent e) {
		updateActions();
	}

	protected class DeleteAction extends AbstractXNapAction {
		
		public DeleteAction() {
			putValue(ICON_FILENAME, "editdelete.png");
		}

		public void actionPerformed(ActionEvent e)
		{
			FileStat selectedFile = getSelectedFile();
			if (selectedFile == null) {
				return;
			}

			if (Dialogs.showConfirmDialog(JHylaFAX.getInstance(), 
					i18n.tr("Do you really want to delete the file {0}?", selectedFile.filename),
					i18n.tr("Delete File"), 
					JOptionPane.YES_NO_OPTION, 
					Settings.CONFIRM_DELETE) == JOptionPane.YES_OPTION) {
				JobHelper.delete(getAbsolutePath(selectedFile.filename));
			}
		}

		public void updateLabels() {
			putValue(Action.NAME, i18n.tr("Delete"));
		}
		
	}

	protected class ResetQueueTableAction extends AbstractXNapAction {
		
		public ResetQueueTableAction() {
		}

		public void actionPerformed(ActionEvent event) {
			resetTable();
		}
		
	}
	
	protected class SaveAction extends AbstractXNapAction {
		
		public SaveAction() {
			putValue(ICON_FILENAME, "filesaveas.png");
		}

		public void actionPerformed(ActionEvent e)
		{
			FileStat selectedFile = getSelectedFile();
			if (selectedFile == null) {
				return;
			}
			
			JFileChooser chooser = new JFileChooser();
			chooser.setSelectedFile(new File(StringHelper.lastToken(selectedFile.filename, "/")));
			if (chooser.showSaveDialog(JHylaFAX.getInstance()) == JFileChooser.APPROVE_OPTION) {
				// TODO show warning, if file exists
				JobHelper.save(chooser.getSelectedFile(), 
						getAbsolutePath(selectedFile.filename),	selectedFile.filesize);
			}
		}
		
		public void updateLabels() {
			putValue(Action.NAME, i18n.tr("Save"));
		}

	}
	
	protected class ViewAction extends AbstractXNapAction {
		
		public ViewAction() {
			putValue(ICON_FILENAME, "viewmag.png");
		}

		public void actionPerformed(ActionEvent event)
		{
			FileStat selectedFile = getSelectedFile();
			if (selectedFile == null) {
				return;
			}
			
			String viewerPath = JHylaFAXHelper.getViewerPath(getQueueName());
			if (viewerPath != null) {
				File tempFile = createTempFile(selectedFile.filename);
				if (tempFile != null
						&& JobHelper.save(tempFile, getAbsolutePath(selectedFile.filename), selectedFile.filesize)) {
					JHylaFAXHelper.view(viewerPath, new File[] { tempFile });
				}
			}
		}

		public void updateLabels() {
			putValue(Action.NAME, i18n.tr("View"));
			putValue(Action.LONG_DESCRIPTION, 
					i18n.tr("The selected file is opened in an external viewer."));
		}
		
	}
	
}
