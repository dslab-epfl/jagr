/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ha.framework.interfaces;


import org.jboss.ha.framework.interfaces.DistributedState.DSListener;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;

/**
 * DistributedState is a service on top of HAPartition that provides a cluster-wide distributed state.
 * First, the DistributedState (DS) service provides a spliting in categories. Thus, any
 * service, application, container, ... can request its own DS "private space" by working
 * in its own category (a string name).
 * Then, it is possible to work in a category like with a Dictionary: you set values to key.
 * Each time a value is added/modified/removed, the modification is made cluster-wide,
 * on all other nodes.
 * Reading values is always made locally (no network access!)
 * Objects can also subscribes to DS events to be notified when some values gets
 * modified/removed/added in a particular category.
 *
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2001/12/31: Sacha Labourey</b>
 * <ol>
 *   <li>Allow caller to select if cluster call is synchronous or asynchronous</li>
 * </ol>
 * @see HAPartition
 */
public interface DistributedState
{
   /**
    * When a particular key in a category of the DistributedState service gets modified,
    * all listeners will be notified of DS changes for that category.
    */
   public interface DSListener
   {
      /**
       * Called whenever a key has been added or modified in the category the called object
       * has subscribed in.
       * @param category The category of the modified/added entry
       * @param key The key that has been added or its value modified
       * @param value The new value of the key
       */
      public void valueHasChanged (String category, String key, Serializable value, boolean locallyModified);
      /**
       * Called whenever a key has been removed from a category the called object had
       * subscribed in.
       * @param category The category under which a key has been removed
       * @param key The key that has been removed
       * @param previousContent The previous content of the key that has been removed
       */
      public void keyHasBeenRemoved (String category, String key, Serializable previousContent, boolean locallyModified);
   }
   
   /**
    * Subscribes to receive {@link DistributedState.DSListener} events
    * @param category Name of the private-space to watch for
    * @param subscriber Object that will receive callbacks
    */
   public void registerDSListener (String category, DSListener subscriber);
   /**
    * Subscribes from {@link DistributedState.DSListener} events
    * @param category Name of the private-space dictionary currently observed
    * @param subscriber object currently observing this category
    */
   public void unregisterDSListener (String category, DSListener subscriber);
   
   // State binding methods
   //
   /**
    * Associates a value to a key in a specific category
    * @param category Name of the private naming-space
    * @param key Name of the data to set
    * @param value Value of the data to set
    * @throws Exception If a network communication occurs
    */
   public void set (String category, String key, Serializable value) throws Exception;
   /**
    * Same as set(String, String) but caller can choose if the call is made
    * synchronously or asynchronously. By default, calls are asynchronous.
    */
   public void set (String category, String key, Serializable value, boolean asynchronousCall) throws Exception;
   
   /**
    * Read in a value associated to a key in the given category. Read is performed locally.
    * @param category Name of the private naming-space
    * @param key The key of the value to read
    * @return The value of the key in the given category
    */
   public Serializable get (String category, String key);   
   
   /**
    * Return a list of all categories. Call managed locally: no network access.
    * @return A collection of String representing the existing categories in the DS service.
    */
   public Collection getAllCategories ();
   
   /**
    * Return a list of all keys in a category. Call managed locally: no network access.
    * @param category The category under which to look for keys
    * @return A collection of all keys in the give category
    */
   public Collection getAllKeys (String category);
   
   /**
    * Return a list of all values in a category. Call managed locally: no network access.
    * @param category The category name under which to look for values
    * @return A collection of all values in the give category
    */
   public Collection getAllValues (String category);
   
   /**
    * Remove the key from the ReplicationService in the given category
    * @param category Name of the category
    * @param key Key to be removed
    * @throws Exception if a network exception occurs while removing the entry.
    */
   public Serializable remove (String category, String key) throws Exception;
   /**
    * Same as remove(String, String) but caller can choose if the call is made
    * synchronously or asynchronously. By default, calls are asynchronous.
    */
   public Serializable remove (String category, String key, boolean asynchronousCall) throws Exception;
}