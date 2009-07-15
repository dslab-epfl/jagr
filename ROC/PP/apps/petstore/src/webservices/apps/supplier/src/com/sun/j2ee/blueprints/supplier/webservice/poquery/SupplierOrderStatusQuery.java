package com.sun.j2ee.blueprints.supplier.webservice.poquery;

import java.rmi.*;
import javax.ejb.*;

import com.sun.j2ee.blueprints.supplier.webservice.*;
import com.sun.j2ee.blueprints.supplierpo.ejb.*;
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;


public class SupplierOrderStatusQuery {
  private SupplierOrderLocalHome supplierOrderLocalHome;


  public SupplierOrderStatusQuery() throws RemoteException {
    try {
          ServiceLocator serviceLocator = new ServiceLocator();
          supplierOrderLocalHome = (SupplierOrderLocalHome) serviceLocator.getLocalHome(JNDINames.SUPPLIER_ORDER_EJB);
    } catch (ServiceLocatorException exception) {
          System.err.println(exception);
          throw new RemoteException("Service internal error.", exception);
    }
    return;
  }

  public String queryStatus(String orderId) throws RemoteException, UnknownOrderIdException {
    try {
          SupplierOrderLocal supplierOrder = supplierOrderLocalHome.findByPrimaryKey(orderId);
          return supplierOrder.getPoStatus();
    } catch (FinderException exception) {
          System.err.println(exception);
          throw new UnknownOrderIdException(exception.getMessage());
    } catch (Exception exception) {
          System.err.println(exception);
          throw new RemoteException("Service internal error.", exception);
    }
  }
}

