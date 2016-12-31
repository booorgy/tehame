package de.tehame.photo;

import de.tehame.event.Event;
import de.tehame.photo.meta.PhotoMetadaten;

public class Photo {

	private final String url;
	private final PhotoMetadaten metadaten;
	private final Event event;
	
	public Photo(String url, PhotoMetadaten metadaten, Event event) {
		super();
		this.url = url;
		this.metadaten = metadaten;
		this.event = event;
	}

	public String getUrl() {
		return url;
	}

	public PhotoMetadaten getMetadaten() {
		return metadaten;
	}

	public Event getEvent() {
		return event;
	}
}
