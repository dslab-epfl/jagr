package roc.pinpoint.rtsrcbug;

import roc.pinpoint.tracing.*;

public class RuntimeSourceBugHelper {

    public static final int OP_BIN_GT = 0;
    public static final int OP_BIN_GTE = 1;
    public static final int OP_BIN_EQ = 2;
    public static final int OP_BIN_LTE = 3;
    public static final int OP_BIN_LT = 4;
    public static final int OP_BIN_NEQ = 5;
    public static final int OP_BIN_OR = 6;
    public static final int OP_BIN_AND = 7;

    public static boolean injectInvertUnaryOp( boolean correctValue,
					 String location ) {
	ReportBugInjection( "unary", location );
	return !correctValue;
    }

    public static void reportSynchronizationError( String location ) {
	ReportBugInjection( "synchronization", location );
    }

    public static void reportBranchError( String location ) {
	ReportBugInjection( "branch", location );
    }

    public static void reportInitializationError( String location ) {
	ReportBugInjection( "mis-initialization", location );
    }

    public static boolean reportMisassignment( boolean rhs, 
					      String location ) {
	ReportBugInjection( "misassignment", location );
	return rhs;
    }

    public static char reportMisassignment( char rhs, 
					      String location ) {
	ReportBugInjection( "misassignment", location );
	return rhs;
    }
    public static byte reportMisassignment( byte rhs, 
					      String location ) {
	ReportBugInjection( "misassignment", location );
	return rhs;
    }
    public static short reportMisassignment( short rhs, 
					      String location ) {
	ReportBugInjection( "misassignment", location );
	return rhs;
    }
    public static int reportMisassignment( int rhs, 
					      String location ) {
	ReportBugInjection( "misassignment", location );
	return rhs;
    }
    public static long reportMisassignment( long rhs, 
					      String location ) {
	ReportBugInjection( "misassignment", location );
	return rhs;
    }
    public static float reportMisassignment( float rhs, 
					      String location ) {
	ReportBugInjection( "misassignment", location );
	return rhs;
    }
    public static double reportMisassignment( double rhs, 
					      String location ) {
	ReportBugInjection( "misassignment", location );
	return rhs;
    }
    public static Object reportMisassignment( Object rhs, 
					      String location ) {
	ReportBugInjection( "misassignment", location );
	return rhs;
    }



    public static Object injectMisreference( Object goodValue, Object badValue,
					     String location ) {

	if( goodValue == badValue ) {
	    ReportFalseInjection( "misreference", location );
	}
	else if(( goodValue instanceof String) &&
		(goodValue.equals(badValue))) {
	    ReportFalseInjection( "misreference", location );
	}
	else {
	    ReportBugInjection( "misreference", location );
	}

	return badValue;
    }
    
    public static boolean injectFaultyBinaryComparison( Comparable lhs,
							Comparable rhs, 
							int correctOp,
							int falseOp,
							String location ) {
	int comparison = lhs.compareTo( rhs );
	
	boolean[] binValues = new boolean[] {
	    comparison > 0,
	    comparison >= 0,
	    comparison == 0,
	    comparison <= 0,
	    comparison < 0,
	    comparison != 0 };

	boolean correctRet = binValues[correctOp];
	boolean falseRet = binValues[falseOp];
	    
	if( correctRet == falseRet ) {
	    ReportBugInjection( "binarycompare", location );
	}
	else {
	    ReportFalseInjection( "binarycompare", location );
	}

	return falseRet;
    }

    public static boolean injectFaultyBinaryComparison( short lhs,
							short rhs, 
							int correctOp,
							int falseOp,
							String location ) {
	boolean[] binValues = new boolean[] {
	    lhs > rhs,
	    lhs >= rhs,
	    lhs == rhs,
	    lhs <= rhs,
	    lhs < rhs,
	    lhs != rhs
	};

	boolean correctRet = binValues[correctOp];
	boolean falseRet = binValues[falseOp];
	    
	if( correctRet == falseRet ) {
	    ReportBugInjection( "binarycompare", location );
	}
	else {
	    ReportFalseInjection( "binarycompare", location );
	}

	return falseRet;
    }

    public static boolean injectFaultyBinaryComparison( byte lhs,
							byte rhs, 
							int correctOp,
							int falseOp,
							String location ) {
	boolean[] binValues = new boolean[] {
	    lhs > rhs,
	    lhs >= rhs,
	    lhs == rhs,
	    lhs <= rhs,
	    lhs < rhs,
	    lhs != rhs
	};

	boolean correctRet = binValues[correctOp];
	boolean falseRet = binValues[falseOp];
	    
	if( correctRet == falseRet ) {
	    ReportBugInjection( "binarycompare", location );
	}
	else {
	    ReportFalseInjection( "binarycompare", location );
	}

	return falseRet;
    }

    public static boolean injectFaultyBinaryComparison( char lhs,
							char rhs, 
							int correctOp,
							int falseOp,
							String location ) {
	boolean[] binValues = new boolean[] {
	    lhs > rhs,
	    lhs >= rhs,
	    lhs == rhs,
	    lhs <= rhs,
	    lhs < rhs,
	    lhs != rhs
	};

	boolean correctRet = binValues[correctOp];
	boolean falseRet = binValues[falseOp];
	    
	if( correctRet == falseRet ) {
	    ReportBugInjection( "binarycompare", location );
	}
	else {
	    ReportFalseInjection( "binarycompare", location );
	}

	return falseRet;
    }

    public static boolean injectFaultyBinaryComparison( int lhs,
							int rhs, 
							int correctOp,
							int falseOp,
							String location ) {
	boolean[] binValues = new boolean[] {
	    lhs > rhs,
	    lhs >= rhs,
	    lhs == rhs,
	    lhs <= rhs,
	    lhs < rhs,
	    lhs != rhs
	};

	boolean correctRet = binValues[correctOp];
	boolean falseRet = binValues[falseOp];
	    
	if( correctRet == falseRet ) {
	    ReportBugInjection( "binarycompare", location );
	}
	else {
	    ReportFalseInjection( "binarycompare", location );
	}

	return falseRet;
    }

    public static boolean injectFaultyBinaryComparison( long lhs,
							long rhs, 
							int correctOp,
							int falseOp,
							String location ) {
	boolean[] binValues = new boolean[] {
	    lhs > rhs,
	    lhs >= rhs,
	    lhs == rhs,
	    lhs <= rhs,
	    lhs < rhs,
	    lhs != rhs
	};

	boolean correctRet = binValues[correctOp];
	boolean falseRet = binValues[falseOp];
	    
	if( correctRet == falseRet ) {
	    ReportBugInjection( "binarycompare", location );
	}
	else {
	    ReportFalseInjection( "binarycompare", location );
	}

	return falseRet;
    }

    public static boolean injectFaultyBinaryComparison( float lhs,
							float rhs, 
							int correctOp,
							int falseOp,
							String location ) {
	boolean[] binValues = new boolean[] {
	    lhs > rhs,
	    lhs >= rhs,
	    lhs == rhs,
	    lhs <= rhs,
	    lhs < rhs,
	    lhs != rhs
	};

	boolean correctRet = binValues[correctOp];
	boolean falseRet = binValues[falseOp];
	    
	if( correctRet == falseRet ) {
	    ReportBugInjection( "binarycompare", location );
	}
	else {
	    ReportFalseInjection( "binarycompare", location );
	}

	return falseRet;
    }

    public static boolean injectFaultyBinaryComparison( double lhs,
							double rhs, 
							int correctOp,
							int falseOp,
							String location ) {
	boolean[] binValues = new boolean[] {
	    lhs > rhs,
	    lhs >= rhs,
	    lhs == rhs,
	    lhs <= rhs,
	    lhs < rhs,
	    lhs != rhs
	};

	boolean correctRet = binValues[correctOp];
	boolean falseRet = binValues[falseOp];
	    
	if( correctRet == falseRet ) {
	    ReportBugInjection( "binarycompare", location );
	}
	else {
	    ReportFalseInjection( "binarycompare", location );
	}

	return falseRet;
    }

    public static boolean injectFaultyBinaryComparison( boolean lhs,
							boolean rhs, 
							int correctOp,
							int falseOp,
							String location ) {
	boolean[] binValues = new boolean[] {
	    false,
	    false,
	    lhs == rhs,
	    false,
	    false,
	    lhs != rhs,
	    lhs && rhs,
	    lhs || rhs
	};

	

	boolean correctRet = binValues[correctOp];
	boolean falseRet = binValues[falseOp];
	    
	if( correctRet == falseRet ) {
	    ReportBugInjection( "binarycompare", location );
	}
	else {
	    ReportFalseInjection( "binarycompare", location );
	}

	return falseRet;
    }



    private static void ReportBugInjection( String bugname, String location ) {
	System.out.println( "SRCBUGINJECTION: INJECTING " + bugname + 
			    " into request id: " + 
			    ThreadedRequestTracer.getRequestInfo().getRequestId() );
    }

    private static void ReportFalseInjection( String bugname, String location ) {
	System.out.println( "SRCBUGINJECTION: DID NOT INJECT (no change at runtime) " + bugname + 
			    " into request id: " + 
			    ThreadedRequestTracer.getRequestInfo().getRequestId() );
    }
					    
					    
					    


}
