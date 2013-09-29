@echo Change cmd.exe to command.com for win 9x in the batch file
@java -server -cp %classpath%;.\..\lib\QuickServer.jar;.\..\dist\cmdserver.jar cmdserver.CmdServer cmd.exe
