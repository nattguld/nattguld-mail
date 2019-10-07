package com.nattguld.mail.client.connections.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nattguld.http.requests.impl.GetRequest;
import com.nattguld.http.response.RequestResponse;
import com.nattguld.mail.client.MailClient;
import com.nattguld.mail.client.connections.DisposableMailConnection;
import com.nattguld.mail.client.impl.AsdasdMailClient;

/**
 * 
 * @author randqm
 *
 */

public class AsdasdMailConnection extends DisposableMailConnection {

	
	/**
	 * Creates a new sute.jp mail.
	 */
	public AsdasdMailConnection(MailClient client) {
		super(client);
	}

	@Override
	protected List<String> extractLinks(String sender, String subject, String verifier) {
		RequestResponse rr = getClient().getHttpClient().dispatchRequest(new GetRequest("http://asdasd.nl/email/" + getClient().getCreds().getValue())
				.setCacheControl("max-age=0"));
		
		if (!rr.validate()) {
			getClient().getLogger().warning("Failed to refresh emails (" + rr.getCode() + ")");
			return null;
		}
		/*Element formIdEl = rr.getAsDoc().selectFirst("[name=form_id]");
		
		if (Objects.isNull(formIdEl)) {
			getClient().getLogger().error("Failed to extract form id element");
			return null;
		}*/
		Element messagesEl = rr.getAsDoc().getElementById("asdasd-email-view-main");
		
		if (Objects.isNull(messagesEl)) {
			getClient().getLogger().warning("Failed to extract messages container");
			return null;
		}
		Element messagesList = messagesEl.getElementsByTag("tbody").first();
		
		if (Objects.isNull(messagesList)) {
			getClient().getLogger().warning("Failed to extract messages list");
			return null;
		}
		String messageLink = null;
		
		for (Element el : messagesList.getElementsByTag("tr")) {
			Elements rows = el.getElementsByTag("td");
			
			if (rows.size() < 4) {
				getClient().getLogger().warning("Unexpected rows size (" + rows.size() + ")");
				continue;
			}
			String title = rows.get(1).text();
			@SuppressWarnings("unused")
			String fromEmail = rows.get(2).text();
			
			/*if (Objects.nonNull(sender) && !fromEl.text().toLowerCase().contains(sender.toLowerCase())) {
				continue;
			}*/
			if (!subject.toLowerCase().contains(title.toLowerCase())) {
				continue;
			}
			Element linkEl = rows.get(2).getElementsByTag("a").first();
			
			if (Objects.isNull(linkEl)) {
				getClient().getLogger().error("Failed to extract link element");
				continue;
			}
			messageLink = linkEl.attr("href");
			break;
		}
		if (Objects.isNull(messageLink)) {
			return null;
		}
		rr = getClient().getHttpClient().dispatchRequest(new GetRequest(messageLink));
		
		if (!rr.validate()) {
			getClient().getLogger().error("Failed to open message (" + rr.getCode() + ")");
			return null;
		}
		Document contentDoc = null;
		
		for (Element el : rr.getAsDoc().getElementsByTag("script")) {
			String scriptHtml = el.html();
			
			if (!scriptHtml.contains("Drupal.settings")) {
				continue;
			}
			String strip = scriptHtml.substring(scriptHtml.indexOf("{"), scriptHtml.lastIndexOf("}") + 1);
			
			JsonObject obj = new JsonParser().parse(strip).getAsJsonObject();
			JsonObject mailContent = obj.get("asdasdEmail").getAsJsonObject();
			contentDoc = Jsoup.parse(mailContent.get("emailContentHTML").getAsString());
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
	public AsdasdMailClient getClient() {
		return (AsdasdMailClient)super.getClient();
	}

}
