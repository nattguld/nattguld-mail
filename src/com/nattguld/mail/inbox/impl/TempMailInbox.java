package com.nattguld.mail.inbox.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.nattguld.http.HttpClient;
import com.nattguld.http.requests.impl.GetRequest;
import com.nattguld.http.response.RequestResponse;
import com.nattguld.mail.inbox.EmailInbox;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;

/**
 * 
 * @author randqm
 *
 */

public class TempMailInbox extends EmailInbox {
	
	/**
	 * The client instance to use to manage the inbox.
	 */
	private HttpClient session;
	
	/**
	 * The mailbox reloads.
	 */
	private int reloads;
	
	
	/**
	 * Creates a new temp mail.
	 */
	public TempMailInbox() {
		super(null);
	}

	@Override
	public boolean open() {
		try {
			if (Objects.isNull(session)) {
				session = new HttpClient();
			}
			RequestResponse r = session.dispatchRequest(new GetRequest("https://temp-mail.org/"));
		    		
			if (!r.validate(200)) {
				if (reloads < 3) {
					reloads++;
					close();
					return open();
				}
				return false;
			}
			Element emailField = r.getAsDoc().getElementById("mail");
			String emailAddress = emailField.attr("value");
			
			setCredentials(new StringKeyValuePair(emailAddress, ""));
			reloads = 0;
			return true;
		    		
		} catch (Exception ex) {
			getLogger().exception(ex);
			getLogger().error("Exception occurred while building email");
			return false;
		}
	}
	
	@Override
	public void close() {
		if (Objects.nonNull(session)) {
			session.close();
			session = null;
		}
	}
	
	/**
	 * Retrieves the client session.
	 * 
	 * @return The client session.
	 */
	public HttpClient getSession() {
		return session;
	}
	
    /**
     * Extracts links by a given verifier from a html document.
     * 
     * @param doc The document.
     * 
     * @param verifier The verifier.
     * 
     * @return The list holding extracted links.
     */
    public static List<String> extractLinksFromDoc(Document doc, String verifier) {
    	List<String> extractedLinks = new ArrayList<>();
    	
    	Elements linkEls = doc.getElementsByTag("a");

    	if (Objects.nonNull(linkEls) && !linkEls.isEmpty()) {
    		for (Element e : linkEls) {
    			String text = e.text();
    			String href = e.attr("href");
			
    			if (Objects.nonNull(href) && href.contains(verifier)) {
    				extractedLinks.add(href);
    				continue;
    			}
    			if (Objects.nonNull(text) && text.contains(verifier)) {
    				if (Objects.nonNull(href)) {
    					extractedLinks.add(href);
    				}
    			}
    		}
    	}
    	return extractedLinks;
    }

}
