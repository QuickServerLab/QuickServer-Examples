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

package cmdserver;

import java.net.*;
import java.io.*;
import org.quickserver.net.server.ClientCommandHandler;
import org.quickserver.net.server.ClientHandler;
import java.util.logging.*;

public class CmdCommandHandler implements ClientCommandHandler {
	private static Logger logger = 
		Logger.getLogger(CmdCommandHandler.class.getName());

	public void gotConnected(ClientHandler handler)
		throws SocketTimeoutException, IOException {
		handler.sendSystemMsg("Connection opened : "+
			handler.getSocket().getInetAddress());

		handler.sendClientMsg("Welcome to CmdServer v "+CmdServer.VER);
		handler.sendClientMsg("Send 'quit server' to exit ");
	}

	public void lostConnection(ClientHandler handler) throws IOException {
		cleanup(handler);
		handler.sendSystemMsg("Connection lost : "+handler.getSocket().getInetAddress());
	}
	public void closingConnection(ClientHandler handler) throws IOException {
		cleanup(handler);
		handler.sendSystemMsg("Connection closed: "+handler.getSocket().getInetAddress());
	}

	public void handleCommand(ClientHandler handler, String command)
			throws SocketTimeoutException, IOException {
		CmdData data = (CmdData)handler.getClientData();
		if(data.process==null) {
			handler.sendClientMsg("Starting shell..");
			startProcess(handler);
		}
		if(command.toLowerCase().equals("quit server")) {
			handler.sendClientMsg("Bye ;-)");
			handler.closeConnection();
		} else {
			try {
				OutputStream out = data.process.getOutputStream();
				BufferedOutputStream b_out = new BufferedOutputStream(out);
				command = command+"\n";
				b_out.write(command.getBytes(),0,command.length());
				b_out.flush();
			} catch(Exception e) {
				handler.sendClientMsg("Error : "+e);
			}
		}
	}

	//////////////////
	// helper methods
	/////////////////
	private void startProcess(ClientHandler handler) 
			throws IOException {
		CmdData data = (CmdData)handler.getClientData();
		try	{
			Object[] store = handler.getServer().getStoreObjects();
			String process = "cmd.exe";
			if(store!=null);
				process = (String)store[0];
			data.process = Runtime.getRuntime().exec(process);	
		} catch (IOException e) {
			handler.sendClientMsg("Error"+e);
		}
		PReader preader = new PReader(handler);
		preader.start();
	}

	private void cleanup(ClientHandler handler) {
		CmdData data = (CmdData)handler.getClientData();
		if(data==null || data.process==null)
			return;
		data.process.destroy();
		data.process = null;
	}
}

/*
 * Class that keeps reading from the process and sends any data 
 * to client
 */
class PReader extends Thread {
	ClientHandler handler;
	private static Logger logger = 
		Logger.getLogger(PReader.class.getName());

	public PReader(ClientHandler handler){
		this.handler = handler;
	}

	public void run() {
		CmdData data = (CmdData)handler.getClientData();
		if(data.process==null) //if no process
			return;
		InputStream in = data.process.getInputStream();
		BufferedReader b_in = null;
		
		try {
			b_in = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		} catch(UnsupportedEncodingException e) {
			logger.warning("UTF-8 was not supported : "+e);
			b_in = new BufferedReader(new InputStreamReader(in));
		}
		

		String got=null;
		while(true){
			try	{
				got=b_in.readLine();
				if(got==null || handler==null)
					break;
				handler.sendClientMsg(got);
			} catch(NullPointerException e) {
				/*
				When client closes connection, 
				then cleanup was called, which destroyes the process,
				so b_in becames null. So we ignore the error.
				*/
				if(data.process!=null)
					logger.logp(Level.SEVERE, "PReader", "run",
						"Error in PReader : " + e);
			} catch(Exception e) {
				if(handler==null || data.process==null)
					break;
				try	{
					handler.isConnected();
					handler.sendClientMsg("Error "+e);
					break;
				} catch(SocketException se) {
					//socket was closed
				} catch(IOException ie) {
					logger.logp(Level.FINEST, "PReader", "run",
						"IOError in PReader : "+e+"\n\t"+ie);
				} catch(Exception ee) {
					logger.logp(Level.SEVERE, "PReader", "run",
						"Error in PReader : "+e+"\n\t"+ee);
				}
			}			
		}
	}
}
