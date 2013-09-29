#!/bin/bash
# In production you may disable assertions using -disableassertions or -da 
#nohup java -server -ea -jar ./../lib/QuickServer.jar -load ./../conf/BroadcastServer.xml
exec java -server -ea -jar ./../lib/QuickServer.jar -load ./../conf/BroadcastServer.xml
