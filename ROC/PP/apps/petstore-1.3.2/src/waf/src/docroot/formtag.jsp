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


<%@ taglib uri="/WEB-INF/waftags.tld" prefix="waf" %>
<html>


<font size="+3">Form Tag</font>
<br>
<br>
<p>The form tag implements rudimentary validatition or
text specified using <a href="inputtag.screen">&lt;input&gt;</a> tags.</p>

<p>It has three parameters:</p>

<table border="1">
 <tr>
  <th>Name</th>
  <th>Purpose</th>
 </tr>
 <tr>
  <td>method</td> 
  <td>The HTTP method to use for this form. This can either be POST or GET.</td>
 </tr>
 <tr>
  <td>action</td>
  <td>The URL that will process this form.</td>
 </tr>
 <tr>
  <td>name</td>
  <td>The name identifying the form on the page.</td>
 </tr>
</table>

<br/><br/>
Example:
<br/>
<pre>
&lt;waf:form method="POST" action="customer.do" name="customerform"&gt;
 &lt;waf:input cssClass="petstore_form"
                       type="text"
                     name="address_1_a"
                       size="55"
             maxlength="70"
               validation="validation"&gt;
  &lt;waf:value&gt;
   &lt;c:out value="${customer.account.contactInfo.address.streetName1}"/&gt;
 &lt;/waf:value&gt;
 &lt;/waf:input&gt;
&lt;/waf:form&gt;
</pre>
<br/>
This tag will create a form with the name customer form that will validate whether or
not text has been inputed into the address_1_a input field. This is done using JavaScript
that is generated by this tag for each input tag that has validation turned on.
</html>