//
// $Id: ROCConfig.java,v 1.3 2003/03/17 03:30:45 candea Exp $
//

package roc.config;

public class ROCConfig 
{
   private ROCConfig() 
      {
	 // don't instantiate this class.
      }

   // If TRUE, enables RR changes to JBoss
   public static final boolean RR = true;

   // If TRUE, disables class loader destruction upon EJB
   // undeployment.  We should turn this off if we see side effects,
   // or if we want to change the EJB class inbetween hot deployments.
   // This pertains to
   // system/src/main/org/jboss/deployment/DeploymentInfo.java.
   public static final boolean RR_DISABLE_UCL_DESTROY = true;
    
   // If TRUE, runs a performability experiment.  This means that
   // RestartAgent will notify FaultInjector whenever recovery
   // completes, so that the next fault can be injected.
   public static final boolean RR_DO_PERFORMABILITY_EXPERIMENT = false;

   // if true, Pinpoint tracing and message reporting will
   //          be turned on.  if this is false, tracing will
   //          be turned off, and the rest of the PINPOINT_TRACING
   //          options will be ignored.
   public static final boolean ENABLE_PINPOINT = false;
    
   // if true, Pinpoint will trace database queries
   public static final boolean ENABLE_PINPOINT_TRACING_DB = true;
    
   // if true, Pinpoint will follow a trace across RMI calls
   public static final boolean ENABLE_PINPOINT_TRACING_RMI = true;
    
   // if true, Pinpoint will trace EJB invocations
   public static final boolean ENABLE_PINPOINT_TRACING_EJB = true;
    
   // if true, Pinpoint will trace JSP and servlet invocations
   public static final boolean ENABLE_PINPOINT_TRACING_SERVLET = true;
    
   // if true, Pinpoint will trace Http connections when they enter
   //          the mortbay http server
   public static final boolean ENABLE_PINPOINT_TRACING_HTTP = true;

   // if true, Pinpoint fault injection will be turned on.
   //    NOTE: FAULT_INJECTION will only be enabled in areas of
   //          JBoss that are being traced.  E.g., to inject faults
   //          in servlets, ENABLE_PINPOINT_TRACING_SERVLET must be
   //          enabled as well.
   public static final boolean ENABLE_PINPOINT_FAULT_INJECTION = true;
}
