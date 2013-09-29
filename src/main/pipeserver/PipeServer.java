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

//import java.net.*;
import java.io.*;
import java.util.StringTokenizer;
import org.quickserver.net.*;
import org.quickserver.net.server.*;

import java.util.logging.*;
import org.quickserver.util.logging.*;

/**
 * A simple redirect server.
 */

public class PipeServer {
	public static void main(String args[])	{
		QuickServer myServer = new QuickServer();		
		//setup logger to log to file
		Logger logger = null;
		FileHandler txtLog = null;
		File log = new File("./log/");
		if(!log.canRead())
			log.mkdir();
		try	{
			logger = Logger.getLogger("org.quickserver");
			logger.setLevel(Level.FINEST);
			txtLog = new FileHandler("log/PipeServer_QuickServer.txt");
			txtLog.setFormatter(new SimpleTextFormatter());
			logger.addHandler(txtLog);

			logger = Logger.getLogger("pipeserver");
			logger.setLevel(Level.FINEST); 
			txtLog = new FileHandler("log/PipeServer.txt");
			txtLog.setFormatter(new SimpleTextFormatter());
			logger.addHandler(txtLog);			

			myServer.setAppLogger(logger); //imp
		} catch(IOException e){
			System.err.println("Could not create txtLog FileHandler : "+e);
		}
		//end of logger code
		String confFile = "."+File.separator+".."+File.separator +"conf"+
			File.separator+"PipeServer.xml";
		Object config[] = new Object[] {confFile};
		if(myServer.initService(config)==true) {
			try	{
				myServer.startServer();
				myServer.startQSAdminServer();
			} catch(AppException e){
				System.err.println("Error in server : "+e);
				e.printStackTrace();
			}
		}
	}
}
