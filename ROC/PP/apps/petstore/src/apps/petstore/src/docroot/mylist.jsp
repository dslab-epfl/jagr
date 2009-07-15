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
 % $Id: mylist.jsp,v 1.1.1.1 2003/03/07 08:30:30 emrek Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits r駸erv駸.
--%>

<%--
 %   Displays the pet favorites of the user. The favorites list
 %   can be included in any web page. Currently it is included in
 %   the cart screen, when the customer has the opportunity to
 %   add more items just before checkout.
--%>

<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>

<jsp:useBean
  id="catalog"
  class="com.sun.j2ee.blueprints.catalog.client.CatalogHelper"
  scope="session"
/>

<c:if test="${customer.profile.myListPreference =='true'}">

 <%--
  Set the catalog start to 0 and count to 10.
  This will need to be adjusted if the number of products in any category 
  is greater than 10
 --%>

 <c:set value="0" target="${catalog}" property="start"/>
 <c:set value="10" target="${catalog}" property="count"/>
 <c:set value="en_US" target="${catalog}" property="locale"/>
 <c:set value="${customer.profile.favoriteCategory}" target="${catalog}" property="categoryId"/>
 <c:set value="${catalog.products}" var="pageResults" />

 <table border="0"
       width="100%"
       cellpadding="1"
       cellspacing="0">
  <tr>
   <td bgcolor="#336666" class="petstore_title" align="center">
    <font color="#FFFFFF">My List</font>
   </td>
  </tr>
  <tr>
   <td bgcolor="#336666">
    <table border="0" 
       width="100%" 
       cellpadding="5" 
       cellspacing="1">
     <tr>
      <td bgcolor="#FFFFFF" class="petstore">
       <c:forEach var="item" items="${pageResults.list}" >
        <a href="product.screen?product_id=<c:out value="${item.id}" />">
        <c:out value="${item.name}" /></a><br>
       </c:forEach>
      </td>
     </tr>
    </table>
   </td>
  </tr>
 </table>
</c:if>