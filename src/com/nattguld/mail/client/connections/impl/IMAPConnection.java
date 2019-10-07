package com.nattguld.mail.client.connections.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.nattguld.mail.client.MailClient;
import com.nattguld.mail.client.connections.MailClientConnection;
import com.nattguld.mail.client.impl.IMAPClient;
import com.nattguld.util.text.TextUtil;

/**
 * 
 * @author randqm
 *
 */

public class IMAPConnection extends MailClientConnection {

	
    /**
     * Creates a new IMAP connection.
     * 
     * @param client The client we're connecting to.
     */
    public IMAPConnection(MailClient client) {
		super(client);
	}
    
    /**
	 * Retrieves a folder.
	 * 
	 * @param store The email store.
	 * 
	 * @param folderName The name of the folder.
	 * 
	 * @return The folder.
	 * 
	 * @throws MessagingException 
	 */
	public Folder openFolder(String folderName) throws MessagingException {
		if (Objects.isNull(getClient().getStore())) {
			if (!getClient().open()) {
				getClient().getLogger().error("Inbox store is closed");
				return null;
			}
		}
		Folder folder = getClient().getStore().getFolder(folderName);

		if (Objects.isNull(folder)) {
			getClient().getLogger().error("inbox null (" + folderName + ") >> inboxes: " );
    			
			Folder[] f = getClient().getStore().getDefaultFolder().list();
    			
			for (Folder fd : f) {
				getClient().getLogger().debug(">> " + fd.getName());
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
			getClient().getLogger().error("Exception trying to open (" + folderName + ") >> inboxes: " );
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
		if (Objects.isNull(getClient().getStore())) {
			return null;
		}
		try {
			Folder[] folders = getClient().getStore().getDefaultFolder().list();
			
			for (Folder folder : folders) {
				getClient().getLogger().debug(">> " + folder.getName());
			}
			return folders;
	    		
		} catch (Exception ex) {
			getClient().getLogger().exception(ex);
			getClient().getLogger().error("Failed to fetch folders");
		}
		return null;
	}
    
	@Override
	public List<String> extractLinks(String sender, String subject, String verifier) {
		List<Message> emailMessages = new ArrayList<>();
	    	
		try {
			String folderName = hasCheckedJunk() ? getClient().getIMAPDetails().getSpamFolder() : getClient().getIMAPDetails().getInboxFolder();
			setCheckedJunk(!hasCheckedJunk());
			getClient().getLogger().debug("Checking: " + folderName);
			
			Folder folder = openFolder(folderName);
			
			if (Objects.isNull(folder)) {
				getClient().getLogger().error("Failed to open folder: " + folderName);
				return null;
			}
			Message messages[] = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
			
			FetchProfile fp = new FetchProfile();
	    	fp.add(FetchProfile.Item.ENVELOPE);
	    	fp.add(FetchProfile.Item.CONTENT_INFO);
	    	
			//folder.fetch(messages, fp);
			
			/*FetchProfile fp = new FetchProfile();
	    	fp.add(FetchProfile.Item.ENVELOPE);
	    	fp.add(FetchProfile.Item.CONTENT_INFO);*/
			
			try {
				/*Arrays.sort(messages, ( m1, m2 ) -> {
					try {
						return m2.getSentDate().compareTo( m1.getSentDate() );
					} catch ( MessagingException e ) {
						throw new RuntimeException( e );
					}
				});*/
				for (int i = 0; i < messages.length; i ++) {
					Message message = messages[i];
					Address[] a = message.getFrom();
	    				
					if (Objects.isNull(a) || a.length == 0) {
						continue;
					}
					if (Objects.nonNull(sender) && !a[0].toString().toLowerCase().contains(sender.toLowerCase())) {
						continue;
					}
					String sub = message.getSubject();
					//getLogger().debug("Subject: " + sub);
	            	    
					if (Objects.nonNull(sub) && !sub.isEmpty() && sub.toLowerCase().contains(subject.toLowerCase())) {
						emailMessages.add(message);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				getClient().getLogger().exception(ex);
				getClient().getLogger().error("Exception occurred while fetching emails");
			}
			if (emailMessages.isEmpty()) {
				folder.close(true);
				return null;
			}
			List<String> extractedLinks = new ArrayList<String>();
			getClient().getLogger().debug("Checking " + emailMessages.size() + " emails for confirmation link");
	    	
			for (Message message : emailMessages) {
				try {
					Object messageContent = message.getContent();
	    				
					if (Objects.isNull(messageContent)) {
						String contentStr = IMAPConnection.getMessageBody(message);
	        				
						if (Objects.isNull(contentStr) || contentStr.isEmpty()) {
							getClient().getLogger().error("Failed to extract email message content");
							return null;
						}
						extractedLinks.addAll(IMAPConnection.extractFromPlainText(contentStr, verifier));
	    					
					} else if (message.isMimeType("text/html")) {
						//emailMessageContent = Jsoup.parse(messageContent.toString()).text();
						String contentStr = messageContent.toString();
						Document contentDoc = Jsoup.parse(contentStr);
						extractedLinks.addAll(IMAPConnection.extractFromHTML(contentDoc, verifier));
	    					
					} else if (message.isMimeType("text/plain")) {
						String contentStr = messageContent.toString();
						extractedLinks.addAll(IMAPConnection.extractFromPlainText(contentStr, verifier));
	    					
					} else if (message.isMimeType("multipart/*")) {
						MimeMultipart mimeMultipart = (MimeMultipart)message.getContent();
						String contentStr = IMAPConnection.getTextFromMimeMultipart(mimeMultipart);
	    					
						Pattern htmlPattern = Pattern.compile(".*\\<[^>]+>.*", Pattern.DOTALL);
						boolean isHTML = htmlPattern.matcher(contentStr).matches();
	    					
						if (isHTML) {
							Document contentDoc = Jsoup.parse(contentStr);
							extractedLinks.addAll(IMAPConnection.extractFromHTML(contentDoc, verifier));
	        					
						} else {
							extractedLinks.addAll(IMAPConnection.extractFromPlainText(contentStr, verifier));
						}
					}
					message.setFlag(Flags.Flag.DELETED, true);
	    				
				} catch (Exception ex) {
					getClient().getLogger().exception(ex);
					getClient().getLogger().error("Exception occurred while fetching email message");
				}
			}
			folder.close(true);
	            
			if (extractedLinks.isEmpty()) {
				return null;
			}
			return extractedLinks;
	    		
		} catch (MessagingException ex) {
			getClient().getLogger().exception(ex);
			getClient().getLogger().error("Exception occurred while searching confirmation links");
			return null;
		}
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
		List<String> extractedUrls = TextUtil.extractUrls(contentStr);
		
		for (String url : extractedUrls) {
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
	
	@Override
	public IMAPClient getClient() {
		return (IMAPClient)super.getClient();
	}

}
