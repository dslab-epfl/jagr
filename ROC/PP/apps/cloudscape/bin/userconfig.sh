#
# Copyright 2002 Sun Microsystems, Inc. All rights reserved.
# SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#

# J2EE_CLASSPATH is appended to the classpath referenced by the EJB server.
# J2EE_CLASSPATH must include the location of the JDBC driver classes 
# (except for the Cloudscape driver shipped with this release).
# Each directory is delimited by a colon.

#J2EE_CLASSPATH=
#export J2EE_CLASSPATH

# JAVA_HOME refers to the directory where the Java(tm) 2 SDK
# Standard Edition software is installed.

if [ -z "$JAVA_HOME" ]
then
    JAVA_HOME=/java/re/jdk/1.3.1_02/latest/binaries/linux-i386
    export JAVA_HOME
fi
