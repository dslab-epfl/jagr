<%--
 Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 
 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
 
 - Redistribution in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in
   the documentation and/or other materials provided with the
   distribution.
 
 Neither the name of Sun Microsystems, Inc. or the names of
 contributors may be used to endorse or promote products derived
 from this software without specific prior written permission.
 
 This software is provided "AS IS," without a warranty of any
 kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
 OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 
 You acknowledge that Software is not designed, licensed or intended
 for use in the design, construction, operation or maintenance of
 any nuclear facility.
--%>

<%--
 % $Id: category.jsp,v 1.1.1.1 2003/03/07 08:30:30 emrek Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits rÈservÈs.
--%>

<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>
<%@ taglib uri="/WEB-INF/waftags.tld" prefix="waf" %>

<waf:cache name="page" scope="context" duration="300000">

<jsp:useBean
  id="catalog"
  class="com.sun.j2ee.blueprints.catalog.client.CatalogHelper"
  scope="session"
/>


<%--
 Do the page listing by geting the start index and count from the request parameters

 When there are no parameters this signifies the listing of a new product and thus
 the counter and start need to be reset.

 These parameters and the locale need to be set on the catalog bean before extracting
 the products to ensure the proper products are listed.
--%>
<c:choose>
 <c:when test="${param.count != null}">
  <c:set value="${param.start}" target="${catalog}" property="start"/>
  <c:set value="${param.count}" target="${catalog}" property="count"/>
 </c:when>
 <c:otherwise>
  <c:set value="0" target="${catalog}" property="start"/>
  <c:set value="2" target="${catalog}" property="count"/>
 </c:otherwise>
</c:choose>
<c:set value="en_US" target="${catalog}" property="locale"/>
<c:set value="${param.category_id}" target="${catalog}" property="categoryId"/>
<c:set value="${catalog.products}" var="pageResults" />

<p class="petstore_title">Products for this Category</p>


<table border="0"
       width="100%"
       cellpadding="1"
       cellspacing="0">
<tr>
<td bgcolor="#808080">
<table width="100%"
       cellspacing="0" 
       cellpadding="2" 
       border="0" 
       bgcolor="#FFFFFF">

<c:forEach var="item" items="${pageResults.list}" >
<tr>
 <td class="petstore_listing">
    <c:url value="/product.screen" var="productURL">
     <c:param name="product_id" value="${item.id}"/>
    </c:url>
    <a href='<c:out value="${productURL}"/>'>
    <c:out value="${item.name}"/>
   </a>
   <br>
   <c:out value="${item.description}"/>
  </td>
 </tr>
</c:forEach>
</table>
</td>
</tr>
</table>

<div align="right">
<p class="petstore_listing">
  <%--
   Allow for the page to be viewed in chunks using the Page List Pattern
  --%>
  <c:if test="${pageResults.previousPageAvailable == true}">
    <c:url value="/category.screen" var="previousURL">
      <c:param name="category_id" value="${param.category_id}"/>
      <c:param name="start" value="${pageResults.startOfPreviousPage}"/>
      <c:param name="count" value="2"/>
    </c:url>
    <a href='<c:out value="${previousURL}"/>'>
     Previous
    </a>
  </c:if>

  <c:if test="${pageResults.nextPageAvailable == true}">
    <c:url value="/category.screen" var="nextURL">
      <c:param name="category_id" value="${param.category_id}"/>
      <c:param name="start" value="${pageResults.startOfNextPage}"/>
      <c:param name="count" value="${pageResults.size}"/>
    </c:url>
    <a href='<c:out value="${nextURL}"/>'>
     Next
    </a>
  </c:if>
</p>
</div>

</waf:cache>
