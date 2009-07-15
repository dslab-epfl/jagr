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
 % $Id: enter_order_information.jsp,v 1.1 2004/02/04 10:06:22 emrek Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits rÈservÈs.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/waftags.tld" prefix="waf" %>
<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>


<p class="petstore_title">請求書送付先</p>

<waf:form name="order_ino" method="POST" action="order.do">
 <table cellpadding="5" cellspacing="0" width="100%" border="0">
  <tr>
   <td class="petstore_form" align="right">
    <b>姓</b>
   </td> 
   <td align="left" colspan="2">
    <waf:input cssClass="petstore_form"
                              type="text"
                             name="family_name_a"
                               size="30"
                    maxlength="30"
                      validation="validation">
     <waf:value><c:out value="${customer.account.contactInfo.familyName}"/></waf:value>
    </waf:input>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>名</b>
   </td> 
   <td align="left" colspan="2">
    <waf:input cssClass="petstore_form"
                             type="text"
                           name="given_name_a"
                              size="30"
                   maxlength="30"
                       validation="validation">
      <waf:value><c:out value="${customer.account.contactInfo.givenName}"/></waf:value>
    </waf:input>
   </td>
  </tr>
  <tr>
   <td class="petstore_form"><b>郵便番号</b>
    <waf:input cssClass="petstore_form" type="text"
                            name="postal_code_a" 
                               size="12"
                    maxlength="12"
                      validation="validation">
     <waf:value><c:out value="${customer.account.contactInfo.address.zipCode}"/></waf:value>
    </waf:input>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>都道府県名</b>
   </td>
   <td class="petstore_form" align="left">
    <waf:select size="1"
                      name="state_or_province_a">
     <waf:selected><c:out value="${customer.account.contactInfo.address.state}"/></waf:selected>
     <waf:option value="東京都">東京都</waf:option>
     <waf:option value="大阪府">大阪府</waf:option>
     <waf:option value="長野県">長野県</waf:option>
    </waf:select>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>市町村名</b>
   </td>
   <td align="left" colspan="2">
    <waf:input cssClass="petstore_form"
                              type="text"
                             name="city_a"
                               size="55"
                    maxlength="70"
                      validation="validation">
      <waf:value><c:out value="${customer.account.contactInfo.address.city}"/></waf:value>
     </waf:input>
   </td>
  </tr>

  <tr>
   <td class="petstore_form" align="right"><b>町名番地</b></td>
   <td align="left" colspan="2">
   <waf:input cssClass="petstore_form"
                             type="text"
                           name="address_1_a"
                              size="55"
                   maxlength="70"
              validation="validation">
     <waf:value><c:out value="${customer.account.contactInfo.address.streetName1}"/></waf:value>
    </waf:input>
   </td>
  </tr>

  <tr>
   <td>&nbsp;</td>
   <td align="left" colspan="2">
    <waf:input cssClass="petstore_form"
                        type="text"
                       name="address_2_a"
                          size="55"
               maxlength="70">
       <waf:value><c:out value="${customer.account.contactInfo.address.streetName2}"/></waf:value>
     </waf:input>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>国名</b>
   </td>
   <td class="petstore_form" align="left" colspan="2">
    <waf:select size="1" name="country_a">
    <waf:selected><c:out value="${customer.account.contactInfo.address.country}"/></waf:selected>
     <waf:option value="アメリカ">アメリカ</waf:option>
     <waf:option value="カナダ" />
     <waf:option value="日本" /> 
     <waf:option value="中国" /> 
    </waf:select>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>電話番号</b>
   </td>
   <td align="left" colspan="2">
    <waf:input cssClass="petstore_form"
                              type="text"
                             name="telephone_number_a"
                                size="12"
                     maxlength="70"
                         validation="validation">
      <waf:value><c:out value="${customer.account.contactInfo.telephone}"/></waf:value>
     </waf:input>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>E-Mailアドレス</b>
   </td>
   <td align="left" colspan="2">
    <waf:input cssClass="petstore_form"
                              type="text"
                             name="email_a"
                               size="12"
                    maxlength="70"
                      validation="validation">
      <waf:value><c:out value="${customer.account.contactInfo.email}"/></waf:value>
     </waf:input>
   </td>
  </tr>
 </table>


 <p class="petstore_title">商品配送先</p>

 <table cellpadding="5" cellspacing="0" width="100%" border="0">
  <tr>
   <td class="petstore_form" align="right">
    <b>姓</b>
   </td> 
   <td align="left" colspan="2">
    <waf:input cssClass="petstore_form"
                              type="text"
                             name="family_name_b"
                                size="30"
                     maxlength="30"
                       validation="validation">
      <waf:value><c:out value="${customer.account.contactInfo.familyName}"/></waf:value>
     </waf:input>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>名</b>
   </td> 
   <td align="left" colspan="2">
    <waf:input cssClass="petstore_form"
                              type="text"
                             name="given_name_b"
                                size="30"
                    maxlength="30"
                      validation="validation">
       <waf:value><c:out value="${customer.account.contactInfo.givenName}"/></waf:value>
     </waf:input>
   </td>
  </tr>
  <tr>
   <td class="petstore_form"><b>郵便番号</b>
    <waf:input cssClass="petstore_form" type="text"
                            name="postal_code_b" 
                              size="12"
                   maxlength="12"
                     validation="validation">
      <waf:value><c:out value="${customer.account.contactInfo.address.zipCode}"/></waf:value>
     </waf:input>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>都道府県名</b>
   </td>
   <td class="petstore_form" align="left">
    <waf:select size="1"
                      name="state_or_province_b">
     <waf:selected>
      <c:out value="${customer.account.contactInfo.address.state}"/>
     </waf:selected>
     <waf:option value="東京都">東京都</waf:option>
     <waf:option value="大阪府">大阪府</waf:option>
     <waf:option value="長野県">長野県</waf:option>
    </waf:select>
   </td>
  </tr>

  <tr>
   <td class="petstore_form" align="right">
    <b>市町村名</b>
   </td>
   <td align="left" colspan="2">
    <waf:input cssClass="petstore_form"
                              type="text"
                            name="city_b"
                               size="55"
                    maxlength="70"
                      validation="validation">
       <waf:value><c:out value="${customer.account.contactInfo.address.city}"/></waf:value>
     </waf:input>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right"><b>町名番地</b></td>
   <td align="left" colspan="2">
   <waf:input cssClass="petstore_form"
                       type="text"
                      name="address_1_b"
                         size="55"
              maxlength="70"
               validation="validation">
      <waf:value><c:out value="${customer.account.contactInfo.address.streetName1}"/></waf:value>
     </waf:input>
   </td>
  </tr>

  <tr>
   <td>&nbsp;</td>
   <td align="left" colspan="2">
    <waf:input cssClass="petstore_form"
                              type="text"
                             name="address_2_b"
                               size="55"
                    maxlength="70">
      <waf:value><c:out value="${customer.account.contactInfo.address.streetName2}"/></waf:value>
     </waf:input>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>国名</b>
   </td>
   <td class="petstore_form" align="left"  colspan="2">
    <waf:select size="1" name="country_b">
     <waf:selected><c:out value="${customer.account.contactInfo.address.country}"/></waf:selected>
     <waf:option value="アメリカ">アメリカ</waf:option>
     <waf:option value="カナダ" />
     <waf:option value="日本" /> 
    </waf:select>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>電話番号</b>
   </td>
   <td align="left" colspan="2">
    <waf:input cssClass="petstore_form"
                              type="text"
                            name="telephone_number_b"
                               size="12"
                    maxlength="70"
                      validation="validation">
      <waf:value><c:out value="${customer.account.contactInfo.telephone}"/></waf:value>
     </waf:input>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>E-Mailアドレス</b>
   </td>
   <td align="left" colspan="2">
    <waf:input cssClass="petstore_form"
                        type="text"
                      name="email_b"
                         size="12"
              maxlength="70"
               validation="validation">
       <waf:value><c:out value="${customer.account.contactInfo.email}"/></waf:value>
     </waf:input>
   </td>
  </tr>
 </table>
 <input class="petstore_form" type="submit" value="送信">
</waf:form>

