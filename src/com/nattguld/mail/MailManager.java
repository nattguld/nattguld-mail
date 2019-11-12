package com.nattguld.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import com.nattguld.data.json.JsonReader;
import com.nattguld.data.json.JsonResourceManager;
import com.nattguld.mail.client.DisposableMailClient;
import com.nattguld.mail.client.MailClient;
import com.nattguld.mail.client.connections.DisposableMailConnection;
import com.nattguld.mail.client.connections.MailClientConnection;
import com.nattguld.mail.client.connections.impl.AsdasdMailConnection;
import com.nattguld.mail.client.connections.impl.FakeTempMailConnection;
import com.nattguld.mail.client.connections.impl.IMAPConnection;
import com.nattguld.mail.client.connections.impl.ManualConnection;
import com.nattguld.mail.client.connections.impl.SuteJPConnection;
import com.nattguld.mail.client.connections.impl.TempMailConnection;
import com.nattguld.mail.client.connections.impl.TijdelijkeEmailConnection;
import com.nattguld.mail.client.impl.AsdasdMailClient;
import com.nattguld.mail.client.impl.FakeTempMailClient;
import com.nattguld.mail.client.impl.IMAPClient;
import com.nattguld.mail.client.impl.ManualMailClient;
import com.nattguld.mail.client.impl.SuteJPClient;
import com.nattguld.mail.client.impl.TempMailClient;
import com.nattguld.mail.client.impl.TijdelijkeEmailClient;
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
				add(new IMAPDetails(defaultService, true, "Outlook.office365.com", 993, "Inbox", "Junk"
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
		if (mailType == MailType.DISPOSABLE || mailType == MailType.MANUAL) {
			System.err.println("Can not open " + mailType.getName() + " as IMAP connection");
			return null;
		}
		try {
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
			IMAPConnection connection = new IMAPConnection(client);
			client.addConnection(connection);
			return connection;
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Attempts to connect to a temp mail email client.
	 * 
	 * @return The connection.
	 */
	public DisposableMailConnection connectToDisposableClient(DisposableMailType det) {
		try {
			DisposableMailClient client = null;
			
			switch (det) {
			case ASDASD:
				client = new AsdasdMailClient();
				break;
				
			case TIJDELIJKE_EMAIL:
				client = new TijdelijkeEmailClient();
				break;
				
			case SUTE:
				client = new SuteJPClient();
				break;
				
			case TEMP_MAIL:
				client = new TempMailClient();
				break;
				
			case FAKE_TEMP_MAIL:
				client = new FakeTempMailClient();
				break;
				
			default:
				System.err.println(det.getName() + " not supported");
				return null;
			}
			if (!client.open()) {
				System.err.println("[" + det.getName() + "] Failed to open inbox");
				return null;
			}
			switch (det) {
			case ASDASD:
				return new AsdasdMailConnection(client);
				
			case TIJDELIJKE_EMAIL:
				return new TijdelijkeEmailConnection(client);
				
			case SUTE:
				return new SuteJPConnection(client);
				
			case TEMP_MAIL:
				return new TempMailConnection(client);
				
			case FAKE_TEMP_MAIL:
				return new FakeTempMailConnection(client);
				
			default:
				System.err.println(det.getName() + " has no known client instance");
				return null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Attempts to connect to a manual. email client.
	 * 
	 * @return The connection.
	 */
	public ManualConnection connectToManualClient() {
		return new ManualConnection(new ManualMailClient());
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
	public List<StringKeyValuePair> filterIMAPMails(String importPath) {
		List<StringKeyValuePair> valid = new ArrayList<>();
		
		for (StringKeyValuePair skvp : importMailCreds(importPath)) {
			MailClientConnection mcc = connectToIMAPClient(MailType.IMPORTED, skvp);
			
			if (Objects.isNull(mcc)) {
				continue;
			}
			valid.add(skvp);
		}
		return valid;
	}
	
	/**
	 * Retrieves imported mail credentials.
	 * 
	 * @param importPath The import path.
	 * 
	 * @return The mail credentials.
	 */
	public List<StringKeyValuePair> importMailCreds(String importPath) {
		List<StringKeyValuePair> emailCreds = new ArrayList<>();
		
		if (Objects.isNull(importPath)) {
			return new ArrayList<>();
		}
		for (String line : FileOperations.read(importPath)) {
			String format = line.trim();
			
			if (format.isEmpty() || !format.contains("@") || !format.contains(":")) {
				continue;
			}
			String username = format.substring(0, format.indexOf(":")).trim();
			String password = format.substring(format.indexOf(":") + 1, format.length()).trim();
			
			emailCreds.add(new StringKeyValuePair(username, password));
		}
		return emailCreds;
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
