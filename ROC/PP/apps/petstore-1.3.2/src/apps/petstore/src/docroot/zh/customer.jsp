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
 % $Id: customer.jsp,v 1.1 2004/02/04 10:06:23 emrek Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits rÈservÈs.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/waftags.tld" prefix="waf" %>
<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>

<p class="petstore_title">顾客信息</p>

<table cellpadding="5" cellspacing="0" width="100%" border="0">
<tr>
<td colspan="3"><p class="petstore_title">联系信息</p></td>
</tr>
 <tr>
 <td class="petstore_form" align="right"><b>姓</b></td> 
 <td align="left" colspan="2"><c:out value="${customer.account.contactInfo.familyName}"/></td>
 </tr>
 <tr>
  <td class="petstore_form" align="right"><b>名</b></td> 
  <td align="left" colspan="2">
   <c:out value="${customer.account.contactInfo.givenName}"/>
  </td>
 </tr>

 <tr>
  <td class="petstore_form" align="right"><b>所在省份</b></td>
  <td class="petstore_form" align="left">
   <c:out value="${customer.account.contactInfo.address.state}"/>
  </td>

  <td class="petstore_form"><b>邮政编码</b>
   <c:out value="${customer.account.contactInfo.address.zipCode}"/>
  </td>
 </tr>

 <tr>
  <td class="petstore_form" align="right"><b>所在城市</b></td>
  <td align="left" colspan="2">
   <c:out value="${customer.account.contactInfo.address.city}"/>
  </td>
 </tr>
 <tr>
  <td class="petstore_form" align="right">
   <b>详细街道地址</b>
  </td>
  <td align="left" colspan="2">
   <c:out value="${customer.account.contactInfo.address.streetName1}"/>
  </td>
 </tr>

 <tr>
  <td>&nbsp;</td>
  <td align="left" colspan="2">
      <c:out value="${customer.account.contactInfo.address.streetName2}"/>
  </td>
 </tr>

 <tr>
  <td class="petstore_form" align="right"><b>国家</b></td>
  <td class="petstore_form" align="left" colspan="2">
     <c:out value="${customer.account.contactInfo.address.country}"/>
  </td>
 </tr>

 <tr>
  <td class="petstore_form" align="right">
   <b>电话号码</b>
  </td>
  <td align="left" colspan="2">
   <c:out value="${customer.account.contactInfo.telephone}"/>
  </td>
 </tr>

 <tr>
  <td nowrap="true" class="petstore_form" align="right">
   <b>E-Mail 地址</b>
  </td>
  <td align="left" colspan="2">
      <c:out value="${customer.account.contactInfo.email}"/>
  </td>
 </tr>
</table>

<p class="petstore_title">信用卡信息</p>
<table cellpadding="5" cellspacing="0" width="100%" border="0">
 <tr>
  <td class="petstore_form" align="right"><b>信用卡种类</b></td> 
  <td align="left" colspan="2">
   <c:out value="${customer.account.creditCard.cardType}"/>
  </td>
 </tr>
 <tr>
  <td class="petstore_form" align="right">
   <b>卡号</b>
  </td> 
  <td align="left" colspan="2">
   <c:out value="${customer.account.creditCard.cardNumber}"/>
  </td>
 </tr>
 <tr>
  <td class="petstore_form" align="right">
   <b>有效期限</b>
  </td> 
  <td align="left" colspan="2">
   <c:out value="${customer.account.creditCard.expiryYear}"/>年
   <c:out value="${customer.account.creditCard.expiryMonth}"/>月
  </td>
 </tr>
</table>

<p class="petstore_title">概要信息</p>
<table border="0" cellpadding="5" width="100%" cellspacing="0">
 <tr>
  <td></td>
  <td>
  我希望我的PetStore使用以下的语言：
   <b>
   <waf:select size="1" name="language" editable="false">
    <waf:selected><c:out value="${customer.profile.preferredLanguage}"/></waf:selected>
    <waf:option value="en_US">英语</waf:option>
    <waf:option value="ja_JP">日文</waf:option>
    <waf:option value="zh_CN">中文</waf:option>
   </waf:select>
  </b>
  </td>
 </tr>
 <tr>
  <td></td>
  <td>
   我喜欢的宠物种类是：
   <b>
    <waf:select size="1" name="favorite_category" editable="false">
     <waf:selected><c:out value="${customer.profile.favoriteCategory}"/></waf:selected>
     <waf:option value="BIRDS">鸟</waf:option>
     <waf:option value="CATS">猫</waf:option>
     <waf:option value="DOGS">狗</waf:option>
     <waf:option value="FISH">鱼</waf:option>
     <waf:option value="REPTILES">爬虫类</waf:option>
    </waf:select>
    </b>
   </td>
 </tr>

 <tr>
  <td nowrap="true">
   &nbsp;
   <c:choose>
    <c:when test="${customer.profile.myListPreference == true}">
     <font size="+1" color="green">是</font>
    </c:when>
    <c:otherwise>
     <font size="+1" color="red">不是</font>
    </c:otherwise>
   </c:choose>
   &nbsp;
   </td>
  <td>
   是的，我希望使用MyList功能。
   <i>MyList将在你每次购物时更明显地提供给你喜欢的商品的信息</i>
  </td>
 </tr>

 <tr>
  <td nowrap="true">
   &nbsp;
   <c:choose>
    <c:when test="${customer.profile.bannerPreference == true}">
     <font size="+1" color="green">是</font>
    </c:when>
    <c:otherwise>
     <font size="+1" color="red">不是</font>
    </c:otherwise>
   </c:choose>
   &nbsp;
  </td>
  <td>
   是的，我希望使用Pet Banner的提示功能。
   <i>
  它将在你每次购物时显示你喜欢的商品或种类的提示信息
   </i>
  </td>
 </tr>
</table>
<br>
<a href="update_customer.screen">更改顾客信息</a>

