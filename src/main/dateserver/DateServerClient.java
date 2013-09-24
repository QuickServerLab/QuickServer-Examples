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

package dateserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

public class DateServerClient {

	public static void main(String args[]) {
		Logger logger = Logger.getLogger("");
		logger.setLevel(Level.FINEST);
		logger.getHandlers()[0].setLevel(Level.FINEST);
		logger.getHandlers()[0].setFormatter(new org.quickserver.util.logging.SimpleTextFormatter());

		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;

		BufferedReader br = null;
		BufferedOutputStream bos = null;
		byte buffer[] = null;

		Socket socket;
		int port = 125;
		Date date;
		String send;
		if(args.length<2) {
			System.err.println("Usage : "+
			"\n DateServerClient ip_address port");
			System.exit(0);
		}
		try {
			port = Integer.parseInt(args[1]);
			socket = new Socket(args[0], port);

			logger.info("Connected to server.");

			br  = new BufferedReader(
				new InputStreamReader(socket.getInputStream(), "UTF-8"));
			bos  = new BufferedOutputStream(socket.getOutputStream());

			logger.info("Got : "+br.readLine());

			send = "hi server\r\n";
			buffer = send.getBytes("UTF-8");
			bos.write(buffer,0,buffer.length);
			bos.flush();

			logger.info("Got : "+br.readLine());

//for(int i=0;i<20;i++) {
	
			send = "exchange date\r\n";
			buffer = send.getBytes("UTF-8");
			bos.write(buffer,0,buffer.length);
			bos.flush();

			logger.fine("Opening ObjectInputStream..");
			ois = new ObjectInputStream(socket.getInputStream());
			logger.fine("Reading.. object..");
			date = (Date) ois.readObject();
			logger.fine("Done");
			logger.info("Got : Server Date : " + date);
			
			logger.fine("Opening getOutputStream..");
			oos = new ObjectOutputStream(bos);
			
			Object sendbj = new Date();
			logger.fine("Writing.. object..");
			oos.writeObject(sendbj);
			oos.flush();
			logger.fine("Done");

			sendbj = new String("back to string mode");
			logger.fine("Writing.. String object..");
			oos.writeObject(sendbj);
			oos.flush();
			logger.fine("Done");

			logger.fine("Reading.. String");
			logger.info("Got : "+br.readLine());

			logger.fine("Writing.. string..");
			send = "thanks for the date\r\n";
			buffer = send.getBytes("UTF-8");
			bos.write(buffer,0,buffer.length);
			bos.flush();

			logger.fine("Reading.. String");
			logger.info("Got : "+br.readLine());
//}
			logger.fine("Writing.. string..");
			send = "quit\r\n";
			buffer = send.getBytes("UTF-8");
			bos.write(buffer,0,buffer.length);
			bos.flush();

			logger.fine("Reading.. String");
			logger.info("Got : "+br.readLine());

			bos.close();
			br.close();

			oos.close();
			ois.close();
		} catch(Exception e) {
			logger.warning("Error " + e);
		}
	}
}
