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

/**
 * Representation of a modem.
 */
public class Modem {
	
    private String hostname;
    private String localIdentifier;
    private String canonicalName;
    private String faxNumber;
    private int maxPagesPerCall;
    private String status;
    private int serverTracing;
    private int sessionTracing;
    private Volume speakerVolume;
    private boolean running;
	
    public enum Volume {
    	OFF,
    	LOW,
    	MEDIUM,
    	HIGH,
    }
    
	public String getCanonicalName()
	{
		return canonicalName;
	}
	
	public String getFaxNumber()
	{
		return faxNumber;
	}
	
	public String getHostname()
	{
		return hostname;
	}
	
	public String getLocalIdentifier()
	{
		return localIdentifier;
	}
	
	public int getMaxPagesPerCall()
	{
		return maxPagesPerCall;
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	public int getServerTracing()
	{
		return serverTracing;
	}
	
	public int getSessionTracing()
	{
		return sessionTracing;
	}
	
	public Volume getSpeakerVolume()
	{
		return speakerVolume;
	}
	
	public String getStatus()
	{
		return status;
	}
	
	public void setCanonicalName(String canonicalName)
	{
		this.canonicalName = canonicalName;
	}
	
	public void setFaxNumber(String faxNumber)
	{
		this.faxNumber = faxNumber;
	}
	
	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}
	
	public void setLocalIdentifier(String localIdentifier)
	{
		this.localIdentifier = localIdentifier;
	}
	
	public void setMaxPagesPerCall(int maxPagesPerCall)
	{
		this.maxPagesPerCall = maxPagesPerCall;
	}
	
	public void setRunning(boolean running)
	{
		this.running = running;
	}
	
	public void setServerTracing(int serverTracing)
	{
		this.serverTracing = serverTracing;
	}
	
	public void setSessionTracing(int sessionTracing)
	{
		this.sessionTracing = sessionTracing;
	}
	
	public void setSpeakerVolume(Volume speakerVolume)
	{
		this.speakerVolume = speakerVolume;
	}
	
	public void setStatus(String status)
	{
		this.status = status;
	}
    
}