# IN PREPARATION !! #

# Downloads #

These instructions were verified to work with:

  * RedHat Linux 9.0 and 7.2
  * JBoss 3.2.1 [download](download.md)
  * Petstore 1.4.1 [download](download.md)
  * Java j2sdk1.4.1\_04 and j2sdk1.4.2\_01 [download](download.md)
  * Java j2sdkee1.3.1 [download](download.md)
  * MySQL-3.23.56-1 and MySQL-3.23.57
  * Apache Ant 1.5.4 [download](download.md)

# Drop-in #

If you have no desire to make changes to Pet Store source code, then:

```
   1. Download vanilla JBoss and untar it:

        wget crash.stanford.edu/download/jboss-3.2.1-src-nocvs.tar.gz
        tar xvpzf jboss-3.2.1-src-nocvs.tar.gz
        rm jboss-3.2.1-src-nocvs.tar.gz

   2. Build JBoss and set $JBOSS_HOME (your $JAVA_HOME must be set):

        cd jboss-3.2.1-src/build
        ./build.sh
        setenv JBOSS_HOME `pwd`/output/jboss-3.2.1

   3. Download our Petstore drop-in archive and create a new JBoss configuration called 'petstore':

        cd $JBOSS_HOME/server
        cp -r default petstore
        wget crash.stanford.edu/download/petstore_1.3.2_minimal_dropin.tar
        tar xvf petstore_1.3.2_minimal_dropin.tar
        rm petstore_1.3.2_minimal_dropin.tar

   4. Start Cloudscape and JBoss (make sure $J2EE_HOME is set and you have write permissions for $J2EE_HOME/cloudscape):

        $J2EE_HOME/bin/cloudscape -start &
        $JBOSS_HOME/bin/run.sh -c petstore
        

   5. Browse http://localhost:8080/petstore. 

```