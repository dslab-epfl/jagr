<!--
 % $Id: showmail.jsp,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits r&eacute;serv&eacute;s.
-->

<jsp:useBean
  id="result"
  type="com.sun.j2ee.blueprints.mailerapp.ResultBean"
  scope="request"
/>

<html>
<head>
<title>J2EE[tm] Blueprints: Blueprints Mailer > Sent Message
Confirmation</title>
</head>
<body>

<%@ include file="header.jsp" %>

<h2>Sent Message Confirmation</h2>

<p>The following message has been sent to <code><%=
result.getEmailAddress() %></code> with subject <b><%=
result.getSubject() %></b>.</p>

<pre><code><%= result.getResultMessage() %></code></pre>

</body>
</html>
