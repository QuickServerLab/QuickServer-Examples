#!/bin/bash
# Change shell_program to a valid executable
#nohup java -server -cp ./lib/QuickServer.jar:./dist/cmdserver.jar cmdserver.CmdServer sh
exec java -server -cp ./lib/QuickServer.jar:./dist/cmdserver.jar cmdserver.CmdServer sh
