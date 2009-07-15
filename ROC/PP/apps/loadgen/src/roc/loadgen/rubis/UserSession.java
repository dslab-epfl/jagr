/*
 * $Id: UserSession.java,v 1.1 2004/08/18 23:31:39 candea Exp $
 */

package roc.loadgen.rubis;

import java.util.Random;

import org.apache.log4j.Logger;

/**
 * Provides the notion of a user session, which consists of multiple
 * user actions.
 *
 * @version <tt>$Revision: 1.1 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 * Based on the RUBiS 1.4.1 client emulator, written by
 * <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and
 * <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 */

public class UserSession
{
    private static Logger log = Logger.getLogger( "UserSession" );

    public UserSession()
    {
	log.debug( "New session: " + toString() );
    }

    public String toString()
    {
	return "[]";
    }
}

