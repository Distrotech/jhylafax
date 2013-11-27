INTRODUCTION
------------

JHylaFax is a client for HylaFAX (http://www.hylafax.org). It is
complettly written in Java. 

It supports:

 * Faxing of documents in PostScript format
 * Polling of faxes
 * Automatic generation and preview of cover page
 * Editing of fax job parameters
 * Viewing of received faxes and stored documents
 * Addressbook that supports vCard 2.1 import and export
 * Language support: English, German, Spanish and Italian

Currently only documents in PostScript format are supported for
faxing. Received faxes are stored in TIFF format.

Bug reports and feedback is welcome! Please use the facilities at
http://jhylafax.sf.net.

Steffen Pingel <steffenp@gmx.de>


REQUIREMENTS
-------------

 * JRE 6.0 or higher
 * An image program to view faxes (like kview)
 * A running HylaFAX server (otherwise JHylaFAX is pretty useless)


INSTALL
-------

Download the jar file from http://jhylafax.sourceforge.net/ and run it
(on some platforms simply clicking the jar file will start the
program):

 java -jar jhylafax-x.y.z-app.jar

Replace x.y.z by the version you have downloaded.


KNOWN BUGS
----------

 * Cancel may not abort an operation right aways. It may take a while until
   the program will continue.
   
 * The "Accessed" and "Created" columns in the Documents table never show any 
   values.
   

BUILD
-----

If you would like to build JHylaFAX from source I recommend 

 * Apache Maven (http://maven.apache.org)

Extract the JHylaFAX source archive and change to the project
directory (the top level directory that contains the file
project.xml).

To build a full jar with all dependencies, run: 'mvn assembly:assembly'.

For releases:

 mvn clean
 mvn org.xnap.commons:maven-gettext-plugin:dist
 mvn assembly:assembly
 


LICENSE
-------

JHylaFax is free software, licenced under the GPL. See file
LICENSE.TXT for full details.


HISTORY
-------

JHylaFAX used to be based on SuSEFax by Carsten Hoeger but has been
rewritten. It still uses a few lines of the old code from SuSEFax.
