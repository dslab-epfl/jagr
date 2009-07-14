<%--
 % $Id: shiporder.jsp,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 % Copyright 2000 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2000 Sun Microsystems, Inc. Tous droits r?serv?s. 
--%>
<%@ page contentType="text/html;charset=SJIS" %>
<%@ page import="com.sun.j2ee.blueprints.shoppingcart.catalog.model.Product" %>
<%@ page import="com.sun.j2ee.blueprints.shoppingcart.catalog.model.Item" %>
<%@ page import="com.sun.j2ee.blueprints.customer.order.model.LineItem" %>
<%@ page import="com.sun.j2ee.blueprints.petstore.util.JSPUtil" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Iterator" %>

<p>

<jsp:useBean 
  id="catalog" 
  class="com.sun.j2ee.blueprints.petstore.control.web.CatalogWebImpl" 
  scope="application" 
/>

<jsp:useBean 
  id="order" 
  class="com.sun.j2ee.blueprints.petstore.control.web.OrderWebImpl" 
  scope="request">
<%
 order.init(request); 
%>
</jsp:useBean>

<%
if (order.getOrderId() != -1){
%>
    <p>
      <font size="5">������:</font>
      <%= order.getOrderDate().getFullDateString(java.util.Locale.JAPAN) %>
    <p>
      <font size="5">���[�U ID:</font><%= order.getUserId() %>
    <p>
      <font size="5">�������:</font>
    <p>
      <font size="3">�����ԍ�:</font><%= order.getOrderId() %>
    <p>
    <table bgcolor="#336666">
      <tr background="../images/bkg-topbar.gif" border="0">
	<th><font size="3" color="white">�A�C�e���R�[�h</font></th>
	<th><font size="3" color="white">�A�C�e����</font></th>
	<th><font size="3" color="white">�P��</font></th>
	<th><font size="3" color="white">����</font></th>
	<th><font size="3" color="white">���v</font></th>
      </tr>

<%
  Collection lineItems = order.getLineItems();
  Iterator it = lineItems.iterator(); 
  while (it.hasNext()) {
    LineItem lineItem = (LineItem) it.next();
    Item item = catalog.getItem(lineItem.getItemNo().trim(), Locale.JAPAN);
    Product product = catalog.getProduct(item.getProductId(), Locale.JAPAN);
    // For each line item in the order -
%>

      <tr bgcolor="#eeebcc">
	<td><%= item.getItemId() %></td>
	<td>
	  <a href="productdetails?item_id=<%= item.getItemId() %>">
	    <%= item.getAttribute() %> 
	    <%= product.getName() %>
         </a>
	</td>
	<td>
	  <%= JSPUtil.formatCurrency(lineItem.getUnitPrice(), java.util.Locale.JAPAN) %>
	</td>
	<td>
	  <%= lineItem.getQty() %>
	</td>
	<td>
	  <%= JSPUtil.formatCurrency(lineItem.getUnitPrice() *
	  lineItem.getQty(), java.util.Locale.JAPAN) %>
	</td>
      </tr>
<%}%>


<%-- End of line items --%>
      <tr bgcolor="#336666">
	<td><font size="3" color="white">�����v:</font></td>
	<td></td>
	<td></td>
	<td></td>
	<td>
	  <font size="3" color="white">
	    <%=JSPUtil.formatCurrency(order.getTotalPrice(), java.util.Locale.JAPAN) %>
	  </font>
	</td>
      </tr>
    </table>

    <p>
      <font size="5">���i�z�����:</font>
    <p>
      <font size="3">���i�z����:</font>
      
    <table border="0">
      <tr>
	<td colspan="3">
	  <%= order.getShipToAddr().getStreetName1() %>
	</td>
      </tr>
      <tr>
	<td colspan="3">
	  <%= order.getShipToAddr().getStreetName2() %>
	</td>
      </tr>
      <tr>
	<td>
	  <%= order.getShipToAddr().getCity() %> ,
	</td>
	<td>
	  <%= order.getShipToAddr().getState() %>
	</td>
	<td>
	  <%= order.getShipToAddr().getZipCode() %>
	</td>
      </tr>
    </table>
    
    <p>
      <font size="3">�z�����:</font>
      <%= order.getCarrier() %>
    <p>
      <font size="5">���x�������:</font>
    <p>
      <font size="3">���������t��:</font>

    <table border="0">
      <tr>
	<td colspan="3">
	  <%= order.getBillToLastName() %>
	  <%= order.getBillToFirstName() %> 
	</td>
      </tr>
      <tr>
	<td colspan="3">
	  <%= order.getBillToAddr().getStreetName1() %>
	</td>
      </tr>
      <tr>
	<td colspan="3">
	  <%= order.getBillToAddr().getStreetName2() %>
	</td>
      </tr>
      <tr>
	<td>
	  <%= order.getBillToAddr().getCity() %>,
	</td>
	<td>
	  <%= order.getBillToAddr().getState() %>
	</td>
	<td>
	  <%= order.getBillToAddr().getZipCode() %>
	</td>
      </tr>
    </table>

    <p>
      <font size="3">�N���W�b�g�J�[�h���:</font>
    <p>
    <table border="0">
      <tr>
	<td>�N���W�b�g�J�[�h���:</td>
	<td>
	  <%= order.getCreditCard().getCardType() %>
	</td>
      </tr>
      <tr>
	<td>�J�[�h�ԍ�:</td>
	<td>
	  <%= order.getCreditCard().getCardNo() %>
	</td>
      </tr>
      <tr>
	<td>�L������:</td>
	<td>
	  <%= order.getCreditCard().getExpiryDateString() %>
	</td></tr>
    </table>

    <p>
      <font size="5">�X�e�[�^�X:</font>
    <p>
      �X�e�[�^�X: <%= order.getStatus() %>

<% } %>



