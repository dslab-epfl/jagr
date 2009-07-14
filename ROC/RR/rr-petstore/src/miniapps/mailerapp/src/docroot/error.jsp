<!--
 % $Id: error.jsp,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits r&eacute;serv&eacute;s.
-->

<html>
<head>
<title>J2EE[tm] Blueprints: Blueprints Mailer > Error</title>
</head>
<body>

<%@ include file="header.jsp" %>

<h2>Error</h2>

<p>We had problems processing your e-mail.</p>

<%= request.getAttribute("error_message") %>

<p><a href="<%=request.getContextPath()%>/control/index">Try Again!</a></p>

</body>
</html>
