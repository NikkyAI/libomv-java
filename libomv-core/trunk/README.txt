libomv-java
------------------

This is the Java port of the OpenMetaverse (http://www.OpenMetaverse.org) library originally written in C#.

The initial port was performed by Simon Whiteside (http://www.larts.co.uk).

Many modifications to the original Java port and additions from newer OpenMetaverse source code were done
by Frederick Martian.


Installing the source code
--------------------------

The libomv-java project consists of several subprojects. There is the actual core library libomv-core
which provides the entire network and resource handling for SL network protocol, which is used by 
penSim and Secondlife servers.

This library makes use of various other libraries which are provided in the distribution as precompiled
libraries. One of them is a modified version of the jj2000 library. The modified source code of this
library is provided in the libomv-jj2k subproject. You don't necessarily need to download the source
code of this library, since the libomv-core subproject contains a precompiled binary archive of this
library in its classpath.

The libomv-gui subproject contains various gui components which use the awt/swing library. It will
provide a text chat client and various other GUI based tools, derived from the OpenMetaverse project
library. This subproject requires either the libomv-core binary archive and all its dependencies in
the classpath, or the libomv-core subproject added to the libomv-gui project as dependent project.
In Eclipse you can easily add another project as dependencies to a project.

The source code for all these subprojects is provided in the SubVersion repository of this project
on sourceforge.net in separate subdirectories of the project. While it's possible to retrieve
everything in one go from the root directory of the repository it is much better to retrieve each
subproject separately, especially when you develop from an IDE such as Eclipse.
	
They can be extracted with following svn URLs:
libomv-core: svn co https://libomv-java.svn.sourceforge.net/svnroot/libomv-java/libomv-core/trunk libomv-java
libomv-jj2K: svn co https://libomv-java.svn.sourceforge.net/svnroot/libomv-java/libomv-jj2K/trunk libomv-jj2K
libomv-gui: svn co https://libomv-java.svn.sourceforge.net/svnroot/libomv-java/libomv-gui/trunk libomv-gui

libomv-gui has an extra dependency for the jogl libraries that get best installed as user libraries in the
Eclipse Environment. More details to follow. For now jogl is not a critical part as the only program
(AvatarViewer) that needs it not yet functional.


Building the library and examples
---------------------------------

The library can be built using Eclipse.

You will need various existing Java libraries installed - more details to follow. Most non-standard Java
libraries are provided in the lib directory of this project download.

Before you can build the library itself, you need to generate the packet classes in the packets subdirectory.
Refer to the subheading "Rebuilding packet classes" below for more details.

While building with ant should be possible, it's not what I use for the moment, so whoever wants to do
that will be on its own and should understand the ant specific issues.


LindenLabs server certificate
-----------------------------

There seems to be issues with the SSL certificate used by the linden labs server, since it is not traceable
to any standard root certification agency. The current library will install a modified KeyStore with an added
Linden Lab root certificate, loaded from the res directory, when attempting to open an https connection to
a server having "lindenlab" in its URI. It actually appears that they use different root certificates for
the aditi (beta) and agni (release) grid. So the certificate handling for https is now trying to match the
certificates in the res directory to the URI and if loads it into the keystore for that connection. If no
stored certificate can be found the Java default keystore will be used, which contains certificates for
most standard root CAs.


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

You can obtain the latest protocol definition file (called message_template.msg) directly from
https://bitbucket.org/lindenlab/master-message-template, any recent old style Secondlife client, or the
libsecondlife site.

Put this file into the libomv/mapgenerator/ directory, and rerun the code generator by right clicking
src/libomv.mapgenerator/mapgenerator and selecting "Run as Java Application". Don't forget to refresh
the src/libomv.packets directory in the Eclipse Package Explorer pane after generation and before
compiling the library or opening one of the generated packet files in that directory.


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

There is currently no image handling at all, and also no voice chat support.

While image handling will be required to implement proper baking (de-clouding) of the avatar, I'm
still trying to figure out what image handling interface to use for that functionality as I would
like to try to avoid Swing or other GUI framework dependencies in the core library (anything in
src.libomv.* except src.libomv.GUI.*). Preliminary plans are to use a somewhat enhanced version
of the j2k library as most of the baking is actually happening with jpeg2k formated files.
Additional extensions to import/export other file formats such as tga, jpg, png, etc. will be
added as needed, using the generic j2k image file classes as appropriate.

Voice chat is also a challenge since it uses SIP and although there are many Java SIP libraries out
there, most of them have their quirks and limitations or are not free to use. So it's not very likely
to be added anytime soon, unless someone else provides an implementation of this.

3D Rendering would be an interesting challenge to implement but it is unlikely to happen anytime
soon either, since it requires a whole 3D rendering java infrastructure. OpenGL or OpenGL ES alone
is to basic for this and other higher level 3D rendering libraries are usually to different for what
we would need here, to platform specific, and to incomplete and if at all available in a free version,
mostly abandoned. An interesting library for this might be oglio but it seems also abandoned.
