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

package xmladder;

import java.util.logging.*;
import java.io.*;
import org.apache.commons.digester3.Digester;

/*
	<add-number-req>
		<number-a>5</number-a>
		<number-b>15</number-b>
	</add-number-req>
 */
public class AddNumberReq {
	private static Logger logger = Logger.getLogger(AddNumberReq.class.getName());
	//make true for performance testing 
	private static boolean performanceTest = false;
	private static Digester digester = null;

	static {
		performanceTest = Boolean.getBoolean("xmladder.AddNumberReq.performanceTest");
		
		if(performanceTest==false) {
			digester = new Digester();
			digester.setValidating(false);
			digester.setUseContextClassLoader(true);
			String mainTag = "add-number-req";
			digester.addObjectCreate(mainTag, AddNumberReq.class);
			digester.addBeanPropertySetter(mainTag+"/number-a", "numberA");
			digester.addBeanPropertySetter(mainTag+"/number-b", "numberB");
		}
	}

	private int numberA;
	private int numberB;

	/**
	 * @return
	 */
	public int getNumberA() {
		return numberA;
	}

	/**
	 * @param numberA
	 */
	public void setNumberA(int numberA) {
		this.numberA = numberA;
	}

	/**
	 * @return
	 */
	public int getNumberB() {
		return numberB;
	}

	/**
	 * @param numberB
	 */
	public void setNumberB(int numberB) {
		this.numberB = numberB;
	}

	public String toXML() {
		StringBuffer sb = new StringBuffer();
		sb.append("<add-number-req>\n");
		sb.append("\t<number-a>").append(getNumberA()).append("</number-a>\n");
		sb.append("\t<number-b>").append(getNumberB()).append("</number-b>\n");
		sb.append("</add-number-req>");
		return sb.toString();
	}

	public static AddNumberReq fromXML(String data) 
			throws IOException, org.xml.sax.SAXException {
		AddNumberReq addNumberReq = null;
		if(performanceTest==false) {
			logger.fine("Got xml:\n"+ data);
			
			addNumberReq = (AddNumberReq) digester.parse(
				new ByteArrayInputStream(data.getBytes("UTF-8")));
		} else {
			addNumberReq = new AddNumberReq();
			addNumberReq.setNumberA(14);
			addNumberReq.setNumberB(9);
		}
		return addNumberReq;
	}
}
