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
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.xnap.commons.gui.ProgressDialog;
import org.xnap.commons.io.Job;
import org.xnap.commons.io.UserAbortProgressMonitor;

public class JobQueue {
	
	private boolean jobRunning = false;
	private long lastUpdate;
	private LinkedList<Notification> notificationQueue = new LinkedList<Notification>();
	
	@SuppressWarnings("unchecked")
	public <T> T runJob(JDialog owner, final Job<T> job) throws Exception {
		if (jobRunning) {
			throw new RuntimeException("Concurrent job invocation");
		}
		
		final Object[] retSuccess = new Object[1];
		final Throwable[] ret = new Exception[1];
		final ProgressDialog dialog;
		
		if (owner != null) { dialog = new ProgressDialog(owner); } 
		else { dialog = new ProgressDialog(JHylaFAX.getInstance()); }

		Runnable runner = new Runnable() {
			public void run()
			{
				try {
					retSuccess[0] = job.run(new UserAbortProgressMonitor(dialog));
				}
				catch (Throwable e) {
					ret[0] = e;
				}
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						dialog.done();
					}
				});
			}
		};

        // make sure no other jobs are started
        jobRunning = true;
        
		Thread t = new Thread(runner, "Job");
		t.start();
		
		// show blocking dialog
		if (owner != null) { dialog.setLocationRelativeTo(owner); } 
		else { dialog.setLocationRelativeTo(JHylaFAX.getInstance()); }
		dialog.setTitle(i18n.tr("HylaFAX Server"));
		dialog.setModal(true);
		dialog.showDialog();
			
		// at this point t is guranteed to be finished
		jobRunning = false;
		
		// XXX what if the errordialog is displayed?
		processNotifications();
		
		if (ret[0] != null) {
			if (ret[0] instanceof Exception) {
				throw (Exception)ret[0];
			}
			else {
				throw new InvocationTargetException(ret[0]);
			}
		}
		return (T)retSuccess[0];
	}
	
	public void setLastUpdate(long lastUpdate)
	{
		this.lastUpdate = lastUpdate;
	}
	
	public boolean isJobRunning()
	{
		return jobRunning;
	}

	public void addNotification(final Notification notification)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				if (isJobRunning()) {
					addNotificationInternal(notification);
				}
				else {
					runNotification(notification);
				}
			}
		});
	}
	
	protected void runNotification(Notification notification)
	{
		notification.run();
	}

	private void addNotificationInternal(Notification notification)
	{
		notificationQueue.add(notification);
	}
	
	private void processNotifications()
	{
		if (isJobRunning()) {
			throw new IllegalStateException();
		}
		
		while (!notificationQueue.isEmpty()) {
			Notification notification = notificationQueue.removeFirst();
			runNotification(notification);
		}
	}
	
}
