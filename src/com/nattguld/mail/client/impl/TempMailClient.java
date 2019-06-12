package com.nattguld.mail.client.impl;

import java.util.List;
import java.util.Objects;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.nattguld.http.requests.impl.GetRequest;
import com.nattguld.http.response.RequestResponse;
import com.nattguld.mail.client.EmailClient;
import com.nattguld.mail.inbox.impl.TempMailInbox;

/**
 * 
 * @author randqm
 *
 */

public class TempMailClient extends EmailClient<TempMailInbox> {

	
	/**
	 * Creates a new temporary client.
	 * 
	 * @param inbox The temporary inbox the client is accessing.
	 */
	public TempMailClient(TempMailInbox inbox) {
		super(inbox);
	}
	
	@Override
	public List<String> extractLinks(String sender, String subject, String verifier) {
		if (Objects.isNull(getInbox().getSession())) {
			return null;
		}
		try {
			RequestResponse r = getInbox().getSession().dispatchRequest(new GetRequest("https://temp-mail.org/"));
		    	
			if (!r.validate(200)) {
				getInbox().getLogger().debug("Failed to load main page");
				return null;
			}
			String messageLink = null;
		    	
			Elements links = r.getAsDoc().getElementsByTag("a");
				
			if (Objects.nonNull(links) && !links.isEmpty()) {
				for (Element e : links) {
					String text = e.text().toLowerCase();

					if (Objects.nonNull(text) && text.contains(subject.toLowerCase())) {
						String href = e.attr("href");
			
						if (Objects.nonNull(href)) {
							messageLink = href;
							break;
						}
					}
				}	
			}
			if (Objects.isNull(messageLink)) {
				return null;
			}
			r = getInbox().getSession().dispatchRequest(new GetRequest(messageLink));
		
			if (!r.validate(200)) {
				getInbox().getLogger().error("Failed to open email");
				return null;
			}
			return TempMailInbox.extractLinksFromDoc(r.getAsDoc(), verifier);
			
		} catch (Exception ex) {
			getInbox().getLogger().exception(ex);
			getInbox().getLogger().error("Exception occurred while extracting links");
			return null;
		}
	}
	
	@Override
	public void dispose() {
		getInbox().close();
	}
	
	@Override
	public TempMailInbox getInbox() {
		return super.getInbox();
	}
	
}