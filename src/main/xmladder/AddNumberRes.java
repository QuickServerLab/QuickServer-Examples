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
/*
	<add-number-res>
		<result-type>sum</result-type>
		<result-value>15</result-value>
	</add-number-res>
	or
	<add-number-res>
		<result-type>error</result-type>
		<result-value>Bad input</result-value>
	</add-number-res>
 */
public class AddNumberRes {
	private static Logger logger = Logger.getLogger(AddNumberRes.class.getName());
	private static final Digester digester;

	static {
		digester = new Digester();
	    digester.setValidating(false);
		digester.setUseContextClassLoader(true);
		String mainTag = "add-number-res";
		digester.addObjectCreate(mainTag, AddNumberRes.class);
		digester.addBeanPropertySetter(mainTag+"/result-type", "type");
		digester.addBeanPropertySetter(mainTag+"/result-value", "value");
	}

	private String type;
	private String value;

	/**
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	public String toXML() {
		StringBuffer sb = new StringBuffer();
		sb.append("<add-number-res>\n");
		sb.append("\t<result-type>").append(getType()).append("</result-type>\n");
		sb.append("\t<result-value>").append(getValue()).append("</result-value>\n");
		sb.append("</add-number-res>");
		return sb.toString();
	}

	public static AddNumberRes fromXML(String data) 
			throws IOException, org.xml.sax.SAXException {
		logger.fine("Got xml:\n"+ data);		
		AddNumberRes addNumberRes = (AddNumberRes) digester.parse(
			new ByteArrayInputStream(data.getBytes("UTF-8")));
		return addNumberRes;
	}
}
