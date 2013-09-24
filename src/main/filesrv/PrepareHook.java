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

import org.quickserver.net.server.*;
import org.quickserver.net.ServerHook;
import java.util.*;

/**
 * PrepareHook
 * @author Akshathkumar Shetty
 */
public class PrepareHook implements ServerHook {
	private QuickServer quickserver;

	public String info() {
		return "Prepare Hook";
	}

	public void initHook(QuickServer quickserver) {
		this.quickserver = quickserver;
	}

	public boolean handleEvent(int event) {
		if(event==ServerHook.PRE_STARTUP) {
			HashMap appConfig = 
				quickserver.getConfig().getApplicationConfiguration();
			if(appConfig==null)
				return false;

			String temp = (String)appConfig.get("HFTP_ROOT");
			Data.setUserRootHome(temp);
			return true;
		}
		return false;
	}
}
