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

import org.quickserver.net.server.*;
import org.quickserver.net.InitServerHook;

import java.io.*;

import org.quickserver.util.logging.*;
import java.util.logging.*;

public class SetupLoggingHook implements InitServerHook {
	private QuickServer quickserver;

	public String info() {
		return "Init Server Hook to setup logging.";
	}

	public void handleInit(QuickServer quickserver) throws Exception {
		Logger logger = null;
		FileHandler txtLog = null;
		File log = new File("./log/");
		if(!log.canRead())
			log.mkdir();
		try	{
			logger = Logger.getLogger("");
			logger.setLevel(Level.FINEST);

			logger = Logger.getLogger("");
			txtLog = new FileHandler("log/EchoServer_Full%u%g.txt", 
				1024*1024, 5, true);
			txtLog.setFormatter(new SimpleTextFormatter());
			txtLog.setLevel(Level.FINEST);
			logger.addHandler(txtLog);

			logger = Logger.getLogger("org.quickserver");
			txtLog = new FileHandler("log/EchoServer_QuickServer%u%g.txt", 
				1024*1024, 20, true);
			txtLog.setFormatter(new SimpleTextFormatter());
			txtLog.setLevel(Level.FINEST);
			logger.addHandler(txtLog);
			
			logger = Logger.getLogger("echoserver");
			txtLog = new FileHandler("log/EchoServer%u%g.txt", 
				1024*1024, 20, true);
			txtLog.setFormatter(new SimpleTextFormatter());
			txtLog.setLevel(Level.FINEST);
			logger.addHandler(txtLog);

			quickserver.setAppLogger(logger); //img

			//debug non-blocking mode 
			//quickserver.debugNonBlockingMode(false);
		} catch(IOException e){
			System.err.println("Could not create txtLog FileHandler : "+e);
			throw e;
		}
	}
}
