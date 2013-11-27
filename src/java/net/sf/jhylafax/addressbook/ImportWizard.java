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
package net.sf.jhylafax.addressbook;

import static net.sf.jhylafax.JHylaFAX.i18n;

import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import net.sf.jhylafax.Constants;
import net.sf.jhylafax.LocaleChangeListener;
import net.wimpi.pim.contact.facades.SimpleContact;

import org.xnap.commons.gui.Dialogs;
import org.xnap.commons.gui.ErrorDialog;
import org.xnap.commons.gui.FileChooserPanel;
import org.xnap.commons.gui.util.EnableListener;
import org.xnap.commons.gui.util.IconHelper;
import org.xnap.commons.gui.wizard.WizardDialog;
import org.xnap.commons.gui.wizard.WizardDialogListener;
import org.xnap.commons.gui.wizard.WizardPage;
import org.xnap.commons.io.FileExtensionFilter;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A wizard for importing address book entries. 
 * 
 * @author Steffen Pingel
 */
public class ImportWizard extends WizardDialog implements LocaleChangeListener {

	private FilePage filePage;
	private FormatPage formatPage;
	private boolean firstTime = true;
	private AddressBook addressbook;
	
	public ImportWizard(AddressBook addressbook) {
		super(addressbook);

		this.addressbook = addressbook;
		
		filePage = new FilePage();
		addPage(filePage, "file");

		formatPage = new FormatPage();
		addPage(formatPage, "format");

		addWizardDialogListener(new WizardDialogListener() {

			@SuppressWarnings("deprecation")
            public void pageChanged(WizardPage oldPage, WizardPage newPage)
			{
				if (firstTime) {
					if (oldPage == filePage && newPage == formatPage) {
						formatPage.guessFormat(filePage.fileChooserPanel.getFilename());
						firstTime = false;
					}
				}
			}
			
		});
		
		updateLabels();
		revert();
		
		pack();
	}

	private DefaultFormBuilder createForm() {
		FormLayout layout = new FormLayout("left:max(20dlu;pref), 3dlu, pref, 3dlu, left:max(20dlu;pref), pref, pref:grow", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		return builder;
	}

	@Override
	public void finish()
	{
		File file = filePage.fileChooserPanel.getFile();
		if (file == null) {
			Dialogs.showError(this,	i18n.tr("No file selected"), i18n.tr("Import Error"));
			return;
		}
		
		try {
			SimpleContact[] contacts;
			if (formatPage.vCardFormatRadioButton.isSelected()) {
				contacts = addressbook.importVCardContacts(file);
			}
			else if (formatPage.suseFaxFormatRadioButton.isSelected()) {
				contacts = addressbook.importCSVContacts(file, "|");
			}
			else {
				String separator = formatPage.separatorTextField.getText();
				if (file == null) {
					Dialogs.showError(this,	i18n.tr("No valid separator specified"), i18n.tr("Import Error"));
					return;
				}
				contacts = addressbook.importCSVContacts(file, separator);
			}
			Dialogs.showInfo(this, 
					i18n.tr("Imported {0} contacts", contacts.length), 
					i18n.tr("JHylaFAX Address Book"));

		}
		catch (Exception e) {
			ErrorDialog.showError(this, 
					i18n.tr("Could not import from file \"{0}\"", file.getAbsolutePath()), 
					i18n.tr("JHylaFAX Addressbook Error"), e);
			return;
		}

		super.finish();
	}
	
	public void revert()
	{
		formatPage.revert();
		filePage.revert();
	}
	
	public void updateLabels()
	{
		setTitle(i18n.tr("JHylaFAX Address Book Import Wizard"));
		
		formatPage.updateLabels();
		filePage.updateLabels();
	}

	private class FilePage implements WizardPage {
		
		private DefaultFormBuilder builder;
		FileChooserPanel fileChooserPanel;
		private JLabel fileChooserPanelLabel;

		public FilePage() {
			builder = createForm();

			fileChooserPanel = new FileChooserPanel(Constants.DEFAULT_COLUMNS);
			fileChooserPanel.getFileChooser().addChoosableFileFilter(
					new FileExtensionFilter(i18n.tr("vCards (*.vcf)"), ".vcf"));
			fileChooserPanel.getFileChooser().addChoosableFileFilter(
					new FileExtensionFilter(i18n.tr("CSV (*.csv)"), ".csv"));
			fileChooserPanel.getFileChooser().addChoosableFileFilter(
					new FileExtensionFilter(i18n.tr("SuSEFax (.susephone)"), ".susephone"));
			fileChooserPanel.getFileChooser().setFileHidingEnabled(false);
			fileChooserPanelLabel = builder.append("", fileChooserPanel, 5);
		}

		public boolean apply() {
			return true;
		}

		public String getDescription() {
			return i18n.tr("Select file to import.");
		}

		public Icon getIcon() {
			return IconHelper.getTitleIcon("file.png");
		}

		public JComponent getPanel() {
			return builder.getPanel();
		}

		public String getTitle() {
			return i18n.tr("Import File");
		}

		public void revert() {
		}
		
		public void updateLabels() {
			fileChooserPanelLabel.setText(i18n.tr("File:"));
		}
	}
	
	private class FormatPage implements WizardPage {

		private DefaultFormBuilder builder;
		JRadioButton csvFormatRadioButton;
		JRadioButton vCardFormatRadioButton;
		JRadioButton suseFaxFormatRadioButton;
		private JTextField separatorTextField;
		private JLabel separatorLabel;
		private JLabel separatorDescriptionLabel;

		public FormatPage() {
			builder = createForm();
			
			vCardFormatRadioButton = new JRadioButton();
			builder.append(vCardFormatRadioButton);
			builder.nextLine();
			
			csvFormatRadioButton = new JRadioButton();
			builder.append(csvFormatRadioButton);
			separatorTextField = new JTextField(1);
			separatorLabel = builder.append("", separatorTextField);
			builder.nextLine();

			separatorDescriptionLabel = new JLabel(); 
			builder.append(separatorDescriptionLabel, 7);
			builder.nextLine();
			
			csvFormatRadioButton.addItemListener(new EnableListener(
					separatorLabel, separatorTextField, separatorDescriptionLabel));
			
			suseFaxFormatRadioButton = new JRadioButton();
			builder.append(suseFaxFormatRadioButton);
			builder.nextLine();
			
			ButtonGroup bg = new ButtonGroup();
			bg.add(vCardFormatRadioButton);
			bg.add(csvFormatRadioButton);
			bg.add(suseFaxFormatRadioButton);
		}			

		public boolean apply() {
			
			return true;
		}

		public String getDescription() {
			return i18n.tr("Select import format.");
		}

		public Icon getIcon() {
			return null;
		}

		public void guessFormat(String filename) {
			if (filename.endsWith("vcf")) {
				vCardFormatRadioButton.setSelected(true);
			}
			else if (filename.endsWith("susephone")) {
				suseFaxFormatRadioButton.setSelected(true);
			}
			else {
				csvFormatRadioButton.setSelected(true);
			}
		}
		
		public JComponent getPanel() {
			return builder.getPanel();
		}

		public String getTitle() {
			return i18n.tr("Import Format");
		}

		public void revert()
		{
			csvFormatRadioButton.setSelected(true);
			separatorTextField.setText(",");
		}
	
		public void updateLabels() {
			vCardFormatRadioButton.setText(i18n.tr("vCard"));
			csvFormatRadioButton.setText(i18n.tr("CSV"));
			separatorLabel.setText(i18n.tr("Separator:"));
			separatorDescriptionLabel.setText(i18n.tr("Expected Format: First Name, Last Name, Fax, Company"));
			suseFaxFormatRadioButton.setText(i18n.tr("SuSEFax"));
		}
		
	}

}
