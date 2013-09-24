@rem java -server -cp %classpath%;.\lib\QuickServer.jar;.\dist\ftpserver.jar ftpserver.FtpServer %1
@java -server -jar .\lib\QuickServer.jar -load conf/FtpServer.xml
