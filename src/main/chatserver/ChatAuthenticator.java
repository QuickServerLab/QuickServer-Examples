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

import org.quickserver.net.server.*;
import java.io.*;
import java.util.*;
import org.quickserver.net.*;

/**
 *
 * @author  Akshathkumar Shetty
 */
public class ChatAuthenticator extends QuickAuthenticationHandler {
	public AuthStatus askAuthentication(ClientHandler handler) 
			throws IOException, AppException {
		ChatData data = (ChatData) handler.getClientData();
		data.setLastAsked("U");
		handler.sendClientMsg("{system.data} Username");
		return null;
	}

	public AuthStatus handleAuthentication(ClientHandler handler, String command) 
			throws IOException, AppException {
		ChatData data = (ChatData)handler.getClientData();

		if(data.getLastAsked().equals("U")) {
			data.setUsername(command);
			data.setLastAsked("P");
			handler.sendClientMsg("{system.data} Password");
			return null;
		}
		
		if(data.getLastAsked().equals("P")) {
			data.setPassword(command.getBytes());
			data.setLastAsked("R");
			handler.sendClientMsg("{system.data} Room");
			return null;
		}
		
		if(data.getLastAsked().equals("R")) {
			if(data.registerUsername(data.getUsername())==false) {
				handler.sendClientMsg("{system.error} AuthFailed. Username taken!");
				data.setUsername(null);
				return AuthStatus.FAILURE;
			}

			if(validate(handler, data.getUsername(), data.getPassword())) {
				data.setRoom(command);
				handler.sendClientMsg("{system.ok} Auth Ok");
				handler.sendClientMsg("{system.msg} Current Chat Room: "+data.getRoom());
				data.setPassword(null);				

				ChatMessaging.sendInfoMessage2Room(handler, data.getRoom(), "LoggedIn");
				ChatMessaging.printHelp(handler, null);

				return AuthStatus.SUCCESS;
			} else {
				handler.sendClientMsg("{system.error} AuthFailed");
				data.deregisterUsername(data.getUsername());
				data.setPassword(null);
				return AuthStatus.FAILURE;
			}
		} else {
			throw new AppException("Unknown LastAsked!");
		}
	}

	/**
	 * This function is used to validate username and password.
	 * May be overridden to change username and/or password.
	 */ 
	protected static boolean validate(ClientHandler handler, String username, byte[] password) {
		return Arrays.equals(password,username.getBytes());
	}
	
	/*
	//simpler edition of ChatAuthenticator, that would extend QuickAuthenticator 
	public boolean askAuthorisation(ClientHandler handler) 
			throws IOException {		
		String username = askStringInput(handler, "{system.data} Username");
		String password = askStringInput(handler, "{system.data} Password");
		String room = askStringInput(handler, "{system.data} Room");

		//no need to check username or username for null : done by QuickAuthenticator
		
		if(username.equals(password)) {
			ChatData cd = (ChatData)handler.getClientData();
			try {
				cd.setUsername(username);
			} catch(IllegalArgumentException iae) {
				sendString(handler, "{system.error} AuthFailed. "+iae.getMessage());
				return false;
			}
			
			if(room.length()==0) room = "home";

			cd.setRoom(room); //"home"

			sendString(handler, "{system.ok} Auth Ok");
			sendString(handler, "{system.msg} Current Chat Room: "+cd.getRoom());

			ChatMessaging.sendInfoMessage2Room(handler, room, "LoggedIn");

			ChatMessaging.printHelp(handler, null);
			
			return true;
		} else {
			sendString(handler, "{system.error} AuthFailed");
			return false;
		}
	}
	*/
}
