package com.nattguld.mail.client.impl;

import java.util.Objects;

import org.jsoup.nodes.Element;

import com.nattguld.http.HttpClient;
import com.nattguld.http.requests.impl.GetRequest;
import com.nattguld.http.response.RequestResponse;
import com.nattguld.mail.client.MailClient;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;

/**
 * 
 * @author randqm
 *
 */

public class TempMailClient extends MailClient {

	/**
	 * The http client session.
	 */
	private HttpClient c;
	
	
	/**
	 * Creates a new temporary client.
	 */
	public TempMailClient(StringKeyValuePair creds) {
		super(creds);
	}

	@Override
	public boolean open() {
		this.c = new HttpClient();
		
		RequestResponse rr = c.dispatchRequest(new GetRequest("https://temp-mail.org/"));
		
		if (!rr.validate()) {
			getLogger().error("Failed to open main page (" + rr.getCode() + ")");
			return false;
		}
		Element mailEl = rr.getAsDoc().getElementById("mail");
		
		if (Objects.isNull(mailEl)) {
			getLogger().error("Failed to extract mail element");
			return false;
		}
		setCreds(new StringKeyValuePair(mailEl.val(), ""));
		return true;
	}

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