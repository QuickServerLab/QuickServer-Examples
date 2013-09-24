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

import java.io.*;
import java.net.*;

/**
 * EchoServer - readLine() testing under non-blocking mode.
 * @author Akshathkumar Shetty
 */
public class TestEchoServerReadLine {    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    
    public boolean testEchoServer(String host, int port, String newLine) {
        try {
            socket =  new Socket(host, port);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(), "UTF-8")));
            
            //banner
            for(int i=0;i<5;i++)
                in.readLine();
            
            //username
            in.readLine();
            out.print("user");
            out.print(newLine);
            out.flush();
            
            //password
            in.readLine();
            out.print("user1");
            out.flush();
            Thread.sleep(2000);
            out.print(newLine);
            out.flush();
            
            //Auth Failed
            in.readLine();
            
            //username
            in.readLine();
            out.print("user");
            out.print(newLine);
            out.flush();
            
            //password
            in.readLine();
            out.print("user");
            Thread.sleep(200);
            out.print(newLine);
            out.flush();
            
            //ok
            in.readLine();
            
            out.print("test string");
            out.flush();
            Thread.sleep(6000);
            out.print(newLine);
            out.flush();
            String data = in.readLine();
            if(data.equals("Echo : test string")==false)
                System.err.println("Not equals!");
            
            out.print("test string1");
            out.print(newLine);
            out.print("test string2");
            out.print(newLine);
            out.flush();
            data = in.readLine();
            if(data.equals("Echo : test string1")==false)
                System.err.println("Not equals=1!");
             data = in.readLine();
            if(data.equals("Echo : test string2")==false)
                System.err.println("Not equals=2!");
             
            //quit
            out.print("quit");
            out.print(newLine);
            out.flush();
            data = in.readLine();
            if(data.equals("Bye ;-)")==false)
                System.err.println("bye not got!");
            
             return true;
        } catch(Exception e) {
            System.err.println("Socket failed : "+e);
            return false;
        } finally {
            try {
                if(socket!=null) socket.close();
            } catch(IOException e) {
                System.err.println("Socket not closed: "+e);
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
		TestEchoServerReadLine main = new TestEchoServerReadLine();

		int port = 4123;
		String host = "localhost";

		if(args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch(NumberFormatException nfe) {}
		}
		
		if(args.length>=2) {
			host = args[1];
		}

		System.out.print("\\r\\n Test: ");
		System.out.println(main.testEchoServer(host, port, "\r\n"));
		Thread.sleep(30000);

		System.out.print("\\n Test: ");
		System.out.println(main.testEchoServer(host, port, "\n"));

		Thread.sleep(30000);
		System.out.println("\\r Test: ");
		System.out.println(main.testEchoServer(host, port, "\r"));
    }
    
}
