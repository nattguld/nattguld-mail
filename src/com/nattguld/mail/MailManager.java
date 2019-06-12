package com.nattguld.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import com.nattguld.data.json.JsonReader;
import com.nattguld.data.json.JsonResourceManager;
import com.nattguld.mail.client.impl.IMAPClient;
import com.nattguld.mail.client.impl.ManualMailClient;
import com.nattguld.mail.client.impl.TempMailClient;
import com.nattguld.mail.inbox.impl.IMAPInbox;
import com.nattguld.mail.inbox.impl.ManualInbox;
import com.nattguld.mail.inbox.impl.TempMailInbox;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;

/**
 * 
 * @author randqm
 *
 */

public class MailManager extends JsonResourceManager<IMAPDetails> {

	/**
	 * The default available IMAP email services.
	 */
	private static final String[] DEFAULT_SERVICES = new String[] {
			"Gmail", "Outlook", "Yahoo", "Mail.ru", "Mail.com"
			, "Onet.pl", "O2.pl"
	};
	
	/**
	 * The singleton instance.
	 */
	private static MailManager singleton;
	
	/**
	 * A list holding the active IMAP clients.
	 */
	private final List<IMAPClient> imapClients = new CopyOnWriteArrayList<>();
	
	
	/**
	 * Loads the default IMAP services.
	 */
	private void loadDefaultServices() {
		for (String defaultService : DEFAULT_SERVICES) {
			IMAPDetails id = getByName(defaultService);
			
			if (Objects.nonNull(id)) {
				continue;
			}
			switch (defaultService) {
			case "Gmail":
				add(new IMAPDetails(defaultService, true, "imap.gmail.com", 993, "INBOX", "[Gmail]/Spam"
						, new String[] {"gmail", "googlemail"}));
				//@NOTE: https://myaccount.google.com/lesssecureapps?pli=1
				break;
				
			case "Outlook":
				add(new IMAPDetails(defaultService, true, "imap-mail.outlook.com", 993, "Inbox", "Junk"
						, new String[] {"outlook", "hotmail"}));
				break;
				
			case "Yahoo":
				add(new IMAPDetails(defaultService, true, "imap.mail.yahoo.com", 993, "Inbox", "Bulk Mail"
						, new String[] {"yahoo"}));
				//@NOTE: https://login.yahoo.com/account/security?.scrumb=l05REsUoppH#other-apps
				break;
				
			case "Mail.ru":
				add(new IMAPDetails(defaultService, true, "imap.mail.ru", 993, "INBOX", "Спам"
						, new String[] {"mail.ru", "bk.ru", "inbox.ru", "list.ru"}));
				break;
				
			case "Mail.com":
				add(new IMAPDetails(defaultService, true, "imap.mail.com", 993, "INBOX", "Spam"
						, new String[] {"Mail.com"}));
				break;
				
			case "Onet.pl":
				add(new IMAPDetails(defaultService, true, "imap.poczta.onet.pl", 993, "INBOX", "Junk"
						, new String[] {"Onet.pl"}));
				break;
				
			case "O2.pl":
				add(new IMAPDetails(defaultService, true, "poczta.o2.pl", 993, "INBOX", "Junk"
						, new String[] {"o2.pl", "tlen.pl", "prokonto.pl", "go2.pl"}));
				break;
			}
		}
	}
	
	@Override
	public void add(IMAPDetails imapDetails) {
		super.add(imapDetails, new Predicate<IMAPDetails>() {
			@Override
			public boolean test(IMAPDetails id) {
				return Objects.isNull(getByName(id.getName()));
			}
		});
	}
	
	/**
	 * Connects to an IMAP inbox and retrieves the client instance.
	 * 
	 * @param creds The inbox credentials.
	 * 
	 * @return The client instance.
	 */
	public IMAPClient connectIMAP(StringKeyValuePair creds) {
		String[] addressArgs = creds.getKey().split("@");
		
		if (addressArgs.length != 2) {
			System.err.println("Invalid email address: " + creds.getKey());
			return null;
		}
		IMAPDetails details = getByDomain(addressArgs[1]);
		
		if (Objects.isNull(details)) {
			System.out.println("Failed to find an IMAP email service for domain: " + addressArgs[1]);
			
			details = getByDomain("Mail.com");
			
			if (Objects.isNull(details)) {
				return null;
			}
			System.out.println("Falling back to Mail.com assuming this is the right one");
		}
		for (IMAPClient o : imapClients) {
			if (Objects.isNull(o.getInbox()) || !o.getInbox().getCredentials().getKey().equalsIgnoreCase(creds.getKey())) {
				continue;
			}
			IMAPClient client = new IMAPClient(o.getInbox());
			imapClients.add(client);
			return client;
		}
		IMAPInbox inbox = new IMAPInbox(details, creds);
		
		if (!inbox.open()) {
			System.err.println("Failed to open inbox " + inbox.getIMAPDetails().getName() + " for [" + creds.getKey() + ":" + creds.getValue() + "]");
			return null;
		}
		IMAPClient client = new IMAPClient(inbox);
		return client;
	}
	
	/**
	 * Retrieves the amount of clients using an IMAP inbox.
	 * 
	 * @param inbox The IMAP inbox.
	 * 
	 * @return The count.
	 */
	public int getClientsUsingIMAPInbox(IMAPInbox inbox) {
		int count = 0;
		
		for (IMAPClient o : imapClients) {
			if (o.getInbox() == inbox) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Removes a client.
	 * 
	 * @param client The client to remove.
	 */
	public void removeClient(IMAPClient client) {
		if (getClientsUsingIMAPInbox(client.getInbox()) <= 1) {
			client.getInbox().close();
		}
	}
	
	/**
	 * Builds a disposable email and retrieves the client.
	 * 
	 * @return The email.
	 */
	public TempMailClient connectTempMail() {
		TempMailInbox inbox = new TempMailInbox();
		
		if (!inbox.open()) {
			return null;
		}
		TempMailClient client = new TempMailClient(inbox);
		return client;
	}
	
	/**
	 * Builds a manual email inbox.
	 * 
	 * @param emailAddress The email address.
	 * 
	 * @return The email inbox.
	 */
	public ManualMailClient connectManual(String emailAddress) {
		ManualInbox inbox = new ManualInbox(new StringKeyValuePair(emailAddress, ""));
		
		if (!inbox.open()) {
			return null;
		}
		ManualMailClient client = new ManualMailClient(inbox);
		return client;
	}
	
	/**
	 * Generates and retrieves dot variations of an email address.
	 * 
	 * @param emailAddress The email address.
	 * 
	 * @return The generated dot variations.
	 */
	public List<String> generateDotVariations(String emailAddress) {
		List<String> variations = new ArrayList<>();
		
		String begin = emailAddress.substring(0, emailAddress.indexOf("@"));

		for (int i = 0; i < (int) Math.pow(2D, begin.length() - 1); i++) {
		    String dotted = makeDotted(emailAddress, i);

		    if (Objects.nonNull(dotted)) {
		    	variations.add(dotted);
		    }
		}
		return variations;
	}
	
	/**
     * Makes a dotted version of an email.
     * 
     * @param email The email to use.
     * 
     * @param amount The modification index.
     * 
     * @return The dotted variation.
     */
    private String makeDotted(String email, int amount) {
    	String mail = email.substring(0, email.indexOf("@"));
    	String ending = email.substring(email.indexOf("@"));

    	String tempString = mail;
    	int powerTwo = 1;
	
    	for (int i1 = 1; i1 < mail.length(); i1++) {
    		if ((amount / powerTwo) % 2 == 0) {
    			int position = i1;
    			char chars[] = tempString.toCharArray();
    			String resultString = "";
		
    			for (int i2 = 0; i2 < position; i2++) {
    				if (chars[i2] == '.') {
    					position++;
    				}
    				resultString = resultString + chars[i2];
    			}
    			resultString = resultString + ".";
		
    			for (int i2 = position; i2 < chars.length; i2++) {
    				resultString = resultString + chars[i2];
    			}
    			tempString = resultString;
    		}
    		powerTwo = powerTwo * 2;
    	}
    	return tempString + ending;
    }
	
	@Override
	protected IMAPDetails instantiateResource(JsonReader reader) {
		return new IMAPDetails(reader);
	}

	@Override
	protected String getStorageDirName() {
		return "mail" + File.separator + "imap_details";
	}
	
	/**
	 * Retrieves IMAP details by their name.
	 * 
	 * @param name The name.
	 * 
	 * @return The IMAP details.
	 */
	public IMAPDetails getByName(String name) {
		return getResources().stream()
				.filter(id -> id.getName().equalsIgnoreCase(name))
				.findFirst()
				.orElse(null);
	}
	
	/**
	 * Retrieves the IMAP details by a domain.
	 * 
	 * @param domain The domain.
	 * 
	 * @return The IMAP details.
	 */
	public IMAPDetails getByDomain(String domain) {
		for (IMAPDetails id : getResources()) {
			for (String od : id.getDomains()) {
				if (domain.toLowerCase().contains(od.toLowerCase())) {
					return id;
				}
			}
		}
		return null;
	}
	
	/**
	 * Retrieves the singleton instance.
	 * 
	 * @return The singleton instance.
	 */
	public static MailManager getSingleton() {
		if (Objects.isNull(singleton)) {
			singleton = (MailManager)new MailManager().load();
			singleton.loadDefaultServices();
		}
		return singleton;
	}

}
