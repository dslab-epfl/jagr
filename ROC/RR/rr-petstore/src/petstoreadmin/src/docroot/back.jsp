<!--
 % $Id: back.jsp,v 1.1.1.1 2002/10/03 21:17:37 candea Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits r&eacute;serv&eacute;s.
-->

<html>
  <head>
    <title>Estore Admin Page</title>
  </head>
  <body bgcolor="white">
    <center>
      <h1>Estore Admin Page</h1>
    </center>
    <center><hr width="100%"></center>
    Order Status updated successfully !!!!!
    <form action="AdminRequestProcessor" method=post>
    <p>
    <p>
    <p>
     To update order status click here :
    <input type="hidden" name="currentScreen" value="manageorders">
    <input type="submit" value="Update Order Status">
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
