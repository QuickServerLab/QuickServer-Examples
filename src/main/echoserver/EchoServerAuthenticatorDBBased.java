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
import java.io.*;
import java.sql.*;
import java.util.*;
import org.quickserver.net.AppException;

public class EchoServerAuthenticatorDBBased extends QuickAuthenticationHandler {
	public AuthStatus askAuthentication(ClientHandler handler) 
			throws IOException, AppException {
		Data data = (Data) handler.getClientData();
		data.setLastAsked("U");
		handler.sendClientMsg("User Name :");
		return null;
	}

	public AuthStatus handleAuthentication(ClientHandler handler, String command) 
			throws IOException, AppException {
		Data data = (Data)handler.getClientData();

		if(data.getLastAsked().equals("U")) {
			data.setUsername(command);
			data.setLastAsked("P");
			handler.sendClientMsg("Password :");
		} else if(data.getLastAsked().equals("P")) {
			data.setPassword(command.getBytes());
			
			if(validate(handler, data.getUsername(), data.getPassword())) {
				handler.sendClientMsg("Auth OK");
				data.setPassword(null);
				handler.sendClientMsg(data.getWelcomeMsg());
				return AuthStatus.SUCCESS;
			} else {
				handler.sendClientMsg("Auth Failed");
				data.setPassword(null);
				return AuthStatus.FAILURE;
			}
		} else {
			throw new AppException("Unknown LastAsked!");
		}

		return null;
	}

	protected static boolean validate(ClientHandler handler, String username, byte[] password) {
		Connection con = null;
		try {
			con = handler.getServer().getDBPoolUtil().getConnection("TestDB1");
			Statement s = con.createStatement();
			ResultSet r = s.executeQuery("SELECT welcomemesage FROM Auth "+
				"WHERE USERNAME='"+username+"' AND PASSWORD='"+new String(password)+"'");
			if(r.next()) {
				Data data = (Data)handler.getClientData();
				data.setWelcomeMsg(r.getString(1));
				return true;
			} else {
				return false;
			}	
		} catch(Exception e) {
			return false;
		} finally {
			try	{
				con.close();
			} catch(Exception e) {
				handler.sendSystemMsg("IGNORING: "+e);
			}			
		}
	}
}
