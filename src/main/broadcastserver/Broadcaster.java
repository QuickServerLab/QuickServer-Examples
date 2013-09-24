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

package broadcastserver;

import org.quickserver.net.*;
import org.quickserver.net.server.*;

import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 * Broadcaster
 */
public class Broadcaster implements Runnable {
	private static final Logger logger = Logger.getLogger(Broadcaster.class.getName());
	private static Broadcaster instance;

	private Broadcaster() {}

	public static synchronized Broadcaster getInstance() {
		if(instance!=null) return instance;

		instance = new Broadcaster();
		Thread t = new Thread(instance);
		t.setName("BroadcasterThread");
		t.start();

		return instance;
	}

	private List currentSendList = new ArrayList();
	private List notoSendList = new ArrayList();
	private List newSendList = new ArrayList();

	private List currentDataList = new ArrayList();
	private List newDataList = new ArrayList();

	private final Object dataLock = new Object();
	private final Object clientLock = new Object();
	private final Object newGotLock = new Object();

	public void addClient(ClientHandler handler) {
		logger.fine(handler.toString());
		synchronized(clientLock) {
			newSendList.add(handler);
		}
	}
	public void removeClient(ClientHandler handler) {
		logger.fine(handler.toString());
		synchronized(clientLock) {
			notoSendList.add(handler);
		}
	}

	public void addMsgToBroadcast(String data) {
		logger.fine(data);
		synchronized(dataLock) {
			newDataList.add(data);
		}
		synchronized(newGotLock) {
			newGotLock.notify();
		}
	}

	public void run() {
		while(true) {
			synchronized(clientLock) {
				currentSendList.removeAll(notoSendList);
				currentSendList.addAll(newSendList);				

				notoSendList.clear();
				newSendList.clear();
			}

			synchronized(dataLock) {
				if(newDataList.size()>0) {
					currentDataList.addAll(newDataList);
					newDataList.clear();
				}
			}

			if(currentDataList.isEmpty()) {
				synchronized(newGotLock) {
					try {
						newGotLock.wait();
					} catch(InterruptedException e) {
						logger.warning("Interrupted: "+e);
						return;
					}					
				}
				continue;
			} else {
				logger.log(Level.FINEST, "Start broadcasting msg: "+currentDataList.size());
				ClientHandler handler = null;
				String data = null;
				Iterator dataListIterator = currentDataList.iterator();
				while(dataListIterator.hasNext()) {
					data = (String) dataListIterator.next();
					Iterator clientHandlerIterator = currentSendList.iterator();
					while(clientHandlerIterator.hasNext()) {
						handler = (ClientHandler) clientHandlerIterator.next();
						try {
							handler.isConnected();
							handler.sendClientMsg(data);
						} catch(Exception e) {
							removeClient(handler);
							logger.log(Level.FINE, "IGNORE: {0} - had error - {1}", new Object[]{handler, e});
						}
					}//while
				}//while
				currentDataList.clear();
				logger.finest("End broadcasting..");
			}
		}
	}
}


