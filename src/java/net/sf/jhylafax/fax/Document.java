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
 * Representation of a document.
 */
public class Document {

	private Date creationTime;
	private int deviceNumber;
	private String filename;
	private long filesize;
	private int groupID;
	private long inodeNumber;
	private Date lastAccessTime;
	private Date lastModificationTime;
	private int linkCount;
	private String owner;
	private int ownerID;
	private String permissions;
	private int rootDeviceNumber;	
		
	public Date getCreationTime()
	{
		return creationTime;
	}
	
	public int getDeviceNumber()
	{
		return deviceNumber;
	}

	public String getFilename()
	{
		return filename;
	}
	
	public long getFilesize()
	{
		return filesize;
	}
	
	public int getGroupID()
	{
		return groupID;
	}

	public long getInodeNumber()
	{
		return inodeNumber;
	}
	
	public Date getLastAccessTime()
	{
		return lastAccessTime;
	}
	
	public Date getLastModificationTime()
	{
		return lastModificationTime;
	}
	
	public int getLinkCount()
	{
		return linkCount;
	}
	
	public String getOwner()
	{
		return owner;
	}
	
	public int getOwnerID()
	{
		return ownerID;
	}
	
	public String getPermissions()
	{
		return permissions;
	}
	
	public int getRootDeviceNumber()
	{
		return rootDeviceNumber;
	}
	
	public void setCreationTime(Date creationTime)
	{
		this.creationTime = creationTime;
	}
	
	public void setDeviceNumber(int deviceNumber)
	{
		this.deviceNumber = deviceNumber;
	}
	
	public void setFilename(String filename)
	{
		this.filename = filename;
	}
	
	public void setFilesize(long filesize)
	{
		this.filesize = filesize;
	}
	
	public void setGroupID(int groupID)
	{
		this.groupID = groupID;
	}
	
	public void setInodeNumber(long inodeNumber)
	{
		this.inodeNumber = inodeNumber;
	}
	
	public void setLastAccessTime(Date lastAccessTime)
	{
		this.lastAccessTime = lastAccessTime;
	}
	
	public void setLastModificationTime(Date lastModificationTime)
	{
		this.lastModificationTime = lastModificationTime;
	}
	
	public void setLinkCount(int linkCount)
	{
		this.linkCount = linkCount;
	}
	
	public void setOwner(String owner)
	{
		this.owner = owner;
	}
	
	public void setOwnerID(int ownerID)
	{
		this.ownerID = ownerID;
	}
	
	public void setPermissions(String permissions)
	{
		this.permissions = permissions;
	}
	
	public void setRootDeviceNumber(int rootDeviceNumber)
	{
		this.rootDeviceNumber = rootDeviceNumber;
	}

}