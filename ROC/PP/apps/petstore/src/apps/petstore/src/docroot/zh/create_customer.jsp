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
 % $Id: create_customer.jsp,v 1.1.1.1 2003/03/07 08:30:30 emrek Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits reserved.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
 
<p class="petstore_title">顾客信息</p>

<form method="POST" action="createcustomer.do">
<input type="hidden" name="action" value="create"/>

<table cellpadding="5" cellspacing="0" width="100%" border="0">
<tr>
<td colspan="3"><p class="petstore_title">联系信息</p></td>
</tr>
<tr>
<td class="petstore_form" align="right"><b>姓</b></td> 

<td align="left" colspan="2"><input class="petstore_form" type="text" name="family_name_a"
size="30" maxlength="30" value="万"></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>名</b></td> 

<td align="left" colspan="2"><input class="petstore_form" type="text" name="given_name_a"
size="30" maxlength="30" value="长城"></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>所在省份</b></td>

<td class="petstore_form" align="left">
<select size="1" name="state_or_province_a"> 
<option value="江苏省" selected>江苏省</option>
<option value="北京市">北京市</option>
<option value="上海市">上海市</option>
</select></td>

<td class="petstore_form"><b>邮政编码</b> <input class="petstore_form" type="text" name="postal_code_a" size="12" maxlength="12" value="123456"></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>所在城市</b></td>

<td align="left" colspan="2"><input class="petstore_form" type="text" name="city_a"
size="55" maxlength="70" value="南京市"></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>详细街道地址</b></td>

<td align="left" colspan="2"><input class="petstore_form" type="text" name="address_1_a"
size="55" maxlength="70" value="秦怀小区 1234"></td>
</tr>

<tr>
<td>&nbsp;</td>

<td align="left" colspan="2"><input class="petstore_form" type="text" name="address_2_a"
size="55" maxlength="70"></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>国家</b></td>

<td class="petstore_form" align="left" colspan="2">
<select size="1" name="country_a"> 
<option value="美国">美国</option> 
<option value="加拿大">加拿大</option> 
<option value="日本">日本</option> 
<option value="中国" selected>中国</option>
</select></td>
</tr>

<tr>
<td class="petstore_form" align="right"><b>电话号码</b></td>

<td align="left" colspan="2"><input class="petstore_form" type="text"
name="telephone_number_a" size="12" maxlength="70"
value="025－4316910"></td>
</tr>
<tr>
<td nowrap="true" class="petstore_form" align="right"><b>E-Mail 地址</b></td>

<td align="left" colspan="2"><input class="petstore_form" type="text"
name="email_a" size="12" maxlength="70"
value="someone@somecompany.com"></td>
</tr>
</table>

<p class="petstore_title">信用卡信息</p>

<table cellpadding="5" cellspacing="0" width="100%" border="0">
<tr>
<td class="petstore_form" align="right"><b>信用卡种类</b></td> 
<td align="left" colspan="2">
<select size="1" name="credit_card_type">
 <option value="Java(TM) Card">Java(TM) Card</option>
 <option value="Duke Express">Duke Express</option>
 <option value="Blue Jay Club">Meow Card</option>
</select>
</td>
</tr>
<tr>
<td class="petstore_form" align="right"><b>卡号</b></td> 
<td align="left" colspan="2"><input class="petstore_form" type="text" name="credit_card_number"
size="30" maxlength="30" value="0100-0001-0001"></td>
</tr>
<tr>
<td class="petstore_form" align="right"><b>有效期限</b></td> 
<td align="left" colspan="2"> 
<select size="1" name="credit_card_expiry_year">
 <option value="2001">2001</option>
 <option value="2002">2002</option>
 <option value="2003">2003</option>
 <option value="2004">2004</option>
</select>
年
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
月
</td>
</tr>
</table>

<p class="petstore_title">概要信息</p>

<table border="0" cellpadding="5" width="100%" cellspacing="0">

<tr>
<td></td>
<td>
我喜欢的 PetStore 语言界面是
<select name="language" size="1">
 <option value="en_US">英语</option>
 <option value="ja_JP">日文</option>
 <option value="zh_CN" selected>中文</option>
</select>
</td>
</tr>
<tr>
<td></td>
<td>
我喜欢的宠物种类是
<select name="favorite_category" size="1">
 <option value="BIRDS" selected>鸟</option>
 <option value="CATS">猫</option>
 <option value="DOGS">狗</option>
 <option value="FISH">鱼</option>
 <option value="REPTILES">爬虫类</option>
</select>
</td>
</tr>

<tr>
<td>
&nbsp;
<input type=checkbox name="mylist_on" checked >
&nbsp;
</td>
<td>是的，我希望使用MyList功能。  <i> MyList将在你
每次购物时更明显地提供给你喜欢的商品的信息</i></td>
</tr>

<tr>
<td>
&nbsp;
<input type=checkbox name="banners_on" checked >
&nbsp;
</td>
<td>是的，我希望使用Pet Banner的提示功能。  <i>它将在你每次购物时显示你喜欢的商品或种类
的提示信息</i>
</td>
</tr></td>
</tr>
</table>

<input class="petstore_form" type="submit" value="提交">
</form>
