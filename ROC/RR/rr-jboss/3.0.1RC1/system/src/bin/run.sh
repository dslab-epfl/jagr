#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  JBoss Bootstrap Script                                                  ##
##                                                                          ##
### ====================================================================== ###

## PK test
## changed ~/ROC/RR/rr-jboss/system/src/bin/run.sh
## Added a line to run org.apache.log4j.net.SimpleSocketServer

### $Id: run.sh,v 1.6 2003/03/01 06:49:47 candea Exp $ ###

DIRNAME=`dirname $0`
PROGNAME=`basename $0`
GREP="grep"

#
# Helper to complain.
#
warn() {
    echo "${PROGNAME}: $*"
}

#
# Helper to puke.
#
die() {
    warn $*
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false;
darwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;

    Darwin*)
        darwin=true
        ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$JBOSS_HOME" ] &&
        JBOSS_HOME=`cygpath --unix "$JBOSS_HOME"`
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$JAVAC_JAR" ] &&
        JAVAC_JAR=`cygpath --unix "$JAVAC_JAR"`
fi

# Setup JBOSS_HOME
if [ "x$JBOSS_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    JBOSS_HOME=`cd $DIRNAME/..; pwd`
fi
export JBOSS_HOME

# Setup Profiler
useprofiler=false
if [ "x$PROFILER" != "x" ]; then
    if [ -r "$PROFILER" ]; then
        . $PROFILER
        useprofiler=true
    else
        die "Profiler file not found: $PROFILER"
    fi
fi

# Setup the JVM
if [ "x$JAVA_HOME" != "x" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA="java"
fi

# Setup the classpath
runjar="$JBOSS_HOME/bin/run.jar"
if [ ! -f $runjar ]; then
    die "Missing required file: $runjar"
fi
JBOSS_BOOT_CLASSPATH="$runjar"

# Include the JDK javac compiler for JSP pages. The default is for a Sun JDK
# compatible distribution which JAVA_HOME points to
if [ "x$JAVAC_JAR" = "x" ]; then
    JAVAC_JAR="$JAVA_HOME/lib/tools.jar"
fi
if [ ! -f "$JAVAC_JAR" ]; then
   # MacOSX does not have a seperate tools.jar
   if [ "$darwin" != "true" ]; then
      warn "Missing file: $JAVAC_JAR"
      warn "Unexpected results may occur.  Make sure JAVA_HOME points to a JDK and not a JRE."
   fi
fi

#  #
#  # FIXME: this is a temporary hack to get to org.jboss.jmx.rmi.RMIAdaptor
#  #
#  JMX_JAR="$JBOSS_HOME/client/jbossjmx-ant.jar"

#  if [ "x$JBOSS_CLASSPATH" = "x" ]; then
#      JBOSS_CLASSPATH="$JBOSS_BOOT_CLASSPATH:$JAVAC_JAR:$JMX_JAR"
#  else
#      JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_BOOT_CLASSPATH:$JAVAC_JAR:$JMX_JAR"
#  fi

#
# FIXME: this is a temporary hack; remove when FMap becomes an MBean
#
GFC_JAR="$JBOSS_HOME/lib/GFCall1.1.2.zip"

if [ "x$JBOSS_CLASSPATH" = "x" ]; then
    JBOSS_CLASSPATH="$JBOSS_BOOT_CLASSPATH:$JAVAC_JAR:$GFC_JAR"
else
    JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_BOOT_CLASSPATH:$JAVAC_JAR:$GFC_JAR"
fi

# Check for SUN(tm) JVM w/ HotSpot support
if [ "x$HAS_HOTSPOT" = "x" ]; then
    HAS_HOTSPOT=`$JAVA -version 2>&1 | $GREP HotSpot`
fi

# If JAVA_OPTS is not set and the JVM is HOTSPOT enabled, then the server mode
if [ "x$JAVA_OPTS" = "x" -a "x$HAS_HOTSPOT" != "x" ]; then
    # MacOS does not support -server flag
    if [ "$darwin" != "true" ]; then
        JAVA_OPTS="-server"
    fi
fi

# Setup JBoss sepecific properties
JAVA_OPTS="$JAVA_OPTS -Dprogram.name=$PROGNAME"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    JBOSS_HOME=`cygpath --path --windows "$JBOSS_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
    JBOSS_CLASSPATH=`cygpath --path --windows "$JBOSS_CLASSPATH"`
fi


# Display our environment
echo "================================================================================"
echo ""
echo "  JBoss Bootstrap Environment"
echo ""
echo "  JBOSS_HOME: $JBOSS_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "  CLASSPATH: $JBOSS_CLASSPATH"
echo ""
echo "================================================================================"
echo ""

if $useprofiler; then
    # Hand over control to profiler
    runProfiler
else
    # Uncomment the following line to execute logger outside JVM
    # (also need to modify jboss-service.xml)
    # exec $JAVA org.apache.log4j.net.SimpleSocketServer 4445 rr-logreceiver.properties &

    # Execute the JVM
    exec $JAVA $JAVA_OPTS \
            -classpath "$JBOSS_CLASSPATH" \
            org.jboss.Main "$@"
fi
