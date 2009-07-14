===============================================================================
Graph Foundation Classes for Java (GFC) - Version 1.1.2 - For JDK1.1 and above.
===============================================================================

===============================================================================
Changes (1.1.2)
===============================================================================

1. Several new classes in GFC core and GDF, e.g. SingleTree.
2. New methods in pre-existing classes, e.g. Walk.
3. Many more examples, especially drag-and-drop.

===============================================================================
Changes (1.1.1)
===============================================================================

1. Removed all dependencies on ibm.* packages.  All packages should be com.ibm.
2. Added the graph-canvas events support in package com.ibm.graph.awt.

===============================================================================
Files
===============================================================================

GFC1.1.2.zip    Class files (core GFC)
GDF1.1.2.zip    Class files (Graph Drawing Framework)
GLF1.1.2.zip    Class files (Graph Layout Framework)
GFCall1.1.2.zip Merge of the above three
API1.1.2.zip    Javadoc files
GEX1.1.2.zip    Examples with source code
DOC1.1.2.zip    Miscellaneous documents and faq

===============================================================================
Installation instructions
===============================================================================

There are three types of installation:

A. Core Graph Foundation Classes (core GFC) only.
B. GFC and Graph Drawing Framework (GDF) plug-in collection.
C. GFC, GDF, and Graph Layout Framework (GLF) plug-in collection.

Install GFC if you want to work with graphs as data structures only.

Install GFC and GDF if you want to display graphs.

Install GFC, GDF and GLF if you want to automatically layout and display graphs.


Install class files with unzipping:

1. Choose a directory into which the class containing zip files are downloaded,
say, C:\mydir\java\classes (Win)
or ~/mydir/java/classes (Unix).

2. Download zip files to that directory.
2a. Download GFC1.1.2.zip    (for installation A, B or C)
2b. Download GDF1.1.2.zip    (for installation B or C)
2c. Download GLF1.1.2.zip    (for installation C)
2x. Download GFCall1.1.2.zip (alternate installation)

3. Unzip files.
3a. Unzip: unzip GFC1.1.2.zip
3b. Unzip: unzip GDF1.1.2.zip (say yes to all replacements)
3c. Unzip: unzip GLF1.1.2.zip (say yes to all replacements)
3x. Unzip: unzip GFCall1.1.2.zip

4. Those zip files can be erased or removed.

5. Set CLASSPATH:
5a. Win:  set CLASSPATH=c:\mydir\java\classes;%CLASSPATH%
5b. Unix(ksh): export CLASSPATH=~/mydir/java/classes:$CLASSPATH

Install class files without unzipping:

1. Choose a directory into which the class containing zip files are downloaded,
say, C:\mydir\java\classes (Win)
or ~/mydir/java/classes (Unix).

2. Download zip files to that directory.
2a. Download GFC1.1.2.zip    (for installation A, B or C)
2b. Download GDF1.1.2.zip    (for installation B or C)
2c. Download GFCall1.1.2.zip (for installation C)

4. Set CLASSPATH:
4a. Win:  set CLASSPATH=c:\mydir\java\classes\GFC1.1.2.zip;%CLASSPATH%
4a. Unix(ksh): export CLASSPATH=~/mydir/java/classes/GFC1.1.2.zip:$CLASSPATH
4b. Win:  set CLASSPATH=c:\mydir\java\classes\GDF1.1.2.zip;c:\mydir\java\classes\GFC1.1.2;%CLASSPATH%
4b. Unix(ksh): export CLASSPATH=~/mydir/java/classes/GDF1.1.2.zip:~/mydir/java/classes/GFC1.1.2:$CLASSPATH
4c. Win:  set CLASSPATH=c:\mydir\java\classes\GFCall1.1.2.zip;%CLASSPATH%
4c. Unix(ksh): export CLASSPATH=~/mydir/java/classes/GFCall1.1.2.zip:$CLASSPATH

===============================================================================
Packages
===============================================================================

GFC packages:

  com.ibm.graph                 The GFC proper.
  com.ibm.research.util         Ancillary utilities for supporting the GFC.
  com.ibm.research.util.list    Ancillary data structure used by the GFC.
  ibm.graph.draw                (should disappear in future releases)

GDF packages:

  com.ibm.graph.awt             Convenience classes for building visual applications.
  com.ibm.graph.awt.event       Mouse events for graph canvases.
  com.ibm.graph.draw            A collection of drawable plug-ins.
  com.ibm.graph.event           (Work in progress.)
  com.ibm.graph.ext             An extension of the GFC that blends drawing capabilities.
  com.ibm.research.awt          Ancillary classes used by some drawable plug-ins.
  com.ibm.research.awt.draw     Ancillary classes used by some drawable plug-ins.
  com.ibm.research.geometry     Ancillary classes used by some drawable plug-ins.

GLF packages:

  com.ibm.graph.layout          A (currently small) collection of layout algorithms plug-ins.

===============================================================================
Install and view API documentation (the javadoc):
===============================================================================

1. Choose a directory into which the javadoc containing zip file is downloaded,
say, C:\mydir\api (Win) or ~/mydir/api (Unix).

2. Download API1.1.2.zip

3. Unzip: unzip API1.1.2.zip

4. API1.1.2.zip can be erased or removed.

5. View the API through a browser by opening
file:///c:/mydir/api/index.html (Win)
or
file:///~/mydir/api/index.html (Unix).


===============================================================================
Install and view examples:
===============================================================================

1. Choose a directory into which the source containing zip file is downloaded,
say, C:\mydir\examples (Win) or ~/mydir/examples (Unix).

2. Download GEX1.1.2.zip

3. Unzip: unzip GEX1.1.2.zip

4. GEX1.1.2.zip can be erased or removed.

Directory structure:

mydir/examples/GFC/...
mydir/examples/GDF/...
mydir/examples/GLF/...

===============================================================================
Programming notes
===============================================================================

GDF

For the time being, stick to the basic drawing interfaces:
Drawable and Drawable2.  Other drawing interfaces are
speculative at present.

Vertex drawables with the prefix "Draw3Vertex" can be used
at any level of the framework and are therefore recommended.
Some of those drawables are work-in-progress.


===============================================================================
Discussion
===============================================================================

http://www.alphaworks.ibm.com/tech/gfc
Then click on Discussion

===============================================================================
Contact
===============================================================================

Christian Lenz Cesar, cesar@us.ibm.com, cesar@watson.ibm.com

IBM T.J.Watson Research Center
P.O.Box 704
Yorktown Heights, NY 10588-0704
(914)784-7669

