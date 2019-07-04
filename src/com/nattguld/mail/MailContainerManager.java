package com.nattguld.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.nattguld.data.json.JsonReader;
import com.nattguld.data.json.JsonResourceManager;
import com.nattguld.util.files.FileOperations;
import com.nattguld.util.generics.kvps.impl.StringKeyValuePair;

/**
 * 
 * @author randqm
 *
 */

public class MailContainerManager extends JsonResourceManager<MailContainer> {
	
	/**
	 * The singleton instance.
	 */
	private static MailContainerManager singleton;

	
	@Override
	protected MailContainer instantiateResource(JsonReader reader) {
		return new MailContainer(reader);
	}
	
	/**
	 * Parses an emails file.
	 * 
	 * @param f The file.
	 * 
	 * @return The list with email credentials.
	 */
	public List<StringKeyValuePair> parseFile(File f) {
		return parseFile(f.getAbsolutePath());
	}
	
	/**
	 * Parses an emails file.
	 * 
	 * @param filePath The file path.
	 * 
	 * @return The list with email credentials.
	 */
	public List<StringKeyValuePair> parseFile(String filePath) {
		List<StringKeyValuePair> creds = new ArrayList<>();
		
		List<String> lines = FileOperations.read(filePath);
		
		if (Objects.isNull(lines) || lines.isEmpty()) {
			return creds;
		}
		for (String line : lines) {
			String format = line.trim();
			
			if (format.isEmpty()) {
				continue;
			}
			String[] args = format.split(":");
			
			if (Objects.isNull(args) || args.length <= 0) {
				args = format.split(";");
				
				if (Objects.isNull(args) || args.length <= 0) {
					args = format.split(",");
					
					if (Objects.isNull(args) || args.length <= 0) {
						continue;
					}
				}
			}
			if (args.length != 2) {
				continue;
			}
			creds.add(new StringKeyValuePair(args[0].trim(), args[1].trim()));
		}
		return creds;
	}
	
	@Override
	public MailContainer getByUUID(String uuid) {
		return super.getByUUID(uuid);
	}

	@Override
	protected String getStorageDirName() {
		return "mail" + File.separator + "containers";
	}
	
	@Override
	public List<MailContainer> getResources() {
		return super.getResources();
	}
	
	/**
	 * Retrieves the singleton instance.
	 * 
	 * @return The singleton instance.
	 */
	public static MailContainerManager getSingleton() {
		if (Objects.isNull(singleton)) {
			singleton = (MailContainerManager)new MailContainerManager().load();
		}
		return singleton;
	}

}
