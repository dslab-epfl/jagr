<%--
 % $Id: addressform.jsp,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s. 
--%>

<%--
 % A set of form fields, that prompt for an address.  This expects to
 % be includes in the context of a FORM tag.
--%>
<%@ page contentType="text/html;charset=SJIS" %>

<table>
  <tr>
    <td align="right">����:</td>
    <td align="left" colspan="2">
      <input type="text" name="family_name" size="30" maxlength="30">
    </td>
  </tr>
  <tr>
    <td align="right">���O:</td>
    <td align="left" colspan="2">
      <input type="text" name="given_name" size="30" maxlength="30">
    </td>
  </tr>
  <tr>
    <td align="right">�����Ԓn:</td>
    <td align="left" colspan="2">
      <input type="text" name="address_1" size="55" maxlength="70">
    </td>
  </tr>
  <tr>
    <td></td>
    <td align="left" colspan="2">
      <input type="text" name="address_2" size="55" maxlength="70">
    </td>
  </tr>
  <tr>
    <td align="right">�s������:</td>
    <td align="left" colspan="2">
      <input type="text" name="city" size="55" maxlength="70">
    </td>
  </tr>
  <tr>
    <td>�B��/�s���{����:</td>
    <td align="left">
      <select size="1" name="state_or_province">
        <option value="�����s">�����s</option>
        <option value="���{">���{</option>
        <option value="���쌧">���쌧</option>
      </select>
    </td>
    <td>�X�֔ԍ�: 
      <input type="text" name="postal_code" size="12" maxlength="12">
    </td>
  </tr>
  <tr>
    <td>����:</td>
    <td align="left" colspan="2">
      <select size="1" name="country">
        <option value="USA">USA</option>
        <option value="Canada">Canada</option>
        <option value="Japan" selected>Japan</option>
      </select>
    </td>
  </tr>
  <tr>
    <td>�d�b�ԍ�:</td>
    <td align="left" colspan="2">
      <input type="text" name="telephone_number" size="12" maxlength="70">
    </td>
  </tr>
</table>
