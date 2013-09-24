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

package cmdserver;

import org.quickserver.net.*;
import org.quickserver.net.server.*;

import java.io.*;
import java.util.logging.*;

public class CmdServer {
	public static String VER = "1.2";
	public static void main(String s[])	{
		Logger logger = Logger.getLogger("");
		logger.setLevel(Level.FINEST);

		String cmdHandle = "cmdserver.CmdCommandHandler";
		String data = "cmdserver.CmdData";
		String auth = null;

		QuickServer cmdServer=new QuickServer(cmdHandle);
		cmdServer.setClientAuthenticationHandler(auth);
		cmdServer.setClientData(data);
		cmdServer.setPort(23);
		cmdServer.setName("Cmd Server v "+VER);

		//check if cmd args was passed
		if(s.length>0) {
			Object[] store = new Object[]{s[0]};
			cmdServer.setStoreObjects(store);
		}

		//start admin server with default auth
		QuickServer adminServer = cmdServer.getQSAdminServer().getServer();
		adminServer.setServerBanner("QSAdminServer Started on port : "+124);
		adminServer.setCommunicationLogging(false);
		try	{
			cmdServer.startServer();
			cmdServer.startQSAdminServer(124, null);
		} catch(AppException e){
			System.out.println("Error in server : "+e);
			e.printStackTrace();
		}
	}
}


