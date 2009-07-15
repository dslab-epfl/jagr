/*
 * $Header: /home/candea/Documents/Stanford/home/CVS/ROC/PP/pp-jboss/jetty/src/main/org/apache/jasper/compiler/KjcJavaCompiler.java,v 1.1.1.1 2003/03/07 08:26:04 emrek Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/03/07 08:26:04 $
 *
 * ====================================================================
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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.File;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.apache.jasper.Constants;

/**
 * A Plug-in class for specifying a 'kjc' compiler.
 *
 * Please link $CATALINA_HOME/jasper/jasper-compiler.jar and kjc.jar
 * (or kopi.jar) to $CATALINA_HOME/lib before use.
 +
 * Most of code in this class is copied from SunJavaCompiler.java.
 *
 * @author Anil K. Vijendran
 * @author Takashi Okamoto <tora@debian.org>
 * @author teik <teik@rd5.so-net.ne.jp>
 */
public class KjcJavaCompiler implements JavaCompiler {

    String encoding;
    String classpath; // ignored
    String compilerPath;
    String outdir; // ignored
    OutputStream out;
    boolean classDebugInfo=false;

    /**
     * Specify where the compiler can be found
     */
    public void setCompilerPath(String compilerPath) {
        // not used by the KjcJavaCompiler
	this.compilerPath = compilerPath;
    }

    /**
     * Set the encoding (character set) of the source
     */
    public void setEncoding(String encoding) {
      this.encoding = encoding;
    }

    /**
     * Set the class path for the compiler
     */
    public void setClasspath(String classpath) {
      this.classpath = classpath;
    }

    /**
     * Set the output directory
     */
    public void setOutputDir(String outdir) {
      this.outdir = outdir;
    }

    /**
     * Set where you want the compiler output (messages) to go
     */
    public void setMsgOutput(OutputStream out) {
      this.out = out;
    }

    /**
     * Set if you want debugging information in the class file
     */
    public void setClassDebugInfo(boolean classDebugInfo) {
        this.classDebugInfo = classDebugInfo;
    }

    /**
     * Set where you want the compiler output (messages) to go
     */
    public void setOut(OutputStream out) {
        this.out = out;
    }

    public boolean compile(String source) {
	char spr = File.separatorChar;
	String outputdir = source.substring(0, source.lastIndexOf(spr));
	String[] args = new String[]
	{
            "-encoding", encoding,
            "-classpath", classpath,
            "-d", outputdir,
            source
	};

        try {
            Class c = Class.forName("at.dms.kjc.Main");

            Constructor cons = c.getConstructor(null);
            Object compiler = cons.newInstance(null);

            Method compile = c.getMethod
                ("run", new Class [] {String.class, PrintWriter.class,
                                      String[].class});

            Boolean ok = (Boolean)compile.invoke
                (compiler, new Object[] {
                    (String)null,
                    new PrintWriter(new OutputStreamWriter(out, encoding)),
                    args});

            String packageName = Constants.JSP_PACKAGE_NAME;
            if(packageName != null) {
                packageName = spr + packageName.replace('.', spr);
            } else {
                packageName = "";
            }
            String className = source.substring
                (source.lastIndexOf(spr), source.lastIndexOf(".java")) 
                + ".class";
            File classFile = new File
                (outputdir + packageName + spr +  className);
            classFile.renameTo(new File(outputdir + spr + className));

            return ok.booleanValue();
        } catch (ClassNotFoundException e) {
	    try {
		out.write(":kjc can't find. please check kjc installation.".getBytes());
	    } catch (Exception e2) {
	    }
	    return false;
        } catch (InvocationTargetException ei) {
	    try {
		out.write(":maybe kjc setup is invalid. please check gnu.getopt.jar installation.".getBytes());
	    } catch (Exception e2) {
	    }
	    return false;
	} catch (Exception e){
	    try {
		out.write(":unknown error occurred while compiling jsp with kjc.".getBytes());
	    } catch (Exception e2) {
	    }
	    return false;
	}
    }

}
