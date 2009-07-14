<%--
 % $Id: entershippingaddress.jsp,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
--%>

<%--
 % prompts for address information, applies it to the shipping
 % address.
--%>

<p>
<form action="validateshippinginformation">
  Please enter the name and address to which you would like your
  order shipped.
  <p>
    <%@ include file="addressform.html" %>
    <p>
    <input type="image" src="../images/button_cont.gif" value=
      "Continue" border="0">
</form>
