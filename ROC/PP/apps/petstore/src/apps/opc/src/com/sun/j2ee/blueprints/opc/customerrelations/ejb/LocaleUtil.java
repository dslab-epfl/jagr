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

package com.sun.j2ee.blueprints.opc.customerrelations.ejb;

import java.util.Locale;

/**
 *
 */
public class LocaleUtil {


  public static Locale getLocaleFromString(String localeString) {
    if (localeString == null) return null;
    if (localeString.toLowerCase().equals("default")) return Locale.getDefault();
    int languageIndex = localeString.indexOf('_');
    if (languageIndex  == -1) return null;
    int countryIndex = localeString.indexOf('_', languageIndex +1);
    String country = null;
    if (countryIndex  == -1) {
      if (localeString.length() > languageIndex) {
        country = localeString.substring(languageIndex +1, localeString.length());
      } else {
        return null;
      }
    }
    int variantIndex = -1;
    if (countryIndex != -1) countryIndex = localeString.indexOf('_', countryIndex +1);
    String language = localeString.substring(0, languageIndex);
    String variant = null;
    if (variantIndex  != -1) {
      variant = localeString.substring(variantIndex +1, localeString.length());
    }
    if (variant != null) {
      return new Locale(language, country, variant);
    } else {
      return new Locale(language, country);
    }

  }

}


