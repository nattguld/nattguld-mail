package com.nattguld.mail.client.impl;

import com.nattguld.data.json.JsonReader;
import com.nattguld.http.HttpClient;
import com.nattguld.http.content.EncType;
import com.nattguld.http.requests.impl.GetRequest;
import com.nattguld.http.response.RequestResponse;
import com.nattguld.mail.client.DisposableMailClient;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;
import com.nattguld.util.text.TextUtil;

/**
 * 
 * @author randqm
 *
 */

public class TijdelijkeEmailClient extends DisposableMailClient {
	
	
	/**
	 * Creates a new sute.jp client.
	 */
	public TijdelijkeEmailClient() {
		super(new StringKeyValuePair("tijdelijke-email.nl", "tijdelijke-email.nl"));
	}
	
	@Override
	protected StringKeyValuePair retrieveCreds(HttpClient c) {
		String username = TextUtil.generatePassword();
		
		RequestResponse rr = c.dispatchRequest(new GetRequest("https://tijdelijke-email.nl/inbox/backend/?inbox=" + username)
				.setResponseEncType(EncType.JSON));
		
		if (!rr.validate()) {
			getLogger().error("Failed to create inbox (" + rr.getCode() + ")");
			return null;
		}
		JsonReader respObj = rr.getJsonReader();
		String address = respObj.getAsString("address");
		return new StringKeyValuePair(address, username);
	}
	
}