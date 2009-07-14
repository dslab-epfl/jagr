
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: Result.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.ruleengine;


class Result implements Cloneable {

    static final char[] typeMap   = {
        'Z', 'B', 'C', 'S', 'I', 'J', 'F', 'D'
    };
    static final byte   BOOLEAN   = 0;
    static final byte   BYTE      = 1;
    static final byte   CHAR      = 2;
    static final byte   SHORT     = 3;
    static final byte   INT       = 4;
    static final byte   LONG      = 5;
    static final byte   FLOAT     = 6;
    static final byte   DOUBLE    = 7;
    static final byte   OBJECT    = 8;
    static final byte   VOID      = 9;     // results of void methods
    static final byte   EXCEPTION = 10;    // results of methods throwing exceptions
    static final byte   CLASS     = 11;    // used for identifier of

    // static methods and fields.
    static final byte MEMBER = 12;    // unresolved member, like object & class

    // name held in assignables field
    static final byte UNRESOLVED = 13;    // used for yet incomplete identifiers.
    byte              type;
    String            refType;
    Object            ref;

    /*
     * this field is set for results that represent an
     * assignable thing. For field references, it will contain
     * an object array with the first entry referencing the
     * object and the second entry referencing the field
     * or having an instance of java.lang.Integer representing
     * array indexes of the array.
     * For local variables, it holds a reference to
     * java.lang.Boolean.TRUE. If it is not assignable, this
     * field should be null.
     * Note that this field never gets cloned.
     */
    Object  assignables;
    boolean booleanValue;
    byte    byteValue;
    char    charValue;
    short   shortValue;
    int     intValue;
    long    longValue;
    float   floatValue;
    double  doubleValue;

    /**
     * Method toString
     *
     *
     * @return
     *
     */
    public String toString() {

        StringBuffer res = new StringBuffer(50);
        StringBuffer idx = new StringBuffer();

        if (assignables != null) {
            if (Boolean.TRUE.equals(assignables)) {
                res.append("Local variable");
            } else if (assignables instanceof Object[]) {
                Object[] oa = (Object[]) assignables;

                if (oa[1] instanceof java.lang.reflect.Field) {
                    java.lang.reflect.Field f =
                        (java.lang.reflect.Field) oa[1];

                    res.append(f.getDeclaringClass().getName());
                    res.append('.');
                    res.append(f.getName());
                } else {
                    idx.append('[');
                    idx.append(((Integer) oa[1]).intValue());
                    idx.append(']');
                }
            }
        }

        if (res.length() > 0) {
            res.append(' ');
        }

        switch (type) {

        case DOUBLE :
            res.append("(double");
            res.append(idx.toString());
            res.append(") ");
            res.append(doubleValue);
            break;

        case FLOAT :
            res.append("(float");
            res.append(idx.toString());
            res.append(") ");
            res.append(floatValue);
            break;

        case LONG :
            res.append("(long");
            res.append(idx.toString());
            res.append(") ");
            res.append(longValue);
            break;

        case INT :
            res.append("(int");
            res.append(idx.toString());
            res.append(") ");
            res.append(intValue);
            break;

        case SHORT :
            res.append("(short");
            res.append(idx.toString());
            res.append(") ");
            res.append(shortValue);
            break;

        case CHAR :
            res.append("(char");
            res.append(idx.toString());
            res.append(") ");
            res.append(charValue);
            break;

        case BYTE :
            res.append("(byte");
            res.append(idx.toString());
            res.append(") ");
            res.append(byteValue);
            break;

        case BOOLEAN :
            res.append("(boolean");
            res.append(idx.toString());
            res.append(") ");
            res.append(booleanValue);
            break;

        case OBJECT :
            res.append('(');
            res.append(refType);
            res.append(idx.toString());
            res.append(") ");
            res.append(ref);
            break;

        case VOID :
            res.append("(void)");
            break;

        case EXCEPTION :
            res.append('(');
            res.append(refType);
            res.append(") ");
            res.append(((Exception) ref).getMessage());
            break;

        case CLASS :
            res.append("(class) ");
            res.append(refType);
            break;

        case MEMBER :
            res.append("(member) ");
            res.append(refType);
            res.append('.');
            res.append(assignables);
            break;

        case UNRESOLVED :
            res.append("(unresolved) ");
            res.append(refType);
            break;
        }

        return res.toString();
    }

    /**
     * Method clone
     *
     *
     * @return
     *
     */
    public Object clone() {

        Result c = new Result();

        c.type = type;

        switch (type) {

        case DOUBLE :
            c.doubleValue = doubleValue;
            break;

        case FLOAT :
            c.floatValue = floatValue;
            break;

        case LONG :
            c.longValue = longValue;
            break;

        case INT :
            c.intValue = intValue;
            break;

        case SHORT :
            c.shortValue = shortValue;
            break;

        case CHAR :
            c.charValue = charValue;
            break;

        case BYTE :
            c.byteValue = byteValue;
            break;

        case BOOLEAN :
            c.booleanValue = booleanValue;
            break;

        case MEMBER :
            c.assignables = assignables;
        case OBJECT :
        case CLASS :
            c.ref     = ref;
            c.refType = refType;
        }

        return c;
    }

    /**
     * Method equals
     *
     *
     * @param o
     *
     * @return
     *
     */
    public boolean equals(Object o) {

        boolean eq = false;
        Result  c  = null;

        if ((o instanceof Result) && (type == ((Result) o).type)) {
            c = (Result) o;

            switch (type) {

            case DOUBLE :
                eq = c.doubleValue == doubleValue;
                break;

            case FLOAT :
                eq = c.floatValue == floatValue;
                break;

            case LONG :
                eq = c.longValue == longValue;
                break;

            case INT :
                eq = c.intValue == intValue;
                break;

            case SHORT :
                eq = c.shortValue == shortValue;
                break;

            case CHAR :
                eq = c.charValue == charValue;
                break;

            case BYTE :
                eq = c.byteValue == byteValue;
                break;

            case BOOLEAN :
                eq = c.booleanValue == booleanValue;
                break;

            case OBJECT :
                eq = c.ref.equals(ref) && c.refType.equals(refType);
            case MEMBER :
                eq = c.refType.equals(refType) && c.ref.equals(ref)
                     && c.assignables.equals(assignables);
            case UNRESOLVED :
                eq = c.refType.equals(refType);
            }
        }

        return eq;
    }
}

