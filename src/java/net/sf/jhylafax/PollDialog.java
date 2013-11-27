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
import javax.swing.JFrame;
import net.sf.jhylafax.fax.FaxJob;
import net.sf.jhylafax.fax.HylaFAXClientHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xnap.commons.gui.ErrorDialog;
import org.xnap.commons.io.Job;
import org.xnap.commons.io.ProgressMonitor;
import org.xnap.commons.io.UserAbortException;

/**
 * A dialog for polling of faxes.
 * 
 * @author Steffen Pingel
 */
public class PollDialog extends AbstractFaxDialog {

	private final static Log logger = LogFactory.getLog(PollDialog.class);
	
	public PollDialog(JFrame owner) {
		super(owner);
		
		addNumberTextField();
		addDateControls();
		
		FaxJob job = new FaxJob(); 
		HylaFAXClientHelper.initializeFromSettings(job);
		setJob(job);
		
		updateLabels();
		pack();
	}
	
	@Override
	public boolean apply() {
		if (!super.apply()) {
			return false;
		}

		Job<?> ioJob = new Job() {
			public Object run(ProgressMonitor monitor) throws Exception
			{
				monitor.setTotalSteps(4);
				
				HylaFAXClient client = JHylaFAX.getInstance().getConnection(monitor);
				monitor.work(1);
				
				gnu.hylafax.Job pollJob = client.createJob();
				HylaFAXClientHelper.applyParameter(pollJob, getJob());
				pollJob.setProperty("POLL", "\"\" \"\"");
				monitor.work(1);
								
				client.submit(pollJob);
				monitor.work(2);
				
				return null;
			}
		};
		
		try {
			JHylaFAX.getInstance().runJob(PollDialog.this, ioJob);
			JHylaFAX.getInstance().updateTables();
		} 
		catch (UserAbortException e) {
			return false;
		} 
		catch (Exception e) {
			logger.debug("Error polling fax", e);
			ErrorDialog.showError(this, i18n.tr("Could not poll fax"), 
					i18n.tr("JHylaFAX Error"),
					e);
			return false;
		}
		return true;
	}
	
	public void updateLabels() {
		super.updateLabels();
		
		setTitle(i18n.tr("Poll Fax"));
	}

}
