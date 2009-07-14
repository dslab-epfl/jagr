<%--
 % $Id: mylist.jsp,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 % Copyright 2000 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2000 Sun Microsystems, Inc. Tous droits r蜩erv蜩.
--%>

<%--
 %   Displays the pet favorites of the user. The favorites list
 %   can be included in any web page. Currently it is included in
 %   the cart screen, when the customer has the opportunity to
 %   add more items just before checkout.
--%>
<%@ page contentType="text/html;charset=SJIS" %>
<%@ taglib uri="/WEB-INF/tlds/taglib.tld" prefix="j2ee" %>


<j2ee:myList numItems="3">
  <table border="0" bgcolor="#336666" width="100%">
    <tr background="../images/bkg-topbar.gif">
      <td>
        <center>
          <font color="white" size="+1">お気に入りのペット</font>
          <br><font color="white" size="+0">ここであなたのお気に入りのペットを購入できます。</font>
        </center>
      </td>
    </tr>
    <tr colspan="2" bgcolor="#eeebcc">
      <td>
        <j2ee:items>
	  <p>
	  <a href='product?product_id=<j2ee:productAttribute attribute="id"/>'
	    ><j2ee:productAttribute/>
	  </a> (<j2ee:productAttribute attribute="id"/>)
	  </p>
        </j2ee:items>
      </td>
    </tr>

    <tr>
        <j2ee:prevForm action="cart">
          <td align="left">
           <input type="image" border="0" src="../images/button_prev.gif" value="Prev">
          </td>
        </j2ee:prevForm>
        <j2ee:nextForm action="cart">
          <td align="right">
           <input type="image" border="0" src="../images/button_more.gif" value="Next">
         </td>
        </j2ee:nextForm>
    </tr>
  </table>
</j2ee:myList>

