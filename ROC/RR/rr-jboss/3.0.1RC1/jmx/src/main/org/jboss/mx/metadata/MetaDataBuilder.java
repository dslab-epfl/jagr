/*
 * LGPL
 */
package org.jboss.mx.metadata;

import javax.management.MBeanInfo;
import javax.management.NotCompliantMBeanException;

public interface MetaDataBuilder
{

   public MBeanInfo build() throws NotCompliantMBeanException;

}

