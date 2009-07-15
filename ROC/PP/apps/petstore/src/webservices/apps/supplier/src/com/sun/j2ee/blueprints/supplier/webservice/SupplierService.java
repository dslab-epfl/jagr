package com.sun.j2ee.blueprints.supplier.webservice;

import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.xml.transform.Source;


public interface SupplierService extends Remote {

    public String submitOrder(Source supplierOrder) throws InvalidOrderException, RemoteException;

    public String queryOrderStatus(String supplierOrderId) throws UnknownOrderIdException, RemoteException;
}

