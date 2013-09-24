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

import org.apache.commons.digester3.Digester;
import java.util.logging.*;
import java.io.*;


import org.quickserver.net.server.ClientData;
import org.quickserver.util.pool.PoolableObject;
import org.apache.commons.pool.BasePoolableObjectFactory; 
import org.apache.commons.pool.PoolableObjectFactory; 

import java.net.*;
import java.io.*;
import java.util.logging.*;

public class Data implements ClientData, PoolableObject {
	private static Logger logger = Logger.getLogger(Data.class.getName());

	private StringBuffer xmlDump = new StringBuffer();

	public void addXmlPart(String data) throws Exception {
		xmlDump.append(data);
	}

	public synchronized String getNextXML() {
		int i1 = xmlDump.indexOf("<");
		int i2 = xmlDump.indexOf(">",i1);
		if(i1!=-1 && i2!=-1) {
			String tag = xmlDump.substring(i1+1, i2);
			int j = -1;
			String xmlEndTag = null;
			if(tag.trim().charAt(tag.trim().length()-1)=='/') {
				j = i2;
				xmlEndTag = ">";
			} else {
				xmlEndTag = "</" + tag + ">";
				j = xmlDump.indexOf(xmlEndTag, i2);
			}
			if(j!=-1) {
				String data = xmlDump.substring(i1, j+xmlEndTag.length());
				xmlDump.delete(i1, j+xmlEndTag.length());
				return data;
			}
		} 
		return null;
	}

	//-- PoolableObject
	public void clean() {
		xmlDump.setLength(0);
	}

	public boolean isPoolable() {
		return true;
	}

	public PoolableObjectFactory getPoolableObjectFactory() {
		return  new BasePoolableObjectFactory() {
			public Object makeObject() { 
				return new Data();
			} 
			public void passivateObject(Object obj) {
				Data ed = (Data)obj;
				ed.clean();
			} 
			public void destroyObject(Object obj) {
				if(obj==null) return;
				passivateObject(obj);
				obj = null;
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
