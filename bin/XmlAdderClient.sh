#!/bin/bash
exec java -Dxmladder.XmlAdderClient.bebug=true -Dxmladder.XmlAdderClient.brokenReq=true -cp ./dist/xmladder.jar:./lib/commons-digester.jar:./lib/commons-collections.jar:./lib/commons-logging.jar:./lib/commons-beanutils.jar xmladder.XmlAdderClient "$@"
