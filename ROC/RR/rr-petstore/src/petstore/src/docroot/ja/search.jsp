<%--
 % $Id: search.jsp,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 % Copyright 2000 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2000 Sun Microsystems, Inc. Tous droits r?serv?s. 
--%>

<%--
 % Displays the results of a search 
--%>

<%@ page contentType="text/html;charset=SJIS" %>
<%@ taglib uri="/WEB-INF/tlds/taglib.tld" prefix="j2ee" %>
<%@ page import="com.sun.j2ee.blueprints.petstore.util.JSPUtil" %>

<j2ee:searchList numItems="4" searchText='<%=JSPUtil.convertJISEncoding(request.getParameter("search_text"))%>' emptyList="�Y�����鏤�i�͌�����܂���B">

   <table border="0" bgcolor="#336666">
      <tr background="../images/bkg-topbar.gif">
	<th><font color="white" size="3">���i�R�[�h</font></th>
	<th><font color="white" size="3">���i��</font></th>
	<th><font color="white" size="3">����</font></th>
      </tr>

      <j2ee:items>

      <tr bgcolor="#eeebcc">
	<td><j2ee:productAttribute attribute="id"/></td>
	<td>
	  <a href="product?product_id=<j2ee:productAttribute attribute="id"/>">
	    <j2ee:productAttribute attribute="name"/>
	  </a>
	</td>
	<td><j2ee:productAttribute attribute="description"/></td>
      </tr>

     </j2ee:items>

   <tr>
     <j2ee:prevForm action="search">
         <td colspan="2" align="left">
          <input type="hidden" name="search_text" value="<%=request.getParameter("search_text")%>">
          <input type="image" border="0" src="../images/button_prev.gif" value="Prev">
         </td>
    </j2ee:prevForm>

    <j2ee:nextForm action="search">
      <td colspan="4" align="right">
          <input type="hidden" name="search_text" value="<%=request.getParameter("search_text")%>">
          <input type="image" border="0" src="../images/button_more.gif" value="Next">
       </td>
        </j2ee:nextForm>

    </tr>

    </table>
 

</j2ee:searchList>

