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
 % $Id: edit_customer.jsp,v 1.1.1.1 2003/03/07 08:30:30 emrek Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits r√àserv√às.
--%>

<%@ taglib uri="/WEB-INF/waftags.tld" prefix="waf" %>
<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>

<p class="petstore_title">Your Account Information</p>

<waf:form method="POST" action="customer.do" name="customerform">
 <input type="hidden" name="action" value="update"/>
 <table cellpadding="5" cellspacing="0" width="100%" border="0">
  <tr>
   <td colspan="3">
    <p class="petstore_title">
     Contact Information
    </p>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>First Name</b>
   </td> 
   <td align="left" colspan="2">
    <waf:input cssClass="petstore_form"
                             name="given_name_a"
                              type="text"
                               size="30"
                    maxlength="30"
                      validation="validation">
     <waf:value><c:out value="${customer.account.contactInfo.givenName}"/></waf:value>
    </waf:input>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>Last Name</b>
   </td> 
   <td align="left" colspan="2">
    <waf:input cssClass="petstore_form"
                              type="text"
                             name="family_name_a"
                               size="30"
                    maxlength="30">
     <waf:value><c:out value="${customer.account.contactInfo.familyName}"/></waf:value>
    </waf:input>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>Street Address</b>
   </td>
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
    <b>City</b>
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
   <td class="petstore_form" align="right">
    <b>State/Province</b>
   </td>
   <td class="petstore_form" align="left">
    <waf:select size="1"
                      name="state_or_province_a">
     <waf:selected><c:out value="${customer.account.contactInfo.address.state}"/></waf:selected>
     <waf:option value="California" /> 
     <waf:option value="New York" />
     <waf:option value="Texas" /> 
    </waf:select>
   </td>
   <td class="petstore_form">
    <b>Postal Code</b>
    <waf:input cssClass="petstore_form"
                              type="text"
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
    <b>Country</b>
   </td>
   <td class="petstore_form" align="left" colspan="2">
    <waf:select size="1"
                      name="country_a">
     <waf:selected><c:out value="${customer.account.contactInfo.address.country}"/></waf:selected>
     <waf:option value="USA">United States</waf:option>
     <waf:option value="Canada" />
     <waf:option value="Japan" /> 
     <waf:option value="China" /> 
    </waf:select>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>Telephone Number</b>
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
    <b>E-Mail</b>
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
  <tr>
   <td colspan="3">
    <p class="petstore_title">
     Credit Card Information
    </p>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>Card Number</b>
   </td> 
   <td align="left" colspan="2">
    <waf:input cssClass="petstore_form"
                              type="text"
                              name="credit_card_number"
                               size="30"
                    maxlength="30"
                      validation="validation">
     <waf:value><c:out value="${customer.account.creditCard.cardNumber}"/></waf:value>
    </waf:input>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>Card Type</b>
   </td> 
   <td class="petstore_form" align="left" colspan="2">
    <waf:select size="1"
                      name="credit_card_type">
     <waf:selected><c:out value="${customer.account.creditCard.cardType}"/></waf:selected>
     <waf:option value="Java(TM) Card" />
     <waf:option value="Duke Express" />
     <waf:option value="Meow Card" />
    </waf:select>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <b>Card Expiry Date</b>
   </td> 
   <td class="petstore_form" align="left" colspan="2">
    <waf:select size="1"
                      name="credit_card_expiry_month">
     <waf:selected><c:out value="${customer.account.creditCard.expiryMonth}"/></waf:selected>
     <waf:option value="01" />
     <waf:option value="02" />
     <waf:option value="03" />
     <waf:option value="04" />
     <waf:option value="05" />
     <waf:option value="06" /> 
     <waf:option value="07" /> 
     <waf:option value="08" />
     <waf:option value="09" /> 
     <waf:option value="10" /> 
     <waf:option value="11" /> 
     <waf:option value="12" />
    </waf:select>
/ 
    <waf:select size="1"
                      name="credit_card_expiry_year">
     <waf:selected><c:out value="${customer.account.creditCard.expiryYear}"/></waf:selected>
     <waf:option value="2002" />
     <waf:option value="2003" />
     <waf:option value="2004" />
     <waf:option value="2005" />
    </waf:select>
   </td>
  </tr>
  <tr>
   <td colspan="3">
    <p class="petstore_title">Profile Information</p>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td colspan="2" class="petstore_form">I want MyPetStore to be in
    <waf:select size="1"
                      name="language">
     <waf:selected><c:out value="${customer.profile.preferredLanguage}"/></waf:selected>
     <waf:option value="en_US">English</waf:option>
     <waf:option value="ja_JP">Japanese</waf:option>
     <waf:option value="zh_CN">Chinese</waf:option>
    </waf:select>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td colspan="2" class="petstore_form">
     My favorite category is
    <waf:select size="1"
                      name="favorite_category">
     <waf:selected><c:out value="${customer.profile.favoriteCategory}"/></waf:selected>
      <waf:option value="BIRDS">Birds</waf:option>
      <waf:option value="CATS">Cats</waf:option>
      <waf:option value="DOGS">Dogs</waf:option>
     <waf:option value="FISH">Fish</waf:option>
     <waf:option value="REPTILES">Reptiles</waf:option>
    </waf:select>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <waf:checkbox name="mylist_on">
     <waf:checked><c:out value="${customer.profile.myListPreference}"/></waf:checked>
    </waf:checkbox>
   </td>
   <td class="petstore_form" colspan="2">
    Yes, I want to enable the MyList
    feature. <i>MyList makes your favorite items and categories more
    prominent as you shop.</i>
   </td>
  </tr>
  <tr>
   <td class="petstore_form" align="right">
    <waf:checkbox name="banners_on">
     <waf:checked><c:out value="${customer.profile.bannerPreference}"/></waf:checked>
    </waf:checkbox>
   </td>
   <td class="petstore_form" colspan="2">
    Yes, I want to enable the pet
    tips banners. <i>Java Pet Store will display pet tips as you shop, 
    which are based on your favorite items and categories.</i>
   </td>
  </tr>
  </table>
  <input class="petstore_form" type="submit" value="Update">
</waf:form>

