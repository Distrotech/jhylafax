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
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import net.sf.jhylafax.DetailsDialog.Property;
import net.sf.jhylafax.JobHelper.FileStat;
import net.sf.jhylafax.fax.FaxJob;
import org.xnap.commons.gui.Builder;
import org.xnap.commons.gui.Dialogs;
import org.xnap.commons.gui.action.AbstractXNapAction;
import org.xnap.commons.gui.util.DoubleClickListener;

/**
 * A panel that displays a list of jobs. Used for "sendq", "pollq" and "doneq".
 *  
 * @author Steffen Pingel
 */
public class JobQueuePanel extends AbstractQueuePanel {
	
	private DetailsAction detailsAction;
	private EditJobAction editAction;
	private JobTableModel jobTableModel;
	private RemoveJobAction removeAction;
	private ResumeJobAction resumeAction;
	private RetryJobAction retryAction;
	private SuspendJobAction suspendAction;
	private ViewJobAction viewAction;
	
	public JobQueuePanel(String queueName) {
		super(queueName);
		
		removeAction = new RemoveJobAction();
		suspendAction = new SuspendJobAction();
		resumeAction = new ResumeJobAction();
		retryAction = new RetryJobAction();
		detailsAction = new DetailsAction();
		editAction = new EditJobAction();
		viewAction = new ViewJobAction();
		
		// TODO: should not hard code queue names here
		if (queueName.equals("doneq")) {
			getButtonPanel().add(Builder.createButton(viewAction));
			getButtonPanel().add(Builder.createButton(removeAction));

			getTablePopupMenu().add(Builder.createMenuItem(viewAction));
			getTablePopupMenu().add(Builder.createMenuItem(detailsAction));

			getTable().addMouseListener(new DoubleClickListener(viewAction));
		}
		else { // "sendq" / "pollq" (?)
			getButtonPanel().add(Builder.createButton(editAction));
			getButtonPanel().add(Builder.createButton(removeAction));
			getButtonPanel().add(Builder.createButton(suspendAction));
			getButtonPanel().add(Builder.createButton(resumeAction));
			getButtonPanel().add(Builder.createButton(viewAction));
			
			getTablePopupMenu().add(Builder.createMenuItem(editAction));
			getTablePopupMenu().add(Builder.createMenuItem(removeAction));
			getTablePopupMenu().add(Builder.createMenuItem(detailsAction));
			getTablePopupMenu().addSeparator();
			getTablePopupMenu().add(Builder.createMenuItem(retryAction));
			getTablePopupMenu().addSeparator();
			getTablePopupMenu().add(Builder.createMenuItem(suspendAction));
			getTablePopupMenu().add(Builder.createMenuItem(resumeAction));
			getTablePopupMenu().addSeparator();
			getTablePopupMenu().add(Builder.createMenuItem(viewAction));		

			getTable().addMouseListener(new DoubleClickListener(editAction));
		}
		
		updateLabels();
		updateActions();
	}
	
	@Override
	public FileStat getSelectedFile()
	{
		return null;
	}

	public FaxJob getSelectedJob()
	{
		int row = getSelectedRow();
		return (row == -1) ? null : jobTableModel.getJob(row); 
	}
	
	@Override
	protected TableModel getTableModel()
	{
		if (jobTableModel == null) {
			jobTableModel = new JobTableModel();
		}
		return jobTableModel;
	}
	
	protected void initializeTableLayout() {
		getTableLayout().setColumnProperties(0, "id", 20);
		getTableLayout().setColumnProperties(1, "priority", 20);
		getTableLayout().setColumnProperties(2, "result", 20);
		getTableLayout().setColumnProperties(3, "permissions", 40);
		getTableLayout().setColumnProperties(4, "owner", 40);
		getTableLayout().setColumnProperties(5, "sender", 80);
		getTableLayout().setColumnProperties(6, "clientMachine", 80);
		getTableLayout().setColumnProperties(7, "resolution", 40);
		getTableLayout().setColumnProperties(8, "number", 80);
		getTableLayout().setColumnProperties(9, "time", 60);
		getTableLayout().setColumnProperties(10, "pages", 20);
		getTableLayout().setColumnProperties(11, "dials", 20);
		getTableLayout().setColumnProperties(12, "error", 100);
		getTableLayout().setColumnProperties(13, "state", 18);
		getTableLayout().setColumnProperties(14, "cid", 40);
		getTableLayout().setColumnProperties(15, "tag", 40);
	}

	public void setData(List<FaxJob> data) {
		jobTableModel.setData(data);
	}

	@Override
	public void updateActions() {
		FaxJob job = getSelectedJob();
		viewAction.setEnabled(job != null);
		boolean isEditable = (job != null) && job.getID() != -1;
		editAction.setEnabled(isEditable);
		removeAction.setEnabled(isEditable);
		suspendAction.setEnabled(job != null && job.getState() != FaxJob.State.SUSPENDED);
		resumeAction.setEnabled(job != null && job.getState() == FaxJob.State.SUSPENDED);
		retryAction.setEnabled(job != null);
	}

	@Override
	public void updateLabels() {
		super.updateLabels();
		
		removeAction.updateLabels();
		suspendAction.updateLabels();
		resumeAction.updateLabels();
		retryAction.updateLabels();
		detailsAction.updateLabels();
		editAction.updateLabels();
		viewAction.updateLabels();
		
		getTableLayout().setColumnNames(new String[] {
				i18n.tr("ID"),
				i18n.tr("Priority"), 
				i18n.tr("Result"),
				i18n.tr("Permission"),
				i18n.tr("Owner"),
				i18n.tr("Sender"), 
				i18n.tr("Client Machine"), 
				i18n.tr("Resolution"),
				i18n.tr("Number"),
				i18n.tr("Time"),
				i18n.tr("Pages"),
				i18n.tr("Dials"),				
				i18n.tr("Last Error"),
				i18n.tr("State"),
				i18n.tr("CID"),
				i18n.tr("Tag"),}); 
	}

	private class EditJobAction extends AbstractXNapAction {
		
		public EditJobAction() {
			putValue(ICON_FILENAME, "edit.png");
		}

		public void actionPerformed(ActionEvent e)
		{
			// a double click could be triggered despite of the disabled state
			if (!isEnabled()) {
				return;
			}

			FaxJob job = getSelectedJob();
			EditDialog dialog = new EditDialog(JHylaFAX.getInstance(), job);
			dialog.setLocationRelativeTo(JHylaFAX.getInstance());
			dialog.setVisible(true);
		}

		public void updateLabels() {
			putValue(Action.NAME, i18n.tr("Edit..."));
		}

	}

	private class DetailsAction extends AbstractXNapAction {
		
		public DetailsAction() {
			//putValue(ICON_FILENAME, "redo.png");
		}

		public void actionPerformed(ActionEvent e)
		{
			FaxJob job = getSelectedJob();
			List<Property> data = new ArrayList<Property>();
			data.add(new Property(i18n.tr("Assigned modem"), job.getAssignedModem()));
			data.add(new Property(i18n.tr("Client-specefied dial string"), job.getClientDialString()));
			data.add(new Property(i18n.tr("Client machine name"), job.getClientMachineName()));
			data.add(new Property(i18n.tr("Scheduling priority"), job.getClientSchedulingPriority()));
			data.add(new Property(i18n.tr("Communication identifier"), job.getCommunicationIdentifier()));
			data.add(new Property(i18n.tr("Page chopping threshold"), job.getChoppingThreshold()));
			data.add(new Property(i18n.tr("Client-specified minimum signalling rate"), job.getClientMinimumSignallingRate()));
			data.add(new Property(i18n.tr("Client-specified tag"), job.getTag()));
			data.add(new Property(i18n.tr("Client-specified tagline format"), job.getTaglineFormat()));
			data.add(new Property(i18n.tr("# of consecutive failed dials"), job.getConsecutiveFailedDials()));
			data.add(new Property(i18n.tr("# of consecutive failed tries"), job.getConsecutiveFailedTries()));
			data.add(new Property(i18n.tr("Desired data format"), job.getDesiredDataFormat()));
			data.add(new Property(i18n.tr("Desired use of ECM"), job.getDesiredECM()));
			data.add(new Property(i18n.tr("Desired minimum scanline time"), job.getDesiredMinScanline()));
			data.add(new Property(i18n.tr("Desired signalling rate"), job.getDesiredSignallingRate()));
			data.add(new Property(i18n.tr("Destination company name"), job.getDestinationCompanyName()));
			data.add(new Property(i18n.tr("Destination geographic location"), job.getDestinationLocation()));
			data.add(new Property(i18n.tr("Destination password"), job.getDestinationPassword()));
			data.add(new Property(i18n.tr("Destination sub-address"), job.getDestinationSubAddress()));
			data.add(new Property(i18n.tr("# of attempted dials"), job.getDialsAttempted()));
			data.add(new Property(i18n.tr("Group identifier"), job.getGroupID()));
			data.add(new Property(i18n.tr("Horizontal resolution"), job.getHorizontalResolution()));
			data.add(new Property(i18n.tr("ID"), job.getID()));
			data.add(new Property(i18n.tr("Job done operation"), job.getJobDoneOperation()));
			data.add(new Property(i18n.tr("Job type"), job.getJobType()));
			data.add(new Property(i18n.tr("Kill time"), job.getKillTime()));
			data.add(new Property(i18n.tr("Last error"), job.getLastError()));
			data.add(new Property(i18n.tr("Total # of dials"), job.getMaxDials()));
			data.add(new Property(i18n.tr("Total # of tries"), job.getMaxTries()));
			data.add(new Property(i18n.tr("Notify"), job.getNotify()));
			data.add(new Property(i18n.tr("Notify address"), job.getNotifyAdress()));
			data.add(new Property(i18n.tr("Number"), job.getNumber()));
			data.add(new Property(i18n.tr("Owner"), job.getOwner()));
			data.add(new Property(i18n.tr("Page chopping"), job.getPageChopping()));
			data.add(new Property(i18n.tr("Permissions"), job.getPermissions()));
			data.add(new Property(i18n.tr("Total # of pages"), job.getPageCount()));
			data.add(new Property(i18n.tr("Page length"), job.getPageLength()));
			data.add(new Property(i18n.tr("# of transmitted pages"), job.getPagesTransmitted()));
			data.add(new Property(i18n.tr("Page width"), job.getPageWidth()));
			data.add(new Property(i18n.tr("Priority"), job.getPriority()));
			data.add(new Property(i18n.tr("Receiver"), job.getReceiver()));
			data.add(new Property(i18n.tr("Result"), job.getResult()));
			data.add(new Property(i18n.tr("Retry time"), job.getRetryTime()));
			data.add(new Property(i18n.tr("Sender"), job.getSender()));
			data.add(new Property(i18n.tr("Send time"), job.getSendTime()));
			data.add(new Property(i18n.tr("State"), job.getState()));
			data.add(new Property(i18n.tr("Vertical resolution"), job.getVerticalResolution()));
			DetailsDialog dialog = new DetailsDialog(JHylaFAX.getInstance(), data);
			dialog.setLocationRelativeTo(JHylaFAX.getInstance());
			dialog.setVisible(true);
		}
		
		public void updateLabels() {
			putValue(Action.NAME, i18n.tr("Details"));
		}
	}

	private static class JobTableModel extends AbstractTableModel {

		private static final Class[] columnClasses= {
			Integer.class,
			Integer.class,
			String.class, 
			String.class,
			String.class,
			String.class,
			String.class,
			Integer.class,
			String.class,
			Date.class,
			Integer.class, 
			String.class,
			String.class,
			FaxJob.State.class,
			String.class,
			String.class,
		};
		
		private List<FaxJob> data = new ArrayList<FaxJob>();
		
		public JobTableModel() 
		{
		}
		
		public Class<?> getColumnClass(int column) {
	        return columnClasses[column];
	    }
	
		public int getColumnCount()
		{
			return columnClasses.length;
		}

		public FaxJob getJob(int row) {
			return data.get(row);
		}

		public int getRowCount()
		{
			return data.size();
		}
		
	    public Object getValueAt(int row, int column) {
	    	FaxJob job = data.get(row);
			switch (column) {
			case 0:
				return job.getID();
			case 1:
				return job.getPriority();
			case 2:
				return job.getResult();
			case 3:
				return job.getPermissions();
			case 4:
				return job.getOwner();
			case 5:
				return job.getSender();
			case 6:
				return job.getClientMachineName();
			case 7:
				return job.getVerticalResolution();
			case 8:
				return job.getNumber();
			case 9:
				return job.getSendTime();
			case 10:
				return job.getPageCount();
			case 11:
				return job.getDialsAttempted() + "/" + job.getMaxDials();
			case 12:
				return job.getLastError();
			case 13:
				return job.getState();
			case 14:
				return job.getCommunicationIdentifier();
			case 15:
				return job.getTaglineFormat(); // FIME display tag
			default:
				return null;
			}
		}
		
		public void setData(List<FaxJob> data)
		{
			this.data = data;
			fireTableDataChanged();
		}

	}

	private class RemoveJobAction extends AbstractXNapAction {
		
		public RemoveJobAction() {
			putValue(ICON_FILENAME, "editdelete.png");
		}

		public void actionPerformed(ActionEvent event) {
			FaxJob job = getSelectedJob();
			if (Dialogs.showConfirmDialog(JHylaFAX.getInstance(), 
					i18n.tr("Do you really want to delete the job with id {0}?", job.getID()),
					i18n.tr("Remove Job"), 
					JOptionPane.YES_NO_OPTION, 
					Settings.CONFIRM_DELETE) == JOptionPane.YES_OPTION) {
				JobHelper.removeJob(job.getID());
			}
		}
		
		public void updateLabels() {
			putValue(Action.NAME, i18n.tr("Remove"));
		}
	}

	private class ResumeJobAction extends AbstractXNapAction {
		
		public ResumeJobAction() {
			putValue(ICON_FILENAME, "player_play.png");
		}

		public void actionPerformed(ActionEvent event)
		{
			FaxJob job = getSelectedJob();
			JobHelper.resumeJob(job.getID());
		}

		public void updateLabels() {
			putValue(Action.NAME, i18n.tr("Resume"));
		}

	}

	private class RetryJobAction extends AbstractXNapAction {
		
		public RetryJobAction() {
			putValue(ICON_FILENAME, "redo.png");
		}

		public void actionPerformed(ActionEvent e)
		{
			FaxJob job = getSelectedJob();
			JobHelper.retryJob(job.getID());
		}
		
		public void updateLabels() {
			putValue(Action.NAME, i18n.tr("Send Now"));
		}
	}

	private class SuspendJobAction extends AbstractXNapAction {
		
		public SuspendJobAction() {
			putValue(ICON_FILENAME, "player_pause.png");
		}

		public void actionPerformed(ActionEvent event)
		{
			FaxJob job = getSelectedJob();
			JobHelper.suspendJob(job.getID());
		}
		
		public void updateLabels() {
			putValue(Action.NAME, i18n.tr("Suspend"));
		}
	}

	private class ViewJobAction extends AbstractXNapAction {
		
		public ViewJobAction() {
			putValue(ICON_FILENAME, "viewmag.png");
		}

		public void actionPerformed(ActionEvent event)
		{
			FaxJob job = getSelectedJob();
			if (job == null) {
				return;
			}
			
			// TODO should find a better way to determine viewer
			String viewerPath = JHylaFAXHelper.getViewerPath(getQueueName());
			if (viewerPath != null) {
				FileStat[] files = JobHelper.retrieveJobFilenames(job.getID());
				if (files != null) {
					if (files.length == 0) {
						Dialogs.showInfo(JHylaFAX.getInstance(), 
								i18n.tr("The job does not contain documents"), 
								i18n.tr("JHylaFAX Information"));
					}
					else {
						File[] tempFiles = new File[files.length];
						for (int i = 0; i < files.length; i++) {
							tempFiles[i] = createTempFile(files[i].filename);
							if (tempFiles[i] == null) {
								return;
							}
							if (!JobHelper.save(tempFiles[i], files[i].filename, files[i].filesize)) {
								// user abort or error
								return;
							}
						}
						JHylaFAXHelper.view(viewerPath, tempFiles);
					}
				}
			}
		}

		public void updateLabels() {
			putValue(Action.NAME, i18n.tr("View"));
		}
		
	}

}
