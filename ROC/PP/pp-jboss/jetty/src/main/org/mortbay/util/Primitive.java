// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: Primitive.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ========================================================================

package org.mortbay.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/* ------------------------------------------------------------ */
/** Utility handling of primitive types 
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class Primitive
{

    /* ------------------------------------------------------------ */
    private static final HashMap name2Class=new HashMap();
    static
    {
        name2Class.put("boolean",java.lang.Boolean.TYPE);
        name2Class.put("byte",java.lang.Byte.TYPE);
        name2Class.put("char",java.lang.Character.TYPE);
        name2Class.put("double",java.lang.Double.TYPE);
        name2Class.put("float",java.lang.Float.TYPE);
        name2Class.put("int",java.lang.Integer.TYPE);
        name2Class.put("long",java.lang.Long.TYPE);
        name2Class.put("short",java.lang.Short.TYPE);
        name2Class.put("void",java.lang.Void.TYPE);
        
        name2Class.put("java.lang.Boolean.TYPE",java.lang.Boolean.TYPE);
        name2Class.put("java.lang.Byte.TYPE",java.lang.Byte.TYPE);
        name2Class.put("java.lang.Character.TYPE",java.lang.Character.TYPE);
        name2Class.put("java.lang.Double.TYPE",java.lang.Double.TYPE);
        name2Class.put("java.lang.Float.TYPE",java.lang.Float.TYPE);
        name2Class.put("java.lang.Integer.TYPE",java.lang.Integer.TYPE);
        name2Class.put("java.lang.Long.TYPE",java.lang.Long.TYPE);
        name2Class.put("java.lang.Short.TYPE",java.lang.Short.TYPE);
        name2Class.put("java.lang.Void.TYPE",java.lang.Void.TYPE);

        name2Class.put("java.lang.Boolean",java.lang.Boolean.class);
        name2Class.put("java.lang.Byte",java.lang.Byte.class);
        name2Class.put("java.lang.Character",java.lang.Character.class);
        name2Class.put("java.lang.Double",java.lang.Double.class);
        name2Class.put("java.lang.Float",java.lang.Float.class);
        name2Class.put("java.lang.Integer",java.lang.Integer.class);
        name2Class.put("java.lang.Long",java.lang.Long.class);
        name2Class.put("java.lang.Short",java.lang.Short.class);

        name2Class.put("Boolean",java.lang.Boolean.class);
        name2Class.put("Byte",java.lang.Byte.class);
        name2Class.put("Character",java.lang.Character.class);
        name2Class.put("Double",java.lang.Double.class);
        name2Class.put("Float",java.lang.Float.class);
        name2Class.put("Integer",java.lang.Integer.class);
        name2Class.put("Long",java.lang.Long.class);
        name2Class.put("Short",java.lang.Short.class);

        name2Class.put(null,java.lang.Void.TYPE);
        name2Class.put("string",java.lang.String.class);
        name2Class.put("String",java.lang.String.class);
        name2Class.put("java.lang.String",java.lang.String.class);
    }
    
    /* ------------------------------------------------------------ */
    private static final HashMap class2Name=new HashMap();
    static
    {
        class2Name.put(java.lang.Boolean.TYPE,"boolean");
        class2Name.put(java.lang.Byte.TYPE,"byte");
        class2Name.put(java.lang.Character.TYPE,"char");
        class2Name.put(java.lang.Double.TYPE,"double");
        class2Name.put(java.lang.Float.TYPE,"float");
        class2Name.put(java.lang.Integer.TYPE,"int");
        class2Name.put(java.lang.Long.TYPE,"long");
        class2Name.put(java.lang.Short.TYPE,"short");
        class2Name.put(java.lang.Void.TYPE,"void");

        class2Name.put(java.lang.Boolean.class,"java.lang.Boolean");
        class2Name.put(java.lang.Byte.class,"java.lang.Byte");
        class2Name.put(java.lang.Character.class,"java.lang.Character");
        class2Name.put(java.lang.Double.class,"java.lang.Double");
        class2Name.put(java.lang.Float.class,"java.lang.Float");
        class2Name.put(java.lang.Integer.class,"java.lang.Integer");
        class2Name.put(java.lang.Long.class,"java.lang.Long");
        class2Name.put(java.lang.Short.class,"java.lang.Short");
        
        class2Name.put(null,"void");
        name2Class.put(java.lang.String.class,"java.lang.String");
    }
    
    /* ------------------------------------------------------------ */
    private static final HashMap class2Value=new HashMap();
    static
    {
        try
        {
            Class[] s ={java.lang.String.class};
            
            class2Value.put(java.lang.Boolean.TYPE,
                           java.lang.Boolean.class.getMethod("valueOf",s));
            class2Value.put(java.lang.Byte.TYPE,
                           java.lang.Byte.class.getMethod("valueOf",s));
            class2Value.put(java.lang.Double.TYPE,
                           java.lang.Double.class.getMethod("valueOf",s));
            class2Value.put(java.lang.Float.TYPE,
                           java.lang.Float.class.getMethod("valueOf",s));
            class2Value.put(java.lang.Integer.TYPE,
                           java.lang.Integer.class.getMethod("valueOf",s));
            class2Value.put(java.lang.Long.TYPE,
                           java.lang.Long.class.getMethod("valueOf",s));
            class2Value.put(java.lang.Short.TYPE,
                           java.lang.Short.class.getMethod("valueOf",s));

            class2Value.put(java.lang.Boolean.class,
                           java.lang.Boolean.class.getMethod("valueOf",s));
            class2Value.put(java.lang.Byte.class,
                           java.lang.Byte.class.getMethod("valueOf",s));
            class2Value.put(java.lang.Double.class,
                           java.lang.Double.class.getMethod("valueOf",s));
            class2Value.put(java.lang.Float.class,
                           java.lang.Float.class.getMethod("valueOf",s));
            class2Value.put(java.lang.Integer.class,
                           java.lang.Integer.class.getMethod("valueOf",s));
            class2Value.put(java.lang.Long.class,
                           java.lang.Long.class.getMethod("valueOf",s));
            class2Value.put(java.lang.Short.class,
                           java.lang.Short.class.getMethod("valueOf",s));
        }
        catch(Exception e)
        {
            Code.warning(e);
        }
    }

    /* ------------------------------------------------------------ */
    public static Class fromName(String name)
    {
        return (Class)name2Class.get(name);
    }
    
    /* ------------------------------------------------------------ */
    public static String toName(Class primitive)
    {
        return (String)class2Name.get(primitive);
    }
    
    /* ------------------------------------------------------------ */
    public static Object valueOf(Class type, String value)
    {
        try
        {
            if (type.equals(java.lang.String.class))
                return value;
            
            Method m = (Method)class2Value.get(type);
            if (m!=null)
                return m.invoke(null,new Object[] {value});

            if (type.equals(java.lang.Character.TYPE) ||
                type.equals(java.lang.Character.class))
                return new Character(value.charAt(0));
        }
        catch(IllegalAccessException e)
        {
            Code.ignore(e);
        }
        catch(InvocationTargetException e)
        {
            if (e.getTargetException() instanceof Error)
                throw (Error)(e.getTargetException());
            Code.ignore(e);
        }
        return null;
    }
    
    /* ------------------------------------------------------------ */
    public static Object valueOf(String type, String value)
    {
        return valueOf(fromName(type),value);
    }
}

        
