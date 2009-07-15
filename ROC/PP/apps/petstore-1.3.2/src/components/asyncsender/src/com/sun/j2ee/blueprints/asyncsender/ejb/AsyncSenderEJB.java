/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
 * OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of
 * any nuclear facility.
 */

package com.sun.j2ee.blueprints.asyncsender.ejb;

import javax.jms.*;
import javax.ejb.*;
import javax.naming.NamingException;

import com.sun.j2ee.blueprints.asyncsender.util.JNDINames;
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;


public class AsyncSenderEJB implements SessionBean {

  private SessionContext sc;
  private Queue q;
  private QueueConnectionFactory qFactory;


  public AsyncSenderEJB() {}

  public void ejbCreate( ) throws CreateException {
        try {
      ServiceLocator serviceLocator = new ServiceLocator();
      qFactory = serviceLocator.getQueueConnectionFactory(JNDINames.QUEUE_CONNECTION_FACTORY);
      q = serviceLocator.getQueue(JNDINames.ASYNC_SENDER_QUEUE);
        } catch (ServiceLocatorException sle) {
      throw new EJBException("AsyncSenderEJB.ejbCreate failed", sle);
        }
  }

  public void sendAMessage(String msg)  {
    QueueSession session = null;
    QueueConnection qConnect = null;
    QueueSender qSender = null;

    try {
      qConnect = qFactory.createQueueConnection();
      session = qConnect.createQueueSession(false,0);
      qSender = session.createSender(q);
      TextMessage jmsMsg = session.createTextMessage();
      jmsMsg.setText(msg);
      qSender.send(jmsMsg);
    }catch  (Exception e) {
      e.printStackTrace();
      throw new EJBException("askMDBToSendAMessage: Error!",e);
    } finally {
      try {
                if( qConnect != null ) {
          qConnect.close();
                }
      } catch(Exception e) {}
        }
  }

  public void setSessionContext(SessionContext sc) { }

  public void ejbRemove() { }

  //empty for stateless session EJBs
  public void ejbActivate() { }
  //empty for stateless session EJBs
  public void ejbPassivate() { }
}

