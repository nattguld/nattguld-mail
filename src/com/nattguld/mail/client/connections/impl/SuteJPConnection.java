package com.nattguld.mail.client.connections.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jsoup.nodes.Element;

import com.nattguld.http.requests.impl.GetRequest;
import com.nattguld.http.response.RequestResponse;
import com.nattguld.mail.client.MailClient;
import com.nattguld.mail.client.connections.DisposableMailConnection;
import com.nattguld.mail.client.impl.SuteJPClient;

/**
 * 
 * @author randqm
 *
 */

public class SuteJPConnection extends DisposableMailConnection {

	
	/**
	 * Creates a new sute.jp mail.
	 */
	public SuteJPConnection(MailClient client) {
		super(client);
	}

	@Override
	protected List<String> extractLinks(String sender, String subject, String verifier) {
		RequestResponse rr = getClient().getHttpClient().dispatchRequest(new GetRequest("https://sute.jp/mails"));
		
		if (!rr.validate()) {
			getClient().getLogger().warning("Failed to refresh emails (" + rr.getCode() + ")");
			return null;
		}
		Element messagesEl = rr.getAsDoc().getElementById("messages");
		
		if (Objects.isNull(messagesEl)) {
			getClient().getLogger().error("Failed to extract messages container");
			return null;
		}
		String messageId = null;
		
		for (Element el : messagesEl.getElementsByClass("message")) {
			if (!el.hasAttr("data-id")) {
				continue;
			}
			String dataId = el.attr("data-id");
			
			Element subjectEl = el.getElementsByClass("subject").first();
			
			if (Objects.isNull(subjectEl)) {
				getClient().getLogger().warning("Failed to extract subject");
				continue;
			}
			Element fromEl = el.getElementsByClass("from").first();
			
			if (Objects.isNull(fromEl)) {
				getClient().getLogger().warning("Failed to extract sender");
				continue;
			}
			if (Objects.nonNull(sender) && !fromEl.text().toLowerCase().contains(sender.toLowerCase())) {
				continue;
			}
			if (!subject.toLowerCase().contains(subjectEl.text().toLowerCase())) {
				continue;
			}
			messageId = dataId;
			break;
		}
		if (Objects.isNull(messageId)) {
			return null;
		}
		rr = getClient().getHttpClient().dispatchRequest(new GetRequest("https://sute.jp/mails/" + messageId));
		
		if (!rr.validate()) {
			getClient().getLogger().error("Failed to open message (" + rr.getCode() + ")");
			return null;
		}
		Element threadsEl = rr.getAsDoc().getElementById("threads");
		
		if (Objects.isNull(threadsEl)) {
			getClient().getLogger().error("Failed to extract threads");
			return null;
		}
		Element messageEl = threadsEl.getElementById("message-" + messageId);
		
		if (Objects.isNull(messageEl)) {
			getClient().getLogger().error("Failed to extract message");
			return null;
		}
		Element bodyEl = messageEl.getElementsByClass("body").first();
		
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
	public SuteJPClient getClient() {
		return (SuteJPClient)super.getClient();
	}

}
