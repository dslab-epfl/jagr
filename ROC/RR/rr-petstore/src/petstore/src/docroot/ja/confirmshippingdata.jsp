<%--
 % $Id: confirmshippingdata.jsp,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 % Copyright 2000 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2000 Sun Microsystems, Inc. Tous droits r?serv?s. 
--%>



<%--
 % Displays the details of the information collected from the user
 % regarding processing of their order.
--%>
<%@ page contentType="text/html;charset=SJIS" %>
<%@ page import="com.sun.j2ee.blueprints.customer.util.ContactInformation" %>
<%@ page import="com.sun.j2ee.blueprints.petstore.util.WebKeys" %>

<%
ContactInformation billTo = (ContactInformation)session.getAttribute(WebKeys.BillingContactInfoKey);
ContactInformation shipTo = (ContactInformation)session.getAttribute(WebKeys.ShippingContactInfoKey);
%>


<p>

  ���L�̃f�[�^�����������Ƃ����m�F���������A�������̂���������]�̕��́A

  <b>Continue</b> �{�^���������ĉ������B  

<p> 

  <font size="3" color="black">���������t��</font>



<table border="0">

  <tr>

    <td>

      <%=billTo.getFamilyName()%>

      <%=billTo.getGivenName()%>

    </td>

  </tr>

  <tr>

    <td><%=billTo.getAddress().getStreetName1()%></td>

  </tr>

  <tr>

    <td><%=billTo.getAddress().getStreetName2()%></td>

  </tr>

  <tr>

    <td>

      <%=billTo.getAddress().getCity()%>,

      <%=billTo.getAddress().getState()%>&nbsp;&nbsp;

      <%=billTo.getAddress().getZipCode()%>

    </td>

  </tr>

</table>

<p>

  <font size="3" color="black">���i�z����</font>

<table border="0">

  <tr>

    <td>

      <%=shipTo.getFamilyName()%>

      <%=shipTo.getGivenName()%>

    </td>

  </tr>

  <tr>

    <td><%=shipTo.getAddress().getStreetName1()%></td>

  </tr>

  <tr>

    <td><%=shipTo.getAddress().getStreetName2()%></td>

  </tr>

  <tr>

    <td>

      <%=shipTo.getAddress().getCity()%>,

      <%=shipTo.getAddress().getState()%>&nbsp;&nbsp;

      <%=shipTo.getAddress().getZipCode()%>

    </td>

  </tr>

</table>

<p>

  <a href="commitorder"><img src="../images/button_cont.gif" alt="Continue" border="0"></a> 

<p>

