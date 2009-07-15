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

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="waf" uri="/WEB-INF/waftags.tld"  %>
<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/fmt.tld" %>

<c:choose>
 <c:when test="${cart.count == 0}">
  <p class="petstore_title">ショッピングカートには何も入ってません。</p>
 </c:when>
 <c:otherwise><p class="petstore_title">ショッピングカート</p>
  <%-- Set the Locale for the Page to Japanese --%>
  <fmt:setLocale value="ja_JP" />
  <form action="cart.do">
   <input type="hidden" name="action" value="update">
   <table border="0"width="100%" cellpadding="1" cellspacing="0">
    <tr>
     <td bgcolor="#808080">
      <table width="100%" 
                              cellspacing="0" 
                              cellpadding="2" 
                              border="0"
                              bgcolor="#FFFFFF"> 
        <c:forEach var="item" items="${cart.items}" >
        <tr>
         <td width="50%" class="petstore_listing">
          <c:url value="/item.screen" var="viewItemURL">
           <c:param name="item_id" value="${item.itemId}"/>
          </c:url>
          <a href='<c:out value="${viewItemURL}"/>'>
           <c:out value="${item.attribute}" /> 
           <c:out value="${item.name}" />
          </a>
         </td>
         <td class="petstore_listing">
          <c:url value="/cart.do" var="removeURL">
           <c:param name="action" value="remove"/>
           <c:param name="itemId" value="${item.itemId}"/>
          </c:url>
          <a href='<c:out value="${removeURL}"/>'>
          削除
         </a>
        </td>
        <td class="petstore_listing" align="right">
         <waf:input type="text" maxlength="10" size="3">
         <waf:name>itemQuantity_<c:out value="${item.itemId}"/></waf:name>
         <waf:value><c:out value="${item.quantity}" /></waf:value></waf:input>
        </td>
        <td class="petstore_listing" align="right"> @
         <fmt:formatNumber value="${item.unitCost}" type="currency" />
        </td>
       </tr>
       </c:forEach>
       <tr>
        <td class="petstore_listing" colspan="2">
         <input type="submit" value="カートを更新">
        </td>
        <td class="petstore_listing" align="right">
         <b>総合計:</b>
        </td>
        <td bgcolor="#CCCCFF" class="petstore_listing" align="right">

         <fmt:formatNumber value="${cart.subTotal}" type="currency" />
        </td>
       </tr>
      </table>
     </td>
    </tr>
    <tr>
     <td align="right">
      <p class="petstore">
       <a href="enter_order_information.screen">
        チェクアウトへ
       </a>
      </p>
     </td>
    </tr>
   </table>
  </form>
 </c:otherwise>
</c:choose>
