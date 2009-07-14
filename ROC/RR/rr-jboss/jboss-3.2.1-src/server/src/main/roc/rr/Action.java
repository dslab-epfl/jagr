/**
 *  Definition of actions and thier parameters used for fault injection
 *
 *  $Id: Action.java,v 1.1 2004/08/26 17:47:39 skawamo Exp $
 */


package roc.rr;

public class Action 
{
    // The types of actions 
    public final static int NO_ACTION        = 1110;
    public final static int END_OF_CAMPAIGN  = 1120; 
    public final static int MICROREBOOT      = 1130;
    public final static int FULL_REBOOT      = 1140;
    public final static int INJECT_MEMLEAK   = 1150;
    public final static int INJECT_THROWABLE = 1160;
    public final static int SET_NULL_TXINT   = 1170;
    public final static int DEADLOCK         = 1180;
    public final static int INFINITE_LOOP    = 1190;
    public final static int UNBIND_NAME      = 1200;
    public final static int CORRUPT_FIELD    = 1210;
    public final static int CORRUPT_JNDI     = 1220;
    public final static int CORRUPT_DATA     = 1230;

    // parameters for passing values corresponding to actions
    public final static int MEMLEAK_BYTES_PER_CALL = 2110;
    public final static int THROWABLE_NAME         = 2120;
    public final static int DEADLOCK_PARAMS        = 2130;
    public final static int MAX_INFINITE_LOOP      = 2140;
    public final static int FIELD_NAME             = 2150;
    public final static int NO_PARAM               = 2160;
}
