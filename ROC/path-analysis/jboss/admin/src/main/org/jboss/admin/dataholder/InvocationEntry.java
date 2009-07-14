package org.jboss.admin.dataholder;

// standard imports
import java.io.Serializable;

import javax.jms.Message;
import javax.jms.JMSException;
import javax.transaction.Status;


/**
 * ...
 *
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class InvocationEntry implements Serializable {

    private String checkPoint  = "<undefined>";
    private String application = "<undefined>";
    private String bean        = "<undefined>";
    private String method      = "<undefined>";
    private String txID        = "<undefined>";
    
    private long   time        = 0;
    private int    status      = Status.STATUS_UNKNOWN;
    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */
 
    public InvocationEntry() {}

    public InvocationEntry(String checkpoint, String application, String bean,
                           String txID,   int status,  long time) {
        
        setCheckPoint(checkpoint);
        setApplication(application);
        setBean(bean);
        setTxID(txID);
        setStatus(status);
        setTime(time);
    }

    public InvocationEntry(Message msg) throws JMSException {
        
        setCheckPoint(msg.getStringProperty("CHECKPOINT"));
        setApplication(msg.getStringProperty("APPLICATION"));
        setBean(msg.getStringProperty("BEAN"));
        setMethod(msg.getStringProperty("METHOD"));
        setTxID(msg.getStringProperty("ID"));
        setTime(msg.getLongProperty("TIME"));
    }
    
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */
 
    public void setCheckPoint(String checkpoint) {
        if ((checkpoint == null) || ("").equals(checkpoint))
            return;
            
        this.checkPoint = checkpoint;
    }
    
    public String getCheckPoint() {
        return checkPoint;
    }
    
    public void setApplication(String applicationName) {
        if ((applicationName == null) || ("").equals(applicationName))
            return;
            
        this.application = applicationName;
    }
    
    public String getApplication() {
        return application;
    }
    
    public void setBean(String beanName) {
        if ((beanName == null) || ("").equals(beanName))
            return;
            
        this.bean = beanName;
    }
    
    public String getBean() {
        return bean;
    }
    
    public void setMethod(String method) {
        if ((method == null) || ("").equals(method))
            return;
            
        this.method = method;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setTxID(String txID) {
        if ((txID == null) || ("").equals(txID))
            return;
            
        this.txID = txID;
    }
    
    public String getTxID() {
        return txID;
    }
    
    public void setTime(long time) {
        this.time = time;
    }
    
    public long getTime() {
        return time;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
/*
 *************************************************************************
 *
 *      METHOD OVERRIDES
 *
 *************************************************************************
 */
 
    public String toString() {
        return checkPoint + ": " + application + "/" + bean + " [TxID:"  +
               txID       + ", " + printTxStatus(status)    + ", time: " +
               time       + "]";
    }
    
/*
 *************************************************************************
 *
 *      PRIVATE INSTANCE METHODS
 *
 *************************************************************************
 */
 
    private String printTxStatus(int status) {
     
        String stat = "";
        
        switch (status) {
            
        case Status.STATUS_ACTIVE:          stat = "ACTIVE";
            break;
        case Status.STATUS_COMMITTED:       stat = "COMMITTED";
            break;
        case Status.STATUS_COMMITTING:      stat = "COMMITTING";
            break;
        case Status.STATUS_MARKED_ROLLBACK: stat = "MARKED_ROLLBACK";
            break;
        case Status.STATUS_NO_TRANSACTION:  stat = "NO_TRANSACTION";
            break;
        case Status.STATUS_PREPARED:        stat = "STATUS_PREPARED";
            break;
        case Status.STATUS_PREPARING:       stat = "STATUS_PREPARING";
            break;
        case Status.STATUS_ROLLEDBACK:      stat = "STATUS_ROLLEDBACK";
            break;
        case Status.STATUS_ROLLING_BACK:    stat = "STATUS_ROLLING_BACK";
            break;
        case Status.STATUS_UNKNOWN:         stat = "STATUS_UNKNOWN";
            break;
            
        default:
            stat = "<undefined>";            
        }
        
        return stat;
    }

               
}
