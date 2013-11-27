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
import net.sf.jhylafax.fax.FaxJob;

import org.xnap.commons.gui.DefaultDialog;

/**
 * A dialog for editing properties of a fax job.
 * 
 * @author Steffen Pingel
 */
public class JobDialog extends DefaultDialog implements LocaleChangeListener {

	private JobPanel jobPanel;
	private FaxJob job;
	
	public JobDialog(FaxJob job) {
		super(BUTTON_OKAY | BUTTON_CANCEL);
	
		this.job = job;
		
		setTitle(i18n.tr("Job"));
		
		jobPanel = new JobPanel(true);
		setMainComponent(jobPanel);
		
		updateLabels();
		pack();
	}
	
	@Override
	public boolean apply() {
		jobPanel.applyTo(job);
		return true;
	}
	
	public JobPanel getJobPanel() {
		return jobPanel;
	}
	
	public void revert() {
		jobPanel.revertFrom(job);
	}
	
	public void updateLabels() {
		jobPanel.updateLabels();
	}

}
