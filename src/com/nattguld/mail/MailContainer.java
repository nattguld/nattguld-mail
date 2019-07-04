package com.nattguld.mail;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import com.google.gson.reflect.TypeToken;
import com.nattguld.data.json.JsonReader;
import com.nattguld.data.json.JsonResource;
import com.nattguld.data.json.JsonWriter;
import com.nattguld.mail.client.connections.MailClientConnection;
import com.nattguld.mail.util.MailUtil;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;
import com.nattguld.util.maths.Maths;

/**
 * 
 * @author randqm
 *
 */

public class MailContainer extends JsonResource {
	
	/**
	 * The container id.
	 */
	private final String uuid;
	
	/**
	 * The mail type.
	 */
	private final MailType mailType;
	
	/**
	 * The email credentials.
	 */
	private final Deque<StringKeyValuePair> emailCreds;
	
	
	/**
	 * Creates a new mail container.
	 * 
	 * @param mailType The mail type.
	 */
	public MailContainer(MailType mailType) {
		this.uuid = Maths.getUniqueId();
		this.mailType = mailType;
		this.emailCreds = new ArrayDeque<>();
		
		if (mailType == MailType.DISPOSABLE || mailType == MailType.MANUAL) {
			emailCreds.add(new StringKeyValuePair("na", "na"));
		}
	}
	
	/**
	 * Loads a mail container.
	 * 
	 * @param reader The json reader.
	 */
	public MailContainer(JsonReader reader) {
		super(reader);
		
		this.uuid = getReader().getAsString("uuid");
		this.mailType = (MailType)getReader().getAsObject("mail_type", MailType.class, MailType.IMPORTED);
		this.emailCreds = getReader().getAsDeque("email_credentials", new TypeToken<Deque<StringKeyValuePair>>() {}.getType(), new ArrayDeque<StringKeyValuePair>());
	}
	
	@Override
	protected void write(JsonWriter writer) {
		writer.write("uuid", uuid);
		writer.write("mail_type", mailType);
		writer.write("email_credentials", emailCreds);
	}
	
	@Override
	protected String getSaveDirName() {
		return "mail" + File.separator + "containers";
	}

	@Override
	protected String getSaveFileName() {
		return getUUID();
	}
	
	/**
	 * Adds email credentials.
	 * 
	 * @param emailCreds The email credentials.
	 * 
	 * @return The email container.
	 */
	public MailContainer addEmailCreds(List<StringKeyValuePair> emailCreds) {
		this.emailCreds.addAll(emailCreds);
		
		save();
		return this;
	}
	
	/**
	 * Adds a new set of email credentials.
	 * 
	 * @param emailCreds The email credentials.
	 * 
	 * @return The mail container.
	 */
	public MailContainer addEmailCreds(StringKeyValuePair emailCreds) {
		this.emailCreds.add(emailCreds);
		
		save();
		return this;
	}
	
	/**
	 * Generates dot variations of the given email credentials to populate the container with.
	 * 
	 * @param emailCreds The email credentials.
	 * 
	 * @return The mail container.
	 */
	public MailContainer generateDotVariations(StringKeyValuePair emailCreds) {
		if (mailType != MailType.GMAIL_DOT_TRICK) {
			System.err.println("Can not generate dot variations with mail type " + mailType.getName());
			return this;
		}
		List<String> variations = MailUtil.generateDotVariations(emailCreds.getKey());
		List<StringKeyValuePair> skvps = new ArrayList<>();
		
		for (int i = (variations.size() - 1); i >= 0; i--) {
			skvps.add(new StringKeyValuePair(variations.get(i), emailCreds.getValue()));
		}
		return addEmailCreds(skvps);
	}
	
	/**
	 * Opens & retrieves the next email client connection.
	 * 
	 * @return The email connection.
	 */
	public MailClientConnection nextMailConnection() {
		switch (mailType) {
		case DISPOSABLE:
			return MailManager.getSingleton().connectToTempMailClient();
			
		case GMAIL_DOT_TRICK:
		case IMPORTED:
			StringKeyValuePair next = next();
			
			if (Objects.isNull(next)) {
				return null;
			}
			return MailManager.getSingleton().connectToIMAPClient(mailType, next);
			
		case MANUAL:
			return MailManager.getSingleton().connectToManualClient();
			
		default:
			System.err.println("No email client available for " + mailType.getName());
			return null;
		}
	}
	
	/**
	 * Pulls the next email credentials from the container.
	 * 
	 * @return The email credentials.
	 */
	protected StringKeyValuePair next() {
		if (isEmpty()) {
			return null;
		}
		StringKeyValuePair creds = emailCreds.poll();
		
		save();
		return creds;
	}
	
	/**
	 * Clears the container.
	 * 
	 * @return The mail container.
	 */
	public MailContainer clear() {
		emailCreds.clear();
		
		save();
		return this;
	}
	
	@Override
	public String getUUID() {
		return uuid;
	}
	
	/**
	 * Retrieves the email credentials.
	 * 
	 * @return The email credentials.
	 */
	public Deque<StringKeyValuePair> getEmailCreds() {
		return emailCreds;
	}
	
	/**
	 * Retrieves whether the container is empty or not.
	 * 
	 * @return The result.
	 */
	public boolean isEmpty() {
		return getEmailCreds().isEmpty();
	}
 
}
