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

import java.util.Date;

/**
 * Representation of a fax job.
 */
public class FaxJob {
	
	private String destinationSubAddress;
	private String destinationPassword;
	private String destinationCompanyName;
	private String desiredSignallingRate;
	private String taglineFormat;
	private String desiredMinScanline;
	private String desiredDataFormat;
	private String clientSchedulingPriority;
	private String clientJobTag;
	private String desiredECM;
	private String destinationLocation;
	private boolean usePrivateTagLine;
	private boolean useContinuationCoverPage;
	private int clientMinimumSignallingRate;
	private String receiver;
	private double choppingThreshold; // inch
	private String jobDoneOperation;
	private String communicationIdentifier;
	private JobType jobType;
	private int consecutiveFailedTries;
	private String clientMachineName;
	private int consecutiveFailedDials;
	private int groupID;
	private	PageChopping pageChopping;
	private String killTime;
	private String assignedModem;
	private int retryTime; // seconds
	private String clientDialString;
	private int dialsAttempted;
	private int ID = -1;
	private String lastError;
	private int maxDials;
	private int maxTries;
	private String notify;
	private String notifyAdress;
	private String number;
	private String owner;
	private int pageCount;
	private int pagesTransmitted;
	private int pageLength;
	private int pageWidth;
	private String permissions;
	private int priority;
	private int verticalResolution;
	private int horizontalResolution;
	private String result;
	private String sender;
	private Date sendTime;
	private State state = State.UNDEFINED;
	private int tag;
	private int triesAttempted;
	
	public int getDialsAttempted()
	{
		return dialsAttempted;
	}
		
	public int getID()
	{
		return ID;
	}
	
	public String getLastError()
	{
		return lastError;
	}

	public int getMaxDials()
	{
		return maxDials;
	}
	
	public int getMaxTries()
	{
		return maxTries;
	}
	
	public String getNotify()
	{
		return notify;
	}
	
	public String getNotifyAdress()
	{
		return notifyAdress;
	}
	
	public String getNumber()
	{
		return number;
	}
	
	public String getOwner()
	{
		return owner;
	}
	
	public int getPageCount()
	{
		return pageCount;
	}
	
	public int getPagesTransmitted()
	{
		return pagesTransmitted;
	}
	
	public int getPageLength()
	{
		return pageLength;
	}
	
	public int getPageWidth()
	{
		return pageWidth;
	}
	
	public String getPermissions()
	{
		return permissions;
	}
	
	public int getPriority()
	{
		return priority;
	}
	
	public int getVerticalResolution()
	{
		return verticalResolution;
	}
	
	public String getResult()
	{
		return result;
	}
	
	public String getSender()
	{
		return sender;
	}
	
	public Date getSendTime()
	{
		return sendTime;
	}
	
	public State getState()
	{
		return state;
	}
	
	public int getTag()
	{
		return tag;
	}
	
	public int getTriesAttempted()
	{
		return triesAttempted;
	}
	
	public void setDialsAttempted(int dialsAttempted)
	{
		this.dialsAttempted = dialsAttempted;
	}
	
	public void setID(int id)
	{
		ID = id;
	}
	
	public void setLastError(String lastError)
	{
		this.lastError = lastError;
	}
	
	public void setMaxDials(int maxDials)
	{
		this.maxDials = maxDials;
	}
	
	public void setMaxTries(int maxTries)
	{
		this.maxTries = maxTries;
	}
	
	public void setNotify(String notify)
	{
		this.notify = notify;
	}
	
	public void setNotifyAdress(String notifyAdress)
	{
		this.notifyAdress = notifyAdress;
	}
	
	public void setNumber(String number)
	{
		this.number = number;
	}
	
	public void setOwner(String owner)
	{
		this.owner = owner;
	}
	
	public void setPageCount(int pageCount)
	{
		this.pageCount = pageCount;
	}
	
	public void setPagesTransmitted(int pagesTransmitted)
	{
		this.pagesTransmitted = pagesTransmitted;
	}
	
	public void setPageLength(int pageLength)
	{
		this.pageLength = pageLength;
	}
	
	public void setPageWidth(int pageWidth)
	{
		this.pageWidth = pageWidth;
	}
	
	public void setPermissions(String permissions)
	{
		this.permissions = permissions;
	}
	
	public void setPriority(int priority)
	{
		this.priority = priority;
	}
	
	public void setResolution(int resolution)
	{
		this.verticalResolution = resolution;
	}
	
	public void setResult(String result)
	{
		this.result = result;
	}
	
	public void setSender(String sender)
	{
		this.sender = sender;
	}
	
	public void setSendTime(Date sendTime)
	{
		this.sendTime = sendTime;
	}

	public void setState(State state)
	{
		this.state = state;
	}
	
	public void setTag(int tag)
	{
		this.tag = tag;
	}

	public void setTriesAttempted(int triesAttempted)
	{
		this.triesAttempted = triesAttempted;
	}
	
	public String getAssignedModem()
	{
		return assignedModem;
	}
	
	public double getChoppingThreshold()
	{
		return choppingThreshold;
	}
	
	public String getClientDialString()
	{
		return clientDialString;
	}
	
	public String getClientJobTag()
	{
		return clientJobTag;
	}
	
	public String getClientMachineName()
	{
		return clientMachineName;
	}

	public int getClientMinimumSignallingRate()
	{
		return clientMinimumSignallingRate;
	}

	public String getClientSchedulingPriority()
	{
		return clientSchedulingPriority;
	}
	
	public String getCommunicationIdentifier()
	{
		return communicationIdentifier;
	}
	
	public int getConsecutiveFailedDials()
	{
		return consecutiveFailedDials;
	}

	public int getConsecutiveFailedTries()
	{
		return consecutiveFailedTries;
	}

	public String getDesiredDataFormat()
	{
		return desiredDataFormat;
	}
	
	public String getDesiredECM()
	{
		return desiredECM;
	}
	
	public String getDesiredMinScanline()
	{
		return desiredMinScanline;
	}
	
	public String getDesiredSignallingRate()
	{
		return desiredSignallingRate;
	}
	
	public String getDestinationCompanyName()
	{
		return destinationCompanyName;
	}

	public String getDestinationLocation()
	{
		return destinationLocation;
	}
	
	public String getDestinationPassword()
	{
		return destinationPassword;
	}
	
	public String getDestinationSubAddress()
	{
		return destinationSubAddress;
	}
	
	public int getGroupID()
	{
		return groupID;
	}
	
	public int getHorizontalResolution()
	{
		return horizontalResolution;
	}
	
	public String getJobDoneOperation()
	{
		return jobDoneOperation;
	}
	
	public JobType getJobType()
	{
		return jobType;
	}

	
	public String getKillTime()
	{
		return killTime;
	}
	
	public PageChopping getPageChopping()
	{
		return pageChopping;
	}

	
	public String getReceiver()
	{
		return receiver;
	}

	
	public int getRetryTime()
	{
		return retryTime;
	}

	public String getTaglineFormat()
	{
		return taglineFormat;
	}
	
	public boolean isUseContinuationCoverPage()
	{
		return useContinuationCoverPage;
	}
	
	public boolean isUsePrivateTagLine()
	{
		return usePrivateTagLine;
	}
	
	public void setAssignedModem(String assignedModem)
	{
		this.assignedModem = assignedModem;
	}
	
	public void setChoppingThreshold(double choppingThreshold)
	{
		this.choppingThreshold = choppingThreshold;
	}
	
	public void setClientDialString(String clientDialString)
	{
		this.clientDialString = clientDialString;
	}
	
	public void setClientJobTag(String clientJobTag)
	{
		this.clientJobTag = clientJobTag;
	}
	
	public void setClientMachineName(String clientMachineName)
	{
		this.clientMachineName = clientMachineName;
	}
	
	public void setClientMinimumSignallingRate(int clientMinimumSignallingRate)
	{
		this.clientMinimumSignallingRate = clientMinimumSignallingRate;
	}
	
	public void setClientSchedulingPriority(String clientSchedulingPriority)
	{
		this.clientSchedulingPriority = clientSchedulingPriority;
	}
	
	public void setCommunicationIdentifier(String communicationIdentifier)
	{
		this.communicationIdentifier = communicationIdentifier;
	}
	
	public void setConsecutiveFailedDials(int consecutiveFailedDials)
	{
		this.consecutiveFailedDials = consecutiveFailedDials;
	}
	
	public void setConsecutiveFailedTries(int consecutiveFailedTries)
	{
		this.consecutiveFailedTries = consecutiveFailedTries;
	}
	
	public void setDesiredDataFormat(String desiredDataFormat)
	{
		this.desiredDataFormat = desiredDataFormat;
	}
	
	public void setDesiredECM(String desiredECM)
	{
		this.desiredECM = desiredECM;
	}
	
	public void setDesiredMinScanline(String desiredMinScanline)
	{
		this.desiredMinScanline = desiredMinScanline;
	}
	
	public void setDesiredSignallingRate(String desiredSignallingRate)
	{
		this.desiredSignallingRate = desiredSignallingRate;
	}
	
	public void setDestinationCompanyName(String destinationCompanyName)
	{
		this.destinationCompanyName = destinationCompanyName;
	}
	
	public void setDestinationLocation(String destinationLocation)
	{
		this.destinationLocation = destinationLocation;
	}
	
	public void setDestinationPassword(String destinationPassword)
	{
		this.destinationPassword = destinationPassword;
	}
	
	public void setDestinationSubAddress(String destinationSubAddress)
	{
		this.destinationSubAddress = destinationSubAddress;
	}
	
	public void setGroupID(int groupID)
	{
		this.groupID = groupID;
	}
	
	public void setHorizontalResolution(int horizontalResolution)
	{
		this.horizontalResolution = horizontalResolution;
	}
	
	public void setJobDoneOperation(String jobDoneOperation)
	{
		this.jobDoneOperation = jobDoneOperation;
	}
	
	public void setJobType(JobType jobType)
	{
		this.jobType = jobType;
	}
	
	public void setKillTime(String killTime)
	{
		this.killTime = killTime;
	}
	
	public void setPageChopping(PageChopping pageChopping)
	{
		this.pageChopping = pageChopping;
	}

	public void setReceiver(String receiver)
	{
		this.receiver = receiver;
	}
	
	public void setRetryTime(int retryTime)
	{
		this.retryTime = retryTime;
	}
	
	public void setTaglineFormat(String taglineFormat)
	{
		this.taglineFormat = taglineFormat;
	}
	
	public void setUseContinuationCoverPage(boolean useContinuationCoverPage)
	{
		this.useContinuationCoverPage = useContinuationCoverPage;
	}
	
	public void setUsePrivateTagLine(boolean usePrivateTagLine)
	{
		this.usePrivateTagLine = usePrivateTagLine;
	}
	
	public void setVerticalResolution(int verticalResolution)
	{
		this.verticalResolution = verticalResolution;
	}
	
	public enum State { 
		BLOCKED,
		DONE,
		FAILED,
		PENDING,
		RUNNING,
		SLEEPING,
		SUSPENDED,
		UNDEFINED,
		WAITING,
	}

	public enum JobType { 
		FACSIMILE,
		PAGER,
	}
	
	public enum PageChopping {
		DISABLED,
		DEFAULT,
		ALL,
		LAST,
	}

}