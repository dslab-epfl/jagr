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
package com.sun.j2ee.blueprints.petstore.controller.web;

/**
 *
 * This class relates to some web tier specific display selection where
 * this helper will choose the banner to be displayed based on the category.
 *  The mapping of banners to categories could also be maintained in
 *  a properties file or database.
 *
 * For simplicity the names are mapped directly in this class.
 */
public class BannerHelper implements java.io.Serializable {

    private String categoryId = "";

    public BannerHelper() {
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId.toLowerCase();
    }

    public String getBanner() {

        if (categoryId.equals("dogs")) {
            return "banner_dogs.gif";
        } else if (categoryId.equals("cats")) {
            return "banner_cats.gif";
        } else if (categoryId.equals("reptiles")) {
            return "banner_reptiles.gif";
        } else if (categoryId.equals("birds")) {
            return "banner_birds.gif";
        } else if (categoryId.equals("fish")) {
            return "banner_fish.gif";
        }
        return "banner_dogs.gif";
    }


}

