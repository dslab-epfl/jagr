/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.modelmbean;

public interface ModelMBeanConstants
{
   // Model MBean notification type string
   final static String GENERIC_MODELMBEAN_NOTIFICATION = "jmx.modelmbean.generic";
   
   // Model MBean resource types
   final static String OBJECT_REF               = "ObjectReference";
   //final static String CLASS_NAME             = "ClassName";

   // mandatory descriptor fields
   final static String NAME                     = "name";
   final static String DESCRIPTOR_TYPE          = "descriptorType";
   final static String ROLE                     = "role";
   
   // descriptor types
   final static String MBEAN_DESCRIPTOR         = "MBean";//should this be mbean? as in XMLMetaData
   final static String ATTRIBUTE_DESCRIPTOR     = "attribute";
   final static String OPERATION_DESCRIPTOR     = "operation";
   final static String NOTIFICATION_DESCRIPTOR  = "notification";
   final static String CONSTRUCTOR_DESCRIPTOR   = "constructor";
   //not so sure about this guy
   final static String DESCRIPTOR = "descriptor";
   
   final static String ALL_DESCRIPTORS          = null;
   
   // roles
   final static String GETTER                   = "getter";
   final static String SETTER                   = "setter";
   final static String CONSTRUCTOR              = "constructor";
   
   // optional descriptor fields
   final static String VISIBILITY               = "visibility";
   final static String LOG                      = "log";
   final static String EXPORT                   = "export";
   final static String DISPLAY_NAME             = "displayName";
   final static String VALUE                    = "value";
   final static String GET_METHOD               = "getMethod";
   final static String SET_METHOD               = "setMethod";
   final static String PERSIST_POLICY           = "persistPolicy";
   final static String PERSIST_PERIOD           = "persistPeriod";
   final static String PERSIST_NAME             = "persistName";
   final static String PERSIST_LOCATION         = "persistLocation";
   final static String CURRENCY_TIME_LIMIT      = "currencyTimeLimit";
   final static String LAST_UPDATED_TIME_STAMP  = "lastUpdatedTimeStamp";
  
   // visibility values
   final static String HIGH_VISIBILITY          = "1";
   final static String NORMAL_VISIBILITY        = "2";
   final static String LOW_VISIBILITY           = "3";
   final static String MINIMAL_VISIBILITY       = "4";
   
   // persist policies
   final static String ON_UPDATE                = "OnUpdate";
   final static String NO_MORE_OFTEN_THAN       = "NoMoreOftenThan";
   final static String NEVER                    = "Never";
   final static String ON_TIMER                 = "OnTimer";
   final static String[] PERSIST_POLICY_LIST = {NEVER, ON_UPDATE, NO_MORE_OFTEN_THAN, ON_TIMER};
         
   // Constants for metadata objects
   final static boolean IS_READABLE             = true;
   final static boolean IS_WRITABLE             = true;
   final static boolean IS_IS                   = true;


   //impact
   final static String ACTION = "ACTION";
   final static String ACTION_INFO = "ACTION_INFO";
   final static String INFO = "INFO";

   //these are jboss specific.
   final static String STATE_ACTION_ON_UPDATE = "state-action-on-update";
   final static String KEEP_RUNNING = "KEEP_RUNNING";
   final static String RESTART = "RESTART";
   final static String REINSTANTIATE = "REINSTANTIATE";
   final static String[] STATE_ACTION_ON_UPDATE_LIST = {KEEP_RUNNING, RESTART, REINSTANTIATE};
}

