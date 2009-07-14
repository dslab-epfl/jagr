<%--
 % $Id: checkout.jsp,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 % Copyright 2000 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2000 Sun Microsystems, Inc. Tous droits r?serv?s. 
--%>

<%--
 % Displays the contents of the current cart for final approval of 
 % the user, before initiating the order processing.
--%>
<%@ page contentType="text/html;charset=SJIS" %>
<jsp:useBean 
  id="cart" 
  class="com.sun.j2ee.blueprints.petstore.control.web.ShoppingCartWebImpl" 
  scope="session" 
>
  <% 
    cart.init(session); 
  %>
</jsp:useBean>

<p>

<% 
  if (cart.getSize() > 0) { 
%>

  <font size="5" color="black">ショッピングカート:</font>
    <p>
      <jsp:include page="/ja/carttable.jsp" flush="true"/>
    </p>
    <p></p>
    <a href="placeorder"><img src="../images/button_cont.gif" alt="Continue" border="0"></a>
<% 
  } else { 
  // The cart is empty
%>

  <font size="5" color="red">カート空きです。</font>

<% } %>
