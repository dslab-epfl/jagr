<%--
 % $Id: splash.jsp,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 % Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 % Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
--%>

    <map name="estoremap">
      <area href="category?category_id=BIRDS" alt="Birds" coords="72,2, 280,250">
      <area href="category?category_id=FISH" alt="Fish" coords="2, 180,72,250">
      <area href="category?category_id=DOGS" alt="Dogs" coords="60,250,130,320">
      <area href="category?category_id=REPTILES" alt="Reptiles" coords="140,270,210,340">
      <area href="category?category_id=CATS" alt="Cats" coords="225,240,295,310">
      <area href="category?category_id=BIRDS" alt="Birds" coords="280,180,350,250">
    </map>
    <image src="<%=request.getContextPath()%>/images/splash.gif"
      usemap="#estoremap" width="350" height="355" border="0">
