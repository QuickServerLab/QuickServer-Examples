#!/bin/bash
# exec java -server -cp ./../lib/QuickServer.jar:./../dist/ftpserver.jar ftpserver.FtpServer "$@"
#nohup java -server -jar ./../lib/QuickServer.jar -load ./../conf/FtpServer.xml
exec java -server -jar ./../lib/QuickServer.jar -load ./../conf/FtpServer.xml
