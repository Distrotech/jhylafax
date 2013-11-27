package net.sf.jhylafax.fax;

import junit.framework.TestCase;


public class HylaFAXClientHelperTest extends TestCase {
	
	public void testParseDuration()
	{
		assertEquals(0, HylaFAXClientHelper.parseDuration(""));
		assertEquals(0, HylaFAXClientHelper.parseDuration("0"));
		assertEquals(1, HylaFAXClientHelper.parseDuration("1"));
		assertEquals(59, HylaFAXClientHelper.parseDuration("59"));
		assertEquals(60, HylaFAXClientHelper.parseDuration("1:00"));
		assertEquals(184, HylaFAXClientHelper.parseDuration("3:04"));
		assertEquals(3600, HylaFAXClientHelper.parseDuration("1:0:0"));
		assertEquals(3600, HylaFAXClientHelper.parseDuration("01:00:00"));
		assertEquals(7903, HylaFAXClientHelper.parseDuration("02:11:43"));
	}
}
