package com.nattguld.mail.client;

import java.util.List;
import java.util.Objects;

import com.nattguld.mail.inbox.EmailInbox;
import com.nattguld.util.Misc;

/**
 * 
 * @author randqm
 *
 */

public abstract class EmailClient<T extends EmailInbox> {
	
	/**
	 * The accessed email inbox.
	 */
	private final T inbox;
	
	
	/**
	 * Creates a new email client.
	 * 
	 * @param inbox The email inbox the client is accessing.
	 */
	public EmailClient(T inbox) {
		this.inbox = inbox;
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
				dispose();
				return extractedLinks;
			}
		}
		dispose();
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
     * Disposes the client's inbox connect.
     */
    public abstract void dispose();
	
	/**
	 * Retrieves the accessed email inbox.
	 * 
	 * @return The inbox.
	 */
	public T getInbox() {
		return inbox;
	}

}
