/*
 * $Header: /home/candea/Documents/Stanford/home/CVS/ROC/PP/pp-jboss/jetty/src/main/org/apache/jasper/compiler/CommandLineCompiler.java,v 1.1.1.1 2003/03/07 08:26:04 emrek Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/03/07 08:26:04 $
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.jasper.compiler;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.jasper.Constants;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.CommandLineContext;

/**
 * Overrides some methods so that we get the desired effects.
 *@author Danno Ferrin
 */
public class CommandLineCompiler extends Compiler implements Mangler {

    String javaFileName;
    String classFileName;
    String packageName;
    String pkgName;
    String className;
    File jsp;
    String outputDir;

    public CommandLineCompiler(CommandLineContext ctxt) {
        super(ctxt);

        jsp = new File(ctxt.getJspFile());
        outputDir =  ctxt.getOptions().getScratchDir().getAbsolutePath();
	packageName = ctxt.getServletPackageName();
	pkgName = packageName;
        setMangler(this);

        className = getBaseClassName();
        // yes this is kind of messed up ... but it works
        if (ctxt.isOutputInDirs()) {
            String tmpDir = outputDir
                   + File.separatorChar
                   + pkgName.replace('.', File.separatorChar);
            File f = new File(tmpDir);
            if (!f.exists()) {
                if (f.mkdirs()) {
                    outputDir = tmpDir;
                }
            } else {
                outputDir = tmpDir;
            }
        }
        computeClassFileName();
        computeJavaFileName();
    };


    /**
     * Always outDated.  (Of course we are, this is an explicit invocation
     * @return true
     */
    public boolean isOutDated() {
        return true;
    };


    public final void computeJavaFileName() {
	javaFileName = ctxt.getServletClassName() + ".java";
	if ("null.java".equals(javaFileName)) {
    	    javaFileName = getBaseClassName() + ".java";
    	};
	if (outputDir != null && !outputDir.equals(""))
	    javaFileName = outputDir + File.separatorChar + javaFileName;
    }

    void computeClassFileName() {
        classFileName = getBaseClassName() + ".class";
	if (outputDir != null && !outputDir.equals(""))
	    classFileName = outputDir + File.separatorChar + classFileName;
    }

    public static String [] keywords = {
        "abstract", "boolean", "break", "byte",
        "case", "catch", "char", "class",
        "const", "continue", "default", "do",
        "double", "else", "extends", "final",
        "finally", "float", "for", "goto",
        "if", "implements", "import",
        "instanceof", "int", "interface",
        "long", "native", "new", "package",
        "private", "protected", "public",
        "return", "short", "static", "super",
        "switch", "synchronized", "this",
        "throw", "throws", "transient",
        "try", "void", "volatile", "while"
    };

    private final String getInitialClassName() {
        return getBaseClassName();
    }

    private final String getBaseClassName() {
	String className = ctxt.getServletClassName();

	if (className == null) {
            if (jsp.getName().endsWith(".jsp"))
                className = jsp.getName().substring(0, jsp.getName().length() - 4);
            else
                className = jsp.getName();

        }
	return mangleName(className);
    }
	
    private static final String mangleName(String name) {

	// since we don't mangle extensions like the servlet does,
	// we need to check for keywords as class names
	for (int i = 0; i < keywords.length; i++) {
	    if (name.equals(keywords[i])) {
		name += "%";
		break;
	    };
	};
	
	// Fix for invalid characters. If you think of more add to the list.
	StringBuffer modifiedName = new StringBuffer();
	if (Character.isJavaIdentifierStart(name.charAt(0)))
	    modifiedName.append(name.charAt(0));
	else
	    modifiedName.append(mangleChar(name.charAt(0)));
	for (int i = 1; i < name.length(); i++) {
	    if (Character.isJavaIdentifierPart(name.charAt(i)))
		modifiedName.append(name.charAt(i));
	    else
		modifiedName.append(mangleChar(name.charAt(i)));
	}
	
	return modifiedName.toString();
    }

    private static final String mangleChar(char ch) {
	
        if(ch == File.separatorChar) {
	    ch = '/';
	}
	String s = Integer.toHexString(ch);
	int nzeros = 5 - s.length();
	char[] result = new char[6];
	result[0] = '_';
	for (int i = 1; i <= nzeros; i++)
	    result[i] = '0';
	for (int i = nzeros+1, j = 0; i < 6; i++, j++)
	    result[i] = s.charAt(j);
	return new String(result);
    }

    /**
     * Make sure that the package name is a legal Java name
     *
     * @param name The input string, containing arbitary chars separated by
     *             '.'s, with possible leading, trailing, or double '.'s
     * @return legal Java package name.
     */
    public static String manglePackage(String name) {
        boolean first = true;

        StringBuffer b = new StringBuffer();
        StringTokenizer t = new StringTokenizer(name, ".");
        while (t.hasMoreTokens()) {
            String nt = t.nextToken();
            if (nt.length() > 0) {
                if (b.length() > 0)
                    b.append('.');
                b.append(mangleName(nt));
            }
        }
        return b.toString();
    }

    public final String getClassName() {
        return className;
    }

    public final String getPackageName() {
        return packageName;
    }

    public final String getJavaFileName() {
        return javaFileName;
    }

    public final String getClassFileName() {
        return classFileName;
    }


}
