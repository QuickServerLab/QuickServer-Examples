#!/bin/bash
#exec java -cp ./../lib/QuickServer.jar:./../dist/chatserver.jar chatserver.ChatServer
#nohup java -server -ea -jar ./../lib/QuickServer.jar -load ./../conf/ChatServer.xml
exec java -server -ea -jar ./../lib/QuickServer.jar -load ./../conf/ChatServer.xml
