#!/bin/bash
# Exchages object over socket
exec java -server -cp ./../lib/QuickServer.jar:./../dist/dateserver.jar dateserver.DateServerClient 127.0.0.1 8125