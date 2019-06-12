package com.nattguld.mail;

import java.io.File;
import java.util.List;
import java.util.Objects;

import com.nattguld.data.json.JsonReader;
import com.nattguld.data.json.JsonResourceManager;

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
	 * Retrieves a container by it's id.
	 * 
	 * @param id The id.
	 * 
	 * @return The container.
	 */
	public MailContainer getById(int id) {
		return getResources().stream()
				.filter(mc -> mc.getId() == id)
				.findFirst().orElse(null);
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
