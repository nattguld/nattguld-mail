package com.nattguld.mail;

/**
 * 
 * @author randqm
 *
 */

public enum DisposableMailType {
	
	TEMP_MAIL("Temp-mail (recommended)"),
	FAKE_TEMP_MAIL("FakeTempMail"),
	TIJDELIJKE_EMAIL("Tijdelijke email"),
	ASDASD("Asdasd"),
	SUTE("Sute");
	
	
	/**
	 * The name.
	 */
	private final String name;
	
	
	/**
	 * Creates a new disposable email type.
	 * 
	 * @param name The name.
	 */
	private DisposableMailType(String name) {
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
