package org.jboss.admin.dataholder;

// standard imports
import java.io.Serializable;

import javax.jms.Message;
import javax.jms.JMSException;


/**
 * ...
 *
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class BeanCacheEntry implements Serializable {

    private String application = "<undefined>";
    private String bean        = "<undefined>";
    private String type        = "<undefined>";
    private String activity    = "<undefined>";
    private long   time        = 0;
    private int    size        = 0;
    private int    capacity    = 0;
    private int    oldCapacity = 0;
    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */
 
    public BeanCacheEntry() {}

    public BeanCacheEntry(String application, String bean,
                          String type, long time) {
        
        //setApplication(application);
        setBean(bean);
        setType(type);
        setTime(time);
    }

    public BeanCacheEntry(Message msg) throws JMSException {
        
        //setApplication(msg.getStringProperty("APPLICATION"));
        setBean(msg.getStringProperty("BEAN"));
        setType(msg.getStringProperty("TYPE"));
        setTime(msg.getLongProperty("TIME"));
        
        if (getType().equals("RESIZER")) {
            setOldCapacity(msg.getIntProperty("OLD_CAPACITY"));
            setCapacity(msg.getIntProperty("NEW_CAPACITY"));
            setSize(msg.getIntProperty("SIZE"));
        }
        
        if (getType().equals("PASSIVATION")) {
            setActivity(msg.getStringProperty("ACTIVITY"));    
        }
        
        if (getType().equals("ACTIVATION")) {
            // no additional information
        }
        
        if (getType().equals("OVERAGER")) {
            setSize(msg.getIntProperty("SIZE"));   
        }
        
        if (getType().equals("CACHE")) {
            setActivity(msg.getStringProperty("ACTIVITY"));
            
            if (getActivity().equals("REMOVE")) {
                setCapacity(msg.getIntProperty("CAPACITY"));
                setSize(msg.getIntProperty("SIZE"));
            }
            
            if (getActivity().equals("ADD")) {
                setCapacity(msg.getIntProperty("CAPACITY"));
                setSize(msg.getIntProperty("SIZE"));
            }
            
            if (getActivity().equals("CAPACITY")) {
                setOldCapacity(msg.getIntProperty("OLD_CAPACITY"));
                setCapacity(msg.getIntProperty("NEW_CAPACITY"));
                setSize(msg.getIntProperty("SIZE"));
            }
        }
    }
    
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */
 
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
    
    public void setType(String type) {
        if ((type == null) || ("").equals(type))
            return;
            
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public void setTime(long time) {
        this.time = time;
    }
    
    public long getTime() {
        return time;
    }
    
    public int getCapacity() {
        return capacity;
    }
    
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    
    public int getOldCapacity() {
        return oldCapacity;
    }
    
    public void setOldCapacity(int capacity) {
        this.oldCapacity = capacity;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public void setActivity(String activity) {
        this.activity = activity;
    }
    
    public String getActivity() {
        return activity;
    }
/*
 *************************************************************************
 *
 *      METHOD OVERRIDES
 *
 *************************************************************************
 */
 
    public String toString() {
        return application + "/" + bean + ", time: " + time;
    }

}
