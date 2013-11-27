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
import java.awt.Color;
import java.util.Locale;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import org.xnap.commons.gui.DefaultDialog;
import org.xnap.commons.gui.FileChooserPanel;
import org.xnap.commons.gui.completion.CompletionModeFactory;
import org.xnap.commons.gui.completion.CompletionModeFactory.CompletionModeInfo;
import org.xnap.commons.gui.settings.SettingComponentMediator;
import org.xnap.commons.gui.util.EnableListener;
import org.xnap.commons.gui.util.WhatsThis;
import org.xnap.commons.util.PortRange;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A dialog for editing of global settings.
 * 
 * @author Steffen Pingel
 */
public class SettingsDialog extends DefaultDialog implements LocaleChangeListener {

	private JPanel connectionPanel;
	private JLabel hostnameLabel;
	private JTextField hostnameTextField;
	private JPanel jobPanel;
	private JobPanel jobParameterPanel;
	private JTabbedPane mainTabbedPane;
	private JLabel passwordLabel;
	private JTextField passwordTextField;
	private JLabel portLabel;
	private JSpinner portSpinner;
	private JLabel usernameLabel;
	private JTextField usernameTextField;
	private JLabel languageLabel;
	private DefaultComboBoxModel languageModel;
	private JCheckBox confirmNonPSCheckBox;
	private JCheckBox confirmDeleteCheckBox;
	private JRadioButton internalCoverRadionButton;
	private JLabel viewerPathLabel;
	private JLabel docViewerPathLabel;
	private JPanel pathsPanel;
	private JPanel monitorPanel;
	private JRadioButton externalCoverRadionButton;
	private FileChooserPanel externalCoverPathFileChooserPanel;
	private ExecutableChooserPanel viewerPathFileChooserPanel;
	private ExecutableChooserPanel docViewerPathFileChooserPanel;
	private JLabel passwordInfoLabel;
	private JCheckBox usePassiveCheckBox;
	private JPanel generalPanel;
	private SpinnerNumberModel portSpinnerModel;
	private DefaultComboBoxModel completionModeModel;
	private JLabel completionModeLabel;
	private JCheckBox sendCoverAsDocumentCheckBox;
	private JCheckBox updateOnStartupCheckBox;
	private JCheckBox showPollqCheckBox;
	private SettingComponentMediator settingMediator = new SettingComponentMediator();
	private JCheckBox adminModeCheckBox;
	private JPasswordField adminPasswordTextField;
	private JCheckBox autoUpdateCheckBox;
	private SpinnerNumberModel autoUpdateIntervalModel;
	private JSpinner autoUpdateIntervalSpinner;
	private JLabel autoUpdateIntervalLabel;
	private JCheckBox monitorPathCheckBox;
	private SpinnerNumberModel monitorPathIntervalModel;
	private JSpinner monitorPathIntervalSpinner;
	private JLabel monitorPathIntervalLabel;
	private ExecutableChooserPanel monitorPathFileChooserPanel;
	private JLabel monitorPathLabel;
	private FileChooserPanel addressBookPathFileChooserPanel;
	private JCheckBox customizeAddressBookCheckBox;
	
	public SettingsDialog(JFrame owner) {
		super(owner, BUTTON_OKAY | BUTTON_APPLY | BUTTON_CANCEL | BUTTON_CONTEXT_HELP);
		
		mainTabbedPane = new JTabbedPane();
		setMainComponent(mainTabbedPane);
		
		initializeConnectionForm();
		initializeGeneralForm();
		initializeJobForm();
		initializePathForm();
		initializeMonitorForm();
		
		updateLabels();
		revert();
		
		pack();
	}
	
	@Override
	public boolean apply() {
		settingMediator.apply();
		
		Settings.LOCALE.setValue((Locale)languageModel.getSelectedItem());
		Settings.DEFAULT_COMPLETION_MODE.setValue(((CompletionModeInfo)completionModeModel.getSelectedItem()).getClassName());
		
		jobParameterPanel.applyToSettings();
		
		Settings.VIEWER_PATH.setValue(viewerPathFileChooserPanel.getTextField().getText());
		Settings.DOC_VIEWER_PATH.setValue(docViewerPathFileChooserPanel.getTextField().getText());
		Settings.USE_INTERNAL_COVER.setValue(internalCoverRadionButton.isSelected());
		Settings.COVER_PATH.setValue(externalCoverPathFileChooserPanel.getTextField().getText());
		Settings.MONITOR_PATH_INTERVAL.setValue(monitorPathIntervalModel.getNumber().intValue());
		
		// XXX this is so wrong
		JHylaFAX.getInstance().settingsUpdated();
		
		return true;
	}

	private DefaultFormBuilder createForm() {
		FormLayout layout = new FormLayout("left:max(40dlu;pref), 3dlu, pref, pref:grow(0.5), pref:grow(0.5)", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		return builder;
	}

	public void initializeConnectionForm() {
		DefaultFormBuilder builder = createForm();
		connectionPanel = builder.getPanel();
		mainTabbedPane.add(connectionPanel);
		
		builder.appendSeparator(i18n.tr("HylaFAX Server"));
		
		hostnameTextField = new JTextField(Constants.DEFAULT_COLUMNS);
		settingMediator.add(Settings.HOSTNAME, hostnameTextField);
		hostnameLabel = builder.append("", hostnameTextField, 3);
		builder.nextLine();
		
		portSpinnerModel = new SpinnerNumberModel();
		portSpinnerModel.setMinimum(PortRange.MIN_PORT);
		portSpinnerModel.setMaximum(PortRange.MAX_PORT);
		settingMediator.add(Settings.PORT, portSpinnerModel);
		portSpinner = new JSpinner(portSpinnerModel);
		portSpinner.setEditor(new JSpinner.NumberEditor(portSpinner, "#"));
		portLabel = builder.append("", portSpinner);
		builder.nextLine();
		
		usePassiveCheckBox = new JCheckBox();
		settingMediator.add(Settings.USE_PASSIVE, usePassiveCheckBox);
		builder.append("", usePassiveCheckBox, 3);
		builder.nextLine();

		builder.appendSeparator(i18n.tr("Authentication"));
		
		usernameTextField = new JTextField(Constants.DEFAULT_COLUMNS);
		settingMediator.add(Settings.USERNAME, usernameTextField);
		usernameLabel = builder.append("", usernameTextField, 3);
		builder.nextLine();

		passwordInfoLabel = new JLabel();
		passwordInfoLabel.setForeground(Color.RED);
		builder.append(passwordInfoLabel, 5);
		builder.nextLine();

		passwordTextField = new JPasswordField(Constants.DEFAULT_COLUMNS);
		settingMediator.add(Settings.PASSWORD, passwordTextField);
		passwordLabel = builder.append("", passwordTextField, 3);
		builder.nextLine();
		
		adminModeCheckBox = new JCheckBox();
		settingMediator.add(Settings.ADMIN_MODE, adminModeCheckBox);		
		adminPasswordTextField = new JPasswordField(Constants.DEFAULT_COLUMNS);
		settingMediator.add(Settings.ADMIN_PASSWORD, adminPasswordTextField);
		builder.append(adminModeCheckBox); builder.append(adminPasswordTextField, 3);
		builder.nextLine();

		adminModeCheckBox.addItemListener(new EnableListener(adminPasswordTextField));
		
		builder.appendSeparator(i18n.tr("Status Update"));

		updateOnStartupCheckBox = new JCheckBox();
		settingMediator.add(Settings.UPDATE_ON_STARTUP, updateOnStartupCheckBox);
		builder.append(updateOnStartupCheckBox, 4);
		builder.nextLine();

		autoUpdateCheckBox = new JCheckBox();
		autoUpdateCheckBox.setEnabled(false);
		settingMediator.add(Settings.DO_AUTO_UPDATE, autoUpdateCheckBox);
		// the auto updater is error prone, therefore it has been disabled
		// until fixed
		//builder.append(autoUpdateCheckBox, 4);
		builder.nextLine();
		
		autoUpdateIntervalModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 30);
		settingMediator.add(Settings.AUTO_UPDATE_INTERVAL, autoUpdateIntervalModel);
		autoUpdateIntervalSpinner = new JSpinner(autoUpdateIntervalModel);
		autoUpdateIntervalSpinner.setEditor(new JSpinner.NumberEditor(autoUpdateIntervalSpinner, "# s"));
		//autoUpdateIntervalLabel = builder.append("", autoUpdateIntervalSpinner);
		autoUpdateIntervalLabel = new JLabel();
		builder.nextLine();		

		autoUpdateCheckBox.addItemListener(new EnableListener(autoUpdateIntervalSpinner));
	}
	
	public void initializeGeneralForm() {
		DefaultFormBuilder builder = createForm();
		generalPanel = builder.getPanel();
		mainTabbedPane.add(generalPanel);

		builder.appendSeparator(i18n.tr("Appearance"));

		showPollqCheckBox = new JCheckBox();
		settingMediator.add(Settings.SHOW_POLLQ, showPollqCheckBox);
		builder.append(showPollqCheckBox, 5);
		builder.nextLine();
		
		languageModel = new DefaultComboBoxModel();
		JComboBox lanuageComboBox = new JComboBox(languageModel);
		lanuageComboBox.setRenderer(new DefaultListCellRenderer() {
			public java.awt.Component getListCellRendererComponent(javax.swing.JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value != null) {
					setText(((Locale)value).getDisplayName());
				}
				return this;
			}
		});
		languageLabel = builder.append("", lanuageComboBox, 2);
		builder.nextLine();
		
		builder.appendSeparator(i18n.tr("Behavior"));
		
		completionModeModel = new DefaultComboBoxModel();
		JComboBox completionModeComboBox = new JComboBox(completionModeModel);
		completionModeComboBox.setRenderer(new DefaultListCellRenderer() {
			public java.awt.Component getListCellRendererComponent(javax.swing.JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value != null) {
					setText(((CompletionModeFactory.CompletionModeInfo)value).getName());
				}
				return this;
			}
		});
		completionModeLabel = builder.append("", completionModeComboBox, 2);
		builder.nextLine();
		
		builder.appendSeparator(i18n.tr("Confirmations"));
		
		confirmNonPSCheckBox = new JCheckBox();
		settingMediator.add(Settings.CONFIRM_NONPS, confirmNonPSCheckBox);
		builder.append(confirmNonPSCheckBox, 5);
		builder.nextLine();
		
		confirmDeleteCheckBox = new JCheckBox();
		settingMediator.add(Settings.CONFIRM_DELETE, confirmDeleteCheckBox);
		builder.append(confirmDeleteCheckBox, 5);
		builder.nextLine();
	}
	
	private void initializeJobForm() {
		DefaultFormBuilder builder = createForm();
		jobPanel = builder.getPanel();
		mainTabbedPane.add(jobPanel);
		
		jobParameterPanel = new JobPanel(false);
		builder.append(jobParameterPanel, 5);
	}
	
	public void initializePathForm() {
		DefaultFormBuilder builder = createForm();
		pathsPanel = builder.getPanel();
		mainTabbedPane.add(pathsPanel);
		
		builder.appendSeparator(i18n.tr("Programs"));
		
		viewerPathFileChooserPanel = new ExecutableChooserPanel(Constants.DEFAULT_COLUMNS);
		viewerPathFileChooserPanel.setDialogParent(this);
		viewerPathLabel = builder.append("", viewerPathFileChooserPanel, 3);
		builder.nextLine();
		
		docViewerPathFileChooserPanel = new ExecutableChooserPanel(Constants.DEFAULT_COLUMNS);
		docViewerPathFileChooserPanel.setDialogParent(this);
		docViewerPathLabel = builder.append("", docViewerPathFileChooserPanel, 3);
		builder.nextLine();
		
		builder.appendSeparator(i18n.tr("Address Book"));
		
		customizeAddressBookCheckBox = new JCheckBox();
		settingMediator.add(Settings.CUSTOMIZE_ADDRESS_BOOK_FILENAME, customizeAddressBookCheckBox);
		builder.append(customizeAddressBookCheckBox, 5);
		builder.nextLine();		
		
		addressBookPathFileChooserPanel = new FileChooserPanel(Constants.DEFAULT_COLUMNS);
		settingMediator.add(Settings.ADDRESS_BOOK_FILENAME, addressBookPathFileChooserPanel.getTextField());
		addressBookPathFileChooserPanel.setDialogParent(this);
		builder.append(addressBookPathFileChooserPanel, 5);
		builder.nextLine();
		
		customizeAddressBookCheckBox.addItemListener(new EnableListener(addressBookPathFileChooserPanel));
		
		builder.appendSeparator(i18n.tr("Cover"));
		
		internalCoverRadionButton = new JRadioButton();
		internalCoverRadionButton.setSelected(true);
		builder.append(internalCoverRadionButton, 5);
		builder.nextLine();
		
		externalCoverRadionButton = new JRadioButton();
		externalCoverPathFileChooserPanel = new FileChooserPanel(Constants.DEFAULT_COLUMNS);
		externalCoverPathFileChooserPanel.setEnabled(false);
		externalCoverPathFileChooserPanel.setDialogParent(this);
		builder.append(externalCoverRadionButton, 5);
		builder.nextLine();
		
		externalCoverRadionButton.addItemListener(new EnableListener(externalCoverPathFileChooserPanel));
		
		builder.append(externalCoverPathFileChooserPanel, 5);
		builder.nextLine();
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(internalCoverRadionButton);
		buttonGroup.add(externalCoverRadionButton);
		
		sendCoverAsDocumentCheckBox = new JCheckBox();
		settingMediator.add(Settings.SEND_COVER_AS_DOCUMENT, sendCoverAsDocumentCheckBox);
		builder.append(sendCoverAsDocumentCheckBox, 5);
		builder.nextLine();
	}
	

	public void initializeMonitorForm() {
		DefaultFormBuilder builder = createForm();
		monitorPanel = builder.getPanel();
		mainTabbedPane.add(monitorPanel);

		monitorPathCheckBox = new JCheckBox();
		settingMediator.add(Settings.DO_MONITOR_PATH, monitorPathCheckBox);
		builder.append(monitorPathCheckBox, 4);
		builder.nextLine();

		monitorPathFileChooserPanel = new ExecutableChooserPanel(Constants.DEFAULT_COLUMNS);
		monitorPathFileChooserPanel.setDialogParent(this);
		settingMediator.add(Settings.MONITOR_PATH, monitorPathFileChooserPanel.getTextField());
		monitorPathLabel = builder.append("", monitorPathFileChooserPanel, 3);
		builder.nextLine();

		monitorPathIntervalModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 30);
		monitorPathIntervalSpinner = new JSpinner(monitorPathIntervalModel);
		monitorPathIntervalSpinner.setEditor(new JSpinner.NumberEditor(monitorPathIntervalSpinner, "# s"));
		monitorPathIntervalLabel = builder.append("", monitorPathIntervalSpinner);
		builder.nextLine();		
		
		monitorPathCheckBox.addItemListener(new EnableListener(monitorPathFileChooserPanel, monitorPathIntervalSpinner));
	}

	public void defaults() {
		settingMediator.revertToDefaults();
		
		languageModel.setSelectedItem(Settings.LOCALE.getDefaultValue());
		completionModeModel.setSelectedItem(CompletionModeFactory.getCompletionModeInfoByClassName(Settings.DEFAULT_COMPLETION_MODE.getDefaultValue()));
		
		jobParameterPanel.revertFromDefaultSettings();		
		
		viewerPathFileChooserPanel.getTextField().setText(Settings.VIEWER_PATH.getDefaultValue());
		docViewerPathFileChooserPanel.getTextField().setText(Settings.DOC_VIEWER_PATH.getDefaultValue());
		internalCoverRadionButton.setSelected(Settings.USE_INTERNAL_COVER.getDefaultValue());
		externalCoverPathFileChooserPanel.getTextField().setText(Settings.COVER_PATH.getDefaultValue());
		monitorPathIntervalModel.setValue(Settings.MONITOR_PATH_INTERVAL.getDefaultValue());
	}
	
	public void revert() {
		settingMediator.revert();
		
		languageModel.setSelectedItem(Settings.LOCALE.getValue());
		completionModeModel.setSelectedItem(CompletionModeFactory.getCompletionModeInfoByClassName(Settings.DEFAULT_COMPLETION_MODE.getValue()));
		
		jobParameterPanel.revertFromSettings();
		
		viewerPathFileChooserPanel.getTextField().setText(Settings.VIEWER_PATH.getValue());
		docViewerPathFileChooserPanel.getTextField().setText(Settings.DOC_VIEWER_PATH.getValue());
		internalCoverRadionButton.setSelected(Settings.USE_INTERNAL_COVER.getValue());
		externalCoverPathFileChooserPanel.getTextField().setText(Settings.COVER_PATH.getValue());
		monitorPathIntervalModel.setValue(Settings.MONITOR_PATH_INTERVAL.getValue());
	}
	
	public void updateLabels() {
		setTitle(i18n.tr("Settings"));
		
		mainTabbedPane.setTitleAt(mainTabbedPane.indexOfComponent(connectionPanel), i18n.tr("Connection"));	
		hostnameLabel.setText(i18n.tr("Host"));
		usePassiveCheckBox.setText(i18n.tr("Passive Transfers (Select If Transfers Timeout)"));
		portLabel.setText(i18n.tr("Port"));
		usernameLabel.setText(i18n.tr("Username"));
		WhatsThis.setText(usernameTextField, i18n.tr("The username that is sent to the server."));
		passwordLabel.setText(i18n.tr("Password"));
		passwordInfoLabel.setText("Warning: Passwords are saved in plain text! If not entered a prompt will be displayed.");
		adminModeCheckBox.setText(i18n.tr("Admin Privileges"));
		updateOnStartupCheckBox.setText(i18n.tr("Update Status on Startup"));
		autoUpdateCheckBox.setText(i18n.tr("Automatically Update Status"));
		autoUpdateIntervalLabel.setText(i18n.tr("Update Interval"));
		
		mainTabbedPane.setTitleAt(mainTabbedPane.indexOfComponent(generalPanel), i18n.tr("General"));
		Object selectedItem = languageModel.getSelectedItem();
		languageModel.removeAllElements();
		for (Locale locale : JHylaFAX.SUPPORTED_LOCALES) {
			languageModel.addElement(locale);
		}
		languageModel.setSelectedItem(selectedItem);
		showPollqCheckBox.setText(i18n.tr("Show Pollable Faxes (Takes Effect after Restart)"));
		selectedItem = completionModeModel.getSelectedItem();
		completionModeModel.removeAllElements();
		for (CompletionModeInfo mode : CompletionModeFactory.getInstalledCompletionModes()) {
			completionModeModel.addElement(mode);
		}
		completionModeModel.setSelectedItem(selectedItem);
		languageLabel.setText(i18n.tr("Lanuage"));
		completionModeLabel.setText(i18n.tr("Default Text Completion"));
		confirmNonPSCheckBox.setText(i18n.tr("Confirm Sending Non-Postscript Documents"));
		confirmDeleteCheckBox.setText(i18n.tr("Confirm Delete"));

		mainTabbedPane.setTitleAt(mainTabbedPane.indexOfComponent(jobPanel), i18n.tr("Fax"));
		jobParameterPanel.updateLabels();
		
		mainTabbedPane.setTitleAt(mainTabbedPane.indexOfComponent(pathsPanel), i18n.tr("Paths"));
		viewerPathLabel.setText(i18n.tr("Fax Viewer"));
		docViewerPathLabel.setText(i18n.tr("Document Viewer"));
		customizeAddressBookCheckBox.setText(i18n.tr("Use Custom Address Book (Takes Effect after Restart)"));
		internalCoverRadionButton.setText(i18n.tr("Use Internal Cover"));
		externalCoverRadionButton.setText(i18n.tr("Use Custom PostScript File"));
		sendCoverAsDocumentCheckBox.setText(i18n.tr("Save Cover in Document Queue After Sending"));

		mainTabbedPane.setTitleAt(mainTabbedPane.indexOfComponent(monitorPanel), i18n.tr("Monitor"));
		monitorPathCheckBox.setText(i18n.tr("Monitor File"));
		monitorPathLabel.setText(i18n.tr("File"));
		monitorPathIntervalLabel.setText(i18n.tr("Monitor Interval"));
	}

}
