<%--
 % $Id: entershippingaddress.jsp,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 % Copyright 2000 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2000 Sun Microsystems, Inc. Tous droits r�erv�.
--%>

<%--
 % prompts for address information, applies it to the shipping
 % address.
--%>
<%@ page contentType="text/html;charset=SJIS" %>

<p>
<form action="validateshippinginformation">
  ���Ȃ��̂������̔�����̏Z���Ɩ��O����͂��ĉ������B
  <p>
    <%@ include file="/addressform.html" %>
    <p>
    <input type="image" src="../images/button_cont.gif" value=
      "Continue" border="0">
</form>
