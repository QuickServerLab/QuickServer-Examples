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

package pipeserver;

import org.quickserver.net.server.*;
import org.quickserver.net.AppException;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;

public class Authenticator extends QuickAuthenticator {
	private static Logger logger = Logger.getLogger(Authenticator.class.getName());
	private static boolean init = false;

	private static String remoteHost = "127.0.0.1";
	private static int remotePort = 8080;

	public boolean askAuthorisation(ClientHandler clientHandler)
			throws IOException, AppException {
		if(init==false) {
			init(clientHandler);
		}

		Data data = (Data) clientHandler.getClientData();
		data.setRemoteHost(remoteHost);
		data.setRemotePort(remotePort);
		try {
			data.init(new Socket(data.getRemoteHost(), 
				data.getRemotePort()), clientHandler);
			return true;
		} catch(Exception e) {
			logger.severe("Error : "+e);
			return false;
		}		
	}

	private static void init(ClientHandler clientHandler) {
		HashMap appConfig = clientHandler.getServer().getConfig().getApplicationConfiguration();
		if(appConfig!=null) {
			String temp = null;
			try	{
				temp = (String) appConfig.get("REMOTE_HOST");
				if(temp!=null) remoteHost = temp;

				temp = (String) appConfig.get("REMOTE_PORT");
				if(temp!=null) remotePort = Integer.parseInt(temp);

				init = true;
			} catch(Exception e) {
				logger.severe("Error loading app. properties : "+e);
			}		
		}
	}
}
