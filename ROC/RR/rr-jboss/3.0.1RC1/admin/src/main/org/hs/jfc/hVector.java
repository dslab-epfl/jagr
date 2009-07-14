package org.hs.jfc;

import java.util.Vector;

public class hVector extends Vector
{
   public boolean add(Object o) { super.addElement(o); return true; }
   public boolean remove(Object o)
   {
      if (super.indexOf(o) == -1)
         return false;
      else
      {
         super.removeElement(o);
         return true;
      }
   }
   public Object remove(int i)
   {
      Object o = super.elementAt(i);
      super.removeElementAt(i);
      return o;
   }
}
