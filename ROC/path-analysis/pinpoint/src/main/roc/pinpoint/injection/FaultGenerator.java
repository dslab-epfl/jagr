package roc.pinpoint.injection;

import java.util.Map;
import java.io.File;
import java.io.IOException;


public class FaultGenerator {

    private static FaultConfig faultConfig;

    static {
	InitializeFaultTriggers();
    }

    
    public static void InitializeFaultTriggers() {
        try {
	    String filename =
		System.getProperty( "roc.pinpoint.injection.FaultTriggerFile" );

	    if( filename != null ) {
		System.err.println( "EMK: Reading fault trigger " +
				    "information from file " + filename );
		faultConfig  = FaultConfig.ParseFaultConfig( new File( filename ));
		System.err.println( "EMK: Done reading fault trigger file" );
	    }
	    else {
		System.err.println( "EMK: No fault trigger file specified." );
	    }
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }


   public static int CheckFaultTriggers( Map currComponent ) {
       if( faultConfig == null )
	   return FaultTrigger.FT_NOFAULT;

       return faultConfig.checkFaultTriggers( currComponent );
   }


    public static boolean isAutomatableFault( int ft ) {
        return (ft>0) && (ft <= FaultTrigger.IS_AUTOMATIC_FAULT);
    }

    public static void GenerateFault( int ft ) {
        try {

        System.err.println( "EMK: no fault triggered" );

        if( ft == FaultTrigger.FT_NOFAULT ) {
            // do nothing
            return;
        }
        else if( ft == FaultTrigger.FT_THROWRUNTIMEEXCEPTION ) {
            System.err.println( "EMK: triggering run-time exception" );
            throw new RuntimeException( "ROC Fault System triggered Runtime Exception" );
        }
        else if( ft == FaultTrigger.FT_INFINITELOOP ) {
            System.err.println( "EMK: triggering infinite-loop" );
            while( true ) { }
        }
        else if( ft == FaultTrigger.FT_HALTJVM ) {
            System.err.println( "EMK: triggering haltjvm" );
            System.exit(-1);
        }
        else {
            System.err.println( "EMK: Ooops! GenerateFault called with unrecognized Fault.  Are you sure you called 'isAutomatableFault()' first?" );
            Exception e = new Exception();
            e.printStackTrace();
        }

        }
        catch( NullPointerException e ) {
            e.printStackTrace();
        }
    }



}
