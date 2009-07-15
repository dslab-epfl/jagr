<%@page contentType="text/html"
   import="java.net.*,java.util.*,org.jboss.jmx.adaptor.model.*"
%>
<html>
<head><title>JSP Page</title>
<link rel="stylesheet" href="style_master.css" type="text/css">
</head>
<body>
<table>
	<tr><td><img src="images/head_blue.gif" align="left" border="0" alt=""></td></tr>

<tr><td>
<h1>JMX Agent View</h1>
<hr>
<%
   Iterator mbeans = (Iterator) request.getAttribute("mbeans");
   while( mbeans.hasNext() )
   {
      DomainData domainData = (DomainData) mbeans.next();
%>
   <h2 class='DomainName'><%= domainData.getDomainName() %></h2>
   <ul class='MBeanList'>
<%
      MBeanData[] data = domainData.getData();
      Arrays.sort(data);
      for(int d = 0; d < data.length; d ++)
      {
         String name = data[d].getObjectName().toString();
         String properties = data[d].getNameProperties();
%>
      <li><a href="HtmlAdaptor?action=inspectMBean&name=<%= URLEncoder.encode(name) %>"><%= properties %></a></li>
<%
      }
%>
   </ul>
<%
   }
%>
</td></tr>
</table>
</body>
</html>
