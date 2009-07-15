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
 % $Id: banner.jsp,v 1.1 2004/02/04 10:06:21 emrek Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
--%>

<%@ taglib prefix="c" uri="/WEB-INF/c.tld" %>
<%@ taglib prefix="waf" uri="/WEB-INF/waftags.tld" %>


 <table bgcolor="#FFFFFF" width="100%" cellpadding="0" cellspacing="0" border="0">
  <tr>
   <td align="left" valign="middle">
    <a href="main.screen">
     <img src="images/banner_logo.gif" alt="Java Pet Store Demo logo" border="0">
    </a>
   </td>
   <td class="petstore" align="right" valign="middle">
     <form action="search.screen">
      <input class="petstore_listing" type="text" name="keywords" size="8">
      <input class="petstore_listing" type="submit" value="Search">
    </form>
    <br>
    <a href="customer.do">Account</a> | <a href="cart.do">
     Cart
    </a>
     |
    <c:choose>
     <c:when test="${j_signon == true}"><a href="signoff.do">Sign out</a></c:when>
     <c:otherwise><a href="signon_welcome.screen">Sign in</a></c:otherwise>
    </c:choose>
  </td>
 </tr>
 <tr>
  <td align="right" colspan="3">
    <table>
     <tr>
      <td>
       <waf:client_cache_link   id="petstore"
                              targetURL="changelocale.do"
                                           alt="Change the Language to English"
  encodeRequestParameters="true" 
    encodeRequestAttributes="true"
                               imageURL="images/us_flag.gif">
        <waf:param name="locale" value="en_US"/>
       </waf:client_cache_link>
      </td>
      <td>&nbsp;</td>
      <td align="middle">
      <waf:client_cache_link   id="petstore"
                              targetURL="changelocale.do"
                                           alt="Change the Language to Japanese"
  encodeRequestParameters="true" 
   encodeRequestAttributes="true"
                              imageURL="images/ja_flag.gif">
      <waf:param name="locale" value="ja_JP"/>
      </waf:client_cache_link>
     </td>
     <td>&nbsp;</td>
      <td align="right">
      <waf:client_cache_link   id="petstore"
                              targetURL="changelocale.do"
                                           alt="Change the Language to Chinese"
  encodeRequestParameters="true" 
   encodeRequestAttributes="true"
                              imageURL="images/zh_flag.gif">
      <waf:param name="locale" value="zh_CN"/>
      </waf:client_cache_link>
     </td>
    </tr>
   </table>
   </td>
  </tr>
  <tr>
   <td colspan="2">
    <hr noshade="noshade" size="1">
   </td>
  </tr>
 </table>