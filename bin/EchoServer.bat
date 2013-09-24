@java -server -enableassertions -cp "%classpath%";.\..\lib\QuickServer.jar;.\..\ldist\echoserver.jar echoserver.EchoServer
@rem In production you may disable assertions using -disableassertions or -da 
java -server -ea -jar .\..\lib\QuickServer.jar -load ./../conf/EchoServer.xml
