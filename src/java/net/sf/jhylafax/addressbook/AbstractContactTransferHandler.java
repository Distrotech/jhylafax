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
import java.io.InputStream;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import net.wimpi.pim.Pim;
import net.wimpi.pim.contact.io.ContactUnmarshaller;
import net.wimpi.pim.contact.model.Contact;
import net.wimpi.pim.factory.ContactIOFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractContactTransferHandler extends TransferHandler {
	
	private final static Log logger = LogFactory.getLog(AbstractContactTransferHandler.class);
	
	public abstract void importData(Contact[] contacts);
	
	@Override
	public boolean importData(JComponent component, Transferable transferable)
	{
        if (canImport(component, transferable.getTransferDataFlavors())) {
            try {
    			ContactIOFactory factory = Pim.getContactIOFactory();
    		    ContactUnmarshaller unmarshaller = factory.createContactUnmarshaller();
    		    InputStream in = (InputStream)transferable.getTransferData(ContactTransferable.VCARD_FLAVOR);
    		    unmarshaller.setEncoding("UTF-8");
    		    Contact[] contacts = unmarshaller.unmarshallContacts(in);
    		    if (contacts != null && contacts.length > 0) {
    		    	importData(contacts);
    		    }
                return true;
            } 
            catch (Exception e) {
            	logger.debug("Error during import", e);
            } 
        }
        return false;
	}
	
	@Override
	public boolean canImport(JComponent component, DataFlavor[] transferFlavors)
	{
        if (transferFlavors == null) {
             return false;
        }

        for (int i = 0; i < transferFlavors.length; i++) {
            if (ContactTransferable.VCARD_FLAVOR.equals(transferFlavors[i])) {
                return true;
            }
        }
        return false;
	}
}