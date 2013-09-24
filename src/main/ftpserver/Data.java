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

import org.quickserver.util.pool.PoolableObject;
import org.apache.commons.pool.BasePoolableObjectFactory; 
import org.apache.commons.pool.PoolableObjectFactory; 

import java.io.*;
import java.net.*;
import java.util.logging.Logger;

public class Data implements ClientData, PoolableObject {
	private static Logger logger = 
		Logger.getLogger(Data.class.getName());
	boolean isTransferring = false;
	boolean isRenameFrom = false;
	boolean isStop = false;
	boolean isPassive = false;

	Socket socket = null;
	ServerSocket server = null; //if passive	
	int socketPort = -1;
	int serverPort = -1;
	boolean closeDataServer = true;
	String ip = null;

	//pipelining 
	boolean wasQuit = false;
	boolean isProcessing = false;
	int noOfBytesTransferred = -1;
	int startPosition = 0; //for REST 

	String root = null; //no ending slash \
	String wDir = "/"; //WORKING DIRECTORY

	String account = null; //ACCOUNT
	String username = ""; //USERNAME
	boolean binary = false;
	char type = 'A'; //ASCII,IMAGE	
	char typeSub = 'N'; //Non-print,
	String mode = "Stream"; //Stream
	String structure = "File"; //File, Record

	public void sendFile(String file) throws Exception {
		isStop = false;
		if(socket==null)
			throw new IOException("No client connected");
		String input = "";
		int i=0;
		FileInputStream fin = null;
		BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream(socket.getOutputStream());
			fin = new FileInputStream(file);
			/*
			if(data.type == 'A') {
				PrintWriter rout=new PrintWriter(out);
				BufferedReader br = new BufferedReader(new InputStreamReader(fin));
				while(true) {
					input = br.readLine();
					if(input==null)
						break;
					rout.
				}
				rout.flush();
				//rout.close();
			} else {
				
			}*/
			while(true) {
				i = fin.read();
				if(i==-1 || isStop==true) //if aborted
					break;
				out.write(i);
			}
			out.flush();
		}catch (Exception e){
			throw e;
		} finally {
			if(fin!=null)
				fin.close();
		}
	}

	public void sendData(String sdata) throws Exception {
		//System.out.print("Sending data on datacon ");
		if(socket==null) {
			Thread.currentThread().sleep(500);
			if(socket==null) {
				throw new IOException("No client connected");
			}
		}
		BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream(socket.getOutputStream());
			out.write( sdata.getBytes(),0,sdata.length() );
			out.flush();
		}catch (Exception e){
			throw e;
		}
	}

	public void startDataServer(ServerSocket acceptSocket, ftpserver.Data data) {
		new DataServer(acceptSocket, data);
	}

	//-- PoolableObject
	public void clean() {
		isTransferring = false;
		isRenameFrom = false;
		isStop = false;
		isPassive = false;
		socket = null;
		server = null;
		socketPort = -1;
		serverPort = -1;
		closeDataServer = true;
		ip = null;
		wasQuit = false;
		isProcessing = false;
		noOfBytesTransferred = -1;
		startPosition = 0;
		root = null;
		wDir = "/";
		account = null;
		username = "";
		binary = false;
		type = 'A'; 
		typeSub = 'N';
		mode = "Stream";
		structure = "File";
	}

	public boolean isPoolable() {
		return true;
	}

	public PoolableObjectFactory getPoolableObjectFactory() {
		return  new BasePoolableObjectFactory() {
			public Object makeObject() { 
				return new Data();
			} 
			public void passivateObject(Object obj) {
				Data ed = (Data)obj;
				ed.clean();
			} 
			public void destroyObject(Object obj) {
				if(obj==null) return;
				passivateObject(obj);
				obj = null;
			}
			public boolean validateObject(Object obj) {
				if(obj==null) 
					return false;
				else
					return true;
			}
		};
	}
}

class DataServer extends Thread {
	private static Logger logger = 
		Logger.getLogger(DataServer.class.getName());
	
	ServerSocket acceptSocket=null;
	ftpserver.Data data = null;
	public DataServer(ServerSocket acceptSocket, ftpserver.Data data) {
		super("DataServer");
		this.acceptSocket = acceptSocket;
		this.data = data;
		setDaemon(true);
		this.data.isStop = false;
		start();
	}

	public void run() {
		try {
			data.closeDataServer = false;
			data.socket = acceptSocket.accept();
			logger.fine("Data con opened.");
			while(data!=null && data.closeDataServer == false 
				&& data.isStop==false ) {
				Thread.sleep(300);
			}
			if(data.socket!=null) {
				data.socket.close();
			}
		} catch(IOException e) {
			System.err.println("Error at dataServer :"+e);
		} catch(InterruptedException e) {
			System.err.println("Thread Error at dataServer :"+e);
		} finally {
			try {
				acceptSocket.close();
			}catch(Exception re){
				System.err.println("BAD Error at dataServer :"+re);
			}
		}
		data.socket = null;
		data.server = null;
	}

	
}



