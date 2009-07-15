
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: CachedClass.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.ruleengine;


import java.util.*;

import java.lang.ref.*;
import java.lang.reflect.*;


/**
 * Class CachedClass
 *
 *
 * @author
 * @version %I%, %G%
 */
public class CachedClass {

    String     name;
    HashMap    methodCache;
    HashMap    fieldCache;
    HashMap    constructorCache;
    TimeoutSet blackListMethods;
    TimeoutSet blackListFields;
    TimeoutSet blackListConstructors;
    TimeoutSet instanceTypes;
    Reference  classRef;
    TimeoutRef cacheRef;

    CachedClass(String name, TimeoutRef cacheRef)
            throws ClassNotFoundException {

        this.name = name;

        Class classToCache = Class.forName(name);

        methodCache           = new HashMap();
        fieldCache            = new HashMap();
        constructorCache      = new HashMap();
        blackListMethods      = new TimeoutSet(300000);    //5 minute timeout.
        blackListFields       = new TimeoutSet(300000);
        blackListConstructors = new TimeoutSet(300000);
        instanceTypes         = new TimeoutSet(1200000);    //20 minute timeout.
        classRef              = new SoftReference(classToCache);
        this.cacheRef         = cacheRef;
    }

    CachedClass(TimeoutRef cacheRef, Class clazz) {

        this.name = clazz.getName();

        Class classToCache = clazz;

        methodCache           = new HashMap();
        fieldCache            = new HashMap();
        constructorCache      = new HashMap();
        blackListMethods      = new TimeoutSet(300000);    //5 minute timeout.
        blackListFields       = new TimeoutSet(300000);
        blackListConstructors = new TimeoutSet(300000);
        instanceTypes         = new TimeoutSet(1200000);    //20 minute timeout.
        classRef              = new SoftReference(clazz);
        this.cacheRef         = cacheRef;
    }

    /**
     * Method getName
     *
     *
     * @return
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Method getClassObject
     *
     *
     * @return
     *
     */
    public Class getClassObject() {

        Class retClass = (Class) classRef.get();

        if (retClass == null) {
            try {
                retClass = Class.forName(name);
                classRef = new SoftReference(retClass);
            } catch (ClassNotFoundException e) {
                throw new ClassLostException(name);
            }
        }

        cacheRef.add(retClass);

        return retClass;
    }

    /**
     * Method getConstructor
     *
     *
     * @param parameterTypes
     *
     * @return
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     *
     */
    public Constructor getConstructor(Class[] parameterTypes)
            throws NoSuchMethodException, SecurityException {

        Constructor c         = null;
        String      signature = buildSignature("", parameterTypes);
        Reference   ref       = (Reference) methodCache.get(signature);

        if (ref != null) {
            c = (Constructor) ref.get();
        } else if (blackListConstructors.contains(signature)) {
            throw new NoSuchMethodException(signature
                                            + "is in blackList list");
        }

        if (c == null) {
            try {
                c   = getClassObject().getConstructor(parameterTypes);
                ref = new SoftReference(c);

                constructorCache.put(signature, ref);
            } catch (NoSuchMethodException e) {
                blackListConstructors.add(signature);

                throw e;
            }
        }

        cacheRef.add(c);

        return c;
    }

    /**
     * Method getMethod
     *
     *
     * @param name
     * @param parameterTypes
     *
     * @return
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     *
     */
    public Method getMethod(String name, Class[] parameterTypes)
            throws NoSuchMethodException, SecurityException {

        Method    m         = null;
        String    signature = buildSignature(name, parameterTypes);
        Reference ref       = (Reference) methodCache.get(signature);

        if (ref != null) {
            m = (Method) ref.get();
        } else if (blackListMethods.contains(signature)) {
            throw new NoSuchMethodException(signature
                                            + "is in blackList list");
        }

        if (m == null) {
            try {
                m   = getClassObject().getMethod(name, parameterTypes);
                ref = new SoftReference(m);

                methodCache.put(signature, ref);
            } catch (NoSuchMethodException e) {
                blackListMethods.add(signature);

                throw e;
            }
        }

        cacheRef.add(m);

        return m;
    }

    /**
     * Method getField
     *
     *
     * @param name
     *
     * @return
     *
     * @throws NoSuchFieldException
     * @throws SecurityException
     *
     */
    public Field getField(String name)
            throws NoSuchFieldException, SecurityException {

        Field     f   = null;
        Reference ref = (Reference) fieldCache.get(name);

        if (ref != null) {
            f = (Field) ref.get();
        } else if (blackListFields.contains(name)) {
            throw new NoSuchFieldException(name + "is in blackList list");
        }

        if (f == null) {
            try {
                f   = getClassObject().getField(name);
                ref = new SoftReference(f);

                fieldCache.put(name, ref);
            } catch (NoSuchFieldException e) {
                blackListFields.add(name);

                throw e;
            }
        }

        cacheRef.add(f);

        return f;
    }

    /**
     * Method isInstance
     *
     *
     * @param r
     *
     * @return
     *
     */
    public boolean isInstance(Result r) {

        boolean instance = instanceTypes.contains(r.refType);

        if (!instance) {
            instance = getClassObject().isInstance(r.ref);

            if (instance) {
                instanceTypes.add(r.refType);
            }
        }

        return instance;
    }

    String buildSignature(String name, Class[] types) {

        StringBuffer signature = new StringBuffer(name);

        signature.append('(');

        if (types != null) {
            for (int i = 0; i < types.length; i++) {
                signature.append(types[i].getName());
                signature.append(',');
            }
        }

        signature.append(')');

        return signature.toString();
    }
}

