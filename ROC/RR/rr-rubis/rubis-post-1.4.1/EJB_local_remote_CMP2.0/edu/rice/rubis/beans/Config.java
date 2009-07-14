/*
 * $Id: Config.java,v 1.1 2004/04/09 01:38:04 candea Exp $
 */

package edu.rice.rubis.beans;

/** 
 * This class contains the configuration for the beans
 */

public class Config
{

    private Config()
    {
	/* should never be instantiated */
    }

    /* If false, then don't look up nicknames in ItemBean.java, but
     * rather only return the userId instead. */
   public static final boolean noNicknameLookup = true;
}
