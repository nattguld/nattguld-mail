package com.nattguld.mail.client.connections.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nattguld.http.content.EncType;
import com.nattguld.http.content.bodies.FormBody;
import com.nattguld.http.requests.impl.GetRequest;
import com.nattguld.http.requests.impl.PostRequest;
import com.nattguld.http.response.RequestResponse;
import com.nattguld.mail.client.MailClient;
import com.nattguld.mail.client.connections.DisposableMailConnection;
import com.nattguld.mail.client.impl.FakeTempMailClient;

/**
 * 
 * @author randqm
 *
 */

public class FakeTempMailConnection extends DisposableMailConnection {

	
	/**
	 * Creates a new sute.jp mail.
	 */
	public FakeTempMailConnection(MailClient client) {
		super(client);
	}

	@Override
	protected List<String> extractLinks(String sender, String subject, String verifier) {
		FormBody fb = new FormBody();
		fb.add("usermail", getClient().getCreds().getKey());
		
		RequestResponse rr = getClient().getHttpClient().dispatchRequest(new PostRequest("https://faketempmail.com/ajax/getmails.php", fb)
				.setXMLHttpRequest(true).setResponseEncType(EncType.ALL));
		
		if (!rr.validate()) {
			getClient().getLogger().warning("Failed to refresh emails (" + rr.getCode() + ")");
			return null;
		}
		JsonObject respObj = rr.getAsJsonElement().getAsJsonObject();
		int count = respObj.get("count").getAsInt();
		
		if (count <= 0) {
			return null;
		}
		JsonArray mailsArr = respObj.get("maillist").getAsJsonArray();
		String mailId = null;
		
		for (JsonElement el : mailsArr) {
			JsonObject mailObj = el.getAsJsonObject();
			
			String id = mailObj.get("mailid").getAsString();
			String from = mailObj.get("from_mail").getAsString();
			String fromName = mailObj.get("fromName").getAsString();
			String title = mailObj.get("subject").getAsString();
			
			if (Objects.nonNull(sender) && (!from.toLowerCase().contains(sender.toLowerCase())
					&& !fromName.toLowerCase().contains(sender.toLowerCase()))) {
				continue;
			}
			if (!subject.toLowerCase().contains(title.toLowerCase())) {
				continue;
			}
			mailId = id;
			break;
		}
		if (Objects.isNull(mailId)) {
			return null;
		}
		fb = new FormBody();
		fb.add("usermail", getClient().getCreds().getKey());
		fb.add("mailid", mailId);
		
		rr = getClient().getHttpClient().dispatchRequest(new GetRequest("https://faketempmail.com/ajax/readmail.php"));
		
		if (!rr.validate()) {
			getClient().getLogger().error("Failed to open message (" + rr.getCode() + ")");
			return null;
		}
		JsonObject msgResp = rr.getAsJsonElement().getAsJsonObject();
		JsonObject mailDataObj = msgResp.get("maildata").getAsJsonObject();
		
		String msgHtml = mailDataObj.get("html").getAsString();
		
		Document doc = Jsoup.parse(msgHtml);
		
		List<String> links = new ArrayList<>();
		
		for (Element linkEl : doc.getElementsByTag("a")) {
			String text = linkEl.text();
			String href = linkEl.attr("href");
			
			if (href.contains(verifier)) {
				links.add(href);
				continue;
			}
			if (text.contains(verifier)) {
				links.add(href);
				continue;
			}
		}
		return links;
	}
	
	@Override
	public FakeTempMailClient getClient() {
		return (FakeTempMailClient)super.getClient();
	}

}
