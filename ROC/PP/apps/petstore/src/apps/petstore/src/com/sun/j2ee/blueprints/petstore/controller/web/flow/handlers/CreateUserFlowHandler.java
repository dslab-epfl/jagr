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

package com.sun.j2ee.blueprints.petstore.controller.web.flow.handlers;


// J2EE imports
import javax.servlet.http.HttpServletRequest;

// SignOn imports
import com.sun.j2ee.blueprints.signon.web.SignOnFilter;

// WAF imports
import com.sun.j2ee.blueprints.waf.controller.web.flow.FlowHandler;
import com.sun.j2ee.blueprints.waf.controller.web.flow.FlowHandlerException;

/**
 * This class will forward a user to the screen they left before they
 * attempted to create an account.
 *
 * It is assumed that the SignOn component is used which provides an
 * attribute of which screen to return to.
 *
*/
public class CreateUserFlowHandler implements FlowHandler {

    public void doStart(HttpServletRequest request) {
    }

    public String processFlow(HttpServletRequest request)
        throws FlowHandlerException {

        String forwardScreen = (String)request.getSession().getAttribute(SignOnFilter.ORIGINAL_URL);
        return forwardScreen;
    }


    public void doEnd(HttpServletRequest request) {
    }

}

