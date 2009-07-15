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

public class CanonicalComponent extends Component {

    static Map components = new HashMap();

    private CanonicalComponent( Map attrs ) {
        super(attrs);
    }

    private CanonicalComponent( String attr ) {
        super(attr);
    }

    public static CanonicalComponent get( Map attrs ) {
        CanonicalComponent cc = (CanonicalComponent)components.get(attrs);
        if( cc == null ) {
            cc = new CanonicalComponent( attrs );
            components.put( attrs, cc );
        }
        return cc;
    }

    public static CanonicalComponent get( String attr ) {
        CanonicalComponent cc = (CanonicalComponent)components.get(attr);
        if( cc == null ) {
            cc = new CanonicalComponent( attr );
            components.put( attr, cc );
        }
        return cc;
    }

    public boolean equals( Object o ) {
        if( o instanceof CanonicalComponent ) {
            return (o == this);
        }
        else {
            return super.equals(o);
        }
    }

}
