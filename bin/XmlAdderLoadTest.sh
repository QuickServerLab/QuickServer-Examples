#!/bin/bash
#Args: [client_count] [host] [port]
exec java -Dxmladder.XmlAdderClient.bebug=false -Dxmladder.XmlAdderClient.brokenReq=false -cp ./dist/xmladder.jar:./lib/commons-digester.jar:./lib/commons-collections.jar:./lib/commons-logging.jar:./lib/commons-beanutils.jar xmladder.LoadTest "$@"