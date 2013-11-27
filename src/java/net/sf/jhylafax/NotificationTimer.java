package net.sf.jhylafax;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import net.sf.jhylafax.JobHelper.StatusResponse;
import net.sf.jhylafax.JobHelper.StatusUpdateJob;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xnap.commons.io.Job;
import org.xnap.commons.io.NullProgressMonitor;


public class NotificationTimer {
	
	private final static Log logger = LogFactory.getLog(NotificationTimer.class);
	private Timer timer;
	private TimerTask monitorPathTask;
	private StatusUpdateTask statusUpdateTask;
	
	public NotificationTimer()
	{
		timer = new Timer();
	}

	public void cancel()
	{
		timer.cancel();
	}
	
	public void settingsUpdated()
	{
		if (monitorPathTask != null) {
			monitorPathTask.cancel();
			monitorPathTask = null;
		}
		
		if (Settings.DO_MONITOR_PATH.getValue()) {
			monitorPathTask = new MonitorPathTask(Settings.MONITOR_PATH.getValue());
			timer.schedule(monitorPathTask, 0, Settings.MONITOR_PATH_INTERVAL.getValue() * 1000);
		}
		
		if (statusUpdateTask != null) {
			statusUpdateTask.cancel();
			statusUpdateTask = null;
		}
		
		if (Settings.DO_AUTO_UPDATE.getValue()) {
			statusUpdateTask  = new StatusUpdateTask();
			timer.schedule(statusUpdateTask, 0, Settings.AUTO_UPDATE_INTERVAL.getValue() * 1000);
		}	

	}
	
	private class MonitorPathTask extends TimerTask {

		private File file;
		private long lastUpdate;

		public MonitorPathTask(String path)
		{
			this.file = new File(path);
			this.lastUpdate = file.lastModified();
		}

		@Override
		public void run()
		{
			long check = file.lastModified();
			if (check != lastUpdate) {
				JHylaFAX.getInstance().runNotification(new Notification() {
					public void run()
					{
						SendDialog dialog = new SendDialog(JHylaFAX.getInstance());
						dialog.setDocument(file.getAbsolutePath());
						dialog.setLocationRelativeTo(JHylaFAX.getInstance());
						dialog.setVisible(true);
					}					
				});
				this.lastUpdate = check; 
			}
		}
		
	}	

	private class StatusUpdateTask extends TimerTask {

		@Override
		public void run()
		{
			Job<StatusResponse> ioJob = new StatusUpdateJob();
			try {
				StatusResponse response = ioJob.run(new NullProgressMonitor());
				JHylaFAX.getInstance().updateTables(response);
			}
			catch (Exception e) {
				logger.warn("Error during auto status update", e);
			}
		}
		
		
	}
	
}
