package com.nattguld.mail.client.impl;

import com.nattguld.mail.client.MailClient;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;
import com.nattguld.util.text.TextUtil;

/**
 * 
 * @author randqm
 *
 */

public class FakeMailClient extends MailClient {
	
	
	/**
	 * Creates a fake client.
	 */
	public FakeMailClient() {
		super(new StringKeyValuePair(TextUtil.generateFakeEmail(), "fakemail"));
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