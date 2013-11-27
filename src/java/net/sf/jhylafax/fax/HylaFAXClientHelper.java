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
package net.sf.jhylafax.fax;

import gnu.hylafax.Job;
import gnu.inet.ftp.ServerResponseException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;
import net.sf.jhylafax.Settings;
import net.sf.jhylafax.fax.FaxJob.JobType;
import net.sf.jhylafax.fax.FaxJob.PageChopping;
import net.sf.jhylafax.fax.Modem.Volume;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides static methods to handle the server communication.
 * 
 * @author Steffen Pingel
 */
public class HylaFAXClientHelper extends Thread {
	
	/**
	 * The format used for docq, contains all valid tokens except %p.
	 * 
	 * <p>Note: %a, %c, %m adds a line break, therefore it needs to be last.
	 */
	public final static String FILEFMT = "__FILEFMT |%d |%f |%g |%i |%l |%o |%p |%q |%r |%s |%m ";
	
	/**
	 * The format used for sendq and doneq, contains all valid tokens except %T,
	 * %Z, %z.
	 * 
	 * <p>Note: %s may contain line breaks, therefore it is last
	 */
	public final static String JOBFMT = "__JOBFMT |%a |%b |%c |%e |%f |%g |%h |%i |%j |%k |%l |%m |%n |%o |%p |%q |%r |%t |%u |%v |%w |%x |%y |%z"
		+ " |%A |%B |%C |%D |%E |%F |%G |%H |%I |%J |%K |%L |%M |%N |%O |%P |%Q |%R |%S |%U |%V |%W |%X |%Y |%Z |%s ";
	
	private final static Log logger = LogFactory.getLog(HylaFAXClientHelper.class);
	
	/**
	 * The format used for modem status, contains all valid tokens.
	 */
	public final static String MODEMFMT = "__MODEMFMT |%h |%l |%m |%n |%r |%s |%t |%v |%z ";
	
	/**
	 * The format strings need an additional space to avoid empty tokens.
	 */
	protected final static String QUEUE_SEPARATOR = "|";
	
	/**
	 * The format used for recvq, contains all valid tokens except %m, %t.
     * 
     * <p>%q has been removed as well since it causes some versions of HylaFAX 
     * to segfault (#1496477).
	 */
	//public final static String RCVFMT = "__RCVFMT |%Y |%a |%b |%d |%e |%f |%h |%i |%j |%l |%n |%o |%p |%q |%r |%s |%w |%z ";
    public final static String RCVFMT = "__RCVFMT |%Y |%a |%b |%d |%e |%f |%h |%i |%j |%l |%n |%o |%p |%r |%s |%w |%z ";
    
	private static DateFormat fileDateFormat = new SimpleDateFormat("MMM dd HH:mm:ss yyyy", Locale.ENGLISH);

	public static void applyParameter(Job faxJob, FaxJob job) throws ServerResponseException, IOException
	{
		//faxJob.setChopThreshold(3);
		faxJob.setDialstring(job.getNumber());
		if (job.getSender() != null && job.getSender().trim().length() > 0) {
			faxJob.setFromUser(job.getSender());
		}
		faxJob.setKilltime("000259");
		faxJob.setMaximumDials(job.getMaxDials());
		faxJob.setMaximumTries(job.getMaxDials());
		if (job.getNotifyAdress() != null && job.getNotifyAdress().trim().length() > 0) {
			faxJob.setNotifyAddress(job.getNotifyAdress());
		}
		if (job.getNotify() != null) {
			faxJob.setNotifyType(job.getNotify());
		}
		faxJob.setPageChop(Job.CHOP_DEFAULT);
		faxJob.setPageWidth(job.getPageWidth());
		faxJob.setPageLength(job.getPageLength());
		faxJob.setPriority(job.getPriority());
		faxJob.setProperty("SENDTIME", calculateTime(job.getSendTime(), Settings.TIMEZONE.getValue()));
		faxJob.setVerticalResolution(job.getVerticalResolution());
	}
	
	public static String calculateTime(Date sendTime, String timeZoneID) {
		if (sendTime == null) {
			return "NOW";
		}
		else {
			long date = sendTime.getTime();
			
			TimeZone tz = TimeZone.getTimeZone(timeZoneID);
//			tz.setStartRule(Calendar.MARCH, -1, Calendar.SUNDAY,  2*60*60*1000);
//			tz.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY,  2*60*60*1000);

			date -= tz.getRawOffset();
			if (tz.inDaylightTime(sendTime)) {
				date -= 3600 * 1000;
			}

			return new SimpleDateFormat("yyyyMMddHHmm").format(new Date(date));
		}
	}

	private static JobType getJobType(char c)
	{
		switch (c) {
		case 'P':
			return JobType.PAGER;
		default: // 'F'
			return JobType.FACSIMILE;
		}
	}

	private final static String getNotify(char notify) {
		switch (notify) {
		case 'D' :
			return Job.NOTIFY_DONE;
		case 'Q' :
			return Job.NOTIFY_REQUEUE;
		case 'A' :
			return Job.NOTIFY_ALL;
		default :
			return Job.NOTIFY_NONE;
		}
	}

	private static PageChopping getPageChopping(char c)
	{
		switch (c) {
		case 'D':
			return PageChopping.DISABLED;
		case 'A':
			return PageChopping.ALL;
		case 'L':
			return PageChopping.LAST;
		default: // ' '
			return PageChopping.DEFAULT;
		}
	}

	public final static FaxJob.State getState(char state) {
		switch (state) {
		case 'T' : 
			return FaxJob.State.SUSPENDED;
		case 'P' : 
			return FaxJob.State.PENDING;
		case 'S' : 
			return FaxJob.State.SLEEPING;
		case 'B' : 
			return FaxJob.State.BLOCKED;
		case 'W' : 
			return FaxJob.State.WAITING;
		case 'D' : 
			return FaxJob.State.DONE;
		case 'R' : 
			return FaxJob.State.RUNNING;
		case 'F' : 
			return FaxJob.State.FAILED;
		default : // '?'
			return FaxJob.State.UNDEFINED;
		}		
	}
	
	private static Volume getVolume(char c)
	{
		// TODO add switch
		return Volume.OFF;
	}
	
	public final static void initializeFromSettings(FaxJob job) {
		job.setSender(Settings.FULLNAME.getValue());
		job.setNotifyAdress(Settings.EMAIL.getValue());
		job.setMaxDials(Settings.MAXDIALS.getValue());
		job.setMaxTries(Settings.MAXTRIES.getValue());
		job.setNotify(Settings.NOTIFICATION.getValue().getCommand());
		job.setPageLength(Settings.PAPER.getValue().getHeight());
		job.setPageWidth(Settings.PAPER.getValue().getWidth());
		job.setPriority(Settings.PRIORITY.getValue());
		job.setResolution(Settings.RESOLUTION.getValue().getLinesPerInch());
	}

	public static boolean isPostscript(String filename) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
		try {
			return in.readLine().startsWith("%!");
		}
		finally {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
	}

	static int parseDuration(String s)
	{
		StringTokenizer t = new StringTokenizer(s, ":");
		int duration = 0;
		while (t.hasMoreTokens()) {
			int n = Integer.parseInt(t.nextToken());
			duration = duration * 60 + n;
		}
		return duration;
	}

	public final static Document parseFileFmt(String response) {
		StringTokenizer st = new StringTokenizer(response, QUEUE_SEPARATOR);
		StringTokenizer jf = new StringTokenizer(FILEFMT, QUEUE_SEPARATOR);
		Document file = new Document();
		while (st.hasMoreElements() && jf.hasMoreElements()) {
			char c = jf.nextToken().charAt(1);
			String s = st.nextToken().trim();
			if (s.length() > 0) {
				try {
					// parse
					switch (c) {
					case 'a':
						file.setLastAccessTime(fileDateFormat.parse(s));
						break;
					case 'c':
						file.setCreationTime(fileDateFormat.parse(s));
						break;
					case 'd':
						file.setDeviceNumber(Integer.parseInt(s, 8));
						break;
					case 'f':
						file.setFilename(s);
						break;
					case 'g':
						file.setGroupID(Integer.parseInt(s));
						break;
					case 'i':
						file.setInodeNumber(Long.parseLong(s));
						break;
					case 'l':
						file.setLinkCount(Integer.parseInt(s));
						break;
					case 'm':
						file.setLastModificationTime(fileDateFormat.parse(s));
						break;
					case 'o':
						file.setOwner(s);
						break;
					case 'p': // Fax-style protection flags (no group bits)
						// 'q' is used instead
						break;
					case 'q':
						file.setPermissions(s);
						break;
					case 'r':
						file.setRootDeviceNumber(Integer.parseInt(s));
						break;
					case 's':
						file.setFilesize(Long.parseLong(s));
						break;
					case 'u':
						file.setOwnerID(Integer.parseInt(s));
						break;
					}
				}
				catch (NumberFormatException e) {
					logger.info("Error parsing respone", e);
				}
				catch (ParseException e) {
					logger.info("Error parsing response", e);
				}
			}
		}
		return file;
	}
	
	public final static Object parseFmt(String response) {
		if (logger.isDebugEnabled()) logger.debug("Received: " + response);
		
		if (response.trim().length() == 0) {
			// work around a bug in HylaFax
			return null;
		}
		if (response.startsWith("__JOBFMT")) {
			return parseJobFmt(response);
		}
		else if (response.startsWith("__RCVFMT")) {
			return parseRcvFmt(response);
		}
		else if (response.startsWith("__FILEFMT")) {
			return parseFileFmt(response);
		}
		else if (response.startsWith("__MODEMFMT")) {
			return parseModemFmt(response);
		}
		else {
			logger.error("Invalid response: " + response);
			return null;
		}
	}

	public final static FaxJob parseJobFmt(String response) {
		StringTokenizer st = new StringTokenizer(response, QUEUE_SEPARATOR);
		StringTokenizer jf = new StringTokenizer(JOBFMT, QUEUE_SEPARATOR);
		FaxJob job = new FaxJob();
		while (st.hasMoreElements() && jf.hasMoreElements()) {
			char c = jf.nextToken().charAt(1);
			String s = st.nextToken().trim();
			if (s.length() > 0) {
				try {
					switch (c) {
					case 'a' :
						job.setState(getState(s.charAt(0)));
						break;
					case 'b':
						job.setConsecutiveFailedTries(Integer.parseInt(s));
						break;
					case 'c':
						job.setClientMachineName(s);
						break;
					case 'd' :
						job.setDialsAttempted(Integer.parseInt(s));
						break;
					case 'e' :
						job.setNumber(s);
						break;
					case 'f':
						job.setConsecutiveFailedDials(Integer.parseInt(s));
						break;
					case 'g':
						job.setGroupID(Integer.parseInt(s));
						break;
					case 'h':
						job.setPageChopping(getPageChopping(s.charAt(0)));
						break;
					case 'i' :
						job.setPriority((new Integer(s)).intValue());
						break;
					case 'j' :
						job.setID((new Integer(s)).intValue());
						break;
					case 'k':
						job.setKillTime(s);
						break;
					case 'l' :
						// FIXME 'any' job.setPageLength(Integer.parseInt(s));
						break;
					case 'm':
						job.setAssignedModem(s);
						break;
					case 'n' :
						job.setNotify(getNotify(s.charAt(0)));
						break;
					case 'o' :
						job.setOwner(s);
						break;
					case 'p':
						job.setPagesTransmitted(Integer.parseInt(s));
						break;
					case 'q':
						job.setRetryTime(parseDuration(s));
						break;
					case 'r' :
						job.setResolution((new Integer(s)).intValue());
						break;
					case 's' :
						job.setLastError(s);
						break;
					case 't' :
						job.setTriesAttempted((new Integer(s)).intValue());
						break;
					case 'u' :
						job.setMaxTries((new Integer(s)).intValue());
						break;
					case 'v':
						job.setClientDialString(s);
						break;
					case 'w' :
						job.setPageWidth(Integer.parseInt(s));
						break;
					case 'x' :
						// FIXME 'x/y' job.setMaxDials((new Integer(s)).intValue());
						break;
					case 'z' :
						// the handling code never worked correctly, use
						// 'Y' instead
						//Date date = parseDate(s, true);
						//job.setSendTime(date);
						break;
					case 'A':
						job.setDestinationSubAddress(s);
						break;
					case 'B':
						job.setDestinationPassword(s);
						break;
					case 'C':
						job.setDestinationCompanyName(s);
						break;
					case 'D' : {
						StringTokenizer t = new StringTokenizer(s, ":");
						job.setDialsAttempted(Integer.parseInt(t.nextToken()));
						job.setMaxDials(Integer.parseInt(t.nextToken()));
						break; }
					case 'E':
						job.setDesiredSignallingRate(s);
						break;
					case 'F':
						job.setClientDialString(s);
						break;
					case 'G':
						job.setDesiredMinScanline(s);
						break;
					case 'H':
						job.setDesiredDataFormat(s);
						break;
					case 'I':
						job.setClientSchedulingPriority(s);
						break;
					case 'J':
						job.setClientJobTag(s);
						break;
					case 'K':
						job.setDesiredECM(s);
						break;
					case 'L':
						job.setDestinationLocation(s);
						break;
					case 'M':
						job.setNotifyAdress(s);
						break;
					case 'N':
						job.setUsePrivateTagLine("P".equals(s));
						break;
					case 'P' : {
						StringTokenizer t = new StringTokenizer(s, ":");
						job.setPagesTransmitted(Integer.parseInt(t.nextToken()));
						job.setPageCount(Integer.parseInt(t.nextToken()));
						break; }
					case 'R':
						job.setReceiver(s);
						break;
					case 'S':
						job.setSender(s);
						break;
					case 'T': // Total # tries/maximum # tries
						// %t, %u are used instead
						break;
					case 'U':
						job.setChoppingThreshold(Double.parseDouble(s));
						break;
					case 'V':
						job.setJobDoneOperation(s);
						break;
					case 'W':
						job.setCommunicationIdentifier(s);
						break;
					case 'X':
						job.setJobType(getJobType(s.charAt(0)));
						break;
					case 'Y': {
						Date date = new SimpleDateFormat("yyyy/MM/dd HH.mm.ss").parse(s); 
						job.setSendTime(date);
						break; }
					case 'Z': {
						// should work, but for some reason calculates the
						// wrong time, so 'Y' is used instead
						Date date = new Date(Long.parseLong(s));
						//job.setSendTime(date); 
						break; }
					}
				} 
				catch (ParseException e) {
					logger.info("Error parsing response", e);
				}
				catch (NumberFormatException e) {
					logger.info("Error parsing response", e);
				}
				catch (NoSuchElementException e) {
					logger.info("Error parsing response", e);
				}
			}
		}
		return job;
	}

	
	public final static Modem parseModemFmt(String response) {
		StringTokenizer st = new StringTokenizer(response, QUEUE_SEPARATOR);
		StringTokenizer jf = new StringTokenizer(MODEMFMT, QUEUE_SEPARATOR);
		Modem modem = new Modem();
		while (st.hasMoreElements() && jf.hasMoreElements()) {
			char c = jf.nextToken().charAt(1);
			String s = st.nextToken().trim();
			if (s.length() > 0) {
				try {
					switch (c) {
					case 'h':
						modem.setHostname(s);
						break;
					case 'l':
						modem.setLocalIdentifier(s);
						break;
					case 'm':
						modem.setCanonicalName(s);
						break;
					case 'n':
						modem.setFaxNumber(s);
						break;
					case 'r':
						modem.setMaxPagesPerCall(Integer.parseInt(s));
						break;
					case 's':
						modem.setStatus(s);
						break;
					case 't': {
						StringTokenizer t = new StringTokenizer(s, ":");
						modem.setServerTracing(Integer.parseInt(t.nextToken()));
						modem.setSessionTracing(Integer.parseInt(t.nextToken()));
						break; }
					case 'v':
						modem.setSpeakerVolume(getVolume(s.charAt(0)));
						break;
					case 'z':
						modem.setRunning("*".equals(s));
						break;
					}
				}
				catch (NumberFormatException e) {
					logger.info("Error parsing respone", e);
				}
				catch (NoSuchElementException e) {
					logger.info("Error parsing response", e);
				}
			}
		}
		return modem;
	}
	
	public final static ReceivedFax parseRcvFmt(String response) {
		StringTokenizer st = new StringTokenizer(response, QUEUE_SEPARATOR);
		StringTokenizer jf = new StringTokenizer(RCVFMT, QUEUE_SEPARATOR);
		ReceivedFax fax = new ReceivedFax();
		while (st.hasMoreElements() && jf.hasMoreElements()) {
			char c = jf.nextToken().charAt(1);
			String s = st.nextToken().trim();
			if (s.length() > 0) {
				try {
					switch (c) {
					case 'Y':
						Date date = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(s); 
						fax.setReceivedTime(date);
						break;
					case 'a' :
						fax.setSubAddress(s);
						break;
					case 'b' :
						fax.setSignallingRate(Integer.parseInt(s));
						break;
					case 'd' :
						fax.setDataFormat(s);
						break;
					case 'e' :
						fax.setLastError(s);
						break;
					case 'f' :
						fax.setFilename(s);
						break;
					case 'h' :
						fax.setTimeSpent(parseDuration(s));
						break;
					case 'i' :
						fax.setCallerIDName(s);
						break;
					case 'j' :
						fax.setCallerIDNumber(s);
						break;
					case 'l' :
						// FIXME '4294967295' fax.setPageLength(Integer.parseInt(s));
						break;
					case 'm' : // Fax-style protection mode string
						// 'q' is used instead
						break;
					case 'n' :
						fax.setFilesize(Long.parseLong(s));
						break;
					case 'o' :
						fax.setOwner(s);
						break;
					case 'p' :
						fax.setPageCount(Integer.parseInt(s));
						break;
					case 'q' :
						fax.setProtectionMode(Integer.parseInt(s));
						break;
					case 'r' :
						fax.setResolution(Integer.parseInt(s));
						break;
					case 's' :
						fax.setSender(s);
						break;
					case 't' :
						// the handling code never worked correctly
						// 'Y' is used instead
						//job.setSendTime(parseDate(s, false));
						break;
					case 'w' :
						fax.setPageWidth(Integer.parseInt(s));
						break;
					case 'z' :
						fax.setReceiving(s.equals("*"));
						break;
					}
				} 
				catch (ParseException e) {
					logger.info("Error parsing response", e);
				}
				catch (NumberFormatException e) {
					logger.info("Error parsing response", e);
				}
			}
		}
		return fax;
	}
	
	public static void setJobProperties(Job faxJob, FaxJob job) throws ServerResponseException, IOException
	{
		/*
		job.setNumber(faxJob.getDialstring());
		job.setChopThreshold(faxJob.getChopThreshold());
		job.setDialstring(faxJob.getNumber());
		job.setSender(faxJob.getSender());
		job.setKilltime(faxJob.getKilltime());
		job.setMaxDials(faxJob.getMaximumDials());
		job.setMaxTries(faxJob.getMaximumTries());
		job.setNotifyAddress(faxJob.getNotifyAddress());
		job.setNotify(faxJob.getNotifyType());
		job.setPageChop(faxJob.getPageChop());
		job.setPaperWidth(faxJob.getPageWidth());
		job.setPaperHeight(faxJob.getPageLength());
		job.setPriority(faxJob.getPriority());
		job.setSendTime(faxJob.getRetrytime());
		job.setResolution(faxJob.getVerticalResolution());
		job.setDocumentNames(faxJob.getDocumentName());
		*/
	}

}