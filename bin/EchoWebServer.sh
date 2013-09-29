#!/bin/bash
# Use any one of the command below to start the server.
#exec java -server -enableassertions -cp ./../lib/QuickServer.jar:./../dist/echowebserver.jar echowebserver.EchoWebServer
#nohup java -server -ea -jar ./../lib/QuickServer.jar -load ./../conf/EchoWebServer.xml
exec java -server -ea -jar ./../lib/QuickServer.jar -load ./../conf/EchoWebServer.xml
