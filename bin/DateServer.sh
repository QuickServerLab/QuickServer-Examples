#!/bin/bash
# Exchages object over socket
#nohup java -server -cp ./lib/QuickServer.jar:./dist/dateserver.jar dateserver.DateServer
exec java -server -cp ./lib/QuickServer.jar:./dist/dateserver.jar dateserver.DateServer
