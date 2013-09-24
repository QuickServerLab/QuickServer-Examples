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

import org.quickserver.net.*;
import org.quickserver.net.server.*;

import java.io.*;
import java.util.*;
import java.util.logging.*;

public class FtpServer {
	public static String version = "0.3";

	public static void main(String args[])	{
		QuickServer ftpServer = new QuickServer();
		
		String confFile = "conf"+File.separator+"FtpServer.xml";
		Object config[] = new Object[] {confFile};
		if(ftpServer.initService(config) == true) {
			try	{
				ftpServer.startServer();
				ftpServer.startQSAdminServer();				
			} catch(AppException e){
				System.out.println("Error in server : "+e);
				e.printStackTrace();
			}
		}
	}

	private static void usage() {
		System.err.println(
			"---------------------------\n"+
			"QuickFTPServer v "+version+" Usage \n"+
			"---------------------------\n"+
			"ftpserver help\n"+
			"\tDisplays this help screen.\n"+
			"ftpserver [ftp_root_dir]\n"+
			"\tDefault are \n"+
			"\t Dir  : User Home Dir");
	}
}
