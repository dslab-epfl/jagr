<%--
 % $Id: errorpage.jsp,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 % Copyright 2000 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2000 Sun Microsystems, Inc. Tous droits r�erv�.
--%>

<%--
 % This page is invoked when an error happens at the server.  The
 % error details are available in the implicit 'exception' object.
 % We set the error page to this file in each of our screens.
 % (via the template.jsp)
--%>
<%@ page contentType="text/html;charset=SJIS" %>

<%@ page isErrorPage="true" %>
<%
  if (exception == null)
     exception = new Exception("Exception UNAVAILABLE: Tracing Stack...");
%>

<html>
  <head>
    <title> �T�[�o�G���[: <%= exception.getMessage() %></title>
  </head>
  <body>
    <h2>Server Error</h2>
      <font face="Verdana" color="red" size="3">
        <b><em><%= exception.getMessage() %></em></b>
      </font>
    </h2>
    <h3>
        ���Ȃ��̃��N�G�X�g�͐���ɏI�����܂���ł����B�T�[�o�ňȉ��̃G���[��
        �������܂����B
    </h3>
    <p>
    <p>
    <pre>
      <% exception.printStackTrace(new PrintWriter(out)); %>
    </pre>

    <p> �V�X�e���Ǘ��҂ɂ��₢���킹�������B </p>
  </body>
</html>
