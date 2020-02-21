package com.nattguld.mail.client.connections.impl;

import java.util.ArrayList;
import java.util.List;

import com.nattguld.mail.client.connections.MailClientConnection;
import com.nattguld.mail.client.impl.FakeMailClient;

/**
 * 
 * @author randqm
 *
 */

public class FakeConnection extends MailClientConnection {

	
	/**
	 * Creates a new fake email.
	 * 
	 * @param client The email client.
	 */
	public FakeConnection() {
		super(new FakeMailClient());
	}
	
	@Override
	protected List<String> extractLinks(String sender, String subject, String verifier) {
		return new ArrayList<>();
	}

}
