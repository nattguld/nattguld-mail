package com.nattguld.mail.client.impl;

import java.util.Objects;

import org.jsoup.nodes.Element;

import com.nattguld.http.HttpClient;
import com.nattguld.http.content.bodies.FormBody;
import com.nattguld.http.requests.impl.GetRequest;
import com.nattguld.http.requests.impl.PostRequest;
import com.nattguld.http.response.RequestResponse;
import com.nattguld.mail.client.DisposableMailClient;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;

/**
 * 
 * @author randqm
 *
 */

public class SuteJPClient extends DisposableMailClient {
	
	
	/**
	 * Creates a new sute.jp client.
	 */
	public SuteJPClient() {
		super(new StringKeyValuePair("sute.jp", "sute.jp"));
	}
	
	@Override
	protected StringKeyValuePair retrieveCreds(HttpClient c) {
		RequestResponse rr = c.dispatchRequest(new GetRequest("https://sute.jp/"));
		
		if (!rr.validate()) {
			getLogger().error("Failed to open main page (" + rr.getCode() + ")");
			return null;
		}
		Element authEl = rr.getAsDoc().selectFirst("[name=authenticity_token]");
		Element addressEl = rr.getAsDoc().getElementById("account_member_login");
		
		if (Objects.isNull(authEl)) {
			getLogger().error("Failed to extract authentication token");
			return null;
		}
		if (Objects.isNull(addressEl)) {
			getLogger().error("Failed to extract address");
			return null;
		}
		FormBody fb = new FormBody();
		fb.add("authenticity_token", authEl.val());
		fb.add("account_member[login]", addressEl.val());
		fb.add("commit", "メールアドレスを作成する");
		
		rr = c.dispatchRequest(new PostRequest("https://sute.jp/signup", fb));
		
		if (!rr.validate()) {
			getLogger().error("Failed to signup (" + rr.getCode() + ")");
			return null;
		}
		Element fullAddressEl = rr.getAsDoc().getElementById("myaddress");
		
		if (Objects.isNull(fullAddressEl)) {
			getLogger().error("Failed to extract full address");
			return null;
		}
		return new StringKeyValuePair(fullAddressEl.val(), "");
	}
	
}