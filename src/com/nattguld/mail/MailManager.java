package com.nattguld.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import com.nattguld.data.json.JsonReader;
import com.nattguld.data.json.JsonResourceManager;
import com.nattguld.mail.client.MailClient;
import com.nattguld.mail.client.connections.MailClientConnection;
import com.nattguld.mail.client.connections.impl.IMAPConnection;
import com.nattguld.mail.client.connections.impl.ManualConnection;
import com.nattguld.mail.client.connections.impl.TempMailConnection;
import com.nattguld.mail.client.impl.IMAPClient;
import com.nattguld.mail.client.impl.ManualMailClient;
import com.nattguld.mail.client.impl.TempMailClient;
import com.nattguld.util.files.FileOperations;
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
	 * A list holding the active email clients.
	 */
	private final List<MailClient> activeClients = new CopyOnWriteArrayList<>();
	
	
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
	 * Attempts to connect to an IMAP email client.
	 * 
	 * @param creds The email credentials.
	 * 
	 * @return The connection.
	 */
	public IMAPConnection connectToIMAPClient(MailType mailType, StringKeyValuePair creds) {
		return (IMAPConnection)connectToClient(mailType, creds);
	}
	
	/**
	 * Attempts to connect to a temp mail email client.
	 * 
	 * @return The connection.
	 */
	public TempMailConnection connectToTempMailClient() {
		return (TempMailConnection)connectToClient(MailType.DISPOSABLE, null);
	}
	
	/**
	 * Attempts to connect to a manual. email client.
	 * 
	 * @return The connection.
	 */
	public ManualConnection connectToManualClient() {
		return (ManualConnection)connectToClient(MailType.MANUAL, null);
	}
	
	/**
	 * Attempts to connect to an email client.
	 * 
	 * @param mailType The mail type.
	 * 
	 * @param creds The email credentials.
	 * 
	 * @return The email client connection.
	 */
	public MailClientConnection connectToClient(MailType mailType, StringKeyValuePair creds) {
		try {
			if (Objects.isNull(creds)) {
				if (mailType == MailType.DISPOSABLE) {
					creds = new StringKeyValuePair("temp-mail.org", "temp-mail.org");
					
				} else if (mailType == MailType.MANUAL) {
					creds = new StringKeyValuePair("Manual", "Manual");
					
				} else {
					System.err.println("[" + mailType.toString() + "] No email credentials provided!");
					return null;
				}
			}
			if (mailType == MailType.MANUAL) {
				ManualMailClient client = new ManualMailClient(creds);
				return new ManualConnection(client);
			}
			if (mailType == MailType.DISPOSABLE) {
				TempMailClient client = new TempMailClient(creds);
				
				if (!client.open()) {
					System.err.println("[" + mailType.toString() + "] Failed to open inbox");
					return null;
				}
				return new TempMailConnection(client);
			}
			String[] addressArgs = creds.getKey().split("@");
			
			if (addressArgs.length != 2) {
				System.err.println("[" + mailType.toString() + "] Invalid email address: " + creds.getKey());
				return null;
			}
			String loginEmailAddress = creds.getKey();
			
			if (mailType == MailType.GMAIL_DOT_TRICK) {
				loginEmailAddress = addressArgs[0].replace(".", "") + "@" + addressArgs[1];
			}
			IMAPDetails details = getByDomain(addressArgs[1]);
				
			if (Objects.isNull(details)) {
				System.err.println("[" + mailType.toString() + "] Failed to find an IMAP email service for domain: " + addressArgs[1]);
					
				details = getByDomain("Mail.com");
					
				if (Objects.isNull(details)) {
					return null;
				}
				System.out.println("Falling back to Mail.com assuming this is the right one");
			}
			MailClient client = getActiveEmailClientByEmailAddresss(loginEmailAddress);
			
			if (Objects.isNull(client)) {
				client = new IMAPClient(creds, details);
				
				if (!client.open()) {
					System.err.println("[" + mailType.toString() + "] Failed to open inbox [" + creds.getKey() + ":" + creds.getValue() + "]");
					return null;
				}
				activeClients.add(client);
			}
			MailClientConnection connection = mailType == MailType.DISPOSABLE ? new TempMailConnection(client) : new IMAPConnection(client);
			client.addConnection(connection);
			return connection;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Retrieves the active email clients using a given email address.
	 * 
	 * @param emailAddress The email address.
	 * 
	 * @return The email clients.
	 */
	protected MailClient getActiveEmailClientByEmailAddresss(String emailAddress) {
		for (MailClient client : activeClients) {
			if (!client.getCreds().getKey().equalsIgnoreCase(emailAddress)) {
				continue;
			}
			return client;
		}
		return null;
	}
	
	/**
	 * Removes a client.
	 * 
	 * @param client The client to remove.
	 */
	public void removeClient(MailClient client) {
		activeClients.remove(client);
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
	 * Filters IMAP emails by removing non-working ones.
	 * 
	 * @param importPath The import path.
	 * 
	 * @return The mail manager.
	 */
	public List<String> filterIMAPMails(String importPath) {
		List<String> valid = new ArrayList<>();
		
		for (String line : FileOperations.read(importPath)) {
			String format = line.trim();
			
			if (format.isEmpty()) {
				continue;
			}
			String[] args = format.split(":");
			
			if (Objects.isNull(args) || args.length != 2) {
				continue;
			}
			MailClientConnection mcc = connectToIMAPClient(MailType.IMPORTED, new StringKeyValuePair(args[0], args[1]));
			
			if (Objects.isNull(mcc)) {
				continue;
			}
			valid.add(format);
		}
		return valid;
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
