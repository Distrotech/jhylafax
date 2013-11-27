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

import static net.sf.jhylafax.JHylaFAX.i18n;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import net.wimpi.pim.contact.facades.SimpleContact;
import org.xnap.commons.gui.DefaultDialog;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class EditContactDialog extends DefaultDialog {

	private SimpleContact contact;
	private JTextField firstNameTextField;
	private JLabel firstNameLabel;
	private JLabel lastNameLabel;
	private JTextField lastNameTextField;
	private JTextField companyTextField;
	private JLabel companyLabel;
	private JTextField faxNumberTextField;
	private JLabel faxNumberLabel;

	public EditContactDialog(JFrame owner, SimpleContact contact) {
		super(owner);

		setApplyOnEnter(true);
		
		FormLayout layout = new FormLayout("left:max(40dlu;pref), 3dlu, pref:grow", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		setMainComponent(builder.getPanel());
		
		firstNameTextField = new JTextField(20);
		firstNameLabel = builder.append("", firstNameTextField);
		builder.nextLine();

		lastNameTextField = new JTextField(20);
		lastNameLabel = builder.append("", lastNameTextField);
		builder.nextLine();

		companyTextField = new JTextField(20);
		companyLabel = builder.append("", companyTextField);
		builder.nextLine();

		faxNumberTextField = new JTextField(20);
		faxNumberLabel = builder.append("", faxNumberTextField);
		builder.nextLine();

		setContact(contact);
		revert();
		
		updateLabels();
		pack();
	}
	
	public SimpleContact getContact()
	{
		return contact;
	}
	
	public void setContact(SimpleContact contact)
	{
		this.contact = contact;
	}

	public void revert()
	{
		firstNameTextField.setText(contact.getFirstname());
		lastNameTextField.setText(contact.getLastname());
		companyTextField.setText(contact.getCompany());
		faxNumberTextField.setText(contact.getFaxNumber());
	}

	@Override
	public boolean apply() {
		contact.setFirstname(firstNameTextField.getText());
		contact.setLastname(lastNameTextField.getText());
		contact.setCompany(companyTextField.getText());
		contact.setFaxNumber(faxNumberTextField.getText());
		return true;
	}

	public void updateLabels() {
		setTitle(i18n.tr("Edit Contact"));
		
		firstNameLabel.setText(i18n.tr("First Name"));
		lastNameLabel.setText(i18n.tr("Last Name"));
		companyLabel.setText(i18n.tr("Company"));
		faxNumberLabel.setText(i18n.tr("Fax"));
	}

}
