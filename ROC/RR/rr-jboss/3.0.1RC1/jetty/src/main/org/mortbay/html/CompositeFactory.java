// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: CompositeFactory.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
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


