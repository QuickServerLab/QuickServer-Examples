@rem Exchages object over socket
@java -server -cp %classpath%;.\..\lib\QuickServer.jar;.\..\dist\dateserver.jar dateserver.DateServerClient 127.0.0.1 8125
