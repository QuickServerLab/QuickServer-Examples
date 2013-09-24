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

package chatserver;

import org.quickserver.net.server.*;
import java.io.*;
import java.util.*;
import org.quickserver.net.qsadmin.Authenticator;

/**
 * ChatServer Authenticator.
 * <p>
 * Username : MyAdmin<br>
 * Password : password
 * </p>
 * @since 1.1
 * @author  Akshathkumar Shetty
 */
public class QsAdminAuthenticator extends Authenticator {

	protected static boolean validate(String username, byte[] password) {
		return username.equals("MyAdmin") && 
			Arrays.equals(password,"password".getBytes());
	}
}
