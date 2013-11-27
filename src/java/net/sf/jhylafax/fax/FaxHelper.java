package net.sf.jhylafax.fax;


public class FaxHelper {
	
	/**
	 * Returns the fax number in <code>receipient</code>. Accepts email address 
	 * like formatting:
	 * 
	 * <ul>
	 *  <li>Bar &lt;+49...&gt;</li>
	 *  <li>+49... (Bar)</li>
	 *  <li>+49...</li>
	 * </ul>
	 */
	public static String extractNumber(String receipient)
	{
		if (receipient.endsWith(">")) {	
			int left = receipient.indexOf('<');
			if (left != -1) {
				return receipient.substring(left + 1, receipient.length() - 1);
			}
		}
		else if (receipient.endsWith(")")) {
			int left = receipient.indexOf('(');
			if (left != -1) {
				return receipient.substring(0, left);
			}
		}
		return receipient;
	}
	
}
