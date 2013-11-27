package net.sf.jhylafax;

import java.awt.AWTException;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xnap.commons.gui.util.IconHelper;

public class FaxTray {

	private final static Log logger = LogFactory.getLog(JHylaFAX.class);
	
	private TrayIcon trayIcon;

	private PopupMenu popup = new PopupMenu();

	private boolean supported = false;
	
	public FaxTray() {
		if (!SystemTray.isSupported()) {
			return;
		}

		ImageIcon icon = IconHelper.getSystemTrayIcon("kdeprintfax.png");
		if (icon == null) {
			logger.warn("Could not find icon for system tray");
			return;
		}

		SystemTray tray = SystemTray.getSystemTray();
		trayIcon = new TrayIcon(icon.getImage(), "JHylaFAX", popup);

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JHylaFAX.getInstance().setVisible(true);
			}
		};

		trayIcon.setImageAutoSize(true);
		trayIcon.addActionListener(actionListener);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			logger.warn("Could not add icon to system tray", e);
			return;			
		}
		
		supported = true;
	}
	
	public boolean isSupported() {
		return supported;
	}
	
	public PopupMenu getPopupMenu() {
		return popup;
	}

}
