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
import gnu.inet.ftp.TransferListener;
import org.xnap.commons.io.ProgressMonitor;
import org.xnap.commons.io.SubTaskProgressMonitor;
import org.xnap.commons.io.UserAbortException;

/**
 * A mediator that links a {@link gnu.inet.ftp.TransferListener} and
 * a {@link org.xnap.commons.io.ProgressMonitor} object.
 * 
 * @author Steffen Pingel
 */
public class TransferMonitor implements TransferListener {

	private SubTaskProgressMonitor monitor;

	public TransferMonitor(ProgressMonitor monitor, int amount, long totalSize) {
		this.monitor = new SubTaskProgressMonitor(monitor, amount, totalSize);
	}

	public void transferStarted()
	{
	}

	public void transferCompleted()
	{
		try {
			monitor.done();
		}
		catch (UserAbortException e) {
			// user cancelled transfer, will be handled elsewhere
		}
	}

	public void transfered(long value)
	{
		try {
			monitor.setValue(value);
		}
		catch (UserAbortException e) {
			// may not throw UserAbortException as this will break the server
			// communication: this means no way to abort a transfer, somewhat 
			// ugly but there is no way to call Putter.cancel() because the 
			// Putter object is not accessible
			monitor.setText(i18n.tr("Aborting transfer, please wait"));
		}
	}

	public void transferFailed()
	{
		try {
			monitor.done();
		}
		catch (UserAbortException e) {
			// user cancelled transfer, will be handled elsewhere
		}
	}

}
