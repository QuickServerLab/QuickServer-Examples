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

package broadcastserver;

import java.net.*;
import java.io.*;
import org.quickserver.net.server.ClientCommandHandler;
import org.quickserver.net.server.ClientHandler;
import org.quickserver.net.server.ClientEventHandler;
import java.util.logging.*;

public class CommandHandler implements ClientCommandHandler, ClientEventHandler {
	private static Logger logger = Logger.getLogger(CommandHandler.class.getName());

	//--ClientEventHandler
	public void gotConnected(ClientHandler handler)
			throws SocketTimeoutException, IOException {
		logger.fine("Connection opened: "+handler.getHostAddress());

		handler.sendClientMsg("# Welcome to BroadcastServer");
		handler.sendClientMsg("# Send 'quit' to exit");

		Broadcaster.getInstance().addClient(handler);
	}

	public void lostConnection(ClientHandler handler) 
			throws IOException {
		Broadcaster.getInstance().removeClient(handler);
		logger.log(Level.FINE, "Connection lost: {0}", handler.getHostAddress());
	}
	public void closingConnection(ClientHandler handler) 
			throws IOException {
		Broadcaster.getInstance().removeClient(handler);
		logger.log(Level.FINE, "Connection closed: {0}", handler.getHostAddress());
	}
	//--ClientEventHandler

	public void handleCommand(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException {
		if(command.toLowerCase().equals("quit")) {
			handler.closeConnection();
			return;
		}

		//broadcast
		Broadcaster.getInstance().addMsgToBroadcast(command);
	}
}
