
/*
 * @(#)Subject.java     1.114 01/02/20
 *
 * Copyright 2000 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2000 Sun Microsystems, Inc. Tous droits reserves.
 */

package javax.security.auth;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.text.MessageFormat;
import java.security.AccessController;
import java.security.AccessControlContext;
import java.security.DomainCombiner;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.security.ProtectionDomain;

/**
 * <p> A <code>Subject</code> represents a grouping of related information
 * for a single entity, such as a person.
 * Such information includes the Subject's identities as well as
 * its security-related attributes
 * (passwords and cryptographic keys, for example).
 *
 * <p> Subjects may potentially have multiple identities.
 * Each identity is represented as a <code>Principal</code>
 * within the <code>Subject</code>.  Principals simply bind names to a
 * <code>Subject</code>.  For example, a <code>Subject</code> that happens
 * to be a person, Alice, might have two Principals:
 * one which binds "Alice Bar", the name on her driver license,
 * to the <code>Subject</code>, and another which binds,
 * "999-99-9999", the number on her student identification card,
 * to the <code>Subject</code>.  Both Principals refer to the same
 * <code>Subject</code> even though each has a different name.
 *
 * <p> A <code>Subject</code> may also own security-related attributes,
 * which are referred to as credentials.
 * Sensitive credentials that require special protection, such as
 * private cryptographic keys, are stored within a private credential
 * <code>Set</code>.  Credentials intended to be shared, such as
 * public key certificates or Kerberos server tickets are stored
 * within a public credential <code>Set</code>.  Different permissions
 * are required to access and modify the different credential Sets.
 *
 * <p> To retrieve all the Principals associated with a <code>Subject</code>,
 * invoke the <code>getPrincipals</code> method.  To retrieve
 * all the public or private credentials belonging to a <code>Subject</code>,
 * invoke the <code>getPublicCredentials</code> method or
 * <code>getPrivateCredentials</code> method, respectively.
 * To modify the returned <code>Set</code> of Principals and credentials,
 * use the methods defined in the <code>Set</code> class.
 * For example:
 * <pre>
 *      Subject subject;
 *      Principal principal;
 *      Object credential;
 *
 *      // add a Principal and credential to the Subject
 *      subject.getPrincipals().add(principal);
 *      subject.getPublicCredentials().add(credential);
 * </pre>
 *
 * <p> This <code>Subject</code> class implements <code>Serializable</code>.
 * While the Principals associated with the <code>Subject</code> are serialized,
 * the credentials associated with the <code>Subject</code> are not.
 * Note that the <code>java.security.Principal</code> class
 * does not implement <code>Serializable</code>.  Therefore all concrete
 * <code>Principal</code> implementations associated with Subjects
 * must implement <code>Serializable</code>.
 *
 * @version 1.114, 02/20/01
 * @see java.security.Principal
 * @see java.security.DomainCombiner
 */
public final class Subject implements java.io.Serializable {

    private static final long serialVersionUID = -8308522755600156056L;

    /**
     * A <code>Set</code> that provides a view of all of this
     * Subject's Principals
     *
     * <p>
     *
     * @serial Each element in this set is a
     *          <code>java.security.Principal</code>.
     *          The set is a <code>Subject.SecureSet</code>.
     */
    Set principals;

    /**
     * Sets that provide a view of all of this
     * Subject's Credentials
     */
    transient Set pubCredentials;
    transient Set privCredentials;

    /**
     * Whether this Subject is read-only
     *
     * @serial
     */
    private boolean readOnly = false;

    private static final int PRINCIPAL_SET = 1;
    private static final int PUB_CREDENTIAL_SET = 2;
    private static final int PRIV_CREDENTIAL_SET = 3;

    /**
     * Create an instance of a <code>Subject</code>
     * with an empty <code>Set</code> of Principals and empty
     * Sets of public and private credentials.
     *
     * <p> The newly constructed Sets check whether this <code>Subject</code>
     * has been set read-only before permitting subsequent modifications.
     * The newly created Sets also prevent illegal modifications
     * by ensuring that callers have sufficient permissions
     * (to modify the Principals Set, the caller must have
     * <code>AuthPermission("modifyPrincipals")</code>, for example).
     */
    public Subject() {

        this.principals = new SecureSet(PRINCIPAL_SET);
        this.pubCredentials = new SecureSet(PUB_CREDENTIAL_SET);
        this.privCredentials = new SecureSet(PRIV_CREDENTIAL_SET);
    }

    /**
     * Create an instance of a <code>Subject</code> with
     * the specified Sets of Principals and credentials.
     *
     * <p> The specified Sets must check whether this <code>Subject</code>
     * has been set read-only before permitting subsequent modifications.
     * The specified Sets must also prevent illegal modifications
     * by ensuring that callers have sufficient permissions.
     *
     * <p>
     *
     * @param readOnly true if the <code>Subject</code> is to be read-only,
     *          and false otherwise. <p>
     *
     * @param principals the <code>Set</code> of Principals
     *          to be associated with this <code>Subject</code>. <p>
     *
     * @param pubCredentials the <code>Set</code> of public credentials
     *          to be associated with this <code>Subject</code>. <p>
     *
     * @param privCredentials the <code>Set</code> of private credentials
     *          to be associated with this <code>Subject</code>.
     *
     * @exception NullPointerException if the specified
     *          <code>principals</code>, <code>pubCredentials</code>,
     *          or <code>privCredentials</code> are <code>null</code>.
     */
    public Subject(boolean readOnly, Set principals,
                Set pubCredentials, Set privCredentials) {

        if (principals == null ||
            pubCredentials == null ||
            privCredentials == null)
            throw new NullPointerException
                ("invalid null input(s)");

        this.principals = new SecureSet(PRINCIPAL_SET,
                                        principals);
        this.pubCredentials = new SecureSet(PUB_CREDENTIAL_SET,
                                        pubCredentials);
        this.privCredentials = new SecureSet(PRIV_CREDENTIAL_SET,
                                        privCredentials);
        this.readOnly = readOnly;
    }

    /**
     * Set this <code>Subject</code> to be read-only.
     *
     * <p> Modifications (additions and removals) to this Subject's
     * <code>Principal</code> <code>Set</code> and
     * credential Sets will be disallowed.
     * The <code>destroy</code> operation on this Subject's credentials will
     * still be permitted.
     *
     * <p> Subsequent attempts to modify the Subject's <code>Principal</code>
     * and credential Sets will result in an
     * <code>IllegalStateException</code> being thrown.
     * Also, once a <code>Subject</code> is read-only,
     * it can not be reset to being writable again.
     *
     * <p>
     *
     * @exception SecurityException if the caller does not have permission
     *          to set this <code>Subject</code> to be read-only.
     */
    public void setReadOnly() {
        java.lang.SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AuthPermission("setReadOnly"));
        }

        this.readOnly = true;
    }

    /**
     * Query whether this <code>Subject</code> is read-only.
     *
     * <p>
     *
     * @return true if this <code>Subject</code> is read-only, false otherwise.
     */
    public boolean isReadOnly() {
        return this.readOnly;
    }

    /**
     * Get the <code>Subject</code> associated with the provided
     * <code>AccessControlContext</code>.
     *
     * <p> The <code>AccessControlContext</code> may contain many
     * Subjects (from nested <code>doAs</code> calls).
     * In this situation, the most recent <code>Subject</code> associated
     * with the <code>AccessControlContext</code> is returned.
     *
     * <p>
     *
     * @param  acc the <code>AccessControlContext</code> from which to retrieve
     *          the <code>Subject</code>.
     *
     * @return  the <code>Subject</code> associated with the provided
     *          <code>AccessControlContext</code>, or <code>null</code>
     *          if no <code>Subject</code> is associated
     *          with the provided <code>AccessControlContext</code>.
     *
     * @exception SecurityException if the caller does not have permission
     *          to get the <code>Subject</code>. <p>
     *
     * @exception NullPointerException if the provided
     *          <code>AccessControlContext</code> is <code>null</code>.
     */
    public static Subject getSubject(final AccessControlContext acc) {

        java.lang.SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AuthPermission("getSubject"));
        }

        if (acc == null) {
            throw new NullPointerException(
                "invalid null AccessControlContext provided");
        }

        // return the Subject from the DomainCombiner of the provided context
        return (Subject)AccessController.doPrivileged
            (new java.security.PrivilegedAction() {
            public Object run() {
                DomainCombiner dc = acc.getDomainCombiner();
                if (!(dc instanceof SubjectDomainCombiner))
                    return null;
                SubjectDomainCombiner sdc = (SubjectDomainCombiner)dc;
                return sdc.getSubject();
            }
        });
    }

    /**
     * Perform work as a particular <code>Subject</code>.
     *
     * <p> This method first retrieves the current Thread's
     * <code>AccessControlContext</code> via
     * <code>AccessController.getContext</code>,
     * and then instantiates a new <code>AccessControlContext</code>
     * using the retrieved context along with a new
     * <code>SubjectDomainCombiner</code> (constructed using
     * the provided <code>Subject</code>).
     * Finally, this method invokes <code>AccessController.doPrivileged</code>,
     * passing it the provided <code>PrivilegedAction</code>,
     * as well as the newly constructed <code>AccessControlContext</code>.
     *
     * <p>
     *
     * @param subject the <code>Subject</code> that the specified
     *                  <code>action</code> will run as.  This parameter
     *                  may be <code>null</code>. <p>
     *
     * @param action the code to be run as the specified
     *                  <code>Subject</code>. <p>
     *
     * @return the <code>Object</code> returned by the PrivilegedAction's
     *                  <code>run</code> method.
     *
     * @exception NullPointerException if the <code>PrivilegedAction</code>
     *                  is <code>null</code>. <p>
     *
     * @exception SecurityException if the caller does not have permission
     *                  to invoke this method.
     */
    public static Object doAs(final Subject subject,
                        final java.security.PrivilegedAction action) {

        java.lang.SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AuthPermission("doAs"));
        }
        if (action == null)
            throw new NullPointerException
                ("invalid null action provided");

        // set up the new Subject-based AccessControlContext
        // for doPrivileged
        final AccessControlContext currentAcc = AccessController.getContext();

        // call doPrivileged and push this new context on the stack
        return java.security.AccessController.doPrivileged
                                        (action,
                                        createContext(subject, currentAcc));
    }

    /**
     * Perform work as a particular <code>Subject</code>.
     *
     * <p> This method first retrieves the current Thread's
     * <code>AccessControlContext</code> via
     * <code>AccessController.getContext</code>,
     * and then instantiates a new <code>AccessControlContext</code>
     * using the retrieved context along with a new
     * <code>SubjectDomainCombiner</code> (constructed using
     * the provided <code>Subject</code>).
     * Finally, this method invokes <code>AccessController.doPrivileged</code>,
     * passing it the provided <code>PrivilegedExceptionAction</code>,
     * as well as the newly constructed <code>AccessControlContext</code>.
     *
     * <p>
     *
     * @param subject the <code>Subject</code> that the specified
     *                  <code>action</code> will run as.  This parameter
     *                  may be <code>null</code>. <p>
     *
     * @param action the code to be run as the specified
     *                  <code>Subject</code>. <p>
     *
     * @return the <code>Object</code> returned by the
     *                  PrivilegedExceptionAction's <code>run</code> method.
     *
     * @exception PrivilegedActionException if the
     *                  <code>PrivilegedExceptionAction.run</code>
     *                  method throws a checked exception. <p>
     *
     * @exception NullPointerException if the specified
     *                  <code>PrivilegedExceptionAction</code> is
     *                  <code>null</code>. <p>
     *
     * @exception SecurityException if the caller does not have permission
     *                  to invoke this method.
     */
    public static Object doAs(final Subject subject,
                        final java.security.PrivilegedExceptionAction action)
                        throws java.security.PrivilegedActionException {

        java.lang.SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AuthPermission("doAs"));
        }

        if (action == null)
            throw new NullPointerException
                ("invalid null action provided");

        // set up the new Subject-based AccessControlContext for doPrivileged
        final AccessControlContext currentAcc = AccessController.getContext();

        // call doPrivileged and push this new context on the stack
        return java.security.AccessController.doPrivileged
                                        (action,
                                        createContext(subject, currentAcc));
    }

    /**
     * Perform privileged work as a particular <code>Subject</code>.
     *
     * <p> This method behaves exactly as <code>Subject.doAs</code>,
     * except that instead of retrieving the current Thread's
     * <code>AccessControlContext</code>, it uses the provided
     * <code>AccessControlContext</code>.  If the provided
     * <code>AccessControlContext</code> is <code>null</code>,
     * this method instantiates a new <code>AccessControlContext</code>
     * with an empty collection of ProtectionDomains.
     *
     * <p>
     *
     * @param subject the <code>Subject</code> that the specified
     *                  <code>action</code> will run as.  This parameter
     *                  may be <code>null</code>. <p>
     *
     * @param action the code to be run as the specified
     *                  <code>Subject</code>. <p>
     *
     * @param acc the <code>AccessControlContext</code> to be tied to the
     *                  specified <i>subject</i> and <i>action</i>. <p>
     *
     * @return the <code>Object</code> returned by the PrivilegedAction's
     *                  <code>run</code> method.
     *
     * @exception NullPointerException if the <code>PrivilegedAction</code>
     *                  is <code>null</code>. <p>
     *
     * @exception SecurityException if the caller does not have permission
     *                  to invoke this method.
     */
    public static Object doAsPrivileged(final Subject subject,
                        final java.security.PrivilegedAction action,
                        final java.security.AccessControlContext acc) {

        java.lang.SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AuthPermission("doAsPrivileged"));
        }

        if (action == null)
            throw new NullPointerException
                ("invalid null action provided");

        // set up the new Subject-based AccessControlContext
        // for doPrivileged
        final AccessControlContext callerAcc =
                (acc == null ?
                new AccessControlContext(new ProtectionDomain[0]) :
                acc);

        // call doPrivileged and push this new context on the stack
        return java.security.AccessController.doPrivileged
                                        (action,
                                        createContext(subject, callerAcc));
    }

    /**
     * Perform privileged work as a particular <code>Subject</code>.
     *
     * <p> This method behaves exactly as <code>Subject.doAs</code>,
     * except that instead of retrieving the current Thread's
     * <code>AccessControlContext</code>, it uses the provided
     * <code>AccessControlContext</code>.  If the provided
     * <code>AccessControlContext</code> is <code>null</code>,
     * this method instantiates a new <code>AccessControlContext</code>
     * with an empty collection of ProtectionDomains.
     *
     * <p>
     *
     * @param subject the <code>Subject</code> that the specified
     *                  <code>action</code> will run as.  This parameter
     *                  may be <code>null</code>. <p>
     *
     * @param action the code to be run as the specified
     *                  <code>Subject</code>. <p>
     *
     * @param acc the <code>AccessControlContext</code> to be tied to the
     *                  specified <i>subject</i> and <i>action</i>. <p>
     *
     * @return the <code>Object</code> returned by the
     *                  PrivilegedExceptionAction's <code>run</code> method.
     *
     * @exception PrivilegedActionException if the
     *                  <code>PrivilegedExceptionAction.run</code>
     *                  method throws a checked exception. <p>
     *
     * @exception NullPointerException if the specified
     *                  <code>PrivilegedExceptionAction</code> is
     *                  <code>null</code>. <p>
     *
     * @exception SecurityException if the caller does not have permission
     *                  to invoke this method.
     */
    public static Object doAsPrivileged(final Subject subject,
                        final java.security.PrivilegedExceptionAction action,
                        final java.security.AccessControlContext acc)
                        throws java.security.PrivilegedActionException {

        java.lang.SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AuthPermission("doAsPrivileged"));
        }

        if (action == null)
            throw new NullPointerException
                ("invalid null action provided");

        // set up the new Subject-based AccessControlContext for doPrivileged
        final AccessControlContext callerAcc =
                (acc == null ?
                new AccessControlContext(new ProtectionDomain[0]) :
                acc);

        // call doPrivileged and push this new context on the stack
        return java.security.AccessController.doPrivileged
                                        (action,
                                        createContext(subject, callerAcc));
    }

    private static AccessControlContext createContext(final Subject subject,
                                        final AccessControlContext acc) {


        return (AccessControlContext)
            java.security.AccessController.doPrivileged
            (new java.security.PrivilegedAction() {
            public Object run() {
                if (subject == null)
                    return new AccessControlContext(acc, null);
                else
                    return new AccessControlContext
                                        (acc,
                                        new SubjectDomainCombiner(subject));
            }
        });
    }

    /**
     * Return the <code>Set</code> of Principals associated with this
     * <code>Subject</code>.  Each <code>Principal</code> represents
     * an identity for this <code>Subject</code>.
     *
     * <p> The returned <code>Set</code> is backed by this Subject's
     * internal <code>Principal</code> <code>Set</code>.  Any modification
     * to the returned <code>Set</code> affects the internal
     * <code>Principal</code> <code>Set</code> as well.
     *
     * <p>
     *
     * @return  The <code>Set</code> of Principals associated with this
     *          <code>Subject</code>.
     */
    public Set getPrincipals() {

        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return principals;
    }

    /**
     * Return a <code>Set</code> of Principals associated with this
     * <code>Subject</code> that are instances or subclasses of the specified
     * <code>Class</code>.
     *
     * <p> The returned <code>Set</code> is not backed by this Subject's
     * internal <code>Principal</code> <code>Set</code>.  A new
     * <code>Set</code> is created and returned for each method invocation.
     * Modifications to the returned <code>Set</code>
     * will not affect the internal <code>Principal</code> <code>Set</code>.
     *
     * <p>
     *
     * @param c the returned <code>Set</code> of Principals will all be
     *          instances of this class.
     *
     * @return a <code>Set</code> of Principals that are instances of the
     *          specified <code>Class</code>.
     *
     * @exception NullPointerException if the specified <code>Class</code> 
     *                  is <code>null</code>.
     */
    public Set getPrincipals(Class c) {

        if (c == null)
            throw new NullPointerException
                ("invalid null Class provided");

        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return new ClassSet(PRINCIPAL_SET, c);
    }

    /**
     * Return the <code>Set</code> of public credentials held by this
     * <code>Subject</code>.
     *
     * <p> The returned <code>Set</code> is backed by this Subject's
     * internal public Credential <code>Set</code>.  Any modification
     * to the returned <code>Set</code> affects the internal public
     * Credential <code>Set</code> as well.
     *
     * <p>
     *
     * @return  A <code>Set</code> of public credentials held by this
     *          <code>Subject</code>.
     */
    public Set getPublicCredentials() {

        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return pubCredentials;
    }

    /**
     * Return the <code>Set</code> of private credentials held by this
     * <code>Subject</code>.
     *
     * <p> The returned <code>Set</code> is backed by this Subject's
     * internal private Credential <code>Set</code>.  Any modification
     * to the returned <code>Set</code> affects the internal private
     * Credential <code>Set</code> as well.
     *
     * <p> A caller requires permissions to access the Credentials
     * in the returned <code>Set</code>, or to modify the
     * <code>Set</code> itself.  A <code>SecurityException</code>
     * is thrown if the caller does not have the proper permissions.
     * 
     * <p> While iterating through the <code>Set</code>,
     * a <code>SecurityException</code> is thrown
     * if the caller does not have permission to access a
     * particular Credential.  The <code>Iterator</code>
     * is nevertheless advanced to next element in the <code>Set</code>.
     *
     * <p>
     *
     * @return  A <code>Set</code> of private credentials held by this
     *          <code>Subject</code>.
     */
    public Set getPrivateCredentials() {

        // XXX
        // we do not need a security check for
        // AuthPermission(getPrivateCredentials)
        // because we already restrict access to private credentials
        // via the PrivateCredentialPermission.  all the extra AuthPermission
        // would do is protect the set operations themselves
        // (like size()), which don't seem security-sensitive.

        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return privCredentials;
    }

    /**
     * Return a <code>Set</code> of public credentials associated with this
     * <code>Subject</code> that are instances or subclasses of the specified
     * <code>Class</code>.
     *
     * <p> The returned <code>Set</code> is not backed by this Subject's
     * internal public Credential <code>Set</code>.  A new
     * <code>Set</code> is created and returned for each method invocation.
     * Modifications to the returned <code>Set</code>
     * will not affect the internal public Credential <code>Set</code>.
     *
     * <p>
     *
     * @param c the returned <code>Set</code> of public credentials will all be
     *          instances of this class.
     *
     * @return a <code>Set</code> of public credentials that are instances
     *          of the  specified <code>Class</code>.
     *
     * @exception NullPointerException if the specified <code>Class</code>
     *          is <code>null</code>.
     */
    public Set getPublicCredentials(Class c) {

        if (c == null)
            throw new NullPointerException
                ("invalid null Class provided");

        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return new ClassSet(PUB_CREDENTIAL_SET, c);
    }

    /**
     * Return a <code>Set</code> of private credentials associated with this
     * <code>Subject</code> that are instances or subclasses of the specified
     * <code>Class</code>. 
     *
     * <p> The caller must have permission to access all of the
     * requested Credentials, or a <code>SecurityException</code>
     * will be thrown.
     *
     * <p> The returned <code>Set</code> is not backed by this Subject's
     * internal private Credential <code>Set</code>.  A new
     * <code>Set</code> is created and returned for each method invocation.
     * Modifications to the returned <code>Set</code>
     * will not affect the internal private Credential <code>Set</code>.
     *
     * <p>
     *
     * @param c the returned <code>Set</code> of private credentials will all be
     *          instances of this class.
     *
     * @return a <code>Set</code> of private credentials that are instances
     *          of the  specified <code>Class</code>.
     *
     * @exception NullPointerException if the specified <code>Class</code>
     *          is <code>null</code>.
     */
    public Set getPrivateCredentials(Class c) {

        // XXX
        // we do not need a security check for
        // AuthPermission(getPrivateCredentials)
        // because we already restrict access to private credentials
        // via the PrivateCredentialPermission.  all the extra AuthPermission
        // would do is protect the set operations themselves
        // (like size()), which don't seem security-sensitive.

        if (c == null)
            throw new NullPointerException
                ("invalid null Class provided");

        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return new ClassSet(PRIV_CREDENTIAL_SET, c);
    }

    /**
     * Compares the specified Object with this <code>Subject</code>
     * for equality.  Returns true if the given object is also a Subject
     * and the two <code>Subject</code> instances are equivalent.
     * More formally, two <code>Subject</code> instances are
     * equal if their <code>Principal</code> and <code>Credential</code>
     * Sets are equal.
     *
     * <p>
     *
     * @param o Object to be compared for equality with this
     *          <code>Subject</code>.
     *
     * @return true if the specified Object is equal to this
     *          <code>Subject</code>.
     *
     * @exception SecurityException if the caller does not have permission
     *          to access the private credentials for this <code>Subject</code>,
     *          or if the caller does not have permission to access the
     *          private credentials for the provided <code>Subject</code>.
     */
    public boolean equals(Object o) {

        if (o == null)
            return false;

        if (this == o)
            return true;

        if (o instanceof Subject) {

            final Subject that = (Subject)o;

            // check the principal and credential sets
            if (!getPrincipals().equals(that.getPrincipals()) ||
                !getPublicCredentials().equals(that.getPublicCredentials()) ||
                !getPrivateCredentials().equals(that.getPrivateCredentials())) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the String representation of this <code>Subject</code>.
     *
     * <p>
     *
     * @return the String representation of this <code>Subject</code>.
     */
    public String toString() {
        return toString(true);
    }

    /**
     * package private convenience method to print out the Subject
     * without firing off a security check when trying to access
     * the Private Credentials
     */
    String toString(boolean includePrivateCredentials) {

        String s = new String("Subject:\n");

        String suffix = new String();
        Iterator principals = getPrincipals().iterator();
        Iterator pubCreds = getPublicCredentials().iterator();
        Iterator privCreds = null;
                   
        if (includePrivateCredentials) {
            try {
                privCreds = getPrivateCredentials().iterator();
            } catch (SecurityException se) {
                // ok
            }
        }

        while (principals.hasNext()) {
            Principal p = (Principal)principals.next();
            suffix = suffix + "\tPrincipal: " +
                        p.toString() + "\n";
        }

        while (pubCreds.hasNext()) {
            Object o = pubCreds.next();
            suffix = suffix + "\tPublic Credential: " +
                        o.toString() + "\n";
        }

        if (privCreds == null) {
            suffix = suffix +
                "\tPrivate Credentials inaccessible\n";
        } else {
            while (privCreds.hasNext()) {
                try {
                    Object o = privCreds.next();
                    suffix += "\tPrivate Credential: " +
                        o.toString() + "\n";
                } catch (SecurityException se) {
                    suffix += 
                        "\tPrivate Credential inaccessible\n";
                    break;
                }
            }
        }
        return s + suffix;
    }

    /**
     * Returns a hashcode for this <code>Subject</code>.
     *
     * <p>
     *
     * @return a hashcode for this <code>Subject</code>.
     *
     * @exception SecurityException if the caller does not have permission
     *          to access this Subject's private credentials.
     */
    public int hashCode() {

        /** 
         * The hashcode is derived exclusive or-ing the
         * hashcodes of this Subject's Principals and credentials.
         *
         * If a particular credential was destroyed
         * (<code>credential.hashCode()</code> throws an
         * <code>IllegalStateException</code>),
         * the hashcode for that credential is derived via:
         * <code>credential.getClass().toString().hashCode()</code>.
         */

        int hashCode = 0;

        Iterator pIterator = getPrincipals().iterator();
        Iterator pubCIterator = getPublicCredentials().iterator();
        Iterator privCIterator = getPrivateCredentials().iterator();

        while (pIterator.hasNext()) {
            Principal p = (Principal)pIterator.next();
            hashCode ^= p.hashCode();
        }

        while (pubCIterator.hasNext()) {
            hashCode ^= getCredHashCode(pubCIterator.next());
        }
        return hashCode;
    }

    /**
     * get a credential's hashcode
     */
    private int getCredHashCode(Object o) {

        try {
            return o.hashCode();
        } catch (IllegalStateException ise) {
            return o.getClass().toString().hashCode();
        }
    }

    private void sort(int[] sortMe) {
 
        // bubble sort :)
 
        int i = 0;
        boolean flipped = true;
        int size = sortMe.length - 1;
 
        while (flipped) {
            i = 0;
            flipped = false;
            while (i < size) {
                if (sortMe[i] < sortMe[i + 1]) {
                    flipped = true;
                    int tmp = sortMe[i];
                    sortMe[i] = sortMe[i + 1];
                    sortMe[i + 1] = tmp;
                }
                i++;
            }
            size--;
        }
    }

    /**
     * Writes this object out to a stream (i.e., serializes it).
     */
    private synchronized void writeObject(java.io.ObjectOutputStream oos)
    throws java.io.IOException {

        // XXX possibly add security checks in the future

        oos.defaultWriteObject();
    }

    /**
     * Reads this object from a stream (i.e., deserializes it)
     */
    private void readObject(java.io.ObjectInputStream s) throws
                                        java.io.IOException,
                                        ClassNotFoundException {

        s.defaultReadObject();

        // The Credential <code>Set</code> is not serialized, but we do not
        // want the default deserialization routine to set it to null.
        this.pubCredentials = new SecureSet(PUB_CREDENTIAL_SET);
        this.privCredentials = new SecureSet(PRIV_CREDENTIAL_SET);
    }

    /**
     * Prevent modifications unless caller has permission.
     *
     * @serial include
     */
    private class SecureSet
        extends AbstractSet
        implements java.io.Serializable {

        private static final long serialVersionUID = 7911754171111800359L;

        /**
         * @serial A <code>LinkedList</code> of the elements
         *      in this set.
         */
        LinkedList elements;

        /**
         * @serial An integer identifying the type of objects contained
         *      in this set.  If <code>which == 1</code>,
         *      this is a Principal set and all the elements are
         *      of type <code>java.security.Principal</code>.
         *      If <code>which == 2</code>, this is a public credential
         *      set and all the elements are of type <code>Object</code>.
         *      If <code>which == 3</code>, this is a private credential
         *      set and all the elements are of type <code>Object</code>.
         */
        private int which;

        SecureSet(int which) {
            this.which = which;
            this.elements = new LinkedList();
        }

        SecureSet(int which, Set set) {
            this.which = which;
            this.elements = new LinkedList(set);
        }

        public synchronized int size() {
            return elements.size();
        }

        public Iterator iterator() {
            final LinkedList list = elements;
            return new Iterator() {
                ListIterator i = list.listIterator(0);

                public synchronized boolean hasNext() {return i.hasNext();}
            
                public synchronized Object next() {
                    if (which != Subject.PRIV_CREDENTIAL_SET) 
                        return i.next();

                    java.lang.SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        try {
                            if (Subject.this.getPrincipals() == null ||
                                Subject.this.getPrincipals().size() == 0) {
                                sm.checkPermission
                                    (new PrivateCredentialPermission
                                (list.get(i.nextIndex()).getClass().getName(),
                                new java.util.HashSet()));
                            } else {
                                sm.checkPermission
                                    (new PrivateCredentialPermission
                                (PrivateCredentialPermission.buildTarget
                                (list.get(i.nextIndex()).getClass().getName(),
                                Subject.this.getPrincipals()), "read"));
                            }
                        } catch (SecurityException se) {
                            i.next();
                            throw (se);
                        }
                    }
                    return i.next();
                }
            
                public synchronized void remove() {

                    if (Subject.this.isReadOnly()) {
                        throw new IllegalStateException(
                                "Subject is read-only");
                    }

                    java.lang.SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        switch (which) {
                        case Subject.PRINCIPAL_SET:
                            sm.checkPermission(new AuthPermission
                                        ("modifyPrincipals"));
                            break;
                        case Subject.PUB_CREDENTIAL_SET:
                            sm.checkPermission(new AuthPermission
                                        ("modifyPublicCredentials"));
                            break;
                        default:
                            sm.checkPermission(new AuthPermission
                                        ("modifyPrivateCredentials"));
                            break;
                        }
                    }
                    i.remove();
                }
            };
        }
          
        public synchronized boolean add(Object o) {

            if (Subject.this.isReadOnly()) {
                throw new IllegalStateException
                        ("Subject is read-only");
            }

            java.lang.SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                switch (which) {
                case Subject.PRINCIPAL_SET:
                    sm.checkPermission
                        (new AuthPermission("modifyPrincipals"));
                    break;
                case Subject.PUB_CREDENTIAL_SET:
                    sm.checkPermission
                        (new AuthPermission("modifyPublicCredentials"));
                    break;
                default:
                    sm.checkPermission
                        (new AuthPermission("modifyPrivateCredentials"));
                    break;
                }
            }

            switch (which) {
            case Subject.PRINCIPAL_SET:
                if (!(o instanceof Principal)) {
                    throw new SecurityException(
                        "attempting to add an object which is not an " +
                        "instance of java.security.Principal to a " +
                        "Subject's Principal Set");
                }
                break;
            default:
                // ok to add Objects of any kind to credential sets
                break;
            }

            // check for duplicates
            if (!elements.contains(o))
                return elements.add(o);
            else
                return false;
        }
          
        public synchronized boolean remove(Object o) {

            final Iterator e = iterator();
            while (e.hasNext()) {
                Object next;
                if (which != Subject.PRIV_CREDENTIAL_SET) {
                    next = e.next();
                } else {
                    next = (Object)java.security.AccessController.doPrivileged
                        (new java.security.PrivilegedAction() {
                        public Object run() {
                            return e.next();
                        }
                    });
                }
          
                if (next == null) {
                    if (o == null) {
                        e.remove();
                        return true;
                    }
                } else if (next.equals(o)) {
                    e.remove();
                    return true;
                }
            }
            return false;
        }

        public synchronized boolean contains(Object o) {

            // For private credentials:
            // If the caller does not have read permission for
            // for o.getClass(), we throw a SecurityException.
            // Otherwise we check the private cred set to see whether
            // it contains the Object

            java.lang.SecurityManager sm = System.getSecurityManager();
            if (sm != null && which == Subject.PRIV_CREDENTIAL_SET) {
                if (Subject.this.getPrincipals() == null ||
                    Subject.this.getPrincipals().size() == 0) {
                    sm.checkPermission(new PrivateCredentialPermission
                                (o.getClass().getName(),
                                new java.util.HashSet()));
                } else {
                    sm.checkPermission(new PrivateCredentialPermission
                                (PrivateCredentialPermission.buildTarget
                                        (o.getClass().getName(),
                                        Subject.this.getPrincipals()), "read"));
                }
            }

            final Iterator e = iterator();
            while (e.hasNext()) {
                Object next;
                if (which != Subject.PRIV_CREDENTIAL_SET) {
                    next = e.next();
                } else {
                    next = (Object)java.security.AccessController.doPrivileged
                        (new java.security.PrivilegedAction() {
                        public Object run() {
                            return e.next();
                        }
                    });
                }
          
                if (next == null) {
                    if (o == null) {
                        return true;
                    }
                } else if (next.equals(o)) {
                    return true;
                }
            }
            return false;
        }
          
        public boolean removeAll(Collection c) {

            boolean modified = false;
            final Iterator e = iterator();
            while (e.hasNext()) {
                Object next;
                if (which != Subject.PRIV_CREDENTIAL_SET) {
                    next = e.next();
                } else {
                    next = (Object)java.security.AccessController.doPrivileged
                        (new java.security.PrivilegedAction() {
                        public Object run() {
                            return e.next();
                        }
                    });
                }

                Iterator ce = c.iterator();
                while (ce.hasNext()) {
                    Object o = ce.next();
                    if (next == null) {
                        if (o == null) {
                            e.remove();
                            modified = true;
                            break;
                        }
                    } else if (next.equals(o)) {
                        e.remove();
                        modified = true;
                        break;
                    }
                }
            }
            return modified;
        }

        public boolean retainAll(Collection c) {

            boolean modified = false;
            boolean retain = false;
            final Iterator e = iterator();
            while (e.hasNext()) {
                retain = false;
                Object next;
                if (which != Subject.PRIV_CREDENTIAL_SET) {
                    next = e.next();
                } else {
                    next = (Object)java.security.AccessController.doPrivileged
                        (new java.security.PrivilegedAction() {
                        public Object run() {
                            return e.next();
                        }
                    });
                } 

                Iterator ce = c.iterator();
                while (ce.hasNext()) {
                    Object o = ce.next();           
                    if (next == null) {
                        if (o == null) {
                            retain = true;
                            break;
                        }
                    } else if (next.equals(o)) {
                        retain = true;
                        break;
                    }
                }
          
                if (!retain) {
                    e.remove();
                    retain = false;
                    modified = true;
                }
            }
            return modified;
        }
          
        public void clear() {
            final Iterator e = iterator();
            while (e.hasNext()) {
                Object next;
                if (which != Subject.PRIV_CREDENTIAL_SET) {
                    next = e.next();
                } else {
                    next = (Object)java.security.AccessController.doPrivileged
                        (new java.security.PrivilegedAction() {
                        public Object run() {
                            return e.next();
                        }
                    });
                }
                e.remove();
            }
        }

        /**
         * Writes this object out to a stream (i.e., serializes it).
         *
         * <p>
         *
         * @serialData If this is a private credential set,
         *      a security check is performed to ensure that
         *      the caller has permission to access each credential
         *      in the set.  If the security check passes,
         *      the set is serialized.
         */
        private synchronized void writeObject(java.io.ObjectOutputStream oos)
        throws java.io.IOException {

            if (which == Subject.PRIV_CREDENTIAL_SET) {
                // check permissions before serializing
                Iterator i = iterator();
                while (i.hasNext()) {
                    i.next();
                }
            }
            oos.defaultWriteObject();
        }
    }

    /**
     * This class implements a <code>Set</code> which returns only 
     * members that are an instance of a specified Class. 
     */
    private class ClassSet extends AbstractSet {

        private int which;
        private Set set;
        private Class c;

        ClassSet(int which, Class c) {

            synchronized(this) {

                this.which = which;
                this.c = c;

                Iterator iterator = null;
                switch(which) {
                case Subject.PRINCIPAL_SET:
                    iterator = Subject.this.principals.iterator();
                    break;
                case Subject.PUB_CREDENTIAL_SET:
                    iterator = Subject.this.pubCredentials.iterator();
                    break;
                default:
                    iterator = Subject.this.privCredentials.iterator();
                    break;
                }
                final Iterator iterator_copy = iterator;
                java.lang.SecurityManager sm = System.getSecurityManager();
                set = new HashSet();

                // Check whether the caller has permisson to get
                // credentials of Class c 
           
                while (iterator_copy.hasNext()) {
                    Object next =
                        (Object)java.security.AccessController.doPrivileged
                        (new java.security.PrivilegedAction() {
                        public Object run() {
                            return iterator_copy.next();
                        }
                    });
                    if (c.isAssignableFrom(next.getClass())) {
                        if (which != Subject.PRIV_CREDENTIAL_SET) {
                                set.add(next);
                        } else {
                            // Check permission for private creds
                            if (sm != null) {
                                if (Subject.this.getPrincipals() == null ||
                                    Subject.this.getPrincipals().size() == 0) {
                                        sm.checkPermission
                                            (new PrivateCredentialPermission
                                                (next.getClass().getName(),
                                                new java.util.HashSet()));
                                  } else {
                                        sm.checkPermission
                                            (new PrivateCredentialPermission
                                        (PrivateCredentialPermission.buildTarget
                                                (next.getClass().getName(),
                                                 Subject.this.getPrincipals()),
                                                "read"));
                                  }
                                }
                                set.add(next);    
                        }
                    }
                }
            }
        }
    

        public synchronized int size() {
            return set.size();
        }

        public Iterator iterator() {
            return set.iterator();
        }

        public synchronized boolean add(Object o) {
            
            if (!o.getClass().isAssignableFrom(c)) {
                MessageFormat form = new MessageFormat(
                        "attempting to add an object which is not an " +
                        "instance of class");
                Object[] source = {c.toString()};
                throw new SecurityException(form.format(source));
            }
            
            return set.add(o);
        }
    }
}

