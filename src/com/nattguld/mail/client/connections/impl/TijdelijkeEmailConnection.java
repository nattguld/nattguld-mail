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
import com.nattguld.http.requests.impl.GetRequest;
import com.nattguld.http.response.RequestResponse;
import com.nattguld.mail.client.MailClient;
import com.nattguld.mail.client.connections.DisposableMailConnection;
import com.nattguld.mail.client.impl.TijdelijkeEmailClient;

/**
 * 
 * @author randqm
 *
 */

public class TijdelijkeEmailConnection extends DisposableMailConnection {

	
	/**
	 * Creates a new sute.jp mail.
	 */
	public TijdelijkeEmailConnection(MailClient client) {
		super(client);
	}

	@Override
	protected List<String> extractLinks(String sender, String subject, String verifier) {
		RequestResponse rr = getClient().getHttpClient().dispatchRequest(new GetRequest("https://tijdelijke-email.nl/inbox/backend/?inbox=" + getClient().getCreds().getValue())
				.setResponseEncType(EncType.JSON));
		
		if (!rr.validate()) {
			getClient().getLogger().error("Failed to refresh emails (" + rr.getCode() + ")");
			return null;
		}
		JsonObject respObj = rr.getAsJsonElement().getAsJsonObject();
		JsonArray mailsArr = respObj.get("mails").getAsJsonArray();
		
		if (mailsArr.size() <= 0) {
			return null;
		}
		Document contentDoc = null;
		
		for (JsonElement el : mailsArr) {
			JsonObject mailEl = el.getAsJsonObject();
			
			String title = mailEl.get("subject").getAsString();
			String fromName = mailEl.get("fromName").getAsString();
			String fromAddress = mailEl.get("fromAddress").getAsString();
			String textHtml = mailEl.get("textHtml").getAsString();
			
			if (Objects.nonNull(sender) && (!fromName.toLowerCase().contains(sender.toLowerCase())
					&& !fromAddress.toLowerCase().contains(sender.toLowerCase()))) {
				continue;
			}
			if (!subject.toLowerCase().contains(title.toLowerCase())) {
				continue;
			}
			contentDoc = Jsoup.parse(textHtml);
			break;
		}
		if (Objects.isNull(contentDoc)) {
			return null;
		}
		List<String> links = new ArrayList<>();
		
		for (Element linkEl : contentDoc.getElementsByTag("a")) {
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
	public TijdelijkeEmailClient getClient() {
		return (TijdelijkeEmailClient)super.getClient();
	}

}
