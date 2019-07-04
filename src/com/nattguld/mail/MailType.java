package com.nattguld.mail;

/**
 * 
 * @author randqm
 *
 */

public enum MailType {
	
	DISPOSABLE("Disposable"),
	GMAIL_DOT_TRICK("GMail Dot Trick"),
	IMPORTED("Imported"),
	MANUAL("Manual");
	
	
	/**
	 * The name.
	 */
	private final String name;
	
	
	/**
	 * Creates a new mail type.
	 * 
	 * @param name The name.
	 */
	private MailType(String name) {
		this.name = name;
	}
	
	/**
	 * Retrieves the name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
