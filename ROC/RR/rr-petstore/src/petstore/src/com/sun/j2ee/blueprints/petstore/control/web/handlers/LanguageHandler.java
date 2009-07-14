/*
 * $Id: LanguageHandler.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r?erv?.
 */

package com.sun.j2ee.blueprints.petstore.control.web.handlers;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import javax.servlet.http.HttpSession;

import javax.servlet.http.HttpServletRequest;

import com.sun.j2ee.blueprints.petstore.control.event.EStoreEvent;
import com.sun.j2ee.blueprints.petstore.control.event.LanguageChangeEvent;
import com.sun.j2ee.blueprints.util.tracer.Debug;
import com.sun.j2ee.blueprints.petstore.util.WebKeys;
import com.sun.j2ee.blueprints.petstore.util.JSPUtil;


/**
 * LanguageHandler
 *
*/
public class LanguageHandler extends RequestHandlerSupport {

    public EStoreEvent processRequest(HttpServletRequest request){
        String language = request.getParameter("language");
        Debug.println("Changing Language to " + language);
        Locale locale = JSPUtil.getLocaleFromLanguage(language);
        request.getSession().setAttribute(WebKeys.LanguageKey, locale);
        return new LanguageChangeEvent();
    }
}
