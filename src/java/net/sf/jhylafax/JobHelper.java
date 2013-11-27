package net.sf.jhylafax;

import static net.sf.jhylafax.JHylaFAX.i18n;
import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import net.sf.jhylafax.fax.Document;
import net.sf.jhylafax.fax.FaxJob;
import net.sf.jhylafax.fax.HylaFAXClientHelper;
import net.sf.jhylafax.fax.ReceivedFax;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xnap.commons.io.Job;
import org.xnap.commons.io.ProgressMonitor;
import org.xnap.commons.io.UserAbortException;

public class JobHelper {
	
	private final static Log logger = LogFactory.getLog(JobHelper.class);
	
	static void retryJob(final int jobID) {
		Job<?> ioJob = new Job() {
			public Object run(ProgressMonitor monitor) throws Exception
			{
				monitor.setTotalSteps(3);
				
				HylaFAXClient client = JHylaFAX.getInstance().getConnection(monitor);
				monitor.work(1);

				monitor.setText(i18n.tr("Retrying job"));
				gnu.hylafax.Job editJob = client.getJob(jobID);
				client.suspend(editJob);
				editJob.setProperty("SENDTIME", "NOW");
				monitor.work(1);
				
				client.submit(editJob);
				monitor.work(1);

				return null;
			}
		};
		
		try {
			JHylaFAX.getInstance().runJob(ioJob);
			JHylaFAX.getInstance().updateTables();
		} 
		catch (UserAbortException e) {
		} 
		catch (Exception e) {
			logger.debug("Error retrying job", e);
			JHylaFAX.getInstance().showError(i18n.tr("Could not retry job"), e);
		}
	}

	static void resumeJob(final int jobID) {
		Job<?> ioJob = new Job() {
			public Object run(ProgressMonitor monitor) throws Exception
			{
				monitor.setTotalSteps(2);
				
				HylaFAXClient client = JHylaFAX.getInstance().getConnection(monitor);
				monitor.work(1);

				monitor.setText(i18n.tr("Resuming job"));
				gnu.hylafax.Job editJob = client.getJob(jobID);
				client.submit(editJob);
				monitor.work(1);

				return null;
			}
		};
		
		try {
			JHylaFAX.getInstance().runJob(ioJob);
			JHylaFAX.getInstance().updateTables();
		} 
		catch (UserAbortException e) {
		} 
		catch (Exception e) {
			logger.debug("Error resuming job", e);
			JHylaFAX.getInstance().showError(i18n.tr("Could not resume job"), e); 
		}
	}

	static FileStat[] retrieveJobFilenames(final int jobID) {
		Job<FileStat[]> ioJob = new Job<FileStat[]>() {
			public FileStat[] run(ProgressMonitor monitor) throws Exception
			{
				monitor.setTotalSteps(2);
				
				HylaFAXClient client = JHylaFAX.getInstance().getConnection(monitor);
				monitor.work(1);

				monitor.setText(i18n.tr("Getting document filenames"));
				gnu.hylafax.Job editJob = client.getJob(jobID);
				String[] filenames = editJob.getDocumentName().split("\n");
				
				List<FileStat> results = new ArrayList<FileStat>(filenames.length);
				for (int i = 0; i < filenames.length; i++) {
					StringTokenizer t = new StringTokenizer(filenames[i]);
					String filetype = t.nextToken();
					if (t.hasMoreTokens() && 
							("PS".equalsIgnoreCase(filetype) || "PDF".equalsIgnoreCase(filetype))) {
						String filename = t.nextToken();
						try {
							long filesize = client.size(filename);
							results.add(new FileStat(filename, filesize));
						}
						catch (FileNotFoundException e) {
						}
					}
				}
				monitor.work(1);

				return results.toArray(new FileStat[0]);
			}
		};
		
		try {
			return JHylaFAX.getInstance().runJob(ioJob);
		} 
		catch (UserAbortException e) {
		} 
		catch (Exception e) {
			logger.debug("Error getting job documents", e);
			JHylaFAX.getInstance().showError(i18n.tr("Could not get filenames"), e); 
		}
		return null;
	}

	static boolean delete(final String filename) {
		Job<?> ioJob = new Job() {
			public Object run(ProgressMonitor monitor) throws Exception
			{
				monitor.setTotalSteps(2);
				monitor.setText(i18n.tr("Deleting file"));
				HylaFAXClient client = JHylaFAX.getInstance().getConnection(monitor);
				monitor.work(1);
				client.dele(filename);
				monitor.work(1);
				return null;
			}
		};
		
		try {
			JHylaFAX.getInstance().runJob(ioJob);
			JHylaFAX.getInstance().updateTables();
		} 
		catch (UserAbortException e) {
			return false;
		}
		catch (Exception e) {
			logger.debug("Error deleting file " + filename, e);
			JHylaFAX.getInstance().showError(i18n.tr("Could not delete file"), e); 
			return false;
		}
		return true;
	}

	static boolean save(final File file, final String filename, final long size) {
		Job<?> ioJob = new Job() {
			public Object run(ProgressMonitor monitor) throws Exception
			{
				monitor.setTotalSteps(12);
				
				HylaFAXClient client = JHylaFAX.getInstance().getConnection(monitor);
				monitor.work(1);
				
				monitor.setText(i18n.tr("Setting mode"));
				client.mode(HylaFAXClient.MODE_STREAM);
				client.type(HylaFAXClient.TYPE_IMAGE);
				monitor.work(1);

				monitor.setText(i18n.tr("Downloading file"));
				TransferMonitor transferMonitor = new TransferMonitor(monitor, 10, size);
				OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
				try {
					client.addTransferListener(transferMonitor);
					client.get(filename, out);
				}
				finally {
					transferMonitor.transferCompleted();
					client.removeTransferListener(transferMonitor);
					out.close();
				}
				// check if monitor was cancelled
				monitor.work(0);
				return null;
			}
		};
		
		try {
			JHylaFAX.getInstance().runJob(ioJob);
		} 
		catch (UserAbortException e) {
			return false;
		}
		catch (Exception e) {
			logger.debug("Error getting file " + filename, e);
			JHylaFAX.getInstance().showError(i18n.tr("Could not download file"), e); 
			return false;
		}
		return true;
	}

	static void removeJob(final int jobID) {
		Job<?> ioJob = new Job() {
			public Object run(ProgressMonitor monitor) throws Exception
			{
				monitor.setTotalSteps(2);
				
				HylaFAXClient client = JHylaFAX.getInstance().getConnection(monitor);
				monitor.work(1);

				monitor.setText(i18n.tr("Removing job"));
				gnu.hylafax.Job editJob = client.getJob(jobID);
				client.kill(editJob);
				monitor.work(1);

				return null;
			}
		};
		
		try {
			JHylaFAX.getInstance().runJob(ioJob);
			JHylaFAX.getInstance().updateTables();
		} 
		catch (UserAbortException e) {
		} 
		catch (Exception e) {
			logger.debug("Error killing job", e);
			JHylaFAX.getInstance().showError(i18n.tr("Could not remove job"), e); 
		}
	}

	static void suspendJob(final int jobID) {
		Job<?> ioJob = new Job() {
			public Object run(ProgressMonitor monitor) throws Exception
			{
				monitor.setTotalSteps(2);
				
				HylaFAXClient client = JHylaFAX.getInstance().getConnection(monitor);
				monitor.work(1);

				monitor.setText(i18n.tr("Suspending job"));
				gnu.hylafax.Job editJob = client.getJob(jobID);
				client.suspend(editJob);
				monitor.work(1);

				return null;
			}
		};
		
		try {
			JHylaFAX.getInstance().runJob(ioJob);
			JHylaFAX.getInstance().updateTables();
		} 
		catch (UserAbortException e) {
		} 
		catch (Exception e) {
			logger.debug("Error suspending job", e);
			JHylaFAX.getInstance().showError(i18n.tr("Could not suspend job"), e); 
		}
	}

	public static class FileStat {

		public String filename;
		public long filesize;

		public FileStat(String filename, long filesize)
		{
			this.filename = filename;
			this.filesize = filesize;
		}
		
	}

	public static StatusResponse updateStatus()
	{
		Job<StatusResponse> ioJob = new StatusUpdateJob(); 
		try {
			return JHylaFAX.getInstance().runJob(ioJob);
		} 
		catch (UserAbortException e) {
		} 
		catch (Exception e) {
			logger.debug("Error updating status", e);
			JHylaFAX.getInstance().showError(i18n.tr("Could not get status"), e); 
		}
		return null;
	}

	public static class StatusResponse {
		String status;
		String verboseStatus;
		List<ReceivedFax> recvq;
		List<FaxJob> sendq;
		List<FaxJob> pollq;
		List<FaxJob> doneq;
		List<Document> docq;
	}

	public static class StatusUpdateJob implements Job<StatusResponse> {
		@SuppressWarnings("unchecked")
		public StatusResponse run(ProgressMonitor monitor) throws Exception {				
			monitor.setTotalSteps(1 + 1 + 5 /* connection + status + queues*/);
			
			HylaFAXClient client = JHylaFAX.getInstance().getConnection(monitor);
			monitor.work(1);
			
			StatusResponse response = new StatusResponse();
			
			monitor.setText(i18n.tr("Getting Status"));
			List<String> lines = client.getList("status");
			StringBuffer sb = new StringBuffer();
			for (Iterator<String> it = lines.iterator(); it.hasNext();) {
				String line = it.next();
				if (response.status == null) {
					response.status = line;
				}
				sb.append(line + "\n"); 
			}
			response.verboseStatus = sb.toString();
			monitor.work(1);
			
			client.jobfmt(HylaFAXClientHelper.JOBFMT);
			client.rcvfmt(HylaFAXClientHelper.RCVFMT);
			client.filefmt(HylaFAXClientHelper.FILEFMT);
			
			response.recvq = getQueue(monitor, client, "recvq", ReceivedFax.class);
			response.sendq = getQueue(monitor, client, "sendq", FaxJob.class);
			response.pollq = getQueue(monitor, client, "pollq", FaxJob.class);
			response.doneq = getQueue(monitor, client, "doneq", FaxJob.class);
			response.docq = getQueue(monitor, client, "docq", Document.class);
			
			return response;
		}				
		
		@SuppressWarnings("unchecked")
		private <T> List<T> getQueue(ProgressMonitor monitor, HylaFAXClient client, String name, Class<T> clazz) 
		throws IOException, ServerResponseException {
			monitor.setText(i18n.tr("Getting {0}", name));
			List<T> result = new ArrayList<T>();
			List<String> lines = client.getList(name);
			for (String line : lines) {
				Object o = HylaFAXClientHelper.parseFmt(line);
				if (o != null && clazz.isAssignableFrom(o.getClass())) {
					result.add((T)o);
				}
			}
			monitor.work(1);				
			return result;
		}
		
	}
	
}
