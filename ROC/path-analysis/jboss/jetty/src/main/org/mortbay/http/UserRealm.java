// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: UserRealm.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ---------------------------------------------------------------------------

package org.mortbay.http;

/* ------------------------------------------------------------ */
/** User Realm.
 *
 * This interface should be specialized to provide specific user
 * lookup and authentication using arbitrary methods.
 *
 * @version $Id: UserRealm.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
 * @author Greg Wilkins (gregw)
 */
public interface UserRealm
{
    static public String __UserRole="org.mortbay.http.User";
    
    /* ------------------------------------------------------------ */
    public String getName();

    /* ------------------------------------------------------------ */
    /** Authenticate a users credentials.
     * Implementations of this method may adorn the calling context to
     * assoicate it with the authenticated principal (eg ThreadLocals). If
     * such context associations are made, they should be considered valid
     * until a UserRealm.deAuthenticate(UserPrincipal) call is made for this
     * UserPrincipal.
     * @param username The username. 
     * @param credentials The user credentials, normally a String password. 
     * @param request The request to be authenticated. Additional
     * parameters may be extracted or set on this request as needed
     * for the authentication mechanism (none required for BASIC and
     * FORM authentication).
     * @return The authenticated UserPrincipal.
     */
    public UserPrincipal authenticate(String username,
                                      Object credentials,
                                      HttpRequest request);

    /* ------------------------------------------------------------ */
    /** Dissassociate the calling context with a Principal.
     * This method is called when the calling context is not longer
     * associated with the Principal.  It should be used by an implementation
     * to remove context associations such as ThreadLocals.
     * The UserPrincipal object remains authenticated, as it may be
     * associated with other contexts.
     * @param user A UserPrincipal allocated from this realm.
     */
    public void disassociate(UserPrincipal user);
    
    /* ------------------------------------------------------------ */
    /** Push role onto a Principal.
     * This method is used to add a role to an existing principal.
     * @param user An existing UserPrincipal or null for an anonymous user.
     * @param role The role to add.
     * @return A new UserPrincipal object that wraps the passed user, but
     * with the added role.
     */
    public UserPrincipal pushRole(UserPrincipal user, String role);


    /* ------------------------------------------------------------ */
    /** Pop role from a Principal.
     * @param user A UserPrincipal previously returned from pushRole
     * @return The principal without the role.  Most often this will be the
     * original UserPrincipal passed.
     */
    public UserPrincipal popRole(UserPrincipal user);
    
}
