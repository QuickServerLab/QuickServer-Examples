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

import java.io.*;
import java.net.*;
import java.util.*;

public class XmlAdderClient {
	private static boolean debug = false;
	private static boolean brokenReq = false;

	static {
		debug = Boolean.getBoolean("xmladder.XmlAdderClient.bebug");
		brokenReq = Boolean.getBoolean("xmladder.XmlAdderClient.brokenReq");
	}

	private String host = "127.0.0.1";
	private int port = 2222;
	private long time = -1;


	public XmlAdderClient() {}

	public XmlAdderClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public long getTime() {
		return time;
	}

	public void test() {
		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;
		Socket socket = null;

		if(debug) System.out.println("Connecting.. ");
		long stime = System.currentTimeMillis();
		try {
			socket = new Socket(host, port);
			bis  = new BufferedInputStream(socket.getInputStream());
			bos  = new BufferedOutputStream(socket.getOutputStream());

			if(debug) {
				System.out.println("========== Got ==========");
				System.out.println(readInputStream(bis)+"\n");
			} else {
				readInputStream(bis);
			}

			int a = 14;
			int b = 9;
			AddNumberReq addNumberReq = new AddNumberReq();
			addNumberReq.setNumberA(a);
			addNumberReq.setNumberB(b);
			String msg = addNumberReq.toXML();

			if(debug) {
				System.out.println("======== Sending ========\n");
				System.out.println(msg+"\n");
			}

			bos.write(msg.getBytes(),0,msg.length());
			bos.flush();


			String msg11 = "<add-number-req><number-a>1</number-a>";
			String msg12 = "<number-b>12</number-b></add-number-req>";

			if(brokenReq) {
				if(debug) {
					System.out.println("======== Sending ========\n");
					System.out.println(msg11+"\n");
				}

				bos.write(msg11.getBytes(),0,msg11.length());
				bos.flush();
			}

			if(debug) {
				System.out.println("========== Got ==========\n");
				System.out.println(readInputStream(bis)+"\n");
			} else {
				readInputStream(bis);
			}

			if(brokenReq) {
				if(debug) {
					System.out.println("======== Sending ========\n");
					System.out.println(msg12+"\n");
				}

				bos.write(msg12.getBytes(),0,msg12.length());
				bos.flush();

				if(debug) {
					System.out.println("========== Got ==========\n");
					System.out.println(readInputStream(bis)+"\n");
				} else {
					readInputStream(bis);
				}
			}

			msg = "<quit />";

			if(debug) {
				System.out.println("======== Sending ========\n");
				System.out.println(msg+"\n");
			}

			bos.write(msg.getBytes(),0,msg.length());
			bos.flush();

			if(debug) {
				System.out.println("========== Got ==========\n");
				System.out.println(readInputStream(bis)+"\n");
			} else {
				readInputStream(bis);
			}

			if(debug) System.out.println("Closing socket.");

			bos.close();
			bis.close();			
		} catch(Exception e) {
			System.err.println("Error ("+host+":"+port+")" + e);
		} finally {
			try {
				if(socket!=null)
					socket.close();
			} catch(Exception er) {
					System.err.println("Error closing socket: " + er);
			}
		}
		long etime = System.currentTimeMillis();
		time = etime - stime;
	}

	private static String readInputStream(BufferedInputStream bin) 
			throws IOException {
		if(bin==null) {
			return null;
		}
		byte data[] = null;
		int s = bin.read();
		if(s==-1) {
			return null; //Connection lost
		}
		int alength = bin.available();
		if(alength > 0) {
			data = new byte[alength+1];
			
			bin.read(data, 1, alength);
		} else {
			data = new byte[1];
		}
		data[0] = (byte)s;
		return new String(data);
	}

	public static void main(String args[]) {
		XmlAdderClient client = null;

		if(args.length!=0 && args.length<2) {
			System.err.println("Usage : "+
			"\n XmlAdderClient <ip_address> <port>");
			System.exit(0);
		}

		if(args.length==2) {
			try {
				int p = Integer.parseInt(args[1]);
				client = new XmlAdderClient(args[0], p);
			} catch(Exception e) {
				System.err.println("Error " + e);
				return;
			}
		} else {
			client = new XmlAdderClient();
		}
		client.test();
		System.out.println("Time Taken: "+client.getTime()+"ms");
	}
}
