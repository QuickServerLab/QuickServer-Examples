#!/bin/bash
#nohup java -server -Xbatch -Dxmladder.AddNumberReq.performanceTest=false -Dorg.quickserver.util.logging.SimpleJDKLoggingHook.Level=INFO -cp ./lib/QuickServer.jar:./dist/xmladder.jar
exec java -server -Xbatch -Dxmladder.AddNumberReq.performanceTest=false -Dorg.quickserver.util.logging.SimpleJDKLoggingHook.Level=INFO -cp ./lib/QuickServer.jar:./dist/xmladder.jar xmladder.XmlAdder
