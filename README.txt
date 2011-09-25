libomv-java
------------------

This is the Java port of the OpenMetaverse (http://www.OpenMetaverse.org) library originally written in C#.

The initial port was performed by Simon Whiteside (http://www.larts.co.uk).
Many modifications to the original Java port and additions from newer OpenMetaverse source code were done
by Frederick Martian.

Building the library and examples
---------------------------------

The library can be built using Eclipse.

You will need various existing Java libraries installed - more details to follow. Most non-standard Java
libraries are provided in the lib directory of this project download.

LindenLabs server certificate
-----------------------------

There seems to be issues with the SSL certificate used by the linden labs server, since it is not traceable
to any standard root certification agency. The current library will install a modified KeyStore with an added
lindenlab root certificate, loaded from the res directory, when attempting to open an https connection to
a server having "lindenlab" in its URI. 

Running examples
----------------

You can run examples directly from within Eclipse.

For example to run the "sldump" test application select the src-sample/libomv.test/sldump entry and select
Run as Java Application from the context menu.

Rebuilding packet classes
-------------------------

The UDP packet classes, which are used when communicating with the secondlife or OpenSim based servers,
are generated from the current protocol definition using an application called mapgenerator, which has
also been ported.

You can obtain the latest protocol definition files (called message_template.msg and keywords.txt) from
the libsecondlife site.

Put these files into the libsecondlife/mapgenerator/ directory, and rerun the code generator by right
clicking src/libomv.mapgenerator/mapgenerator and selecting Run as Java Application. Don't forget to
refresh the src/libomv.packets directory in the Eclipse Package Explorer pane before compiling the
library or opening one of the generated packet files in that directory.


Porting Information
-------------------

The initial port was performed by Simon Whiteside and taken pretty straight from the original C# code
as of October 26th 2006

Principal areas of porting were:

*) change "bit-twiddling" code into calls into Java ByteBuffer class
*) altered C# style networking code into Java networking code

This code has been provided and is still available on sourceforge in the libsecondlife-java project.

Another attempt to make this library more useful was performed by Frederick Martian, using the
libsecondlife-java code as a starting base and performing many modifications and additions, using
the OpenMetaverse 0.8.3 C# code as a guideline.

The current library has provisions to do XML-RPC and LLSD CAPS based logins, send and receive UDP
and CAPS messages, implement agent and avatar operations including display names support, instant
messaging, teleporting, friends and groups, as well as basic inventory, asset, object and parcel
management.

There is currently no image handling at all, and also no voice chat support. While image handling
will be required to implement proper baking (de-clouding) of the avatar, voice chat is not likely
to be added anytime soon, unless someone else provides an implementation of this.

