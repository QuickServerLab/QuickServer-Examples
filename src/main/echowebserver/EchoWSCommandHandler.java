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

package echowebserver;

import java.net.*;
import java.io.*;
import org.quickserver.net.server.*;
import java.util.logging.*;

/**
 * EchoWSCommandHandler
 * @author Akshathkumar Shetty
 */
public class EchoWSCommandHandler implements ClientCommandHandler {
	private static Logger logger = 
			Logger.getLogger(EchoWSCommandHandler.class.getName());
	
	private static String header = null;
	private static String trailer = null;

	public static void init(QuickServer quickserevr) {
		StringBuffer sb = new StringBuffer();
		sb.append("<!DOCTYPE HTML PUBLIC ");
		sb.append("\"-//W3C//DTD HTML 4.0 Transitional//EN\">\n");
		sb.append("<HTML>\n");
		sb.append("<HEAD>\n");
		sb.append(" <TITLE>" +quickserevr.getName());
		sb.append(" Results</TITLE>\n");
		sb.append("</HEAD>\n" +"\n");
		sb.append("<BODY>\n");
		sb.append("<H1 ALIGN=\"CENTER\">"+quickserevr.getName());
		sb.append(" Results</H1>\n");
		sb.append("<table width=\"100%\"><tr><td><PRE>");
		header = sb.toString();

		sb.setLength(0); //clear
		sb.append("</PRE></td></tr></table>\n");
		sb.append("</BODY>\n");
		sb.append("</HTML>");
		trailer = sb.toString();
	}


	public void gotConnected(ClientHandler handler)
			throws SocketTimeoutException, IOException {
		//handler.setDataMode(DataMode.BYTE, DataType.IN); //doing from xml
		logger.fine("Connection opened : "+handler.getHostAddress());
	}
	public void lostConnection(ClientHandler handler) 
		throws IOException {
		logger.fine("Connection lost : "+handler.getHostAddress());
	}
	public void closingConnection(ClientHandler handler) 
		throws IOException {
		handler.sendSystemMsg("Connection closed: "+handler.getHostAddress(), Level.FINE);
	}

	public void handleCommand(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException {
		EchoWSData data = (EchoWSData)handler.getClientData();

		data.addInput(command);

		if(data.isRequestComplete()) {
			logger.fine("Request Complete");
			sendOutput(handler, data);
			return;
		}
	}


	//helper methods
	private void sendOutput(ClientHandler handler, EchoWSData data) throws IOException {
		StringBuffer sb = new StringBuffer();
		sb.append(header);		
		sb.append(data.getDataForOutput());
		sb.append(trailer);

		handler.sendClientMsg("HTTP/1.0 200 OK");
		handler.sendClientMsg("Server: " + handler.getServer().getName());
		handler.sendClientMsg("Content-Type: text/html");
		handler.sendClientMsg("Content-Length: "+sb.length());
		handler.sendClientMsg("");
		handler.sendClientMsg(sb.toString());

		handler.closeConnection();
	}

}
