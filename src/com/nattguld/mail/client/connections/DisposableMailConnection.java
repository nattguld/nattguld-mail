package com.nattguld.mail.client.connections;

import com.nattguld.mail.client.DisposableMailClient;
import com.nattguld.mail.client.MailClient;

/**
 * 
 * @author randqm
 *
 */

public abstract class DisposableMailConnection extends MailClientConnection {

	
	/**
	 * Creates a new disposable mail.
	 */
	public DisposableMailConnection(MailClient client) {
		super(client);
	}
	
	@Override
	public DisposableMailClient getClient() {
		return (DisposableMailClient)super.getClient();
	}
	
}
