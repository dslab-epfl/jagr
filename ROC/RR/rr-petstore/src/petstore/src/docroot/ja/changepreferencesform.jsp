<%--
 % $Id: changepreferencesform.jsp,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 % Copyright 2000 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2000 Sun Microsystems, Inc. Tous droits r�erv�.
--%>

<%@ page import="com.sun.j2ee.blueprints.personalization.profilemgr.model.ExplicitInformation" %>
<%@ page contentType="text/html;charset=SJIS" %>

<%--
 % A set of form fields, that prompt for personalization preferences.
 % This expects to be includes in the context of a FORM tag.  Right now
 % the categories for favorite cateogory are hard-coded.  The details are
 % expected to be found in an ExplicitInformation object called explicitInfo.
--%>


<table border="0" cellpadding="0" cellspacing="0">

  <%
      ExplicitInformation explicitInfo = (ExplicitInformation)request.getAttribute("explicitInfo");
      String lang = explicitInfo.getLangPref().toLowerCase();
   %>

  <tr>
    <td></td>
    <td>
      �킽���� PetStore �͎��̌������]���܂��B
      <select name="language" size="1">
        <option
          value="English"
          <% if (lang.equals("english")) { %> selected <% } %>
          >English</option>
        <option
          value="Japanese"
          <% if (lang.equals("japanese")) { %> selected <% } %>
          >Japanese</option>
      </select>
    </td>
  </tr>

  <% String favCat = explicitInfo.getFavCategory().toLowerCase(); %>

  <tr>
    <td></td>
    <td>
      ���̍D���ȃJ�e�S���́A
      <select name="favorite_category" size="1">
        <option
          value="Birds"
          <% if (favCat.equals("birds")) { %> selected <% } %>
          >Birds</option>
        <option
          value="Cats"
          <% if (favCat.equals("cats")) { %> selected <% } %>
          >Cats</option>
        <option
          value="Dogs"
          <% if (favCat.equals("dogs")) { %> selected <% } %>
          >Dogs</option>
        <option
          value="Fish"
          <% if (favCat.equals("fish")) { %> selected <% } %>
          >Fish</option>
        <option
          value="Reptiles"
          <% if (favCat.equals("reptiles")) { %> selected <% } %>
          >Reptiles</option>
      </select>
    </td>
  </tr>

  <tr>
    <td>
      &nbsp;
      <input type=checkbox name="myList_on"
        <% if (explicitInfo.getMyListOpt()) { %> checked <% } %>>
      &nbsp;
    </td>
    <td>MyList �@�\��L���ɂ��邱�Ƃ���]���܂��B  <i>MyList �́A���������̎���
        �ڂɕt���悤�ɁA���C�ɓ���̏��i��J�e�S����\�����܂��B</i></td>
  </tr>

  <tr>
    <td>
      &nbsp;
      <input type=checkbox name="banners_on"
        <% if (explicitInfo.getBannerOpt()) { %> checked <% } %>>
      &nbsp;
    </td>
    <td>�y�b�g�̏��o�i�[�@�\��L���ɂ��邱�Ƃ���]���܂��B  <i>Java �y�b�g������́A
        ���Ȃ��̂��C�ɓ���̏��i��J�e�S�������ƂɁA���������̎��ɁA�y�b�g�̏���\�����܂��B
        </i>
    </td>
  </tr></td>
  </tr>
</table>

