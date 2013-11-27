package net.sf.jhylafax;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

class TimeCellRenderer extends DefaultTableCellRenderer
{

	private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	private DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
	private DateFormat tooltipFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);

	public TimeCellRenderer() {
		setHorizontalAlignment(SwingConstants.RIGHT);
    }

	protected void setValue(Object value) 
	{
		if (value == null) {
			super.setValue(null);
		}
		else {
			Date date = (Date)value;

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			Date today = cal.getTime();
			
			cal.setTimeInMillis(cal.getTimeInMillis() + 24 * 3600 * 1000);
			Date tomorrow = cal.getTime();
			
			if (date.after(today) && date.before(tomorrow)) {
				super.setValue(timeFormat.format(date));
			}
			else {
				super.setValue(dateFormat.format(date));
			}
			setToolTipText(tooltipFormat.format(date));
		}
	}
	
}