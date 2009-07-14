package roc.pinpoint.tracing;

/**
 * A ThreadLocal implementation of a RequestMarker. The marker is local
 * to the current thread so that it's preserved across software components.
 *
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">
 *                mikechen@cs. berkeley.edu</A>)
 */

public class ThreadedRequestTracer {

    // a variable local to the current thread
    private static ThreadLocal threadLocalVar = new ThreadLocal();

    /**
     * returns the request info associated with the current thread. creates a
     * new request info if necessary.
     * @return RequestInfo request info
     */
    public static RequestInfo getRequestInfo() {
        RequestInfo info = (RequestInfo) threadLocalVar.get();
        if (info == null) {
            info = new RequestInfo();
            threadLocalVar.set(info);
        }
        return info;
    }

    /**
     * set the request info associated with the current thread.
     * @param info request info
     */
    public static void setRequestInfo(RequestInfo info) {
        threadLocalVar.set(info);
    }

}
