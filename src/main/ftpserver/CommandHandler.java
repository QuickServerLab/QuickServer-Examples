/*
 * This file is part of the QuickServer library 
 * Copyright (C) 2003-2005 QuickServer.org
 *
 * Use, modification, copying and distribution of this software is subject to
 * the terms and conditions of the GNU Lesser General Public License. 
 * You should have received a copy of the GNU LGP License along with this 
 * library; if not, you can download a copy from <http://www.quickserver.org/>.
 *
 * For questions, suggestions, bug-reports, enhancement-requests etc.
 * visit http://www.quickserver.org
 *
 */

package ftpserver;

import org.quickserver.net.server.ClientCommandHandler;
import org.quickserver.net.server.ClientHandler;

import java.net.*;
import java.io.*;
import java.util.StringTokenizer;
import org.quickserver.util.*;
import java.text.*;
import java.util.Date;
import java.util.logging.*;

/**
 * IMPORTANT NOTE: This example just demonstrates how to use some 
 * of the QuickServer features. This example not built keeping 
 * security in mind. 
 */
public class CommandHandler implements ClientCommandHandler {
	private static Logger logger = 
		Logger.getLogger(CommandHandler.class.getName());

	public void gotConnected(ClientHandler handler)
		throws SocketTimeoutException, IOException {
		handler.sendSystemMsg("Connection opened : "+
			handler.getSocket().getInetAddress(), Level.FINE);

		handler.sendClientMsg("220-     QuickFTPServer v 0.1   ");
		handler.sendClientMsg("220- Developed using QuickServer");
		handler.sendClientMsg("220 ");
		//send feature supported msg
		//handler.sendClientMsg("220 Features: a p .");
	}

	public void lostConnection(ClientHandler handler) throws IOException {
		logger.fine("Connection lost : "+
			handler.getSocket().getInetAddress());
	}
	public void closingConnection(ClientHandler handler) throws IOException {
		logger.fine("Connection closed: " + 
			handler.getSocket().getInetAddress());
	}

	public void handleCommand(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException {
		Data data = (Data)handler.getClientData();
		command = command.trim();
		String ucCommand = command.toUpperCase();
		String args = null;
		String temp = null;
		
		logger.log(Level.FINE, "Got command : {0}", command);

		if(ucCommand.equals("QUIT")) {
			//LOGOUT 
			data.wasQuit = true;
			handler.sendClientMsg("221 Logged out.");
			handler.closeConnection();
			return;
		} else if(ucCommand.endsWith("ABOR")) {
			//ABORT
			data.isStop = true;
			//close data connection
			//handler.sendClientMsg("502 Command not implemented.");
			handler.sendClientMsg("220 OK");
			return;
		} else if(ucCommand.startsWith("STAT")) {
			//STATUS
			handler.sendClientMsg("211- QuickFTPServer");
			handler.sendClientMsg(" Version 0.1 ");
			handler.sendClientMsg(" Connected to " + 
				handler.getSocket().getInetAddress());
			handler.sendClientMsg(" Logged in as "+data.username);
			handler.sendClientMsg(" Cur Dir : "+data.wDir);
			handler.sendClientMsg(" Data connection : "+
				data.isTransferring);
			handler.sendClientMsg("211 End of status");
			return;
		}
		
		///////////// now check if only not processing ///////
		if(data.isProcessing==true) {
			handler.sendClientMsg("503 Bad sequence of commands; another command is being processed.");
			return;
		}
		/////////// ACCESS CONTROL COMMANDS /////////
		if(ucCommand.equals("REIN")) {
			//REINITIALIZE
			handler.sendClientMsg("502 Command not implemented.");
		} else if(ucCommand.startsWith("ACCT")) {
			//ACCOUNT
			args = command.substring("ACCT".length()).trim();
			data.account = args;
			handler.sendClientMsg("230 Account OK.");
		} else if(ucCommand.equals("CDUP")) {
			//CHANGE TO PARENT DIRECTORY 
			int i = data.wDir.lastIndexOf("/");
			String temp_wDir = null;
			if(i!=-1) {
				temp_wDir = data.wDir.substring(0,i);
				if(temp_wDir.equals(""))
					temp_wDir = "/";
				else
					temp_wDir += "/"; //end
				temp = MyString.replaceAll(data.root+temp_wDir,"/","\\");
				temp = MyString.replaceAll(temp,"\\\\","\\");
				File file = new File(temp);
				if(	file.canRead() ){
					data.wDir = temp_wDir;
					handler.sendClientMsg("250 Okay");
				} else {
					handler.sendClientMsg("550 No parent dir");
				}
			} else {
				handler.sendClientMsg("550 No parent dir");
			}
		} else if(ucCommand.startsWith("SMNT")) {
			//STRUCTURE MOUNT 
			handler.sendClientMsg("502 Command not implemented.");
		} 
		///////// TRANSFER PARAMETER COMMANDS ////////
		 else if(ucCommand.startsWith("PORT")) {
			/*
            PORT h1,h2,h3,h4,p1,p2
			*/
			args = command.substring("PORT".length()).trim();
			String ipAddr = "";
			int port = 0;
			StringTokenizer st = new StringTokenizer(args,",");
			int p1 = 0;
			int p2 = 0;
			try	{
				ipAddr+=st.nextToken()+".";	
				ipAddr+=st.nextToken()+".";	
				ipAddr+=st.nextToken()+".";	
				ipAddr+=st.nextToken();	
				p1 = Integer.parseInt(st.nextToken());
				p2 = Integer.parseInt(st.nextToken());
				port = p1*256+p2;
				data.ip = ipAddr;
				data.socketPort = port;
				handler.sendClientMsg("200 Command okay.");
			} catch (Exception e)	{
				handler.sendClientMsg("501 Syntax error in parameters or arguments. : PORT "+e);
			}			
		} else if(ucCommand.startsWith("PASV")) {
			/*
			PASSIVE
			This command requests the server-DTP to "listen" on a data
            port (which is not its default data port) and to wait for a
            connection rather than initiate one upon receipt of a
            transfer command.  The response to this command includes the
            host and port address this server is listening on.
			PORT = p1*256+p2
			p1 = PORT / 256
			p2 = PORT - p1*256
			*/
			//227 =127,0,0,1,10,5

			//reset for PORT
			data.ip = null;

			try	{
				InetAddress ipAddr = handler.getSocket().getLocalAddress();
				String ip_port ="";
				StringTokenizer st = 
					new StringTokenizer(ipAddr.getHostAddress(),".");
				while (st.hasMoreTokens()) {
					ip_port+=st.nextToken()+",";
				}
				data.server = new ServerSocket(0,1,ipAddr);
				data.serverPort = data.server.getLocalPort();
				logger.fine("pasv port "+data.serverPort);
				int p1 = data.serverPort / 256;
				int p2 = data.serverPort - p1*256;
				ip_port += p1+","+p2;
				data.startDataServer(data.server,data); //start server
				handler.sendClientMsg("227 ="+ip_port);
			} catch(Exception e){
				handler.sendClientMsg("425 Can't open data port; Error : "+e);
			}
		} else if(ucCommand.startsWith("TYPE")) {
			/*
			REPRESENTATION TYPE 
			             \    /
               A - ASCII |    | N - Non-print
                         |-><-| T - Telnet format effectors
               E - EBCDIC|    | C - Carriage Control (ASA)
                         /    \
               I - Image
               
               L <byte size> - Local byte Byte size
			*/
			args = command.substring("TYPE".length()).trim();
			if(args.equals("A")) {
				data.binary = false;
				data.type = 'A';
				data.typeSub = 'Z';
			} else if(args.equals("A N")) {
				data.binary = false;
				data.type = 'A';
				data.typeSub = 'N';
			} else if(args.equals("I")) {
				data.binary = true;
				data.type = 'I';
				data.typeSub = 'Z';
			} else if(args.equals("L 8")) {
				data.binary = true;
				//data.type = 'A';
				//data.typeSub = 'N';
			} else {
				handler.sendClientMsg("501 Syntax error in parameters.");
				return;
			}
			handler.sendClientMsg("200 Command OK.");
		} else if(ucCommand.startsWith("STRU")) {
			/*
			FILE STRUCTURE
	 		   F - File (no record structure) - default
               R - Record structure
               P - Page structure
			*/
			if(ucCommand.equals("STRU F")) {
				handler.sendClientMsg("200 Command OK.");
			} else {
				//obsolete
				handler.sendClientMsg("504 Command not implemented for that parameter.");
			}
		} else if(ucCommand.startsWith("MODE")) {
			/*
			TRANSFER MODE
			   S - Stream - Default
               B - Block
               C - Compressed
			*/
			if(ucCommand.equals("MODE S")) {
				handler.sendClientMsg("200 Command OK.");
			} else {
				//obsolete
				handler.sendClientMsg("504 Command not implemented for that parameter.");
			}
		}
		/////// FTP SERVICE COMMANDS ///////
		else if(ucCommand.startsWith("RETR")) {
			data.isTransferring = true;
			//RETRIEVE
			args = command.substring("RETR".length()).trim();
			String sfile = "";
			//check if NOT PASSIVE, i.e PORT was set
			if(data.ip!=null) {
				try	{
					data.socket = new Socket(
						InetAddress.getByName(data.ip),	data.socketPort);
				} catch (Exception e)	{
					handler.sendClientMsg("425 Can't open data connection.");
					data.isTransferring = false;
					return;
				}			
			}
			if(data.socket != null) {
				if(args.charAt(0)=='/') {
					sfile = data.root + args;
				} else {
					sfile = data.root + data.wDir + "/" + args;
				}
				temp = MyString.replaceAll(sfile,"/","\\");
				temp = MyString.replaceAll(temp,"\\\\","\\");
				File file = new File(temp);
				if(file.canRead() && file.isFile()) {
					handler.sendClientMsg("150 I see that file.");
					//send file
					try	{
						data.sendFile(temp);
						//close data connection when done
						if(data.ip!=null)
							data.socket.close();
						data.closeDataServer = true;
						if(data.isStop==false)
							handler.sendClientMsg("226 File transferred successfully.");
						else
							handler.sendClientMsg("551 Error sending file : User Aborted");
					} catch (Exception e) {
						data.closeDataServer = true;
						handler.sendClientMsg("551 Error sending file : "+e);
					}
				} else {
					handler.sendClientMsg("451 Sorry, that isn't a data file");
				}
			} else {
				handler.sendClientMsg("425 Sorry no TCP connection was established.");
			}
			data.isTransferring = false;
		} else if(ucCommand.startsWith("STOR")) {
			//STORE 
			args = command.substring("STOR".length()).trim();
			handler.sendClientMsg("502 Command not implemented.");
		} else if(ucCommand.startsWith("STOU")) {
			//STORE UNIQUE - The 250 Transfer Started response
			//must include the name generated.
			args = command.substring("STOU".length()).trim();
			handler.sendClientMsg("502 Command not implemented.");
		} else if(ucCommand.startsWith("APPE")) {
			//APPEND (with create)
			handler.sendClientMsg("502 Command not implemented.");
		} else if(ucCommand.startsWith("ALLO")) {
			//ALLOCATE - obsolete
			handler.sendClientMsg("502 Command not implemented - obsolete.");
		} else if(ucCommand.startsWith("REST")) {
			//RESTART transfer
			//350 ok
			handler.sendClientMsg("502 Command not implemented.");
		} else if(ucCommand.startsWith("RNFR")) {
			//RENAME FROM
			data.isRenameFrom = true;
			handler.sendClientMsg("502 Command not implemented.");
		} else if(ucCommand.startsWith("RNTO")) {
			//RENAME TO
			if(!data.isRenameFrom) {
				//error should not happen
			}
			data.isRenameFrom = false;
			handler.sendClientMsg("502 Command not implemented.");
		} else if(ucCommand.startsWith("DELE")) {
			//DELETE
			handler.sendClientMsg("502 Command not implemented.");
		} else if(ucCommand.startsWith("RMD")) {
			//REMOVE DIRECTORY
			handler.sendClientMsg("502 Command not implemented.");
		} else if(ucCommand.startsWith("MKD")) {
			//MAKE DIRECTORY
			args = command.substring("MKD".length()).trim();
			temp = MyString.replaceAll(data.root+data.wDir+args,"/","\\");
			temp = MyString.replaceAll(temp,"\\\\","\\");
			File file = new File(temp);
			try	{
				file.mkdir();
				file.canRead();
				temp = file.getAbsolutePath();
				temp = "/"+MyString.replaceAll(temp,data.root,"");
				temp = MyString.replaceAll(temp,"\\","/");
				temp = MyString.replaceAll(temp,"//","/");
				handler.sendClientMsg("257 \""+temp+"\" directory created");
			} catch(Exception e) {
				handler.sendClientMsg("521-Could not create dir \""+args+"\"");
				handler.sendClientMsg("521 Error : "+e);
			}
		} else if(ucCommand.startsWith("CWD")) {
			//CHANGE DIRECTORY
			temp = data.wDir;
			args = command.substring("CWD".length()).trim();
			if(data.wDir.charAt(data.wDir.length()-1)!='/')
				data.wDir += "/";
			if(args.charAt(args.length()-1)!='/')
				args += "/";

			if(args.charAt(0)!='/')
				data.wDir += args;
			else
				data.wDir = args;

			temp = MyString.replaceAll(data.root+data.wDir,"/","\\");
			temp = MyString.replaceAll(temp,"\\\\","\\");
			File file = new File(temp);
			if(file.canRead() && file.isDirectory()) {
				handler.sendClientMsg("250 Directory changed to "+data.wDir);
			} else {
				if(file.canRead())
					handler.sendClientMsg("550 "+data.wDir+": The directory name is invalid.");
				else
					handler.sendClientMsg("550 "+data.wDir+": The system cannot find the file specified .");
				data.wDir = temp;
				logger.logp(Level.FINER, "CommandHandler", "handleCommand",
					"Command=CWD; ERROR : 550 No such directory "+file.getCanonicalPath());
			}
		} else if(ucCommand.startsWith("PWD")) {
			//PRINT WORKING DIRECTORY
			temp = MyString.replaceAll(data.wDir,"\"","\"\"");
			handler.sendClientMsg("257 \""+data.wDir+"\"");
		} else if(ucCommand.startsWith("LIST")) {
			data.isTransferring = true;
			if(ucCommand.equals("LIST")) {
				args = "";
			} else {
				args = command.substring("LIST".length()).trim();
				if(args.equals("-latr")) //not known
					args="";
			}
			temp = MyString.replaceAll(data.root+data.wDir+args,"/","\\");
			temp = MyString.replaceAll(temp,"\\\\","\\");
			File file = new File(temp);
			
			if( file.canRead() ) {
				handler.sendClientMsg("150 Opening data connection for LIST "+data.wDir+args);
				if(data.ip!=null) {
					try	{
						data.socket = new Socket(
							InetAddress.getByName(data.ip), 
							data.socketPort);
					} catch (Exception e)	{
						handler.sendClientMsg("425 Can't open data connection.");
						data.isTransferring = false;
						return;
					}			
				}
				String result = winDirList(temp);
				try	{
					data.sendData(result);
					//close data connection when done
					if(data.ip!=null) {
						data.socket.close();
					}
					data.closeDataServer = true;
					if(data.isStop==false)
						handler.sendClientMsg("226 File transferred successfully.");
					else
						handler.sendClientMsg("551 Error sending file : User Aborted");
				} catch (Exception e) {
					if(data.ip!=null && data.socket!=null)
						data.socket.close();
					data.closeDataServer = true;
					handler.sendClientMsg("551 Error sending LIST : "+e);
				}
			} else {
				handler.sendClientMsg("550 No such directory : "+data.wDir+args);
				logger.logp(Level.FINER, "CommandHandler", "handleCommand",
					"Command=LIST; ERROR : 550 No such directory "+file);
			}
			data.isTransferring = false;
		} else if(ucCommand.startsWith("NLST")) {
			//NAME LIST of directory only
			data.isTransferring = true;
			if(ucCommand.equals("NLST")) {
				args = "";
			} else {
				args = command.substring("NLST".length()).trim();
			}
			temp = MyString.replaceAll(data.root+data.wDir+args,"/","\\");
			temp = MyString.replaceAll(temp,"\\\\","\\");
			File file = new File(temp);
			String result = "";
			if( file.canRead() && file.isDirectory() ) {
				handler.sendClientMsg("150 Opening data connection for LIST "+data.wDir+args);
				if(data.ip!=null) {
					try	{
						data.socket = new Socket(
							InetAddress.getByName(data.ip), 
							data.socketPort);
					} catch (Exception e)	{
						handler.sendClientMsg("425 Can't open data connection.");
						data.isTransferring = false;
						return;
					}			
				}
				String list[] = file.list();
				for(int i=0;i<list.length;i++) {
					if(!list[i].equals(".") && !list[i].equals("..") )
						result +=list[i]+"\r\n";
				}
				try	{
					data.sendData(result);
					//close data connection when done
					if(data.ip!=null)
						data.socket.close();
					data.closeDataServer = true;
					if(data.isStop==false)
						handler.sendClientMsg("226 File transferred successfully.");
					else
						handler.sendClientMsg("551 Error sending file : User Aborted");
				} catch (Exception e) {
					if(data.ip!=null && data.socket!=null)
						data.socket.close();
					data.closeDataServer = true;
					handler.sendClientMsg("551 Error sending NLST : "+e);
				}
			} else {
				handler.sendClientMsg("550 No such directory : "+data.wDir+args);
				logger.logp(Level.FINER, "CommandHandler", "handleCommand",
					"Command=NLST; ERROR : 550 No such directory "+file);
			}
			data.isTransferring = false;
		} else if(ucCommand.startsWith("SITE")) {
			//SITE PARAMETERS
			handler.sendClientMsg("502 Command not implemented.");
		} else if(ucCommand.startsWith("SYST")) {
			//SYSTEM - Assigned Numbers document [4]
			// UNIX Type: L8
			handler.sendClientMsg("215 "+"Windows_NT version 5.0");
		} else if(ucCommand.startsWith("HELP")) {
			//HELP
			//The reply is type 211 or 214.
			handler.sendClientMsg("502 Command not implemented.");
		} else if(ucCommand.startsWith("NOOP")) {
			//NOOP - OK reply
			handler.sendClientMsg("200 OK");
		} else if(ucCommand.startsWith("SIZE")) {
			//SIZE OF FILE
			args = command.substring("SIZE".length()).trim();
			temp = MyString.replaceAll(data.root+data.wDir+args,"/","\\");
			temp = MyString.replaceAll(temp,"\\\\","\\");
			File file = new File(temp);
			if( file.canRead() ) {
				handler.sendClientMsg("213 "+file.length());
			} else {
				handler.sendClientMsg("550 No such file.");
			}
		} else {
			//ERROR
			handler.sendClientMsg("500 Syntax error, command unrecognized.");
		}
	}

	// helper meethods
	private String dirList(String dir) {
		//+FACTS1,FACTS@..,FACTN\tfile_name\r\n
		/*
			FACE = xy
		   >X<
			r -> File
			/ -> Dir
			s ->Size, y=size in bytes
			m ->Modified since 1970
			i ->This file has identifier y. 
			up->Chmode is allowed
		*/
		File file = new File(dir);
		File subFile = null;
		String result = "";
		if( file.canRead() ) {
			String list[] = file.list();
			for(int i=0;i<list.length;i++) {
				//if(list[i].equals(".") || list[i].equals("..") )
				//	continue;
				subFile = new File(dir+File.separator+list[i]);
				result += "+";
				result +="i"+subFile.hashCode()+",";
				result +="s"+subFile.length()+",";
				result +="m"+subFile.lastModified()+",";
				if(subFile.isFile()) {
					result +="r,";
				} else {
					result +="/,";
				}
				result +="\t"+list[i]+"\r\n";
			}
		}
		return result;
	}

	private String winDirList(String dir) {
		File file = new File(dir);
		File subFile = null;
		String result = "";
		if( file.canRead() ) {
			String list[] = file.list();
			for(int i=0;i<list.length;i++) {
				subFile = new File(dir+File.separator+list[i]);
				SimpleDateFormat dformat = 
					new SimpleDateFormat("MM-dd-yy  HH:mm:a ");
				result += dformat.format(new Date(subFile.lastModified()));
				if(subFile.isFile()) {
					//20 field length
					StringBuffer size = new StringBuffer(20);
					size.append(subFile.length());
					while(size.length()<20) {
						size.insert(0," ");
					}
					result += size.toString();
				} else {
					result +="      <DIR>         ";
				}
				result +=" "+list[i]+"\r\n";
			}
		}
		return result;
	}
}
