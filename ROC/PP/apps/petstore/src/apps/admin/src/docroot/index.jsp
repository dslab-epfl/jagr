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

 $Id: index.jsp,v 1.1.1.1 2003/03/07 08:30:30 emrek Exp $
 Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 Copyright 2001 Sun Microsystems, Inc. Tous droits réservés. 
--%>

<html>
  <head>
    <title>Java[tm] Pet Store Admin Page</title>
  </head>
  <body bgcolor="white">
    <center>
      <h1>Java Pet Store Admin Page</h1>
    </center>
    <center><hr width="100%"></center>
    <p>
    <p>
    <p>
    The Java Pet Store Admin Client showcases how the J2EE platform technologies
    along with the Java WebStart technology can be used to deploy Rich client
    interface for customers. For more information on Java WebStart please refer
    to <a href="http://java.sun.com/products/javawebstart/">
    http://java.sun.com/products/javawebstart/</a>.
    <br>
    <br>
    In this sample application, we showcase how the Java Pet Store 
    administrator can have the facility of a rich client to get information of
    sales, revenue, orders with specified state. The administrator can also 
    approve / deny pending orders.
    <br>
    <br>
    <form action="AdminRequestProcessor" method=post>
    <p>
    <p>
    <p>
     To launch the JAVA Web Start based Rich Client click here :
    <input type="hidden" name="currentScreen" value="manageorders">
    <input type="submit" value="Launch Rich Client">
    </form>
    <form action="AdminRequestProcessor" method=post>
    <p>
    <p>
    <p>
     To logout of Pet Store Admin click here :
    <input type="hidden" name="currentScreen" value="logout">
    <input type="submit" value="logout">
    </form>
  </body>
</html>
