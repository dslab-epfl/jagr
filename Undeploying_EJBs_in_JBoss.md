# IN CONSTRUCTION !! #

# Undeploying an EJB in JBoss 3.2.x #


When the MainDeployer is asked to undeploy an EJB, it first "stops" the EJB and then "destroys" it.

  1. MainDeployer asks the EJBDeployer to stop the deployment unit corresponding to the EJB module.
> 2. The EJBDeployer asks the ServiceController to stop the MBean corresponding to the EJB, which leads to the corresponding EjbModule being invoked, the MBean's stop method, and finally the EJB's container's stopService(), further described below.
> 3. EJBDeployer calls its superclass destroy method, which sends a notification that the EJB is stopped; this leads among others to the corresponding MBean being deregistered.
> 4. After that, the MainDeployer recursively stops all sub-deployments corresponding to the just-stopped EJB module.
> 5. The MainDeployer's destroy phase starts with the EJBDeployer removing the EJB module from its list of deployments, then asking the ServiceController to destroy and remove the MBean corresponding to the EJB.
> 6. The ServiceController recursively destroys all the MBeans depending on this one, then it asks the EjbModule to destroy the EJB and corresponding MBean; this results in an invocation to the container's destroyService(), further described below.
> 7. ServiceController sends a notification that the EJB is stopped, which leads among others to the corresponding MBean being deregistered.
> 8. MainDeployer recursively destroys all sub-deployments under the just-destroyed EJB module.
> 9. Finally, the MainDeployer nukes the state maintained on behalf of the just-undeployed EJB module: it is removed from the local and/or waiting deployment list, the corresponding class loader is removed, removes any deployment-specific repository, and deletes the file corresponding to the EJB module.

Remember that isolation among containers is provided by each container having its own classloader; when an invocation is made on that container, the thread needs to switch to its classloader.

## Stop/Destroy Actions by EJB Type ##

**org.jboss.ejb.Container**

> This is the superclass for all containers; their respective methods override but eventually invoke these generic methods.

> stopService() sets a flag indicating the container is no longer active, then stops the local proxy factory, and tears down the environment (i.e., unbinds in JNDI everything from the comp/env context).

> destroyService() destroys the local proxy factory, removes the local home (??) of the container, and nulls out the class loaders and the corresponding EJB module reference.

**org.jboss.ejb.StatelessSessionContainer
org.jboss.ejb.MessageDrivenContainer**

> stopService() invokes the superclass' stopService(), then stops all container plugins (container invoker), stops the instance pool, and stops all interceptors in the chain.

> destroyService()destroys all container plugins (container invoker) and nulls out ref to container, destroys the instance pool and nulls out ref to container, destroys all interceptors in the chain and nulls out their refs to container, and finally invokes the superclass' destroyService() method.

**org.jboss.ejb.StatefulSessionContainer**

> Same as the previous case, except it also stops/destroys the instance cache and the persistence manager, immediately after stopping/destroying the instance cache.

**org.jboss.ejb.EntityContainer**

> stopService() stops all interceptors (this way it removes CachedConnectionInterceptor before stopping the persistence manager), stops the instance pool, stops the persistence manager, stops the instance cache, and stops all container plugins (container invoker).

  * Persistence manager stop: if using the JDBC persistence manager, this results in the DB table corresponding to the entity EJB being dropped, along with all the tables corresponding to the entity's CMR fields. The readahead cache is cleared.

> destroyService() destroys all container plugins (container invoker) and nulls out ref to container, destroys instance cache and nulls out ref to container, destroys persistence manager and nulls out ref to container, destroys the instance pool and nulls out ref to container, destroys all interceptors in the chain and nulls out their refs to container, and finally invokes the superclass' destroyService().

  * Container plugin destroy:
  * Persistence manager destroy: if using JDBC, destroy consists of nulling out the readahead cache (thus making it potentially GC-able), clearing all mappings from query methods to query commands in the query manager and nulling it out, and removing the proxy so the class loader can be released.

## Stop/Destroy Actions for Interceptors ##

Most interceptors have empty stop() and destroy() methods. In this section I described the exceptions (only those configured by default in standardjboss.xml):

**org.jboss.ejb.plugins.CleanShutdownInterceptor**

This interceptor can introduce significant delays during undeploy. It tracks the incoming invocations and, when the container is being stop-ed or destroy-ed, it waits for current invocations to finish before returning from the stop or destroy call. This interceptor is used for clustered EJBs, because in a cluster shutting down a node doesn't necessarily mean that an application cannot be reached.
org.jboss.ejb.plugins.AbstractInstanceCache

This is a base class for caches of entity and stateful beans. Upon stop() or destroy(), it will try to acquire the cache lock and stop (empty and deregister) or destroy it, respectively.

**org.jboss.resource.connectionmanager.CachedConnectionInterceptor**

When instantiated, this interceptor interposes on the container's ref to the persistence manager with a ref to itself, to intercept and cache connection. When the interceptor is stop-ed, it un-interposes itself by re-establishing the container's ref to the original persistence manager. The interceptor also clears its own list of unshareable resources.

**org.jboss.ejb.plugins.EntitySynchronizationInterceptor**

This interceptor synchronizes the cache (??) with the underlying storage; this is done by a context refresher thread. Upon stop-ing the interceptor, its ref to the thread is nulled and the thread is interrupted (presumably to be GC-able?).

**org.jboss.cache.invalidation.triggers.EntityBeanCacheBatchInvalidatorInterceptor**

Cleans up some references and usage counter.

**org.jboss.ejb.plugins.MetricsInterceptor**

Collects data from the bean invocation call and publishes them on a JMS topic; upon destroy() it interrupts the publishing thread.

**org.jboss.ejb.plugins.AbstractTxInterceptorBMT**

This is a common superclass for BMT transaction interceptors; upon stop() it simply unbinds "java:comp/UserTransaction" from JNDI.