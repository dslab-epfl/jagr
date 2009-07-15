package org.jboss.test.cmp2.simple;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Calendar;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import javax.naming.InitialContext;
import junit.framework.Test;
import net.sourceforge.junitejb.EJBTestCase;
import org.jboss.test.JBossTestCase;

public class PageSizeUnitTestCase extends EJBTestCase {
   private static org.apache.log4j.Category log =
       org.apache.log4j.Category.getInstance(PageSizeUnitTestCase.class);

    public static Test suite() throws Exception {
        return JBossTestCase.getDeploySetup(
            PageSizeUnitTestCase.class, "cmp2-simple.jar");
   }

    public PageSizeUnitTestCase(String name) {
        super(name);

      Calendar c = Calendar.getInstance();
      c.clear();    // Must clear time components
      c.set(1981, 4, 5);
      sqlDateValue = new java.sql.Date(c.getTime().getTime());

      c = Calendar.getInstance();
      c.clear();    // Must set date components to epoch
      c.set(Calendar.HOUR_OF_DAY, 22);
      c.set(Calendar.MINUTE, 33);
      c.set(Calendar.SECOND, 44);
      // java.sql.Time does not have a millisecond component
      timeValue = new java.sql.Time(c.getTime().getTime());
      
      objectValue = new HashMap();
      ((HashMap)objectValue).put("boolean", booleanObject);
      ((HashMap)objectValue).put("byte", byteObject);
      ((HashMap)objectValue).put("short", shortObject);
      ((HashMap)objectValue).put("int", integerObject);
      ((HashMap)objectValue).put("long", longObject);
      ((HashMap)objectValue).put("float", floatObject);
      ((HashMap)objectValue).put("double", doubleObject);
      ((HashMap)objectValue).put("string", stringValue);
      ((HashMap)objectValue).put("utilDate", utilDateValue);
      ((HashMap)objectValue).put("sqlDate", sqlDateValue);
      ((HashMap)objectValue).put("time", timeValue);
      ((HashMap)objectValue).put("timestamp", timestampValue);
      ((HashMap)objectValue).put("bigDecimal", bigDecimalValue);
    }

    private SimpleHome getSimpleHome() {
        try {
            InitialContext jndiContext = new InitialContext();
            
            return (SimpleHome) jndiContext.lookup("cmp2/simple/Simple");
        } catch(Exception e) {
            log.debug("failed", e);
            fail("Exception in getSimpleHome: " + e.getMessage());
        }
        return null;
    }
   
   private final boolean booleanPrimitive = true;
   private final Boolean booleanObject = Boolean.FALSE;
   private final byte bytePrimitive = (byte)11;
   private final Byte byteObject = new Byte((byte)22);
   private final short shortPrimitive = (short)33;
   private final Short shortObject = new Short((short)44);
   private final int integerPrimitive = 55;
   private final Integer integerObject = new Integer(66);
   private final long longPrimitive = 77;
   private final Long longObject = new Long(88);
   private final float floatPrimitive = 11.11f;
   private final Float floatObject = new Float(22.22f);
   private final double doublePrimitive = 33.33;
   private final Double doubleObject = new Double(44.44);
   private final String stringValue = "test string value";
   private final java.util.Date utilDateValue = new java.util.Date(1111);
   private final java.sql.Date sqlDateValue;
   private final Time timeValue;
   private final Timestamp timestampValue = new Timestamp(4444);
   private final BigDecimal bigDecimalValue = new BigDecimal("12345678");
   private final byte[] byteArrayValue = "byte array test".getBytes();
   private final Object objectValue;
   private final ValueClass valueClass = new ValueClass(111, 999);

   public void testFindAll() throws Exception {
      SimpleHome simpleHome = getSimpleHome();
      Collection c = simpleHome.findAll();
      assertEquals(50, c.size());

      for(Iterator iterator = c.iterator(); iterator.hasNext(); ) {
         Simple s = (Simple)iterator.next();

         assertEquals(booleanPrimitive, s.getBooleanPrimitive());
         assertEquals(booleanObject, s.getBooleanObject());
         assertEquals(bytePrimitive, s.getBytePrimitive());
         assertEquals(byteObject, s.getByteObject());
         assertEquals(shortPrimitive, s.getShortPrimitive());
         assertEquals(shortObject, s.getShortObject());
         assertEquals(integerPrimitive, s.getIntegerPrimitive());
         assertEquals(integerObject, s.getIntegerObject());
         assertEquals(longPrimitive, s.getLongPrimitive());
         assertEquals(longObject, s.getLongObject());
         assertEquals(floatPrimitive, s.getFloatPrimitive(), 0);
         assertEquals(floatObject, s.getFloatObject());
         assertEquals(doublePrimitive, s.getDoublePrimitive(), 0);
         assertEquals(doubleObject, s.getDoubleObject());
         assertEquals(stringValue, s.getStringValue());
         assertTrue(
               "expected :<" + s.getUtilDateValue() + "> but was <" +
               utilDateValue + ">",
               utilDateValue.compareTo(s.getUtilDateValue()) == 0);
         assertTrue(
               "expected :<" + s.getSqlDateValue() + "> but was <" +
               sqlDateValue + ">",
               sqlDateValue.compareTo(s.getSqlDateValue()) == 0);
         assertTrue(
               "expected :<" + s.getTimeValue() + "> but was <" +
               timeValue + ">",
               timeValue.compareTo(s.getTimeValue()) == 0);
         assertTrue(
               "expected :<" + s.getTimestampValue() + "> but was <" +
               timestampValue + ">",
               timestampValue.compareTo(s.getTimestampValue()) == 0);
         assertTrue(
               "expected :<" + s.getBigDecimalValue() + "> but was <" +
               bigDecimalValue + ">",
               bigDecimalValue.compareTo(s.getBigDecimalValue()) == 0);

         byte[] array = s.getByteArrayValue();
         assertEquals(byteArrayValue.length, array.length);
         for(int i=0; i<array.length; i++) {
            assertEquals(byteArrayValue[i], array[i]);
         }

         assertEquals(valueClass, s.getValueClass());
         assertEquals(objectValue, s.getObjectValue());
      }
   }

   public void setUpEJB() throws Exception {
      SimpleHome simpleHome = getSimpleHome();

      for(int i=0; i<50; i++) {
         Simple simple = simpleHome.create("test"+i);

         simple.setBooleanPrimitive(booleanPrimitive);
         simple.setBooleanObject(booleanObject);
         simple.setBytePrimitive(bytePrimitive);
         simple.setByteObject(byteObject);
         simple.setShortPrimitive(shortPrimitive);
         simple.setShortObject(shortObject);
         simple.setIntegerPrimitive(integerPrimitive);
         simple.setIntegerObject(integerObject);
         simple.setLongPrimitive(longPrimitive);
         simple.setLongObject(longObject);
         simple.setFloatPrimitive(floatPrimitive);
         simple.setFloatObject(floatObject);
         simple.setDoublePrimitive(doublePrimitive);
         simple.setDoubleObject(doubleObject);
         simple.setStringValue(stringValue);
         simple.setUtilDateValue(utilDateValue);
         simple.setSqlDateValue(sqlDateValue);
         simple.setTimeValue(timeValue);
         simple.setTimestampValue(timestampValue);
         simple.setBigDecimalValue(bigDecimalValue);
         simple.setByteArrayValue(byteArrayValue);
         simple.setObjectValue(objectValue);
         simple.setValueClass(valueClass);
      }
   }

   public void tearDownEJB() throws Exception {
      SimpleHome simpleHome = getSimpleHome();
      Collection c = simpleHome.findAll();
      for(Iterator iterator = c.iterator(); iterator.hasNext(); ) {
         Simple simple = (Simple)iterator.next();
         simple.remove();
      }
   }
}

