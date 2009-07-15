// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: Observed.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ---------------------------------------------------------------------------

package org.mortbay.util;

import java.util.Observable;

/* ======================================================================== */
/** Helpful extension to Observable.
 * NotifyObservers will set a changed first.
 */
public class Observed  extends Observable
{
    public void notifyObservers(Object arg)
    {
        setChanged();
        super.notifyObservers(arg);
    }

    public void notifyObservers()
    {
        setChanged();
        super.notifyObservers(null);
    }
}
