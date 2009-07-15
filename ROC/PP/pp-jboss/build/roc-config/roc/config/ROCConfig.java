package roc.config;

public class ROCConfig {

    private ROCConfig() {
	// don't instantiate this class.
    }
    
    // if true, Pinpoint tracing and message reporting will
    //          be turned on.  if this is false, tracing will
    //          be turned off, and the rest of the PINPOINT_TRACING
    //          options will be ignored.
    public static final boolean ENABLE_PINPOINT = true;
    
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

    // if true, Pinpoint will trace JNDI requests
    public static final boolean ENABLE_PINPOINT_TRACING_JNDI = true;

    // if true, Pinpoint will associate paths that communicate with one
    //          another over JMS queues/topics
    public static final boolean ENABLE_PINPOINT_TRACING_JMS = true;

    // if true, Pinpoint fault injection will be turned on.
    //    NOTE: FAULT_INJECTION will only be enabled in areas of
    //          JBoss that are being traced.  E.g., to inject faults
    //          in servlets, ENABLE_PINPOINT_TRACING_SERVLET must be
    //          enabled as well.
    public static final boolean ENABLE_PINPOINT_FAULT_INJECTION = true;

}
