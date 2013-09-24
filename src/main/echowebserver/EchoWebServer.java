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

package echowebserver;

import java.io.*;
import java.util.StringTokenizer;
import org.quickserver.net.*;
import org.quickserver.net.server.*;

import java.util.logging.*;
import org.quickserver.util.logging.*;

/**
 * A simple HTTP server that generates a Web page
 * showing all the data that it received from
 * the Web client.
 * @author Akshathkumar Shetty
 */
public class EchoWebServer {
	public static void main(String args[])	{
		QuickServer myServer = new QuickServer();		
		String confFile = "conf"+
			File.separator+"EchoWebServer.xml";
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
