# Downloads #

These instructions were verified to work on the following platform:

  * RedHat Linux 9.0 and Fedora Core 1.0
  * Java j2sdk1.4.1\_04 [download](download.md)
  * Java j2sdkee1.3.1 [download](download.md)

# Installation Steps #

```
   1. Everything will be downloaded and installed in a directory; we'll refer to it as $TOP. First, set your environment up; for example:

      setenv TOP ${HOME}
      setenv JBOSS_HOME ${TOP}/jboss-3.2.1-src/build/output/jboss-3.2.1
      setenv JAVA_HOME /usr/java/j2sdk1.4.1_04
      setenv J2EE_HOME /usr/java/j2sdkee1.3.1
      setenv PATH ${PATH}:${JAVA_HOME}/bin:${JBOSS_TOP}/tools/bin

   2. Check out RR-SSM from CVS:

      setenv CVS_RSH ssh
      cd /tmp
      rm -rf ROC
      cvs -d cvs:/home/CVS co ROC/RR/ssm
      mkdir $TOP/ssm
      cd $TOP/ssm
      cp -r /tmp/ROC/RR/ssm/* .
               

   3. Configure SSM and compile it. SSM uses multicast groups; if you're running in the same cluster with other SSM users, you must set this group appropriately, to avoid conflicts. Right now, we've allocated the following groups:

          226.1.1.1 (Shinichi)
          226.1.1.2 (Yuichi)
          226.1.1.3 (Geo)
          226.1.1.4 (Greg) 

      Replace DESIRED_MULTICAST_GROUP in roc/rr/ssm/Brick.java with the right group, then type "ant".

   4. Deploy the ssm.jar file:

      cp ssm.jar ${JBOSS_HOME}/server/default/deploy
```