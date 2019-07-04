package com.nattguld.mail.client.impl;

import java.util.Objects;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import com.nattguld.mail.IMAPDetails;
import com.nattguld.mail.client.MailClient;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;

/**
 * 
 * @author randqm
 *
 */

public class IMAPClient extends MailClient {
	
    /**
     * Regex for url's.
     */
    protected static final String URL_REGEX = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*[-a-zA-Z0-9+&@#/%=~_|])";
    
    /**
     * The IMAP email service details.
     */
    private final IMAPDetails imapDetails;
    
    /**
     * The store instance.
     */
    private Store store;
	
	
	/**
	 * Creates a new IMAP client.
	 * 
	 * @param creds The email credentials.
	 * 
	 * @param inbox The IMAP inbox the client is accessing.
	 */
	public IMAPClient(StringKeyValuePair creds, IMAPDetails imapDetails) {
		super(creds);
		
		this.imapDetails = imapDetails;
	}

	@Override
	public boolean open() {
		try {
			getLogger().debug("Authenticating with inbox [" + getCreds().getKey() + "]");
			
			Properties properties = new Properties();
			properties.put("mail.store.protocol", "imaps");
        
			Session session = Session.getInstance(properties, null);
			
			String username = getCreds().getKey();
        
			//For disposable yahoo emails
			if (imapDetails.getImapAddress().equalsIgnoreCase("imap.mail.yahoo.com")) {
				if (username.contains("-")) {
					username = username.split("-")[0] + "@yahoo.com";
				}
			}
			store = session.getStore();
			store.connect(imapDetails.getImapAddress(), imapDetails.getImapPort(), username, getCreds().getValue());
			
		} catch (AuthenticationFailedException ex) {
			ex.printStackTrace();
			getLogger().error("Unable to use " + getCreds().getKey() + "! Check if the email has no checkpoints and if unsecure apps (gmail) is turned on.");
			getLogger().exception(ex);
            close();
            return false;
            
        } catch (MessagingException ex) {
        	getLogger().exception(ex);
        	getLogger().error("Message error occurred while building javax email");
            close();
			return false;
		}
		return true;
	}
	
    @Override
    public void close() {
    	if (Objects.nonNull(store)) {
    		try {
    			store.close();
    		} catch (MessagingException ex) {
                getLogger().exception(ex);
                getLogger().error("Exception occurred while trying to close email inbox");
            }
    		store = null;
    	}
    }
    
    /**
     * Retrieves the IMAP details.
     * 
     * @return The details.
     */
    public IMAPDetails getIMAPDetails() {
    	return imapDetails;
    }
    
    /**
     * Retrieves the store.
     * 
     * @return The store.
     */
    public Store getStore() {
    	return store;
    }

}
