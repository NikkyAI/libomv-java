libomv-java
------------------

This is the Java port of the OpenMetaverse (http://www.OpenMetaverse.org) library originally written in C#.

The initial port was performed by Simon Whiteside (http://www.larts.co.uk).
Many modifications to the original Java port and additions from newer OpenMetaverse source code were done
by Frederick Martian.

Building the library and examples
---------------------------------

The library can be built using ANT with the enclosed build.xml.

You should customize the build.xml file to suit your environment, in particular properties:

java_lib_dir - the location of the your installations java jar files
forename, surname, password - the login credentials for a SecondLife account to use for testing

You will need various existing Java libraries installed - more details to follow.

LindenLabs server certificate
-----------------------------

There seem to be issues with the SSL certificate used by the linden labs server. You will need to import their certificate
into your keychain.

You can extract the certificate with:

ant installcert

This puts the certificate into a file called jssecacerts in the current directory.

You can copy this into your $JAVA_HOME/jre/lib/security directory. 

The InstallCert.java program, and the instructions were taken from http://blogs.sun.com/andreas/entry/no_more_unable_to_find. 

You can visit that page for more information.

Running examples
----------------

You can run examples using the enclosed build.xml

Each example is a separate target within the build.xml file.

For example to run the "sldump" test application execute the following command:

ant sldump





Rebuilding packet classes
-------------------------

The packet classes, which are used when communicating with the secondlife servers, are generated from the
current protocol definition using an application called mapgenerator, which has also been ported.

You can obtain the latest protocol definition files (called message_template.msg and keywords.txt) from the libsecondlife site.

Put these files into the libsecondlife/mapgenerator/ directory, and rerun the code generator:

ant mapgenerator



Porting Information
-------------------

The initial port was taken pretty straight from the original C# code as of October 26th 2006

Principal areas of porting were:

*) change "bit-twiddling" code into calls into Java ByteBuffer class

*) altered C# style networking code into Java networking code
