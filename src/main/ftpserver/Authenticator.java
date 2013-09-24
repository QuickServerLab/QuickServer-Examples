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

import org.quickserver.net.server.*;
import org.quickserver.net.AppException;
import java.io.*;
import java.util.*;

public class Authenticator extends QuickAuthenticator {

	public boolean askAuthorisation(ClientHandler clientHandler)
			throws IOException, AppException {
		
		String username = askStringInput(clientHandler, null); //USER <USERNAME>
		//no need to check for null : done by QuickAuthenticator

		if(username.equalsIgnoreCase("QUIT")) {
			sendString(clientHandler, "221 Logged out.");
			throw new AppException("Quit");
		}
		if( username.toUpperCase().startsWith("USER ") == false){
			sendString(clientHandler, "503 Bad sequence of command, USER required.");
			return false;
		} else {
			sendString(clientHandler, "331 User name okay, need password.");
		}

		String password = askStringInput(clientHandler, null);//PASS <PASSWORD>
		//no need to check for null : done by QuickAuthenticator

		if(password.equalsIgnoreCase("QUIT")) {
			sendString(clientHandler, "221 Logged out.");
			throw new AppException("Quit");
		}
		if( password.toUpperCase().startsWith("PASS ") == false ){
			sendString(clientHandler, "503 Bad sequence of command, PASS required.");
			return false;
		} 

		Data data = (Data)clientHandler.getClientData();
		data.username = username;
		/*
		sendString(clientHandler, "332 PASS password okay, need account.");
		data.account = in.readLine();
		if( data.account==null || 
			!data.account.toUpperCase().startsWith("ACCT ") ){
			sendString(clientHandler, "503 Bad sequence of command, ACCT required.");
			return false;
		} 
		*/
		
		if(username.toLowerCase().equals("user anonymous")) {	
			//data.root = (String)clientHandler.getServer().getStoreObjects()[0];
			HashMap appConfig = 
				clientHandler.getServer().getConfig().getApplicationConfiguration();
			String temp = null;
			if(appConfig!=null)
				temp = (String)appConfig.get("FTP_ROOT");
			else
				temp = System.getProperty("user.home");
			data.root = temp;
			sendString(clientHandler, "230 User logged in, proceed.");
			return true;
		} else {
			sendString(clientHandler, "530 Not logged in.");
			return false;
		}
	}
}
