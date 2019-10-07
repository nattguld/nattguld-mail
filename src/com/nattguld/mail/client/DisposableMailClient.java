package com.nattguld.mail.client;

import java.util.Objects;

import com.nattguld.http.HttpClient;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;

/**
 * 
 * @author randqm
 *
 */

public abstract class DisposableMailClient extends MailClient {

	/**
	 * The http client session.
	 */
	private HttpClient c;
	
	
	/**
	 * Creates a new disposable client.
	 */
	public DisposableMailClient(StringKeyValuePair creds) {
		super(creds);
	}
	
	@Override
	public boolean open() {
		this.c = new HttpClient();
		
		try {
			StringKeyValuePair skvp = retrieveCreds(c);
			
			if (Objects.isNull(skvp)) {
				getLogger().error("Failed to retrieve email credentials");
				return false;
			}
			setCreds(skvp);
			return true;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Retrieves the email credentials.
	 * 
	 * @param c The client session.
	 * 
	 * @return The credentials.
	 */
	protected abstract StringKeyValuePair retrieveCreds(HttpClient c);
	
	@Override
	public void close() {
		if (Objects.nonNull(c)) {
			c.close();
			c = null;
		}
	}
	
	/**
	 * Retrieves the http client session.
	 * 
	 * @return The http client session.
	 */
	public HttpClient getHttpClient() {
		return c;
	}
	
}
