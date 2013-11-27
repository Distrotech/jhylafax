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
package net.sf.jhylafax.fax;

import static net.sf.jhylafax.JHylaFAX.i18n;

public enum Paper {
		
	A4(209, 296) { public String toString() { return i18n.tr("DIN A4"); } },
	A3(297, 420) { public String toString() { return i18n.tr("DIN A3"); } }, 
	LETTER(216, 279) { public String toString() { return i18n.tr("Letter"); } },
	LEGAL(216, 356)  { public String toString() { return i18n.tr("Legal"); } };
	
	private int width;
	private int height;
	
	private Paper(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}

	public static Paper getEnum(int width, int height) {
		for (Paper value : values()) {
			if (value.getWidth() == width && value.getHeight() == height) { 
				return value;				
			}
		}
		throw new IllegalArgumentException("Invalid values: " + width + ", " + height);
	}
	
}
