package com.nattguld.mail;

import java.io.File;

import com.nattguld.data.json.JsonReader;
import com.nattguld.data.json.JsonResource;
import com.nattguld.data.json.JsonWriter;

/**
 * 
 * @author randqm
 *
 */

public class IMAPDetails extends JsonResource {
	
	/**
	 * The email service name.
	 */
	private final String name;
	
	/**
	 * Whether the service is a default one or not.
	 */
	private final boolean defaultService;
	
	/**
	 * The IMAP address.
	 */
	private final String imapAddress;
	
	/**
	 * The IMAP port.
	 */
	private final int imapPort;
	
	/**
	 * The inbox folder name.
	 */
	private final String inboxFolder;
	
	/**
	 * The spam folder name.
	 */
	private final String spamFolder;
	
	/**
	 * The email domains.
	 */
	private final String[] domains;
	
	
	/**
	 * Creates a new IMAP email details instance.
	 * 
	 * @param name The email service name.
	 * 
	 * @param defaultService Whether this is a default service or not.
	 * 
	 * @param imapAddress The IMAP address.
	 * 
	 * @param imapPort The IMAP port.
	 * 
	 * @param inboxFolder The inbox folder name.
	 * 
	 * @param spamFolder The spam folder name.
	 * 
	 * @param domains The email domains.
	 */
	public IMAPDetails(String name, boolean defaultService, String imapAddress, int imapPort
			, String inboxFolder, String spamFolder, String[] domains) {
		this.name = name;
		this.defaultService = defaultService;
		this.imapAddress = imapAddress;
		this.imapPort = imapPort;
		this.inboxFolder = inboxFolder;
		this.spamFolder = spamFolder;
		this.domains = domains;
	}
	
	/**
	 * Creates a new IMAP email details instance.
	 * @param reader
	 */
	public IMAPDetails(JsonReader reader) {
		super(reader);
		
		this.name = getReader().getAsString("name");
		this.defaultService = getReader().getAsBoolean("default_service");
		this.imapAddress = getReader().getAsString("imap_address");
		this.imapPort = getReader().getAsInt("imap_port");
		this.inboxFolder = getReader().getAsString("inbox_folder");
		this.spamFolder = getReader().getAsString("spam_folder");
		this.domains = (String[])getReader().getAsObject("domains", String[].class);
	}

	@Override
	public void write(JsonWriter writer) {
		writer.write("name", name);
		writer.write("default_service", defaultService);
		writer.write("imap_address", imapAddress);
		writer.write("imap_port", imapPort);
		writer.write("inbox_folder", inboxFolder);
		writer.write("spam_folder", spamFolder);
		writer.write("domains", domains);
	}
	
	@Override
	protected String getSaveDirPath() {
		return "mail" + File.separator + "imap_details";
	}

	@Override
	protected String getSaveFileName() {
		return name;
	}

	/**
	 * Retrieves the service name.
	 * 
	 * @return The service name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Retrieves whether this is a default service or not.
	 * 
	 * @return The result.
	 */
	public boolean isDefaultService() {
		return defaultService;
	}

	/**
	 * Retrieves the IMAP address.
	 * 
	 * @return The IMAP address.
	 */
	public String getImapAddress() {
		return imapAddress;
	}

	/**
	 * Retrieves the IMAP port.
	 * 
	 * @return The IMAP port.
	 */
	public int getImapPort() {
		return imapPort;
	}

	/**
	 * Retrieves the inbox folder name.
	 * 
	 * @return The inbox folder name.
	 */
	public String getInboxFolder() {
		return inboxFolder;
	}

	/**
	 * Retrieves the spam folder name.
	 * 
	 * @return The spam folder name.
	 */
	public String getSpamFolder() {
		return spamFolder;
	}
	
	/**
	 * Retrieves the domains.
	 * 
	 * @return The domains.
	 */
	public String[] getDomains() {
		return domains;
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
