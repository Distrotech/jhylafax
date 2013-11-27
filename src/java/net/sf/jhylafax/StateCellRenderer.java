package net.sf.jhylafax;

import static net.sf.jhylafax.JHylaFAX.i18n;
import javax.swing.table.DefaultTableCellRenderer;
import net.sf.jhylafax.fax.FaxJob;
import org.xnap.commons.gui.util.IconHelper;

class StateCellRenderer extends DefaultTableCellRenderer
{

	public StateCellRenderer() {
    }

	protected void setValue(Object value) 
	{
		if (value == null) {
			setIcon(null);
			setToolTipText(null);
		}
		else {
			FaxJob.State state = (FaxJob.State)value;
			switch (state) {
			case BLOCKED:
				setIcon(IconHelper.getTableIcon("gear.png"));
				setToolTipText(i18n.tr("Blocked (by concurrent activity to the same destination)"));
				break;
			case WAITING:
				setIcon(IconHelper.getTableIcon("gear.png"));
				setToolTipText(i18n.tr("Waiting (for resources such as a free modem)"));
				break;
			case DONE:
				setIcon(IconHelper.getTableIcon("ok.png"));
				setToolTipText(i18n.tr("Done (successfully)"));
				break;
			case FAILED:
				setIcon(IconHelper.getTableIcon("flag.png"));
				setToolTipText(i18n.tr("Failed"));
				break;
			case PENDING:
				setIcon(IconHelper.getTableIcon("history.png"));
				setToolTipText(i18n.tr("Pending (waiting for its time to send to arrive)"));
				break;
			case RUNNING:
				setIcon(IconHelper.getTableIcon("launch.png"));
				setToolTipText(i18n.tr("Running"));
				break;
			case SLEEPING:
				setIcon(IconHelper.getTableIcon("history.png"));
				setToolTipText(i18n.tr("Sleeping (waiting for a scheduled timeout such as a delay between attempts to send)"));
				break;
			case SUSPENDED:
				setIcon(IconHelper.getTableIcon("player_pause.png"));
				setToolTipText(i18n.tr("Suspended (not being  scheduled)"));
				break;
			default: // UNDEFINED
				setIcon(null);
				setToolTipText(null);					
			}
		}
	}
	
}