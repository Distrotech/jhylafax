package net.sf.jhylafax;

import static net.sf.jhylafax.JHylaFAX.i18n;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xnap.commons.gui.ErrorDialog;
import org.xnap.commons.gui.util.GUIHelper;
import org.xnap.commons.util.QuotedStringTokenizer;
import org.xnap.commons.util.StringHelper;

public class JHylaFAXHelper {

	private final static Log logger = LogFactory.getLog(JobHelper.class);
	
	public static void view(String viewerPath, File[] files) {
		for (int i = 0; i < files.length; i++) {
			List<String> args = new ArrayList<String>();
			QuotedStringTokenizer t = new QuotedStringTokenizer(viewerPath);
			boolean filenameAdded = false;
			while (t.hasMoreTokens()) {
				String token = t.nextToken();
				if ("%s".equals(token) || "%f".equals(token) || "$f".equals(token)) {
					args.add(files[i].getAbsolutePath());
					filenameAdded = true;
				}
				else {
					args.add(token);
				}
			}
			if (!filenameAdded) {
				args.add(files[i].getAbsolutePath());
			}
			if (!execute(args.toArray(new String[0]))) {
				return;
			}
		}
	}	
	
	public static boolean execute(String[] args) {
		try {
			Runtime.getRuntime().exec(args);
			return true;
		}
		catch (IOException e) {
			logger.debug("Error executing viewer: '" + StringHelper.toString(args, " ") + "'", e);
			ErrorDialog.showError(JHylaFAX.getInstance(), 
					i18n.tr("Could not execute viewer"),
					i18n.tr("JHylaFAX Error"),
					e);					
		}
		return false;
	}

	public static String getViewerPath(String filename) {
		// TODO should not hard code queuename here
		String viewerPath;
		if (filename.startsWith("recvq")) {
			viewerPath = Settings.VIEWER_PATH.getValue().trim();
		}
		else {
			viewerPath = Settings.DOC_VIEWER_PATH.getValue().trim();				
		}
		if (viewerPath.length() == 0) {
			JHylaFAX.getInstance().showError(GUIHelper.tt(i18n.tr("Could not open file: Please enter the path of an external viewer in the settings."))); 
			return null;
		}
		return viewerPath;
	}
	

}
