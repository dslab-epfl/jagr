package org.jboss.test.cmp2.commerce;

import javax.ejb.EJBLocalObject;
import javax.ejb.FinderException;

public interface TxTester extends EJBLocalObject
{
   void accessCMRCollectionWithoutTx() throws Exception;
}
