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


import java.io.*;
import java.net.*;
import org.quickserver.net.server.ClientHandler;
import java.util.*;
import java.util.logging.*;

/**
 *
 * @author  Akshathkumar Shetty
 */
public class ChatMessaging {
	private static final Logger logger = 
		Logger.getLogger(ChatMessaging.class.getName());

	public static void sendInfoMessage2Room(ClientHandler handler, 
			String room, String msg) throws SocketTimeoutException, 
				IOException {
		if(room==null || msg==null) return;
		Iterator iterator = handler.getServer().findAllClientByKey(
			".+@"+room);
		ClientHandler toHandler = null;
		if(iterator.hasNext()) {
			while(iterator.hasNext()) {
				if(msg.equals("LoggedOut")==false)
					handler.isConnected();//check if src is still alive
				toHandler = (ClientHandler) iterator.next();
				if(toHandler==handler) continue;
				sendInfoMessage(handler, toHandler, msg);
			}
		} /*else {
			handler.sendClientMsg("{system.error} MessageNotSent No Clients in that room");
		}*/
	}

	public static void sendInfoMessage(ClientHandler handler, 
			ClientHandler toHandler, String message) throws IOException {
		if(message==null) return;
		logger.fine("From: "+handler+"To: "+toHandler+", "+message);
		if(toHandler==null) return;

		ChatData data = (ChatData)handler.getClientData();
		try	{
			toHandler.sendClientMsg("{user.info:"+data.getClientKey()+"} "+message);	
		} catch(Exception e) {
			logger.fine("Error sending msg: "+e);
		}
	}

	public static void sendMessageBwUsers(ClientHandler handler, 
			ClientHandler toHandler, String message) throws IOException {
		logger.fine("From: "+handler+"To: "+toHandler+", "+message);
		if(toHandler==null) return;

		ChatData data = (ChatData)handler.getClientData();
		try	{
			ChatData toCd = (ChatData)toHandler.getClientData();
			toHandler.sendClientMsg("{user.msg:"+data.getClientKey()+":"+
				toCd.getClientKey()+"} "+message);
			handler.sendClientMsg("{system.debug} SentMessageTo "+toCd.getClientKey());
		} catch(Exception e) {
			logger.warning("Error sending msg: "+e);
			handler.sendClientMsg("{system.error} MessageNotSent "+e.getMessage());
		}
	}

	public static void sendMessage2Room(ClientHandler handler, 
			ClientHandler toHandler, String message) throws IOException {
		logger.fine("From: "+handler+"To: "+toHandler+", "+message);
		if(toHandler==null) return;

		ChatData data = (ChatData)handler.getClientData();
		try	{
			ChatData toCd = (ChatData)toHandler.getClientData();
			toHandler.sendClientMsg("{user.msg:"+data.getClientKey()+":@"+
				toCd.getRoom()+"} "+message);			
			handler.sendClientMsg("{system.debug} SentMessageTo "+toCd.getClientKey());
		} catch(Exception e) {
			logger.warning("Error sending msg: "+e);
			handler.sendClientMsg("{system.error} MessageNotSent "+e.getMessage());
		}
	}

	public static void printHelp(ClientHandler handler, String command) 
			throws IOException {
		if(command!=null)
			handler.sendClientMsg("{system.error} BadCommand "+command);
		handler.sendClientMsg("{system.help} Sending Message Format:");
		handler.sendClientMsg("{system.help} sendMsg <<username>> <<message>>");
		handler.sendClientMsg("{system.help} sendMsg <<username@room>> <<message>>");
		handler.sendClientMsg("{system.help} sendMsgToRoom <<room_name>> <<message>>");
		handler.sendClientMsg("{system.help} userList");
		handler.sendClientMsg("{system.help} changeRoom <<room_name>>");
	}

	
	public static void sendMsgToRoom(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException {
		String room = null;
		String message = null;
		int i = command.indexOf(" ");
		if(i==-1) {
			handler.sendClientMsg("{system.error} BadCommand "+command);
			return;
		}
		int j = command.indexOf(" ",i+1);
		if(j==-1) j = command.length();
		room = command.substring(i+1, j);

		if(j!=command.length())
			message = command.substring(j+1);
		else
			message = "";

		Iterator iterator = handler.getServer().findAllClientByKey(".+@"+room);
		if(iterator.hasNext()) {
			while(iterator.hasNext()) {
				handler.isConnected();//check if src is still alive
				ChatMessaging.sendMessage2Room(handler, (ClientHandler) 
					iterator.next(), message);
			}
		} else {
			handler.sendClientMsg("{system.error} MessageNotSent No Clients in that room");
		}
	}

	public static void sendMsg(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException {
		String id = null;
		String message = null;
		int i = command.indexOf(" ");
		if(i==-1) {
			handler.sendClientMsg("{system.error} BadCommand "+command);
			return;
		}
		int j = command.indexOf(" ",i+1);
		if(j==-1) j = command.length();
		id = command.substring(i+1, j);
		if(j!=command.length())
			message = command.substring(j+1);
		else
			message = "";

		ClientHandler toHandler = null;
		Iterator iterator = null;
		if(id.indexOf("@")!=-1) {
			toHandler = handler.getServer().findClientByKey(id);
			List list = new ArrayList();
			if(toHandler!=null) {
				logger.finest("Found to by Key: "+toHandler);
				list.add(toHandler);
			}
			iterator = list.iterator();
		} else {
			logger.finest("Finding to by Id");
			iterator = handler.getServer().findAllClientById(id);
		}

		if(iterator.hasNext()) {
			while(iterator.hasNext()) {
				handler.isConnected();//check if src is still alive
				toHandler = (ClientHandler) iterator.next();
				ChatMessaging.sendMessageBwUsers(handler, toHandler, message);
			}//end of while
		} else {
			handler.sendClientMsg("{system.error} MessageNotSent No Client by that id");
		} 
	}

	public static void sendRoomList(ClientHandler handler)
			throws SocketTimeoutException, IOException {
		ChatData data = (ChatData)handler.getClientData();
		Iterator iterator = 
			handler.getServer().findAllClientByKey(".+@"+data.getRoom());
		if(iterator.hasNext()) {
			while(iterator.hasNext()) {
				handler.isConnected();//check if src is still alive
				sendList(handler, (ClientHandler) iterator.next());
			}
		}
	}

	public static void sendList(ClientHandler toHandler, 
			ClientHandler handler) throws IOException {
		logger.finest("From: "+handler+"To: "+toHandler);
		if(handler==null) return;

		ChatData data = (ChatData)handler.getClientData();
		try	{
			toHandler.sendClientMsg("{user.list} "+data.getClientKey());	
		} catch(Exception e) {
			logger.finest("Error sending msg: "+e);
		}
	}
}
