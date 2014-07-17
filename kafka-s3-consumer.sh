#!/bin/bash

java -server \
-verbosegc \
-XX:MaxPermSize=256M \
-Xss1024k \
-XX:+PrintGCDetails \
-XX:+PrintGCTimeStamps \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/tmp/druid/heapdump \
-XX:+UseConcMarkSweepGC \
-Xloggc:/tmp/gc/log \
-Duser.timezone=UTC \
-Dfile.encoding=UTF-8 \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.local.only=false \
-Dcom.sun.management.jmxremote.port=10000 \
-Djava.rmi.server.hostname=localhost \
-jar target/kafka-s3-consumer-1.0.jar
