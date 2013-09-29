@rem args = port ip maxClient clientidstart
@java -cp %classpath%;.\..\dist\QuickServer.jar;.\..\dist\chatserver.jar chatserver.TestChatServer 7412 127.0.0.1 %1 %2
