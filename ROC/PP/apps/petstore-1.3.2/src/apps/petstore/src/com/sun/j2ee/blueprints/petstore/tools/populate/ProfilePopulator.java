/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
 * OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of
 * any nuclear facility.
 */

package com.sun.j2ee.blueprints.petstore.tools.populate;

import javax.naming.*;
import javax.ejb.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import com.sun.j2ee.blueprints.customer.profile.ejb.*;


public class ProfilePopulator {
  public static final String JNDI_PROFILE_HOME = "java:comp/env/ejb/Profile";
  private static final String XML_PROFILE = "Profile";
  private static final String XML_PREFERREDLANGUAGE = "PreferredLanguage";
  private static final String XML_FAVORITECATEGORY = "FavoriteCategory";
  private static final String XML_MYLISTPREFERENCE = "MyListPreference";
  private static final String XML_BANNERPREFERENCE = "BannerPreference";
  private String rootTag;
  private ProfileLocalHome profileHome = null;
  private ProfileLocal profile ;

  public ProfilePopulator(String rootTag) {
    this.rootTag = rootTag;
    return;
  }

  public XMLFilter setup(XMLReader reader) throws PopulateException {
    return new XMLDBHandler(reader, rootTag, XML_PROFILE) {

      public void update() throws PopulateException {}

      public void create() throws PopulateException {
        profile = createProfile(getValue(XML_PREFERREDLANGUAGE),
                                getValue(XML_FAVORITECATEGORY),
                                getValue(XML_MYLISTPREFERENCE, false),
                                getValue(XML_BANNERPREFERENCE, false));
        return;
      }
    };
  }

  public boolean check() throws PopulateException {
    return true;
  }

  private ProfileLocal createProfile(String preferredLanguage, String favoriteCategory, boolean myListPreference, boolean bannerPreference) throws PopulateException {
    try {
      if (profileHome == null) {
        InitialContext context = new InitialContext();
        profileHome = (ProfileLocalHome) context.lookup(JNDI_PROFILE_HOME);
      }
      return profileHome.create(preferredLanguage, favoriteCategory, myListPreference, bannerPreference);
    } catch (Exception exception) {
      throw new PopulateException ("Could not create: " + exception.getMessage(), exception);
    }

  }

  public ProfileLocal getProfile() {
    return profile;
  }
}




