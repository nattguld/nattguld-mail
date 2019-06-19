package com.nattguld.mail.client.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.nattguld.mail.MailManager;
import com.nattguld.mail.client.EmailClient;
import com.nattguld.mail.inbox.impl.IMAPInbox;

/**
 * 
 * @author randqm
 *
 */

public class IMAPClient extends EmailClient<IMAPInbox> {
	
	/**
	 * Whether we checked junk or not.
	 */
	private boolean checkedJunk;
	
	
	/**
	 * Creates a new IMAP client.
	 * 
	 * @param inbox The IMAP inbox the client is accessing.
	 */
	public IMAPClient(IMAPInbox inbox) {
		super(inbox);
	}

	@Override
	public List<String> extractLinks(String sender, String subject, String verifier) {
		List<Message> emailMessages = new ArrayList<>();
	    	
		try {
			String folderName = hasCheckedJunk() ? getInbox().getIMAPDetails().getSpamFolder() : getInbox().getIMAPDetails().getInboxFolder();
			setCheckedJunk(!hasCheckedJunk());
			getInbox().getLogger().debug("Checking: " + folderName);
			
			Folder folder = getInbox().openFolder(folderName);
			
			if (Objects.isNull(folder)) {
				getInbox().getLogger().error("Failed to open folder: " + folderName);
				return null;
			}
			Message messages[] = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
			
			FetchProfile fp = new FetchProfile();
	    	fp.add(FetchProfile.Item.ENVELOPE);
	    	fp.add(FetchProfile.Item.CONTENT_INFO);
	    	
			folder.fetch(messages, fp);
			
			/*FetchProfile fp = new FetchProfile();
	    	fp.add(FetchProfile.Item.ENVELOPE);
	    	fp.add(FetchProfile.Item.CONTENT_INFO);*/
			
			try {
				Arrays.sort(messages, ( m1, m2 ) -> {
					try {
						return m2.getSentDate().compareTo( m1.getSentDate() );
					} catch ( MessagingException e ) {
						throw new RuntimeException( e );
					}
				});
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
				getInbox().getLogger().exception(ex);
				getInbox().getLogger().error("Exception occurred while fetching emails");
			}
			if (emailMessages.isEmpty()) {
				folder.close(true);
				return null;
			}
			List<String> extractedLinks = new ArrayList<String>();
			getInbox().getLogger().debug("Checking " + emailMessages.size() + " emails for confirmation link");
	    	
			for (Message message : emailMessages) {
				try {
					Object messageContent = message.getContent();
	    				
					if (Objects.isNull(messageContent)) {
						String contentStr = IMAPInbox.getMessageBody(message);
	        				
						if (Objects.isNull(contentStr) || contentStr.isEmpty()) {
							getInbox().getLogger().error("Failed to extract email message content");
							return null;
						}
						extractedLinks.addAll(IMAPInbox.extractFromPlainText(contentStr, verifier));
	    					
					} else if (message.isMimeType("text/html")) {
						//emailMessageContent = Jsoup.parse(messageContent.toString()).text();
						String contentStr = messageContent.toString();
						Document contentDoc = Jsoup.parse(contentStr);
						extractedLinks.addAll(IMAPInbox.extractFromHTML(contentDoc, verifier));
	    					
					} else if (message.isMimeType("text/plain")) {
						String contentStr = messageContent.toString();
						extractedLinks.addAll(IMAPInbox.extractFromPlainText(contentStr, verifier));
	    					
					} else if (message.isMimeType("multipart/*")) {
						MimeMultipart mimeMultipart = (MimeMultipart)message.getContent();
						String contentStr = IMAPInbox.getTextFromMimeMultipart(mimeMultipart);
	    					
						Pattern htmlPattern = Pattern.compile(".*\\<[^>]+>.*", Pattern.DOTALL);
						boolean isHTML = htmlPattern.matcher(contentStr).matches();
	    					
						if (isHTML) {
							Document contentDoc = Jsoup.parse(contentStr);
							extractedLinks.addAll(IMAPInbox.extractFromHTML(contentDoc, verifier));
	        					
						} else {
							extractedLinks.addAll(IMAPInbox.extractFromPlainText(contentStr, verifier));
						}
					}
					message.setFlag(Flags.Flag.DELETED, true);
	    				
				} catch (Exception ex) {
					getInbox().getLogger().exception(ex);
					getInbox().getLogger().error("Exception occurred while fetching email message");
				}
			}
			folder.close(true);
	            
			if (extractedLinks.isEmpty()) {
				return null;
			}
			return extractedLinks;
	    		
		} catch (MessagingException ex) {
			getInbox().getLogger().exception(ex);
			getInbox().getLogger().error("Exception occurred while searching confirmation links");
			return null;
		}
	}
	
	@Override
	public void dispose() {
		MailManager.getSingleton().removeClient(this);
	}
	
	/**
	 * Modifies whether we checked junk or not.
	 * 
	 * @param checkedJunk
	 */
	public void setCheckedJunk(boolean checkedJunk) {
		this.checkedJunk = checkedJunk;
	}
	
	/**
	 * Retrieves whether we checked junk or not.
	 * 
	 * @return The result.
	 */
	public boolean hasCheckedJunk() {
		return checkedJunk;
	}
	
	@Override
	public IMAPInbox getInbox() {
		return super.getInbox();
	}

}
