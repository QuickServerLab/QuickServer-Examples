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

import java.net.*;
import java.io.*;
import java.util.StringTokenizer;
import org.quickserver.net.server.ClientEventHandler;
import org.quickserver.net.server.ClientBinaryHandler;
import org.quickserver.net.server.ClientHandler;
import org.quickserver.net.server.DataMode;
import org.quickserver.net.server.DataType;
import java.util.logging.*;

public class CommandHandler 
		implements ClientEventHandler, ClientBinaryHandler {
	private static Logger logger = Logger.getLogger(CommandHandler.class.getName());
	
	public void gotConnected(ClientHandler handler)
			throws SocketTimeoutException, IOException {
		logger.fine("Connection opened: "+handler.getHostAddress());
		/*
		//will pick form xml
		handler.setDataMode(DataMode.BINARY, DataType.IN);
		handler.setDataMode(DataMode.BINARY, DataType.OUT);
		*/
	}

	public void lostConnection(ClientHandler handler) 
			throws IOException {
		Data data=(Data)handler.getClientData();
		data.setClosed(true);
		data.preclean();
		logger.fine("Connection lost: "+handler.getHostAddress());
	}
	public void closingConnection(ClientHandler handler) 
			throws IOException {
		Data data=(Data)handler.getClientData();
		data.setClosed(true);
		data.preclean();
		logger.fine("Connection closed: "+handler.getHostAddress());
	}

	
	public void handleBinary(ClientHandler handler, byte command[])
			throws SocketTimeoutException, IOException {
		Data data=(Data)handler.getClientData();
		data.sendData(command);
	}
}
