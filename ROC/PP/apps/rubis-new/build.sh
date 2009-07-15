#!/bin/sh


########################

export JBOSS_HOME=$ROC_TOP/PP/pp-jboss-3.2.1/build/output/jboss-3.2.1/
export JBOSS_DIST=$JBOSS_HOME
export J2EE_HOME=$ROC_TOP/PP/apps/rubis-new/j2sdkee1.3.1
export ANT_HOME=$ROC_TOP/common/tools

export PATH=$PATH:$ANT_HOME/bin

cd RUBiS
ant clean
ant eb_bmp




