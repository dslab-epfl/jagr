1. you must edit config/jboss.env to point to the right directories and JDBC paths and correct hostnames.
2. Edit driver.bat or driver.sh to contain the correct paths and hostnames
3. Edit jboss/bin/run.bat to make sure that 
4. Edit jboss-service.xml so that JNDI runs under port 1100.
5. Edit .../ecperf/config/run.properties to set correct directories and to
set Transaction Rate (txRate).
6. Edit .../ecperf/build.xml to set SCALE to match txRate.
   
7. ant
8. ant deploy
9. ant create-schema (there is a drop-schema if needed)
10. ant loaddb
11. startup jboss
12. cd ecperf/bin
13. driver.bat or driver.sh
14. View results .../ecperf/output of driver's machine

I may have missed some points here.
- Build only works with one DB, meaning you can't partition different tables onto 
different DBs.  You'll have to configure this yourself if you want to test in this way.

To run with clustering.

1. ant clean
2. Edit build.xml:
  <property name="jbossconfig" value="all"/>

  <property name="jdbcsuffix" value="cluster.xml"/>
  <property name="jbosssuffix" value="CMP.CLUSTER"/>
3. in cluster-service.xml, comment out the HAJNDI service.  It is not used.

  <!--
  <mbean code="org.jboss.ha.jndi.HANamingService" 
         name="jboss:service=HAJNDI">
     <depends>jboss:service=DefaultPartition</depends>
  </mbean>
  -->

