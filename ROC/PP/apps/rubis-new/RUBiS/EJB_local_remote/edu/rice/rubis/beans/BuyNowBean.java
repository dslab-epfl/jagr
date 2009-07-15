package edu.rice.rubis.beans;

import java.rmi.*;
import javax.ejb.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

/**
 * BuyNowBean is an entity bean with "container managed persistence". 
 * The state of an instance is stored into a relational database. 
 * The following table should exist:<p>
 * <pre>
 * CREATE TABLE buy_now (
 *   id       INTEGER UNSIGNED NOT NULL UNIQUE,
 *   buyer_id INTEGER UNSIGNED NOT NULL,
 *   item_id  INTEGER UNSIGNED NOT NULL,
 *   qty      INTEGER,
 *   date     DATETIME,
 *   PRIMARY KEY(id),
 *   INDEX buyer (buyer_id),
 *   INDEX item (item_id)
 * );
 * </pre>
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */

public class BuyNowBean implements EntityBean 
{
  private EntityContext entityContext;
  private transient boolean isDirty; // used for the isModified function

  /* Class member variables */

  public Integer id;
  public Integer buyerId;
  public Integer itemId;
  public int     qty;
  public String  date;


  /**
   * Get BuyNow id.
   *
   * @return BuyNow id
   */
  public Integer getId()
  {
    return id;
  }

  /**
   * Get the buyer id which is the primary key in the users table.
   *
   * @return user id
   */
  public Integer getBuyerId()
  {
    return buyerId;
  }

  /**
   * Get the item id which is the primary key in the items table.
   *
   * @return item id
   */
  public Integer getItemId()
  {
    return itemId;
  }

  /**
   * Get how many of this item the user has bought.
   *
   * @return quantity of items for this BuyNow.
   */
  public int getQuantity()
  {
    return qty;
  }

  /**
   * Time of the BuyNow in the format 'YYYY-MM-DD hh:mm:ss'
   *
   * @return BuyNow time
   */
  public String getDate()
  {
    return date;
  }

  /**
   * Set a new buyer identifier. This id must match
   * the primary key of the users table.
   *
   * @param id buyer id
   */
  public void setBuyerId(Integer id)
  {
    buyerId = id;
    isDirty = true; // the bean content has been modified
  }

  /**
   * Set a new item identifier. This id must match
   * the primary key of the items table.
   *
   * @param id item id
   */
  public void setItemId(Integer id)
  {
    itemId = id;
    isDirty = true; // the bean content has been modified
  }

  /**
   * Set a new quantity for this BuyNow
   *
   * @param Qty quantity
   */
  public void setQuantity(int Qty)
  {
    qty = Qty;
    isDirty = true; // the bean content has been modified
  }

  /**
   * Set a new date for this BuyNow
   *
   * @param newDate BuyNow date
   */
  public void setDate(String newDate)
  {
    date = newDate;
    isDirty = true; // the bean content has been modified
  }


  /**
   * This method is used to create a new BuyNow Bean.
   * The date is automatically set to the current date when the method is called.
   *
   * @param BuyNowUserId user id of the buyer, must match the primary key of table users
   * @param BuyNowItemId item id, must match the primary key of table items
   * @param quantity number of items the user wants to buy
   *
   * @return pk primary key set to null
   * @exception CreateException if an error occurs
   */
  public BuyNowPK ejbCreate(Integer BuyNowUserId, Integer BuyNowItemId, int quantity) throws CreateException
  {
     // Connecting to IDManager Home interface thru JNDI
      IDManagerLocalHome home = null;
      IDManagerLocal idManager = null;
      
      try 
      {
        InitialContext initialContext = new InitialContext();
        home = (IDManagerLocalHome)initialContext.lookup("java:comp/env/ejb/IDManager");
      } 
      catch (Exception e)
      {
        throw new EJBException("Cannot lookup IDManager: " +e);
      }
     try 
      {
        IDManagerPK idPK = new IDManagerPK();
        idManager = home.findByPrimaryKey(idPK);
        id = idManager.getNextBuyNowID();
        buyerId = BuyNowUserId;
        itemId  = BuyNowItemId;
        qty     = quantity;
        date    = TimeManagement.currentDateToString();
      } 
     catch (Exception e)
     {
        throw new EJBException("Cannot create buyNow: " +e);
      }
    return null;
  }

  /** This method just set an internal flag to 
      reload the id generated by the DB */
  public void ejbPostCreate(Integer BuyNowUserId, Integer BuyNowItemId, int quantity)
  {
    isDirty = true; // the id has to be reloaded from the DB
  }

  /** Persistence is managed by the container and the bean
      becomes up to date */
  public void ejbLoad()
  {
    isDirty = false;
  }

  /** Persistence is managed by the container and the bean
      becomes up to date */
  public void ejbStore()
  {
    isDirty = false;
  }

  /** This method is empty because persistence is managed by the container */
  public void ejbActivate() {}
  /** This method is empty because persistence is managed by the container */
  public void ejbPassivate()  {}
  /** This method is empty because persistence is managed by the container */
  public void ejbRemove() {}

  /**
   * Sets the associated entity context. The container invokes this method 
   *  on an instance after the instance has been created. 
   * 
   * This method is called in an unspecified transaction context. 
   * 
   * @param context An EntityContext interface for the instance. The instance should 
   *              store the reference to the context in an instance variable. 
   * @exception EJBException  Thrown by the method to indicate a failure 
   *                          caused by a system-level error.
   */
  public void setEntityContext(EntityContext context)
  {
    entityContext = context;
  }

  /**
   * Unsets the associated entity context. The container calls this method 
   *  before removing the instance. This is the last method that the container 
   *  invokes on the instance. The Java garbage collector will eventually invoke 
   *  the finalize() method on the instance. 
   *
   * This method is called in an unspecified transaction context. 
   * 
   * @exception EJBException  Thrown by the method to indicate a failure 
   *                          caused by a system-level error.
   */
  public void unsetEntityContext()
  {
    entityContext = null;
  }

  /**
   * Returns true if the beans has been modified.
   * It prevents the EJB server from reloading a bean
   * that has not been modified.
   *
   * @return a <code>boolean</code> value
   */
  public boolean isModified() 
  {
    return isDirty;
  }
}