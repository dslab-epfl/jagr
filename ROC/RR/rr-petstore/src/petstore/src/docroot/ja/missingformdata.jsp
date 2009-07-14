<%--
 % $Id: missingformdata.jsp,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 % Copyright 2000 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2000 Sun Microsystems, Inc. Tous droits r�erv�.
--%>

<%--
 % Report the missing fields to the user after a form validation
 % fails.
--%>
<%@ page contentType="text/html;charset=SJIS" %>

<%@ page import="com.sun.j2ee.blueprints.petstore.control.web.MissingFormDataException" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>

<p>

<%
  MissingFormDataException error =
    (MissingFormDataException)request.getAttribute("missingFormData");
  Collection missingFields = null;
  if (error != null)
    missingFields = error.getMissingFields();
%>

<font size="5" color="red"><%= error.getMessage() %></font>
<p>
  �u���E�U��"�߂�"�{�^���őO�̃y�[�W�ɖ߂��āA�ȉ��̍��ڂ�������
  ���͂���Ă��邱�Ƃ��m�F���āA�ēx�A���̃t�H�[���𑗐M����
  �������B
<ul>

<%
  if (missingFields != null) {
    Iterator it = missingFields.iterator();
    while (it.hasNext()) {
      String item = (String)it.next();
%>

    <li><%= item %></li>

<%   } %>
<% } %>

</ul>
