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
/**
 * This file has been adopted for JHylaFAX. Original copyright notice below.
 */
/*
 * $Id: FaxCover.java,v 1.3 2009/04/19 02:10:08 squig Exp $
 *
 * Die Klasse FaxCovergen
 * (c) 1997 SuSE GmbH
 * Autor: Carsten Hoeger
 *
 * Diese Klasse erzeugt ein Faxcover aus dem Faxcover-Template
 *
 * Konstruktor:
 * public FaxCovergen(String cover, String doc, boolean debug)
 *
 * String  cover : Pfad auf das Covertemplate
 * String  doc   : Pfad auf das zu sendende Dokument
 * boolean debug : Debug Flag
 */
package net.sf.jhylafax.fax;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.jhylafax.JHylaFAX;

import org.xnap.commons.io.ProgressMonitor;

public class FaxCover {

	public static final String CHARSET = "ISO-8859-1";
	
	public String to = "";
	public String to_adress = "";
	public String to_company = "";
	public String to_location = "";
	public String to_voice_number = "";
	public String to_fax_number = "";
	public float pagewidth;
	public float pagelength;
	public String from = "";
	public String from_fax_number = "";
	public String from_voice_number = "";
	public String from_company = "";
	public String from_location = "";
	public String todays_date = "";
	public String regarding = "";
	public String comments = "";
	private boolean debug;
	private int page_count;
	private int npages;
	// EPS Bounding Box
	private int urx; // upper right
	private int ury;
	private List<String> docs = new ArrayList<String>();

	public FaxCover(Paper paper)
	{
		float width = 9920;
		float height = 14030;

		switch (paper) {
		case A4:
			width = 9920;
			height = 14030;
			break;
		case A3:
			width = 14030;
			height = 19840;
			break;
		case LETTER:
			width = 10200; // North American Letter
			height = 13200;
			break;
		}
		urx = (int)((width / 1200.0) * 72);
		ury = (int)((height / 1200.0) * 72);
		/* Pagesizes from hylafax-database 
		 #						Guaranteed Reproducible Area
		 # Name			Abbrev	Width	Height	Width	Height	Top	Left
		 ISO A3			A3	14030	19840	13200	 18480	472	345
		 ISO A4			A4	 9920	14030	 9240	 13200	472	345
		 ISO A5			A5	 7133	 9921	 6455	  9236	472	345
		 ISO A6			A6	 5055	 6991	 4575	  6508	472	345
		 ISO B4			B4	12048	17196	11325	 16010	472	345
		 North American Letter	NA-LET	10200	13200	 9240	 12400	472	345
		 American Legal		US-LEG	10200	16800	 9240	 15775	472	345
		 American Ledger		US-LED	13200	20400	11946	 19162	472	345
		 American Executive	US-EXE	 8700	12600	 7874	 11835	472	345
		 Japanese Letter		JP-LET	 8598	12141	 7600	 10200	900	400
		 Japanese Legal		JP-LEG	12141	17196	11200	 15300	900	400
		 */
	}

	public void addDocument(String filename) {
		docs.add(filename);
	}
	
	public StringBuffer generate(InputStream in, ProgressMonitor monitor) throws IOException
	{
		StringBuffer coverBuffer = new StringBuffer();
		
		// read in cover template
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, FaxCover.CHARSET));
		try {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("%%Page: ")) {
					page_count++;
				}
				coverBuffer.append(line);
				coverBuffer.append("\n");
			}
		}
		finally {
			reader.close();
		}
		
		// count pages of attached documents
		for (String filename : docs) {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			try {
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("%%Page: ")) {
						page_count++;
						npages++;
					}
				}
			}
			finally {
				reader.close();
			}
		}
		
		// prepend generated header to cover template that defines the 
		// template variables
		StringBuffer sb = createHeader();
		sb.append(coverBuffer);
		return sb;
	}

	private StringBuffer createHeader()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("%!PS-Adobe-2.0 EPSF-2.0\n"
				+ "%%Creator: JHylaFAX Version " + JHylaFAX.getVersion() + "\n"
				+ "%%Title: HylaFAX Cover Sheet\n"
				+ "%%CreationDate: "
				+ todays_date
				+ "\n"
				+ "%%Origin: 0 0\n"
				+ "%%BoundingBox: "
				+ 0
				+ " "
				+ 0
				+ " "
				+ urx
				+ " "
				+ ury
				+ "\n"
				+ "%%Pages: "
				+ page_count
				+ "\n"
				+ "%%EndComments\n"
				+ "%%BeginProlog\n"
				+ "100 dict begin\n"
				+ "/wordbreak ( ) def\n"
				+ "/linebreak (\\n) def\n"
				+ "/doLine {\n"
				+ "% <line> <width> <height> <x> <y> doLine <width> <height> <x> <y>\n"
				+ "2 copy moveto 5 -1 roll\n"
				+ "wordbreak\n"
				+ "{\n"
				+ "  search {\n"
				+ "      dup stringwidth pop currentpoint pop add 7 index 6 index add gt {\n"
				+ "          6 3 roll 2 index sub 2 copy moveto 6 3 roll\n"
				+ "      } if\n"
				+ "      show wordbreak show\n"
				+ "  }{\n"
				+ "      dup stringwidth pop currentpoint pop add 5 index 4 index add gt {\n"
				+ "          3 1 roll 3 index sub 2 copy moveto 3 -1 roll\n"
				+ "      } if\n" + "      show exit\n" + "  } ifelse\n"
				+ "} loop\n" + "2 index sub 2 copy moveto\n" + "} def\n"
				+ "/BreakIntoLines{\n"
				+ "% <width> <height> <x> <y> <text> BreakIntoLines\n"
				+ "linebreak\n" + "{\n" + "   search {\n"
				+ "       7 3 roll doLine 6 -2 roll\n" + "   }{\n"
				+ "       5 1 roll doLine exit\n" + "      } ifelse\n"
				+ "  } loop\n" + "pop pop pop pop\n" + "} def\n" + "/to (" + to
				+ ") def\n" + "/to-company (" + to_company + ") def\n"
				+ "/to-adress (" + to_adress + ") def\n" + "/to-Location ("
				+ to_location + ") def\n" + "/to-voice-number ("
				+ to_voice_number + ") def\n" + "/to-fax-number ("
				+ to_fax_number + ") def\n" + "/pageWidth " + pagewidth
				+ " def\n" + "/pageLength " + pagelength + " def\n" + "/from ("
				+ from + ") def\n" + "/from-fax-number (" + from_fax_number
				+ ") def\n" + "/from-voice-number (" + from_voice_number
				+ ") def\n" + "/from-company (" + from_company + ") def\n"
				+ "/from-Location (" + from_location + ") def\n"
				+ "/page-count (" + npages + ") def\n" + "/todays-date ("
				+ todays_date + ") def\n" + "/regarding (" + regarding
				+ ") def\n" + "/comments (" + emitCommentDefs(comments)
				+ ") def\n");
		sb.append("%%EndProlog\n");
		return sb;
	}

	private String emitCommentDefs(String comments)
	{
		int len = comments.length();
		StringBuffer buffer = new StringBuffer();
		buffer.setLength(2 * len);
		int n = 0;
		for (int i = 0; i < len; i++) {
			char cur = comments.charAt(i);
			if (cur == '\n') {
				buffer.setCharAt(n++, '\\');
				buffer.insert(n++, "\\n");
			}
			else {
				if (cur == '(' || cur == ')' || cur == '\\')
					buffer.insert(n++, '\\');
				buffer.setCharAt(n, cur);
			}
			n++;
		}
		buffer.setLength(n);
		return buffer.toString();
	}
	
}
