libomv-gui
----------

This is the GUI component for the libomv-java project (http://libomv-java.sourceforge.net).

The initial port was performed by Simon Whiteside (http://www.larts.co.uk).

Many modifications to the original Java port and additions from newer OpenMetaverse source code were done
by Frederick Martian.


Building the GUI application
----------------------------

The application can be built using Eclipse.

It requires the libomv-core subproject of libomv-java either as source code project added to the classpath
or a compiled library. In addition all the classpath dependencies of libomv-core need to be satisfied too.

The application makes use of the teamdev.com JxBrowser component to display webcontent. The necessary
libraries and a runtime license for this library are contained in the lib directory for this subproject. 

