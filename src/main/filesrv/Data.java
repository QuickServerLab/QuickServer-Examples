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

package filesrv;

import org.quickserver.net.*;
import org.quickserver.net.server.*;
import org.quickserver.util.MyString;

import org.quickserver.util.pool.PoolableObject;
import org.apache.commons.pool.BasePoolableObjectFactory; 
import org.apache.commons.pool.PoolableObjectFactory; 

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.nio.*;
import java.nio.channels.*;

/**
 * FileServer Example
 * @author Akshathkumar Shetty
 */
public class Data implements ClientData, PoolableObject {
	public static String serverIP = null;
	private static Logger logger =  Logger.getLogger(Data.class.getName());
	static {
		try {
			serverIP = java.net.InetAddress.getLocalHost().getHostName();
		} catch(UnknownHostException e)	{
			serverIP = "UnknownHost";
		}
	}
	private static String userRootHome = "dist";

	public static void setUserRootHome(String name) {
		userRootHome = name;
	}

	private HashMap httpHeader;
	private String user_root = userRootHome;
	private ByteBuffer wrapedByteBuffer;
	private ByteBuffer pooledByteBuffer;
	private boolean sendFile = false;
	private FileChannel fileChannel;

	private long startRange = 0;
	private long endRange = -1;
	private long fileLength = -1;
	private boolean wroteFileHttpHeader;
	private boolean closeConWhenDone;

	private String nonBlockingWriteDesc = null;

	public boolean isHeaderReady() {
		return httpHeader!=null;
	}
	
	public void initHeader(String data) {
		httpHeader = new HashMap();
		int i = data.lastIndexOf(" HTTP/");
		httpHeader.put("FILE", data.substring(4, i));
	}

	public boolean addHeader(String data) {
		if(data.startsWith("GET /")) {
			initHeader(data);
			return false;
		}

		if(data.length()==0) return true;
		int i = data.indexOf(": ");
		if(i==-1) {
			logger.warning("Got unknown header: "+data);
			return false;
		}

		httpHeader.put(data.substring(0, i).toUpperCase(), 
			data.substring(i+2));
		return false;
	}

	public File getFile() throws BadRequestException {
		String reqDir = (String) httpHeader.get("FILE");
		if(reqDir==null) 
			throw new BadRequestException("No File Requested!");
		if(reqDir.length()==0) reqDir = File.separator;
		return new File(user_root+reqDir);
	}

	public boolean isDirList() throws BadRequestException {
		File file = getFile();
		if(file.canRead()==false) {
			logger.finest("File : "+file.getAbsolutePath());
			throw new BadRequestException("File Not Found: "+(String)httpHeader.get("FILE"));
		}
		if(file.isDirectory()) {
			String reqDir = (String) httpHeader.get("FILE");
			if(reqDir.charAt(reqDir.length()-1)!='/') {
				httpHeader.put("FILE", reqDir+"/");
			}
		}
		return file.isDirectory();
	}

	public String getDirList() throws BadRequestException {
		File file = getFile();
		File list[] = file.listFiles();
		String loc = (String)httpHeader.get("FILE");

		StringBuffer content = new StringBuffer();
		content.append("<html>\r\n<head>\r\n<title>"+serverIP+" - "+loc+"</title>\r\n</head>\r\n<body>\r\n");
		content.append("<H3>Filesrv Server - File List for "+serverIP+" - "+loc+"</H3>\r\n<hr/>\r\n");
		content.append("<blockquote>\r\n<table width=\"80%\">");
		
		if(loc.equals("/")==false) {
			content.append("\r\n<tr>\r\n<td colspan=\"5\">");
			String parent = file.getParent();
			parent = parent.replace('\\', '/');
			//parent = parent.replaceAll("\\("+user_root+"\\)", "");
			parent = MyString.replaceAll(parent, user_root, "");
			if(parent.equals("")) parent = "/";
			content.append("[<a href=\""+parent+"\">To Parent Directory</a>]"); //to fix
			content.append("\r\n</td>\r\n</tr>");
		}

		for(int i=0;i<list.length;i++) {
			content.append("\r\n<tr>");
			
			content.append("\r\n<td align=\"right\">");
			content.append(new java.util.Date(list[i].lastModified()));
			content.append("</td>");

			content.append("\r\n<td>&nbsp;&nbsp;</td>");

			content.append("\r\n<td align=\"right\">");
			if(list[i].isDirectory()) {
				content.append(" &lt;dir&gt;");
			} else {
				content.append(" "+list[i].length());
			}
			content.append("</td>");

			content.append("\r\n<td>&nbsp;&nbsp;</td>");

			content.append("\r\n<td><a href=\"");
			content.append(loc+list[i].getName());//to fix
			content.append("\">");
			content.append(list[i].getName());
			content.append("</a></td>");

			content.append("\r\n</tr>");
		}
		content.append("\r\n</table>\r\n</blockquote>");
		content.append("\r\n<hr/>\r\n</body>\r\n</html>");
		return content.toString();
	}

	private String makeFileResponseHeader(ClientHandler handler) {
		String range = (String) httpHeader.get("RANGE");

		StringBuffer sb = new StringBuffer();

		if(range==null || range.startsWith("bytes=")==false) {
			sb.append("HTTP/1.1 200 OK\r\n");
			sb.append("Content-Length: "+fileLength).append("\r\n");
			startRange = 0;
			endRange = fileLength-1;
		} else {
			try {
				range = range.substring(6); //skip bytes=
				int i = range.indexOf("-");
				if(i==-1) i = range.length();
				startRange = Integer.parseInt(range.substring(0, i));
				i++;
				if(i<range.length()) {
					endRange = Integer.parseInt(range.substring(i));
				} else {
					endRange = fileLength-1;
				}
			} catch(Exception e) {
				logger.finest("IGNORE Error: "+e);
			}
			sb.append("HTTP/1.1 206 Partial content\r\n");
			sb.append("Content-Range: bytes "+startRange+"-"+endRange+
				"/"+fileLength+"\r\n");

		}
		sb.append("Server: ").append(handler.getServer().getName()).append("\r\n");
		sb.append("Content-Type: application/octet-stream").append("\r\n");
		sb.append("\r\n");
		
		return sb.toString();
	}

	public void sendFileNonBlocking(ClientHandler handler) 
			throws IOException, BadRequestException {
		fileLength = getFile().length();
		String header = makeFileResponseHeader(handler);
		logger.fine("Will Send: \n"+header);

		makeNonBlockingWrite(handler, header.getBytes(), true, 
			"Sending HTTP header for file.", false);
	}

	public void makeNonBlockingWrite(ClientHandler handler, byte data[], 
			boolean sendFileFlag, String desc, boolean closeConWhenDone) throws IOException {
		if(wrapedByteBuffer!=null) {
			//this client must have sent another req. with out waiting for res.. let close him.. 
			throw new IOException("The old data was still not fully written sorry!");
		}
		wrapedByteBuffer = ByteBuffer.wrap(data);
		sendFile = sendFileFlag;
		nonBlockingWriteDesc = desc;
		this.closeConWhenDone = closeConWhenDone;
		handler.registerForWrite();
	}

	public void sendFileBlocking(ClientHandler handler) 
			throws IOException, BadRequestException {
		File file = getFile();
		
		fileLength = file.length();
		String header = makeFileResponseHeader(handler);
		
		FileInputStream in = null;
		byte buffer[] = new byte[1024*1024];
		try {
			in = new FileInputStream(file);
			if(startRange>0) {
				logger.finest("Will skip: "+startRange);
				in.skip(startRange);
			}
			int i = 0;
			
			i = in.read(buffer);
			logger.finest("Sending HTTP header for file.");
			handler.sendClientBytes(header);

			handler.setDataMode(DataMode.BINARY, DataType.OUT);
			logger.finest("Sending file data: "+file);
			long remain = fileLength-startRange;
			logger.finest("Remain: "+remain+", startRange: "+startRange+", i: "+i);

			while(i!=-1 && remain!=0) {				
				if(i>remain) i = (int) remain;				

				handler.sendClientBinary(buffer,0, i);
				startRange+=i;

				remain = fileLength-startRange;
				i = in.read(buffer);
				logger.finest("Remain: "+remain+", startRange: "+startRange+", i: "+i);
				Thread.currentThread().yield();
			}
			logger.fine("Sent the file!");
		} catch(Exception er) {
			logger.info("Error sending file: "+er);
		} finally {
			if(in!=null) in.close();
			handler.setDataMode(DataMode.BYTE, DataType.OUT);
		}
		
		if(false) {
			handler.closeConnection();
		} else {
			//so that we can take more req.
			clean();
		}
	}

	public void writeData(ClientHandler handler) throws Exception {
		if(wrapedByteBuffer!=null && wrapedByteBuffer.remaining()!=0) {
			logger.finest(nonBlockingWriteDesc);
			int written = handler.getSocketChannel().write(wrapedByteBuffer);
			if(written>0) {
				handler.updateLastCommunicationTime();
			}
			logger.finest("Written "+written+" Bytes");
			if(wrapedByteBuffer.remaining()!=0) {
				handler.registerForWrite();
				return;
			}
			wroteFileHttpHeader = true;
			if(sendFile==false) {
				wrapedByteBuffer = null;
				if(closeConWhenDone) {
					handler.closeConnection();
				}
				return;
			}
		}
		
		if(sendFile==true) {
			if(fileChannel==null) {
				File file = getFile();
				logger.finest("Sending file data: "+file);
				FileInputStream fin = new FileInputStream(file);
				fileChannel = fin.getChannel();
				fileChannel.position(startRange);
			}
			
			if(pooledByteBuffer==null) {
				pooledByteBuffer = (ByteBuffer) 
					handler.getServer().getByteBufferPool().borrowObject();
			}

			if(pooledByteBuffer.hasRemaining()) {
				int ret = -1;
				long remain = fileLength-startRange;
				logger.finest("Remain: "+remain);

				if(pooledByteBuffer.remaining()>remain) {
					pooledByteBuffer.limit(
						(int) (pooledByteBuffer.position()+remain) );
				}

				ret = fileChannel.read(pooledByteBuffer);

				logger.finest("Read "+ret+" Bytes from file");
				if(ret<0 || remain==0) {//EOF
					fileChannel.close();
					//fileChannel = null;
				} else {
					startRange+=ret;
				}				
			}
			pooledByteBuffer.flip();
			
			if(pooledByteBuffer.hasRemaining()) {
				int written = handler.getSocketChannel().write(pooledByteBuffer);
				if(written>0) {
					handler.updateLastCommunicationTime();
				}
				logger.finest("Written "+written+" Bytes to socket");
			}

			long remain = fileLength-startRange;
			if(remain==0 && pooledByteBuffer.hasRemaining()==false) {
				fileChannel.close();
			}

			if(pooledByteBuffer.hasRemaining()==false && fileChannel.isOpen()==false) {
				sendFile = false;
				logger.finest("Sent the file!");
				
				if(false) {
					handler.closeConnection();
				} else {
					//so that we can take more req.
					clean();
				}
				return; //work done
			}
			pooledByteBuffer.compact(); //In case of partial write
			handler.registerForWrite();
		} //end of sendFile
	}

	public void cleanPooledByteBuffer(QuickServer quickserver) {
		if(pooledByteBuffer!=null) {
			try {
				quickserver.getByteBufferPool().returnObject(pooledByteBuffer);	
			} catch(Exception er) {
				logger.warning("Could not return ByteBuffer back to pool: "+er);
			}		
			pooledByteBuffer = null;
		}
	}

	public boolean getWroteFileHttpHeader() {
		return wroteFileHttpHeader;
	}

	//--- pool --
	public void clean() {
		httpHeader = null;
		user_root = userRootHome;
		wrapedByteBuffer = null;
		if(pooledByteBuffer!=null) {
			pooledByteBuffer.clear();
		}
		sendFile = false;
		if(fileChannel!=null) {
			try {
				fileChannel.close();	
			} catch(Exception er) {}			
			fileChannel = null;
		}		
		startRange = 0;
		endRange = -1;
		fileLength = -1;
		wroteFileHttpHeader = false;

		nonBlockingWriteDesc = null;
		closeConWhenDone = false;
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
				Data d = (Data)obj;
				d.clean();
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


