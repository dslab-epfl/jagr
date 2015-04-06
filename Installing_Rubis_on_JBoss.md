# IN PREPARATION !! #

# Downloads #

These instructions were verified to work on the following platform:

  * RedHat Fedora Core 2 (kernel 2.6.5)
  * Java j2sdk1.4.1\_04 [download](download.md)
  * Java j2sdkee1.3.1 [download](download.md)

# Installation Steps #

```
   1. Everything will be downloaded and installed in a directory; we'll refer to it as $TOP. First, set your environment up; for example:

      setenv TOP ${HOME}
      setenv JBOSS_TOP ${TOP}/jboss-3.2.1-src
      setenv JBOSS_HOME ${JBOSS_TOP}/build/output/jboss-3.2.1
      setenv JBOSS_DIST ${JBOSS_HOME}
      setenv JAVA_HOME /usr/java/j2sdk1.4.1_04
      setenv J2EE_HOME /usr/java/j2sdkee1.3.1
      setenv PATH ${PATH}:${JAVA_HOME}/bin:${JBOSS_TOP}/tools/bin

   2. Install and build RR-JBoss in $TOP/jboss-3.2.1-src.

   3. Download MySQL together with our pre-populated RUBiS database:

      cd $TOP
      wget crash.stanford.edu/download/mysql-server_v4.tgz
      wget crash.stanford.edu/download/mysql-datafiles_v1.tgz
      tar xvzf mysql-server_v4.tgz
      tar xvzf mysql-datafiles_v1.tgz
      rm mysql-server_v4.tgz mysql-datafiles_v1.tgz
                

   4. Check out RR-RUBiS from CVS:

      setenv CVS_RSH ssh
      cd /tmp
      rm -rf ROC
      cvs -d cvs:/home/CVS co ROC/RR/rr-rubis/rubis-post-1.4.1
      mkdir $TOP/rubis
      cd $TOP/rubis
      cp -r /tmp/ROC/RR/rr-rubis/rubis-post-1.4.1/* . 
               

   5. Change the HTMLFilesPath constant in $TOP/rubis/EJB_local_remote_CMP2.0/edu/rice/rubis/beans/servlets/ConfigJBoss.java to the value of $TOP/rubis/ejb_rubis_web (do not use the environment variable name). The absolute path is required.
   6.

      Ensure the 'j2ee' constant in $TOP/rubis/build.properties points to the location of your J2EE installation (see value of $J2EE_HOME).
   7. Deploy the ssm.jar file, then build the CMP 2.0 version of RUBiS:

      wget crash.stanford.edu/download/ssm.jar -O ${JBOSS_DIST}/server/default/deploy/ssm.jar
      cd $TOP/rubis/EJB_local_remote_CMP2.0
      rehash
      ant rubis_ear
      	

   8. Start MySQL and start up JBoss:

      cd $TOP/mysql-max-3.23.58-pc-linux-i686
      bin/safe_mysqld &
      $JBOSS_HOME/bin/run.sh
              

   9. Now you can use the application by pointing your browser at

      http://localhost:8080/ejb_rubis_web
```

# Running the Client #

  1. In $TOP/rubis/Client/rubis.properties, make sure that workload\_transition\_table, database\_regions\_file, and database\_categories\_file are set to your local paths. This file can be used to further customize the experiments (e.g., replace the hostnames as needed).
> 2.
```
      Build and run the RUBiS client:

      cd $TOP/rubis/Client
      make all
      cd $TOP/rubis
      make emulator
      	  

      When the run is complete, results of the experiment will appear in $TOP/rubis/bench/$YEAR-$MONTH-$DAY@$HOUR:$MIN:$SEC/index.html.
```

## Notes ##
  1. The client depends on sar (from the sysstat package) and gnuplot. It will fail more-or-less silently if they are not available.
> 2. Set monitoring\_rsh to whatever works for you (sh, rsh, ssh, etc.). Ideally, set monitoring\_rsh to ssh (or some other rsh-like), and arrange for the user running make emulator to be able to login without a password to the machines things are running on. We use ssh with public key authentication for this.


