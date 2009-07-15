<!--
 Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 
 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
 
 - Redistribution in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in
   the documentation and/or other materials provided with the
   distribution.
 
 Neither the name of Sun Microsystems, Inc. or the names of
 contributors may be used to endorse or promote products derived
 from this software without specific prior written permission.
 
 This software is provided "AS IS," without a warranty of any
 kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
 OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 
 You acknowledge that Software is not designed, licensed or intended
 for use in the design, construction, operation or maintenance of
 any nuclear facility.
-->

<%--
 % $Id: displayinventory.jsp,v 1.1.1.1 2003/03/07 08:30:30 emrek Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits réservés. 
--%>

<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.sun.j2ee.blueprints.supplier.inventory.ejb.InventoryLocal" %>

<html>
<head>
      <title>Update Inventory</title>
</head>
<body bgcolor="white" text="#000000" link="#0000EE" vlink="#551A8B" alink="#FF0000">
      <h1>Inventory Update Page</h1>
      <h3>The existing inventory of all items is displayed below. Please
          enter the new quantity, check the adjacent box and, when all updates
	  are done, click "submit" button</h3>
      <hr width="100%">

<jsp:useBean 
    id="displayInventory"
    class="com.sun.j2ee.blueprints.supplier.inventory.web.DisplayInventoryBean"
    scope="session"
/>

<%
if(request.isUserInRole("administrator")) {
%>

    <form action="RcvrRequestProcessor" method="post">
    <input type="hidden" name="currentScreen" value="updateinventory">
    <input type="hidden" name="action" value="updateinventory">
<%
    Collection inventoryItems = displayInventory.getInventory();
    if (inventoryItems != null) {
%>
            <br>
	    <br>
	    <table border="0" bgcolor="#336666">
	    <tr>
		<td><font color="white" size="3">Item Id</font></td>
		<td><font color="white" size="3">Existing Quantity</font></td>
		<td><font color="white" size="3">New Quantity</font></td>
		<td><font color="white" size="3">Update</font></td>
	    </tr>

<%
	    Iterator it = null;
	    if(inventoryItems != null)
		it = inventoryItems.iterator();
	    while ((it != null) && it.hasNext()) {
		InventoryLocal anItem = (InventoryLocal)it.next();
%>

		<tr bgcolor="#eeebcc">
		<td align="center"><%= anItem.getItemId() %></td>
		<td align="right"><%= anItem.getQuantity() %></td>
		<td><input type="text"
			   name="<%= "qty_"+ anItem.getItemId() %>"
			   size="6"></td>
		<td align="center">
    		       <input type=checkbox 
		       name="<%= "item_"+ anItem.getItemId() %>" 
		       value="false"></td>
		</tr>
<%
	    }
%>
            </table>
	    <br><br>
	    <input type="image" border="0"
	           src="images/button_submit.gif" name="button">
    	    </form>
<%
    } else {
%>
    	<font size="5" color="green">
    	There are no items in inventory at all !!!!!! 
    	</font>
<%
    }
} else {
%>
    <font size="5" color="green">
    You are not authorised to update the status of orders.
    </font>
<%
}
%>
</body>
</html>

