<%--
 % $Id: signin.jsp,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 % Copyright 2000 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2000 Sun Microsystems, Inc. Tous droits r�erv�.
--%>
<%@ page contentType="text/html;charset=SJIS" %>


<%@ page errorPage="errorpage.jsp" %>

  <body bgcolor="white">
    <h2><center>Java �y�b�g������ �f�� �Ƀ��O�C�����ĉ������B</center></h2>
    <br><br><br><br>
    <center>
      <form action="verifysignin">
      <input type="hidden" name="target_screen" value='<%=request.getParameter("target_screen")%>'> 
      <table>
      <tr>
       <td align="center" >
       <table border="0">
       <tr>
        <td><b>���[�U�[ ID:</b></td>
        <td>
          <input type="text" size="15" name="j_username" value="j2ee-���{">
        </td>
       </tr>
       <tr>
        <td><b>�p�X���[�h:</b></td>
         <td>
          <input type="password" size="15" name="j_password" value="j2ee-���{">
        </td>
       </tr>
       <tr>
        <td></td>
        <td align="right">
         <input type="image" border="0" src="<%=request.getContextPath()%>/images/button_submit.gif" name="submit">
        </td>
       </tr>
       <tr>
       <td><br></td>
       </tr>
      <tr>
       <td colspan="3" align="center">
         <a href="createnewaccount">
          �V�K���[�U�[�o�^
        </a>
       </td>
       </tr>
     </table>
      </td>
        </tr>
      </table>
    </form>
    </center>
  </body>
