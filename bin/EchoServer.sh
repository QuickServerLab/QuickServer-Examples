#!/bin/bash
#nohup java -server -enableassertions -cp ./../lib/QuickServer.jar:./../dist/echoserver.jar echoserver.EchoServer &
#exec java -server -enableassertions -cp ./../lib/QuickServer.jar:./../dist/echoserver.jar echoserver.EchoServer
exec java -server -enableassertions -jar ./../lib/QuickServer.jar -load ./../conf/EchoServer.xml

