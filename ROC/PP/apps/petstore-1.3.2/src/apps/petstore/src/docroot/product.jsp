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
 % $Id: product.jsp,v 1.1 2004/02/04 10:06:21 emrek Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits rÈservÈs.
--%>

<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/fmt.tld" %>
<%@ taglib uri="/WEB-INF/waftags.tld" prefix="waf" %>

<waf:cache name="product_en_US" scope="context" duration="300000">

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
 the products to ensure the proper items are listed.
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
<c:set value="${param.product_id}" target="${catalog}" property="productId"/>
<c:set value="${catalog.items}" var="pageResults" />

<p class="petstore_title">Items for this Product</p>
<fmt:setLocale value="en_US" />
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
   <c:url value="/item.screen" var="viewItemURL">
    <c:param name="item_id" value="${item.itemId}"/>
   </c:url>
   <a href='<c:out value="${viewItemURL}"/>'>
    <c:out value="${item.attribute}" />
    <c:out value="${item.productName}"/>
   </a>
   <br>
   <c:out value="${item.description}"/>
  </td>
  <td class="petstore_listing" align="right">
   <fmt:formatNumber value="${item.listCost}" type="currency" />
   <br>
   <c:url value="/cart.do" var="cartURL">
     <c:param name="action" value="purchase"/>
     <c:param name="itemId" value="${item.itemId}"/>
   </c:url>
   <a href='<c:out value="${cartURL}"/>'>
    Add to Cart
   </a>
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
    <c:url value="/product.screen" var="previousURL">
      <c:param name="product_id" value="${param.product_id}"/>
      <c:param name="start" value="${pageResults.startOfPreviousPage}"/>
      <c:param name="count" value="2"/>
    </c:url>
    <a href='<c:out value="${previousURL}"/>'>
     Previous
    </a>
  </c:if>
  <c:if test="${pageResults.nextPageAvailable == true}">
    <c:url value="/product.screen" var="nextURL">
      <c:param name="product_id" value="${param.product_id}"/>
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
