package com.nattguld.mail;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
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
	 * The disposable email type if any.
	 */
	private final DisposableMailType det;
	
	/**
	 * The email credentials.
	 */
	private final Deque<StringKeyValuePair> emailCreds;
	
	
	/**
	 * Creates a new mail container.
	 * 
	 * @param mailType The mail type.
	 * 
	 * @param det The disposable email type if any.
	 */
	public MailContainer(MailType mailType, DisposableMailType det) {
		this(mailType, det, new ArrayDeque<StringKeyValuePair>());
	}
	
	/**
	 * Creates a new mail container.
	 * 
	 * @param emailCreds The email credentials.
	 */
	public MailContainer(Deque<StringKeyValuePair> emailCreds) {
		this(MailType.IMPORTED, null, emailCreds);
	}
	
	/**
	 * Creates a new mail container.
	 * 
	 * @param mailType The mail type.
	 * 
	 * @param det The disposable email type if any.
	 * 
	 * @param emailCreds The email credentials.
	 */
	public MailContainer(MailType mailType, DisposableMailType det, Deque<StringKeyValuePair> emailCreds) {
		this.uuid = Maths.getUniqueId();
		this.mailType = mailType;
		this.det = det;
		this.emailCreds = emailCreds;
		
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
		this.det = (DisposableMailType)getReader().getAsObject("disposable_mail_type", DisposableMailType.class, null);
		this.emailCreds = getReader().getAsDeque("email_credentials", new TypeToken<Deque<StringKeyValuePair>>() {}.getType(), new ArrayDeque<StringKeyValuePair>());
	}
	
	@Override
	protected void write(JsonWriter writer) {
		writer.write("uuid", uuid);
		writer.write("mail_type", mailType);
		writer.write("disposable_mail_type", det);
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
	public MailContainer generateDotVariations(StringKeyValuePair skvp) {
		if (mailType != MailType.GMAIL_DOT_TRICK) {
			System.err.println("Can not generate dot variations with mail type " + mailType.getName());
			return this;
		}
		List<String> variations = MailUtil.generateDotVariations(skvp.getKey());
		List<StringKeyValuePair> skvps = new ArrayList<>();
		
		for (int i = (variations.size() - 1); i >= 0; i--) {
			skvps.add(new StringKeyValuePair(variations.get(i), skvp.getValue()));
		}
		emailCreds.addAll(skvps);
		return this;
	}
	
	/**
	 * Filters the email credentials using a blacklist.
	 * 
	 * @param blacklistedAddresses The blacklisted addresses.
	 * 
	 * @return The container.
	 */
	public MailContainer filter(List<String> blacklistedAddresses) {
		boolean changesMade = false;
		
		for (Iterator<StringKeyValuePair> it = emailCreds.iterator(); it.hasNext();) {
			String address = it.next().getKey();
			
			if (blacklistedAddresses.contains(address)) {
				it.remove();
				changesMade = true;
			}
		}
		if (changesMade) {
			save();
		}
		return this;
	}
	
	/**
	 * Opens & retrieves the next email client connection.
	 * 
	 * @return The email connection.
	 */
	public MailClientConnection nextMailConnection() {
		MailClientConnection conn = null;
		
		switch (mailType) {
		case DISPOSABLE:
			conn = MailManager.getSingleton().connectToDisposableClient(det);
			break;
			
		case GMAIL_DOT_TRICK:
		case IMPORTED:
			StringKeyValuePair next = next();
			
			if (Objects.isNull(next)) {
				return null;
			}
			conn = MailManager.getSingleton().connectToIMAPClient(mailType, next);
			break;
			
		case MANUAL:
			conn =  MailManager.getSingleton().connectToManualClient();
			break;
			
		default:
			System.err.println("No email client available for " + mailType.getName());
			return null;
		}
		if (Objects.isNull(conn)) {
			return nextMailConnection();
		}
		return conn;
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
		return (mailType == MailType.GMAIL_DOT_TRICK || mailType == MailType.IMPORTED) && getEmailCreds().isEmpty();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mailType.getName());
		
		if (Objects.nonNull(det)) {
			sb.append(" - " + det.getName());
		}
		sb.append(" (" + getUUID() + ")");
		return sb.toString();
	}
 
}
