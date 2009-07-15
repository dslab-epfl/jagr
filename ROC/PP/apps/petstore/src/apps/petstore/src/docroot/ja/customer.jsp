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
 % $Id: customer.jsp,v 1.1.1.1 2003/03/07 08:30:30 emrek Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits rÈservÈs.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/waftags.tld" prefix="waf" %>
<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>

<p class="petstore_title">顧客情報</p>
<p class="petstore_title">連絡先</p>

<table cellpadding="5" cellspacing="0" width="100%" border="0">
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
  <td class="petstore_form"><b>郵便番号</b>
   <c:out value="${customer.account.contactInfo.address.zipCode}"/>
  </td>
 </tr>

 <tr>
  <td class="petstore_form" align="right"><b>都道府県名</b></td>
  <td class="petstore_form" align="left">
   <c:out value="${customer.account.contactInfo.address.state}"/>
  </td>
 </tr>

 <tr>
  <td class="petstore_form" align="right"><b>市町村名</b></td>
  <td align="left" colspan="2">
   <c:out value="${customer.account.contactInfo.address.city}"/>
  </td>
 </tr>
 <tr>
  <td class="petstore_form" align="right">
   <b>町名番地</b>
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
  <td class="petstore_form" align="right"><b>国名</b></td>
  <td class="petstore_form" align="left" colspan="2">
     <c:out value="${customer.account.contactInfo.address.country}"/>
  </td>
 </tr>

 <tr>
  <td class="petstore_form" align="right">
   <b>電話番号</b>
  </td>
  <td align="left" colspan="2">
   <c:out value="${customer.account.contactInfo.telephone}"/>
  </td>
 </tr>

 <tr>
  <td nowrap="true" class="petstore_form" align="right">
   <b>E-Mail アドレス</b>
  </td>
  <td align="left" colspan="2">
      <c:out value="${customer.account.contactInfo.email}"/>
  </td>
 </tr>
</table>

<p class="petstore_title">クレジットカード情報</p>
<table cellpadding="5" cellspacing="0" width="100%" border="0">
 <tr>
  <td class="petstore_form" align="right"><b>クレジットカード種別</b></td> 
  <td align="left" colspan="2">
   <c:out value="${customer.account.creditCard.cardType}"/>
  </td>
 </tr>
 <tr>
  <td class="petstore_form" align="right">
   <b>カード番号</b>
  </td> 
  <td align="left" colspan="2">
   <c:out value="${customer.account.creditCard.cardNumber}"/>
  </td>
 </tr>
 <tr>
  <td class="petstore_form" align="right">
   <b>有効期限</b>
  </td> 
  <td align="left" colspan="2">月: 
   <c:out value="${customer.account.creditCard.expiryMonth}"/>
    年: 
   <c:out value="${customer.account.creditCard.expiryYear}"/>
  </td>
 </tr>
</table>

<p class="petstore_title">プロフィール情報</p>
<table border="0" cellpadding="5" width="100%" cellspacing="0">
 <tr>
  <td></td>
  <td>
   わたしの PetStore は次の言語を希望します。
   <b>
   <waf:select size="1" name="language" editable="false">
    <waf:selected><c:out value="${customer.profile.preferredLanguage}"/></waf:selected>
    <waf:option value="en_US">英語</waf:option>
    <waf:option value="ja_JP">日本語</waf:option>
    <waf:option value="zh_CN">中国語</waf:option>
   </waf:select>
  </b>
  </td>
 </tr>
 <tr>
  <td></td>
  <td>
   私の好きなカテゴリは、
   <b>
    <waf:select size="1" name="favorite_category" editable="false">
     <waf:selected><c:out value="${customer.profile.favoriteCategory}"/></waf:selected>
     <waf:option value="BIRDS">鳥</waf:option>
     <waf:option value="CATS">猫</waf:option>
     <waf:option value="DOGS">犬</waf:option>
     <waf:option value="FISH">魚</waf:option>
     <waf:option value="REPTILES">爬虫類</waf:option>
    </waf:select>
    </b>
   </td>
 </tr>

 <tr>
  <td nowrap="true">
   &nbsp;
   <c:choose>
    <c:when test="${customer.profile.myListPreference == true}">
     <font size="+1" color="green">はい</font>
    </c:when>
    <c:otherwise>
     <font size="+1" color="red">いいえ</font>
    </c:otherwise>
   </c:choose>
   &nbsp;
   </td>
  <td>
   MyList 機能を有効にすることを希望します。
   <i>MyList は、お買い物の時に
        目に付くように、お気に入りの商品やカテゴリを表示します。</i>
  </td>
 </tr>

 <tr>
  <td nowrap="true">
   &nbsp;
   <c:choose>
    <c:when test="${customer.profile.bannerPreference == true}">
     <font size="+1" color="green">はい</font>
    </c:when>
    <c:otherwise>
     <font size="+1" color="red">いいえ</font>
    </c:otherwise>
   </c:choose>
   &nbsp;
  </td>
  <td>
   ペットの情報バナー機能を有効にすることを希望します。
   <i>
    Java ペット屋さんは、あなたのお気に入りの商品やカテゴリをもとに、お買い物の時に、ペットの情報を表示します。
   </i>
  </td>
 </tr>
</table>
<br>
<a href="update_customer.screen">顧客情報を変更する</a>

