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

package dateserver;

import org.quickserver.net.*;
import org.quickserver.net.server.*;

import java.io.*;
import java.util.logging.*;

/**
 * Example demonstrates how to send java object over the socket.
 */
public class DateServer {
	public static String VER = "1.0";

	public static void main(String s[])	{
		String cmdHandle = "dateserver.CommandHandler";
		String objHandle = "dateserver.ObjectHandler";
		String auth = null;

		QuickServer myServer = new QuickServer(cmdHandle);
		myServer.setClientAuthenticationHandler(auth);
		myServer.setPort(8125);
		myServer.setName("Date Server v " + VER);
		myServer.setClientObjectHandler(objHandle);

		//ony blocking mode is supported for exchanging Object
		myServer.getBasicConfig().getServerMode().setBlocking(true); 
		myServer.getBasicConfig().getAdvancedSettings().setDebugNonBlockingMode(true);
		myServer.getBasicConfig().setCommunicationLogging(true);

		//setup logger to log to file
		Logger logger = null;
		FileHandler txtLog = null;
		File log = new File("./log/");
		if(!log.canRead())
			log.mkdir();
		try	{
			logger = Logger.getLogger("");
			logger.setLevel(Level.FINEST);

			logger = Logger.getLogger("dateserver");
			logger.setLevel(Level.FINEST);
			
			txtLog = new FileHandler("log/DateServer.txt");
			txtLog.setLevel(Level.FINEST); 
			txtLog.setFormatter(new org.quickserver.util.logging.SimpleTextFormatter());
			logger.addHandler(txtLog);
			
			myServer.setAppLogger(logger); //imp

			//myServer.setConsoleLoggingToMicro();
			myServer.setConsoleLoggingFormatter(
				"org.quickserver.util.logging.SimpleTextFormatter");
			myServer.setConsoleLoggingLevel(Level.WARNING);
		} catch(Exception e){
			System.err.println("Could not create xmlLog FileHandler : "+e);
		}
		//end of logger code

		try	{
			myServer.startServer();	

			myServer.getQSAdminServer().setShellEnable(true);
			myServer.startQSAdminServer();			
		} catch(AppException e){
			System.out.println("Error in server : "+e);
			e.printStackTrace();
		}
	}

	public static float getQuickServerVersion() {
		return QuickServer.getVersionNo();
	}
}


