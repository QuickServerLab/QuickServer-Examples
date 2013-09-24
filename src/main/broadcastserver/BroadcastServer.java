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

package broadcastserver;

import org.quickserver.net.*;
import org.quickserver.net.server.*;

import java.io.*;

public class BroadcastServer {
	public static void main(String s[])	{
		QuickServer server;		
		String confFile = "conf"+File.separator+"BroadcastServer.xml";

		try	{
			server = QuickServer.load(confFile);
		} catch(AppException e) {
			System.out.println("Error in server : "+e);
			e.printStackTrace();
		}
	}
}


