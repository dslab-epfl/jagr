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
 % $Id: item.jsp,v 1.1.1.1 2003/03/07 08:30:30 emrek Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits rÈservÈs.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/fmt.tld" %>

<jsp:useBean
  id="catalog"
  class="com.sun.j2ee.blueprints.catalog.client.CatalogHelper"
  scope="session"
/>


<c:set value="zh_CN" target="${catalog}" property="locale"/>
<c:set value="${param.item_id}" target="${catalog}" property="itemId"/>
<c:set value="${catalog.item}" var="item" />

<p class="petstore_title">
 <c:out value="${item.attribute}"/><c:out value="${item.productName}"/>
</p>

<%-- Set the Locale for the Page to Japanese --%>
<fmt:setLocale value="zh_CN" />
 
<table cellpadding="2" cellspacing="0" border="0" width="100%">
 <tr>
  <td><img src='images/<c:out value="${item.imageLocation}"/>'></td>
  <td class="petstore" width="100%"><b>
  价格表价格：</b>
   <fmt:formatNumber value="${item.listCost}" type="currency" />
   <br><br>
   <b>给您的价格：</b>
   <fmt:formatNumber value="${item.unitCost}" type="currency" />
   <br><br>
   <c:url value="/cart.do" var="cartURL">
     <c:param name="action" value="purchase"/>
     <c:param name="itemId" value="${item.itemId}"/>
   </c:url>
   <a href='<c:out value="${cartURL}"/>'>
    加入购物筐
   </a>
  </td>
 </tr>
</table>
