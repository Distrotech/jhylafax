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
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import org.xnap.commons.gui.Builder;
import org.xnap.commons.gui.action.AbstractXNapAction;
import org.xnap.commons.gui.util.GUIHelper;
import org.xnap.commons.gui.util.IconHelper;
import org.xnap.commons.gui.wizard.WizardDialog;
import org.xnap.commons.gui.wizard.WizardPage;
import org.xnap.commons.util.PortRange;
import org.xnap.commons.util.SystemHelper;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A dialog for editing of global settings.
 * 
 * @author Steffen Pingel
 */
public class SettingsWizard extends WizardDialog implements LocaleChangeListener {

	private static final int WIDTH = 500;
	
	private GeneralPage generalPage;
	private NotifyPage notifyPage;
	private ProgramsPage programsPage;
	private IntroductionPage introPage;
	
	public SettingsWizard(JFrame owner) {
		super(owner);
		
		introPage = new IntroductionPage();
		addPage(introPage, "intro");
		
		generalPage = new GeneralPage();
		addPage(generalPage, "general");
		
		notifyPage = new NotifyPage();
		addPage(notifyPage, "notify");
		
		programsPage = new ProgramsPage();
		addPage(programsPage, "programs");
		
		updateLabels();
		revert();
		
		pack();
	}

	private DefaultFormBuilder createForm() {
		FormLayout layout = new FormLayout("left:max(20dlu;pref), 3dlu, pref, pref:grow(0.5), pref:grow(0.5)", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		return builder;
	}

	public void revert() {
		generalPage.revert();
		notifyPage.revert();
		programsPage.revert();
	}

	public void updateLabels()
	{
		setTitle(i18n.tr("JHylaFAX Setup Wizard"));
		
		introPage.updateLabels();
		generalPage.updateLabels();
		notifyPage.updateLabels();
		programsPage.updateLabels();
	}

	private class IntroductionPage implements WizardPage {

		private DefaultFormBuilder builder;
		private JLabel introLabel;

		public IntroductionPage() {
			builder = createForm();
			
			introLabel = builder.append("");
			builder.nextLine();		
		}			

		public boolean apply() {
			if (!Settings.HAS_SEEN_WIZARD.getValue()) {
				JHylaFAX.getInstance().resetAllTables();
				Settings.HAS_SEEN_WIZARD.setValue(true);
			}
			return true;
		}

		public String getDescription() {
			return i18n.tr("Easily setup JHylaFAX in 3 steps.");
		}

		public Icon getIcon() {
			return null;
		}

		public JComponent getPanel() {
			return builder.getPanel();
		}

		public String getTitle() {
			return i18n.tr("JHylaFAX Setup Wizard");
		}
	
		public void updateLabels() {
			introLabel.setText(GUIHelper.tt(i18n.tr(
					"JHylaFAX is a Java client for the HylaFAX fax server. " +
					"It is licensed under the " +
					"<b>General Public License (GPL)</b>, see about for details." +
					"<p>" + 
					"<p>Setup requires three simple steps:" + 
					"<ol>" +
					"<li>Server connection and authentication" +
					"<li>Sender and notifcation" +
					"<li>External viewer programs" +
					"</ol>" +
					"<p>Thank you for using JHylaFAX. "), WIDTH));
		}
		
	}
	
	private class GeneralPage implements WizardPage {
		
		private DefaultFormBuilder builder;
		private JLabel hostnameLabel;
		private JTextField hostnameTextField;
		private JLabel passwordLabel;
		private JPasswordField passwordTextField;
		private JLabel portLabel;
		private JSpinner portSpinner;
		private SpinnerNumberModel portSpinnerModel;
		private JLabel usernameLabel;
		private JTextField usernameTextField;
		private JLabel hostnameDescriptionLabel;
		private JLabel usernameDescriptionLabel;

		public GeneralPage() {
			builder = createForm();
			
			hostnameDescriptionLabel = new JLabel();
			builder.append(hostnameDescriptionLabel, 5);
			builder.nextLine();
			
			hostnameTextField = new JTextField(Constants.DEFAULT_COLUMNS);
			hostnameLabel = builder.append("", hostnameTextField, 3);
			builder.nextLine();		
			
			portSpinnerModel = new SpinnerNumberModel();
			portSpinnerModel.setMinimum(PortRange.MIN_PORT);
			portSpinnerModel.setMaximum(PortRange.MAX_PORT);
			portSpinner = new JSpinner(portSpinnerModel);
			portSpinner.setEditor(new JSpinner.NumberEditor(portSpinner, "#"));
			portLabel = builder.append("", portSpinner);
			builder.nextLine();
			
			usernameDescriptionLabel = new JLabel();
			builder.append(usernameDescriptionLabel, 5);
			builder.nextLine();
			
			usernameTextField = new JTextField(Constants.DEFAULT_COLUMNS);
			usernameLabel = builder.append("", usernameTextField, 3);
			builder.nextLine();
		}

		public boolean apply() {
			Settings.HOSTNAME.setValue(hostnameTextField.getText());
			Settings.PORT.setValue(portSpinnerModel.getNumber().intValue());
			Settings.USERNAME.setValue(usernameTextField.getText());
			return true;
		}

		public String getDescription() {
			return i18n.tr("The connection to the fax server is configured here.");
		}

		public Icon getIcon() {
			return IconHelper.getTitleIcon("connect_established.png");
		}

		public JComponent getPanel() {
			return builder.getPanel();
		}

		public String getTitle() {
			return i18n.tr("HylaFAX Server Connection");
		}

		public void revert() {
			hostnameTextField.setText(Settings.HOSTNAME.getValue());
			portSpinnerModel.setValue(Settings.PORT.getValue());
			usernameTextField.setText(Settings.USERNAME.getValue());
		}
		
		public void updateLabels() {
			hostnameDescriptionLabel.setText(GUIHelper.tt(i18n.tr("Enter the host and port of your HylaFAX server:"), WIDTH));
			hostnameLabel.setText(i18n.tr("Host"));
			portLabel.setText(i18n.tr("Port"));
			
			usernameDescriptionLabel.setText(GUIHelper.tt(i18n.tr("Enter a username that is used to login to the server:"), WIDTH));
			usernameLabel.setText(i18n.tr("Username"));
		}
	}

	private class NotifyPage implements WizardPage {

		private JTextField fullnameTextField;
		private DefaultFormBuilder builder;
		private JTextField emailTextField;
		private JLabel emailLabel;
		private JLabel fullnameLabel;
		private JLabel fullnameDescriptionLabel;
		private JLabel emailDescriptionLabel;

		public NotifyPage() {
			builder = createForm();
			
			fullnameDescriptionLabel = new JLabel();
			builder.append(fullnameDescriptionLabel, 5);
			builder.nextLine();

			fullnameTextField = new JTextField(Constants.DEFAULT_COLUMNS);
			fullnameLabel = builder.append("", fullnameTextField, 3);
			builder.nextLine();

			emailDescriptionLabel = new JLabel();
			builder.append(emailDescriptionLabel, 5);
			builder.nextLine();
			
			emailTextField = new JTextField(Constants.DEFAULT_COLUMNS);
			emailLabel = builder.append("", emailTextField, 3);
			builder.nextLine();
			
		}
		
		public void updateLabels() {
			fullnameDescriptionLabel.setText(GUIHelper.tt(i18n.tr("Enter a name that is used to identify the sender:"), WIDTH));
			fullnameLabel.setText(i18n.tr("Name"));
			emailDescriptionLabel.setText(GUIHelper.tt(i18n.tr("Enter an email address to receive notification when a fax has been sent or cancelled:"), WIDTH));
			emailLabel.setText(i18n.tr("Email"));
		}

		public boolean apply() {
			Settings.FULLNAME.setValue(fullnameTextField.getText());
			Settings.EMAIL.setValue(emailTextField.getText());
			return true;
		}

		public String getDescription() {
			return i18n.tr("The sender's name and an email address for notifications are configured here.");
		}

		public Icon getIcon() {
			return IconHelper.getTitleIcon("kontact_mail.png");
		}

		public JComponent getPanel() {
			return builder.getPanel();
		}

		public String getTitle() {
			return i18n.tr("Sender and Notification");
		}

		public void revert() {
			fullnameTextField.setText(Settings.FULLNAME.getValue());
			emailTextField.setText(Settings.EMAIL.getValue());
		}
		
	}			

	private class ProgramsPage implements WizardPage {

		private DefaultFormBuilder builder;
		private ExecutableChooserPanel viewerPathFileChooserPanel;
		private JLabel viewerPathLabel;
		private ExecutableChooserPanel docViewerPathFileChooserPanel;
		private JLabel docViewerPathLabel;
		private String docViewer;
		private String viewer;
		private SearchProgramsAction searchProgramsAction;
		private JLabel viewerDescriptionLabel;
		private JLabel searchDescriptionLabel;
		private JLabel docViewerDescriptionLabel;
		private JLabel descriptionLabel;

		public ProgramsPage() {
			builder = createForm();

			descriptionLabel = new JLabel();
			builder.append(descriptionLabel, 5);
			builder.nextLine();

			viewerDescriptionLabel = new JLabel();
			builder.append(viewerDescriptionLabel, 5);
			builder.nextLine();
			
			viewerPathFileChooserPanel = new ExecutableChooserPanel(Constants.DEFAULT_COLUMNS);
			viewerPathFileChooserPanel.setDialogParent(SettingsWizard.this);
			viewerPathLabel = builder.append("", viewerPathFileChooserPanel, 3);
			builder.nextLine();
			
			docViewerDescriptionLabel = new JLabel();
			builder.append(docViewerDescriptionLabel, 5);
			builder.nextLine();
			
			docViewerPathFileChooserPanel = new ExecutableChooserPanel(Constants.DEFAULT_COLUMNS);
			docViewerPathFileChooserPanel.setDialogParent(SettingsWizard.this);
			docViewerPathLabel = builder.append("", docViewerPathFileChooserPanel, 3);
			builder.nextLine();

			searchDescriptionLabel = new JLabel();
			builder.append(searchDescriptionLabel, 5);
			builder.nextLine();
			
			builder.append("");
			builder.nextLine();
			
			searchProgramsAction = new SearchProgramsAction();
			builder.append(Builder.createButton(searchProgramsAction));
			builder.nextLine();
		}
		
		public void revert() {
			searchForPrograms();
			
			if (!"".equals(Settings.VIEWER_PATH.getValue())) {
				viewerPathFileChooserPanel.getTextField().setText(Settings.VIEWER_PATH.getValue());
			}
			else {
				viewerPathFileChooserPanel.getTextField().setText(viewer);
			}
			
			if (!"".equals(Settings.DOC_VIEWER_PATH.getValue())) {
				docViewerPathFileChooserPanel.getTextField().setText(Settings.DOC_VIEWER_PATH.getValue());
			}
			else {
				docViewerPathFileChooserPanel.getTextField().setText(docViewer);
			}
		}

		private void searchForPrograms() {
			if (SystemHelper.IS_WINDOWS) {
	            if (SystemHelper.IS_WINDOWS_XP) { 
	            	viewer= "rundll32.exe shimgvw.dll,ImageView_Fullscreen $f";
	            }
	            else {
	            	String path = System.getenv("ProgramFiles");
	            	viewer = searchExecutable(new String[][] {
		        			{ path + "\\Windows NT\\Accessories\\ImageVue", "kodakimg.exe", "$f" },
		        	});		
	            }
	            docViewer = searchExecutable(new String[][] {
	        			{ null, "gsview32.exe", "$f" },
	            });
	            
			}
			else if (SystemHelper.IS_MACOSX) {
				viewer = "open -a Preview.app $f";
				docViewer = "open -a Preview.app $f";
			}
			else {
	        	viewer = searchExecutable(new String[][] {
	        			{ "/usr/bin", "kfax", "$f" },
	        	});
	        	docViewer = searchExecutable(new String[][] {
	        			{ "/usr/bin", "gv", "$f" },
	        			{ "/usr/bin", "kghostview", "$f" },
	        	});
			}			
		}
		
		private String searchExecutable(String[][] programs) {
			return programs[0][1] + " " + programs[0][2];
			/*
			for (int programIndex = 0; programIndex < programs.length; programIndex++) {
				String[] program = programs[programIndex];
				if (program[0] != null && new File(program[0], program[1]).exists()) {
					return program[0] + File.pathSeparator + program[1] + " " + program[2];
				}
			}
			*/
		}

		public void updateLabels() {
			descriptionLabel.setText(GUIHelper.tt(i18n.tr("JHylaFAX depends on external programs to display received and sent faxes. " +
					"You can either enter the path of a program or use Search to let JHylaFAX suggest a program." +
					"You may use $f as a placeholder for the filename that is passed as a parameter."), WIDTH));
			
			viewerDescriptionLabel.setText(GUIHelper.tt(i18n.tr("Enter the path of a programm that can handle TIFF G3 files:"), WIDTH));
			viewerPathLabel.setText(i18n.tr("Fax Viewer"));
			docViewerDescriptionLabel.setText(GUIHelper.tt(i18n.tr("Enter the path of a programm that can handle PostScript files:"), WIDTH));
			docViewerPathLabel.setText(i18n.tr("Document Viewer"));
			searchProgramsAction.putValue(Action.NAME, i18n.tr("Search"));
		}

		public boolean apply()
		{
			Settings.VIEWER_PATH.setValue(viewerPathFileChooserPanel.getTextField().getText());
			Settings.DOC_VIEWER_PATH.setValue(docViewerPathFileChooserPanel.getTextField().getText());

			return true;
		}

		public String getDescription() {
			return i18n.tr("Viewer programs are configured here.");
		}

		public Icon getIcon() {
			return IconHelper.getTitleIcon("misc.png");
		}

		public JComponent getPanel() {
			return builder.getPanel();
		}

		public String getTitle() {
			return i18n.tr("External Programs");
		}

		private class SearchProgramsAction extends AbstractXNapAction { 

			public SearchProgramsAction() {
				putValue(ICON_FILENAME, "find.png");
			}
						
			public void actionPerformed(ActionEvent e)
			{
				searchForPrograms();
				viewerPathFileChooserPanel.getTextField().setText(viewer);
				docViewerPathFileChooserPanel.getTextField().setText(docViewer);
			}
			
		}

	}		
		

}
