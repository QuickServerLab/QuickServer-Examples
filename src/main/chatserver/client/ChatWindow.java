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

package chatserver.client;

import javax.swing.text.*;
import java.awt.Color;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Graphics;
import java.awt.event.*;

/**
 *
 * @author  Akshathkumar Shetty
 */
public class ChatWindow extends javax.swing.JFrame {
    private static Logger logger = Logger.getLogger(ChatWindow.class.getName());

	private ClassLoader classLoader = getClass().getClassLoader();
	public final ImageIcon logo = new ImageIcon(
		classLoader.getResource("chatserver/client/face-smile.gif"));
	public final ImageIcon smile = new ImageIcon(
		classLoader.getResource("chatserver/client/smile.gif"));
	public final ImageIcon sad = new ImageIcon(
		classLoader.getResource("chatserver/client/sad.gif"));
    
    private ChatRoom chatRoom;
    private LoginDialog loginDialog;
    private DefaultStyledDocument logDoc = null;
    private DefaultStyledDocument chatDoc = null;
    private DefaultListModel userListModel = null;
    private UserListListener userListListener = null;
    private Map styleMap = new HashMap();
    
    final String NORMALBLUE = "NormalBlue";
    final String BOLDBLUE = "BoldBlue";
    final String NORMALBLACK = "NormalBlack";
	final String ITALICBLACK = "ITALICBLACK";
    final String BOLDGREEN = "BoldGreen";
    final String NORMALRED = "NormalRed";
    final String ITALICRED = "ItalicRed";
    
	private InfiniteProgressPanel glassPane;
    
    /** Creates new form ChatWindow */
    public ChatWindow(ChatRoom chatRoom, String args[]) {
        this.chatRoom = chatRoom;
        setLogDoc(new DefaultStyledDocument());
        setChatDoc(new DefaultStyledDocument());
        setUserListModel(new DefaultListModel());
		prepareAllStyles();
        initComponents();
        userListListener = new UserListListener(userList);
        userList.addListSelectionListener(userListListener);
        loginDialog = new LoginDialog(this, args);
    }
    
    private void initComponents() {
        getContentPane().setLayout(new java.awt.BorderLayout(2, 2));
        setTitle("QuickChat - Please Login");
        
        buildChatPanel();
		buildSendMsg();
		buildLogPanel();
		buildUserListPanel();

		//---
		jPanel1 = new javax.swing.JPanel();
        jPanel1.setLayout(new java.awt.BorderLayout());
		jPanel1.add(jPanel2, java.awt.BorderLayout.SOUTH); //sendmsg

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
			chatRoomScrollPane, jScrollPane1);
		splitPane.setOneTouchExpandable(true);
		java.awt.Dimension minimumSize = new java.awt.Dimension(500, 20);
		chatRoomScrollPane.setMinimumSize(minimumSize);
		splitPane.setDividerLocation(500);
		splitPane.setResizeWeight(0.9);

		jPanel1.add(splitPane, java.awt.BorderLayout.CENTER); //chat

		jTabbedPane1 = new javax.swing.JTabbedPane();
        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        jTabbedPane1.addTab("Chat Room", jPanel1);
        jTabbedPane1.addTab("Logs", logTextScrollPane);

		getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        buildMenu();
		setIconImage(logo.getImage());

		addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
				 glassPane.interrupt();
                 System.exit(0);
             }
			 public void windowOpened(WindowEvent e) {			
			 }			
        });
        pack();
		setLocationRelativeTo(null);

		glassPane = new InfiniteProgressPanel("Logging to server..");
		setGlassPane(glassPane);
    }

    private void loginMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {
		Thread t = new Thread() {
			public void run() {
				while(true) {
					if(login()==false) break;
					logger.info("Calling login()");
				}
			}
		};
		t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }
    
	private boolean login() {
		loginDialog.clearStatus();
        loginDialog.show();
        if(loginDialog.isOk()==true) {
            String r[] = loginDialog.getValues();
			glassPane.start();
			try {
				boolean flag = chatRoom.doLogin(r[0], Integer.parseInt(r[1]), 
					r[2], r[3]);				

				if(flag==true) {									
					enableChat(true);
				} else {
					enableChat(false);
					return true;//recall the login dialog
				}
			} catch(Exception ex) {
				enableChat(false);
				logger.warning("Error logging in : "+ex);
				return true;
			}
        }
		return false;
    }                                             
    
    private void logoutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		Thread t = new Thread() {
			public void run() {
				try {
					chatRoom.doLogout();
				} catch(Exception ex) {
					loginMenuItem.setEnabled(true);
					logoutMenuItem.setEnabled(false);
					logger.warning("Error logging in : "+ex);
				}
			}
		};
		t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }                                              
    
    private void sendTextActionPerformed(java.awt.event.ActionEvent evt) {                                         
       sendButtonActionPerformed(evt);
    }                                        
    
    private void sendPrivateButtonActionPerformed(java.awt.event.ActionEvent evt) {
		if(userListListener.getSelecteduser()==null) {
			setResponse("-ERR No User is selected!");
			return;
		}
		if(sendText.getText().length()==0) {
			setResponse("-ERR No message to send!");
			return;
		}
        chatRoom.sendPrivateMessage(userListListener.getSelecteduser()+"@"+chatRoom.getRoom(),
                sendText.getText());
        sendText.setText("");
    }                                                 
    
    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {
		if(sendText.getText().length()==0) {
			setResponse("-ERR No message to send!");
			return;
		}
        chatRoom.sendMessage(sendText.getText());
        sendText.setText("");
    }                                          
    
    
    // Variables declaration - do not modify
    private javax.swing.JScrollPane chatRoomScrollPane;
    private javax.swing.JTextPane chatRoomTextPane1;
    private javax.swing.JMenu jMenu1, jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextPane logTextPane;
    private javax.swing.JScrollPane logTextScrollPane;
    private javax.swing.JMenuItem loginMenuItem;
    private javax.swing.JMenuItem logoutMenuItem;
	private javax.swing.JMenuItem exitMenuItem;
	private javax.swing.JMenuItem changeRoomMenuItem;
	private javax.swing.JMenuItem updateUserListMenuItem;
	private javax.swing.JMenuItem clearMenuItem;
	private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JButton sendButton;
    private javax.swing.JButton sendPrivateButton;
    private javax.swing.JTextField sendText;
    private javax.swing.JList userList;
    // End of variables declaration
    
    public void log(String msg) {
        logger.fine("Got: "+msg);
        try {
            AttributeSet style = (AttributeSet)styleMap.get(NORMALBLACK);
            getLogDoc().insertString(getLogDoc().getLength(),
                    msg +  "\n" ,style);
            
            //Point pt1=logTextPane.getLocation();
            Point pt2 = new Point((int)(0),
                    (int)(logTextPane.getBounds().getHeight()));
            logTextScrollPane.getViewport().setViewPosition(pt2);
        } catch(Exception e) {
            logger.warning("Error: "+e);
        }
    }
    
    public DefaultStyledDocument getLogDoc() {
        return logDoc;
    }
    
    public void setLogDoc(DefaultStyledDocument logDoc) {
        this.logDoc = logDoc;
    }
    
    
    
    public void prepareAllStyles() {
        SimpleAttributeSet aset = new SimpleAttributeSet();
        StyleConstants.setForeground(aset,Color.blue);
        StyleConstants.setFontSize(aset,12);
        StyleConstants.setFontFamily(aset,"Verdana");
        styleMap.put(NORMALBLUE,aset);

        aset = new SimpleAttributeSet();
        StyleConstants.setForeground(aset,Color.blue);
        StyleConstants.setFontSize(aset,12);
        StyleConstants.setFontFamily(aset,"Verdana");
        StyleConstants.setBold(aset, true);
        styleMap.put(BOLDBLUE,aset);

        aset = new SimpleAttributeSet();
        StyleConstants.setForeground(aset,Color.black);
        StyleConstants.setFontSize(aset,12);
        StyleConstants.setFontFamily(aset,"Verdana");
        styleMap.put(NORMALBLACK,aset);

        aset = new SimpleAttributeSet();
        StyleConstants.setForeground(aset,Color.black);
        StyleConstants.setFontSize(aset,12);
        StyleConstants.setFontFamily(aset,"Verdana");
		StyleConstants.setItalic(aset, true);
        styleMap.put(ITALICBLACK,aset);

        aset = new SimpleAttributeSet();
        StyleConstants.setForeground(aset, new Color(0, 128, 0));
        StyleConstants.setFontSize(aset,12);
        StyleConstants.setFontFamily(aset,"Verdana");
        StyleConstants.setBold(aset, true);
        styleMap.put(BOLDGREEN,aset);

        aset = new SimpleAttributeSet();
        StyleConstants.setForeground(aset,Color.red);
        StyleConstants.setFontSize(aset,12);
        StyleConstants.setFontFamily(aset,"Verdana");
        styleMap.put(NORMALRED,aset);

        aset = new SimpleAttributeSet();
        StyleConstants.setForeground(aset,Color.red);
        StyleConstants.setFontSize(aset,12);
        StyleConstants.setFontFamily(aset,"Verdana");
        StyleConstants.setItalic(aset, true);
        styleMap.put(ITALICRED,aset);
    }
    
    public void addChatMessage(String message) {
        logger.fine("Got: "+message);
        if(message.startsWith("{system.help} ")) {
            return;
        } else if(message.startsWith("{system.debug} ")) {
            //already logged
            return;
        }
        
        AttributeSet style = null;
        String fromid = null;
		String toid = null;
        
		String msgType = null;
        try {
            if(message.startsWith("{system.msg} ")) {
				msgType = "{system.msg}";
                message = message.substring(13);
                style = (AttributeSet)styleMap.get(BOLDBLUE);
            } else if(message.startsWith("{system.error} ")) {
				msgType = "{system.error}";
                message = "Error: "+message.substring(15);
                style = (AttributeSet)styleMap.get(NORMALRED);				
            } else if(message.startsWith("{user.msg:")) { 
				msgType = "{user.msg}";
				int j = message.indexOf(":", 10);//end of from
				int i = message.indexOf("} ", 10);
				if(j!=-1) {
					toid = message.substring(j+1, i);
				} else {
					j = i;
				}
                fromid = message.substring(10, j);
                message = message.substring(i+2);
                style = (AttributeSet)styleMap.get(NORMALBLUE);
            } else if(message.startsWith("{msg.user:")) { //gui command
				msgType = "{msg.user}";
                int i = message.indexOf("} ", 10);
                toid = message.substring(10, i);
                message = message.substring(i+2);
                style = (AttributeSet)styleMap.get(NORMALBLUE);
            } else if(message.startsWith("{user.info:")) {
				msgType = "{user.info}";
                int i = message.indexOf("} ", 11);
                fromid = message.substring(11, i);
                message = message.substring(i+2);
				if(message.equals("LoggedIn")) {
					addToUserList(fromid);
					message = "joined the room";
				} else if(message.equals("LoggedOut")) {
					removeFromUserList(fromid);
					message = "left the room";
				} else
					System.out.println("Unknown ->"+message+"<-");
                style = (AttributeSet)styleMap.get(ITALICBLACK);
            } else {
				msgType = "{unknown}";
                style = (AttributeSet)styleMap.get(NORMALBLACK);
            }
            

			if(msgType.equals("{user.msg}")) {
				toid = removeRoom(toid);
				fromid = removeRoom(fromid);
				if(toid!=null && toid.length()==0) {//to group
	                getChatDoc().insertString(getChatDoc().getLength(),
                        fromid+": ", (AttributeSet)styleMap.get(NORMALRED));
				} else if(toid!=null) {
					getChatDoc().insertString(getChatDoc().getLength(),
                        "PrvMsg From "+fromid+": ", (AttributeSet)styleMap.get(BOLDBLUE));
				}
			} else if(msgType.equals("{msg.user}")) {
				toid = removeRoom(toid);
                getChatDoc().insertString(getChatDoc().getLength(),
                        "PrvMsg To "+toid+": ", (AttributeSet)styleMap.get(BOLDBLUE));
            } else if(msgType.equals("{user.info}")) {
				fromid = removeRoom(fromid);
				getChatDoc().insertString(getChatDoc().getLength(),
                        fromid+": ", (AttributeSet)styleMap.get(NORMALRED));
			}
            
			if(message.indexOf(":-)")==-1 && message.indexOf(":-(")==-1) {
	            getChatDoc().insertString(getChatDoc().getLength(), message+ "\n", style);
			} else {
				checkForSmile(message, style);
				getChatDoc().insertString(getChatDoc().getLength(), "\n", style);
			}
            
            Point pt1 = chatRoomTextPane1.getLocation();
            Point pt2 = new Point((int)(0),
                    (int)(chatRoomTextPane1.getBounds().getHeight()));
            chatRoomScrollPane.getViewport().setViewPosition(pt2);
        } catch(Exception e) {
            logger.warning("Error: "+e);
        }

		toFront();
    }

	protected void setEndSelection() {
		int len = chatRoomTextPane1.getDocument().getLength();
		chatRoomTextPane1.setSelectionStart(len);
		chatRoomTextPane1.setSelectionEnd(len);    
	}
    
    public DefaultStyledDocument getChatDoc() {
        return chatDoc;
    }
    
    public void setChatDoc(DefaultStyledDocument chatDoc) {
        this.chatDoc = chatDoc;
    }
    
    public void setResponse(String res) {
        int msgType = JOptionPane.PLAIN_MESSAGE ;
        if(res.startsWith("+OK"))
            msgType = JOptionPane.INFORMATION_MESSAGE;
        if(res.startsWith("-ERR"))
            msgType = JOptionPane.ERROR_MESSAGE;
		toFront();
        JOptionPane.showMessageDialog(this,
                res.substring(res.indexOf(" ")+1), "Response", msgType);		
    }
    
    public void enableChat(boolean flag) {
        sendText.setEnabled(flag);
        sendButton.setEnabled(flag);
        sendPrivateButton.setEnabled(flag);
        loginMenuItem.setEnabled(!flag);
        logoutMenuItem.setEnabled(flag);
		changeRoomMenuItem.setEnabled(flag);
		updateUserListMenuItem.setEnabled(flag);
		if(flag==false) {
			userListModel.clear();
			setTitle("QuickChat - Not Connected");
		} else {
			chatRoomTextPane1.setText("");
			chatRoom.processReceivedMsg();
		}
		glassPane.stop();
		glassPane.setVisible(false);
    }
    
    public void addToUserList(String id) {
        logger.fine("Got: "+id);
		id = removeRoom(id);
        getUserListModel().addElement(id);
    }
    
    public void removeFromUserList(String id) {
        logger.fine("Got: "+id);
		id = removeRoom(id);
        getUserListModel().removeElement(id);
    }
    
    public DefaultListModel getUserListModel() {
        return userListModel;
    }
    
    public void setUserListModel(DefaultListModel userListModel) {
        this.userListModel = userListModel;
    }
    
	private String removeRoom(String id) {
		if(id==null) return id;
		if( id.endsWith("@"+chatRoom.getRoom()) ) {
			id = id.substring(0, id.indexOf("@"+chatRoom.getRoom()) );
		}
		return id;
	}

	private void about() {
		JOptionPane.showMessageDialog(this,
			"QuickChat v 1.0\n\n"+
			"GUI Client for ChatServer example of QuickServer.\n"+
			"This is compliant with QuickServer v1.4.5 release.\n\n"+
			"Copyright (C) 2005 QuickServer.org\n"+
			"http://www.quickserver.org",
			"About QuickChat",
			JOptionPane.INFORMATION_MESSAGE,
			logo);
	}

	private void changeRoom() {
		String newRoom = (String) JOptionPane.showInputDialog(this,
			"Chat Room:",
			"Change Room", JOptionPane.PLAIN_MESSAGE, logo,
                    null, chatRoom.getRoom());
		if(newRoom==null) return;
		chatRoom.changeRoom(newRoom);
		userListModel.clear();
		chatRoom.updateUserList();
	}

	//-- build gui
	private void buildChatPanel() {
		chatRoomTextPane1 = new JTextPane(getChatDoc());
		//chatRoomTextPane1.setDocument(getChatDoc());
        chatRoomTextPane1.setEditable(false);
        chatRoomTextPane1.setMinimumSize(new java.awt.Dimension(500, 200));
        chatRoomTextPane1.setPreferredSize(new java.awt.Dimension(500, 300));

        chatRoomScrollPane = new JScrollPane(chatRoomTextPane1);
		chatRoomScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	}

	private void buildSendMsg() {
		jPanel2 = new javax.swing.JPanel();
		jPanel2.setLayout(new java.awt.GridBagLayout());
        jPanel2.setMinimumSize(new java.awt.Dimension(373, 40));
        jPanel2.setPreferredSize(new java.awt.Dimension(373, 50));

		sendText = new javax.swing.JTextField();
        sendText.setEnabled(false);
        sendText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
				java.awt.EventQueue.invokeLater(new Runnable() {
		            public void run() {
				        sendTextActionPerformed(evt);
					}
				});
            }
        });

        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        jPanel2.add(sendText, gridBagConstraints);

		sendButton = new javax.swing.JButton();
        sendButton.setText("Send");
        sendButton.setEnabled(false);
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
				java.awt.EventQueue.invokeLater(new Runnable() {
		            public void run() {
				        sendButtonActionPerformed(evt);
					}
				});
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        jPanel2.add(sendButton, gridBagConstraints);

		sendPrivateButton = new javax.swing.JButton();
        sendPrivateButton.setText("Private Mesage");
        sendPrivateButton.setEnabled(false);
        sendPrivateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
				java.awt.EventQueue.invokeLater(new Runnable() {
		            public void run() {
				        sendPrivateButtonActionPerformed(evt);
					}
				});
            }
        });

		gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        jPanel2.add(sendPrivateButton, gridBagConstraints);
	}

	private void buildLogPanel() {
		logTextScrollPane = new javax.swing.JScrollPane();
		logTextScrollPane.setMinimumSize(new java.awt.Dimension(24, 50));
        logTextScrollPane.setPreferredSize(new java.awt.Dimension(11, 50));

		logTextPane = new JTextPane(getLogDoc());
        //logTextPane.setDocument(getLogDoc());
        logTextPane.setEditable(false);
        logTextScrollPane.setViewportView(logTextPane);
	}

	private void buildUserListPanel() {
		jScrollPane1 = new javax.swing.JScrollPane();        
		jScrollPane1.setPreferredSize(new java.awt.Dimension(70, 132));

		userList = new javax.swing.JList(getUserListModel());
        //userList.setModel(getUserListModel());
        userList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(userList);
	}

	private void buildMenu() {
		jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
		jMenu1.setText("Chat");

		loginMenuItem = new JMenuItem("Login...");
        loginMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginMenuItemActionPerformed(evt);
            }
        });

        jMenu1.add(loginMenuItem);


		JMenu optionsjMenu = new javax.swing.JMenu();
		optionsjMenu.setText("Options");

		updateUserListMenuItem = new JMenuItem("Update UserList");
        updateUserListMenuItem.setEnabled(false);
        updateUserListMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
				java.awt.EventQueue.invokeLater(new Runnable() {
		            public void run() {
		                 updateUserList();
					}
				});
            }
        });
        optionsjMenu.add(updateUserListMenuItem);

		changeRoomMenuItem = new JMenuItem("Change Room...");
        changeRoomMenuItem.setEnabled(false);
        changeRoomMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
				java.awt.EventQueue.invokeLater(new Runnable() {
		            public void run() {
		                changeRoom();
					}
				});
            }
        });
        optionsjMenu.add(changeRoomMenuItem);
	
		
		logoutMenuItem = new JMenuItem("Logout");
        logoutMenuItem.setEnabled(false);
        logoutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(logoutMenuItem);

		exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.exit(0);
            }
        });
        jMenu1.add(exitMenuItem);

		clearMenuItem = new JMenuItem("Clear Chat");
        clearMenuItem.setEnabled(true);
        clearMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chatRoomTextPane1.setText("");
				logTextPane.setText("");
            }
        });
        optionsjMenu.add(clearMenuItem);

		jMenu2 = new javax.swing.JMenu();
		jMenu2.setText("Help");

		aboutMenuItem = new JMenuItem("About...");
        aboutMenuItem.setEnabled(true);
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
				java.awt.EventQueue.invokeLater(new Runnable() {
		            public void run() {
		                about();
					}
				});
            }
        });
        jMenu2.add(aboutMenuItem);

        jMenuBar1.add(jMenu1);
		jMenuBar1.add(optionsjMenu);
		jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);
	}

	private boolean checkForSmile(String message, AttributeSet style) throws BadLocationException {
		if(message.length()==0) return false;
		int loc = message.indexOf(":-)");

		int start = 0;
		String temp = null;
		while(loc!=-1) {
			if(loc!=start) {
				temp = message.substring(start, loc);
				if(checkForSad(temp, style)==false) {
					getChatDoc().insertString(getChatDoc().getLength(), temp, style);
				}
			}
			setEndSelection();
			chatRoomTextPane1.insertIcon(smile);
			loc = loc+3;
			start = loc;
			if(loc>=message.length()) break;			
			loc = message.indexOf(":-)", start);
		}
		if(start<message.length()) {
			temp = message.substring(start, message.length());
			if(checkForSad(temp, style)==false) {
				getChatDoc().insertString(getChatDoc().getLength(), temp, style);
			}
		}
		return true;
	}

	private boolean checkForSad(String message, AttributeSet style) throws BadLocationException {
		int loc = message.indexOf(":-(");
		if(message.length()==0) return false;

		int start = 0;
		String temp = null;
		while(loc!=-1) {
			if(loc!=start) {
				temp = message.substring(start, loc);
				getChatDoc().insertString(getChatDoc().getLength(), temp, style);
			}
			setEndSelection();
			chatRoomTextPane1.insertIcon(sad);
			loc = loc+3;
			start = loc;
			if(loc>=message.length()) break;			
			loc = message.indexOf(":-(", start);
		}
		if(start<message.length()) {
			temp = message.substring(start, message.length());
			getChatDoc().insertString(getChatDoc().getLength(), temp, style);
		}
		return true;
	}
    
	private void updateUserList() {
		java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
				chatRoom.updateUserList();
				userListModel.clear();
			}
		});
	}
}

class UserListListener implements ListSelectionListener {
    private String userSelected = null;
    private JList list;
    
    public UserListListener(JList list) {
        this.list = list;
    }
    public String getSelecteduser() {
        return userSelected;
    }
    public void valueChanged(ListSelectionEvent e) {
        if(e.getValueIsAdjusting() == false) {
            int index = list.getSelectedIndex();
            if(index==-1)
                userSelected = null;
            else
                userSelected = list.getSelectedValue().toString();
        }
    }
}
