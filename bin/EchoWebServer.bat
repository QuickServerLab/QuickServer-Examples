@rem Use any one of the command below to start the server.
@rem java -server -enableassertions -cp %classpath%;.\..\lib\QuickServer.jar;.\..\dist\echowebserver.jar echowebserver.EchoWebServer
@java -server -ea -jar .\..\lib\QuickServer.jar -load .\..\conf\EchoWebServer.xml