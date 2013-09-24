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

package echoserver;

import org.quickserver.net.server.*;
import java.io.*;
import org.quickserver.net.AppException;

/**
 * Try to use EchoAuthenticatorHandler
 */
public class EchoServerQuickAuthenticator extends QuickAuthenticator {

	public boolean askAuthorisation(ClientHandler clientHandler) 
			throws IOException, AppException {
		String username = askStringInput(clientHandler, "User Name :");
		//no need to check for null done by QuickAuthenticator

		String password = askStringInput(clientHandler, "Password :");
		//no need to check for null done by QuickAuthenticator
		
		if(username.equals(password)) {
			sendString(clientHandler, "Auth OK");
			return true;
		} else {
			sendString(clientHandler, "Auth Failed");
			return false;
		}
	}
}
