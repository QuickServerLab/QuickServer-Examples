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

package chatserver;

import java.net.*;
import java.io.*;
import java.util.*;
import org.quickserver.net.server.ClientCommandHandler;
import org.quickserver.net.server.ClientEventHandler;
import org.quickserver.net.server.ClientHandler;
import java.util.logging.*;

/**
 * IMPORTANT NOTE: This example just demonstrates how to use some 
 * of the ClientIdentifiable features. This is not the good
 * way to write a chat application. It will be overkill on the server to
 * ask it to find a client from over 1000 clients every time you need
 * to send a message. 
 *
 * @author  Akshathkumar Shetty
 */
public class ChatCommandHandler 
		implements ClientEventHandler, ClientCommandHandler {
	private static final Logger logger = 
		Logger.getLogger(ChatCommandHandler.class.getName());

	//--ClientEventHandler
	public void gotConnected(ClientHandler handler)
		throws SocketTimeoutException, IOException {
		handler.sendSystemMsg("Connection opened : "+handler.getHostAddress());
		
		ChatData cd = (ChatData)handler.getClientData();
		cd.setClientInfo("IP: "+handler.getHostAddress());

		handler.sendClientMsg("{system.msg} Welcome to ChatServer v "+ChatServer.VER);
		handler.sendClientMsg("{system.help} Send username=password ");
		handler.sendClientMsg("{system.help} Send 'quit' to exit ");
	}

	public void lostConnection(ClientHandler handler) throws IOException {
		handler.sendSystemMsg("Connection lost : "+handler.getHostAddress());
		tellOthersInRoom(handler);
	}
	public void closingConnection(ClientHandler handler) throws IOException {
		handler.sendSystemMsg("Connection closed: "+handler.getHostAddress());
		tellOthersInRoom(handler);
	}
	//--ClientEventHandler

	private void tellOthersInRoom(ClientHandler handler) {
		try {
			ChatData cd = (ChatData)handler.getClientData();
			ChatMessaging.sendInfoMessage2Room(handler, cd.getRoom(), "LoggedOut");
		} catch(Exception e) {
			logger.fine("IGNORE Error: "+e);
			e.printStackTrace();
		}		
	}

	public void handleCommand(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException {
		if(command.toLowerCase().equals("quit")) {
			handler.sendClientMsg("{system.msg} Bye");
			handler.closeConnection();
			return;
		} 

		if(command.startsWith("changeRoom")) {
			changeRoom(handler, command);
			return;
		}

		if(command.startsWith("sendMsgToRoom")) {
			ChatMessaging.sendMsgToRoom(handler, command);
			return;
		} 
		
		if(command.startsWith("sendMsg")) {
			ChatMessaging.sendMsg(handler, command);
			return;
		} 

		if(command.startsWith("userList")) {
			ChatMessaging.sendRoomList(handler);
			return;
		} 

		ChatMessaging.printHelp(handler, command);
	}

	public static void changeRoom(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException {
		String room = null;
		int i = command.indexOf(" ");
		if(i==-1) {
			handler.sendClientMsg("{system.error} BadParam no room name sent");
			return;
		}
		room = command.substring(i+1);
		ChatData cd = (ChatData)handler.getClientData();
		String oldRoom = cd.getRoom();
		ChatMessaging.sendInfoMessage2Room(handler, oldRoom, "LoggedOut");
		cd.setRoom(room);
		ChatMessaging.sendInfoMessage2Room(handler, room, "LoggedIn");
		handler.sendClientMsg("{system.msg} Chat Room changed to: "+room);
	}

}
