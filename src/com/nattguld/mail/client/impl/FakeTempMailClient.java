package com.nattguld.mail.client.impl;

import com.google.gson.JsonObject;
import com.nattguld.http.HttpClient;
import com.nattguld.http.content.EncType;
import com.nattguld.http.requests.impl.GetRequest;
import com.nattguld.http.response.RequestResponse;
import com.nattguld.mail.client.DisposableMailClient;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;
import com.nattguld.util.maths.Maths;

/**
 * 
 * @author randqm
 *
 */

public class FakeTempMailClient extends DisposableMailClient {
	
	/**
	 * The available domains.
	 */
	private static final String[] DOMAINS = new String[] {
			"iigmail.com", "eyandex.ru", "ashotmail.com", "zoutlook.com"
	};
	
	/**
	 * Creates a new sute.jp client.
	 */
	public FakeTempMailClient() {
		super(new StringKeyValuePair("faketempmail.com", "faketempmail.com"));
	}
	
	@Override
	protected StringKeyValuePair retrieveCreds(HttpClient c) {
		RequestResponse rr = c.dispatchRequest(new GetRequest("https://faketempmail.com/"));
		
		if (!rr.validate()) {
			getLogger().error("Failed to open main page (" + rr.getCode() + ")");
			return null;
		}
		rr = c.dispatchRequest(new GetRequest("https://faketempmail.com/ajax/randmail.php")
				.setXMLHttpRequest(true).setResponseEncType(EncType.ALL));
		
		if (!rr.validate()) {
			getLogger().error("Failed to request random email (" + rr.getCode() + ")");
			return null;
		}
		JsonObject respObj = rr.getAsJsonElement().getAsJsonObject();
		String username = respObj.get("url").getAsString();
		
		String emailAddress = username + "@" + getRandomDomain();
		
		return new StringKeyValuePair(emailAddress, username);
	}
	
	/**
	 * Retrieves a random domain.
	 * 
	 * @return The domain.
	 */
	protected String getRandomDomain() {
		return DOMAINS[Maths.random(DOMAINS.length)];
	}
	
}