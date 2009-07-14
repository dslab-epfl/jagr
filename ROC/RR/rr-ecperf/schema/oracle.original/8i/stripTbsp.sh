#!/bin/sh
# Script to strip off tablespace requirements from the sql scripts
# Akara Sucharitakul, Jul 12, 2000

if [ ! -d sql.noTbsp ] ; then
    mkdir sql.noTbsp
fi

for i in sql/*.sql
do
    sed /TABLESPACE/d $i > sql.noTbsp/`basename $i`
done
