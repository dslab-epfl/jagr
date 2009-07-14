<%--
 % $Id: editaccount.jsp,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 % Copyright 2000 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2000 Sun Microsystems, Inc. Tous droits r�erv�.
--%>

<%--
 % Form that prompts the user for account details required at the time
 % of editing the user's account.
--%>
<%@ page contentType="text/html;charset=SJIS" %>

<%@ page import="com.sun.j2ee.blueprints.customer.util.ContactInformation" %>
<%@ page import="com.sun.j2ee.blueprints.personalization.profilemgr.model.ExplicitInformation" %>

<jsp:useBean
  id="customer"
  class="com.sun.j2ee.blueprints.petstore.control.web.CustomerWebImpl"
  scope="session"
/>
<jsp:useBean
  id="profilemgr"
  class="com.sun.j2ee.blueprints.petstore.control.web.ProfileMgrWebImpl"
  scope="session"
/>

<form action="updateaccount">
  <input type="hidden" name="action" value="updateAccount">
  <p>
    <font size="5" color="green">
      �ڋq���:
    </font>
  <p>

<%
  if (customer.isLoggedIn()) {
%>

  <table border ="0">
    <tr>
      <td>���[�U ID:</td>
      <td align="left">
        <%= customer.getUserId() %>
        <input type="hidden" name="user_name"
          value="<%= customer.getUserId() %>">
      </td>
    </tr>

    <% ContactInformation contact = customer.getContactInformation();
         request.setAttribute("contact", contact);
    %>
    <tr>
      <td>E-Mail �A�h���X:</td>
      <td align="left">
        <input type="text" size="30" name="user_email" maxlength="70"
          value="<%= contact.getEMail() %>">
      </td>
    </tr>
  </table>
  <p>
  <p>
  <p>
    <font size="5" color="green">Address:</font>
  <p>
    <jsp:include page="changeaddressform.jsp" flush="true"/>
  <p>
    <font size="5" color="green">Preferences:</font>
  <p>
    <% ExplicitInformation explicitInfo = profilemgr.getExplicitInformation();
         request.setAttribute("explicitInfo", explicitInfo );
    %>
    <jsp:include page="changepreferencesform.jsp" flush="true"/>

  <p>
    <input type="image" border="0" name="Submit"
      src="<%=request.getContextPath()%>/images/button_submit.gif">

<%
  } else {
%>

  <p>
    ���Ȃ��̌ڋq����ҏW����O�Ƀ��O�C�����ĉ������B
  </p>

<%
  }
%>
</form>
