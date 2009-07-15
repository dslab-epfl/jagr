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

package com.sun.j2ee.blueprints.admin.web;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.naming.NamingException;
import javax.naming.InitialContext;

/**
 * This servlet serves requests from Admin web client
 */
public class AdminRequestProcessor extends HttpServlet {

    /**
     * This method builds the dynamic JNLP file for java web start
     * @param req The <Code>HttpServletRequest</Code> from which host details
     *            are obtained
     * @param sid The session id to enable the rich client attach itself to
     *            this session and thereby get authenticated
     * @returns String the JNLP file for download by WebStart
     */
    protected String buildJNLP(HttpServletRequest req) throws ServletException {
        String serverName = req.getServerName();
        int serverPort = req.getServerPort();
        StringBuffer jnlp = new StringBuffer();
        jnlp.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); // Append xmlHeader
        jnlp.append("<jnlp codebase=\"http://").append(serverName).append(":").append(serverPort).append("/admin\">\n"); // Append codebase

        // append appInfo
        jnlp.append("<information>\n").append("<title>Pet Store Admin Rich Client</title>\n");
        jnlp.append("<vendor>J2EE BluePrints</vendor>\n");
        jnlp.append("<description>Example of Java Web Start Enabled Rich Client For a J2EE application</description>\n");
        jnlp.append("<description kind=\"short\"></description>\n");
        jnlp.append("</information>\n");

        // append rsrcInfo
        jnlp.append("<resources>\n");
        jnlp.append("<j2se version=\"1.4\"/>\n");
        jnlp.append("<j2se version=\"1.3\"/>\n");
        jnlp.append("<jar href=\"AdminApp.jar\"/>\n");
        jnlp.append("<jar href=\"jaxp.jar\"/>\n");
        jnlp.append("<jar href=\"crimson.jar\"/>\n");
        jnlp.append("</resources>\n");

        // append appDesc
        jnlp.append("<application-desc main-class=\"com.sun.j2ee.blueprints.admin.client.PetStoreAdminClient\">\n");
        jnlp.append("<argument>com.sun.j2ee.blueprints.admin.client.HttpPostPetStoreProxy</argument>\n");
        jnlp.append("<argument>").append(serverName).append("</argument>\n");
        jnlp.append("<argument>").append(serverPort).append("</argument>\n");
        jnlp.append("<argument>").append(req.getSession().getId()).append("</argument>\n");

        jnlp.append("</application-desc>\n</jnlp>\n"); // endOfFile
        return(jnlp.toString());
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws IOException, ServletException {
        getServletConfig().getServletContext().getRequestDispatcher("/index.jsp").forward(req, resp);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws IOException, ServletException {
        String curScreen = req.getParameter("currentScreen");
        if (curScreen == null) {
            return;
        }
        curScreen = curScreen.trim();
        if(curScreen.equals("logout")) {
            getServletConfig().getServletContext().getRequestDispatcher("/logout.jsp").forward(req, resp);
        }
        if(curScreen.equals("manageorders")) {
            resp.setContentType("application/x-java-jnlp-file");
            ServletOutputStream out = resp.getOutputStream();
            out.println(buildJNLP(req));
            out.flush();
            out.close();
        }
    }
}
