#!/usr/bin/perl

$loadgen_dir = "~/ROC/RR/loadgen";
$i = 0;

while (1) 
{
    print("Run #" . $i . "\n");
    $cloudscape_log = "/tmp/cloudscape.log." . $i;
    $jboss_log = "/tmp/jboss.log." . $i;
    $loadgen_log =  "/tmp/loadgen.log." . $i;

    system("rm -f $jboss_log $loadgen_log");

    print("   starting database...\n");
    system("\$J2EE_HOME/bin/cloudscape -start >& $cloudscape_log &");

    print("   starting JBoss...\n");
    system("cd \$JBOSS_HOME/bin ; ./run.sh >& $jboss_log &");

    print("   starting load generator...\n");
    system("cd $loadgen_dir ; java -cp . LoadGen 1 localhost 8080 >& /tmp/loadgen.log." . $i);
    
    print("   shutting down JBoss...\n");
    system("cd \$JBOSS_HOME/bin ; ./shutdown.sh");

    print("   shutting down database...\n");
    system("\$J2EE_HOME/bin/cloudscape -stop");

    sleep(30);

    print("   sending SIGKILL to all Java processes...\n");
    system("killall -KILL java");
    $i++;
}
