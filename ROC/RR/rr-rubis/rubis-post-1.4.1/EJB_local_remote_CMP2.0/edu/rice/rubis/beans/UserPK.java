package edu.rice.rubis.beans;

/**
 * User Primary Key class
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */
public class UserPK implements java.io.Serializable {

  public Integer id;

  /**
   * Creates a new <code>UserPK</code> instance.
   *
   */
  public UserPK() {}
    
  /**
   * Creates a new <code>UserPK</code> instance.
   *
   * @param uniqueId an <code>Integer</code> value
   */
  public UserPK(Integer uniqueId)
  {
      //id = uniqueId;
      id = PKFaultInjector.getId("UserPK",uniqueId);
  }

  /**
   * Specific <code>hashCode</code> just returning the id.
   *
   * @return the hash code
   */
  public int hashCode() 
  {
    if (id == null)
      return 0;
    else
      return id.intValue();
  }

  /**
   * Specific <code>equals</code> method.
   *
   * @param other the <code>Object</code> to compare with
   * @return true if both objects have the same primary key
   */
  public boolean equals(Object other) 
  {
    boolean isEqual = false;
    if (other instanceof UserPK)
    {
      if (id == null)
        isEqual = (id == ((UserPK)other).id);
      else
        isEqual = (id.intValue() == ((UserPK)other).id.intValue());
    }
    return isEqual;
  }
  

}
