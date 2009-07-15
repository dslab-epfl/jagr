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
 % $Id: create_customer.jsp,v 1.1 2004/02/04 10:06:21 emrek Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits rÈservÈs.
--%>

<p class="petstore_title">Your Account Information</p>

<form method="POST" action="createcustomer.do">
<input type="hidden" name="action" value="create"/>

<table cellpadding="5" cellspacing="0" width="100%" border="0">
<tr>
<td colspan="3"><p class="petstore_title">Contact Information</p></td>
</tr>
<tr>
<td class="petstore_form" align="right"><b>First Name</b></td> 

<td align="left" colspan="2"><input class="petstore_form" type="text" name="given_name_a"
size="30" maxlength="30" value="Duke"></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>Last Name</b></td> 

<td align="left" colspan="2"><input class="petstore_form" type="text" name="family_name_a"
size="30" maxlength="30" value="BluesPrints"></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>Street Address</b></td>

<td align="left" colspan="2"><input class="petstore_form" type="text" name="address_1_a"
size="55" maxlength="70" value="1234 Moon Way"></td>
</tr>

<tr>
<td>&nbsp;</td>

<td align="left" colspan="2"><input class="petstore_form" type="text"
name="address_2_a" size="55" maxlength="70"></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>City</b></td>

<td align="left" colspan="2"><input class="petstore_form" type="text" name="city_a"
size="55" maxlength="70" value="Sunville"></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>State/Province</b></td>

<td class="petstore_form" align="left"><select size="1"
name="state_or_province_a"> <option
value="California">California</option> <option value="New York">New
York</option> <option value="Texas">Texas</option> </select></td>

<td class="petstore_form"><b>Postal Code</b> <input
class="petstore_form" type="text" name="postal_code_a" size="12"
maxlength="12" value="10001"></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>Country</b></td>

<td class="petstore_form" align="left" colspan="2"><select size="1"
name="country_a"> <option value="USA">United States</option> <option
value="Canada">Canada</option> <option value="Japan">Japan</option>
<option value="China">China</option>
</select></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>Telephone Number</b></td>

<td align="left" colspan="2"><input class="petstore_form" type="text"
name="telephone_number_a" size="12" maxlength="70"
value="408-555-5555"></td>
</tr>
<tr>
<td class="petstore_form" align="right"><b>E-Mail</b></td>

<td align="left" colspan="2"><input class="petstore_form" type="text"
name="email_a" size="12" maxlength="70"
value="aaa@bbb.ccc"></td>
</tr>

<tr>
<td colspan="3"><p class="petstore_title">Credit Card
Information</p></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>Credit Card Number</b></td> 

<td align="left" colspan="2"><input class="petstore_form" type="text"
name="credit_card_number" size="30" maxlength="30"
value="0100-001-0001"></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>Card Type</b></td> 

<td class="petstore_form" align="left" colspan="2">
<select size="1" name="credit_card_type">
 <option value="Java(TM) Card">Java(TM) Card</option>
 <option value="Duke Express">Duke Express</option>
 <option value="Meow Club">Meow Card</option>
</select>
</td>
</tr>
<tr>
<td class="petstore_form" align="right"><b>Expiry Date</b></td> 
<td class="petstore_form" align="left" colspan="2">
<select size="1" name="credit_card_expiry_month">
 <option value="01">01</option>
 <option value="02">02</option>
 <option value="03">03</option>
 <option value="04">04</option>
 <option value="05">05</option>
 <option value="06">06</option>
 <option value="07">07</option>
 <option value="08">08</option>
 <option value="09">09</option>
 <option value="10">10</option>
 <option value="11">11</option>
 <option value="12">12</option>
</select>
/
<select size="1" name="credit_card_expiry_year">
 <option value="2001">2001</option>
 <option value="2002">2002</option>
 <option value="2003">2003</option>
 <option value="2004">2004</option>
</select>
</td>
</tr>

<tr><td colspan="3"><p class="petstore_title">Profile
Information</p></td></tr>

<tr>
<td>&nbsp;</td>

<td class="petstore_form" colspan="2">I want MyPetStore to be in
<select name="language" size="1">
 <option value="en_US" selected>English</option>
 <option value="ja_JP">Japanese</option>
 <option value="zh_CN">Chinese</option>
</select>
</td>
</tr>

<tr>
<td>&nbsp;</td>

<td class="petstore_form" colspan="2">My favorite category is
<select name="favorite_category" size="1">
 <option value="BIRDS" selected>Birds</option>
 <option value="CATS">Cats</option>
 <option value="DOGS">Dogs</option>
 <option value="FISH">Fish</option>
 <option value="REPTILES">Reptiles</option>
</select>
</td>
</tr>

<tr>
<td class="petstore_form" align="right"><input type=checkbox
name="mylist_on"></td> 

<td class="petstore_form" colspan="2">Yes, I want to enable the MyList
feature. <i>MyList makes your favorite items and categories more
prominent as you shop.</i></td>
</tr>

<tr>
<td class="petstore_form" align="right"><input type=checkbox
name="banners_on"></td>

<td class="petstore_form" colspan="2">Yes, I want to enable the pet
tips banners. <i>Java Pet Store will display pet tips as you shop,
which are based on your favorite items and categories.</i></td>
</tr>

</td>
</tr>
</table>

<input class="petstore_form" type="submit" value="Submit">
</form>

