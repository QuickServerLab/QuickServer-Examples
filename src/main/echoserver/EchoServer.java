/*
 * This file is part of the QuickServer library 
 * Copyright (C) QuickServer.org
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

import java.io.File;
import org.quickserver.net.*;
import org.quickserver.net.server.*;

import org.quickserver.util.xmlreader.KeyStoreInfo;
import org.quickserver.util.xmlreader.QuickServerConfig;
import org.quickserver.util.xmlreader.Secure;
import org.quickserver.util.xmlreader.SecureStore;


public class EchoServer {

	public static String version = "1.3";

	public static void main(String s[]) {
		startFromXML(s);
		//startFromCode(s);
	}
	
	public static void startFromXML(String s[])	{
		QuickServer echoServer;		
		String confFile = "conf"+File.separator+"EchoServer.xml";

		try	{
			echoServer = QuickServer.load(confFile);
		} catch(AppException e) {
			System.out.println("Error in server : "+e);
			e.printStackTrace();
		}
	}
	
	public static void startFromCode(String s[]) {
		QuickServer echoServer = new QuickServer();

		try {
			QuickServerConfig cfg = new QuickServerConfig();
			cfg.setClientCommandHandler("echoserver.EchoCommandHandler");
			cfg.setClientData("echoserver.Data");
			cfg.setPort(5102);
			cfg.setName("EchoServer");			
			

			Secure secure = new Secure();
			secure.setEnable(true);
			secure.setLoad(true);
			secure.setPort(5102);
			secure.setProtocol("SSLv3");
			secure.setClientAuthEnable(false);

			SecureStore secureStore = new SecureStore();
			secureStore.setType("JKS");
			secureStore.setAlgorithm("SunX509");

			KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
			keyStoreInfo.setStoreFile("./conf/user1.keystore");
			keyStoreInfo.setStorePassword("user1spass");
			keyStoreInfo.setKeyPassword("user1kpass");

			secureStore.setKeyStoreInfo(keyStoreInfo);
			secure.setSecureStore(secureStore);
			
			cfg.setSecure(secure);

			echoServer.initServer(cfg);
			echoServer.startServer();
			echoServer.startQSAdminServer();
		} catch (AppException e) {
			System.out.println("Error in server : " + e);
			e.printStackTrace();
		}
	}
}
