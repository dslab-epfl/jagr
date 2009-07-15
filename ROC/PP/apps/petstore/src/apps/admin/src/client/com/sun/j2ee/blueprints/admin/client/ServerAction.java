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
package com.sun.j2ee.blueprints.admin.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * A base class for GUI actions that do their work asynchronously on
 * a <code>WorkQueue</code> thread.  It's intended to be used to
 * create singleton instances of subclasses that will become the
 * value of the <code>Action</code> property of a Swing component.
 * Subclasses should override the <code>request</code>,
 * <code>response</code>, and <code>handleException</code>
 * methods.  The <code>request</code> will be run on the
 * <code>WorkQueue</code> thread, the other methods will be run
 * on the event dispatching thread.  Here's an example:
 * <pre>
 * ServerAction myAction = new ServerAction("MyAction") {
 *     protected Object request(ActionEvent e) {
 *         return myServer.doMyRequest();
 *     }
 *     protected void response(Object requestValue) {
 *         myGUI.showResponse(requestValue);
 *     }
 *     protected void handleException(Exception e) {
 *         myGUI.showException(e);
 *     }
 * };
 * JButton myButton = new JButton(myAction);
 * </pre>
 * In this example <code>'myServer.doMyRequest()'</code> will run
 * on the <code>WorkQueue</code> thread and, when it returns,
 * <code>'myGUI.showResponse(requestValue)'</code>
 * will run on the event dispatching thread.  The
 * <code>showResponse</code> argument, <i>requestValue</i>,
 * is the value returned by the <code>response</code> method.
 * If the <code>request</code> method throws an exception,
 * then <code>handleException</code> is called on the
 * event dispatching thread, instead of <code>response</code>.
 *
 * @author Hans Muller
 * @see WorkQueue
 * @see EventQueue#invokeLater
 */
public class ServerAction extends AbstractAction
{
    /* This WorkQueue is shared by all ServerActions by default.
     * See getWorkQueue().
     */
    private static WorkQueue workQueue = null;

    /* This field holds the value returned by the request method
     * and then passed to the response method.  Access to the field
     * is synchronized - see set/getValue().
     */
    private Object value;


    public ServerAction() {
        super();
    }

    /**
     * Defines a <code>ServerAction</code> object with the specified
     * description string and a default icon.
     */
    public ServerAction(String name) {
        super(name);
    }

    /**
     * Defines a <code>ServerAction</code> object with the specified
     * description string and a the specified icon.
     */
    public ServerAction(String name, Icon icon) {
        super(name, icon);
    }


    /**
     * Access to the value field must be synchronized because it will
     * be accessed from both the WorkQueue thread and the event
     * dispatching thread.
     */
    private synchronized void setValue(Object x) {
        value = x;
    }

    private synchronized Object getValue() {
        return value;
    }


    /**
     * This method queues a Runnable on the WorkQueue that
     * will call the request method on the WorkQueue thread.
     * The value returned by the request method will be
     * be passed to the <code>response</code> method which
     * will be run on the event dispatching thread.  If
     * <code>request</code> throws an exception, then
     * <code>handleException</code> will be called instead.
     *
     * @see EventQueue#invokeLater
     * @see #request
     * @see #response
     * @see #handleException
     * @see #getWorkQueue
     */
    public void actionPerformed(final ActionEvent actionEvent)
    {
        final Runnable doRequest = new Runnable() {
            public void run() {
                try {
                    setValue(request(actionEvent));
                }
                catch (final Exception e) {
                    Runnable doHandleException = new Runnable() {
                        public void run() {
                            handleException(e);
                        }
                    };
                    EventQueue.invokeLater(doHandleException);
                }
                Runnable doResponse = new Runnable() {
                    public void run() {
                        response(getValue());
                    }
                };
                EventQueue.invokeLater(doResponse);
            }
        };

        getWorkQueue().enqueue(doRequest);
    }


    /**
     * Lazily creates a static <code>WorkQueue</code>.
     *
     * @see #request
     * @see #response
     * @see #handleException
     */
    protected WorkQueue getWorkQueue() {
        if (workQueue == null) {
            this.workQueue = new WorkQueue("ServerAction");
        }
        return workQueue;
    }


    /**
     * This method should be overridden to do whatever the action
     * represents.  It will run on the <code>WorkQueue</code> thread.
     * The value returned by this method will be passed
     * to <code>response</code>.
     *
     * @see #response
     * @see #handleException
     * @see #getWorkQueue
     * @see #actionPerformed
     */
    protected Object request(ActionEvent e) {
        return null;
    }


    /**
     * This method typically updates the GUI to reflect the value computed
     * by the <code>request</code> method.  It is run on the event dispatching
     * thread.
     *
     * @see #request
     * @see #handleException
     * @see #getWorkQueue
     * @see #invokeLater
     */
    protected void response(Object requestValue) {
    }


    /**
     * This method is called if the <code>request</code> method
     * throws an exception.  Typically it would alert the user
     * that something has gone wrong vis the applications connection
     * to its server, it might also take the application offline.
     * It is run on the event dispatching thread.
     *
     * @see #request
     * @see #response
     * @see #getWorkQueue
     * @see #invokeLater
     */
    protected void handleException(Exception e) {
    }
}



