@rem In production you may disable assertions using -disableassertions or -da 
@rem java -server -ea -jar .\..\lib\QuickServer.jar -load ./../conf/BroadcastServer.xml
@java -server -ea -cp %classpath%;.\..\lib\QuickServer.jar;.\..\dist\broadcastserver.jar broadcastserver.BroadcastServer