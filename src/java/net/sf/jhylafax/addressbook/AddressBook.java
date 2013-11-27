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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import net.sf.jhylafax.JHylaFAX;
import net.sf.jhylafax.LocaleChangeListener;
import net.sf.jhylafax.Settings;
import net.wimpi.pim.Pim;
import net.wimpi.pim.contact.db.ContactDatabase;
import net.wimpi.pim.contact.db.ContactGroup;
import net.wimpi.pim.contact.facades.SimpleContact;
import net.wimpi.pim.contact.io.ContactMarshaller;
import net.wimpi.pim.contact.io.ContactUnmarshaller;
import net.wimpi.pim.contact.model.Contact;
import net.wimpi.pim.factory.ContactIOFactory;
import net.wimpi.pim.factory.ContactModelFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xnap.commons.gui.Builder;
import org.xnap.commons.gui.ColoredTable;
import org.xnap.commons.gui.Dialogs;
import org.xnap.commons.gui.EraseTextFieldAction;
import org.xnap.commons.gui.ErrorDialog;
import org.xnap.commons.gui.ToolBarButton;
import org.xnap.commons.gui.action.AbstractXNapAction;
import org.xnap.commons.gui.table.StringCellRenderer;
import org.xnap.commons.gui.table.TableLayoutManager;
import org.xnap.commons.gui.table.TableSorter;
import org.xnap.commons.gui.util.DoubleClickListener;
import org.xnap.commons.gui.util.GUIHelper;
import org.xnap.commons.gui.util.IconHelper;
import org.xnap.commons.gui.util.PopupListener;
import org.xnap.commons.io.FileExtensionFilter;
import org.xnap.commons.settings.SettingStore;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class AddressBook extends JFrame implements ListSelectionListener,
	LocaleChangeListener {

	private final static Log logger = LogFactory.getLog(AddressBook.class);
	private static final String[] DEFAULT_CONTACT_TABLE_COLUMNS 
		= new String[] { "displayName", "company", "faxNumber" };

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		final AddressBook app = new AddressBook();
		app.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		app.restoreLayout(new SettingStore(Settings.backstore));
		
		try {
			File file = JHylaFAX.getAddressBookFile();
			if (file.exists()) {
				app.load(file);
			}
		}
		catch (Exception e) {
			ErrorDialog.showError(null, 
					i18n.tr("Could not load address book"), 
					i18n.tr("JHylaFAX Error"), e);					
		}

		app.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				try {
					File file = JHylaFAX.getAddressBookFile();
					app.store(file);
				}
				catch (Exception e) {
					ErrorDialog.showError(null, 
							i18n.tr("Could not store address book"), 
							i18n.tr("JHylaFAX Error"), e);					
				}
			}
		});

		app.setVisible(true);
	}
	
	private JMenu addressBookMenu;
	private JTree addressBookTree;
	private DefaultTreeModel addressBookTreeModel;
	private CloseAction closeAction;
	private JTable contactTable;
	private AddressTableModel contactTableModel;
	private DeleteAction deleteAction;
	private Action doubleClickAction;
	private EditAction editAction;
	private ExportAction exportAction;
	private FilterAction filterAction;
	private ImportAction importAction;
	private ContactCollection localAddressBook;
	private JToolBar mainToolBar;
	private NewAction newAction;
	private DefaultMutableTreeNode rootNode;
	private JTextField searchTextField;
	private JSplitPane horizontalSplitPane;
	private JLabel filterLabel;
	private TableLayoutManager contactTableLayoutManager;
	
	public AddressBook() {
		ContactModelFactory cmf = Pim.getContactModelFactory();

		JHylaFAX.initializeToolkit();
		initialize();
		
		ContactDatabase contactDatabase = Pim.getContactDBFactory().createContactDatabase();
		
		// initialize tree content
		DefaultMutableTreeNode localAddressBookTreeNode = new DefaultMutableTreeNode();
		localAddressBook = new ContactCollection(contactDatabase, i18n.tr("Local Address Book"));
		localAddressBookTreeNode.setUserObject(localAddressBook);
		rootNode.add(localAddressBookTreeNode);
		addressBookTree.expandPath(new TreePath(rootNode));
		addressBookTree.setSelectionRow(0);
		
		updateActions();
	}

	public void exportContacts(File file, SimpleContact[] contacts) throws IOException
	{
		FileOutputStream out = new FileOutputStream(file);
		try {
			ContactIOFactory factory = Pim.getContactIOFactory();
		    ContactMarshaller marshaller = factory.createContactMarshaller();
		    marshaller.setEncoding("UTF-8");
		    for (SimpleContact contact : contacts) {
			    marshaller.marshallContact(out, contact.getContact());	
			}
		}
		finally {
			out.close();
		}
	}

	public SimpleContact[] getAlllContacts()
	{
		TableSorter sorter = (TableSorter)contactTable.getModel();
		SimpleContact[] result = new SimpleContact[sorter.getRowCount()];
		for (int i = 0; i < result.length; i++) {
			int row = sorter.mapToIndex(i);
			result[i] = contactTableModel.getContact(row); 
		}
		return result;
	}

	public ContactCollection getSelectedContactCollection()
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)addressBookTree.getSelectionPath().getLastPathComponent();
		return (ContactCollection)node.getUserObject();
	}

	public SimpleContact[] getSelectedContacts()
	{
		int[] rows = contactTable.getSelectedRows();
		if (rows.length == 0) {
			return new SimpleContact[0];
		}
		SimpleContact[] result = new SimpleContact[rows.length];
		for (int i = 0; i < rows.length; i++) {
			int row = ((TableSorter)contactTable.getModel()).mapToIndex(rows[i]);
			result[i] = contactTableModel.getContact(row); 
		}
		return result;
	}

	public SimpleContact[] importVCardContacts(File file) throws IOException
	{
		FileInputStream in = new FileInputStream(file);
		try {
			ContactIOFactory factory = Pim.getContactIOFactory();
		    ContactUnmarshaller unmarshaller = factory.createContactUnmarshaller();
		    unmarshaller.setEncoding("UTF-8");
		    Contact[] contacts = unmarshaller.unmarshallContacts(in);
		    if (contacts != null) {
			    SimpleContact[] result = new SimpleContact[contacts.length];
			    for (int i = 0; i < contacts.length; i++) {
			    	SimpleContact contact = new SimpleContact(contacts[i]);
				    getSelectedContactCollection().add(contact);
				    result[i] = contact;
				}
			    return result;
		    }
		    return new SimpleContact[0];
		}
		finally {
			in.close();
		}
	}

	public SimpleContact[] importCSVContacts(File file, String separator) throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(file));
		try {
			List<SimpleContact> contacts = new ArrayList<SimpleContact>();
			String line;
			while ((line = in.readLine()) != null) {
				SimpleContact contact = new SimpleContact();
				StringTokenizer t = new StringTokenizer(line, separator);
				if (t.hasMoreTokens()) contact.setFirstname(t.nextToken());
				if (t.hasMoreTokens()) contact.setLastname(t.nextToken());
				if (t.hasMoreTokens()) contact.setFaxNumber(t.nextToken());
				if (t.hasMoreTokens()) contact.setCompany(t.nextToken());
			    getSelectedContactCollection().add(contact);
			    contacts.add(contact);
			}
			return contacts.toArray(new SimpleContact[0]);
		}
		finally {
			in.close();
		}
	}

	private void initialize() {
		initializeActions();
		initializeShortCuts();
		initializeMenuBar();
		initializeContent();
		initializeToolBar();
		
		updateLabels();		
	}

	private void initializeActions()
	{
		newAction = new NewAction();
		editAction = new EditAction();
		deleteAction = new DeleteAction();
		importAction = new ImportAction();
		exportAction = new ExportAction();
		closeAction = new CloseAction();
		filterAction = new FilterAction();
	}

	private void initializeContent()
	{
		horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		horizontalSplitPane.setBorder(GUIHelper.createEmptyBorder(5));
		
		rootNode = new DefaultMutableTreeNode();
		addressBookTreeModel = new DefaultTreeModel(rootNode);
		addressBookTree = new JTree(addressBookTreeModel);
		addressBookTree.setRootVisible(false);
		addressBookTree.setCellRenderer(new ContactCollectionCellRenderer());
		horizontalSplitPane.add(new JScrollPane(addressBookTree));
		
		JPanel contactPanel = new JPanel();
		contactPanel.setLayout(new BorderLayout(0, 10));
		horizontalSplitPane.add(contactPanel);
		
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("min, 3dlu, min, 3dlu, pref:grow, 3dlu, min", ""));
		contactPanel.add(builder.getPanel(), BorderLayout.NORTH);
		
		searchTextField = new JTextField(10);
		EraseTextFieldAction eraseAction = new EraseTextFieldAction(searchTextField) {
			public void actionPerformed(ActionEvent event) {
				super.actionPerformed(event);
				filterAction.actionPerformed(event);
			};
		};
		builder.append(new TabTitleButton(eraseAction));
		filterLabel = new JLabel();
		builder.append(filterLabel);
		builder.append(searchTextField);
		GUIHelper.bindEnterKey(searchTextField, filterAction);
		
		builder.append(Builder.createButton(filterAction));
		
		JPopupMenu tablePopupMenu = new JPopupMenu();
		tablePopupMenu.add(Builder.createMenuItem(newAction));
		tablePopupMenu.addSeparator();		
		tablePopupMenu.add(Builder.createMenuItem(editAction));
		tablePopupMenu.addSeparator();		
		tablePopupMenu.add(Builder.createMenuItem(deleteAction));

		contactTableModel = new AddressTableModel();
		TableSorter sorter = new TableSorter(contactTableModel);
		contactTable = new ColoredTable(sorter);
		contactTableLayoutManager = new TableLayoutManager(contactTable);
		contactTableLayoutManager.addColumnProperties("displayName", "", 180, true);
		contactTableLayoutManager.addColumnProperties("company", "", 80, true);
		contactTableLayoutManager.addColumnProperties("faxNumber", "", 60, true);
		contactTableLayoutManager.initializeTableLayout();
		contactPanel.add(new JScrollPane(contactTable), BorderLayout.CENTER);

		contactTable.setShowVerticalLines(true);
		contactTable.setShowHorizontalLines(false);
		contactTable.setAutoCreateColumnsFromModel(true);
		contactTable.setIntercellSpacing(new java.awt.Dimension(2, 1));
		contactTable.setBounds(0, 0, 50, 50);
		contactTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		contactTable.getSelectionModel().addListSelectionListener(this);
		contactTable.addMouseListener(new PopupListener(tablePopupMenu));
		contactTable.addMouseListener(new DoubleClickListener(new TableDoubleClickAction()));
		contactTable.setTransferHandler(new ContactTransferHandler());
		contactTable.setDragEnabled(true);
		
		contactTable.setDefaultRenderer(String.class, new StringCellRenderer());
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(horizontalSplitPane, BorderLayout.CENTER);
	}
	
	private void initializeMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		addressBookMenu = new JMenu();
		menuBar.add(addressBookMenu);
		addressBookMenu.add(Builder.createMenuItem(newAction));
		addressBookMenu.addSeparator();
		addressBookMenu.add(Builder.createMenuItem(editAction));
		addressBookMenu.addSeparator();
		addressBookMenu.add(Builder.createMenuItem(deleteAction));
		addressBookMenu.addSeparator();
		addressBookMenu.add(Builder.createMenuItem(importAction));
		addressBookMenu.add(Builder.createMenuItem(exportAction));
		addressBookMenu.addSeparator();
		addressBookMenu.add(Builder.createMenuItem(closeAction));
	}

	private void initializeShortCuts()
	{
	}

	private void initializeToolBar()
	{
		mainToolBar = new JToolBar();
		//mainToolBar.setBorderPainted(false);
		//mainToolBar.setRollover(true);
		getContentPane().add(mainToolBar, BorderLayout.NORTH);
		
		mainToolBar.add(Builder.createToolBarButton(newAction));
		mainToolBar.addSeparator();
		mainToolBar.add(Builder.createToolBarButton(editAction));
		mainToolBar.add(Builder.createToolBarButton(deleteAction));
	}
	
	public void load(File file) throws IOException, ClassNotFoundException
	{
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		try {
			ContactDatabase contactDatabase = (ContactDatabase)in.readObject();
			localAddressBook.setDatabase(contactDatabase);
		}
		finally {
			in.close();
		}
	}

	public void saveLayout(SettingStore store) {
		contactTableLayoutManager.saveLayout(store, "addressbook.contact");
		store.saveWindow("addressbook.window", this);
		store.saveSplitPane("addressbook.horizontalSplit", horizontalSplitPane);
	}
	
	public void restoreLayout(SettingStore store) {
		contactTableLayoutManager.restoreLayout(store, "addressbook.contact");
		store.restoreWindow("addressbook.window", 40, 40, 550, 300, this);
		store.restoreSplitPane("addressbook.horizontalSplit", 150, horizontalSplitPane);
	}

	public void setDoubleClickAction(Action doubleClickAction)
	{
		this.doubleClickAction = doubleClickAction;
	}
	
    public void store(File file) throws IOException
	{
    	if (!localAddressBook.isChanged()) {
    		logger.info("Address book unchanged");
    		return;
    	}

    	ObjectOutputStream in = new ObjectOutputStream(new FileOutputStream(file));
    	try {
    		in.writeObject(localAddressBook.getDatabase());
    		logger.info("Stored address book in " + file.getAbsolutePath());
    	}
    	finally {
    		in.close();
    	}
	}

	public void updateLabels() {
		setTitle(i18n.tr("JHylaFAX Address Book"));
		
		addressBookMenu.setText(i18n.tr("Address Book"));
		
		newAction.putValue(Action.NAME, i18n.tr("New Contact..."));
		editAction.putValue(Action.NAME, i18n.tr("Edit Contact..."));
		deleteAction.putValue(Action.NAME, i18n.tr("Delete Contact"));
		
		importAction.putValue(Action.NAME, i18n.tr("Import..."));
		exportAction.putValue(Action.NAME, i18n.tr("Export..."));
		
		closeAction.putValue(Action.NAME, i18n.tr("Close"));
		
		filterLabel.setText(i18n.tr("Search for"));
		filterAction.putValue(Action.NAME, i18n.tr("Go"));
		
		contactTableLayoutManager.getTableLayout().setColumnNames(new String[] {
				i18n.tr("Name"),
				i18n.tr("Company"), 
				i18n.tr("Fax"),});
		
		GUIHelper.setMnemonics(getJMenuBar());
	}

	public void valueChanged(ListSelectionEvent e) {
		updateActions();
	}

	public void updateActions() {
		boolean selected = (contactTable.getSelectedRow() != -1);
		editAction.setEnabled(selected);
		deleteAction.setEnabled(selected);
	}
	
	private static class AddressTableModel extends AbstractTableModel {

		private static final Class[] columnClasses= {
			String.class, 
			String.class,
			String.class,
		};
		
		private List<SimpleContact> data = new ArrayList<SimpleContact>();
		
		public AddressTableModel() 
		{
		}
		
		public void add(SimpleContact contact)
		{
			data.add(contact);
			fireTableRowsInserted(data.size() - 1, data.size() - 1);
		}
		
		public Class<?> getColumnClass(int column) {
	        return columnClasses[column];
	    }
	
		public int getColumnCount()
		{
			return columnClasses.length;
		}

		public SimpleContact getContact(int row) {
			return data.get(row);
		}

		public int getRowCount()
		{
			return data.size();
		}
		
	    public Object getValueAt(int row, int column)
		{
			SimpleContact contact = data.get(row);
			switch (column) {
			case 0:
				return contact.getFirstname() + " " + contact.getLastname();
			case 1:
				return contact.getCompany();
			case 2:
				return contact.getFaxNumber();
			default:
				return null;
			}
		}
		
		public int indexOf(SimpleContact contact)
		{
			for (int i = 0; i < data.size(); i++) {
				if (data.get(i).getContact().equals(contact.getContact())) {
					return i;
				}
			}
			return -1;
		}

		public void remove(SimpleContact contact)
		{
			// XXX work around broken SimpleContant.equals() method
			int i = indexOf(contact);
			if (i != -1) {
				data.remove(i);
				fireTableRowsDeleted(i, i);
			}
		}
		
		public void setData(List<SimpleContact> data)
		{
			this.data = data;
			fireTableDataChanged();
		}

		public void update(SimpleContact contact)
		{
			// XXX work around broken SimpleContant.equals() method
			int i = indexOf(contact);
			if (i != -1) {
				fireTableRowsUpdated(i, i);
			}
		}

	}

	private class CloseAction extends AbstractXNapAction {
		
		public CloseAction() {
		}

		public void actionPerformed(ActionEvent event)
		{
			AddressBook.this.setVisible(false);
		}		
	}

	private class ContactCollection
	{
		
		private ContactDatabase database;
		private String filterText;
		private ContactGroup group;
		private String name;
		private ContactCollection parent;
		private boolean changed;
		
		public ContactCollection(ContactCollection parent, ContactGroup group)
		{
			this.parent = parent;
			this.group = group;
		}
		
		public ContactCollection(ContactDatabase database, String name)
		{
			this.database = database;
			this.name = name;
		}

		public void add(SimpleContact contact)
		{
			getDatabase().getContactCollection().add(contact.getContact());
			if (group != null) {
				group.addContact(contact.getContact());
			}
			if (matches(contact)) {
				contactTableModel.add(contact);
			}
			changed = true;
		}
		
		public void changed(SimpleContact contact)
		{
			if (!matches(contact)) {
				contactTableModel.remove(contact);
			}
			else {
				contactTableModel.update(contact);
			}
			changed = true;
		}
		
		public ContactDatabase getDatabase()
		{
			return (parent != null) ? parent.getDatabase() : database;
		}

		public ContactGroup getGroup()
		{
			return group;
		}
		
		public boolean isChanged()
		{
			return changed;
		}
		
		private boolean matches(SimpleContact contact)
		{
			if (filterText == null || filterText.length() == 0) {
				return true;
			}
			return contact.getFirstname().toLowerCase().contains(filterText)
				|| contact.getLastname().toLowerCase().contains(filterText)
				|| contact.getCompany().toLowerCase().contains(filterText)
				|| contact.getFaxNumber().toLowerCase().contains(filterText);
		}
		
		public void remove(SimpleContact contact)
		{
			getDatabase().getContactCollection().remove(contact.getContact());
			if (group != null) {
				group.removeContact(contact.getContact());
			}
			if (matches(contact)) {
				contactTableModel.remove(contact);
			}
			changed = true;
		}
		
		public void resync()
		{
			Contact[] contacts = (group != null) 
				? group.listContacts()
				: database.getContactCollection().toArray();
			List<SimpleContact> data = new ArrayList<SimpleContact>(contacts.length);
			for (int i = 0; i < contacts.length; i++) {
				SimpleContact contact = new SimpleContact(contacts[i]);
				if (matches(contact)) {
					data.add(contact);
				}
			}
			contactTableModel.setData(data);
			valueChanged(null);
		}
	
		public void setDatabase(ContactDatabase database)
		{
			this.database = database;
			changed = false;
			resync();
		}
		
		public void setFilterText(String filterText)
		{
			this.filterText = filterText.toLowerCase();
			resync();
		}

		@Override
		public String toString()
		{
			return (group != null) ? group.getName() : name;
		}
		
	}

	private static class ContactCollectionCellRenderer extends DefaultTreeCellRenderer {
		Icon bookIcon = IconHelper.getTreeIcon("contents.png");
		Icon groupIcon = IconHelper.getTreeIcon("kdmconfig.png");
		
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
					row, hasFocus);

			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
			if (node.getUserObject() instanceof ContactCollection) {
				ContactCollection collection = (ContactCollection)node.getUserObject();
				setIcon((collection.getGroup() != null) ? groupIcon : bookIcon);
			}
			
			return this;
		}
	}

	private class DeleteAction extends AbstractXNapAction {
		
		public DeleteAction() {
			putValue(ICON_FILENAME, "editdelete.png");
		}

		public void actionPerformed(ActionEvent e)
		{
			SimpleContact[] contacts = getSelectedContacts();
			if (Dialogs.showConfirmDialog(AddressBook.this, 
					i18n.tr("Do you really want to delete the selected contacts?"),
					i18n.tr("Delete Contacts"), 
					JOptionPane.YES_NO_OPTION, 
					Settings.CONFIRM_DELETE) == JOptionPane.YES_OPTION) {
				for (int i = 0; i < contacts.length; i++) {
					getSelectedContactCollection().remove(contacts[i]);
				}
			}
		}
		
	}

	private class EditAction extends AbstractXNapAction {
		
		public EditAction() {
			putValue(ICON_FILENAME, "edit.png");
		}

		public void actionPerformed(ActionEvent e)
		{
			SimpleContact[] contact = getSelectedContacts();
			EditContactDialog dialog = new EditContactDialog(AddressBook.this, contact[0]);
			dialog.setModal(true);
			dialog.setLocationRelativeTo(AddressBook.this);
			dialog.setVisible(true);
			if (dialog.isOkay()) {
				getSelectedContactCollection().changed(contact[0]);
			}
		}
		
	}
	
	private class ExportAction extends AbstractXNapAction {
		
		public ExportAction() {
			putValue(ICON_FILENAME, "fileexport.png");
		}

		public void actionPerformed(ActionEvent event)
		{
			if (contactTableModel.getRowCount() == 0) {
				Dialogs.showInfo(AddressBook.this, i18n.tr("No data to export"), i18n.tr("JHylaFAX Addressbook Error"));
			}
			
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter(new FileExtensionFilter(i18n.tr("vCards (*.vcf)"), ".vcf"));
			if (fileChooser.showSaveDialog(AddressBook.this) == JFileChooser.APPROVE_OPTION) {
				try {
					SimpleContact[] contacts = getSelectedContacts();
					if (contacts.length == 0) {
						contacts = getAlllContacts();
					}
					exportContacts(fileChooser.getSelectedFile(), contacts);
					Dialogs.showInfo(AddressBook.this, 
							i18n.tr("Exported {0} contacts", contacts.length), 
							i18n.tr("JHylaFAX Address Book"));
				}
				catch (Exception e) {
					ErrorDialog.showError(AddressBook.this, 
							i18n.tr("Could not export to file \"{0}\""), 
							i18n.tr("JHylaFAX Error"), e);					
				}
			}
		}		
	}

	private class FilterAction extends AbstractXNapAction {
		
		public FilterAction() {
			putValue(ICON_FILENAME, "filter.png");
		}

		public void actionPerformed(ActionEvent e)
		{
			getSelectedContactCollection().setFilterText(searchTextField.getText());
		}
		
	}

	private class ImportAction extends AbstractXNapAction {
		
		public ImportAction() {
			putValue(ICON_FILENAME, "fileimport.png");
		}
/*
		public void actionPerformed1(ActionEvent event)
		{
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter(new FileExtensionFilter(I18n.tr("vCards (*.vcf)"), ".vcf"));
			if (fileChooser.showOpenDialog(AddressBook.this) == JFileChooser.APPROVE_OPTION) {
				try {
					SimpleContact[] contacts = importContacts(fileChooser.getSelectedFile());
					Dialogs.showInfo(AddressBook.this, 
							I18n.tr("Imported {0} contacts", contacts.length), 
							I18n.tr("JHylaFAX Address Book"));

				}
				catch (Exception e) {
					ErrorDialog.showError(AddressBook.this, 
							I18n.tr("Could not import from file \"{0}\""), 
							I18n.tr("JHylaFAX Addressbook Error"), e);					
				}
			}
		}
*/
		public void actionPerformed(ActionEvent e)
		{
			ImportWizard wizard = new ImportWizard(AddressBook.this);
			wizard.setLocationRelativeTo(AddressBook.this);
			wizard.setVisible(true);
		}
		
	}

	private class NewAction extends AbstractXNapAction {
		
		public NewAction() {
			putValue(ICON_FILENAME, "filenew.png");
		}

		public void actionPerformed(ActionEvent e)
		{
			SimpleContact contact = new SimpleContact();
			EditContactDialog dialog = new EditContactDialog(AddressBook.this, contact);
			dialog.setModal(true);
			dialog.setLocationRelativeTo(AddressBook.this);
			dialog.setVisible(true);
			if (dialog.isOkay()) {
				getSelectedContactCollection().add(contact);
			}
		}
		
	}

	private class TableDoubleClickAction extends AbstractXNapAction {
		
		public TableDoubleClickAction() {
		}

		public void actionPerformed(ActionEvent event)
		{
			if (doubleClickAction == null) {
				if (editAction.isEnabled()) {
					editAction.actionPerformed(event);
				}
			}
			else if (doubleClickAction.isEnabled()){
				doubleClickAction.actionPerformed(event);
			}
		}
		
	}
	
	private class TabTitleButton extends ToolBarButton
    {
    	public TabTitleButton(Action action)
    	{
    		super(action);
    		String iconName = (String)action.getValue(AbstractXNapAction.ICON_FILENAME);
    		setIcon(IconHelper.getTabTitleIcon(iconName));
    		setMargin(new Insets(0, 0, 0, 0));
    	}
    }

	private class ContactTransferHandler extends AbstractContactTransferHandler {

		@Override
		protected Transferable createTransferable(JComponent c)
		{
			SimpleContact[] contacts = getSelectedContacts();
			if (contacts.length == 0) {
				return super.createTransferable(c);
			}
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ContactIOFactory factory = Pim.getContactIOFactory();
			ContactMarshaller marshaller = factory.createContactMarshaller();
			marshaller.setEncoding("UTF-8");
			for (SimpleContact contact : contacts) {
				marshaller.marshallContact(out, contact.getContact());	
			}
			return new ContactTransferable(new ByteArrayInputStream(out.toByteArray()));
		}

		@Override
		public void importData(Contact[] contacts)
		{
		    for (int i = 0; i < contacts.length; i++) {
		    	SimpleContact contact = new SimpleContact(contacts[i]);
			    getSelectedContactCollection().add(contact);
			}
		}

	}

}
