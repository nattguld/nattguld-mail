package com.nattguld.mail.inbox;

import java.util.Objects;

import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;
import com.nattguld.util.logging.Logger;

/**
 * 
 * @author randqm
 *
 */

public abstract class EmailInbox {
	
	/**
	 * The email credentials.
	 */
	private StringKeyValuePair creds;
	
    /**
     * The logger instance.
     */
    private Logger logger;
	
	
	/**
	 * Creates a new email.
	 * 
	 * @param creds The email credentials.
	 */
	public EmailInbox(StringKeyValuePair creds) {
		this.creds = creds;
	}
	
	/**
	 * Opens and retrieves an email inbox instance.
	 * 
	 * @param client The email client opening the inbox.
	 * 
	 * @return Whether the email inbox was opened successfuly or not.
	 */
	public abstract boolean open();
	
	/**
	 * Closes the email inbox.
	 */
	public abstract void close();
	
	/**
	 * Modifies the email credentials.
	 * 
	 * @param creds The new credentials.
	 */
	protected void setCredentials(StringKeyValuePair creds) {
		this.creds = creds;
	}
	
	/**
	 * Retrieves the email credentials.
	 * 
	 * @return The credentials.
	 */
	public StringKeyValuePair getCredentials() {
		return creds;
	}
	
	/**
	 * Retrieves the logger instance.
	 * 
	 * @return The logger instance.
	 */
	public Logger getLogger() {
		if (Objects.isNull(logger)) {
			if (Objects.isNull(getCredentials())) {
				return new Logger("Unassigned Email");
			}
			this.logger = new Logger("[" + getCredentials().getKey() + "]");
		}
		return logger;
	}

}
