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

import java.io.*;
import java.net.*;
import java.util.*;

public class LoadTest {
	private Agent agents[] = null;
	private ThreadGroup agentGroup = null;
	private List times = null;

	public void init(int count, String host, int port) {
		System.out.print("Starting init..");
		agentGroup = new ThreadGroup("AgentGroup");
		times = new ArrayList(count);
		agents = new Agent[count];
		for(int i=0;i<count;i++) {
			agents[i] = new Agent(agentGroup, times, host, port);
			agents[i].start();
		}		
		System.out.println(" "+agentGroup.activeCount()+" ready.");
	}

	public void test() throws InterruptedException {
		int count = agents.length;
		int half = count/2;

		System.out.print("Starting test..");
		long stime = System.currentTimeMillis();
		long htime = -1;
		long hcount = -1;

		/*
		for(int i=0;i<count;i++)
			agents[i].start();
		*/
		synchronized(agentGroup) {
			agentGroup.notifyAll();
		}

		int ac = times.size();
		while(ac<count) {
			if(htime<0 && half>=ac) {
				htime = System.currentTimeMillis();
				hcount = count-ac;
			}
			Thread.sleep(5);
			ac = times.size();
		}

		long etime = System.currentTimeMillis();
		System.out.println("Done\n");


		long time = etime - stime;
		System.out.println("Total Time : "+time+"ms");

		time = time/count;
		System.out.println("Avg. Time  : "+time+"ms\n");

		/*
		if(htime!=-1) {
			time = htime - stime;
			System.out.println("Half Time ("+hcount+"): "+time+"ms");
			time = time/hcount;
			System.out.println("Half Avg Time: "+time+"ms");
		}
		*/

		/*
		System.out.println("\nEach Client time..\n");
		for(int i=0;i<count;) {
			System.out.print(times.get(i)+",");
			if(++i%10==0) System.out.println("");
		}
		*/
	}

	public static void main(String args[]) throws Exception {
		LoadTest lt = new LoadTest();

		String host = "127.0.0.1";
		int port = 2222;
		int count = 50;

		if(args.length>0)
			count = Integer.parseInt(args[0]);
		if(args.length>1)
			host = args[1];
		if(args.length>2)
			port = Integer.parseInt(args[2]);
			
		lt.init(count, host, port);
		
		Thread.sleep(200); //let all thread get ready
		lt.test();
	}
}

class Agent extends Thread {
	private static int count = 0;

	XmlAdderClient client = null;
	List list = null;

	public Agent(ThreadGroup threadGroup, List list, String host, int port) {
		super(threadGroup, null, "Agent:"+ ++count);
		client = new XmlAdderClient(host, port);
		this.list = list;
	}

	public void run() {
		synchronized(getThreadGroup()) {
			try {
				getThreadGroup().wait();
			} catch(Exception e) {
				System.err.println("Error is wait! "+e);
			}
		}
		client.test();
		list.add(""+client.getTime());
	}
}
