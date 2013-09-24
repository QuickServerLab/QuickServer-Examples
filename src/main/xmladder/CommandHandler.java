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

package xmladder;

import java.net.*;
import java.io.*;
import org.quickserver.net.server.ClientCommandHandler;
import org.quickserver.net.server.ClientEventHandler;
import org.quickserver.net.server.ClientHandler;
import org.quickserver.net.server.DataMode;
import org.quickserver.net.server.DataType;
import java.util.logging.*;

public class CommandHandler implements ClientEventHandler, ClientCommandHandler {
	private static Logger logger = Logger.getLogger(
			CommandHandler.class.getName());
	
	private final static String welcome = "<welcome name=\"XmlAdder v1.0\">"+
			"\n"+"<note>Send <quit /> to close connection</note>";

	//--ClientEventHandler
	public void gotConnected(ClientHandler handler)
			throws SocketTimeoutException, IOException {
		logger.fine("Connection opened : "+handler.getHostAddress());
		/*
		//pick from xml
		handler.setDataMode(DataMode.BYTE, DataType.IN);
		handler.setDataMode(DataMode.BYTE, DataType.OUT);
		*/
		handler.sendClientBytes(welcome);
	}

	public void lostConnection(ClientHandler handler) 
			throws IOException {
		logger.fine("Connection lost : "+handler.getHostAddress());
	}
	public void closingConnection(ClientHandler handler) 
			throws IOException {
		handler.sendSystemMsg("Connection closed: "+handler.getHostAddress(), Level.FINE);
	}
	//--ClientEventHandler

	public void handleCommand(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException {
		AddNumberReq addNumberReq = null;
		AddNumberRes addNumberRes = new AddNumberRes();
		command = command.trim();
		try {
			if(command.equals("<quit />")) {
				handler.sendClientBytes("<bye />");
				handler.closeConnection();
				return;
			}

			Data data = (Data)handler.getClientData();
			data.addXmlPart(command);
	
			String xmlToProcess = null;
			while(true) {
				xmlToProcess = data.getNextXML();
				if(xmlToProcess==null)
					break;
				
				logger.fine("Got xml to process from data \n:"+xmlToProcess);

				addNumberReq = AddNumberReq.fromXML(xmlToProcess);
				if(addNumberReq==null) {
					addNumberRes.setType("error");
					addNumberRes.setValue("Bad XML Got!");
				}
				
				int a = addNumberReq.getNumberA();
				int b = addNumberReq.getNumberB();
				int c = a + b;
				
				addNumberRes.setType("sum");
				addNumberRes.setValue(""+c);
				handler.sendClientBytes(addNumberRes.toXML());
			}
		} catch(IOException e) {
			throw e;
		} catch(Exception e) {
			addNumberRes.setType("error");
			addNumberRes.setValue("Exception : "+e);
			handler.sendClientBytes(addNumberRes.toXML());
		}
	}
}
