/*
 * This file is part of the QuickServer library 
 * Copyright (C) QuickServer.org
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

package echoserver;

import org.quickserver.net.server.*;
import java.io.*;
import java.util.*;
import org.quickserver.net.AppException;

/**
 * Needs ClientData
 */
public class EchoAuthenticatorHandler extends QuickAuthenticationHandler {
	public AuthStatus askAuthentication(ClientHandler handler) 
			throws IOException, AppException {
		Data data = (Data) handler.getClientData();
		data.setLastAsked("U");
		handler.sendClientMsg("User Name :");
		return null;//no AuthStatus yet
	}

	public AuthStatus handleAuthentication(ClientHandler handler, String command) 
			throws IOException, AppException {
		Data data = (Data)handler.getClientData();

		if(data.getLastAsked().equals("U")) {
			data.setUsername(command);
			data.setLastAsked("P");
			handler.sendClientMsg("Password :");
		} else if(data.getLastAsked().equals("P")) {
			data.setPassword(command.getBytes());
			
			if(validate(handler, data.getUsername(), data.getPassword())) {
				handler.sendClientMsg("Auth OK");
				data.setPassword(null);
				return AuthStatus.SUCCESS;
			} else {
				handler.sendClientMsg("Auth Failed");
				data.setPassword(null);
				return AuthStatus.FAILURE;
			}
		} else {
			throw new AppException("Unknown LastAsked!");
		}

		return null;//no AuthStatus yet
	}

	/**
	 * This function is used to validate username and password.
	 * May be overridden to change username and/or password.
	 */ 
	protected static boolean validate(ClientHandler handler, String username, byte[] password) {
		return Arrays.equals(password,username.getBytes());
	}
}
