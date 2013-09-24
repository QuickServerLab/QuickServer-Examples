#!/bin/bash
#nohup java -server -ea -jar ./lib/QuickServer.jar -load conf/SecureEchoWebServer.xml
exec java -server -ea -jar ./lib/QuickServer.jar -load conf/SecureEchoWebServer.xml
