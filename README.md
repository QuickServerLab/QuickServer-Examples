ReadMe for QuickServer Examples
-------------------------------

IMPORTANT NOTE: These examples are written to demonstrate how to use some 
of the features of QuickServer. These examples were written to be simple 
and easy to understand and not as a production versions. So the author/s have
not taken security or performance has his main criteria. 

How To Run Examples
-------------------
Execute .sh or .bat file from the bin folder.

Eg: $sh EchoServer.sh
Eg: d:\QuickSerer-Example-Folder\bin>EchoServer.bat


EchoServer v1.2
-------------------
Simple Server - Sends back "Echo :"+MSG where MSG is the message sent by 
the client socket.
Uses:-
 - QuickAuthenticator, ServerAuthenticator extended classes to add authentication.
 - Txt Logging - Separate log files for application and base quickserver.
 - Xml configuration file used to load configuration.
 - XML Based JDBC Mapping - EchoServerAuthenticatorDBBased
 - Remote Administration port - QSAdminServer
 - QSAdmin Shell
 - Init Server Hooks
 - <access-constraint>
Bonus:
 - Client code: Helps to test EchoServer.


EchoWebServer v1.2
-------------------
A simple HTTP server that generates a Web page showing all the data that it 
received from the Web client (browser).
Uses:-
 - Simple txt logging.
 - Xml configuration file used to load configuration.
 - ClientData implementation class to store client data.
 - PoolableObject implemented ClientData
 - QSAdmin Shell
 - Server Hooks


CmdServer v1.2
-------------------
A remote command shell (cmd.exe or command.com or any console program)
Uses:-
 - ClientData implementation class to store client data.
 - StoreObjects feature of QuickServer.
 - Remote Administration - QSAdminServer

FtpServer v 0.2
-------------------
Basic ftp server implementation. 
Uses:-
 - Xml configuration file used to load configuration.
 - ServerAuthenticator extended class to add authentication.
 - ClientData implementation class to store client data.
 - Remote Administration port - QSAdminServer
 - XML Logging - Separate log files for application and base quickserver 
   and remote AdminServer - QSAdminServer
 - CommandPlugin implementation class to add custom commands to remote 
   AdminServer - QSAdminServer.
 - QSAdmin Shell
 - <application-configuration> to load config.
 - Init Server Hooks
 - PoolableObject implemented ClientData


DateServer v 1.0
-------------------
Demonstrates how to send java object over the socket using QuickServer, it 
exchanges java.util.Date object.
Uses:-
 - Simple txt logging.
 - ClientObjectHandler implementation class to handle client objects.


ChatServer v 1.0
-------------------
Demonstrates how to find another client connected and send String to it.
Uses:-
 - ClientIdentifiable and QuickServer.findFirstClientById().
 - QSAdmin Shell
 - Xml configuration file used to load configuration.
 - Init Server Hooks
 - PoolableObject implemented ClientData
Bonus:
 - Client GUI: GUI for chatserver testing
 - Load Client code: Helps to load test ChatServer.


SecureEchoWebServer v1.2
-------------------
A simple HTTPS server that generates a Web page showing all the data that it 
received from the Web client (browser).
Uses:-
 - Secure Mode - SSL
 - Simple txt logging.
 - Xml configuration file used to load configuration.
 - ClientData implementation class to store client data.
 - PoolableObject implemented ClientData
 - QSAdmin Shell
 - Server Hooks


XmlAdder v 1.0
-------------------
A simple xml server that adds 2 integers. Demonstrates how to use BYTE mode.
Uses:-
 - Xml configuration file used to load configuration.
 - BYTE data mode.
 - Simple txt logging.
 - XML data used
 - QSAdmin Shell
 - Remote Administration - QSAdminServer


PipeServer v 1.0
-------------------
A simple redirection server. Demonstrates how to use BINARY mode.
Uses:-
 - Xml configuration file used to load configuration.
 - BINARY data mode.
 - Simple txt logging.
 - <application-configuration> to load configuration.
 - ClientData implementation class to store client data.
 - QSAdmin Shell
 - Remote Administration - QSAdminServer

 
Filesrv v 0.1
-------------------
A simple http file download server. Will listen at 8821, so use your web 
browser as a client. URL: http://localhost:8821/
Uses:-
 - Non blocking mode
 - Xml configuration file used to load configuration.
 - STRING, BYTE and BINARY data mode.
 - ClientWriteHandler implementation class to do non-blocking write.
 - Init-server-hooks.
 - AdvancedSettings
 - QSAdmin Shell
 - Remote Administration - QSAdminServer
 - Simple txt logging.



License: 
--------
a) All source code from the example that contain the following phrase 
   "This file is part of the QuickServer library" may be used as a starting 
   point in your development.
b) chatserver.client.InfiniteProgressPanel is subjected to the BSD license. 

