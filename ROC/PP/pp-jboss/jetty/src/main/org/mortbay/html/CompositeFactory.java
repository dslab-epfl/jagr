// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: CompositeFactory.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ---------------------------------------------------------------------------

package org.mortbay.html;

/* --------------------------------------------------------------------- */
/** Composite Factory.
 * Abstract interface for production of composites
 */
public interface CompositeFactory
{
    public Composite newComposite();
}


