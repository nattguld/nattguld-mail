package com.nattguld.mail.client.connections;

import java.util.List;
import java.util.Objects;

import com.nattguld.mail.client.MailClient;
import com.nattguld.util.Misc;

/**
 * 
 * @author randqm
 *
 */

public abstract class MailClientConnection {
	
	/**
	 * Whether we checked junk or not.
	 */
	private boolean checkedJunk;
	
	/**
	 * The email client we're connected to.
	 */
	private MailClient client;
	
	
	
	/**
	 * Creates a new email client connection.
	 * 
	 * @param client The email client.
	 */
	public MailClientConnection(MailClient client) {
		this.client = client;
	}
	
	/**
	 * Gets a link from an email message.
	 * 
	 * @param subject The email subject.
	 * 
	 * @param verifier The link verifier.
	 * 
	 * @param timeout The search timeout.
	 * 
	 * @return The link.
	 */
	public String getConfirmationLink(String subject, String verifier, int timeout) {
		return getConfirmationLink(null, subject, verifier, timeout);
	}
	
	/**
	 * Gets a link from an email message.
	 * 
	 * @param sender The email sender.
	 * 
	 * @param subject The email subject.
	 * 
	 * @param verifier The link verifier.
	 * 
	 * @param timeout The search timeout.
	 * 
	 * @return The link.
	 */
	public String getConfirmationLink(String sender, String subject, String verifier, int timeout) {
		List<String> confirmationsLinks = getConfirmationLinks(sender, subject, verifier, timeout);
		return (Objects.isNull(confirmationsLinks) || confirmationsLinks.isEmpty()) ? null : confirmationsLinks.get(0);
	}
	
	/**
	 * Gets links from an email message.
	 * 
	 * @param subject The email subject.
	 * 
	 * @param verifier The link verifier.
	 * 
	 * @param timeout The search timeout.
	 * 
	 * @return The link.
	 */
	public List<String> getConfirmationLinks(String subject, String verifier, int timeout) {
		return getConfirmationLinks(null, subject, verifier, timeout);
	}
	
	/**
	 * Gets links from an email message.
	 * 
	 * @param sender The email sender.
	 * 
	 * @param subject The email subject.
	 * 
	 * @param verifier The link verifier.
	 * 
	 * @param timeout The search timeout.
	 * 
	 * @return The link.
	 */
	public List<String> getConfirmationLinks(String sender, String subject, String verifier, int timeout) {
		int timeElapsed = 0;
		
		Misc.sleep(15000);
		
		while (timeElapsed <= timeout) {
			timeElapsed += 15;
			
			Misc.sleep(15000);
			
			List<String> extractedLinks = extractLinks(sender, subject, verifier);
			
			if (Objects.nonNull(extractedLinks) && !extractedLinks.isEmpty()) {
				disconnect();
				return extractedLinks;
			}
		}
		disconnect();
		return null;
	}
	
	/**
	 * Attempts to retrieve confirmation links from emails.
	 * 
	 * @param sender The email sender.
	 * 
	 * @param subject The email subject.
	 * 
	 * @param verifier The verifier for finding the confirmation links.
	 * 
	 * @return The plausible confirmation links.
	 */
	protected abstract List<String> extractLinks(String sender, String subject, String verifier);
	
	/**
	 * Disconnects the connection to the email client.
	 */
	protected void disconnect() {
		if (Objects.nonNull(client)) {
			client.removeConnection(this);
		}
	}
	
	/**
	 * Modifies whether we checked junk or not.
	 * 
	 * @param checkedJunk The new state.
	 * 
	 * @return The connection.
	 */
	public MailClientConnection setCheckedJunk(boolean checkedJunk) {
		this.checkedJunk = checkedJunk;
		return this;
	}
	
	/**
	 * Retrieves whether we checked junk or not.
	 * 
	 * @return The result.
	 */
	public boolean hasCheckedJunk() {
		return checkedJunk;
	}
	
	/**
	 * Retrieves the client we're connected to.
	 * 
	 * @return The client.
	 */
	public MailClient getClient() {
		return client;
	}

}
