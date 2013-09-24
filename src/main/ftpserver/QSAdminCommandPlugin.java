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


import java.io.*;
import java.net.SocketTimeoutException;
import org.quickserver.net.server.*;
import org.quickserver.net.qsadmin.*;
import java.util.*;
import org.quickserver.util.xmlreader.ApplicationConfiguration;

public class QSAdminCommandPlugin implements CommandPlugin {
	/**
	 * FTP Server QSAdminServer commands
	 * ----------------------------------
	 * set ftproot path
	 * get ftptoot
	 */
	public boolean handleCommand(ClientHandler handler,	String command)
		throws SocketTimeoutException, IOException {

		QuickServer ftpqs = (QuickServer) 
			handler.getServer().getStoreObjects()[0];

		if(command.toLowerCase().startsWith("set ftproot ")) {
			String temp = "";
			temp = command.substring("set ftproot ".length());
			ApplicationConfiguration appConfig = ftpqs.getConfig().getApplicationConfiguration();
			File root = new File(temp);
			if(root.canRead() && root.isDirectory()) {
				if(appConfig==null)
					appConfig = new ApplicationConfiguration();
				appConfig.put("FTP_ROOT", temp);
				ftpqs.getConfig().setApplicationConfiguration(appConfig);
				handler.sendClientMsg("+OK root changed");
			} else {
				handler.sendClientMsg("-ERR not a directory or can't read : "+temp);
			}
			return true;
		} else if(command.toLowerCase().equals("get ftproot")) {
			HashMap appConfig = ftpqs.getConfig().getApplicationConfiguration();
			String temp = null;
			if(appConfig!=null)
				temp = (String)appConfig.get("FTP_ROOT");
			else
				temp = System.getProperty("user.home");
			handler.sendClientMsg("+OK " + temp);
			return true;
		} else if(command.toLowerCase().equals("help")) {
			handler.sendClientMsg("+OK info follows");
			handler.sendClientMsg("Custom Commands:");
			handler.sendClientMsg("\tset ftproot <path> //Sets FTP root directory");
			handler.sendClientMsg("\tget ftproot        //Returns the current FTP root directory");
			handler.sendClientMsg(" ");
			handler.sendClientMsg("Standard Commands:");
			handler.sendClientMsg("\tRefer Api Docs for org.quickserver.net.qsadmin.CommandHandler");
			handler.sendClientMsg(".");
			return true;
		}
		return false;
	}
}
