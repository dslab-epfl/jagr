/**
    Copyright (C) 2004 Emre Kiciman and Stanford University

    This file is part of Pinpoint

    Pinpoint is free software; you can distribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    (at your option) any later version.

    Pinpoint is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Pinpoint; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**/
package roc.pinpoint.analysis.structure;

import java.util.*;

public class CanonicalLink extends Link {

    static Map links = new HashMap();

    private CanonicalLink( Component src, Component sink ) {
        super(src,sink);
    }

    public static CanonicalLink get( Component src, Component sink ) {
        Map m = (Map)links.get(src);
        if( m == null ) {
            m = new HashMap(1);
            links.put(src,m);
        }
        CanonicalLink cl = (CanonicalLink)m.get(sink);
        if( cl == null ) {
            cl = new CanonicalLink(src,sink);
            m.put(sink,cl);
        }
        return cl;
    }

    public boolean equals( Object o ) {
        if( o instanceof CanonicalLink ) {
            return (o == this);
        }
        else {
            return super.equals(o);
        }
    }

}
