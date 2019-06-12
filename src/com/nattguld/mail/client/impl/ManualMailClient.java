package com.nattguld.mail.client.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.nattguld.mail.client.EmailClient;
import com.nattguld.mail.inbox.impl.ManualInbox;

/**
 * 
 * @author randqm
 *
 */

public class ManualMailClient extends EmailClient<ManualInbox> {

	
	/**
	 * Creates a new manual client.
	 * 
	 * @param inbox The manual inbox the client is accessing.
	 */
	public ManualMailClient(ManualInbox inbox) {
		super(inbox);
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
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ManualInbox getInbox() {
		return super.getInbox();
	}
	
}