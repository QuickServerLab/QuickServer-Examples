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

import java.net.*;
import java.io.*;
import java.util.Date;
import org.quickserver.net.server.*;

public class CommandHandler implements ClientCommandHandler {

	public void gotConnected(ClientHandler handler)
		throws SocketTimeoutException, IOException {
		handler.sendSystemMsg("Connection opened : "+
			handler.getSocket().getInetAddress());

		handler.sendClientMsg("Welcome to DateServer v " + 
			DateServer.VER);
	}

	public void lostConnection(ClientHandler handler) 
		throws IOException {
		handler.sendSystemMsg("Connection lost : " + 
			handler.getSocket().getInetAddress());
	}
	public void closingConnection(ClientHandler handler) 
		throws IOException {
		handler.sendSystemMsg("Connection closed : " + 
			handler.getSocket().getInetAddress());
	}

	public void handleCommand(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException {
		
		if(command.toLowerCase().equals("quit")) {
			handler.sendClientMsg("Bye ;-)");
			handler.closeConnection();
		} else if(command.toLowerCase().equals("exchange date")) {
			handler.setDataMode(DataMode.OBJECT, DataType.OUT);
			handler.sendClientObject(new Date());
			handler.setDataMode(DataMode.STRING, DataType.OUT);

			//will block until the client ObjectOutputStream 
			//has written and flushed the header.
			//we know our client will send date object
			//as soon as it recives our date its ok
			handler.setDataMode(DataMode.OBJECT, DataType.IN);			
		} else {
			handler.sendSystemMsg("Got cmd : " + command);
			handler.sendClientMsg("You Sent : " + command);
		}
	}
}
