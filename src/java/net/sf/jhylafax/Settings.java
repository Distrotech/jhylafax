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
package net.sf.jhylafax;

import static net.sf.jhylafax.JHylaFAX.i18n;
import gnu.hylafax.Job;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;
import net.sf.jhylafax.fax.Paper;
import org.xnap.commons.gui.completion.AutomaticDropDownCompletionMode;
import org.xnap.commons.settings.BooleanSetting;
import org.xnap.commons.settings.DefaultCompletionModeSetting;
import org.xnap.commons.settings.EnumSetting;
import org.xnap.commons.settings.IntSetting;
import org.xnap.commons.settings.PropertyResource;
import org.xnap.commons.settings.SerializableSetting;
import org.xnap.commons.settings.StringSetting;
import org.xnap.commons.util.PortRange;

/**
 * Manages the global settings.
 * 
 * @author Steffen Pingel
 */
public class Settings {

	public final static PropertyResource backstore = new PropertyResource();
	
	public final static StringSetting HOSTNAME = new StringSetting(backstore, "hostname", "localhost");
	public final static IntSetting PORT = new IntSetting(backstore, "port", 4559, PortRange.MIN_PORT, PortRange.MAX_PORT);
	public final static BooleanSetting USE_PASSIVE = new BooleanSetting(backstore, "usePassive", false);
	public final static BooleanSetting UPDATE_ON_STARTUP = new BooleanSetting(backstore, "updateOnStartup", false);
	public final static StringSetting USERNAME = new StringSetting(backstore, "username", System.getProperty("user.name", "").replaceAll(" ", ""));
	public final static StringSetting PASSWORD = new StringSetting(backstore, "password", "");
	public final static StringSetting FULLNAME = new StringSetting(backstore, "fullname", "");
	public final static StringSetting EMAIL = new StringSetting(backstore, "email", "");
	public final static BooleanSetting ADMIN_MODE = new BooleanSetting(backstore, "adminMode", false);
	public final static StringSetting ADMIN_PASSWORD = new StringSetting(backstore, "adminPassword", "");
	
	public final static EnumSetting<Paper> PAPER = new EnumSetting<Paper>(backstore, "paper", Paper.A4);
	public final static EnumSetting<Resolution> RESOLUTION = new EnumSetting<Resolution>(backstore, "resolution", Resolution.LOW);
	public final static EnumSetting<Notification> NOTIFICATION = new EnumSetting<Notification>(backstore, "notification", Notification.NEVER);
	public final static IntSetting PRIORITY = new IntSetting(backstore, "priority", 127, 0, 255);
	public final static IntSetting MAXDIALS = new IntSetting(backstore, "maxDials", 12, 1);
	public final static IntSetting MAXTRIES = new IntSetting(backstore, "maxTries", 3, 1);

	public final static BooleanSetting USE_INTERNAL_COVER = new BooleanSetting(backstore, "useInternalCover", true);
	public final static BooleanSetting SEND_COVER_AS_DOCUMENT = new BooleanSetting(backstore, "sendCoverAsDocument", false);
	public final static StringSetting COVER_PATH = new StringSetting(backstore, "coverPath", "");
	public final static StringSetting VIEWER_PATH = new StringSetting(backstore, "viewerPath", "");
	public final static StringSetting DOC_VIEWER_PATH = new StringSetting(backstore, "documentViewerPath", "");
	
	public final static BooleanSetting DO_MONITOR_PATH = new BooleanSetting(backstore, "doMonitorPath", false);
	public final static StringSetting MONITOR_PATH = new StringSetting(backstore, "monitorPath", "");
	public final static IntSetting MONITOR_PATH_INTERVAL = new IntSetting(backstore, "monitorPathInterval", 90, 1);

	public final static BooleanSetting DO_AUTO_UPDATE = new BooleanSetting(backstore, "doAutoUpdate", false);
	public final static IntSetting AUTO_UPDATE_INTERVAL = new IntSetting(backstore, "autoUpdateInterval", 180, 1);

	public final static BooleanSetting CONFIRM_NONPS = new BooleanSetting(backstore, "confirmNonPostScript", true);
	public final static BooleanSetting CONFIRM_DELETE = new BooleanSetting(backstore, "confirmDelete", true);
	public final static BooleanSetting SHOW_POLLQ = new BooleanSetting(backstore, "showPollq", false);
	
	public final static SerializableSetting<Locale> LOCALE = new SerializableSetting<Locale>(backstore, "locale", Locale.getDefault());
	public final static StringSetting TIMEZONE = new StringSetting(backstore, "timezone", TimeZone.getDefault().getID());
	public final static DefaultCompletionModeSetting DEFAULT_COMPLETION_MODE = new DefaultCompletionModeSetting(backstore, "defaultCompletionMode", AutomaticDropDownCompletionMode.class.getName());
		
	public final static BooleanSetting HAS_SEEN_WIZARD = new BooleanSetting(backstore, "hasSeenWizard", false);

	public final static BooleanSetting CUSTOMIZE_ADDRESS_BOOK_FILENAME = new BooleanSetting(backstore, "customizeAddressBookFilename", false);
	public final static StringSetting ADDRESS_BOOK_FILENAME = new StringSetting(backstore, "addressBookFilename", "addressbook.bin");
	
	public static void load(File file) throws IOException {
		backstore.load(file);
	}

	public static void store(File file) throws IOException {
		backstore.store(file);
	}

	public static enum Resolution {
		LOW(Job.RESOLUTION_LOW) { public String toString() { return i18n.tr("Normal"); } }, 
		MEDIUM(Job.RESOLUTION_MEDIUM) { public String toString() { return i18n.tr("Fine"); } },
		HIGH(Job.RESOLUTION_MEDIUM * 2) { public String toString() { return i18n.tr("Superfine"); } };
		
		private int linesPerInch;

		private Resolution(int linesPerInch) {
			this.linesPerInch = linesPerInch;
		}
		
		public int getLinesPerInch() {
			return linesPerInch;
		}
		
		public static Resolution getEnum(int linesPerInch) {
			for (Resolution value : values()) {
				if (linesPerInch == value.getLinesPerInch()) { 
					return value;				
				}
			}
			throw new IllegalArgumentException("Invalid value: " + linesPerInch);
		}
		
	}

	public static enum Notification {
		NEVER(Job.NOTIFY_NONE) { public String toString() { return i18n.tr("Errors only"); } },
		SEND(Job.NOTIFY_DONE) { public String toString() { return i18n.tr("After sending"); } },
		REQUEUE(Job.NOTIFY_REQUEUE) { public String toString() { return i18n.tr("After requeuing"); } }, 
		ALWAYS(Job.NOTIFY_ALL) { public String toString() { return i18n.tr("Always"); } };

		private String command;

		private Notification(String command) {
			this.command = command;
		}
		
		public String getCommand()
		{
			return command;
		}

		public static Notification getEnum(String command) {
			for (Notification value : values()) {
				if (value.getCommand().equals(command)) { 
					return value;				
				}
			}
			throw new IllegalArgumentException("Invalid value: " + command);
		}
	}

}
