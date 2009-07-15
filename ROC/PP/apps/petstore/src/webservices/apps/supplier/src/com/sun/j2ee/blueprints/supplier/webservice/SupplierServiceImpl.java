package com.sun.j2ee.blueprints.supplier.webservice;

import javax.xml.rpc.*;
import javax.xml.rpc.server.*;
import javax.xml.transform.Source;
import java.rmi.*;

import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;
import com.sun.j2ee.blueprints.supplier.webservice.porcvr.*;
import com.sun.j2ee.blueprints.supplier.webservice.poquery.*;


public class SupplierServiceImpl implements SupplierService, ServiceLifecycle {


  public void init(Object context) throws JAXRPCException {}

  public String submitOrder(Source supplierOrder) throws InvalidOrderException, RemoteException {
      SupplierOrderRcvr supplierOrderRcvr = new SupplierOrderRcvr(); // Should ideally get it from a pool
      return supplierOrderRcvr.receive(supplierOrder);
  }

  public String queryOrderStatus(String supplierOrderId) throws UnknownOrderIdException, RemoteException {
      SupplierOrderStatusQuery supplierOrderStatusQuery = new SupplierOrderStatusQuery();
      return supplierOrderStatusQuery.queryStatus(supplierOrderId);
  }

  public void destroy() {}
}

