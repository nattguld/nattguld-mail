package com.nattguld.mail.inbox.impl;

import com.nattguld.mail.inbox.EmailInbox;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;

/**
 * 
 * @author randqm
 *
 */

public class ManualInbox extends EmailInbox {

	
	/**
	 * Creates a new manual email.
	 * 
	 * @param creds The credentials.
	 */
	public ManualInbox(StringKeyValuePair creds) {
		super(creds);
	}

	@Override
	public boolean open() {
		return true;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
