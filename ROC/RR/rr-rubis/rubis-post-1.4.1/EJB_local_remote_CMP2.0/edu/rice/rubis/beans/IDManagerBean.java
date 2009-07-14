package edu.rice.rubis.beans;

import java.rmi.RemoteException;
import java.util.Properties;
import javax.ejb.*;
import java.io.Serializable;

/**
 * IDManagerBean is used to generate id since the AUTO_INCREMENT
 * feature of the database that automatically generate id on the primary key 
 * is not supported by JBoss. 
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.1
 */

public abstract class IDManagerBean implements EntityBean 
{
  private EntityContext entityContext;
  private transient boolean isDirty; // used for the isModified function

  /****************************/
  /* Abstract accessor methods*/
  /****************************/

  public abstract Integer getId();
  public abstract void setId(Integer id);

  public abstract Integer getCategoryCount();
  public abstract void setCategoryCount(Integer c);

  public abstract Integer getRegionCount();
  public abstract void setRegionCount(Integer r);

  public abstract Integer getUserCount();
  public abstract void setUserCount(Integer u);


  public abstract Integer getItemCount();
  public abstract void setItemCount(Integer i);


  public abstract Integer getCommentCount();
  public abstract void setCommentCount(Integer co);

  public abstract Integer getBidCount();
  public abstract void setBidCount(Integer bid);

  public abstract Integer getBuyNowCount();
  public abstract void setBuyNowCount(Integer buy);


  /** 
   * Generate the category id.
   *
   * @return Value of the ID
   * @since 1.1
   */
  public Integer getNextCategoryID()
  {
    setCategoryCount(new Integer(getCategoryCount().intValue()+1));
    isDirty = true; // the bean content has been modified
    return getCategoryCount();
  }

  /** 
   * Generate the region id.
   *
   * @return Value of the ID
   * @since 1.1
   */
  public Integer getNextRegionID()
  {
    setRegionCount(new Integer(getRegionCount().intValue()+1));
    isDirty = true; // the bean content has been modified
    return getRegionCount();
  }

  /** 
   * Generate the user id.
   *
   * @return Value of the ID
   * @since 1.1
   */
  public Integer getNextUserID()
  {
    setUserCount(new Integer(getUserCount().intValue()+1));
    isDirty = true; // the bean content has been modified
    return getUserCount();
  }

  /** 
   * Generate the item id.
   *
   * @return Value of the ID
   * @since 1.1
   */
  public Integer getNextItemID()
  {
    setItemCount(new Integer(getItemCount().intValue()+1));
    isDirty = true; // the bean content has been modified
    return getItemCount();
  }

  /** 
   * Generate the comment id.
   *
   * @return Value of the ID
   * @since 1.1
   */
  public Integer getNextCommentID()
  {
    setCommentCount(new Integer(getCommentCount().intValue()+1));
    isDirty = true; // the bean content has been modified
    return getCommentCount();
  }

  /** 
   * Generate the bid id.
   *
   * @return Value of the ID
   * @since 1.1
   */
  public Integer getNextBidID()
  {
    setBidCount(new Integer(getBidCount().intValue()+1));
    isDirty = true; // the bean content has been modified
    return getBidCount();
  }

  /** 
   * Generate the buyNow id.
   *
   * @return Value of the ID
   * @since 1.1
   */
  public Integer getNextBuyNowID()
  {
    setBuyNowCount(new Integer(getBuyNowCount().intValue()+1));
    isDirty = true; // the bean content has been modified
    return getBuyNowCount();
  }


  // ======================== EJB related methods ============================

  /**
   * This method is used to create a new IDManager Bean but should never be called.
   *
   * @return pk primary key set to null
   * @exception CreateException if an error occurs
   * @exception RemoteException if an error occurs
   * @exception RemoveException if an error occurs
   */
  public IDManagerPK ejbCreate() throws CreateException
  {
    throw new CreateException();    
  }

  /**
   * This method is used to create a new IDManager Bean.
   *
   * @return pk primary key set to null
   * @exception CreateException if an error occurs
   * @exception RemoteException if an error occurs
   * @exception RemoveException if an error occurs
   */
  public IDManagerPK ejbCreate(IDManagerPK pk) throws CreateException
  {
      setId(pk.id);
      int k = pk.id.intValue();
      //if k==0, then IDManager table is already populated.
      if( k != 0 )
      {
	  setCategoryCount(new Integer(0));
	  setRegionCount(new Integer(0));
          setUserCount(new Integer(k*IDManagerPK.QUANTITY_PER_HOST));
          setItemCount(new Integer(k*IDManagerPK.QUANTITY_PER_HOST));
          setCommentCount(new Integer(k*IDManagerPK.QUANTITY_PER_HOST));
          setBidCount(new Integer(k*IDManagerPK.QUANTITY_PER_HOST));
          setBuyNowCount(new Integer(k*IDManagerPK.QUANTITY_PER_HOST));
      }
      return pk;    
  }


  /** This method does currently nothing */
  public void ejbPostCreate()  throws CreateException {}
  /** This method does currently nothing */
  public void ejbPostCreate(IDManagerPK pk)  throws CreateException {}
  /** This method is empty because persistence is managed by the container */
  public void ejbActivate() {}
  /** This method is empty because persistence is managed by the container */
  public void ejbPassivate() {}
  /** This method is empty because persistence is managed by the container */
  public void ejbRemove() {}

  /** Persistence is managed by the container and the bean
      becomes up to date */
  public void ejbStore()
  {
    isDirty = false;
  }
  /** Persistence is managed by the container and the bean
      becomes up to date */
  public void ejbLoad()
  {
    isDirty = false;
  }

  /**
   * Sets the associated entity context. The container invokes this method 
   *  on an instance after the instance has been created. 
   * 
   * This method is called in an unspecified transaction context. 
   * 
   * @param context - An EntityContext interface for the instance. The instance should 
   *              store the reference to the context in an instance variable. 
   * @exception EJBException  Thrown by the method to indicate a failure 
   *                          caused by a system-level error.
   * @exception RemoteException - This exception is defined in the method signature
   *                           to provide backward compatibility for enterprise beans
   *                           written for the EJB 1.0 specification. 
   *                           Enterprise beans written for the EJB 1.1 and 
   *                           higher specification should throw the javax.ejb.EJBException 
   *                           instead of this exception. 
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
   * @exception RemoteException - This exception is defined in the method signature
   *                           to provide backward compatibility for enterprise beans
   *                           written for the EJB 1.0 specification. 
   *                           Enterprise beans written for the EJB 1.1 and 
   *                           higher specification should throw the javax.ejb.EJBException 
   *                           instead of this exception.
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
