<%@page contentType="text/html"
   import="java.net.*"
%>
<html>
<head><title>Operation Results</title>
<link rel="stylesheet" href="style_master.css" type="text/css">
</head>
<body>
<table>
	<tr><td><img src="images/head_blue.gif" align="left" border="0" alt=""></td></tr>

<tr><td>

<jsp:useBean id='opResultInfo' class='org.jboss.jmx.adaptor.control.OpResultInfo' scope='request'/>

<h1>Operation <code><%= opResultInfo.name %></code> Results</h1>
<a href='HtmlAdaptor?action=displayMBeans'>Back to Agent View</a>
<br>
<a href='HtmlAdaptor?action=inspectMBean&name=<%= URLEncoder.encode(request.getParameter("name")) %>'>Back to MBean View</a>

<hr>
   <span class='OpResult'>
<%
   if( opResultInfo.result == null )
   {
%>
   Operation completed successfully without a return value.
<%
   }
   else
   {
      String opResultString = opResultInfo.result.toString();
      boolean hasPreTag = opResultString.startsWith("<pre>");
      if( hasPreTag == false )
         out.println("<pre>");
      out.println(opResultString);
      if( hasPreTag == false )
         out.println("</pre>");
   }
%>
   </span>
</td></tr>
</table>
</body>
</html>
