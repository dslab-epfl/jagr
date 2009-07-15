/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.invocation;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.security.Principal;

import javax.transaction.Transaction;

/**
 * The Invocation object is the generic object flowing through our interceptors.
 *
 * <p>The heart of it is the payload map that can contain anything we then put readers on them
 *    The first "reader" is this "Invocation" object that can interpret the data in it.
 *
 * <p>Essentially we can carry ANYTHING from the client to the server, we keep a series of
 *    of predifined variables and method calls to get at the pointers.  But really it is just
 *    a repository of objects.
 *
 * @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.1.1.1 $
 */
public class Invocation
{
   /** The signature of the invoke() method */
   public static final String[] INVOKE_SIGNATURE = {"java.lang.Object"};

   /**
    * The payload is a repository of everything associated with the invocation
    * It is information that will need to travel
    */

   // Contextual information to the invocation that is not part of the payload
   public Map transient_payload = null;

   /** as_is classes that will not be marshalled by the invocation
    * (java.* and javax.* or anything in system classpath is OK)
    */
   public Map as_is_payload = null;

   // Payload will be marshalled for type hiding at the RMI layers
   public Map payload = null;

   protected InvocationContext invocationContext = null;
   protected Object[] args = null;
   protected Object objectName = null;
   protected Method method = null;


   /**
    * For invocation pooling
    */
   public void clear()
   {
      invocationContext = null;
      args = null;
      objectName = null;
      method = null;
      transient_payload.clear();
      as_is_payload.clear();
      payload.clear();
   }

   // The variables used to indicate what type of data and where to put it
   public final static int
         // Put me in the transient map, not part of payload
         TRANSIENT = 1,
   // Do not serialize me, part of payload as is
   AS_IS = 0,
   // Put me in the payload map
   PAYLOAD = 2;


   /**
    * We are using the generic payload to store some of our data, we define some integer entries.
    * These are just some variables that we define for use in "typed" getters and setters.
    * One can define anything either in here explicitely or through the use of external calls to getValue
    */
   public static final Integer
         // Transactional information with the invocation
         TRANSACTION = new Integer(new String("TRANSACTION").hashCode()),
   PRINCIPAL = new Integer(new String("PRINCIPAL").hashCode()),
   CREDENTIAL = new Integer(new String("CREDENTIAL").hashCode()),

   // We can keep a reference to an abstract "container" this invocation is associated with
   OBJECT_NAME = new Integer(new String("CONTAINER").hashCode()),

   // The type can be any qualifier for the invocation, anything (used in EJB)
   TYPE = new Integer(new String("TYPE").hashCode()),

   // The Cache-ID associates an instance in cache somewhere on the server with this invocation
   CACHE_ID = new Integer(new String("CACHE_ID").hashCode()),

   // The invocation can be a method invocation, we give the method to call
   METHOD = new Integer(new String("METHOD").hashCode()),

   // The arguments of the method to call
   ARGUMENTS = new Integer(new String("ARGUMENTS").hashCode()),

   // Invocation context
   INVOCATION_CONTEXT = new Integer(new String("INVOCATION_CONTEXT").hashCode()),

   // Enterprise context
   ENTERPRISE_CONTEXT = new Integer(new String("ENTERPRISE_CONTEXT").hashCode());

   public static final int
         REMOTE = 0,
   LOCAL = 1,
   HOME = 2,
   LOCALHOME = 3;

   public final static Integer[] invocationTypes = {new Integer(0), new Integer(1), new Integer(2), new Integer(3)};

   public static String getInvocationTypeName(int type)
   {
      String typeName = "UNKNOWN";
      switch (type)
      {
         case REMOTE:
            typeName = "REMOTE";
            break;
         case LOCAL:
            typeName = "LOCAL";
            break;
         case HOME:
            typeName = "HOME";
            break;
         case LOCALHOME:
            typeName = "LOCALHOME";
            break;
      }
      return typeName;
   }

   /**
    * Exposed for externalization only.
    */
   public Invocation()
   {
      payload = new HashMap();
      as_is_payload = new HashMap();
      transient_payload = new HashMap();
   }

   public Invocation(Object id, Method m, Object[] args, Transaction tx,
         Principal identity, Object credential)
   {
      this.payload = new HashMap();
      this.as_is_payload = new HashMap();
      this.transient_payload = new HashMap();

      setId(id);
      setMethod(m);
      setArguments(args);
      setTransaction(tx);
      setPrincipal(identity);
      setCredential(credential);
   }

   //
   // The generic getter and setter is really all that one needs to talk to this object
   // We introduce typed getters and setters for convenience and code readability in the codebase
   //

   //The generic store of variables
   public void setValue(Object key, Object value)
   {
      setValue(key, value, PAYLOAD);
   }

   // Advanced store
   // Here you can pass a TYPE that indicates where to put the value.
   // TRANSIENT: the value is put in a map that WON'T be passed
   // AS_IS: no need to marshall the value when passed (use for all JDK java types)
   // PAYLOAD: we need to marshall the value as its type is application specific
   public void setValue(Object key, Object value, int TYPE)
   {
      switch (TYPE)
      {

         case TRANSIENT:
            transient_payload.put(key, value);
            break;

         case AS_IS:
            as_is_payload.put(key, value);
            break;

         case PAYLOAD:
            payload.put(key, value);
            break;
      }
   }

   // Get a value from the stores
   public Object getValue(Object key)
   {
      // find where it is
      Object rtn = payload.get(key);
      if (rtn != null) return rtn;
      rtn = as_is_payload.get(key);
      if (rtn != null) return rtn;
      rtn = transient_payload.get(key);
      return rtn;
   }

   public Object getPayloadValue(Object key)
   {
      return payload.get(key);
   }

   //
   // Convenience typed getters, use pre-declared keys in the store,
   // but it all comes back to the payload, here you see the usage of the
   // different payloads.  Anything that has a well defined type can go in as_is
   // Anything that is arbitrary and depends on the application needs to go in
   // in the serialized payload.  The "Transaction" is known, the type of the
   // method arguments are not for example and are part of the EJB jar.
   //

   // set and get on transaction
   public void setTransaction(Transaction tx)
   {
      as_is_payload.put(TRANSACTION, tx);
   }

   public Transaction getTransaction()
   {
      return (Transaction) as_is_payload.get(TRANSACTION);
   }

   //  Change the security identity of this invocation.
   public void setPrincipal(Principal principal)
   {
      as_is_payload.put(PRINCIPAL, principal);
   }

   public Principal getPrincipal()
   {
      return (Principal) as_is_payload.get(PRINCIPAL);
   }

   //  Change the security credentials of this invocation.
   public void setCredential(Object credential)
   {
      payload.put(CREDENTIAL, credential);
   }

   public Object getCredential()
   {
      return getPayloadValue(CREDENTIAL);
   }

   // A container for server side association
   public void setObjectName(Object objectName)
   {
      this.objectName = objectName;
   }

   public Object getObjectName()
   {
      return objectName;
   }

   // An arbitrary type
   public void setType(int type)
   {
      as_is_payload.put(TYPE, invocationTypes[type]);
   }

   public int getType()
   {
      int type = LOCAL;
      Integer invType = (Integer) as_is_payload.get(TYPE);
      if (invType != null)
         type = invType.intValue();
      return type;
   }

   // Return the invocation target ID.  Can be used to identify a cached object
   public void setId(Object id)
   {
      payload.put(CACHE_ID, id);
   }

   public Object getId()
   {
      return getPayloadValue(CACHE_ID);
   }

   // set and get on method Return the invocation method.
   public void setMethod(Method method)
   {
      this.method = method;
   }

   public Method getMethod()
   {
      return method;
   }

   // A list of arguments for the method
   public void setArguments(Object[] arguments)
   {
      args = arguments;
   }

   public Object[] getArguments()
   {
      return args;
   }

   public InvocationContext getInvocationContext()
   {
      return invocationContext;
   }

   public void setInvocationContext(InvocationContext ctx)
   {
      invocationContext = ctx;
   }

   public void setEnterpriseContext(Object ctx)
   {
      transient_payload.put(ENTERPRISE_CONTEXT, ctx);
   }

   public Object getEnterpriseContext()
   {
      return transient_payload.get(ENTERPRISE_CONTEXT);
   }

}

/*
vim:ts=3:sw=3:et
*/
