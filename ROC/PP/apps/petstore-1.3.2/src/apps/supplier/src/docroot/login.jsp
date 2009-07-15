<!--
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
-->

<%--
 % $Id: login.jsp,v 1.1 2004/02/04 10:06:24 emrek Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits réservés. 
--%>

<%--
 % The login form, as required by the form based login mechanism.
 % Note that this form does NOT go through the templating mechanism.
--%>

  <body bgcolor="white">
    <h2><center>Please sign into Java Pet Store Supplier Module</center></h2>
    <br><br><br><br>
    <center>
      <form action="j_security_check" method=post>
      <table>
      <tr>
       <td align="center" >
       <table border="0">
       <tr>
        <td><b>User ID:</b></td>
        <td>
          <input type="text" size="15" name="j_username" value="supplier"> 
        </td>
       </tr>
       <tr>
        <td><b>Password:</b></td>
         <td> 
          <input type="password" size="15" name="j_password" value="supplier">
        </td>
       </tr>
       <tr>
        <td></td>
        <td align="right"> 
         <input type="image" border="0" src="images/button_submit.gif" name="submit">
        </td>
       </tr>
       <tr>
        <td><br></td>
       </tr>
       </table>
       </td>
      </tr>
      </table>
    </form>
    </center>
  </body>

