<%--
 % $Id: pendingorders.jsp,v 1.1.1.1 2002/10/03 21:17:37 candea Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
--%>

<%--
 % Generates a list of pending orders in XML
 %
 % Currently this file is only accessed from the Microsoft
 % interoperatility demo.
--%>

<%--
<%@ page contentType="text/xml" %>
--%>

<jsp:useBean
  id="manageorders"
  class="com.sun.j2ee.blueprints.petstoreadmin.control.web.ManageOrdersBean"
  scope="page"
/>

<jsp:getProperty name="manageorders" property="pendingOrdersXML" />
