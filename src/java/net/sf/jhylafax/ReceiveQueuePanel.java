package net.sf.jhylafax;

import static net.sf.jhylafax.JHylaFAX.i18n;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import net.sf.jhylafax.JobHelper.FileStat;
import net.sf.jhylafax.fax.ReceivedFax;
import org.xnap.commons.gui.Builder;
import org.xnap.commons.gui.util.DoubleClickListener;

public class ReceiveQueuePanel extends AbstractQueuePanel {

	private ViewAction viewAction;
	private SaveAction saveAction;
	private DeleteAction deleteAction;
	private FaxTableModel faxTableModel;

	public ReceiveQueuePanel(String queueName) {
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
			ReceivedFax doc = faxTableModel.getDocument(row);
			return new FileStat(doc.getFilename(), doc.getFilesize());
		}
	}

	@Override
	protected TableModel getTableModel()
	{
		if (faxTableModel == null) {
			faxTableModel = new FaxTableModel();
		}
		return faxTableModel;
	}
	
	@Override
	protected void initializeTableLayout() {
		getTableLayout().setColumnProperties(0, "sender", 150);
		getTableLayout().setColumnProperties(1, "pages", 40);
		getTableLayout().setColumnProperties(2, "time", 40);
		getTableLayout().setColumnProperties(3, "filename", 100);
		getTableLayout().setColumnProperties(4, "filesize", 40);
		getTableLayout().setColumnProperties(5, "owner", 40);
		getTableLayout().setColumnProperties(6, "resolution", 40);
		getTableLayout().setColumnProperties(7, "singallingRate", 40);
		getTableLayout().setColumnProperties(8, "receiving", 40);
		getTableLayout().setColumnProperties(9, "error", 100);
	}

	public void setData(List<ReceivedFax> data)
	{
		faxTableModel.setData(data);
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
				i18n.tr("Sender"),
				i18n.tr("Pages"), 
				i18n.tr("Time"),
				i18n.tr("Filename"),
				i18n.tr("Filesize"),
				i18n.tr("Owner"),
				i18n.tr("Resolution"),
				i18n.tr("Signalling Rate"),
				i18n.tr("Receiving"),
				i18n.tr("Error"),});
	}

	private static class FaxTableModel extends AbstractTableModel {
	
		private static final Class[] columnClasses= {
			String.class, 
			Integer.class,
			Date.class,
			String.class,
			Long.class,
			String.class,
			Integer.class,
			Integer.class,
			Boolean.class,
			String.class,
		};
		
		private List<ReceivedFax> data = new ArrayList<ReceivedFax>(0);
		
		public FaxTableModel() {
		}
		
		public Class<?> getColumnClass(int column) {
	        return columnClasses[column];
	    }
	
		public int getColumnCount() {
			return columnClasses.length;
		}
	
		public ReceivedFax getDocument(int row) {
			return data.get(row);
		}
	
		public int getRowCount()
		{
			return data.size();
		}
		
	    public Object getValueAt(int row, int column)
		{
			ReceivedFax fax = data.get(row);
			switch (column) {
			case 0:
				return fax.getSender();
			case 1:
				return fax.getPageCount();
			case 2:
				return fax.getReceivedTime();
			case 3:
				return fax.getFilename();
			case 4:
				return fax.getFilesize();
			case 5:
				return fax.getOwner();
			case 6:
				return fax.getResolution();
			case 7:
				return fax.getSignallingRate();
			case 8:
				return fax.isReceiving();
			case 9:
				return fax.getLastError();
			default:
				return null;
			}
		}
		
		public void setData(List<ReceivedFax> data)
		{
			this.data = data;
			fireTableDataChanged();
		}
	
	}

}
