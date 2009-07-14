package org.jboss.test.perf.interfaces;

import javax.ejb.EJBLocalObject;

/**
 @author Scott.Stark@jboss.org
 @version $Revision: 1.1.1.1 $
 */
public interface EntityLocal extends EJBLocalObject
{
  int read();
  void write(int value);
}
