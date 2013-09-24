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

package chatserver;

import java.net.*;
import java.io.*;
import java.util.*;
import org.quickserver.net.AppException;

/**
 * Makes Client that tests the ChatServer for large load
 * @author  Akshathkumar Shetty
 */
class TestClientThread extends Thread {
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	public static int counter = 0;
	private int id = counter++;
	private static int threadcount = 0;
	private Random random = new Random();

	public static int threadCount() { 
		return threadcount; 
	}


	public TestClientThread(InetAddress addr,int port) {
		threadcount++;
		try {
			socket =  new Socket(addr,port);
		} catch(IOException e) {
			System.err.println("Socket failed : "+e);
			return;
		}

		try {    
			in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			// Enable auto-flush:
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), false);
			start();
		} catch(IOException e) {
			System.out.println("Socket Error : "+e);
			try {
				socket.close();
			} catch(IOException e2) {
				System.err.println("Socket not closed");
			}
		}
	}

	public void run() {
		//System.out.println("Client " + id+" ");
		try {
			Thread.sleep(200);

			String str;		
			//to skip the banner
			str = in.readLine();
			if(str==null) throw new IOException("Lost connection!");
			in.readLine();
			in.readLine();

			Thread.sleep(500);

			//auth
			in.readLine();
			out.println("User-"+id);out.flush();
			in.readLine();
			out.println("User-"+id);out.flush();
			in.readLine();
			out.println("home");out.flush();
			in.readLine(); //Auth Ok


			//skip help
			for(int i = 0; i < 7; i++) {
				in.readLine();
			}
	
			do {
				str = in.readLine();
				if(str==null)
					throw new IOException("Lost connection!");
				if(str.startsWith("{system.help}")==false && 
						str.startsWith("{user.info:")==false) {
					System.out.println("["+id+"] Got: "+str);
				}
				if(str.endsWith("quit")){
					socket.close();
					socket = null;
				} else if(str.indexOf("sendBack ")!=-1){
					out.print(str.substring(str.indexOf("sendBack ")+9));
					out.print("\r\n");
					out.flush();
				}
			} while(socket!=null);

		} catch(IOException e) {
			System.err.println("["+id+"] IO Exception : "+e);
		} catch(Exception e) {
			System.err.println("["+id+"] Exception : "+e);
		} finally {
			// Always close it:
			try {
				if(socket!=null) socket.close();
			} catch(IOException e) {
				System.err.println("Socket not closed");
			}
			threadcount--; // Ending this thread
		}
	}
}

public class TestChatServer {
	public static int MAX_THREADS = 100;
	private static Random random = new Random();
	
	//parsm: port ip maxClient startid
	public static void main(String[] args) 
			throws IOException, InterruptedException {
		int port=7412;
		InetAddress addr = InetAddress.getByName(null);

		if(args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch(NumberFormatException nfe) {}
		}
		
		if(args.length>=2) {
			addr = InetAddress.getByName(args[1]);
		}		

		if(args.length>=3) {
			try {
				MAX_THREADS = Integer.parseInt(args[2]);
			} catch(NumberFormatException nfe) {}
		}

		if(args.length>=4) {
			try {
				TestClientThread.counter = Integer.parseInt(args[3]);
			} catch(NumberFormatException nfe) {}
		}

		do {
			if(TestClientThread.threadCount() < MAX_THREADS)
				new TestClientThread(addr,port);
			else
				break;
			Thread.currentThread().sleep(getSleepTime());					
		} while(true);
		System.out.println("All "+MAX_THREADS+" clients are ready!");
	}

	private static int getSleepTime() {
	  return 200;
	}
} 
