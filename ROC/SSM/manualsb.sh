#!/bin/sh



PUBLISHER=-Droc.pinpoint.tracing.Publisher=roc.pinpoint.tracing.java.TCPObservationPublisher
HOSTNAME=-Droc.pinpoint.publishto.hostname=x1.millennium.berkeley.edu

java $PUBLISHER $HOSTNAME -Xms384M -Xmx384M Start b $1








