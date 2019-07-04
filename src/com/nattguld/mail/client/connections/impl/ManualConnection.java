package com.nattguld.mail.client.connections.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.nattguld.mail.client.MailClient;
import com.nattguld.mail.client.connections.MailClientConnection;

/**
 * 
 * @author randqm
 *
 */

public class ManualConnection extends MailClientConnection {

	
	/**
	 * Creates a new manual email.
	 * 
	 * @param client The email client.
	 */
	public ManualConnection(MailClient client) {
		super(client);
	}
	
	@Override
	protected List<String> extractLinks(String sender, String subject, String verifier) {
		String confirmationLink = JOptionPane.showInputDialog(new JFrame()
				, "Enter the full confirmation link"
				, "Input Requested", JOptionPane.QUESTION_MESSAGE);
		
		List<String> confirmationLinks = new ArrayList<>();
		
		if (Objects.isNull(confirmationLink) || confirmationLink.isEmpty()) {
			confirmationLinks.add("invalid");
			return confirmationLinks;
		}
		confirmationLinks.add(confirmationLink);
		return confirmationLinks;
	}

}
