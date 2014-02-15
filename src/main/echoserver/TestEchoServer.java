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

package echoserver;

import java.net.*;
import java.io.*;
import java.util.*;
import org.quickserver.net.AppException;

// Client that tests the EchoServer for all valid requests

class MyTestClientThread extends Thread {
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private static int counter = 0;
	private int id = counter++;
	private static int threadcount = 0;
	private Random random = new Random();

	public static int clientJunkCount = 100;
	public static int clientJunkSleep = 60*3; //sec

	String send[]={"UserPASS","UserPASS","Hello","World","Hi","Last"};
	String expectedFL[]={"Bye ;-)"};
	String expected[]={"User Name :","Password :","Auth OK", 
	  "Echo : Hello","Echo : World","Echo : Hi","Echo : Last"};

	private String getJunk() {
		StringBuffer sb = new StringBuffer();
		int l = random.nextInt(1000);
		char c = ' ';
		for(int i=0;i<l;i++) {
			c = (char) (33+random.nextInt(92));
			sb.append(c);
		}
		return sb.toString();
	}

	private boolean isDead = false; 

	public boolean getIsDead() {
		return isDead;
	}

	public static int threadCount() { 
		return threadcount; 
	}

	private int getSleepTime() {
		return (random.nextInt(20)+5)*300-(random.nextInt(10)+6)*100;
	}

	public MyTestClientThread(InetAddress addr,int port) {
		threadcount++;
		try {
			socket =  new Socket(addr,port);
		} catch(IOException e) {
			System.err.println("Socket failed : "+e);
			return;
		}

		try {    
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// Enable auto-flush:
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);
			start();
		} catch(IOException e) {
			System.out.println("Socket Error [id#"+id+"]: "+e);
			try {
				socket.close();
			} catch(IOException e2) {
				System.err.println("Socket not closed [id#"+id+"]");
			}
		}
	}

	public void astertEqual(String got,String with) {
		//System.out.println("Got : "+got+",with : "+with);
		if(got==null || got.equals(with)==false)
			throw new RuntimeException("Test Exception [id#"+id+"] : Expected : ->"+with+"<- Got->"+got+"<-");
	}

	public void run() {
		System.out.println("Client " + id+" ");
		try {
			String str;		
			//to skip the banner
			in.readLine();
			Thread.yield();
			in.readLine();
			if(timeToDie()) {
				Thread.yield();
				socket.close(); return;
			}
			in.readLine();
			in.readLine();
			Thread.yield();
			if(timeToDie()) {
				Thread.yield();
				socket.close(); return;
			}
			in.readLine();

			//username & password
			for(int i = 0; i < 2; i++) {
				str = in.readLine();
				astertEqual(str,expected[i]);
				if(timeToDie()) {
					Thread.yield();
					socket.close(); return;
				}
				this.sleep(500);
				out.println(send[i]);
			}
			
			Thread.yield();
			str = in.readLine();
			astertEqual(str,expected[2]);
			Thread.yield();
			if(timeToDie()) {
				socket.close(); return;
			}

			for(int i = 2; i < send.length; i++) {
				if(timeToDie()) {
					socket.close(); return;
				}
				out.println(send[i]);
				str = in.readLine();
				astertEqual(str,expected[i+1]);
				this.sleep(getSleepTime());
			}

			String junk = null;
			for(int k=0;k<clientJunkCount;k++) {
				junk = getJunk();
				out.println(junk);
				str = in.readLine();
				astertEqual(str,"Echo : "+junk);
				this.sleep(1000*clientJunkSleep);
			} 

			Thread.yield();
			out.println("Quit");

			if(timeToDie()) {
				socket.close(); return;
			}
			Thread.yield();

			str = in.readLine();
			astertEqual(str,expectedFL[0]);
			// wait to close
			try {
				this.sleep(500);
			} catch(InterruptedException e) {
				System.err.println("Interrupted : "+e);
			}
		} catch(IOException e) {
			System.err.println("IO Exception [id#"+id+"]: "+e);
		} catch(InterruptedException e) {
			System.err.println("Interrupted [id#"+id+"]: "+e);
		} finally {
			// Always close it:
			try {
				if(socket!=null) socket.close();
			} catch(IOException e) {
				System.err.println("Socket not closed [id#"+id+"]");
			}
			threadcount--; // Ending this thread
			isDead = true;
		}
	}

	private boolean timeToDie() {
		int i = random.nextInt(1000);
		if(i%25==23 || i%95==48) {
			return true;
		}
		return false;
	}
}


public class TestEchoServer {
	public static int MAX_THREADS = 100;
	private static Random random = new Random();

	public static void main(String[] args) 
			throws IOException, InterruptedException {
		int port=4123;
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
				MyTestClientThread.clientJunkCount = Integer.parseInt(args[3]);
			} catch(NumberFormatException nfe) {}
		}

		if(args.length>=5) {
			try {
				MyTestClientThread.clientJunkSleep = Integer.parseInt(args[4]);
			} catch(NumberFormatException nfe) {}
		}
		
		System.out.println("=======================");
		System.out.println("Server: "+addr);
		System.out.println("Port: "+port);
		System.out.println("Threads: "+MAX_THREADS);
		System.out.println("Client Junk Count: "+MyTestClientThread.clientJunkCount);
		System.out.println("Client Sleep Time: "+MyTestClientThread.clientJunkSleep);
		System.out.println("=======================");
		
		new MyTestClientThread(addr,port);				
		while(true) {
			Thread.currentThread().sleep(getSleepTime());
			Thread.yield();
			if(MyTestClientThread.threadCount() < MAX_THREADS)
				new MyTestClientThread(addr,port);
		}
	}

	private static int getSleepTime() {
	  return random.nextInt(100)+(random.nextInt(20)+1)*200-random.nextInt(50);
	}
} 
