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
import gnu.hylafax.HylaFAXClient;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import net.sf.jhylafax.fax.FaxCover;
import net.sf.jhylafax.fax.FaxJob;
import net.sf.jhylafax.fax.HylaFAXClientHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xnap.commons.gui.Builder;
import org.xnap.commons.gui.Dialogs;
import org.xnap.commons.gui.ErrorDialog;
import org.xnap.commons.gui.FileChooserPanel;
import org.xnap.commons.gui.action.AbstractXNapAction;
import org.xnap.commons.io.Job;
import org.xnap.commons.io.ProgressMonitor;
import org.xnap.commons.io.SubTaskProgressMonitor;
import org.xnap.commons.io.UserAbortException;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A dialog for sending faxes.
 * 
 * @author Steffen Pingel
 */
public class SendDialog extends AbstractFaxDialog {

	private final static Log logger = LogFactory.getLog(AbstractQueuePanel.class);

	private JLabel documentLabel;
	private JPanel coverPanel;
	private JTextField coverSenderTextField;
	private JLabel coverSenderLabel;
	private JTextField coverRecepientTextField;
	private JLabel coverRecepientLabel;
	private JTextField coverSubjectTextField;
	private JLabel coverSubjectLabel;
	private JTextArea coverCommentTextArea;
	private JLabel coverCommentLabel;
	private JPanel documentPanel;
	private DefaultFormBuilder documentPanelBuilder;
	private List<FileChooserPanel> documentFileChooserPanels;
	private JCheckBox includeCoverCheckBox;
	private JLabel includeCoverLabel;
	private JScrollPane coverCommentScrollPane;
	private PreviewCoverAction previewCoverAction;

	private MoreDocumentsAction moreDocumentsAction;

	private boolean quitAfterSending;
	
	public SendDialog(JFrame owner) {
		super(owner);
		
		moreDocumentsAction = new MoreDocumentsAction();
		previewCoverAction = new PreviewCoverAction();
		
		addNumberTextField();
		
		addDocumentsPanel();

		builder.append("", Builder.createButton(moreDocumentsAction));
		builder.nextLine();

		addDateControls();

		includeCoverCheckBox = new JCheckBox();
		includeCoverLabel = builder.append("", includeCoverCheckBox);
		builder.nextLine();
		
		initializeCoverPanel();
		coverPanel.setVisible(false);
		previewCoverAction.setEnabled(false);
		includeCoverCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				coverPanel.setVisible(includeCoverCheckBox.isSelected());
				previewCoverAction.setEnabled(includeCoverCheckBox.isSelected());
				pack();
			}
		});
		
		getButtonPanel().add(Builder.createButton(previewCoverAction), 0);
		
		FaxJob job = new FaxJob(); 
		HylaFAXClientHelper.initializeFromSettings(job);
		setJob(job);
		
		updateLabels();
		pack();
	}
	
	private void addDocumentsPanel()
	{
		documentFileChooserPanels = new ArrayList<FileChooserPanel>();
		
		FormLayout layout = new FormLayout("left:max(40dlu;pref), 3dlu, pref:grow", "");
		documentPanelBuilder = new DefaultFormBuilder(layout);
		FileChooserPanel documentFileChooserPanel = new MyFileChooserPanel(DEFAULT_COLUMNS);
		documentFileChooserPanel.getFileChooser().setMultiSelectionEnabled(true);
		documentFileChooserPanels.add(documentFileChooserPanel);
		documentLabel = documentPanelBuilder.append("", documentFileChooserPanel);

		this.builder.appendRow(builder.getLineGapSpec());
		this.builder.nextLine();
		this.builder.appendRow("fill:pref:grow");
		this.builder.append(documentPanelBuilder.getPanel(), 6);

		builder.nextLine();
	}

	private void initializeCoverPanel()
	{
		FormLayout layout = new FormLayout("left:max(40dlu;pref), 3dlu, pref:grow", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		coverPanel = builder.getPanel();
		this.builder.appendRow(builder.getLineGapSpec());
		this.builder.nextLine();
		this.builder.appendRow("fill:pref:grow");
		this.builder.append(coverPanel, 6);
		
		coverSenderTextField = new JTextField(DEFAULT_COLUMNS);
		coverSenderTextField.setText(Settings.FULLNAME.getValue());
		coverSenderLabel = builder.append("", coverSenderTextField);
		builder.nextLine();
		
		coverRecepientTextField = new JTextField(DEFAULT_COLUMNS);
		coverRecepientLabel = builder.append("", coverRecepientTextField);
		builder.nextLine();
		
		coverSubjectTextField = new JTextField(DEFAULT_COLUMNS);
		coverSubjectLabel = builder.append("", coverSubjectTextField);
		builder.nextLine();
		
		coverCommentTextArea= new JTextArea(3, DEFAULT_COLUMNS);
		coverCommentLabel = builder.append("");
		builder.appendRow("fill:pref:grow"); // second row for text area
		CellConstraints cc = new CellConstraints();
		coverCommentScrollPane = new JScrollPane(coverCommentTextArea);
		builder.add(coverCommentScrollPane,
				cc.xywh(builder.getColumn(), builder.getRow(), 1, 2));
		builder.nextLine(2);
	}

	@Override
	public boolean apply() {
		if (!super.apply()) {
			return false;
		}

		if (!includeCoverCheckBox.isSelected()) {
			boolean filenameProvided = false;
			for (FileChooserPanel documentFileChooserPanel : documentFileChooserPanels) {
				if (documentFileChooserPanel.getTextField().getText().trim().length() != 0) {
					filenameProvided = true;
					if (Settings.CONFIRM_NONPS.getValue()) {
						if (!checkPostScript(documentFileChooserPanel.getTextField().getText())) {
							documentFileChooserPanel.getTextField().requestFocus();
							return false;
						}
					}
				}
			}
			if (!filenameProvided) {
				Dialogs.showError(this, i18n.tr("You must at least send a document or a cover"), 
						i18n.tr("JHylaFAX Error"));
				documentFileChooserPanels.get(0).getTextField().requestFocus();
				return false;
			}
		}
	
		Fax fax = createFax();
		if (fax != null) {
			if (send(fax)) {
				if (quitAfterSending) {
					JHylaFAX.getInstance().exit();
				}
				return true;
			}
		}
		
		return false;
	}		
	
	private boolean checkPostScript(String filename) {
		try {
			if (!HylaFAXClientHelper.isPostscript(filename)) {
				if (Dialogs.showConfirmDialog(this, 
						i18n.tr("Do you really want to send the non PostScript file \"{0}\"?", filename),
						i18n.tr("JHylaFAX - Send non PostScript file"), 
						JOptionPane.YES_NO_OPTION, 
						Settings.CONFIRM_NONPS) == JOptionPane.NO_OPTION) {
					return false;
				}
			}
		}
		catch (IOException e) {
			logger.debug("Error checking for PostScript", e);
			ErrorDialog.showError(this, i18n.tr("Could not check for PostScript"), 
					i18n.tr("JHylaFAX Error"),
					e);
			return false;
		}
		return true;
	}

	private Fax createFax() {
		Fax fax = new Fax();
		
		for (FileChooserPanel documentFileChooserPanel : documentFileChooserPanels) {
			String filename = documentFileChooserPanel.getTextField().getText();
			if (filename.trim().length() > 0) {
				File file = new File(filename);
				fax.documents.add(file);
			}
		}
		
		if (includeCoverCheckBox.isSelected() && !createFaxCover(fax)) {
			return null;
		}
		
		return fax;
	}
	
	private boolean createFaxCover(Fax fax) {
		if (fax == null) {
			throw new IllegalArgumentException("value may not be null");
		}
		
		String filename;
		if (Settings.USE_INTERNAL_COVER.getValue()) {
			filename = "faxcover.ps";
			fax.coverIn = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename); 
		} else {
			filename = Settings.COVER_PATH.getValue();
			try {
				fax.coverIn = new FileInputStream(filename);
			}
			catch (FileNotFoundException e) {
				fax.coverIn = null;
			}
		}
		if (fax.coverIn == null) {
			Dialogs.showError(this, i18n.tr("Could not read cover file: {0}", filename), 
					i18n.tr("JHylaFAX Error"));
			return false;	
		}
			
		fax.cover = new FaxCover(Settings.PAPER.getValue());
		fax.cover.from = coverSenderTextField.getText();
		fax.cover.to = coverRecepientTextField.getText();
		fax.cover.regarding = coverSubjectTextField.getText();
		fax.cover.comments = coverCommentTextArea.getText();
		fax.cover.todays_date = SimpleDateFormat.getDateTimeInstance().format(new Date());
		for (Iterator<File> it = fax.documents.iterator(); it.hasNext();) {
			fax.cover.addDocument(it.next().getAbsolutePath());
		}
		
		return true;
	}
	
	private boolean send(final Fax fax) {	
		Job<?> ioJob = new Job() {
			public Object run(ProgressMonitor monitor) throws Exception
			{
				monitor.setTotalSteps(13 + fax.documents.size() * 10);
				
				HylaFAXClient client = JHylaFAX.getInstance().getConnection(monitor);
				monitor.work(1);

				client.mode(HylaFAXClient.MODE_STREAM);
				client.type(HylaFAXClient.TYPE_IMAGE);

				String serverCoverFilename = null;
				if (fax.cover != null && fax.coverIn != null) {
					monitor.setText(i18n.tr("Generating cover"));
					SubTaskProgressMonitor coverMonitor = new SubTaskProgressMonitor(monitor, 5, 0);
					StringBuffer data = fax.cover.generate(fax.coverIn, coverMonitor);
					coverMonitor.done();
					
				    // Cover senden
                    byte[] buffer = data.toString().getBytes(FaxCover.CHARSET);
					TransferMonitor transferMonitor = new TransferMonitor(monitor, 5, buffer.length);
					client.addTransferListener(transferMonitor);
					InputStream in = new ByteArrayInputStream(buffer);
					try {
					    serverCoverFilename = client.putTemporary(in);
					}
					finally {
						transferMonitor.transferCompleted();
						client.removeTransferListener(transferMonitor);
						in.close();
					}
					// check if monitor was cancelled
					monitor.work(0);
				}
				else {
					monitor.work(10);
				}

				monitor.setText(i18n.tr("Uploading documents"));
				List<String> serverFilenames = new ArrayList<String>();
				for (File file : fax.documents) {
					TransferMonitor transferMonitor = new TransferMonitor(monitor, 10, file.length());
					client.addTransferListener(transferMonitor);
					InputStream in = new BufferedInputStream(new FileInputStream(file));
					try {
						serverFilenames.add(client.putTemporary(in));
					}
					finally {
						transferMonitor.transferCompleted();
						client.removeTransferListener(transferMonitor);
						in.close();
					}
					// check if monitor was cancelled
					monitor.work(0);
				}
				
				gnu.hylafax.Job sendJob = client.createJob();
				HylaFAXClientHelper.applyParameter(sendJob, getJob());
			    if (serverCoverFilename != null) {
			    	if (Settings.SEND_COVER_AS_DOCUMENT.getValue()) {
			    		sendJob.addDocument(serverCoverFilename);
			    	}
			    	else {
			    		sendJob.setProperty("COVER ", serverCoverFilename);
			    	}
			    }
			    for (String filename : serverFilenames) {
			    	sendJob.addDocument(filename);
			    }
				monitor.work(1);
				
				client.submit(sendJob);
				monitor.work(1);
				
				return null;
			}
		};
		
		try {
			JHylaFAX.getInstance().runJob(SendDialog.this, ioJob);
			JHylaFAX.getInstance().updateTables();
		} 
		catch (UserAbortException e) {
			return false;
		}
		catch (Exception e) {
			logger.debug("Error sending fax", e);
			ErrorDialog.showError(this, i18n.tr("Could not send fax"), 
					i18n.tr("JHylaFAX Error"),
					e);
			return false;
		}
		return true;
	}
	
	private File saveCover(final Fax fax) {	
		if (fax.cover == null || fax.coverIn == null) {
			throw new IllegalArgumentException("fax.cover and fax.coverIn must not be null");
		}
		
		Job<File> ioJob = new Job<File>() {
			public File run(ProgressMonitor monitor) throws Exception
			{
				monitor.setTotalSteps(10);
				
				monitor.setText(i18n.tr("Generating cover"));
				SubTaskProgressMonitor coverMonitor = new SubTaskProgressMonitor(monitor, 5, 0);
				StringBuffer data = fax.cover.generate(fax.coverIn, coverMonitor);
				coverMonitor.done();
				
				File outputFile = File.createTempFile("jhylafax", ".ps");
				outputFile.deleteOnExit();
				OutputStream out = new FileOutputStream(outputFile);
				monitor.setText(i18n.tr("Saving cover"));
				try {
					out.write(data.toString().getBytes(FaxCover.CHARSET));
				}
				finally {
					out.close();
				}
				monitor.work(5);

				return outputFile;
			}
		};

		try {
			return JHylaFAX.getInstance().runJob(SendDialog.this, ioJob);
		} 
		catch (UserAbortException e) {
			return null;
		}
		catch (Exception e) {
			logger.debug("Error previewing cover", e);
			ErrorDialog.showError(this, i18n.tr("Could not preview cover"), 
					i18n.tr("JHylaFAX Error"), e);
			return null;
		}
	}
	
	public void updateLabels() {
		super.updateLabels();

		setTitle(i18n.tr("Send Fax"));
		documentLabel.setText(i18n.tr("Document"));
		includeCoverLabel.setText(i18n.tr("Include Cover"));
		coverSenderLabel.setText(i18n.tr("Sender"));
		coverRecepientLabel.setText(i18n.tr("Recepient"));
		coverSubjectLabel.setText(i18n.tr("Subject"));
		coverCommentLabel.setText(i18n.tr("Comment"));
		
		previewCoverAction.updateLabels();
		moreDocumentsAction.updateLabels();
	}

	public void setDocument(String document) {
		documentFileChooserPanels.get(0).getTextField().setText(document);
	}

	/**
	 * A container used for parameter passing.  
	 */
	private class Fax {
		FaxCover cover;
		InputStream coverIn;
		List<File> documents = new ArrayList<File>();
	}
	
	private class PreviewCoverAction extends AbstractXNapAction implements LocaleChangeListener{
		
		public PreviewCoverAction() {
			//putValue(ICON_FILENAME, "configure.png");
		}

		public void actionPerformed(ActionEvent event) {
			if (!includeCoverCheckBox.isSelected()) {
				throw new IllegalStateException("Cover page is not enabled");
			}
			
			String viewerPath = JHylaFAXHelper.getViewerPath("docq");
			if (viewerPath == null) {
				return;
			}
			
			Fax fax = createFax();
			if (fax == null) {
				return;
			}

			File tempFile = saveCover(fax);
			if (tempFile != null) {
				JHylaFAXHelper.view(viewerPath, new File[] { tempFile });
			}
		}
		
		public void updateLabels() {
			putValue(Action.NAME, i18n.tr("Preview Cover"));
			putValue(Action.SHORT_DESCRIPTION, i18n.tr("Opens an external programm to preview the cover page"));
		}
		
	}

	private class MoreDocumentsAction extends AbstractXNapAction implements LocaleChangeListener{
		
		public MoreDocumentsAction() {
		}

		public void actionPerformed(ActionEvent event) {
			FileChooserPanel documentFileChooserPanel = addDocumentFileChooser();
			documentFileChooserPanel.getTextField().requestFocus();
		}
		
		public void updateLabels() {
			putValue(Action.NAME, i18n.tr("More"));
			putValue(Action.SHORT_DESCRIPTION, i18n.tr("Displays an additional field to enter a document filename"));
		}
		
	}
	
    private class MyFileChooserPanel extends FileChooserPanel {
    	
		public MyFileChooserPanel(int columns)
		{
			super(columns);
		}

		@Override
		protected void fileSelected(File file)
		{
			File[] files = getFileChooser().getSelectedFiles();
			if (files != null && files.length > 1) {
				// files[0] equals file and is handled by the panel
				for (int i = 1; i < files.length; i++) {
					// add a new panel for all other files
					FileChooserPanel chooser = addDocumentFileChooser();
					chooser.setFile(files[i]);
				}
			}
			// reset selection
			getFileChooser().setSelectedFiles(null);
		}
		
    }

	protected FileChooserPanel addDocumentFileChooser()
	{
		FileChooserPanel documentFileChooserPanel = new MyFileChooserPanel(DEFAULT_COLUMNS);
		documentFileChooserPanel.setFileChooser(documentFileChooserPanels.get(0).getFileChooser());
		documentFileChooserPanels.add(documentFileChooserPanel);
		documentPanelBuilder.append("", documentFileChooserPanel);
		documentPanelBuilder.nextLine();
		pack();
		return documentFileChooserPanel;
	}

	public void addDocument(String filename)
	{
		FileChooserPanel panel = addDocumentFileChooser();
		panel.getTextField().setText(filename);
	}

	public void setQuitAfterSending(boolean quitAfterSending) 
	{
		this.quitAfterSending = quitAfterSending;
	}

}
