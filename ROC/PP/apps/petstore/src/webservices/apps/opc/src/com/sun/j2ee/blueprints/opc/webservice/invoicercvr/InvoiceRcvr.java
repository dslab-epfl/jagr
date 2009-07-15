package com.sun.j2ee.blueprints.opc.webservice.invoicercvr;

import java.net.*;
import java.io.*;
import java.rmi.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.jms.*;

import com.sun.j2ee.blueprints.opc.webservice.*;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;
import com.sun.j2ee.blueprints.servicelocator.web.ServiceLocator;


public class InvoiceRcvr {
  public static final boolean TRACE = false;
  private TPAInvoiceXDE invoiceXDE;
  private TopicHelper invoiceTopicHelper;

  public InvoiceRcvr() throws RemoteException {
        try {
      ServiceLocator serviceLocator = ServiceLocator.getInstance();
      TopicConnectionFactory topicFactory = serviceLocator.getTopicConnectionFactory(JNDINames.TOPIC_CONNECTION_FACTORY);
      Topic invoiceTopic = serviceLocator.getTopic(JNDINames.INVOICE_TOPIC);
      invoiceTopicHelper = new TopicHelper(topicFactory, invoiceTopic);
      invoiceXDE = new TPAInvoiceXDE(serviceLocator.getUrl(JNDINames.XML_ENTITY_CATALOG_URL),
                                     serviceLocator.getBoolean(JNDINames.XML_VALIDATION_INVOICE),
                                     serviceLocator.getBoolean(JNDINames.XML_XSD_VALIDATION));
        } catch (Exception exception) {
      System.err.println(exception);
      throw new RemoteException("Service internal error.", exception);
        }
        return;
  }

  public void receive(Source invoiceDocument) throws RemoteException, InvalidInvoiceException {
        try {
      invoiceXDE.setDocument(invoiceDocument);
      String document = invoiceXDE.getDocumentAsString();
      if (TRACE) {
                System.err.println("InvoiceRcvr.receive, resending: " + document);
      }
      invoiceTopicHelper.sendMessage(document);
        } catch (XMLDocumentException exception) {
      System.err.println(exception);
      throw new InvalidInvoiceException(exception.getMessage());
        } catch (Exception exception) {
      System.err.println(exception);
      throw new RemoteException("Service internal error.", exception);
        }
        return;
  }
}

