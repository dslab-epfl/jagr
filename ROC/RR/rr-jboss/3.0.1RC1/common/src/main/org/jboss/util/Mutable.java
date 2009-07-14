/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 1998-2000 by Jason Dillon <jason@org.jboss.com>             *
 *                                                                           *
 *  This file is part of Bliss; a Java class library.                        *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.jboss.util;

/**
 * Mutable object interface.
 *
 * @version <tt>$Revision: 1.1.1.1 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public interface Mutable
{
   /**
    * Set the value of a mutable object.
    *
    * @param value   Target value for object.
    */
   void setValue(Object value);

   /**
    * Get the value of a mutable object.
    *
    * @return Object value.
    */
   Object getValue();
}
