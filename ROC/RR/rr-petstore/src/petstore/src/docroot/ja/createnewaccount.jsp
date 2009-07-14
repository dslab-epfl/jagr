<%--
 % $Id: createnewaccount.jsp,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 % Copyright 2000 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2000 Sun Microsystems, Inc. Tous droits r�erv�.
--%>

<%--
 % Form that prompts the user for account details required at the time
 % of creating a new account.
--%>
<%@ page contentType="text/html;charset=SJIS" %>

<form action="validatenewaccount">
  <input type="hidden" name="action" value="createAccount">
  <p>
    <font size="5" color="green">
      �ڋq���:
    </font>
  <p>
  <table border ="0">
    <tr>
      <td>���[�U ID:</td>
      <td align="left">
        <input type="text" size="15" name="user_name" maxlength="20">
      </td>
    </tr>
    <tr>
      <td>�p�X���[�h:</td>
      <td align="left">
        <input type="password" size="15" name="password" maxlength="20">
      </td>
    </tr>
    <tr>
      <td>�ڋq�^�C�v:</td>
      <td align="left">
        <select  name = "user_type">
          <option value="default">Home Consumer
          <option value="business">Commercial Business
          <option value="whole_seller">Whole Seller
        </select>
      </td>
    </tr>
    <tr>
      <td>E-Mail �A�h���X:</td>
      <td align="left">
        <input type="text" size="30" name="user_email" maxlength="70">
      </td>
    </tr>
  </table>
  <p>
  <p>
  <p>
    <font size="5" color="green">Address:</font>
  <p>
    <jsp:include page="addressform.jsp" flush="true" />
  <p>
    <font size="5" color="green">Preferences:</font>
  <p>
    <jsp:include page="preferencesform.jsp" flush="true"/>
  <p>
    <input type="image" border="0" name="Submit"
      src="<%=request.getContextPath()%>/images/button_submit.gif">
</form>
