package com.nattguld.mail.client.impl;

import com.nattguld.http.HttpClient;
import com.nattguld.http.content.bodies.FormBody;
import com.nattguld.http.requests.impl.GetRequest;
import com.nattguld.http.requests.impl.PostRequest;
import com.nattguld.http.response.RequestResponse;
import com.nattguld.mail.client.DisposableMailClient;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;
import com.nattguld.util.text.TextUtil;

/**
 * 
 * @author randqm
 *
 */

public class AsdasdMailClient extends DisposableMailClient {
	
	
	/**
	 * Creates a new sute.jp client.
	 */
	public AsdasdMailClient() {
		super(new StringKeyValuePair("asdasd.nl", "asdasd.nl"));
	}
	
	@Override
	protected StringKeyValuePair retrieveCreds(HttpClient c) {
		RequestResponse rr = c.dispatchRequest(new GetRequest("http://asdasd.nl/"));
		
		if (!rr.validate()) {
			getLogger().error("Failed to open main page (" + rr.getCode() + ")");
			return null;
		}
		String username = TextUtil.generatePassword();
		
		FormBody fb = new FormBody();
		fb.add("mailbox-form-email", username);
		
		rr = c.dispatchRequest(new PostRequest("http://asdasd.nl/", fb)
				.setCacheControl("max-age=0"));
		
		if (!rr.validate()) {
			getLogger().error("Failed to signup (" + rr.getCode() + ")");
			return null;
		}
		String emailAddress = username + "@" + "asdasd.nl";
		return new StringKeyValuePair(emailAddress, username);
	}
	
}