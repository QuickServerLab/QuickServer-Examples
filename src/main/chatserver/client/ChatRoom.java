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

package chatserver.client;

import java.net.*;
import java.io.*;
import java.util.logging.*;
import java.util.*;

/**
 *
 * @author Akshathkumar Shetty
 */
public class ChatRoom {
    private static Logger logger = Logger.getLogger(ChatRoom.class.getName());
    
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private BufferedReader br;
    private BufferedWriter bw;
    private String room = "home";
    
    private volatile boolean connected = false;
    private volatile boolean loggedIn = false;
    
    //gui
    ChatWindow chatWindow = null;
    
    private LinkedList receivedMsg;
    
    /** Creates a new instance of ChatRoom */
    public ChatRoom() {
		this(null);
    }

	// args: room host_ip port
	// defaults: home 127.0.0.1 7412
	public ChatRoom(String args[]) {
		Logger _logger = Logger.getLogger("");
		_logger.setLevel(Level.FINEST);
		
		_logger = Logger.getLogger("chatserver.client");
		_logger.setLevel(Level.FINEST);

		if(args!=null && args.length>=1) setRoom(args[0]);
        chatWindow = new ChatWindow(this, args);		
        chatWindow.setVisible(true);
    }

	public String getRoom() {
		return room;
	}
    
	public void setRoom(String room) {
		this.room = room;
	}
    
    /**
	 * ChatRoom room ip port
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
		Logger logger = Logger.getLogger("chatserver.client");
		logger.setLevel(Level.FINEST);
		/*
		try {
			javax.swing.UIManager.setLookAndFeel(
				javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ee) {}
		*/
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
				if(args.length!=0)
	                new ChatRoom(args);
				else
                	new ChatRoom();
            }
        });
    }

	public void changeRoom(String newRoom) {
		 if(socket==null) chatWindow.setResponse("-ERR Not connected");
		 try {
		 	 sendCommand("changeRoom "+newRoom, true);
		 } catch(Exception e) {
			 chatWindow.setResponse("-ERR Error: "+e.getMessage());
		 }
		 setRoom(newRoom);
	}

	public void updateUserList() {
		sendCommand("userList", true);
	}
    
    public void doLogout() throws IOException {
        if(socket==null)
            throw new IllegalStateException("Not connected");
        sendCommand("quit", true);
		connected = false;
        clean();
    }
    
    public boolean doLogin(String ipAddress, int port,
            String username, String password) throws IOException {
        connected = false;
        loggedIn = false;
        chatWindow.log("Logging in to " + ipAddress + ":"+port);
        try	{
            socket = new Socket(ipAddress, port);
            connected = true;
            in = socket.getInputStream();
            out = socket.getOutputStream();
            br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            bw = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            startSocketListener();
            
            String res = null;
            
            res = sendCommunicationSilent(null, true);
            if(res.startsWith("{system.msg} ")==false)
                throw new IOException(res.substring(13));
            
            for(int i=0;i<2;i++){
                res = sendCommunicationSilent(null, true);
                if(res.startsWith("{system.help} ")==false)
                    throw new IOException(res.substring(14));
            }
            
            res = sendCommunicationSilent(null, true);
            if(res.startsWith("{system.data} ")==false)
                throw new IOException(res.substring(13));
            //gui.setResponse(res);
            
            //try to login
            res = sendCommunicationSilent(username, true);
            
            //password
            /*
            StringBuffer buffer = new StringBuffer();
            for(int i=0;i<password.length();i++)
                buffer.append('*');
            getGUI().appendToConsole(buffer.toString());
             */
            res = sendCommunicationSilent(password, false);
            res = sendCommunicationSilent(room, true);
            
            if(res.startsWith("{system.ok} ")) {
                chatWindow.log("Authorised");
				chatWindow.setTitle("QuickChat - Room: "+room+"@"+
					ipAddress+":"+port+" Logged as "+username);
				loggedIn = true;
                updateUserList();
            } else {
                loggedIn = false;
                //{system.error}
                chatWindow.log("Error : "+res);
                throw new IOException(res.substring(15));
            }
            return true;
        } catch(UnknownHostException e) {
            if(socket!=null) socket.close();
            chatWindow.log("Error "+e);
            connected = false;
            loggedIn = false;
            socket = null;
            in = null;
            out = null;
            br = null;
            bw = null;
            chatWindow.setResponse("-ERR Unknown Host : "+e.getMessage());
            return false;
        } catch(Exception e) {
            if(socket!=null) socket.close();
            chatWindow.log("Error "+e);
            connected = false;
            socket = null;
            in = null;
            out = null;
            br = null;
            bw = null;
            chatWindow.setResponse("-ERR "+e.getMessage());
            return false;
        }
    }
    
    public void startSocketListener() {
        receivedMsg = new LinkedList();
        Thread t = new Thread() {
            public void run() {
                String rec = null;
                chatWindow.log("Started: startSocketListener");
                while(true) {
                    try {
                        rec =  br.readLine();
                    } catch(IOException e) {
                        logger.warning("Error : "+e);						
                        if(isConnected()==true && loggedIn==true) {
							chatWindow.log("Error : "+e);
							chatWindow.addChatMessage("{system.error} "+e.getMessage());
							clean();
						}
                        break;
                    }
                    if(rec==null) {						
                        if(isConnected()==true) {
							chatWindow.log("Lost Connection!");
							chatWindow.addChatMessage("{system.error} Lost Connection!");
							clean();
						}
                        break;
                    }
                    receivedMsg.add(rec);
					chatWindow.log("R: "+rec);
                    if(loggedIn==true) {
						receivedMsg.remove(rec);
                        if(rec.startsWith("{user.list} "))
                            chatWindow.addToUserList(rec.substring(12));
                        else 
                            chatWindow.addChatMessage(rec);
                    }
                }
               chatWindow.log("Finished: startSocketListener");
            }
        };
		t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }
    
    public void sendCommand(String command, boolean echo) {
        logger.fine("Got command : "+command);
        if(isConnected()==false) {
            chatWindow.setResponse("-ERR Not connected yet.");
            return;
        }
        if(command!=null && command.equals("")==false) {
            if(socket==null)
                throw new IllegalStateException("Not connected");
            if(echo==true)
                chatWindow.log("S: "+command);
            command += "\r\n";
            try {
                bw.write(command, 0, command.length());
                bw.flush();
            } catch(Exception e) {
                chatWindow.setResponse("-ERR "+e.getMessage());
            }
        }
    }
    
    public synchronized String sendCommunicationSilent(String command,
            boolean echo) throws IOException {
        if(isConnected()==false)
            return "-ERR Not connected yet";
        if(socket==null)
            throw new IllegalStateException("Not connected");
        if(command!=null && command.equals("")==false) {
            logger.fine("Got command : "+command);
            if(echo==true)
                chatWindow.log("S: "+command);
            command += "\r\n";
            emptyReceivedMsg();
            bw.write(command, 0, command.length());
            bw.flush();
        }
        return readResponse();
    }
    
    public String getReceivedMsg() {
        while(receivedMsg.size()==0 && isConnected()==true) {
            try {
                Thread.currentThread().sleep(50);
            } catch(InterruptedException e) {
                logger.warning("Error : "+e);
            }
        }
        if(receivedMsg.size()!=0)
            return (String)receivedMsg.removeFirst();
        else
            return null;
    }
    
    public void emptyReceivedMsg() {
        receivedMsg.clear();
    }
    
	public void processReceivedMsg() {
		while(receivedMsg.size()!=0) {
	        chatWindow.addChatMessage( (String)receivedMsg.removeFirst() );
		}
    }
    
    public String readResponse() {
        return getReceivedMsg();
    }
    
    public boolean isConnected(){
        return  connected;
    }
    
    private void clean() {
        if(socket!=null) {
            try {
                socket.close();
            } catch(Exception e) {
                logger.warning("Error : "+e);
            }
            socket = null;
        }
        in = null;
        out = null;
        br = null;
        bw = null;
        connected = false;
		processReceivedMsg();
        loggedIn = false;
        chatWindow.enableChat(false);
    }
    
    public void sendMessage(String msg) {
        sendCommand("sendMsgToRoom "+room+" "+msg, true);
    }
    
    public void sendPrivateMessage(String userid, String msg) {
		if(userid==null) return;
		chatWindow.addChatMessage("{msg.user:"+userid+"} "+msg);
        sendCommand("sendMsg "+userid+" "+msg, true);		
    }

}
