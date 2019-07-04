package com.nattguld.mail.client;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.nattguld.mail.MailManager;
import com.nattguld.mail.client.connections.MailClientConnection;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;
import com.nattguld.util.logging.Logger;

/**
 * 
 * @author randqm
 *
 */

public abstract class MailClient {
	
	/**
	 * The email credentials.
	 */
	private StringKeyValuePair creds;
	
    /**
     * The logger instance.
     */
    private final Logger logger;
    
    /**
     * The connections to this email client.
     */
    private final List<MailClientConnection> connections;
	
	
	/**
	 * Creates a new email client.
	 * 
	 * @param creds The email credentials.
	 */
	public MailClient(StringKeyValuePair creds) {
		this.creds = creds;
		this.logger = new Logger(creds.getKey());
		this.connections = new CopyOnWriteArrayList<>();
	}
	
	/**
	 * Opens the email client.
	 * 
	 * @return Whether the email inbox was opened successfuly or not.
	 */
	public abstract boolean open();
	
	/**
	 * Closes the email inbox.
	 */
	public abstract void close();
	
	/**
	 * Adds a new connection to the client.
	 * 
	 * @param connection The new connection.
	 */
	public void addConnection(MailClientConnection connection) {
		connections.add(connection);
	}
	
	/**
	 * Removes a connection to the client.
	 * 
	 * @param connection The connection to remove.
	 */
	public void removeConnection(MailClientConnection connection) {
		connections.remove(connection);
		
		if (connections.isEmpty()) {
			MailManager.getSingleton().removeClient(this);
		}
	}
	
	/**
	 * Modifies the credentials.
	 * 
	 * @param creds The new credentials.
	 * 
	 * @return The email client.
	 */
	public MailClient setCreds(StringKeyValuePair creds) {
		this.creds = creds;
		return this;
	}
    
    /**
     * Retrieves the email client credentials.
     * 
     * @return The credentials.
     */
    public StringKeyValuePair getCreds() {
    	return creds;
    }
    
    /**
     * Retrieves the logger instance.
     * 
     * @return The logger instance.
     */
    public Logger getLogger() {
    	return logger;
    }

}
