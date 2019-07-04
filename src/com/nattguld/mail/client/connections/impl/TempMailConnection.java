package com.nattguld.mail.client.connections.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jsoup.nodes.Element;

import com.nattguld.http.requests.impl.GetRequest;
import com.nattguld.http.response.RequestResponse;
import com.nattguld.mail.client.MailClient;
import com.nattguld.mail.client.connections.MailClientConnection;
import com.nattguld.mail.client.impl.TempMailClient;

/**
 * 
 * @author randqm
 *
 */

public class TempMailConnection extends MailClientConnection {

	
	/**
	 * Creates a new temp mail.
	 */
	public TempMailConnection(MailClient client) {
		super(client);
	}

	@Override
	protected List<String> extractLinks(String sender, String subject, String verifier) {
		RequestResponse rr = getClient().getHttpClient().dispatchRequest(new GetRequest("https://temp-mail.org/en/option/refresh/"));
		
		if (!rr.validate()) {
			getClient().getLogger().warning("Failed to refresh emails (" + rr.getCode() + ")");
			return null;
		}
		Element contentEl = rr.getAsDoc().getElementsByClass("content").first();
		
		if (Objects.isNull(contentEl)) {
			getClient().getLogger().error("Failed to extract content element");
			return null;
		}
		Element inboxEl = contentEl.getElementsByClass("inbox-dataList").first();
		
		if (Objects.isNull(inboxEl)) {
			getClient().getLogger().error("Failed to extract data list");
			return null;
		}
		String messageLink = null;
		
		for (Element colBox : inboxEl.getElementsByClass("col-box")) {
			Element linkEl = colBox.getElementsByTag("a").first();
			
			if (Objects.isNull(linkEl)) {
				continue;
			}
			Element senderEl = linkEl.getElementsByClass("inboxSenderName").first();
			
			if (Objects.isNull(senderEl)) {
				continue;
			}
			String senderName = senderEl.text();
			String href = linkEl.attr("href");
			String title = linkEl.attr("title");
			
			if (Objects.nonNull(sender) && !senderName.toLowerCase().contains(sender.toLowerCase())) {
				continue;
			}
			if (!subject.toLowerCase().contains(title.toLowerCase())) {
				continue;
			}
			messageLink = href;
			break;
		}
		if (Objects.isNull(messageLink)) {
			return null;
		}
		rr = getClient().getHttpClient().dispatchRequest(new GetRequest(messageLink));
		
		if (!rr.validate()) {
			getClient().getLogger().warning("Failed to open message (" + rr.getCode() + ")");
			return null;
		}
		Element messageEl = rr.getAsDoc().getElementsByClass("inbox-data-content-intro").first();
		
		if (Objects.isNull(messageEl)) {
			getClient().getLogger().error("Failed to extract message content");
			return null;
		}
		Element bodyEl = messageEl.getElementsByClass("ltr").first();
		
		if (Objects.isNull(bodyEl)) {
			getClient().getLogger().error("Failed to extract message body");
			return null;
		}
		List<String> links = new ArrayList<>();
		
		for (Element linkEl : bodyEl.getElementsByTag("a")) {
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
	public TempMailClient getClient() {
		return (TempMailClient)super.getClient();
	}

}
