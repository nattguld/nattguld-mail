package com.nattguld.mail.inbox.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.nattguld.mail.IMAPDetails;
import com.nattguld.mail.inbox.EmailInbox;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;

/**
 * 
 * @author randqm
 *
 */

public class IMAPInbox extends EmailInbox {
	
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
     * Creates a new IMAP email handler.
     * 
     * @param imapDetails The IMAP email service details.
     * 
     * @param creds The credentials.
     */
    public IMAPInbox(IMAPDetails imapDetails, StringKeyValuePair creds) {
    	super(creds);
    	
    	this.imapDetails = imapDetails;
    }
    
	@Override
	public boolean open() {
		try {
			getLogger().debug("Authenticating with inbox [" + getCredentials().getKey() + "]");
			
			Properties properties = new Properties();
			properties.put("mail.store.protocol", "imaps");
        
			Session session = Session.getInstance(properties, null);
			
			String username = getCredentials().getKey();
        
			//For disposable yahoo emails
			if (imapDetails.getImapAddress().equalsIgnoreCase("imap.mail.yahoo.com")) {
				if (username.contains("-")) {
					username = username.split("-")[0] + "@yahoo.com";
				}
			}
			store = session.getStore();
			store.connect(imapDetails.getImapAddress(), imapDetails.getImapPort(), username, getCredentials().getValue());
			
		} catch (AuthenticationFailedException ex) {
			ex.printStackTrace();
			getLogger().error("Unable to use " + getCredentials().getKey() + "! Check if the email has no checkpoints and if unsecure apps (gmail) is turned on.");
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
	
	/**
	 * Retrieves a folder.
	 * 
	 * @param folderName The name of the folder.
	 * 
	 * @return The folder.
	 * 
	 * @throws MessagingException 
	 */
	public Folder openFolder(String folderName) throws MessagingException {
		if (Objects.isNull(store)) {
			if (!open()) {
				getLogger().error("Inbox store is closed");
				return null;
			}
		}
		Folder folder = store.getFolder(folderName);

		if (Objects.isNull(folder)) {
			getLogger().error("inbox null (" + folderName + ") >> inboxes: " );
    			
			Folder[] f = store.getDefaultFolder().list();
    			
			for (Folder fd : f) {
				getLogger().debug(">> " + fd.getName());
			}
			return null;
		}
		/*if (!inbox.isOpen()) {
			getLogger().warning("Inbox (" + folder + ") not open!");
			return null;
		}*/
		try {
			folder.open(Folder.READ_WRITE);
    			
		} catch (MessagingException ex) {
			getLogger().error("Exception trying to open (" + folderName + ") >> inboxes: " );
			return null;
		}
		return folder;
	}
	
	/**
	 * Retrieves the available folders.
	 * 
	 * @return The folders.
	 * 
	 * @throws MessagingException
	 */
	public Folder[] getFolders() {
		if (Objects.isNull(store)) {
			return null;
		}
		try {
			Folder[] folders = store.getDefaultFolder().list();
			
			for (Folder folder : folders) {
				getLogger().debug(">> " + folder.getName());
			}
			return folders;
	    		
		} catch (Exception ex) {
			getLogger().exception(ex);
			getLogger().error("Failed to fetch folders");
		}
		return null;
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
	 * Extracts links from plain text using an identifier.
	 * 
	 * @param contentStr The content string.
	 * 
	 * @param verifier The verifier.
	 * 
	 * @return The extracted links.
	 */
	public static List<String> extractFromPlainText(String contentStr, String verifier) {
		if (contentStr.startsWith("<html>") && contentStr.endsWith("</html>")) {
			return extractFromHTML(Jsoup.parse(contentStr), verifier);
		}
		List<String> extractedLinks = new ArrayList<>();

		Pattern pattern = Pattern.compile(URL_REGEX, Pattern.CASE_INSENSITIVE);
		Matcher urlMatcher = pattern.matcher(contentStr);

		while (urlMatcher.find()) {
			String url = contentStr.substring(urlMatcher.start(0), urlMatcher.end(0));

			if (url.contains(verifier)) {
				extractedLinks.add(url);
			}
		}
		return extractedLinks;
	}
	    
	/**
	 * Extracts links from html using an identifier.
	 * 
	 * @param contentDoc The content document.
	 * 
	 * @param verifier The verifier.
	 * 
	 * @return The extracted links.
	 */
	public static List<String> extractFromHTML(Document contentDoc, String verifier) {
		List<String> extractedLinks = new ArrayList<>();
	    	
		for (Element a : contentDoc.getElementsByTag("a")) {
			String anchor = a.text();
			String href = a.hasAttr("href") ? a.attr("href") : "";
				
			if (href.isEmpty()) {
				continue;
			}
			if (href.toLowerCase().contains(verifier.toLowerCase())) {
				extractedLinks.add(href);
				continue;
			}
			if (!anchor.isEmpty() && anchor.toLowerCase().contains(verifier.toLowerCase())) {
				extractedLinks.add(href);
				continue;
			}
		}
		return extractedLinks;
	}
	    
	/**
	 * Retrieves the message body.
	 * 
	 * @param msgContent The message content.
	 * 
	 * @return The message body.
	 * 
	 * @throws MessagingException
	 */
	public static String getMessageBody(Object msgContent) throws MessagingException {
		MimeMessage msg = (MimeMessage)msgContent;
		String s = "";
		       
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		try {
			msg.writeTo(out);
			s = out.toString();
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return s;
	}
	    
	/**
	 * Retrieves the text from a mime part.
	 * 
	 * @param mimeMultipart The mime part.
	 * 
	 * @return The text.
	 * 
	 * @throws MessagingException
	 * @throws IOException
	 */
	public static String getTextFromMimeMultipart(MimeMultipart mimeMultipart)  throws MessagingException, IOException{
		String result = "";
		int count = mimeMultipart.getCount();
	        
		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
	            
			if (bodyPart.isMimeType("text/plain")) {
				result = result + "\n" + bodyPart.getContent();
				//break; // without break same text appears twice in my tests
			} else if (bodyPart.isMimeType("text/html")) {
				String html = (String) bodyPart.getContent();
				//TODO result = result + "\n" + Jsoup.parse(html).text();
				result = result + Jsoup.parse(html).html();
				
			} else if (bodyPart.getContent() instanceof MimeMultipart) {
				result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
			}
		}
		return result;
	}

}
