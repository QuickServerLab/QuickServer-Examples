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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.io.IOException;

/**
 * Login Dialog GUI
 * @author Akshathkumar Shetty
 */
public class LoginDialog extends JDialog {
    
    private JPanel topPanel;
    private JPanel ipPanel;
    private JPanel authPanel;
    private JPanel buttonPanel;
    
    private JLabel productName;
    private JLabel ipLabel;
    private JTextField ipField;
    private JLabel portLabel;
    private JTextField portField;
    private JLabel loginLabel;
    private JTextField loginField;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    
    private String statusTxt1 = "<html><font style=\"font-size:15pt;color:#535353\"><b>";
    private String statusTxt2 = "</b></font>";
    private GridBagConstraints gbc;
    
    //for storing the values
    private String values[] = new String[4];
    private boolean isOk = false;
    
    public LoginDialog(Frame parent, String args[]) {
        super(parent, "Please Login");
        gbc = new GridBagConstraints();
        productName = new JLabel(statusTxt1+
                "Chat Login"+statusTxt2,JLabel.CENTER);
        
        ipLabel = new JLabel("IP Address");
		if(args!=null && args.length>=2) 
			ipField = new JTextField(args[1]);
		else
	        ipField = new JTextField("127.0.0.1");
        portLabel = new JLabel("Port");
		if(args!=null && args.length>=3) 
			ipField = new JTextField(args[2]);
		else
			portField = new JTextField("7412");
        
        loginLabel = new JLabel("Login");
        loginField = new JTextField("user1");
        passwordLabel = new JLabel("Password");
        passwordField = new JPasswordField("user1");
        
        loginButton = new JButton("Login");
        loginButton.setMnemonic('L');
        cancelButton = new JButton("Cancel");
        cancelButton.setMnemonic('C');
        cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        
        //--- Action
        ipField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                portField.requestFocus();
            }
        });
        portField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginField.requestFocus();
            }
        });
        loginField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                passwordField.requestFocus();
            }
        });
        
        ActionListener loginAl = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isOk = false;
                if(ipField.getText().equals("")) {
                    showError("Blank IP Address");
                    return;
                }
                if(portField.getText().equals("")) {
                    showError("Blank Port Number");
                    return;
                } else {
                    try {
                        Integer.parseInt(portField.getText());
                    } catch(Exception ex) {
                        showError("Bad Port Number.");
                        return;
                    }
                }
                if(loginField.getText().equals("")) {
                    showError("Blank Login");
                    return;
                }
                char p[] = passwordField.getPassword();
                if(p==null || p.length==0) {
                    showError("Blank password");
                    return;
                }
                p = null;
				isOk = true;
                hide();                
            }
        };
        
        loginButton.addActionListener(loginAl);
        passwordField.addActionListener(loginAl);
        
        cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				isOk = false;
                hide();             
            }
        });
        //---- Action
        
        Container cp = getContentPane();
        
        //--- Top Panel
        topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        gbc.insets = new Insets( 2, 2, 2, 2 );
        gbc.weighty = 0.0;
        gbc.weightx = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        topPanel.add(productName, gbc);
        
        //-- IP Panel
        ipPanel = new JPanel();
        ipPanel.setLayout(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        ipPanel.add(ipLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        ipPanel.add(ipField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        ipPanel.add(portLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        ipPanel.add(portField, gbc);
        ipPanel.setBorder(BorderFactory.createTitledBorder(
                new EtchedBorder(),"Location"));
        
        //-- Login Panel
        authPanel = new JPanel();
        authPanel.setLayout(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        authPanel.add(loginLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        authPanel.add(loginField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        authPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        authPanel.add(passwordField, gbc);
        authPanel.setBorder(BorderFactory.createTitledBorder(
                new EtchedBorder(),"Authentication"));
        
        //-- buttonPanel
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        buttonPanel.add(loginButton, gbc);
        
        gbc.gridx = 1;
        buttonPanel.add(cancelButton, gbc);
        
        cp.setLayout(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(topPanel,gbc);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 1;
        cp.add(ipPanel,gbc);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 2;
        cp.add(authPanel,gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 3;
        cp.add(buttonPanel,gbc);
        pack();
        setSize(240,250);
        setResizable(false);
        setModal(true);
        centerWindow(this);
    }
    
    private void showError(String msg) {
        JOptionPane.showMessageDialog(LoginDialog.this,
                msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public String[] getValues(){
        values[0] = ipField.getText();
        values[1] = portField.getText();
        values[2] = loginField.getText();
        values[3] = new String(passwordField.getPassword());
        return values;
    }
    
    public boolean isOk(){
        return isOk;
    }

	 public void clearStatus(){
        isOk = false;
    }
    
    public static void centerWindow(Window window) {
        Dimension dim = window.getToolkit().getScreenSize();
        window.setLocation(dim.width/2 - window.getWidth()/2,
                dim.height/2 - window.getHeight()/2);
    }
}
