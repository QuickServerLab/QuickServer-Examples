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

/*
  Server Messages
  ------------------
	Id format
		user1		<- user in any room
		user1@home	<- user in room
		@home		<- to room

	{user.info:<id>} <type> <- Gives info about users
		Where <type> can be
		LoggedIn
		LoggedOut
	{user.msg:<fromid>:<toid>} <-Message from user
	{user.list} <id> <- system is giving list of users in room


	{system.error} <type> <- Error at server
		Where <type> can be
		MessageNotSent
		BadCommand
		AuthFailed
		BadParam
	{system.debug} <- Debug info from server
	{system.msg} <- Message from server
	{system.help} <- Help from server
	{system.data} <- Server is waiting for data
	{system.ok} <- Success 

*/

import org.quickserver.net.*;
import org.quickserver.net.server.*;

import java.io.*;
import java.util.logging.*;
import org.quickserver.util.logging.*;

/**
 * Main class of ChatServer
 * Demonstrates how to find another client connected and 
 * send String to it.
 * @author  Akshathkumar Shetty
 */
public class ChatServer {
	public static String VER = "2.0";
	public static void main(String s[])	{
		QuickServer chatServer = new QuickServer();
		
		String confFile = "conf"+File.separator+"ChatServer.xml";
		Object config[] = new Object[] {confFile};
		
		try	{
			if(chatServer.initService(config) == true) {
				chatServer.startServer();
				chatServer.startQSAdminServer();
			}
		} catch(AppException e) {
			System.out.println("Error in server : "+e);
			e.printStackTrace();
		}
	}
}


