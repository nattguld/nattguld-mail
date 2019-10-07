package com.nattguld.mail.client.impl;

import java.util.Objects;

import org.jsoup.nodes.Element;

import com.nattguld.http.HttpClient;
import com.nattguld.http.requests.impl.GetRequest;
import com.nattguld.http.response.RequestResponse;
import com.nattguld.mail.client.DisposableMailClient;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;

/**
 * 
 * @author randqm
 *
 */

public class TempMailClient extends DisposableMailClient {
	
	
	/**
	 * Creates a new temporary client.
	 */
	public TempMailClient() {
		super(new StringKeyValuePair("temp-mail.org", "temp-mail.org"));
	}
	
	@Override
	protected StringKeyValuePair retrieveCreds(HttpClient c) {
		RequestResponse rr = c.dispatchRequest(new GetRequest("https://temp-mail.org/"));
		
		if (!rr.validate()) {
			getLogger().error("Failed to open main page (" + rr.getCode() + ")");
			return null;
		}
		Element mailEl = rr.getAsDoc().getElementById("mail");
		
		if (Objects.isNull(mailEl)) {
			getLogger().error("Failed to extract mail element");
			return null;
		}
		return new StringKeyValuePair(mailEl.val(), "");
	}
	
}