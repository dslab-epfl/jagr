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

<%@ taglib uri="/WEB-INF/waftags.tld" prefix="waf" %>
<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>

<p class="petstore_title">Your Account Information</p>


<table cellpadding="5" cellspacing="0" width="100%" border="0">
<tr>
<td colspan="3"><p class="petstore_title">Contact Information</p></td>
</tr>
<tr>
<td class="petstore_form" align="right"><b>First Name</b></td> 

<td class="petstore_form" align="left"
colspan="2"><c:out value="${customer.account.contactInfo.givenName}"/></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>Last Name</b></td> 

<td class="petstore_form" align="left" colspan="2"><c:out value="${customer.account.contactInfo.familyName}"/></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>Street Address</b></td>

<td class="petstore_form" align="left" colspan="2">
<c:out value="${customer.account.contactInfo.address.streetName1}"/>
</td>
</tr>

<tr>
<td>&nbsp;</td>

<td class="petstore_form" align="left"
colspan="2"><c:out value="${customer.account.contactInfo.address.streetName2}"/></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>City</b></td>

<td class="petstore_form" align="left" colspan="2">
<c:out value="${customer.account.contactInfo.address.city}"/>
</td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>State/Province</b></td>

<td class="petstore_form" align="left"><c:out value="${customer.account.contactInfo.address.state}"/></td>

<td class="petstore_form"><b>Postal Code</b> <c:out value="${customer.account.contactInfo.address.zipCode}"/></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>Country</b></td>

<td class="petstore_form" align="left"
colspan="2"><c:out value="${customer.account.contactInfo.address.country}"/>
</td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>Telephone Number</b></td>

<td class="petstore_form" align="left"
colspan="2"><c:out value="${customer.account.contactInfo.telephone}"/>
</td>
</tr>
<tr>
<td class="petstore_form" align="right"><b>E-Mail</b></td>

<td class="petstore_form" align="left"
colspan="2"><c:out value="${customer.account.contactInfo.email}"/></td>
</tr>

<tr><td colspan="3"><p class="petstore_title">Credit Card
Information</p></td></tr>

<tr>
<td class="petstore_form" align="right"><b>Card Type</b></td> 
<td class="petstore_form" align="left" colspan="2">
<c:out value="${customer.account.creditCard.cardType}"/>
</td>
</tr>
<tr>
<td class="petstore_form" align="right"><b>Card Number</b></td> 

<td class="petstore_form" align="left"
colspan="2"><c:out value="${customer.account.creditCard.cardNumber}"/>
</td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>Card Expiry Date</b></td> 

<td class="petstore_form" align="left" colspan="2">
<c:out value="${customer.account.creditCard.expiryMonth}"/> / <c:out value="${customer.account.creditCard.expiryYear}"/>
</td>
</tr>

<tr>
<td colspan="3"><p class="petstore_title">Profile Information</p></td>
</tr>
<tr>
<td>&nbsp;</td>
<td class="petstore_form" colspan="2">
I want MyPetStore to be in
<waf:select size="1" name="language" editable="false">
  <waf:selected><c:out value="${customer.profile.preferredLanguage}"/></waf:selected>
  <waf:option value="en_US">English</waf:option>
  <waf:option value="ja_JP">Japanese</waf:option>
  <waf:option value="zh_CN">Chinese</waf:option>
</waf:select>
</td>
</tr>
<tr>
<td>&nbsp;</td>
<td class="petstore_form" colspan="2">
My favorite category is
<b>
<waf:select size="1" name="favorite_category" editable="false">
  <waf:selected><c:out value="${customer.profile.favoriteCategory}"/></waf:selected>
  <waf:option value="BIRDS">Birds</waf:option>
  <waf:option value="CATS">Cats</waf:option>
  <waf:option value="DOGS">Dogs</waf:option>
  <waf:option value="FISH">Fish</waf:option>
  <waf:option value="REPTILES">Reptiles</waf:option>
</waf:select>
</b>
</td>
</tr>

<tr>
<td class="petstore_form" align="right">

<c:choose>
  <c:when test="${customer.profile.myListPreference == true}">
   <font size="+1" color="green">Yes</font>
  </c:when>
  <c:otherwise>
   <font size="+1" color="red">No</font>
  </c:otherwise>
</c:choose>
</td>
<td class="petstore_form" colspan="2">I want to enable the MyList
feature. <i>MyList makes your favorite items and categories more
prominent as you shop.</i></td>
</tr>

<tr>
<td class="petstore_form" align="right">
<c:choose>
  <c:when test="${customer.profile.bannerPreference == true}">
   <font size="+1" color="green">Yes</font>
  </c:when>
  <c:otherwise>
   <font size="+1" color="red">No</font>
  </c:otherwise>
</c:choose>
</td>
<td class="petstore_form" colspan="2">I want to enable the Pet Tips
banners.  <i>Java Pet Store will display pet tips as you shop, which
are based on your favorite items and categories.</i>
</td>
</tr></td>
</tr>
</table>

<p class="petstore"><a href="update_customer.screen">Edit Your Account
Information</a></p>

