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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.sf.jhylafax.addressbook.AbstractContactTransferHandler;
import net.sf.jhylafax.fax.FaxHelper;
import net.sf.jhylafax.fax.FaxJob;
import net.wimpi.pim.contact.facades.SimpleContact;
import net.wimpi.pim.contact.model.Contact;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xnap.commons.gui.Builder;
import org.xnap.commons.gui.DefaultDialog;
import org.xnap.commons.gui.Dialogs;
import org.xnap.commons.gui.action.AbstractXNapAction;
import org.xnap.commons.gui.completion.Completion;
import org.xnap.commons.gui.completion.DefaultCompletionModel;
import org.xnap.commons.settings.CompletionSettingDirector;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public abstract class AbstractFaxDialog extends DefaultDialog implements LocaleChangeListener {

	private final static Log logger = LogFactory.getLog(AbstractQueuePanel.class);
	public static final int DEFAULT_COLUMNS = 20;
	
	private JobPanel jobPanel;
	private SpinnerDateModel dateModel;
	private FaxJob job;
	private JLabel dateLabel;
	private ParameterDialogAction parameterAction;
	private JRadioButton dateNowRadionButton;
	private JRadioButton dateLaterRadionButton;
	protected DefaultFormBuilder builder;
	private JTextField numberTextField;
	private JLabel numberLabel;
	private Completion numberCompletion;
	private DefaultCompletionModel numberCompletionModel;
	private AddressBookAction addressBookAction;
	
	public AbstractFaxDialog(JFrame owner) {
		super(owner, BUTTON_OKAY | BUTTON_CANCEL);
	
		FormLayout layout = new FormLayout("left:max(40dlu;pref), 3dlu, pref, 3dlu, min, min:grow", "");
		builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		setMainComponent(builder.getPanel());
		
		parameterAction = new ParameterDialogAction();
		getButtonPanel().add(Builder.createButton(parameterAction), 0);
	}

	protected void addNumberTextField() {
		Box box = Box.createHorizontalBox();

		addressBookAction = new AddressBookAction();
		
		numberTextField = new JTextField();
		numberTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, numberTextField.getPreferredSize().height));
		numberTextField.setTransferHandler(new ContactTransferHandler());
		//numberTextField.setDragEnabled(true);
		box.add(numberTextField);
		box.add(Box.createHorizontalStrut(4));
		box.add(Builder.createIconButton(addressBookAction));
		numberLabel = builder.append("", box, 4);
		builder.nextLine();

		numberCompletionModel = new DefaultCompletionModel();
		numberCompletion = Builder.addCompletion(numberTextField, numberCompletionModel);

		new CompletionSettingDirector(Settings.backstore, "number").restore(numberCompletion);
	}			

	protected void addDateControls() {
		dateNowRadionButton = new JRadioButton();
		dateNowRadionButton.setSelected(true);
		dateLabel = builder.append("", dateNowRadionButton);
		builder.nextLine();
		
		dateLaterRadionButton = new JRadioButton();
		dateModel = new SpinnerDateModel();
		final JSpinner dateSpinner = new JSpinner(dateModel);
		dateSpinner.setEnabled(false);
		dateLaterRadionButton.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				dateSpinner.setEnabled(dateLaterRadionButton.isSelected());
			}			
		});
		builder.append("", dateLaterRadionButton, dateSpinner);
		builder.nextLine();
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(dateNowRadionButton);
		buttonGroup.add(dateLaterRadionButton);		
	}
	
	@Override
	public boolean apply() {
		if (numberTextField != null) {
			if (numberTextField.getText().trim().length() == 0) {
				Dialogs.showError(this, i18n.tr("Please enter a number"), 
						i18n.tr("JHylaFAX Error"));
				numberTextField.requestFocus();
				return false;
			}
			getJob().setNumber(FaxHelper.extractNumber(numberTextField.getText().trim()));
			numberCompletionModel.insert(numberTextField.getText());
			new CompletionSettingDirector(Settings.backstore, "number").save(numberCompletion);
		}
		
		if (dateNowRadionButton != null) {
			job.setSendTime((dateNowRadionButton.isSelected()) 
					? null 
					: dateModel.getDate());
		}
		return true;
	}
	
	public FaxJob getJob() {
		return job;
	}
	
	public void updateLabels() {
		if (numberTextField != null) {
			numberLabel.setText(i18n.tr("Number"));
		}
		if (dateNowRadionButton != null) {
			dateLabel.setText(i18n.tr("Date"));
			dateNowRadionButton.setText(i18n.tr("Now"));
			dateLaterRadionButton.setText(i18n.tr("Later"));
		}		
		parameterAction.updateLabels();
	}
	
	public void revert() {
		if (numberTextField != null) {
			numberTextField.setText(getJob().getNumber());
		}
		if (dateNowRadionButton != null) {
			if (getJob().getSendTime() == null) {
				dateNowRadionButton.setSelected(true);
			}
			else {
				dateLaterRadionButton.setSelected(true);
				dateModel.setValue(job.getSendTime());
			}
		}
	}

	public void setJob(FaxJob job) {
		this.job = job;
	}
	
	public void setNumber(String number) {
		if (numberTextField != null) {
			numberTextField.setText(number);
		}
	}
	
	private class ParameterDialogAction extends AbstractXNapAction implements LocaleChangeListener{
		
		private JobDialog dialog;
		
		public ParameterDialogAction() {
			//putValue(ICON_FILENAME, "configure.png");
		}

		public void actionPerformed(ActionEvent e)
		{
			if (job == null) {
				return;
			}
			
			if (dialog == null) {
				dialog = new JobDialog(job);
				dialog.setLocationRelativeTo(AbstractFaxDialog.this);
				dialog.setModal(true);
			}
			
			if (!dialog.isVisible()) {
				dialog.revert();
				dialog.getJobPanel().setSenderEditable(getJob().getID() == -1);
				dialog.setVisible(true);
			}
		}
		
		public void updateLabels() {
			parameterAction.putValue(Action.NAME, i18n.tr("Parameter..."));
			parameterAction.putValue(Action.SHORT_DESCRIPTION, i18n.tr("Opens a dialog for entering job parameter"));

			if (dialog != null) {
				dialog.updateLabels();
			}
		}
		
	}

	private class InsertNumberAction extends AbstractAction {
		
		private boolean visible;

		public InsertNumberAction() {
			visible = JHylaFAX.getInstance().getAddressBook().isVisible();
		}

		public void actionPerformed(ActionEvent e)
		{
			SimpleContact[] contacts = JHylaFAX.getInstance().getAddressBook().getSelectedContacts();
			if (contacts.length > 0) {
				// TODO this is locale dependent
				String receipient = add("", "", contacts[0].getFirstname());
				receipient = add(receipient, " ", contacts[0].getLastname());
				receipient = add(receipient, ", ", contacts[0].getCompany());
				receipient += " <" + contacts[0].getFaxNumber() + ">";
				 
				setNumber(receipient);
			}
			JHylaFAX.getInstance().getAddressBook().setDoubleClickAction(null);
			JHylaFAX.getInstance().getAddressBook().setVisible(false);
			
			AbstractFaxDialog.this.requestFocus();
		 }
	
			public String add(String input, String glue, String token) {
				if (token != null) {
					return input + glue + token;
				}
				return input;
			}
		
	}

	private class AddressBookAction extends AbstractXNapAction {
		
		public AddressBookAction() {
			putValue(ICON_FILENAME, "contents.png");
		}

		public void actionPerformed(ActionEvent e)
		{
			JHylaFAX.getInstance().getAddressBook().setDoubleClickAction(new InsertNumberAction());
			JHylaFAX.getInstance().getAddressBook().setVisible(true);
		}
	
	}

	private class ContactTransferHandler extends AbstractContactTransferHandler {

		@Override
		public void importData(Contact[] contacts)
		{
			SimpleContact contact = new SimpleContact(contacts[0]);
			setNumber(contact.getFaxNumber());
		}

	}

}
