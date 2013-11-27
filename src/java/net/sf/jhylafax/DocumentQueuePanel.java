package net.sf.jhylafax;

import static net.sf.jhylafax.JHylaFAX.i18n;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import net.sf.jhylafax.JobHelper.FileStat;
import net.sf.jhylafax.fax.Document;
import org.xnap.commons.gui.Builder;
import org.xnap.commons.gui.util.DoubleClickListener;

public class DocumentQueuePanel extends AbstractQueuePanel {

	private ViewAction viewAction;
	private SaveAction saveAction;
	private DeleteAction deleteAction;
	private DocTableModel docTableModel;

	public DocumentQueuePanel(String queueName) {
		super(queueName);
		
		viewAction = new ViewAction();
		saveAction = new SaveAction();
		deleteAction = new DeleteAction();

		getButtonPanel().add(Builder.createButton(viewAction));
		getButtonPanel().add(Builder.createButton(saveAction));
		getButtonPanel().add(Builder.createButton(deleteAction));
		
		getTablePopupMenu().add(Builder.createMenuItem(viewAction));
		getTablePopupMenu().add(Builder.createMenuItem(saveAction));
		getTablePopupMenu().add(Builder.createMenuItem(deleteAction));
		
		getTable().addMouseListener(new DoubleClickListener(viewAction));
		
		updateLabels();
		updateActions();
	}
	
	@Override
	public FileStat getSelectedFile()
	{
		int row = getSelectedRow();
		if (row == -1) {
			return null;
		}
		else {
			Document doc = docTableModel.getDocument(row);
			return new FileStat(doc.getFilename(), doc.getFilesize());
		}
	}

	@Override
	protected TableModel getTableModel()
	{
		if (docTableModel == null) {
			docTableModel = new DocTableModel();
		}
		return docTableModel;
	}
	
	@Override
	protected void initializeTableLayout() {
		getTableLayout().setColumnProperties(0, "permissions", 40);
		getTableLayout().setColumnProperties(1, "owner", 40);
		getTableLayout().setColumnProperties(2, "created", 80);
		getTableLayout().setColumnProperties(3, "modified", 80);
		getTableLayout().setColumnProperties(4, "accessed", 80);
		getTableLayout().setColumnProperties(5, "filename", 120);
		getTableLayout().setColumnProperties(6, "filesize", 20);
	}

	public void setData(List<Document> data)
	{
		docTableModel.setData(data);
	}

	@Override
	public void updateActions() {
		FileStat file = getSelectedFile();
		viewAction.setEnabled(file != null);
		saveAction.setEnabled(file != null);
		deleteAction.setEnabled(file != null);
	}

	@Override
	public void updateLabels() {
		super.updateLabels();
		
		viewAction.updateLabels();
		saveAction.updateLabels();
		deleteAction.updateLabels();
		
		getTableLayout().setColumnNames(new String[] {
				i18n.tr("Permissions"),
				i18n.tr("Owner"), 
				i18n.tr("Created"),
				i18n.tr("Modified"),
				i18n.tr("Accessed"),
				i18n.tr("Name"),
				i18n.tr("Size"), }); 
	}

	private static class DocTableModel extends AbstractTableModel {
	
		private static final Class[] columnClasses= {
			Integer.class,
			String.class, 
			Date.class,
			Date.class,
			Date.class,
			String.class,
			Long.class,
		};
		
		private List<Document> data = new ArrayList<Document>(0);
		
		public DocTableModel() {
		}
		
		public Class<?> getColumnClass(int column) {
	        return columnClasses[column];
	    }
	
		public int getColumnCount() {
			return columnClasses.length;
		}
	
		public Document getDocument(int row) {
			return data.get(row);
		}
	
		public int getRowCount()
		{
			return data.size();
		}
		
	    public Object getValueAt(int row, int column)
		{
			Document doc = data.get(row);
			switch (column) {
			case 0:
				return doc.getPermissions();
			case 1:
				return doc.getOwner();
			case 2:
				return doc.getCreationTime();
			case 3:
				return doc.getLastModificationTime();
			case 4:			
				return doc.getLastAccessTime();
			case 5:
				return doc.getFilename();
			case 6:
				return doc.getFilesize();
			default:
				return null;
			}
		}
		
		public void setData(List<Document> data)
		{
			this.data = data;
			fireTableDataChanged();
		}
	
	}

}
