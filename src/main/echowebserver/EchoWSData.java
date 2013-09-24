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

package echowebserver;

import org.quickserver.net.server.ClientData;
import org.quickserver.util.pool.PoolableObject;
import org.apache.commons.pool.BasePoolableObjectFactory; 
import org.apache.commons.pool.PoolableObjectFactory; 
import java.util.*;
import java.io.IOException;
import java.util.logging.*;

/**
 * EchoWSData
 * @author Akshathkumar Shetty
 */
public class EchoWSData implements ClientData, PoolableObject {
	private static Logger logger = Logger.getLogger(EchoWSData.class.getName());
	private static final int MAX_HEADER_LENGTH = 50;

	private List httpHeader = null;
	private StringBuffer httpPost = null;
	private StringBuffer buffer = null;
	private int contentLength = 0;

	public EchoWSData() {
		httpHeader = new ArrayList();
		buffer = new StringBuffer();
	}	

	public void addInput(String command) throws IOException {
		buffer.append(command);
		int k = buffer.indexOf("\r\n");
		int s = 0;
		String temp = null;
		while(k!=-1) {
			if(httpHeader.size() > MAX_HEADER_LENGTH) {
				throw new IOException("Max header length exceeded! ");
			}
			
			temp = buffer.substring(s, k);
			logger.finest("Header "+temp);
			httpHeader.add(temp);
			k = k + 2;
			s = k;
			if(temp.length()==0) { //HeaderComplete
				buffer.delete(0, k);//del header
				if(isPost()) {
					addPost(buffer.toString());
				}
				break;
			}
			k = buffer.indexOf("\r\n", k);
		}
	}

	public void addPost(String command) {
		logger.fine("Data: "+command);
		httpPost.append(command);
	}

	public String getDataForOutput() {
		StringBuffer sb = new StringBuffer();
		for(int j=0; j<httpHeader.size(); j++) {
			sb.append((String)httpHeader.get(j));
			sb.append("\r\n");
		}
		if(httpPost!=null) {
			sb.append(httpPost);
		}
		return sb.toString();
	}

	public boolean isRequestComplete() {
		if(isHeaderComplete()) {
			logger.fine("Header complete!");
			if(httpPost!=null && httpPost.length()<contentLength) {
				logger.fine("Waiting for httpPost!");
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	public boolean isHeaderComplete() {
		if(httpHeader.size()==0) return false;
		String temp = (String) httpHeader.get(httpHeader.size()-1);
		return temp.length() == 0;
	}

	public boolean isPost() {
		if(httpHeader.size()==0) return false;

		String temp = (String) httpHeader.get(0);
		if(temp.toUpperCase().startsWith("POST")) {
			contentLength = contentLength();
			httpPost = new StringBuffer();
			return true;
		} else {
			return false;
		}
	}

	// Given a line that starts with Content-Length,
	// this returns the integer value specified.
	public int contentLength() {
		String input, temp;
		for(int i=0; i<httpHeader.size(); i++) {
			temp = (String) httpHeader.get(i);
			if (temp.length() == 0)
				break;
			input = temp.toUpperCase();
			if(input.startsWith("CONTENT-LENGTH"))
				return(getLength(input));
		}
		return(0);
	}
	private int getLength(String length) {
		StringTokenizer tok = new StringTokenizer(length);
		tok.nextToken();
		try {
			return Integer.parseInt(tok.nextToken());
		} catch(Exception e) {
			return 0;
		}		
	}


	//---- PoolableObject ---
	private void clean() {
		httpHeader.clear();
		buffer.setLength(0);
		httpPost = null;
		contentLength = 0;
	}

	public boolean isPoolable() {
		return true;
	}

	public PoolableObjectFactory getPoolableObjectFactory() {
		return  new BasePoolableObjectFactory() {
			public Object makeObject() { 
				return new EchoWSData();
			} 
			public void passivateObject(Object obj) {
				EchoWSData ed = (EchoWSData)obj;
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
