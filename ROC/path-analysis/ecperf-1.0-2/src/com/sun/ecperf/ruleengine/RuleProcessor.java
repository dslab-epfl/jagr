/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: RuleProcessor.java,v 1.1.1.1 2002/11/16 05:35:30 emrek Exp $
 *
 */

/**
 * Rule Processor Backend
 * Author: Akara Sucharitakul
 * Warning: The backend is only tested with the rules provided by the
 * ECperf(TM) benchmark. More testing/debugging is required for use
 * in other applications.
 */
package com.sun.ecperf.ruleengine;

import java.util.*;
import java.lang.reflect.*;
import com.sun.ecperf.common.*;

public class RuleProcessor {

  private ClassCache classCache;
  private HashMap symbolMap;
  private HashMap pkgMap;
  private HashSet pkgSet;
  private Stack opsStack;
  private Result bean;
  private Debug debug;
  private boolean debugging;

  public RuleProcessor(Object bean, int debugLevel) {
    classCache = new ClassCache();
    symbolMap = new HashMap();
    opsStack = new Stack();
    pkgMap = new HashMap();
    pkgSet = new HashSet();
    pkgSet.add("java.lang"); // java.lang is always available.
    this.bean = new Result();
    this.bean.ref = bean;
    this.bean.refType = bean.getClass().getName();
    this.bean.type = Result.OBJECT;
    if (debugLevel > 0) {
      debug = new DebugPrint(debugLevel, this);
      debugging = true;
    } else {
      debug = new Debug();
      debugging = false;
    }
  }

  public void clearSymbols() {
    symbolMap.clear();
    opsStack.clear();
  }

  public void clearStack() {
    opsStack.clear();
  }

  String printStack() {
    StringBuffer opsString = new StringBuffer(50);
    for (int i = 0; i < opsStack.size(); i++) {
	opsString.append(opsStack.elementAt(i).toString());
	opsString.append('\n');
    }
    return opsString.toString();
  }

  public void conditionalOr() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
	throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
	throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
	// we won't touch variables in this method,
	// so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
	opsStack.pop();
	opsStack.push(ex1);
    }
    ex1.assignables = null;
    if (ex1.type != Result.BOOLEAN)
	throw new ParseException("Expression left of '||' must be boolean!");
    if (ex2.type != Result.BOOLEAN)
	throw new ParseException("Expression right of '||' must be boolean!");
    ex1.booleanValue = ex1.booleanValue || ex2.booleanValue;
  }

  public void conditionalAnd() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
	throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
	throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    if (ex2 == null)
	return;
    if (ex1.type != Result.BOOLEAN)
	throw new ParseException("Expression left of '&&' must be boolean!");
    if (ex2.type != Result.BOOLEAN)
	throw new ParseException("Expression right of '&&' must be boolean!");
    ex1.booleanValue = ex1.booleanValue && ex2.booleanValue;
  }

  public void inclusiveOr() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.LONG    :
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.longValue | ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.longValue = ex1.longValue | ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.longValue = ex1.longValue | ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.longValue = ex1.longValue | ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.longValue = ex1.longValue | ex2.byteValue;
		    break;
		case Result.BOOLEAN :
		default             :
		    throw new ParseException(
			"Expression right of '|' must be integral!");
	    }
	    break;
	case Result.INT     :
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.intValue | ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.intValue | ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.intValue | ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.intValue | ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.intValue | ex2.byteValue;
		    break;
		case Result.BOOLEAN :
		default             :
		    throw new ParseException(
			"Expression right of '|' must be integral!");
	    }
	    break;
	case Result.SHORT   :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.shortValue | ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.shortValue | ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.shortValue | ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.shortValue | ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.shortValue | ex2.byteValue;
		    break;
		case Result.BOOLEAN :
		default             :
		    throw new ParseException(
			"Expression right of '|' must be integral!");
	    }
	    break;
	case Result.CHAR    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.charValue | ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.charValue | ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.charValue | ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.charValue | ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.charValue | ex2.byteValue;
		    break;
		case Result.BOOLEAN :
		default             :
		    throw new ParseException(
			"Expression right of '|' must be integral!");
	    }
	    break;
	case Result.BYTE    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.byteValue | ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.byteValue | ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.byteValue | ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.byteValue | ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.byteValue | ex2.byteValue;
		    break;
		case Result.BOOLEAN :
		default             :
		    throw new ParseException(
			"Expression right of '|' must be integral!");
	    }
	    break;
	case Result.BOOLEAN :
	    if (ex2.type == Result.BOOLEAN)
		ex1.booleanValue = ex1.booleanValue | ex2.booleanValue;
	    else
		throw new ParseException(
		    "Expression right of '|' must be boolean!");
	    break;
	default		    :
	    throw new ParseException(
		"Expression left of '|' must be boolean or integral!");
    }
  }

  public void exclusiveOr() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.LONG    :
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.longValue ^ ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.longValue = ex1.longValue ^ ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.longValue = ex1.longValue ^ ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.longValue = ex1.longValue ^ ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.longValue = ex1.longValue ^ ex2.byteValue;
		    break;
		case Result.BOOLEAN :
		default             :
		    throw new ParseException(
			"Expression right of '^' must be integral!");
	    }
	    break;
	case Result.INT     :
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.intValue ^ ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.intValue ^ ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.intValue ^ ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.intValue ^ ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.intValue ^ ex2.byteValue;
		    break;
		case Result.BOOLEAN :
		default             :
		    throw new ParseException(
			"Expression right of '^' must be integral!");
	    }
	    break;
	case Result.SHORT   :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.shortValue ^ ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.shortValue ^ ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.shortValue ^ ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.shortValue ^ ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.shortValue ^ ex2.byteValue;
		    break;
		case Result.BOOLEAN :
		default             :
		    throw new ParseException(
			"Expression right of '^' must be integral!");
	    }
	    break;
	case Result.CHAR    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.charValue ^ ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.charValue ^ ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.charValue ^ ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.charValue ^ ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.charValue ^ ex2.byteValue;
		    break;
		case Result.BOOLEAN :
		default             :
		    throw new ParseException(
			"Expression right of '^' must be integral!");
	    }
	    break;
	case Result.BYTE    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.byteValue ^ ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.byteValue ^ ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.byteValue ^ ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.byteValue ^ ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.byteValue ^ ex2.byteValue;
		    break;
		case Result.BOOLEAN :
		default             :
		    throw new ParseException(
			"Expression right of '^' must be integral!");
	    }
	    break;
	case Result.BOOLEAN :
	    if (ex2.type == Result.BOOLEAN)
		ex1.booleanValue = ex1.booleanValue ^ ex2.booleanValue;
	    else
		throw new ParseException(
		    "Expression right of '^' must be boolean!");
	    break;
	default		    :
	    throw new ParseException(
		"Expression left of '^' must be boolean or integral!");
    }
  }

  public void and() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.LONG    :
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.longValue & ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.longValue = ex1.longValue & ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.longValue = ex1.longValue & ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.longValue = ex1.longValue & ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.longValue = ex1.longValue & ex2.byteValue;
		    break;
		case Result.BOOLEAN :
		default             :
		    throw new ParseException(
			"Expression right of '&' must be integral!");
	    }
	    break;
	case Result.INT     :
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.intValue & ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.intValue & ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.intValue & ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.intValue & ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.intValue & ex2.byteValue;
		    break;
		case Result.BOOLEAN :
		default             :
		    throw new ParseException(
			"Expression right of '&' must be integral!");
	    }
	    break;
	case Result.SHORT   :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.shortValue & ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.shortValue & ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.shortValue & ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.shortValue & ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.shortValue & ex2.byteValue;
		    break;
		case Result.BOOLEAN :
		default             :
		    throw new ParseException(
			"Expression right of '&' must be integral!");
	    }
	    break;
	case Result.CHAR    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.charValue & ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.charValue & ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.charValue & ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.charValue & ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.charValue & ex2.byteValue;
		    break;
		case Result.BOOLEAN :
		default             :
		    throw new ParseException(
			"Expression right of '&' must be integral!");
	    }
	    break;
	case Result.BYTE    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.byteValue & ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.byteValue & ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.byteValue & ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.byteValue & ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.byteValue & ex2.byteValue;
		    break;
		case Result.BOOLEAN :
		default             :
		    throw new ParseException(
			"Expression right of '&' must be integral!");
	    }
	    break;
	case Result.BOOLEAN :
	    if (ex2.type == Result.BOOLEAN)
		ex1.booleanValue = ex1.booleanValue & ex2.booleanValue;
	    else
		throw new ParseException(
		    "Expression right of '&' must be boolean!");
	    break;
	default		    :
	    throw new ParseException(
		"Expression left of '&' must be boolean or integral!");
    }
  }

  public void equals() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.DOUBLE  :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.doubleValue == ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.doubleValue == ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.doubleValue == ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.doubleValue == ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.doubleValue == ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.doubleValue == ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.doubleValue == ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '==' must be numeric!");
	    }
	    break;
	case Result.FLOAT   :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.floatValue == ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.floatValue == ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.floatValue == ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.floatValue == ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.floatValue == ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.floatValue == ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.floatValue == ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '==' must be numeric!");
	    }
	    break;
	case Result.LONG    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.longValue == ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.longValue == ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.longValue == ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.longValue == ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.longValue == ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.longValue == ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.longValue == ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '==' must be numeric!");
	    }
	    break;
	case Result.INT     :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.intValue == ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.intValue == ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.intValue == ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.intValue == ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.intValue == ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.intValue == ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.intValue == ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '==' must be numeric!");
	    }
	    break;
	case Result.SHORT   :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.shortValue == ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.shortValue == ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.shortValue == ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.shortValue == ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.shortValue == ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.shortValue == ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.shortValue == ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '==' must be numeric!");
	    }
	    break;
	case Result.CHAR    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.charValue == ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.charValue == ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.charValue == ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.charValue == ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.charValue == ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.charValue == ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.charValue == ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '==' must be numeric!");
	    }
	    break;
	case Result.BYTE    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.byteValue == ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.byteValue == ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.byteValue == ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.byteValue == ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.byteValue == ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.byteValue == ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.byteValue == ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '==' must be numeric!");
	    }
	    break;
	case Result.BOOLEAN :
	    if (ex2.type == Result.BOOLEAN)
		ex1.booleanValue = ex1.booleanValue == ex2.booleanValue;
	    else
		throw new ParseException(
			"Expression right of '==' must be boolean!");
	    break;
	case Result.OBJECT  :
	    if (ex2.type == Result.OBJECT)
		ex1.booleanValue = ex1.ref == ex2.ref;
	    else
		throw new ParseException(
			"Expression right of '==' must be an object reference!");
	    break;
	default		    :
	    throw new ParseException(
		    "Unknown data type left of '=='! Likely a bug!");
    }
    ex1.type = Result.BOOLEAN;
  }

  public void notEquals() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.DOUBLE  :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.doubleValue != ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.doubleValue != ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.doubleValue != ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.doubleValue != ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.doubleValue != ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.doubleValue != ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.doubleValue != ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '!=' must be numeric!");
	    }
	    break;
	case Result.FLOAT   :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.floatValue != ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.floatValue != ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.floatValue != ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.floatValue != ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.floatValue != ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.floatValue != ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.floatValue != ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '!=' must be numeric!");
	    }
	    break;
	case Result.LONG    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.longValue != ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.longValue != ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.longValue != ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.longValue != ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.longValue != ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.longValue != ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.longValue != ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '!=' must be numeric!");
	    }
	    break;
	case Result.INT     :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.intValue != ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.intValue != ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.intValue != ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.intValue != ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.intValue != ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.intValue != ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.intValue != ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '!=' must be numeric!");
	    }
	    break;
	case Result.SHORT   :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.shortValue != ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.shortValue != ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.shortValue != ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.shortValue != ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.shortValue != ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.shortValue != ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.shortValue != ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '!=' must be numeric!");
	    }
	    break;
	case Result.CHAR    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.charValue != ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.charValue != ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.charValue != ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.charValue != ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.charValue != ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.charValue != ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.charValue != ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '!=' must be numeric!");
	    }
	    break;
	case Result.BYTE    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.byteValue != ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.byteValue != ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.byteValue != ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.byteValue != ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.byteValue != ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.byteValue != ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.byteValue != ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '!=' must be numeric!");
	    }
	    break;
	case Result.BOOLEAN :
	    if (ex2.type != Result.BOOLEAN)
		ex1.booleanValue = ex1.booleanValue != ex2.booleanValue;
	    else
		throw new ParseException(
			"Expression right of '!=' must be boolean!");
	    break;
	case Result.OBJECT  :
	    if (ex2.type != Result.OBJECT)
		ex1.booleanValue = ex1.ref != ex2.ref;
	    else
		throw new ParseException(
			"Expression right of '!=' must be an object reference!");
	    break;
	default		    :
	    throw new ParseException(
		    "Unknown data type left of '!='! Likely a bug!");
    }
    ex1.type = Result.BOOLEAN;
  }

  public void lessThan() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.DOUBLE  :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.doubleValue < ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.doubleValue < ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.doubleValue < ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.doubleValue < ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.doubleValue < ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.doubleValue < ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.doubleValue < ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<' must be numeric!");
	    }
	    break;
	case Result.FLOAT   :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.floatValue < ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.floatValue < ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.floatValue < ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.floatValue < ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.floatValue < ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.floatValue < ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.floatValue < ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<' must be numeric!");
	    }
	    break;
	case Result.LONG    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.longValue < ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.longValue < ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.longValue < ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.longValue < ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.longValue < ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.longValue < ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.longValue < ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<' must be numeric!");
	    }
	    break;
	case Result.INT     :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.intValue < ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.intValue < ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.intValue < ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.intValue < ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.intValue < ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.intValue < ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.intValue < ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<' must be numeric!");
	    }
	    break;
	case Result.SHORT   :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.shortValue < ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.shortValue < ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.shortValue < ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.shortValue < ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.shortValue < ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.shortValue < ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.shortValue < ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<' must be numeric!");
	    }
	    break;
	case Result.CHAR    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.charValue < ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.charValue < ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.charValue < ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.charValue < ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.charValue < ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.charValue < ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.charValue < ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<' must be numeric!");
	    }
	    break;
	case Result.BYTE    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.byteValue < ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.byteValue < ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.byteValue < ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.byteValue < ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.byteValue < ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.byteValue < ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.byteValue < ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<' must be numeric!");
	    }
	    break;
	default		    :
	    throw new ParseException(
		    "Expression left of '<' must be numeric!");
    }
    ex1.type = Result.BOOLEAN;
  }

  public void greaterThan() throws ParseException {
    if (debugging)
      debug.println(8, "Entering greaterThan(), stack:\n" + printStack());
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    if (Boolean.TRUE.equals(ex1.assignables))
        ex1 = (Result) ex1.clone();
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.DOUBLE  :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.doubleValue > ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.doubleValue > ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.doubleValue > ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.doubleValue > ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.doubleValue > ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.doubleValue > ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.doubleValue > ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>' must be numeric!");
	    }
	    break;
	case Result.FLOAT   :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.floatValue > ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.floatValue > ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.floatValue > ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.floatValue > ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.floatValue > ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.floatValue > ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.floatValue > ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>' must be numeric!");
	    }
	    break;
	case Result.LONG    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.longValue > ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.longValue > ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.longValue > ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.longValue > ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.longValue > ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.longValue > ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.longValue > ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>' must be numeric!");
	    }
	    break;
	case Result.INT     :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.intValue > ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.intValue > ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.intValue > ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.intValue > ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.intValue > ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.intValue > ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.intValue > ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>' must be numeric!");
	    }
	    break;
	case Result.SHORT   :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.shortValue > ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.shortValue > ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.shortValue > ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.shortValue > ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.shortValue > ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.shortValue > ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.shortValue > ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>' must be numeric!");
	    }
	    break;
	case Result.CHAR    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.charValue > ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.charValue > ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.charValue > ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.charValue > ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.charValue > ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.charValue > ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.charValue > ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>' must be numeric!");
	    }
	    break;
	case Result.BYTE    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.byteValue > ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.byteValue > ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.byteValue > ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.byteValue > ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.byteValue > ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.byteValue > ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.byteValue > ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>' must be numeric!");
	    }
	    break;
	default		    :
	    throw new ParseException(
		    "Expression left of '>' must be numeric!");
    }
    ex1.type = Result.BOOLEAN;
  }

  public void lessOrEqual() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.DOUBLE  :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.doubleValue <= ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.doubleValue <= ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.doubleValue <= ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.doubleValue <= ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.doubleValue <= ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.doubleValue <= ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.doubleValue <= ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<=' must be numeric!");
	    }
	    break;
	case Result.FLOAT   :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.floatValue <= ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.floatValue <= ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.floatValue <= ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.floatValue <= ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.floatValue <= ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.floatValue <= ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.floatValue <= ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<=' must be numeric!");
	    }
	    break;
	case Result.LONG    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.longValue <= ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.longValue <= ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.longValue <= ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.longValue <= ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.longValue <= ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.longValue <= ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.longValue <= ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<=' must be numeric!");
	    }
	    break;
	case Result.INT     :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.intValue <= ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.intValue <= ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.intValue <= ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.intValue <= ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.intValue <= ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.intValue <= ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.intValue <= ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<=' must be numeric!");
	    }
	    break;
	case Result.SHORT   :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.shortValue <= ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.shortValue <= ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.shortValue <= ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.shortValue <= ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.shortValue <= ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.shortValue <= ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.shortValue <= ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<=' must be numeric!");
	    }
	    break;
	case Result.CHAR    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.charValue <= ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.charValue <= ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.charValue <= ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.charValue <= ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.charValue <= ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.charValue <= ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.charValue <= ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<=' must be numeric!");
	    }
	    break;
	case Result.BYTE    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.byteValue <= ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.byteValue <= ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.byteValue <= ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.byteValue <= ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.byteValue <= ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.byteValue <= ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.byteValue <= ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<=' must be numeric!");
	    }
	    break;
	default		    :
	    throw new ParseException(
		    "Expression left of '<=' must be numeric!");
    }
    ex1.type = Result.BOOLEAN;
  }

  public void greaterOrEqual() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.DOUBLE  :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.doubleValue >= ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.doubleValue >= ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.doubleValue >= ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.doubleValue >= ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.doubleValue >= ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.doubleValue >= ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.doubleValue >= ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>=' must be numeric!");
	    }
	    break;
	case Result.FLOAT   :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.floatValue >= ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.floatValue >= ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.floatValue >= ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.floatValue >= ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.floatValue >= ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.floatValue >= ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.floatValue >= ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>=' must be numeric!");
	    }
	    break;
	case Result.LONG    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.longValue >= ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.longValue >= ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.longValue >= ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.longValue >= ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.longValue >= ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.longValue >= ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.longValue >= ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>=' must be numeric!");
	    }
	    break;
	case Result.INT     :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.intValue >= ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.intValue >= ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.intValue >= ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.intValue >= ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.intValue >= ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.intValue >= ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.intValue >= ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>=' must be numeric!");
	    }
	    break;
	case Result.SHORT   :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.shortValue >= ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.shortValue >= ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.shortValue >= ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.shortValue >= ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.shortValue >= ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.shortValue >= ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.shortValue >= ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>=' must be numeric!");
	    }
	    break;
	case Result.CHAR    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.charValue >= ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.charValue >= ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.charValue >= ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.charValue >= ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.charValue >= ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.charValue >= ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.charValue >= ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>=' must be numeric!");
	    }
	    break;
	case Result.BYTE    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.booleanValue = ex1.byteValue >= ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.booleanValue = ex1.byteValue >= ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.booleanValue = ex1.byteValue >= ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.booleanValue = ex1.byteValue >= ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.booleanValue = ex1.byteValue >= ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.booleanValue = ex1.byteValue >= ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.booleanValue = ex1.byteValue >= ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>=' must be numeric!");
	    }
	    break;
	default		    :
	    throw new ParseException(
		    "Expression left of '>=' must be numeric!");
    }
    ex1.type = Result.BOOLEAN;
  }

  public void instanceOf() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    CachedClass c = null;
    ex1.assignables = null;
    if (ex1.type != Result.OBJECT)
	throw new ParseException(
		"Expression on left of 'instanceof' must be a reference!");
    if (ex2.type != Result.CLASS)
	throw new ParseException(
		"Right of 'instanceof' must be a reference type!");
    if (ex2.refType.charAt(0) == '[') // array types are not pre looked-up
	try {
	    c = classCache.forName(ex2.refType);
	}
	catch (ClassNotFoundException e) {
	    throw new ParseException(
		"Class " + (String) ex2.ref + " not found!");
	}
    else
	c = (CachedClass) ex2.ref;
    ex1.booleanValue = c.isInstance(ex1);
    ex1.type = Result.BOOLEAN;
  }

  public void leftShift() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.LONG    :
	    ex1.type = Result.LONG;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.longValue << ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.longValue = ex1.longValue << ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.longValue = ex1.longValue << ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.longValue = ex1.longValue << ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.longValue = ex1.longValue << ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<<' must be integral!");
	    }
	    break;
	case Result.INT     :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.intValue = ex1.intValue << ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.intValue << ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.intValue << ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.intValue << ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.intValue << ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<<' must be integral!");
	    }
	    break;
	case Result.SHORT   :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.intValue = ex1.shortValue << ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.shortValue << ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.shortValue << ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.shortValue << ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.shortValue << ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<<' must be integral!");
	    }
	    break;
	case Result.CHAR    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.intValue = ex1.charValue << ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.charValue << ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.charValue << ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.charValue << ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.charValue << ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<<' must be integral!");
	    }
	    break;
	case Result.BYTE    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.intValue = ex1.byteValue << ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.byteValue << ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.byteValue << ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.byteValue << ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.byteValue << ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '<<' must be integral!");
	    }
	    break;
	default		    :
	    throw new ParseException(
		    "Expression left of '<<' must be integral!");
    }
  }

  public void rightShift() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.LONG    :
	    ex1.type = Result.LONG;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.longValue >> ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.longValue = ex1.longValue >> ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.longValue = ex1.longValue >> ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.longValue = ex1.longValue >> ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.longValue = ex1.longValue >> ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>>' must be integral!");
	    }
	    break;
	case Result.INT     :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.intValue = ex1.intValue >> ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.intValue >> ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.intValue >> ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.intValue >> ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.intValue >> ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>>' must be integral!");
	    }
	    break;
	case Result.SHORT   :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.intValue = ex1.shortValue >> ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.shortValue >> ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.shortValue >> ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.shortValue >> ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.shortValue >> ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>>' must be integral!");
	    }
	    break;
	case Result.CHAR    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.intValue = ex1.charValue >> ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.charValue >> ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.charValue >> ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.charValue >> ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.charValue >> ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>>' must be integral!");
	    }
	    break;
	case Result.BYTE    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.intValue = ex1.byteValue >> ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.byteValue >> ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.byteValue >> ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.byteValue >> ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.byteValue >> ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>>' must be integral!");
	    }
	    break;
	default		    :
	    throw new ParseException(
		    "Expression left of '>>' must be integral!");
    }
  }

  public void unsignedRightShift() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.LONG    :
	    ex1.type = Result.LONG;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.longValue = ex1.longValue >>> ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.longValue = ex1.longValue >>> ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.longValue = ex1.longValue >>> ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.longValue = ex1.longValue >>> ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.longValue = ex1.longValue >>> ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>>>' must be integral!");
	    }
	    break;
	case Result.INT     :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.intValue = ex1.intValue >>> ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.intValue >>> ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.intValue >>> ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.intValue >>> ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.intValue >>> ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>>>' must be integral!");
	    }
	    break;
	case Result.SHORT   :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.intValue = ex1.shortValue >>> ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.shortValue >>> ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.shortValue >>> ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.shortValue >>> ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.shortValue >>> ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>>>' must be integral!");
	    }
	    break;
	case Result.CHAR    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.intValue = ex1.charValue >>> ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.charValue >>> ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.charValue >>> ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.charValue >>> ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.charValue >>> ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>>>' must be integral!");
	    }
	    break;
	case Result.BYTE    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.LONG    :
		    ex1.intValue = ex1.byteValue >>> ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.byteValue >>> ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.byteValue >>> ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.byteValue >>> ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.byteValue >>> ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '>>>' must be integral!");
	    }
	    break;
	default		    :
	    throw new ParseException(
		    "Expression left of '>>>' must be integral!");
    }
  }

  public void add() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.DOUBLE  :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.doubleValue + ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.doubleValue = ex1.doubleValue + ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.doubleValue = ex1.doubleValue + ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.doubleValue = ex1.doubleValue + ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.doubleValue = ex1.doubleValue + ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.doubleValue = ex1.doubleValue + ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.doubleValue = ex1.doubleValue + ex2.byteValue;
		    break;
		case Result.OBJECT  :
		    if (ex2.refType.equals("java.lang.String")) {
			ex1.ref = ex1.doubleValue + (String) ex2.ref;
			ex1.type = Result.OBJECT;
			ex1.refType = "java.lang.String";
		        break;
		    }
		default		    :
		    throw new ParseException(
			"Expression right of '+' must be numeric or String!");
	    }
	    break;
	case Result.FLOAT   :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.floatValue + ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.floatValue + ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.floatValue = ex1.floatValue + ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.floatValue = ex1.floatValue + ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.floatValue = ex1.floatValue + ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.floatValue = ex1.floatValue + ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.floatValue = ex1.floatValue + ex2.byteValue;
		    break;
		case Result.OBJECT  :
		    if (ex2.refType.equals("java.lang.String")) {
			ex1.ref = ex1.floatValue + (String) ex2.ref;
			ex1.type = Result.OBJECT;
		    	ex1.refType = "java.lang.String";
			break;
		    }
		default		    :
		    throw new ParseException(
			"Expression right of '+' must be numeric or String!");
	    }
	    break;
	case Result.LONG    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.longValue + ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.longValue + ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.longValue + ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.longValue = ex1.longValue + ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.longValue = ex1.longValue + ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.longValue = ex1.longValue + ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.longValue = ex1.longValue + ex2.byteValue;
		    break;
		case Result.OBJECT  :
		    if (ex2.refType.equals("java.lang.String")) {
			ex1.ref = ex1.longValue + (String) ex2.ref;
			ex1.type = Result.OBJECT;
			ex1.refType = "java.lang.String";
			break;
		    }
		default		    :
		    throw new ParseException(
			"Expression right of '+' must be numeric or String!");
	    }
	    break;
	case Result.INT     :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.intValue + ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.intValue + ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.intValue + ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.intValue + ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.intValue + ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.intValue + ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.intValue + ex2.byteValue;
		    break;
		case Result.OBJECT  :
		    if (ex2.refType.equals("java.lang.String")) {
			ex1.ref = ex1.intValue + (String) ex2.ref;
			ex1.type = Result.OBJECT;
			ex1.refType = "java.lang.String";
			break;
		    }
		default		    :
		    throw new ParseException(
			"Expression right of '+' must be numeric or String!");
	    }
	    break;
	case Result.SHORT   :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.shortValue + ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.shortValue + ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.shortValue + ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.shortValue + ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.shortValue + ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.shortValue + ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.shortValue + ex2.byteValue;
		    break;
		case Result.OBJECT  :
		    if (ex2.refType.equals("java.lang.String")) {
			ex1.ref = ex1.shortValue + (String) ex2.ref;
			ex1.type = Result.OBJECT;
			ex1.refType = "java.lang.String";
			break;
		    }
		default		    :
		    throw new ParseException(
			"Expression right of '+' must be numeric or String!");
	    }
	    break;
	case Result.CHAR    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.charValue + ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.charValue + ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.charValue + ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.charValue + ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.charValue + ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.charValue + ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.charValue + ex2.byteValue;
		    break;
		case Result.OBJECT  :
		    if (ex2.refType.equals("java.lang.String")) {
			ex1.ref = ex1.charValue + (String) ex2.ref;
			ex1.type = Result.OBJECT;
			ex1.refType = "java.lang.String";
			break;
		    }
		default		    :
		    throw new ParseException(
			"Expression right of '+' must be numeric or String!");
	    }
	    break;
	case Result.BYTE    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.byteValue + ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.byteValue + ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.byteValue + ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.byteValue + ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.byteValue + ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.byteValue + ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.byteValue + ex2.byteValue;
		    break;
		case Result.OBJECT  :
		    if (ex2.refType.equals("java.lang.String")) {
			ex1.ref = ex1.byteValue + (String) ex2.ref;
			ex1.type = Result.OBJECT;
			ex1.refType = "java.lang.String";
			break;
		    }
		default		    :
		    throw new ParseException(
			"Expression right of '+' must be numeric or String!");
	    }
	    break;
	case Result.OBJECT  :
	    if (ex1.refType.equals("java.lang.String")) {
		String ex1String = (String) ex1.ref;
		switch (ex2.type) {
		    case Result.DOUBLE  :
			ex1.ref = ex1String + ex2.doubleValue;
			break;
		    case Result.FLOAT   :
			ex1.ref = ex1String + ex2.floatValue;
			break;
		    case Result.LONG    :
			ex1.ref = ex1String + ex2.longValue;
			break;
		    case Result.INT     :
			ex1.ref = ex1String + ex2.intValue;
			break;
		    case Result.SHORT   :
			ex1.ref = ex1String + ex2.shortValue;
			break;
		    case Result.CHAR    :
			ex1.ref = ex1String + ex2.charValue;
			break;
		    case Result.BYTE    :
			ex1.ref = ex1String + ex2.byteValue;
			break;
		    case Result.OBJECT  :
			ex1.ref = ex1String + ex2.ref;
			break;
		    default		    :
			throw new ParseException(
			"Expression of unknown type on right of '+', BUG!");
		}
		break;
	    }
	default		    :
	    throw new ParseException(
		    "Expression left of '+' must be numeric or String!");
    }
  }

  public void subtract() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.DOUBLE  :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.doubleValue - ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.doubleValue = ex1.doubleValue - ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.doubleValue = ex1.doubleValue - ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.doubleValue = ex1.doubleValue - ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.doubleValue = ex1.doubleValue - ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.doubleValue = ex1.doubleValue - ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.doubleValue = ex1.doubleValue - ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '-' must be numeric!");
	    }
	    break;
	case Result.FLOAT   :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.floatValue - ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.floatValue - ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.floatValue = ex1.floatValue - ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.floatValue = ex1.floatValue - ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.floatValue = ex1.floatValue - ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.floatValue = ex1.floatValue - ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.floatValue = ex1.floatValue - ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '-' must be numeric!");
	    }
	    break;
	case Result.LONG    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.longValue - ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.longValue - ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.longValue - ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.longValue = ex1.longValue - ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.longValue = ex1.longValue - ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.longValue = ex1.longValue - ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.longValue = ex1.longValue - ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '-' must be numeric!");
	    }
	    break;
	case Result.INT     :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.intValue - ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.intValue - ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.intValue - ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.intValue - ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.intValue - ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.intValue - ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.intValue - ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '-' must be numeric!");
	    }
	    break;
	case Result.SHORT   :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.shortValue - ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.shortValue - ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.shortValue - ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.shortValue - ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.shortValue - ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.shortValue - ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.shortValue - ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '-' must be numeric!");
	    }
	    break;
	case Result.CHAR    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.charValue - ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.charValue - ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.charValue - ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.charValue - ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.charValue - ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.charValue - ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.charValue - ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '-' must be numeric!");
	    }
	    break;
	case Result.BYTE    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.byteValue - ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.byteValue - ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.byteValue - ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.byteValue - ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.byteValue - ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.byteValue - ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.byteValue - ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '-' must be numeric!");
	    }
	    break;
	default		    :
	    throw new ParseException(
		    "Expression left of '-' must be numeric!");
    }
  }

  public void multiply() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.DOUBLE  :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.doubleValue * ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.doubleValue = ex1.doubleValue * ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.doubleValue = ex1.doubleValue * ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.doubleValue = ex1.doubleValue * ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.doubleValue = ex1.doubleValue * ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.doubleValue = ex1.doubleValue * ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.doubleValue = ex1.doubleValue * ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '*' must be numeric!");
	    }
	    break;
	case Result.FLOAT   :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.floatValue * ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.floatValue * ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.floatValue = ex1.floatValue * ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.floatValue = ex1.floatValue * ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.floatValue = ex1.floatValue * ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.floatValue = ex1.floatValue * ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.floatValue = ex1.floatValue * ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '*' must be numeric!");
	    }
	    break;
	case Result.LONG    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.longValue * ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.longValue * ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.longValue * ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.longValue = ex1.longValue * ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.longValue = ex1.longValue * ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.longValue = ex1.longValue * ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.longValue = ex1.longValue * ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '*' must be numeric!");
	    }
	    break;
	case Result.INT     :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.intValue * ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.intValue * ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.intValue * ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.intValue * ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.intValue * ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.intValue * ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.intValue * ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '*' must be numeric!");
	    }
	    break;
	case Result.SHORT   :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.shortValue * ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.shortValue * ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.shortValue * ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.shortValue * ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.shortValue * ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.shortValue * ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.shortValue * ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '*' must be numeric!");
	    }
	    break;
	case Result.CHAR    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.charValue * ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.charValue * ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.charValue * ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.charValue * ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.charValue * ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.charValue * ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.charValue * ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '*' must be numeric!");
	    }
	    break;
	case Result.BYTE    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.byteValue * ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.byteValue * ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.byteValue * ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.byteValue * ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.byteValue * ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.byteValue * ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.byteValue * ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '*' must be numeric!");
	    }
	    break;
	default		    :
	    throw new ParseException(
		    "Expression left of '*' must be numeric!");
    }
  }

  public void divide() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.DOUBLE  :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.doubleValue / ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.doubleValue = ex1.doubleValue / ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.doubleValue = ex1.doubleValue / ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.doubleValue = ex1.doubleValue / ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.doubleValue = ex1.doubleValue / ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.doubleValue = ex1.doubleValue / ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.doubleValue = ex1.doubleValue / ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '/' must be numeric!");
	    }
	    break;
	case Result.FLOAT   :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.floatValue / ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.floatValue / ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.floatValue = ex1.floatValue / ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.floatValue = ex1.floatValue / ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.floatValue = ex1.floatValue / ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.floatValue = ex1.floatValue / ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.floatValue = ex1.floatValue / ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '/' must be numeric!");
	    }
	    break;
	case Result.LONG    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.longValue / ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.longValue / ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.longValue / ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.longValue = ex1.longValue / ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.longValue = ex1.longValue / ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.longValue = ex1.longValue / ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.longValue = ex1.longValue / ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '/' must be numeric!");
	    }
	    break;
	case Result.INT     :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.intValue / ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.intValue / ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.intValue / ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.intValue / ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.intValue / ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.intValue / ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.intValue / ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '/' must be numeric!");
	    }
	    break;
	case Result.SHORT   :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.shortValue / ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.shortValue / ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.shortValue / ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.shortValue / ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.shortValue / ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.shortValue / ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.shortValue / ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '/' must be numeric!");
	    }
	    break;
	case Result.CHAR    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.charValue / ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.charValue / ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.charValue / ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.charValue / ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.charValue / ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.charValue / ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.charValue / ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '/' must be numeric!");
	    }
	    break;
	case Result.BYTE    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.byteValue / ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.byteValue / ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.byteValue / ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.byteValue / ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.byteValue / ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.byteValue / ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.byteValue / ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '/' must be numeric!");
	    }
	    break;
	default		    :
	    throw new ParseException(
		    "Expression left of '/' must be numeric!");
    }
  }

  public void remainder() throws ParseException {
    Result ex1 = null , ex2 = null;
    try { ex2 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.DOUBLE  :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.doubleValue % ex2.doubleValue;
		    break;
		case Result.FLOAT   :
		    ex1.doubleValue = ex1.doubleValue % ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.doubleValue = ex1.doubleValue % ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.doubleValue = ex1.doubleValue % ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.doubleValue = ex1.doubleValue % ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.doubleValue = ex1.doubleValue % ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.doubleValue = ex1.doubleValue % ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '%' must be numeric!");
	    }
	    break;
	case Result.FLOAT   :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.floatValue % ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.floatValue % ex2.floatValue;
		    break;
		case Result.LONG    :
		    ex1.floatValue = ex1.floatValue % ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.floatValue = ex1.floatValue % ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.floatValue = ex1.floatValue % ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.floatValue = ex1.floatValue % ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.floatValue = ex1.floatValue % ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '%' must be numeric!");
	    }
	    break;
	case Result.LONG    :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.longValue % ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.longValue % ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.longValue % ex2.longValue;
		    break;
		case Result.INT     :
		    ex1.longValue = ex1.longValue % ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.longValue = ex1.longValue % ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.longValue = ex1.longValue % ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.longValue = ex1.longValue % ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '%' must be numeric!");
	    }
	    break;
	case Result.INT     :
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.intValue % ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.intValue % ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.intValue % ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.intValue % ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.intValue % ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.intValue % ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.intValue % ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '%' must be numeric!");
	    }
	    break;
	case Result.SHORT   :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.shortValue % ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.shortValue % ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.shortValue % ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.shortValue % ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.shortValue % ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.shortValue % ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.shortValue % ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '%' must be numeric!");
	    }
	    break;
	case Result.CHAR    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.charValue % ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.charValue % ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.charValue % ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.charValue % ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.charValue % ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.charValue % ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.charValue % ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '%' must be numeric!");
	    }
	    break;
	case Result.BYTE    :
	    ex1.type = Result.INT;
	    switch (ex2.type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.byteValue % ex2.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.byteValue % ex2.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.byteValue % ex2.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.byteValue % ex2.intValue;
		    break;
		case Result.SHORT   :
		    ex1.intValue = ex1.byteValue % ex2.shortValue;
		    break;
		case Result.CHAR    :
		    ex1.intValue = ex1.byteValue % ex2.charValue;
		    break;
		case Result.BYTE    :
		    ex1.intValue = ex1.byteValue % ex2.byteValue;
		    break;
		default		    :
		    throw new ParseException(
			"Expression right of '%' must be numeric!");
	    }
	    break;
	default		    :
	    throw new ParseException(
		    "Expression left of '%' must be numeric!");
    }
  }

  public void unaryPlus() throws ParseException {
    Result ex1 = null;
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.DOUBLE :
	case Result.FLOAT  :
	case Result.LONG   :
	case Result.INT    : break;
	case Result.SHORT  : ex1.intValue = ex1.shortValue;
			     ex1.type = Result.INT;
			     break;
	case Result.CHAR   : ex1.intValue = ex1.charValue;
			     ex1.type = Result.INT;
			     break;
	case Result.BYTE   : ex1.intValue = ex1.byteValue;
			     ex1.type = Result.INT;
			     break;
	default		   :
	    throw new ParseException(
		"Unary '+' must be followed by a numeric expression!");
    }
  }

  public void unaryMinus() throws ParseException {
    Result ex1 = null;
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.DOUBLE : ex1.doubleValue = -ex1.doubleValue; break;
	case Result.FLOAT  : ex1.floatValue = -ex1.floatValue; break;
	case Result.LONG   : ex1.longValue = -ex1.longValue; break;
	case Result.INT    : ex1.intValue = -ex1.intValue; break;
	case Result.SHORT  : ex1.intValue = -ex1.shortValue;
			     ex1.type = Result.INT;
			     break;
	case Result.CHAR   : ex1.intValue = -ex1.charValue;
			     ex1.type = Result.INT;
			     break;
	case Result.BYTE   : ex1.intValue = -ex1.byteValue;
			     ex1.type = Result.INT;
			     break;
	default		   :
	    throw new ParseException(
		"Unary '-' must be followed by a numeric expression!");
    }
  }

  public void preIncrement() throws ParseException {
    Result ex1 = null;
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    switch (ex1.type) {
	case Result.DOUBLE : ++ex1.doubleValue; break;
	case Result.FLOAT  : ++ex1.floatValue; break;
	case Result.LONG   : ++ex1.longValue; break;
	case Result.INT    : ++ex1.intValue; break;
	case Result.SHORT  : ++ex1.shortValue; break;
	case Result.CHAR   : ++ex1.charValue; break;
	case Result.BYTE   : ++ex1.byteValue; break;
	default		   :
	    throw new ParseException(
		"'++' must be followed by a numeric expression!");
    }
    setVariable(ex1);
  }

  public void preDecrement() throws ParseException {
    Result ex1 = null;
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    switch (ex1.type) {
	case Result.DOUBLE : --ex1.doubleValue; break;
	case Result.FLOAT  : --ex1.floatValue; break;
	case Result.LONG   : --ex1.longValue; break;
	case Result.INT    : --ex1.intValue; break;
	case Result.SHORT  : --ex1.shortValue; break;
	case Result.CHAR   : --ex1.charValue; break;
	case Result.BYTE   : --ex1.byteValue; break;
	default		   :
	    throw new ParseException(
		"'--' must be followed by a numeric expression!");
    }
    setVariable(ex1);
  }

  public void bitComplement() throws ParseException {
    Result ex1 = null;
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.LONG   : ex1.longValue = ~ex1.longValue; break;
	case Result.INT    : ex1.intValue = ~ex1.intValue; break;
	case Result.SHORT  : ex1.intValue = ~ex1.shortValue;
			     ex1.type = Result.INT;
			     break;
	case Result.CHAR   : ex1.intValue = ~ex1.charValue;
			     ex1.type = Result.INT;
			     break;
	case Result.BYTE   : ex1.intValue = ~ex1.byteValue;
			     ex1.type = Result.INT;
			     break;
	default		   :
	    throw new ParseException(
		"'~' must be followed by an integral expression!");
    }
  }

  public void logicalComplement() throws ParseException {
    Result ex1 = null;
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    if (ex1.type == Result.BOOLEAN)
	ex1.booleanValue = !ex1.booleanValue;
    else
	throw new ParseException(
	    "'!' must be followed by a boolean expression!");
  }

  public void primitiveCast(byte type, int dims) throws ParseException {
    if (dims > 0) {
	arrayCast(type, dims);
	return;
    }
    Result ex1 = null;
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    switch (ex1.type) {
	case Result.DOUBLE  :
	    switch (type) {
		case Result.DOUBLE  : break;
		case Result.FLOAT   :
		    ex1.floatValue = (float) ex1.doubleValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = (long) ex1.doubleValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = (int) ex1.doubleValue;
		    ex1.type = Result.INT;
		    break;
		case Result.SHORT   :
		    ex1.shortValue = (short) ex1.doubleValue;
		    ex1.type = Result.SHORT;
		    break;
		case Result.CHAR    :
		    ex1.charValue = (char) ex1.doubleValue;
		    ex1.type = Result.CHAR;
		    break;
		case Result.BYTE    :
		    ex1.byteValue = (byte) ex1.doubleValue;
		    ex1.type = Result.BYTE;
		    break;
		case Result.BOOLEAN :
		    throw new ParseException(
			"Cannot cast a numeric value to a Boolean");
		case Result.OBJECT  :
		    throw new ParseException(
			"Cannot cast a numeric value to a Reference");
		default		    :
		    throw new ParseException(
			"Invalid type, possibly a bug!");

	    }
	case Result.FLOAT   :
	    switch (type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.floatValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   : break;
		case Result.LONG    :
		    ex1.longValue = (long) ex1.floatValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = (int) ex1.floatValue;
		    ex1.type = Result.INT;
		    break;
		case Result.SHORT   :
		    ex1.shortValue = (short) ex1.floatValue;
		    ex1.type = Result.SHORT;
		    break;
		case Result.CHAR    :
		    ex1.charValue = (char) ex1.floatValue;
		    ex1.type = Result.CHAR;
		    break;
		case Result.BYTE    :
		    ex1.byteValue = (byte) ex1.floatValue;
		    ex1.type = Result.BYTE;
		    break;
		case Result.BOOLEAN :
		    throw new ParseException(
			"Cannot cast a numeric value to a Boolean");
		case Result.OBJECT  :
		    throw new ParseException(
			"Cannot cast a numeric value to a Reference");
		default		    :
		    throw new ParseException(
			"Invalid type, possibly a bug!");

	    }
	case Result.LONG    :
	    switch (type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.longValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue =  ex1.longValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    : break;
		case Result.INT     :
		    ex1.intValue = (int) ex1.longValue;
		    ex1.type = Result.INT;
		    break;
		case Result.SHORT   :
		    ex1.shortValue = (short) ex1.longValue;
		    ex1.type = Result.SHORT;
		    break;
		case Result.CHAR    :
		    ex1.charValue = (char) ex1.longValue;
		    ex1.type = Result.CHAR;
		    break;
		case Result.BYTE    :
		    ex1.byteValue = (byte) ex1.longValue;
		    ex1.type = Result.BYTE;
		    break;
		case Result.BOOLEAN :
		    throw new ParseException(
			"Cannot cast a numeric value to a Boolean");
		case Result.OBJECT  :
		    throw new ParseException(
			"Cannot cast a numeric value to a Reference");
		default		    :
		    throw new ParseException(
			"Invalid type, possibly a bug!");
	    }
	case Result.INT     :
	    switch (type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.intValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.intValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.intValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     : break;
		case Result.SHORT   :
		    ex1.shortValue = (short) ex1.intValue;
		    ex1.type = Result.SHORT;
		    break;
		case Result.CHAR    :
		    ex1.charValue = (char) ex1.intValue;
		    ex1.type = Result.CHAR;
		    break;
		case Result.BYTE    :
		    ex1.byteValue = (byte) ex1.intValue;
		    ex1.type = Result.BYTE;
		    break;
		case Result.BOOLEAN :
		    throw new ParseException(
			"Cannot cast a numeric value to a Boolean");
		case Result.OBJECT  :
		    throw new ParseException(
			"Cannot cast a numeric value to a Reference");
		default		    :
		    throw new ParseException(
			"Invalid type, possibly a bug!");
	    }
	case Result.SHORT   :
	    switch (type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.shortValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.shortValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.shortValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.shortValue;
		    ex1.type = Result.INT;
		    break;
		case Result.SHORT   : break;
		case Result.CHAR    :
		    ex1.charValue = (char) ex1.shortValue;
		    ex1.type = Result.CHAR;
		    break;
		case Result.BYTE    :
		    ex1.byteValue = (byte) ex1.shortValue;
		    ex1.type = Result.BYTE;
		    break;
		case Result.BOOLEAN :
		    throw new ParseException(
			"Cannot cast a numeric value to a Boolean");
		case Result.OBJECT  :
		    throw new ParseException(
			"Cannot cast a numeric value to a Reference");
		default		    :
		    throw new ParseException(
			"Invalid type, possibly a bug!");
	    }
	case Result.CHAR    :
	    switch (type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.charValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.charValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.charValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.charValue;
		    ex1.type = Result.INT;
		    break;
		case Result.SHORT   :
		    ex1.shortValue = (short) ex1.charValue;
		    ex1.type = Result.SHORT;
		    break;
		case Result.CHAR    : break;
		case Result.BYTE    :
		    ex1.byteValue = (byte) ex1.charValue;
		    ex1.type = Result.BYTE;
		    break;
		case Result.BOOLEAN :
		    throw new ParseException(
			"Cannot cast a numeric value to a Boolean");
		case Result.OBJECT  :
		    throw new ParseException(
			"Cannot cast a numeric value to a Reference");
		default		    :
		    throw new ParseException(
			"Invalid type, possibly a bug!");
	    }
	case Result.BYTE    :
	    switch (type) {
		case Result.DOUBLE  :
		    ex1.doubleValue = ex1.byteValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT   :
		    ex1.floatValue = ex1.byteValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG    :
		    ex1.longValue = ex1.byteValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT     :
		    ex1.intValue = ex1.byteValue;
		    ex1.type = Result.INT;
		    break;
		case Result.SHORT   :
		    ex1.shortValue =  ex1.byteValue;
		    ex1.type = Result.SHORT;
		    break;
		case Result.CHAR    :
		    ex1.charValue =  (char) ex1.byteValue;
		    ex1.type = Result.CHAR;
		    break;
		case Result.BYTE    : break;
		case Result.BOOLEAN :
		    throw new ParseException(
			"Cannot cast a numeric value to a Boolean");
		case Result.OBJECT  :
		    throw new ParseException(
			"Cannot cast a numeric value to a Reference");
		default		    :
		    throw new ParseException(
			"Invalid type, possibly a bug!");
	    }
	case Result.BOOLEAN :
		if (type != Result.BOOLEAN)
		    throw new ParseException(
			"Cannot cast from boolean to a different type");
		break;
	case Result.OBJECT  :
		throw new ParseException(
		    "Cannot cast from reference type to primitive types");
	default		    :
		throw new ParseException(
		    "Invalid type, possibly a bug!");
    }
  }

  static String makeBrackets(int dims) {
    char[] brackets = new char[dims];
    for (int i = 0; i < dims; brackets[i++] = '[');
    return new String(brackets);
  }

  public void arrayCast(byte type, int dims)
	throws ParseException {
    Result ex1 = null;
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    String typeString = makeBrackets(dims) + Result.typeMap[type];
    if (ex1.type != Result.OBJECT)
	throw new ParseException(
	    "Primitive types cannot be casted to an array!");
    try {
	if (classCache.forName(typeString).isInstance(ex1))
	   ex1.refType = typeString;
	else
	   throw new ParseException(
		"Cannot cast " + ex1.refType + " to " + typeString + "!");
    }
    catch (ClassNotFoundException e) {
	throw new ParseException(
		"Bug! array type " + typeString + " not found!");
    }
  }

  public void refCast(String typeName, int dims) throws ParseException {
    if (dims > 0) {
	refArrayCast(typeName, dims);
	return;
    }
    Result ex1 = null;
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    if (ex1.type != Result.OBJECT)
	throw new ParseException(
	    "Primitive types cannot be casted to a reference!");
    try {
	Result r = findClass(typeName);
	if (((CachedClass) r.ref).isInstance(ex1))
	   ex1.refType = r.refType;
	else
	   throw new ParseException(
		"Cannot cast " + ex1.refType + " to " + typeName + "!");
    }
    catch (ClassNotFoundException e) {
	throw new ParseException(
		"Type " + typeName + " not found!");
    }
  }

  public void refArrayCast(String typeName, int dims)
	throws ParseException {
    Result ex1 = null;
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;
    if (ex1.type != Result.OBJECT)
	throw new ParseException(
	    "Primitive types cannot be casted to a object arrays!");
    Result r = null;
    try {
	r = findClass(typeName);
    }
    catch (ClassNotFoundException e) {
	throw new ParseException(e.getMessage());
    }
    typeName = makeBrackets(dims) + 'L' + r.refType + ';';
    try {
	if (classCache.forName(typeName).isInstance(ex1))
	   ex1.refType = typeName;
	else
	   throw new ParseException(
		"Cannot cast " + ex1.refType + " to " + typeName + "!");
    }
    catch (ClassNotFoundException e) {
	throw new ParseException(
		"Type " + typeName + " not found!");
    }
  }

  public void arrayIndexRef() throws ParseException {
    Result prefix = null , ex1 = null;
    try { ex1 = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { prefix = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(prefix.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        prefix = (Result) prefix.clone();
        opsStack.pop();
        opsStack.push(prefix);
    }
    if (prefix.type == Result.OBJECT && prefix.refType.charAt(0) == '[') {
	int arrayIndex;
	// Get the index value first.
	switch (ex1.type) {
	    case Result.INT   : arrayIndex = ex1.intValue; break;
	    case Result.SHORT : arrayIndex = ex1.shortValue; break;
	    case Result.CHAR  : arrayIndex = ex1.charValue; break;
	    case Result.BYTE  : arrayIndex = ex1.byteValue; break;
	    default	      : throw new ParseException(
	"Array index must be an integral expression not bigger than int!");
	}
	// Check whether it is a primitive array or not.
	byte i;
	for (i = 0; i < Result.typeMap.length; i++)
	    if (prefix.refType.charAt(1) == Result.typeMap[i])
		break;
	prefix.type = i;
	Object[] oa = new Object[2];
	oa[0] = prefix.ref;
	oa[1] = new Integer(arrayIndex);
	switch (i) {
	    case Result.DOUBLE	:
		prefix.doubleValue = ((double []) prefix.ref)[arrayIndex];
		break;
	    case Result.FLOAT	:
		prefix.floatValue = ((float []) prefix.ref)[arrayIndex];
		break;
	    case Result.LONG	:
		prefix.longValue = ((long []) prefix.ref)[arrayIndex];
		break;
	    case Result.INT	:
		prefix.intValue = ((int []) prefix.ref)[arrayIndex];
		break;
	    case Result.SHORT	:
		prefix.shortValue = ((short []) prefix.ref)[arrayIndex];
		break;
	    case Result.CHAR	:
		prefix.charValue = ((char []) prefix.ref)[arrayIndex];
		break;
	    case Result.BYTE	:
		prefix.byteValue = ((byte []) prefix.ref)[arrayIndex];
		break;
	    case Result.BOOLEAN :
		prefix.booleanValue = ((boolean []) prefix.ref)[arrayIndex];
		break;
	    case Result.OBJECT  :
		prefix.ref = ((Object []) prefix.ref)[arrayIndex];

		char refType = prefix.refType.charAt(1);

		if (refType == 'L') {
		    // New reference is an Object,
		    // type format ex. "[Ljava.lang.Object;"
		    // reduced to "java.lang.Object"
		    prefix.refType = prefix.refType.substring(
			2, prefix.refType.length() - 1);
		    break;
		}
		else if (refType == '[') {
		    // New reference is an Object array,
		    // type format ex. "[[Ljava.lang.Object;"
		    // reduced to "[Ljava.lang.Object;"
		    prefix.refType = prefix.refType.substring(
			2, prefix.refType.length());
		    break;
		}
	    default		:
		throw new ParseException(
			"Cannot determine reference type, possibly a bug!");
	}
	prefix.assignables = oa;
    }
    else
	throw new ParseException("Expression is not of an array type!");
  }

  Result assignFromField(Result prefix, Field identField)
	throws ParseException {
    if (debugging)
      debug.println(8, "Entering assignFromField(" + prefix + ", " + identField.getName() + ")");
    if (Boolean.TRUE.equals(prefix.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        prefix = (Result) prefix.clone();
    }
    Class fieldType = identField.getType();
    Object[] oa = new Object[2];

    // We always need to keep the field reference around for
    //  assignment purposes when the result is a field reference.
    oa[0] = prefix.ref;
    oa[1] = identField;

    try {
	if (fieldType.isPrimitive())
	    if (fieldType.equals(Integer.TYPE)) {
		prefix.type = Result.INT;
		prefix.intValue = identField.getInt(prefix.ref);
	    }
	    else if (fieldType.equals(Long.TYPE)) {
		prefix.type = Result.LONG;
		prefix.longValue = identField.getLong(prefix.ref);
	    }
	    else if (fieldType.equals(Short.TYPE)) {
		prefix.type = Result.SHORT;
		prefix.shortValue = identField.getShort(prefix.ref);
	    }
	    else if (fieldType.equals(Character.TYPE)) {
		prefix.type = Result.CHAR;
		prefix.charValue = identField.getChar(prefix.ref);
	    }
	    else if (fieldType.equals(Byte.TYPE)) {
		prefix.type = Result.BYTE;
		prefix.byteValue = identField.getByte(prefix.ref);
	    }
	    else if (fieldType.equals(Boolean.TYPE)) {
		prefix.type = Result.BOOLEAN;
		prefix.booleanValue = identField.getBoolean(prefix.ref);
	    }
	    else if (fieldType.equals(Float.TYPE)) {
		prefix.type = Result.FLOAT;
		prefix.floatValue = identField.getFloat(prefix.ref);
	    }
	    else if (fieldType.equals(Double.TYPE)) {
		prefix.type = Result.DOUBLE;
		prefix.doubleValue = identField.getDouble(prefix.ref);
	    }
	    else
		throw new ParseException(
		    "BUG! Detected primitive field with uncovered type!");
	else {
	    prefix.type = Result.OBJECT;
	    prefix.refType = fieldType.getName();
	    prefix.ref = identField.get(prefix.ref);
   	}
        prefix.assignables = oa;
    }
    catch (IllegalAccessException e) {
	throw new ParseException(e.getMessage());
    }
    return prefix;
  }

  Class[] makeParameterTypes(List parameters)
	throws ParseException {
    Class[] paramTypes = new Class[parameters.size()];
    for (int i = 0; i < parameters.size(); i++) {
	Result r = (Result) parameters.get(i);
	switch (r.type) {
	    case Result.DOUBLE	: paramTypes[i] = Double.TYPE;	break;
	    case Result.FLOAT	: paramTypes[i] = Float.TYPE;	break;
	    case Result.LONG	: paramTypes[i] = Long.TYPE;	break;
	    case Result.INT	: paramTypes[i] = Integer.TYPE;	break;
	    case Result.SHORT	: paramTypes[i] = Short.TYPE;	break;
	    case Result.CHAR	: paramTypes[i] = Character.TYPE; break;
	    case Result.BYTE	: paramTypes[i] = Byte.TYPE;	break;
	    case Result.BOOLEAN	: paramTypes[i] = Boolean.TYPE;	break;
	    case Result.OBJECT  :
		try {
		    paramTypes[i] = classCache.forName(
			r.refType).getClassObject();
		} catch (ClassNotFoundException e) {
		    throw new ParseException("Class " + r.refType +
					" not found, possibly a bug!");
		}
		break;
	    default : throw new ParseException(
			"Invalid type in parameters, possibly a bug!");
	}
    }
    return paramTypes;
  }

  static Object[] makeParameterArray(List parameters)
	throws ParseException {
    Object[] paramArray = new Object[parameters.size()];
    for (int i = 0; i < parameters.size(); i++) {
	Result r = (Result) parameters.get(i);
	switch (r.type) {
	    case Result.DOUBLE	:
		paramArray[i] = new Double(r.doubleValue);	break;
	    case Result.FLOAT	:
		paramArray[i] = new Float(r.floatValue);	break;
	    case Result.LONG	:
		paramArray[i] = new Long(r.longValue);		break;
	    case Result.INT	:
		paramArray[i] = new Integer(r.intValue);	break;
	    case Result.SHORT	:
		paramArray[i] = new Short(r.shortValue);	break;
	    case Result.CHAR	:
		paramArray[i] = new Character(r.charValue);	break;
	    case Result.BYTE	:
		paramArray[i] = new Byte(r.byteValue);		break;
	    case Result.BOOLEAN	:
		paramArray[i] = new Boolean(r.booleanValue);	break;
	    case Result.OBJECT  : paramArray[i] = r.ref;	break;
	    default : throw new ParseException(
			"Invalid type in parameters, possibly a bug!");
	}
    }
    return paramArray;
  }


  public void method(List parameters) throws ParseException {

    Result prefix = null;
    try { prefix = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }

    // Here we need to look-up the method ident
    String ident = null;
    
    Method identMethod;
    Class[] paramTypes = makeParameterTypes(parameters);

    if (debugging)
      debug.println(8, "method(" + parameters + ") stack:\n"
		+ printStack() + "Types: " + paramTypes);

    switch (prefix.type) {
	case Result.MEMBER :
	    if (prefix.assignables instanceof String)
		ident = (String) prefix.assignables;
	    else
		throw new ParseException(
			"Trying to resolve method without name!");
	    try {
		identMethod = classCache.forName(prefix.refType).getMethod(
							ident, paramTypes);
		prefix.assignables = null;
		execMethod(identMethod, parameters);
	    }
	    catch (NoSuchMethodException fe) {
		throw new ParseException("Cannot find matching method " + ident
				+ " on class " + prefix.refType + "!");
	    }
	    catch (ClassNotFoundException ce) {
		throw new ParseException("Class " + prefix.refType +
				" not found. Possibly a bug!");
	    }
	    break;
	case Result.UNRESOLVED :
	    ident = prefix.refType;
	    // prefix is incomplete, try local bean's method.
	    try {
		identMethod = classCache.entry(bean.ref.getClass()).getMethod(
							ident, paramTypes);
		prefix.type = bean.type;
		prefix.refType = bean.refType;
		prefix.ref = bean.ref;
		execMethod(identMethod, parameters);
	    }
	    catch (NoSuchMethodException fe) {
		throw new ParseException("Cannot find matching local method "
				+ ident + "!");
	    }
	    break;
	default :
	    throw new ParseException("Primitive types do not contain methods!");
    }
  }

  void execMethod(Method identMethod, List parameters)
	throws ParseException {
    Result prefix = null;
    try { prefix = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    if (Boolean.TRUE.equals(prefix.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        prefix = (Result) prefix.clone();
        opsStack.pop();
        opsStack.push(prefix);
    }
    Class returnType = identMethod.getReturnType();
    Class[] exceptions = identMethod.getExceptionTypes();
    Object[] args = makeParameterArray(parameters);
    try {
	Object result = identMethod.invoke(prefix.ref, args);
	if (returnType.isPrimitive())
	    if (returnType.equals(Void.TYPE)) {
		prefix.type = Result.VOID;
	    }
	    else if (returnType.equals(Integer.TYPE)) {
		prefix.type = Result.INT;
		prefix.intValue = ((Integer) result).intValue();
	    }
	    else if (returnType.equals(Long.TYPE)) {
		prefix.type = Result.LONG;
		prefix.longValue = ((Long) result).longValue();
	    }
	    else if (returnType.equals(Short.TYPE)) {
		prefix.type = Result.SHORT;
		prefix.shortValue = ((Short) result).shortValue();
	    }
	    else if (returnType.equals(Character.TYPE)) {
		prefix.type = Result.CHAR;
		prefix.charValue = ((Character) result).charValue();
	    }
	    else if (returnType.equals(Byte.TYPE)) {
		prefix.type = Result.BYTE;
		prefix.byteValue = ((Byte) result).byteValue();
	    }
	    else if (returnType.equals(Boolean.TYPE)) {
		prefix.type = Result.BOOLEAN;
		prefix.booleanValue = ((Boolean) result).booleanValue();
	    }
	    else if (returnType.equals(Float.TYPE)) {
		prefix.type = Result.FLOAT;
		prefix.floatValue = ((Float) result).floatValue();
	    }
	    else if (returnType.equals(Double.TYPE)) {
		prefix.type = Result.DOUBLE;
		prefix.doubleValue = ((Double) result).doubleValue();
	    }
	    else
		throw new ParseException(
		    "BUG! Detected primitive field with uncovered type!");
	else {
	    prefix.type = Result.OBJECT;
	    prefix.refType = returnType.getName();
	    prefix.ref = result;
   	}
    }
    catch (IllegalAccessException eaccess) {
	throw new ParseException(eaccess.getMessage());
    }
    catch (IllegalArgumentException eargs) {
	throw new ParseException(eargs.getMessage());
    }
    catch (InvocationTargetException einvoke) {
	prefix.type = Result.EXCEPTION;
	prefix.ref = einvoke.getTargetException();
	for (int i = 0; i < exceptions.length; i++)
	    if (exceptions[i].isInstance(prefix.ref)) {
		prefix.refType = exceptions[i].getName();
		break;
	    }
    }
  }

  public void intLiteral(String literal) throws ParseException {
    if (debugging)
      debug.println(8, "Entering intLiteral(" + literal + ")");
    int radix;
    Result r = new Result();
    if (literal.charAt(0) == '0' ) { // hex or octal
	if (literal.length() > 2 &&
	    Character.toLowerCase(literal.charAt(1)) == 'x') { // hex
	    radix = 16;
	    literal = literal.substring(2);
	}
	else { // octal
	    radix = 8;
	    if (literal.length() > 1)
	        literal = literal.substring(1);
	}
    }
    else {// decimal
	radix = 10;
    }
    try {
	if (Character.toLowerCase(literal.charAt(literal.length() - 1))
		== 'l') { // long
	    literal = literal.substring(0, literal.length() -1);
	    r.longValue = Long.parseLong(literal, radix);
	    r.type = Result.LONG;
	}
	else { // int
	    r.intValue = Integer.parseInt(literal, radix);
	    r.type = Result.INT;
	}
    }
    catch (NumberFormatException e) {
	throw new ParseException("Invalid integer, parser bug!");
    }
    opsStack.push(r);
  }

  public void floatLiteral(String literal) throws ParseException {

    Result r = new Result();
    char type = Character.toLowerCase(literal.charAt(literal.length() -1));

    try {
	switch (type) {
	    case 'f'  : literal = literal.substring(0, literal.length() - 1);
			r.floatValue = Float.parseFloat(literal);
			r.type = Result.FLOAT;
			break;
	    case 'd'  : literal = literal.substring(0, literal.length() - 1);
	    default   : r.doubleValue = Double.parseDouble(literal);
			r.type = Result.DOUBLE;
	}
    }
    catch (NumberFormatException e) {
	throw new ParseException("Invalid float/double, parser bug!");
    }
    opsStack.push(r);
  }

  public void charLiteral(String literal) throws ParseException {
    // first take off the single quotes.
    literal = literal.substring(1, literal.length() - 1);
    Result r = new Result();
    r.charValue = parseChar(new StringBuffer(literal));
    r.type = Result.CHAR;
    opsStack.push(r);
  }

  static char parseChar(StringBuffer literal) throws ParseException {
    char ret = '\0';
    char a = literal.charAt(0);
    literal.deleteCharAt(0);
    char b;
    switch (a) {
	case '\\' :
	    b = literal.charAt(0);
	    literal.deleteCharAt(0);
	    switch (b) {
		case 'b'  : ret = '\b'; break;
		case 't'  : ret = '\t'; break;
		case 'n'  : ret = '\n'; break;
		case 'f'  : ret = '\f'; break;
		case '\"' : ret = '\"'; break;
		case '\'' : ret = '\''; break;
		case '\\' : ret = '\\'; break;
		case 'u'  :
		    try {
			ret = (char) Integer.parseInt(
				literal.substring(0,4), 16);
			literal.delete(0,4);
		    }
		    catch (NumberFormatException e) {
			throw new ParseException(
			    "Error parsing unicode literal!");
		    }
		    break;
		case '0'  :
		    int octLimit = 3;
		    int octChars;
		    for (octChars = 0; octChars < octLimit; octChars++) {
			char c = literal.charAt(octChars);
			if (c > '7' || c < '0') {
			    ++octChars;
			    break;
			}
			if (octChars == 0 && c > '3')
			    --octLimit;
		    }
		    try {
			ret = (char) Integer.parseInt(
				literal.substring(0, octChars), 8);
			literal.delete(0, octChars);
		    }
		    catch (NumberFormatException e) {
			throw new ParseException(
			    "Error parsing octal literal!");
		    }
		default   : ret = b;
	    }
	    break;
	case '\'' : throw new ParseException(
		    "Character literal cannot start with \"'\"!");
	default	  : ret = a;
    }
    return ret;
  }

  public void stringLiteral(String literal) throws ParseException {
    Result r = new Result();
    r.type = Result.OBJECT;
    r.refType = "java.lang.String";

    // take off the quotes
    literal = literal.substring(1, literal.length() - 1);

    int offset = literal.indexOf('\\');
    ++offset;

    if (offset > 0) {
	StringBuffer inp = new StringBuffer(literal);
	StringBuffer outp = new StringBuffer(literal.length());
	do {
	    outp.append(inp.substring(0, offset));
	    inp.delete(0, offset);
	    outp.append(parseChar(inp));
	    offset = inp.toString().indexOf('\\');
	    ++offset;
	}
	while (offset > 0);
	outp.append(inp);
	r.ref = outp.toString();
    }
    else {
	r.ref = literal;
    }
    opsStack.push(r);
  }

  public void newPrimitiveArray(byte type, List lst)
	throws ParseException {
    if (type > Result.DOUBLE)
	throw new ParseException("Invalid primitive type for array, bug!");
    if (!(lst instanceof LinkedList))
	throw new ParseException("Dimension list not a LinkedList, bug!");
    LinkedList dims = (LinkedList) lst;
    Result r = new Result();
    int size = dims.size();

    char[] typeBuf = new char[size + 1];
    for (int i = 0; i < size; typeBuf[i++] = '[');
    typeBuf[size] = Result.typeMap[type];
    r.type = Result.OBJECT;
    r.refType = new String(typeBuf);
    r.ref = allocPrimitiveArray(type, dims);
    opsStack.push(r);
  }

  static Object allocPrimitiveArray(byte type, LinkedList dims)
	throws ParseException {
    Object array = null;
    int size;
    switch (dims.size()) {
	case 0	:
	    throw new ParseException(
		"Array allocation with dimensions empty, bug");
	case 1	:
	    size = ((Integer)dims.remove(0)).intValue();
	    if (size == 0)
		break;
	    switch (type) {
		case Result.DOUBLE  : array = new double[size];	 break;
		case Result.FLOAT   : array = new float[size];	 break;
		case Result.LONG    : array = new long[size];	 break;
		case Result.INT     : array = new int[size];	 break;
		case Result.SHORT   : array = new short[size];	 break;
		case Result.CHAR    : array = new char[size];	 break;
		case Result.BYTE    : array = new byte[size];	 break;
		case Result.BOOLEAN : array = new boolean[size]; break;
		default		    :
		    throw new ParseException("Invalid primitive type, bug!");
	    }
	    break;
	default	: 
	    size = ((Integer)dims.remove(0)).intValue();
	    if (size == 0) 
		break;
	    Object[] o = new Object[size];
	    for (int i = 0; i < size - 1; i++)
		o[i] = allocPrimitiveArray(type, (LinkedList) dims.clone());
	    o[size - 1] = allocPrimitiveArray(type, dims);
	    array = o;
    }
    return array;
  }

  public void newObject(String name, List parameters) throws ParseException {
    Result r = new Result();
    r.type = Result.OBJECT;
    r.refType = name;
    Class[] exceptions = null;
    try {
	Constructor constr = ((CachedClass) findClass(name).ref).getConstructor(
				makeParameterTypes(parameters));
    	exceptions = constr.getExceptionTypes();
	r.ref = constr.newInstance(parameters.toArray());
    }
    catch (ClassNotFoundException ce) {
	throw new ParseException("Class " + r.refType +
				" not found!");
    }
    catch (NoSuchMethodException me) {
	throw new ParseException("Cannot find matching constructor on class "
				 + r.refType + "!");
    }
    catch (InstantiationException ie) {
	throw new ParseException("Cannot instantiate " + r.refType
			+ ". Class might be abstract or an interface!");
    }
    catch (IllegalAccessException ae) {
	throw new ParseException(ae.getMessage());
    }
    catch (IllegalArgumentException xe) {
	throw new ParseException(xe.getMessage());
    }
    catch (InvocationTargetException te) {
	r.type = Result.EXCEPTION;
	r.ref = te.getTargetException();
	for (int i = 0; i < exceptions.length; i++)
	    if (exceptions[i].isInstance(r.ref)) {
		r.refType = exceptions[i].getName();
		break;
	    }
    }
    opsStack.push(r);
  }

  public void newObjectArray(String name, List lst)
	throws ParseException {
    if (!(lst instanceof LinkedList))
	throw new ParseException("Dimension list not a LinkedList, bug!");
    Result classRef = null;
    try {
	classRef = findClass(name);
    }
    catch (ClassNotFoundException e) {
	throw new ParseException(e.getMessage());
    }
    LinkedList dims = (LinkedList) lst;
    Result r = new Result();
    int size = dims.size();

    char[] typeBuf = new char[size + 1];
    for (int i = 0; i < size; typeBuf[i++] = '[');
    typeBuf[size] = 'L';
    r.type = Result.OBJECT;
    r.refType = new String(typeBuf) + classRef.refType + ";";
    r.ref = allocObjectArray(dims);
    opsStack.push(r);
  }

  static Object allocObjectArray(LinkedList dims) throws ParseException {
    Object array = null;
    int dimensions = dims.size();
    if (dimensions > 0) {
	int size;
	if ((size = ((Integer)dims.remove(0)).intValue()) > 0) {
	    Object[] o = new Object[size];
	    if (dimensions > 1) {
		for (int i = 0; i < size - 1; i++)
		    o[i] = allocObjectArray((LinkedList) dims.clone());
		o[size - 1] = allocObjectArray(dims);
	    }
	    array = o;
	}
    }
    else
	throw new ParseException(
	    "Array allocation with dimensions empty, bug");
    return array;
  }

  public static void addDimension(int dims, LinkedList dimList) {
    Integer iDims = new Integer(dims);
    dimList.add(iDims);
  }

  public void addDimension(LinkedList dimList)
	throws ParseException {
    Result dims = null;
    try {
	dims = (Result) opsStack.pop();
    }
    catch (EmptyStackException e) {
	throw new ParseException("Empty stack!");
    }
    int iDims;
    switch (dims.type) {
	case Result.INT   : iDims = dims.intValue;   break;
	case Result.SHORT : iDims = dims.shortValue; break;
	case Result.CHAR  : iDims = dims.charValue;  break;
	case Result.BYTE  : iDims = dims.byteValue;  break;
	default		  : throw new ParseException
    ("Array Dimensions must be an integral expression no bigger than int!");
    }
    addDimension(iDims, dimList);
  }

  public void conditional() throws ParseException {
    Result ex1 = null , ex2 = null, ex3 = null;
    try {
	ex3 = (Result) opsStack.pop();
	ex2 = (Result) opsStack.pop();
    }
    catch (EmptyStackException e) {
        throw new ParseException("Null expression!");
    }
    try { ex1 = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    if (Boolean.TRUE.equals(ex1.assignables)) {
        // we won't touch variables in this method,
        // so we need to replace the stack-top with a clone().
        ex1 = (Result) ex1.clone();
        opsStack.pop();
        opsStack.push(ex1);
    }
    ex1.assignables = null;

    if (ex1.type != Result.BOOLEAN)
	throw new ParseException(
	    "First expression in conditional must be boolean!");
    if (Boolean.TRUE.equals(ex1.assignables))
	ex1 = (Result) ex1.clone();
    ex1.assignables = null;
    switch (ex2.type) {
	case Result.BOOLEAN 	:
	    switch (ex3.type) {
		case Result.BOOLEAN 	:
		    ex1.booleanValue = ex1.booleanValue ?
			ex2.booleanValue : ex3.booleanValue;
			break;
		default 		:
		    throw new ParseException(
			"Third expression in conditional must be boolean!");
	    }
	    break;
	case Result.BYTE 	:
	    switch (ex3.type) {
		case Result.BYTE 	:
		    ex1.byteValue = ex1.booleanValue ?
			ex2.byteValue : ex3.byteValue;
		    ex1.type = Result.BYTE;
		    break;
		case Result.CHAR	:
		    ex1.intValue = ex1.booleanValue ?
			ex2.byteValue : ex3.charValue;
		    ex1.type = Result.INT;
		    break;
		case Result.SHORT	:
		    ex1.intValue = ex1.booleanValue ?
			ex2.byteValue : ex3.shortValue;
		    ex1.type = Result.INT;
		    break;
		case Result.INT		:
		    ex1.intValue = ex1.booleanValue ?
			ex2.byteValue : ex3.intValue;
		    if (ex1.intValue >= Byte.MIN_VALUE &&
			ex1.intValue <= Byte.MAX_VALUE) {
			ex1.byteValue = (byte) ex1.intValue;
			ex1.type = Result.BYTE;
		    }
		    else
			ex1.type = Result.INT;
		    break;
		case Result.LONG	:
		    ex1.longValue = ex1.booleanValue ?
			ex2.byteValue : ex3.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.FLOAT	:
		    ex1.floatValue = ex1.booleanValue ?
			ex2.byteValue : ex3.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.DOUBLE	:
		    ex1.doubleValue = ex1.booleanValue ?
			ex2.byteValue : ex3.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		default 		:
		    throw new ParseException(
			"Third expression in conditional must be numeric!");
	    }
	    break;
	case Result.CHAR	:
	    switch (ex3.type) {
		case Result.BYTE 	:
		    ex1.intValue = ex1.booleanValue ?
			ex2.charValue : ex3.byteValue;
		    ex1.type = Result.INT;
		    break;
		case Result.CHAR	:
		    ex1.charValue = ex1.booleanValue ?
			ex2.charValue : ex3.charValue;
		    ex1.type = Result.CHAR;
		    break;
		case Result.SHORT	:
		    ex1.intValue = ex1.booleanValue ?
			ex2.charValue : ex3.shortValue;
		    ex1.type = Result.INT;
		    break;
		case Result.INT		:
		    ex1.intValue = ex1.booleanValue ?
			ex2.charValue : ex3.intValue;
		    if (ex1.intValue >= Character.MIN_VALUE &&
			ex1.intValue <= Character.MAX_VALUE) {
			ex1.charValue = (char) ex1.intValue;
			ex1.type = Result.CHAR;
		    }
		    else
			ex1.type = Result.INT;
		    break;
		case Result.LONG	:
		    ex1.longValue = ex1.booleanValue ?
			ex2.charValue : ex3.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.FLOAT	:
		    ex1.floatValue = ex1.booleanValue ?
			ex2.charValue : ex3.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.DOUBLE	:
		    ex1.doubleValue = ex1.booleanValue ?
			ex2.charValue : ex3.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		default 		:
		    throw new ParseException(
			"Third expression in conditional must be numeric!");
	    }
	    break;
	case Result.SHORT	:
	    switch (ex3.type) {
		case Result.BYTE 	:
		    ex1.intValue = ex1.booleanValue ?
			ex2.shortValue : ex3.byteValue;
		    ex1.type = Result.INT;
		    break;
		case Result.CHAR	:
		    ex1.intValue = ex1.booleanValue ?
			ex2.shortValue : ex3.charValue;
		    ex1.type = Result.INT;
		    break;
		case Result.SHORT	:
		    ex1.shortValue = ex1.booleanValue ?
			ex2.shortValue : ex3.shortValue;
		    ex1.type = Result.SHORT;
		    break;
		case Result.INT		:
		    ex1.intValue = ex1.booleanValue ?
			ex2.shortValue : ex3.intValue;
		    if (ex1.intValue >= Short.MIN_VALUE &&
			ex1.intValue <= Short.MAX_VALUE) {
			ex1.shortValue = (short) ex1.intValue;
			ex1.type = Result.SHORT;
		    }
		    else
			ex1.type = Result.INT;
		    break;
		case Result.LONG	:
		    ex1.longValue = ex1.booleanValue ?
			ex2.shortValue : ex3.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.FLOAT	:
		    ex1.floatValue = ex1.booleanValue ?
			ex2.shortValue : ex3.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.DOUBLE	:
		    ex1.doubleValue = ex1.booleanValue ?
			ex2.shortValue : ex3.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		default 		:
		    throw new ParseException(
			"Third expression in conditional must be numeric!");
	    }
	    break;
	case Result.INT		:
	    switch (ex3.type) {
		case Result.BYTE 	:
		    ex1.intValue = ex1.booleanValue ?
			ex2.intValue : ex3.byteValue;
		    if (ex1.intValue >= Byte.MIN_VALUE &&
			ex1.intValue <= Byte.MAX_VALUE) {
			ex1.byteValue = (byte) ex1.intValue;
			ex1.type = Result.BYTE;
		    }
		    else
			ex1.type = Result.INT;
		    break;
		case Result.CHAR	:
		    ex1.intValue = ex1.booleanValue ?
			ex2.intValue : ex3.charValue;
		    if (ex1.intValue >= Character.MIN_VALUE &&
			ex1.intValue <= Character.MAX_VALUE) {
			ex1.charValue = (char) ex1.intValue;
			ex1.type = Result.CHAR;
		    }
		    else
			ex1.type = Result.INT;
		    break;
		case Result.SHORT	:
		    ex1.intValue = ex1.booleanValue ?
			ex2.intValue : ex3.shortValue;
		    if (ex1.intValue >= Short.MIN_VALUE &&
			ex1.intValue <= Short.MAX_VALUE) {
			ex1.shortValue = (short) ex1.intValue;
			ex1.type = Result.SHORT;
		    }
		    else
			ex1.type = Result.INT;
		    break;
		case Result.INT		:
		    ex1.intValue = ex1.booleanValue ?
			ex2.intValue : ex3.intValue;
		    ex1.type = Result.INT;
		    break;
		case Result.LONG	:
		    ex1.longValue = ex1.booleanValue ?
			ex2.intValue : ex3.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.FLOAT	:
		    ex1.floatValue = ex1.booleanValue ?
			ex2.intValue : ex3.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.DOUBLE	:
		    ex1.doubleValue = ex1.booleanValue ?
			ex2.intValue : ex3.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		default 		:
		    throw new ParseException(
			"Third expression in conditional must be numeric!");
	    }
	    break;
	case Result.LONG	:
	    switch (ex3.type) {
		case Result.BYTE 	:
		    ex1.longValue = ex1.booleanValue ?
			ex2.longValue : ex3.byteValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.CHAR	:
		    ex1.longValue = ex1.booleanValue ?
			ex2.longValue : ex3.charValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.SHORT	:
		    ex1.longValue = ex1.booleanValue ?
			ex2.longValue : ex3.shortValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.INT		:
		    ex1.longValue = ex1.booleanValue ?
			ex2.longValue : ex3.intValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.LONG	:
		    ex1.longValue = ex1.booleanValue ?
			ex2.longValue : ex3.longValue;
		    ex1.type = Result.LONG;
		    break;
		case Result.FLOAT	:
		    ex1.floatValue = ex1.booleanValue ?
			ex2.longValue : ex3.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.DOUBLE	:
		    ex1.doubleValue = ex1.booleanValue ?
			ex2.longValue : ex3.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		default 		:
		    throw new ParseException(
			"Third expression in conditional must be numeric!");
	    }
	    break;
	case Result.FLOAT	:
	    switch (ex3.type) {
		case Result.BYTE 	:
		    ex1.floatValue = ex1.booleanValue ?
			ex2.floatValue : ex3.byteValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.CHAR	:
		    ex1.floatValue = ex1.booleanValue ?
			ex2.floatValue : ex3.charValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.SHORT	:
		    ex1.floatValue = ex1.booleanValue ?
			ex2.floatValue : ex3.shortValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.INT		:
		    ex1.floatValue = ex1.booleanValue ?
			ex2.floatValue : ex3.intValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.LONG	:
		    ex1.floatValue = ex1.booleanValue ?
			ex2.floatValue : ex3.longValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.FLOAT	:
		    ex1.floatValue = ex1.booleanValue ?
			ex2.floatValue : ex3.floatValue;
		    ex1.type = Result.FLOAT;
		    break;
		case Result.DOUBLE	:
		    ex1.doubleValue = ex1.booleanValue ?
			ex2.floatValue : ex3.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		default 		:
		    throw new ParseException(
			"Third expression in conditional must be numeric!");
	    }
	    break;
	case Result.DOUBLE	:
	    switch (ex3.type) {
		case Result.BYTE 	:
		    ex1.doubleValue = ex1.booleanValue ?
			ex2.doubleValue : ex3.byteValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.CHAR	:
		    ex1.doubleValue = ex1.booleanValue ?
			ex2.doubleValue : ex3.charValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.SHORT	:
		    ex1.doubleValue = ex1.booleanValue ?
			ex2.doubleValue : ex3.shortValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.INT		:
		    ex1.doubleValue = ex1.booleanValue ?
			ex2.doubleValue : ex3.intValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.LONG	:
		    ex1.doubleValue = ex1.booleanValue ?
			ex2.doubleValue : ex3.longValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.FLOAT	:
		    ex1.doubleValue = ex1.booleanValue ?
			ex2.doubleValue : ex3.floatValue;
		    ex1.type = Result.DOUBLE;
		    break;
		case Result.DOUBLE	:
		    ex1.doubleValue = ex1.booleanValue ?
			ex2.doubleValue : ex3.doubleValue;
		    ex1.type = Result.DOUBLE;
		    break;
		default 		:
		    throw new ParseException(
			"Third expression in conditional must be numeric!");
	    }
	    break;
	case Result.OBJECT	:
	    switch (ex3.type) {
		case Result.OBJECT	:
		    try {
			if (ex2.refType.equals(ex3.refType) ||
			    ex3.refType.equals("null") ||
		    	    classCache.forName(ex2.refType).isInstance(ex3))
			    ex1.refType = ex2.refType;
			else if (ex2.refType.equals("null") ||
			    classCache.forName(ex3.refType
				).isInstance(ex2))
			    ex1.refType = ex3.refType;
			else
			    throw new ParseException(
				"Second and third expression in conditional" + 
				"must be assignment compatible!");
		    }
		    catch (ClassNotFoundException e) {
			throw new ParseException(
			"Class not found for existing object, possibly a bug! "
				 + e.getMessage());
		    }
		    ex1.ref = ex1.booleanValue ? ex2.ref : ex3.ref;
		    ex1.type = Result.OBJECT;
		    break;
		default 		:
		    throw new ParseException(
		"Third expression in conditional must be an object reference!");
	    }
	    break;
	case Result.VOID	:
	    throw new ParseException(
		"No expression in conditional can be void!");
	default 		:
	    throw new ParseException(
		"Unknown type for second expression in conditional!");
    }
    ex1.assignables = null;
  }

  public void importClass(String name, boolean pkg) throws ParseException {
    if (pkg)
	pkgSet.add(name);
    else {
	int idx = name.lastIndexOf('.');
	if (idx != -1) {
	    try {
		Result r = new Result();
		r.ref = classCache.forName(name);
		r.type = Result.CLASS;
		r.refType = name;
		pkgMap.put(name.substring(++idx), r);
	    }
	    catch (ClassNotFoundException e) {
		throw new ParseException("Class " + name + " not found!");
	    }
	}
	else
	    throw new ParseException(name + " is not a valid classname!");
    }
  }

  Result findClass(String name)
	throws ClassNotFoundException {
    Result r = null;
    if (name.indexOf('.') != -1) {
        CachedClass c = classCache.forName(name);
	r = new Result();
	r.ref = c;
	r.type = Result.CLASS;
	r.refType = name;
    }
    else {
	r = (Result) pkgMap.get(name);

	if (r != null)
	    r = (Result) r.clone();
	
	else  {
	    String pkgName = null;
	    r = new Result();
	    for (Iterator i = pkgSet.iterator(); i.hasNext();) {
		pkgName = (String) i.next();
		pkgName += '.' + name;
		try {
		    r.ref = classCache.forName(pkgName);
		    r.type = Result.CLASS;
		    r.refType = pkgName;
		    pkgMap.put(name, r.clone());
		    break;
		}
		catch (ClassNotFoundException e) {
		    continue;
		}
	    }
	    if (r.ref == null)
		throw new ClassNotFoundException("Cannot find " + name +
						 " in any package!");
	}
    }
    return r;
  }

  public void pushType(byte primType, String className, int dims)
	throws ParseException {
    Result r = null;
    if (dims == 0) {
	if (className == null) {
	    r = new Result();
	    r.type = primType;
	}
	else {
	    try {
		r = findClass(className);
	    }
	    catch (ClassNotFoundException e) {
		throw new ParseException(e.getMessage());
	    }
	}
    }
    else {
	StringBuffer buf = new StringBuffer();
	for (int i = 0; i < dims; buf.append('['), i++);
	if (className == null) {
	    r = new Result();
	    buf.append(Result.typeMap[primType]);
	    r.type = Result.CLASS;
	    r.refType = buf.toString();
	    r.ref = null;
	}
	else if (className.charAt(0) == '[')
	    throw new ParseException("Reference type already an array!");
	else
	    try {
		r = findClass(className);
		buf.append(r.refType);
		buf.append(';');
		r.refType = buf.toString();
		r.ref = null;
	    }
	    catch (ClassNotFoundException e) {
		throw new ParseException(e.getMessage());
	    }
    }
    opsStack.push(r);
  }

  public void firstIdentifier(String name) throws ParseException {

    if (debugging)
      debug.println(8, "Entering firstIdentifier(" + name + ") stack:\n"
			+ printStack());

    Result r;

    // 1. Local variable?
    r = (Result) symbolMap.get(name);

    // 2. Beans variable?
    if (r == null)
	try {
	    Field identField = classCache.entry(bean.ref.getClass()
                               ).getField(name);
	    r = assignFromField((Result) bean.clone(), identField);
	} 
	catch (NoSuchFieldException fe) {}
    
    // 3. Local class name (same package)?
    if (r == null)
	try {
	    CachedClass c = classCache.forName(name);
	    r = new Result();
	    r.ref = c;
	    r.type = Result.CLASS;
	    r.refType = name;
	}
	catch (ClassNotFoundException ce) {}

    // 4. Imported class name?
    if (r == null) {

	// already known to pkgMap?
	r = (Result) pkgMap.get(name);
	if (r != null)
	    r = (Result) r.clone();

	// unknown to pkgmap, search pkgSet.
	else {
	    String pkgName = null;
            for (Iterator i = pkgSet.iterator(); i.hasNext();
                 pkgName = (String) i.next()) {
                pkgName += '.' + name;
                try {
                    CachedClass c = classCache.forName(pkgName);
		    r = new Result();
		    r.ref = c;
                    r.type = Result.CLASS;
                    r.refType = pkgName;
                    pkgMap.put(name, r.clone());
                    break;
                }
                catch (ClassNotFoundException e) {
                    continue;
                }
            }
	}
    }

    // 5. Unresolved (part of full class name or bean method)?
    if (r == null) {
	r = new Result();
	r.type = Result.UNRESOLVED;
	r.refType = name;
    }

    opsStack.push(r);
  }

  Result peekNonlocal() throws ParseException {
    Result r = null;
    try {
	r = (Result) opsStack.peek();
    }
    catch (EmptyStackException e) {
	throw new ParseException("Stack empty!");
    }
    if (Boolean.TRUE.equals(r.assignables)) {
	r = (Result) r.clone();
	opsStack.setElementAt(r, opsStack.size() - 1);
    }
    return r;
  }

  public void prefixIdentifier(String name) throws ParseException {

    if (debugging)
      debug.println(8, "Entering prefixIdentifier(" + name + ") stack:\n"
			+ printStack());

    Result first = peekNonlocal();

    switch (first.type) {

	// 1. first = object
	case Result.OBJECT :
	
	    //	- field
	    try {
		Field identField = classCache.forName(first.refType).getField(name);
		first = assignFromField(first, identField);
	    }
	    catch (NoSuchFieldException fe) {

	    //	- unresolved member (potential method) ?
		first.type = Result.MEMBER;
		first.assignables = name;
	    }
	    catch (ClassNotFoundException ce) {
		throw new ParseException("Class " + first.refType +
					" not found. Possibly a bug!");
	    }
	    break;

	// 2. first = class
	case Result.CLASS :

	    //	- static field?
	    try {
		Field identField = ((CachedClass) first.ref).getField(name);
		first = assignFromField(first, identField);
	    }
	    catch (NoSuchFieldException fe) {

	    //	- unresolved member (potential method) ? 
		first.type = Result.MEMBER;
		first.assignables = name;
	    }
	    break;

	// 3. first = unresolved
	case Result.UNRESOLVED :
	
	    //	- full class name?
	    first.refType += '.' + name;
	    try {
		CachedClass c = classCache.forName(first.refType);
		first.type = Result.CLASS;
		first.ref = c;
	    }
	    catch (ClassNotFoundException ce) {
	    //	- package name, leave unresolved
	    }
	    break;

	// 4. first = primitive, invalid!
	case Result.BOOLEAN :
	case Result.BYTE    :
	case Result.CHAR    :
	case Result.SHORT   :
	case Result.INT     :
	case Result.LONG    :
	case Result.FLOAT   :
	case Result.DOUBLE  :
	    throw new ParseException("Invalid member for primitive type!");

	// 5. first = unresolved member, invalid!
	case Result.MEMBER  :
	    throw new ParseException("Cannot find member " + first.refType
		+ '.' + (String) first.assignables + '.' + name + " !");
	default :
	    throw new ParseException("Invalid type!");
    }
  }

  public void suffixIdentifier(String name) throws ParseException {

    Result prefix = peekNonlocal();

    switch (prefix.type) {

	// 1. prefix = object
	case Result.OBJECT :
	
	    //	- field ?
	    try {
		Field identField = classCache.forName(prefix.refType).getField(name);
		prefix = assignFromField(prefix, identField);
	    }
	    catch (NoSuchFieldException fe) {

	    //	- unresolved member (potential method) ?
		prefix.type = Result.MEMBER;
		prefix.assignables = name;
	    }
	    catch (ClassNotFoundException ce) {
		throw new ParseException("Class " + prefix.refType +
					" not found. Possibly a bug!");
	    }
	    break;

	// 2. prefix = primitive, invalid!
	case Result.BOOLEAN :
	case Result.BYTE    :
	case Result.CHAR    :
	case Result.SHORT   :
	case Result.INT     :
	case Result.LONG    :
	case Result.FLOAT   :
	case Result.DOUBLE  :
	    throw new ParseException("Invalid member for primitive type!");

	// 3. prefix = unresolved member, invalid!
	case Result.MEMBER  :
	    throw new ParseException("Cannot find member " + prefix.refType
		+ '.' + (String) prefix.assignables + '.' + name + " !");
	default :
	    throw new ParseException("Invalid type!");
    }
  }

  public void evalName(String name) throws ParseException {
    if (debugging)
      debug.println(8, "Entering evalName(" + name + ")");

    StringTokenizer tk = new StringTokenizer(name, ".");
    firstIdentifier(tk.nextToken());
    while (tk.hasMoreTokens())
	prefixIdentifier(tk.nextToken());
  }

  public void pushBean() {
    opsStack.push(bean.clone());
  }

  public void localVar(Result type, String name, int arrayCount)
	throws ParseException {

    if (debugging)
      debug.println(8, "localVar(" + type + ", " + name + ", "
                    + arrayCount + "), stack:\n" + printStack());

    pushType(type.type, type.refType, arrayCount);

    try { type = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("First expression is null!");
    }
    switch (type.type) {
	case Result.BOOLEAN : type.booleanValue = false; break;
	case Result.BYTE    : type.byteValue ^= type.byteValue; break; 
	case Result.CHAR    : type.charValue ^= type.charValue; break; 
	case Result.SHORT   : type.shortValue ^= type.shortValue; break; 
	case Result.INT     : type.intValue ^= type.intValue; break; 
	case Result.LONG    : type.longValue ^= type.longValue; break; 
	case Result.FLOAT   : type.floatValue = 0; break; 
	case Result.DOUBLE  : type.doubleValue = 0; break; 
	case Result.CLASS   : type.type = Result.OBJECT;
			      type.ref = null; break;
	default : throw new ParseException(
	"Bug! Type must be a class or primitive, not an object reference!");
    }
    type.assignables = Boolean.TRUE;
    symbolMap.put(name, type);
  }

  // Need to check assignment order in stack
  public void assignValue() throws ParseException {
    Result var = null, ex = null;
    try { ex = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Expression is null!");
    }
    try { var = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("Variable is null!");
    }
    if (var.assignables == null)
	throw new ParseException("No variable to assign to!");
    switch (var.type) {
	case Result.BOOLEAN :
	    if (ex.type != Result.BOOLEAN) {
		throw new ParseException(
		"Incompatible assignment, explicit cast needed!");
	    }
	    else
		var.booleanValue = ex.booleanValue;
	    break;
	case Result.BYTE :
	    if (ex.type != Result.BYTE) {
		throw new ParseException(
		"Incompatible assignment, explicit cast needed!");
	    }
	    else
		var.byteValue = ex.byteValue;
	    break;
	case Result.CHAR :
	    if (ex.type != Result.CHAR) {
		throw new ParseException(
		"Incompatible assignment, explicit cast needed!");
	    }
	    else
		var.charValue = ex.charValue;
	    break;
	case Result.SHORT :
	    switch (ex.type) {
		case Result.BYTE :
		    var.shortValue = ex.byteValue;
		    break;
		case Result.SHORT :
		    var.shortValue = ex.shortValue;
		    break;
		default :
		    throw new ParseException(
		  "Incompatible assignment, explicit cast needed!");
	    }
	    break;
	case Result.INT : 
	    switch (ex.type) {
		case Result.BYTE :
		    var.intValue = ex.byteValue;
		    break;
		case Result.CHAR :
		    var.intValue = ex.charValue;
		    break;
		case Result.SHORT :
		    var.intValue = ex.shortValue;
		    break;
		case Result.INT :
		    var.intValue = ex.intValue;
		    break;
		default :
		    throw new ParseException(
		  "Incompatible assignment, explicit cast needed!");
	    }
	    break;
	case Result.LONG :
	    switch (ex.type) {
		case Result.BYTE :
		    var.longValue = ex.byteValue;
		    break;
		case Result.CHAR :
		    var.longValue = ex.charValue;
		    break;
		case Result.SHORT :
		    var.longValue = ex.shortValue;
		    break;
		case Result.INT :
		    var.longValue = ex.intValue;
		    break;
		case Result.LONG :
		    var.longValue = ex.longValue;
		    break;
		default :
		    throw new ParseException(
		  "Incompatible assignment, explicit cast needed!");
	    }
	    break;
	case Result.FLOAT :
	    switch (ex.type) {
		case Result.BYTE :
		    var.floatValue = ex.byteValue;
		    break;
		case Result.CHAR :
		    var.floatValue = ex.charValue;
		    break;
		case Result.SHORT :
		    var.floatValue = ex.shortValue;
		    break;
		case Result.INT :
		    var.floatValue = ex.intValue;
		    break;
		case Result.LONG :
		    var.floatValue = ex.longValue;
		    break;
		case Result.FLOAT :
		    var.floatValue = ex.floatValue;
		    break;
		default :
		    throw new ParseException(
		  "Incompatible assignment, explicit cast needed!");
	    }
	    break;
	case Result.DOUBLE :
	    switch (ex.type) {
		case Result.BYTE :
		    var.doubleValue = ex.byteValue;
		    break;
		case Result.CHAR :
		    var.doubleValue = ex.charValue;
		    break;
		case Result.SHORT :
		    var.doubleValue = ex.shortValue;
		    break;
		case Result.INT :
		    var.doubleValue = ex.intValue;
		    break;
		case Result.LONG :
		    var.doubleValue = ex.longValue;
		    break;
		case Result.FLOAT :
		    var.doubleValue = ex.floatValue;
		    break;
		case Result.DOUBLE :
		    var.doubleValue = ex.doubleValue;
		    break;
		default :
		    throw new ParseException(
		  "Incompatible assignment, explicit cast needed!");
	    }
	    break;
	case Result.OBJECT :
	    CachedClass c = null;
	    try {
		c = classCache.forName(var.refType);
	    }
	    catch (ClassNotFoundException e) {
		    throw new ParseException("Bug! Class " +
			var.refType + " not found!");
	    }
	    if (ex.type == Result.OBJECT && c.isInstance(ex))
		var.ref = ex.ref;
	    else
		throw new ParseException(
		  "Incompatible assignment, explicit cast needed!");
	    break;
	default :
		throw new ParseException("Bug! invalid variable type!");
    }
    setVariable(var);
  }

  public static void setVariable(Result ex)
	throws ParseException {
    if (ex.assignables == null)
	throw new ParseException("Expression does not represent a variable!");
    // if we're referring to a local variable, it's already assigned.
    if (Boolean.TRUE.equals(ex.assignables))
	return;
    Object[] oa = (Object []) ex.assignables;

    // The var to set can be both an array member or a field.
    if (oa[1] instanceof Integer) {// an array index.
	int index = ((Integer) oa[1]).intValue();
	switch (ex.type) {
	    case Result.BOOLEAN : boolean[] z = (boolean[]) oa[0];
				  z[index] = ex.booleanValue;
				  break;
	    case Result.BYTE    : byte[] b = (byte[]) oa[0];
				  b[index] = ex.byteValue;
				  break;
	    case Result.CHAR	: char[] c = (char[]) oa[0];
				  c[index] = ex.charValue;
				  break;
	    case Result.SHORT	: short[] s = (short[]) oa[0];
				  s[index] = ex.shortValue;
				  break;
	    case Result.INT	: int[] i = (int[]) oa[0];
				  i[index] = ex.intValue;
				  break;
	    case Result.LONG	: long[] j = (long[]) oa[0];
				  j[index] = ex.longValue;
				  break;
	    case Result.FLOAT	: float[] f = (float[]) oa[0];
				  f[index] = ex.floatValue;
				  break;
	    case Result.DOUBLE	: double[] d = (double[]) oa[0];
				  d[index] = ex.doubleValue;
				  break;
	    case Result.OBJECT	: Object[] o = (Object[]) oa[0];
				  o[index] = ex.ref;
				  break;
	    default	: throw new ParseException("Invalid object type, BUG!");
	}
    }
    else if (oa[1] instanceof Field) { // a field
        Object o = oa[0];
	Field f = (Field) oa[1];
	try {
	    switch (ex.type) {
		case Result.BOOLEAN : f.setBoolean(o, ex.booleanValue); break;
		case Result.BYTE	: f.setByte(o, ex.byteValue); break;
		case Result.CHAR	: f.setChar(o, ex.charValue); break;
		case Result.SHORT	: f.setShort(o, ex.shortValue); break;
		case Result.INT	: f.setInt(o, ex.intValue); break;
		case Result.LONG	: f.setLong(o, ex.longValue); break;
		case Result.FLOAT	: f.setFloat(o, ex.floatValue); break;
		case Result.DOUBLE	: f.setDouble(o, ex.doubleValue); break;
		case Result.OBJECT	: f.set(o, ex.ref); break;
		default	: throw new ParseException("Invalid object type, BUG!");
	    }
	}
	catch (IllegalAccessException e) {
	    throw new ParseException("Access to " + f.getName()
		+ " is illegal!");
	}
    }
    else
	throw new ParseException("Variable type not found!");
  }

  public void assignOps(char op) throws ParseException {
    Result var = null, ex = null;
    try { ex = (Result) opsStack.pop(); }
    catch (EmptyStackException e) {
        throw new ParseException("Expression is null!");
    }
    try { var = (Result) opsStack.peek(); }
    catch (EmptyStackException e) {
        throw new ParseException("Variable is null!");
    }
    if (op != '=')
	opsStack.push(var.clone());

    switch (op) {
	case '=' : break;
	case '*' : multiply();		break;
	case '/' : divide();		break;
	case '%' : remainder();		break;
	case '+' : add();		break;
	case '-' : subtract();		break;
	case '<' : leftShift();		break;
	case '>' : rightShift();	break;
	case '@' : unsignedRightShift();break;
	case '&' : and();		break;
	case '^' : exclusiveOr();	break;
	case '|' : inclusiveOr();	break;
	default  : throw new ParseException("Not an assignment operation!");
    }
    assignValue();
  }

  public void booleanTrue() {
    Result r = new Result();
    r.type = Result.BOOLEAN;
    r.booleanValue = true;
    opsStack.push(r);
  }

  public void booleanFalse() {
    Result r = new Result();
    r.type = Result.BOOLEAN;
    r.booleanValue = false;
    opsStack.push(r);
  }

  public void nullLiteral() {
    Result r = new Result();
    r.type = Result.OBJECT;
    r.refType = "null";
    r.ref = null;
    opsStack.push(r);
  }

  public boolean isTrue() throws ParseException {
    boolean t = false;
    try {
	Result r = null;
	r = (Result) opsStack.peek();
	if (r.type == Result.BOOLEAN)
	    t = r.booleanValue;
	else
	    throw new ParseException("Expression not boolean!");
    }
    catch (EmptyStackException e) {
	throw new ParseException("Stack empty, cannot test!");
    }
    return t;
  }

  public void addParams(List params) throws ParseException {
    Result ex = null;
    try {
	params.add(opsStack.pop());
    }
    catch (EmptyStackException e) {
	throw new ParseException("Empty stack!");
    }
  }

  public Result popResult() throws ParseException {
    Result r = null;
    try {
	r = (Result) opsStack.pop();
    }
    catch (EmptyStackException e) {
	throw new ParseException("Empty stack!");
    }
    return r;
  }
}
