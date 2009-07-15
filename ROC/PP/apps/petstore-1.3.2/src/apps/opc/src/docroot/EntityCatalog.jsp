<%@page import="java.util.*"%>
<%@page import="java.net.*"%>
<%@page import="java.io.*"%>

<%
  Properties entityCatalog = (Properties) getServletContext().getAttribute("EntityCatalog");
  if (entityCatalog == null) {
    entityCatalog = new Properties();
    //System.err.println(getServletContext().getResource("/schemas/EntityCatalog.properties"));
    entityCatalog.load(getServletContext().getResource("/schemas/EntityCatalog.properties").openStream());
    for (Enumeration names = entityCatalog.propertyNames(); names.hasMoreElements();) {
      String name = (String) names.nextElement();
      String value = entityCatalog.getProperty(name);
      if (value.startsWith("/")) {
        if (value.startsWith("//")) {
          value = new URL(new URL(HttpUtils.getRequestURL(request).toString()), value.substring(1)).toString();
        } else {
          value = new URL(new URL(HttpUtils.getRequestURL(request).toString()), request.getContextPath() + value).toString();
        }
        entityCatalog.setProperty(name, value);
      }
    }
    getServletContext().setAttribute("EntityCatalog", entityCatalog);
  }
  ByteArrayOutputStream stream = new ByteArrayOutputStream();
  entityCatalog.store(stream, "Entity Catalog");
  out.print(stream.toString());
%>

