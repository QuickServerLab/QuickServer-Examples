@rem args = port ip maxClient clientJunkCount clientJunkSleepInSec
@java -cp %classpath%;.\..\lib\QuickServer.jar;.\..\dist\echoserver.jar echoserver.TestEchoServer 4123 127.0.0.1 1000 50 1
@rem java -cp %classpath%;.\..\dist\echoserver.jar echoserver.TestEchoServerReadLine 4123 127.0.0.1