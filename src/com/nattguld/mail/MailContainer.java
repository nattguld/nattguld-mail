package com.nattguld.mail;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.nattguld.data.json.JsonReader;
import com.nattguld.data.json.JsonResource;
import com.nattguld.data.json.JsonWriter;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;

/**
 * 
 * @author randqm
 *
 */

public class MailContainer extends JsonResource {
	
	/**
	 * The container id.
	 */
	private final int id;
	
	/**
	 * The email credentials.
	 */
	private final Deque<StringKeyValuePair> emailCreds;
	
	
	/**
	 * Creates a new mail container.
	 */
	public MailContainer() {
		this.id = hashCode();
		this.emailCreds = new ArrayDeque<>();
	}
	
	/**
	 * Loads a mail container.
	 * 
	 * @param reader The json reader.
	 */
	public MailContainer(JsonReader reader) {
		super(reader);
		
		this.id = getReader().getAsInt("id");
		this.emailCreds = getReader().getAsDeque("email_credentials", new TypeToken<Deque<StringKeyValuePair>>() {}.getType(), new ArrayDeque<StringKeyValuePair>());
	}
	
	@Override
	protected void write(JsonWriter writer) {
		writer.write("id", id);
		writer.write("email_credentials", emailCreds);
	}
	
	@Override
	protected String getSaveDirPath() {
		return "mail" + File.separator + "containers";
	}

	@Override
	protected String getSaveFileName() {
		return Integer.toString(getId());
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
	 * Pulls the next email credentials from the container.
	 * 
	 * @return The email credentials.
	 */
	public StringKeyValuePair next() {
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
	
	/**
	 * Retrieves the id.
	 * 
	 * @return The id.
	 */
	public int getId() {
		return id;
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
