/*
 * LGPL
 */
package javax.management;

public class ObjectInstance extends Object implements java.io.Serializable {

   private ObjectName objectName  = null;
   private String className = null;
   
   public ObjectInstance(java.lang.String objectName,
                         java.lang.String className)
   throws MalformedObjectNameException {
      this.objectName = new ObjectName(objectName);
      this.className = className;
   
   }

   public ObjectInstance(ObjectName objectName,
                         java.lang.String className) {
       this.objectName = objectName;
       this.className  = className;                  
   }

   public boolean equals(java.lang.Object object) {
      if (!(object instanceof ObjectInstance)) return false;
      
      ObjectInstance oi = (ObjectInstance)object;
      return ( (objectName.equals(oi.getObjectName())) &&
               (className.equals(oi.getClassName())) );
   }

   public ObjectName getObjectName() {
      return objectName;
   }

   public java.lang.String getClassName() {
      return className;
   }


}

