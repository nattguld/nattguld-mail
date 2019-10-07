package com.nattguld.mail.client.impl;

import com.nattguld.mail.client.MailClient;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;

/**
 * 
 * @author randqm
 *
 */

public class ManualMailClient extends MailClient {

	
	/**
	 * Creates a new manual client.
	 */
	public ManualMailClient() {
		super(new StringKeyValuePair("manual", "manual"));
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