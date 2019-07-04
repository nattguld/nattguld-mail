package com.nattguld.mail.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 
 * @author randqm
 *
 */

public class MailUtil {
	
	
	/**
	 * Generates and retrieves dot variations of an email address.
	 * 
	 * @param emailAddress The email address.
	 * 
	 * @return The generated dot variations.
	 */
	public static List<String> generateDotVariations(String emailAddress) {
		List<String> variations = new ArrayList<>();
		
		String begin = emailAddress.substring(0, emailAddress.indexOf("@"));

		for (int i = 0; i < (int) Math.pow(2D, begin.length() - 1); i++) {
		    String dotted = makeDotted(emailAddress, i);

		    if (Objects.nonNull(dotted)) {
		    	variations.add(dotted);
		    }
		}
		if (variations.isEmpty()) {
			System.err.println("Failed to generate dot variations for " + emailAddress);
		}
		return variations;
	}
	
	/**
     * Makes a dotted version of an email.
     * 
     * @param email The email to use.
     * 
     * @param amount The modification index.
     * 
     * @return The dotted variation.
     */
    private static String makeDotted(String email, int amount) {
    	String mail = email.substring(0, email.indexOf("@"));
    	String ending = email.substring(email.indexOf("@"));

    	String tempString = mail;
    	int powerTwo = 1;
	
    	for (int i1 = 1; i1 < mail.length(); i1++) {
    		if ((amount / powerTwo) % 2 == 0) {
    			int position = i1;
    			char chars[] = tempString.toCharArray();
    			String resultString = "";
		
    			for (int i2 = 0; i2 < position; i2++) {
    				if (chars[i2] == '.') {
    					position++;
    				}
    				resultString = resultString + chars[i2];
    			}
    			resultString = resultString + ".";
		
    			for (int i2 = position; i2 < chars.length; i2++) {
    				resultString = resultString + chars[i2];
    			}
    			tempString = resultString;
    		}
    		powerTwo = powerTwo * 2;
    	}
    	return tempString + ending;
    }

}
