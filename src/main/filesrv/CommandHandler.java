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

package filesrv;

import java.net.*;
import java.io.*;
import java.util.*;
import org.quickserver.net.server.*;
import java.util.logging.*;
import org.quickserver.util.MyString;

/**
 * FileServer Example
 * @author Akshathkumar Shetty
 */
public class CommandHandler 
		implements ClientEventHandler, ClientCommandHandler, ClientWriteHandler {
	private static Logger logger = 
		Logger.getLogger(CommandHandler.class.getName());

	public void gotConnected(ClientHandler handler)
			throws SocketTimeoutException, IOException {
		logger.fine("Connection opened: "+handler.getSocket().getInetAddress());
		//handler.setDataMode(DataMode.BYTE, DataType.OUT); //pick from xml
	}

	public void lostConnection(ClientHandler handler) throws IOException {
		cleanByteBuffer(handler);
		logger.fine("Connection lost: "+handler.getSocket().getInetAddress());		
	}
	public void closingConnection(ClientHandler handler) throws IOException {
		cleanByteBuffer(handler);
		logger.fine("Connection closed: "+handler.getSocket().getInetAddress());
	}
	private void cleanByteBuffer(ClientHandler handler) {
		Data data = (Data)handler.getClientData();
		data.cleanPooledByteBuffer(handler.getServer());
	}


	public void handleCommand(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException {

		Data data = (Data)handler.getClientData();
		try	{
			 if(data.isHeaderReady()==false) {
				 if(command.startsWith("GET /")) {
					 data.initHeader(command);
				 } else {
					serveBadRequest(handler, BAD_REQUEST);
					handler.closeConnection();
				 }
				 return;
			 }

			 if(data.addHeader(command)==false) {
				 return;
			 }

			if(data.isDirList()) {
				listDir(handler, data);
			} else {
				if(handler.getServer().getConfig().getServerMode().getBlocking()) {
					data.sendFileBlocking(handler);
				} else {
					data.sendFileNonBlocking(handler);
				}		
			}
		} catch(BadRequestException e) {
			logger.fine("BadRequestException : "+e);
			serveBadRequest(handler, ERROR_HEADER+"Bad Req: "+e.getMessage()+
				ERROR_FOOTER, "400 Bad Request");
			handler.closeConnection();
		} catch(Exception e) {
			logger.info("Error processing : "+MyString.getStackTrace(e));
			try {
				serveBadRequest(handler, ERROR_HEADER+"Bad Req: "+e.getMessage()+
					ERROR_FOOTER);
			} catch(Exception err) {
				logger.warning("Error processing error: "+err);
			}
			handler.closeConnection();
		}
	}

	private void listDir(ClientHandler handler, Data data) 
			throws IOException {
		String content = data.getDirList();

		StringBuffer sb = new StringBuffer();
		sb.append("HTTP/1.1 200 OK\r\n");
		sb.append("Server: ").append(handler.getServer().getName()).append("\r\n");
		sb.append("Content-Type: text/html").append("\r\n");;
		sb.append("Content-Length: "+content.length()).append("\r\n");;
		sb.append("\r\n");
		sb.append(content);

		if(handler.getServer().getConfig().getServerMode().getBlocking()) {
			handler.sendClientBytes(sb.toString());
			handler.closeConnection();
		} else {
			logger.fine("Will Send: \n"+sb.toString());
			data.makeNonBlockingWrite(handler, sb.toString().getBytes(), 
				false, "Sending HTTP header with dir list.", true);
		}
	}

	private void serveBadRequest(ClientHandler handler, String error)  throws IOException {
		serveBadRequest(handler, error, "500 Internal Server Error");
	}

	private void serveBadRequest(ClientHandler handler, String error, String msg)  throws IOException  {
		StringBuffer sb = new StringBuffer();

		sb.append("HTTP/1.1 ").append(msg).append("\r\n");
		sb.append("Server: ").append(handler.getServer().getName()).append("\r\n");
		sb.append("Content-Type: text/html").append("\r\n");;
		sb.append("Content-Length: "+error.length()).append("\r\n");;
		sb.append("\r\n");
		sb.append(error);
		handler.sendClientBytes(sb.toString());
	}

	public void handleWrite(ClientHandler handler)
			throws IOException {
		handler.isConnected();
		Data data = (Data)handler.getClientData();
		try {
			data.writeData(handler);
		} catch(Exception e) {
			logger.fine("Error processing: "+e);
			if(data.getWroteFileHttpHeader()==false) {
				try {
					serveBadRequest(handler, ERROR_HEADER+"Error processing: "+e.getMessage()+
						ERROR_FOOTER);
				} catch(Exception err) {
					logger.warning("Error processing error: "+err);
				}
			}
			handler.closeConnection();
		}
		
	}

	//-- ERRORS--
	private static final String ERROR_HEADER = "<html>\r\n<head>\r\n<title>HFtp Server - Error</title>\r\n</head>\r\n"+
		"<body>"+"<H3>HFtp Server - Error</H3><hr/><font color=\"red\">";
	private static final String ERROR_FOOTER = "</font><hr/>\r\n</body>\r\n</html>";
	private static final String BAD_REQUEST = ERROR_HEADER+
		"HTTP GET requests are only supported!"+ERROR_FOOTER;
		
}
