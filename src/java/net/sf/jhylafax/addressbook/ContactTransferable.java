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
package net.sf.jhylafax.addressbook;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;

public class ContactTransferable implements Transferable
{
	public final static DataFlavor VCARD_FLAVOR 
		= new DataFlavor("text/x-vcard;class=java.io.InputStream", "vCard");
	//charset=UTF-8;
	
	private InputStream in;

	public ContactTransferable(InputStream in)
	{
		this.in = in;
	}
	
	public DataFlavor[] getTransferDataFlavors()
	{
		return new DataFlavor[] { VCARD_FLAVOR };
	}

	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return VCARD_FLAVOR.equals(flavor);
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		return in;
	}
	
}