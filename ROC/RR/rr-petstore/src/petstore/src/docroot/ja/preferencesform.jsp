<%--
 % A set of form fields, that prompt for personalization preferences.  
 % This expects to be includes in the context of a FORM tag.  Right now
 % the categories for favorite cateogory are hard-coded.
--%>

<%@ page contentType="text/html;charset=SJIS" %>


<table border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td></td>
    <td>
      �킽���� PetStore �͎��̌������]���܂��B
      <select name="language" size="1">
	<option value="English">English</option>
        <option value="Japanese" selected>Japanese</option>
      </select>
    </td>
  </tr>

  <tr>
    <td></td>
    <td>
      ���̍D���ȃJ�e�S���́A
      <select name="favorite_category" size="1">
        <option value="birds" selected>Birds</option>
	<option value="cats">Cats</option>
        <option value="dogs">Dogs</option>
	<option value="fish">Fish</option>
	<option value="reptiles">Reptiles</option>
      </select>
    </td>
  </tr>

  <tr>
    <td>
      &nbsp;
      <input type=checkbox name="myList_on" checked>
      &nbsp;
    </td>
    <td>MyList �@�\��L���ɂ��邱�Ƃ���]���܂��B  <i>MyList �́A���������̎���
        �ڂɕt���悤�ɁA���C�ɓ���̏��i��J�e�S����\�����܂��B</i></td>
  </tr>

  <tr>
    <td>
      &nbsp;
      <input type=checkbox name="banners_on" checked>
      &nbsp;
    </td>
    <td>�y�b�g�̏��o�i�[�@�\��L���ɂ��邱�Ƃ���]���܂��B  <i>Java �y�b�g������́A
        ���Ȃ��̂��C�ɓ���̏��i��J�e�S�������ƂɁA���������̎��ɁA�y�b�g�̏���\�����܂��B
        </i>
</td>
  </tr>
</table>

