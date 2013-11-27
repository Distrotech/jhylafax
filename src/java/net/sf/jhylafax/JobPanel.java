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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import net.sf.jhylafax.Settings.Notification;
import net.sf.jhylafax.Settings.Resolution;
import net.sf.jhylafax.fax.FaxJob;
import net.sf.jhylafax.fax.Paper;
import org.xnap.commons.gui.Dialogs;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A panel for editing of fax job properties.
 * 
 * @author Steffen Pingel
 */
public class JobPanel extends JPanel implements LocaleChangeListener {

	private static final int DEFAULT_COLUMNS = 20;
	
	private JLabel notificationLabel;
	private JLabel resolutionLabel;
	private JSpinner prioritySpinner;
	private JLabel priorityLabel;
	private JSpinner maxTriesSpinner;
	private JLabel maxTriesLabel;
	private JSpinner maxDialsSpinner;
	private JLabel maxDialsLabel;
	private JLabel paperLabel;
	private DefaultComboBoxModel notificationModel;
	private DefaultComboBoxModel resolutionModel;
	private DefaultComboBoxModel paperModel;
	private JTextField fullnameTextField;
	private JTextField emailTextField;
	private JLabel fullnameLabel;
	private JLabel emailLabel;

	private SpinnerNumberModel priorityModel;

	public JobPanel(boolean border) {
		FormLayout layout = new FormLayout("left:max(40dlu;pref), 3dlu, pref, pref:grow, min:grow", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
		if (border) {
			builder.setDefaultDialogBorder();
		}
		builder.appendSeparator(i18n.tr("Sender"));
		
		fullnameTextField = new JTextField(DEFAULT_COLUMNS);
		fullnameLabel = builder.append("", fullnameTextField, 3);
		builder.nextLine();

		emailTextField = new JTextField(DEFAULT_COLUMNS);
		emailLabel = builder.append("", emailTextField, 3);
		builder.nextLine();
		
		builder.appendSeparator(i18n.tr("Parameter"));

		notificationModel = new DefaultComboBoxModel();
		JComboBox notificationComboBox = new JComboBox(notificationModel);
		notificationLabel = builder.append("", notificationComboBox, 2);
		builder.nextLine();
		
		resolutionModel = new DefaultComboBoxModel();
		JComboBox resolutionComboBox = new JComboBox(resolutionModel);
		resolutionLabel = builder.append("", resolutionComboBox, 2);
		builder.nextLine();

		priorityModel = new SpinnerNumberModel(0, 0, 255, 1);
		prioritySpinner = new JSpinner(priorityModel);
		priorityLabel = builder.append("", prioritySpinner);
		builder.nextLine();
		
		maxTriesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 255, 1));
		maxTriesLabel = builder.append("", maxTriesSpinner);
		builder.nextLine();
		
		maxDialsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 255, 1));
		maxDialsLabel = builder.append("", maxDialsSpinner);
		builder.nextLine();
		
		paperModel = new DefaultComboBoxModel();
		JComboBox paperComboBox = new JComboBox(paperModel);
		paperLabel = builder.append("", paperComboBox, 2);
	}
	
	public void setSenderEditable(boolean editSender) {
		fullnameTextField.setEditable(editSender);
		emailTextField.setEditable(editSender);
	}
	
	public void updateLabels() {
		fullnameLabel.setText(i18n.tr("Name"));
		emailLabel.setText(i18n.tr("Email"));

		notificationLabel.setText(i18n.tr("Notify"));
		resolutionLabel.setText(i18n.tr("Resolution"));
		priorityLabel.setText(i18n.tr("Priority"));
		maxTriesLabel.setText(i18n.tr("Maximum Tries"));
		maxDialsLabel.setText(i18n.tr("Maximum Dials"));
		paperLabel.setText(i18n.tr("Paper Format"));
		
		Object selectedItem = notificationModel.getSelectedItem();
		notificationModel.removeAllElements();
		for (Settings.Notification notification : Settings.Notification.values()) {
			notificationModel.addElement(notification);
		}
		notificationModel.setSelectedItem(selectedItem);
		selectedItem = resolutionModel.getSelectedItem();
		resolutionModel.removeAllElements();
		for (Settings.Resolution resolution : Settings.Resolution.values()) {
			resolutionModel.addElement(resolution);
		}
		resolutionModel.setSelectedItem(selectedItem);
		selectedItem = paperModel.getSelectedItem();
		paperModel.removeAllElements();
		for (Paper paper : Paper.values()) {
			paperModel.addElement(paper);
		}
		paperModel.setSelectedItem(selectedItem);
	}

	public void applyTo(FaxJob job) {
		job.setSender(fullnameTextField.getText());
		job.setNotifyAdress(emailTextField.getText());
		
		job.setNotify(((Notification)notificationModel.getSelectedItem()).getCommand());
		job.setResolution((((Resolution)resolutionModel.getSelectedItem()).getLinesPerInch()));
		job.setPriority(priorityModel.getNumber().intValue());
		job.setMaxTries(((Integer)maxTriesSpinner.getValue()).intValue());
		job.setMaxDials(((Integer)maxDialsSpinner.getValue()).intValue());
		Paper paper = (Paper)paperModel.getSelectedItem();
		job.setPageWidth(paper.getWidth());
		job.setPageLength(paper.getHeight());

	}
	
	public void revertFrom(FaxJob job) {
		fullnameTextField.setText(job.getSender());
		emailTextField.setText(job.getNotifyAdress());

		if (job.getNotify() == null) {
			notificationModel.setSelectedItem(Settings.Notification.NEVER);
		}
		else {
			try {
				notificationModel.setSelectedItem(Settings.Notification.getEnum(job.getNotify()));
			} catch (IllegalArgumentException e) {
				Dialogs.showError(this, i18n.tr("Unknown notification type. Using default."));
				notificationModel.setSelectedItem(Settings.NOTIFICATION.getValue());
			}
		}
		try {
			resolutionModel.setSelectedItem(Settings.Resolution.getEnum(job.getVerticalResolution()));
		} catch (IllegalArgumentException e) {
			Dialogs.showError(this, i18n.tr("Invalid resolution. Using default."));
			resolutionModel.setSelectedItem(Settings.RESOLUTION.getValue());
		}
		prioritySpinner.setValue(job.getPriority());
		maxTriesSpinner.setValue(job.getMaxTries());
		maxDialsSpinner.setValue(job.getMaxDials());
		try {
			paperModel.setSelectedItem(Paper.getEnum(job.getPageWidth(), job.getPageLength()));
		} catch (IllegalArgumentException e) {
			Dialogs.showError(this, i18n.tr("Unknown paper type. Using default."));
			paperModel.setSelectedItem(Settings.PAPER.getValue());
		}
	}
	
	public void revertFromSettings() {
		fullnameTextField.setText(Settings.FULLNAME.getValue());
		emailTextField.setText(Settings.EMAIL.getValue());

		notificationModel.setSelectedItem(Settings.NOTIFICATION.getValue());
		resolutionModel.setSelectedItem(Settings.RESOLUTION.getValue());
		prioritySpinner.setValue(Settings.PRIORITY.getValue());
		maxTriesSpinner.setValue(Settings.MAXTRIES.getValue());
		maxDialsSpinner.setValue(Settings.MAXDIALS.getValue());
		paperModel.setSelectedItem(Settings.PAPER.getValue());
	}

	public void applyToSettings()
	{
		Settings.FULLNAME.setValue(fullnameTextField.getText());
		Settings.EMAIL.setValue(emailTextField.getText());
		
		Settings.NOTIFICATION.setValue((Notification)notificationModel.getSelectedItem());
		Settings.RESOLUTION.setValue((Resolution)resolutionModel.getSelectedItem());
		Settings.PRIORITY.setValue(priorityModel.getNumber().intValue());
		Settings.MAXTRIES.setValue((Integer)maxTriesSpinner.getValue());
		Settings.MAXDIALS.setValue((Integer)maxDialsSpinner.getValue());
		Settings.PAPER.setValue((Paper)paperModel.getSelectedItem());
	}

	public void revertFromDefaultSettings()
	{
		fullnameTextField.setText(Settings.FULLNAME.getDefaultValue());
		emailTextField.setText(Settings.EMAIL.getDefaultValue());

		notificationModel.setSelectedItem(Settings.NOTIFICATION.getDefaultValue());
		resolutionModel.setSelectedItem(Settings.RESOLUTION.getDefaultValue());
		prioritySpinner.setValue(Settings.PRIORITY.getDefaultValue());
		maxTriesSpinner.setValue(Settings.MAXTRIES.getDefaultValue());
		maxDialsSpinner.setValue(Settings.MAXDIALS.getDefaultValue());
		paperModel.setSelectedItem(Settings.PAPER.getDefaultValue());
	}
	
}
