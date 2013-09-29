#!/bin/bash
#nohup java -server -cp ./../lib/QuickServer.jar:./../dist/pipeserver.jar pipeserver.PipeServer
exec java -server -cp ./../lib/QuickServer.jar:./../dist/pipeserver.jar pipeserver.PipeServer
