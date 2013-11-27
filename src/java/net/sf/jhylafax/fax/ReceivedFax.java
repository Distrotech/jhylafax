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
 * Representation of a received fax.
 */
public class ReceivedFax {

    private String callerIDName;
    private String callerIDNumber;
    private String dataFormat;
	private String directory;
	private String filename;
	private long filesize; // bytes
	private String lastError;
	private String owner;
	private int pageCount;
	private int pageLength; // mm
	private int pageWidth; // mm
	private int protectionMode;
	private Date receivedTime;
	private boolean receiving;
	private int resolution;
	private String sender;
	private int signallingRate;
	private String subAddress;
	private int timeSpent; // seconds
	
	public String getCallerIDName()
	{
		return callerIDName;
	}
	
	public String getCallerIDNumber()
	{
		return callerIDNumber;
	}
	
	public String getDataFormat()
	{
		return dataFormat;
	}
	
	public String getDirectory()
	{
		return directory;
	}
	
	public String getFilename()
	{
		return filename;
	}
	
	public long getFilesize()
	{
		return filesize;
	}
	
	public String getLastError()
	{
		return lastError;
	}
	
	public String getOwner()
	{
		return owner;
	}
	
	public int getPageCount()
	{
		return pageCount;
	}
	
	public int getPageLength()
	{
		return pageLength;
	}
	
	public int getPageWidth()
	{
		return pageWidth;
	}
	
	public int getProtectionMode()
	{
		return protectionMode;
	}
	
	public Date getReceivedTime()
	{
		return receivedTime;
	}
	
	public int getResolution()
	{
		return resolution;
	}
	
	public String getSender()
	{
		return sender;
	}
	
	public int getSignallingRate()
	{
		return signallingRate;
	}
	
	public String getSubAddress()
	{
		return subAddress;
	}
	
	public int getTimeSpent()
	{
		return timeSpent;
	}
	
	public boolean isReceiving()
	{
		return receiving;
	}
	
	public void setCallerIDName(String callerIDName)
	{
		this.callerIDName = callerIDName;
	}
	
	public void setCallerIDNumber(String callerIDNumber)
	{
		this.callerIDNumber = callerIDNumber;
	}
	
	public void setDataFormat(String dataFormat)
	{
		this.dataFormat = dataFormat;
	}
	
	public void setDirectory(String directory)
	{
		this.directory = directory;
	}
	
	public void setFilename(String filename)
	{
		this.filename = filename;
	}
	
	public void setFilesize(long filesize)
	{
		this.filesize = filesize;
	}
	
	public void setLastError(String lastError)
	{
		this.lastError = lastError;
	}
	
	public void setOwner(String owner)
	{
		this.owner = owner;
	}
	
	public void setPageCount(int pageCount)
	{
		this.pageCount = pageCount;
	}
	
	public void setPageLength(int pageLength)
	{
		this.pageLength = pageLength;
	}
	
	public void setPageWidth(int pageWidth)
	{
		this.pageWidth = pageWidth;
	}
	
	public void setProtectionMode(int protectionMode)
	{
		this.protectionMode = protectionMode;
	}
	
	public void setReceivedTime(Date receivedTime)
	{
		this.receivedTime = receivedTime;
	}
	
	public void setReceiving(boolean receiving)
	{
		this.receiving = receiving;
	}
	
	public void setResolution(int resolution)
	{
		this.resolution = resolution;
	}
	
	public void setSender(String sender)
	{
		this.sender = sender;
	}

	public void setSignallingRate(int signallingRate)
	{
		this.signallingRate = signallingRate;
	}

	public void setSubAddress(String subAddress)
	{
		this.subAddress = subAddress;
	}
	
	public void setTimeSpent(int timeSpent)
	{
		this.timeSpent = timeSpent;
	}

}