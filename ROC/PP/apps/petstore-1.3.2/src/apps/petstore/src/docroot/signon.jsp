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
 % $Id: signon.jsp,v 1.1 2004/02/04 10:06:21 emrek Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits r‚Äö√†√∂‚àö‚Ä†serv‚Äö√†√∂‚àö‚Ä†s.
--%>

<%@ taglib uri="/WEB-INF/waftags.tld" prefix="waf" %>
<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>

<p class="petstore_title">Sign In</p>
<p class="petstore"><b>Are you a returning customer?</b></p>

<table cellpadding="20" cellspacing="0" border="1">
 <tr>
  <td valign="top">
   <waf:form  name="existingcustomer" action="j_signon_check" method="POST">
    <table cellpadding="5" cellspacing="0" border="0">
     <tr>
      <td class="petstore" align="center" colspan="2">
       <b>Yes.</b>
      </td>
     </tr>
     <tr>
      <td class="petstore_form" align="right">
       <b>User Name:</b>
      </td>
      <td class="petstore_form">
      <c:choose>
      <c:when  test="${cookie['bp_signon'] != null && cookie['bp_signon'] !=''}">
       <waf:input cssClass="petstore_form"
                             type="text"
                              size="15"
                           name="j_username"
                    validation="validation">
       <waf:value><c:out value="${cookie['bp_signon'].value}"/></waf:value>
      </waf:input>
     </td>
    </tr>
    <tr>
     <td class="petstore_form" align="right">
      <b>Password:</b>
     </td>
     <td class="petstore_form">
       <waf:input cssClass="petstore_form"
                           type="password"
                            size="15"
                          name="j_password"
                   validation="validation"
                           value=""/>
     </td>
    </tr>
    </c:when>
   <c:otherwise>
    <waf:input cssClass="petstore_form"
                              type="text"
                               size="15"
                            name="j_username"
                     validation="validation"
                             value="j2ee"/>
     </td>
    </tr>
    <tr>
     <td class="petstore_form" align="right">
      <b>Password:</b>
     </td>
     <td class="petstore_form">
      <waf:input cssClass="petstore_form"
                                type="password"
                                 size="15"
                               name="j_password"
                        validation="validation"
                              value="j2ee"/>
      </td>
     </tr>
  </c:otherwise>
 </c:choose>
     <tr>
      <td align="center" colspan="2">
       <input class="petstore_form" name="submit" type="submit" value="Sign In">
      </td>
     </tr>
     <tr>
      <td align="center" colspan="2">
       Remember My User Name 
       <waf:checkbox name="j_remember_username">
        <waf:checked><c:out value="${cookie['bp_signon'] != null && cookie['bp_signon'] !=''}"/></waf:checked>
       </waf:checkbox>
      </td>
     </tr>
    </table>
   </waf:form>
  </td>
  <td valign="top">
  <waf:form name="newcustomer" action="createuser.do" method="POST">
   <table cellpadding="5" cellspacing="0" border="0">
    <tr>
     <td class="petstore" align="center" colspan="2">
      <b>No. I would like to sign up for an account.</b>
     </td>
    </tr>
    <tr>
     <td class="petstore_form" align="right">
      <b>User Name:</b>
     </td>
     <td class="petstore_form">
      <waf:input cssClass="petstore_form"
                                type="text"
                                 size="15"
                       validation="validation"
                              name="j_username"/>
     </td>
    </tr>
    <tr>
     <td class="petstore_form" align="right">
      <b>Password:</b>
     </td>
     <td class="petstore_form">
      <waf:input cssClass="petstore_form" 
                                type="password"
                                 size="15"
                       validation="validation"
                              name="j_password"/>
     </td>
    </tr>
    <tr>
     <td class="petstore_form" align="right">
      <b>Password (Repeat):</b>
     </td>
     <td class="petstore_form">
      <waf:input cssClass="petstore_form"
                                type="password"
                                 size="15"
                        validation="validation"
                               name="j_password_2"/>
      </td>
     </tr>
     <tr>
      <td align="center" colspan="2">
       <input class="petstore_form"
                  name="submit"
                   type="submit"
                  value="Create New Account"/>
       </td>
      </tr>
     </table>
    </waf:form>
   </td>
 </tr>
</table>

