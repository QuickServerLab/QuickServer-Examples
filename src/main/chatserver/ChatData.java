/*
 * This	file is	part of	the	QuickServer	library	
 * Copyright (C) 2003-2005 QuickServer.org
 *
 * Use, modification, copying and distribution of this software is subject to
 * the terms and conditions of the GNU Lesser General Public License. 
 * You should have received a copy of the GNU LGP License along with this 
 * library; if not, you can download a copy from <http://www.quickserver.org/>.
 *
 * For questions, suggestions, bug-reports,	enhancement-requests etc.
 * visit http://www.quickserver.org
 *
 */

package	chatserver;

import org.quickserver.net.server.*;

import org.quickserver.util.pool.PoolableObject;
import org.apache.commons.pool.BasePoolableObjectFactory; 
import org.apache.commons.pool.PoolableObjectFactory;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.util.logging.*;

/**
 *
 * @author	Akshathkumar Shetty
 */
public class ChatData implements ClientData, ClientIdentifiable, PoolableObject	{
	private	static final Logger	logger = 
		Logger.getLogger(ChatData.class.getName());

	private	static Set usernameList	= new HashSet();

	private	String username	= null;
	private	String room	= null;
	private	String info	= null;

	//for auth
	private String lastAsked = null;
	private byte password[] = null;

	public void setLastAsked(String lastAsked) {
		this.lastAsked = lastAsked;
	}
	public String getLastAsked() {
		return lastAsked;
	}

	public void setPassword(byte[] password) {
		this.password = password;
	}
	public byte[] getPassword() {
		return password;
	}

	public boolean registerUsername(String username) {
		return usernameList.add(username);
	}
	public void deregisterUsername(String username) {
		usernameList.remove(username);
	}
	public void	setUsername(String username) {
		this.username =	username;
	}
	public String getUsername() {
		return username;
	}

	public void	setRoom(String room) {
		this.room =	room;
	}
	public String getRoom()	{
		return room;
	}

	public String getClientId()	{
		return username;
	}

	public String getClientKey() {
		if(room==null)
			return username;
		else
			return username+"@"+room;
	}

	public void	setClientInfo(String info) {
		this.info =	info;
	}
	public String getClientInfo() {
		return getClientKey()+"	- "+info;
	}

	public String toString() {
		return getClientInfo();
	}

	//-- PoolableObject
	public void	clean()	{
		usernameList.remove(username);
		username = null;
		room = null;
		info = null;
		lastAsked = null;
	}

	public boolean isPoolable()	{
		return true;
	}

	public PoolableObjectFactory getPoolableObjectFactory()	{
		return	new	BasePoolableObjectFactory()	{
			public Object makeObject() { 
				return new ChatData();
			} 
			public void	passivateObject(Object obj)	{
				ChatData ed	= (ChatData)obj;
				ed.clean();
			} 
			public void	destroyObject(Object obj) {
				if(obj==null) return;
				passivateObject(obj);
				obj	= null;
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


