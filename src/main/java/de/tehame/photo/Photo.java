package de.tehame.photo;

import de.tehame.event.Event;
import de.tehame.photo.meta.PhotoMetadaten;

public class Photo {

	private final String thumbnailUrl;
	private final String photoUrl;
	private final PhotoMetadaten metadaten;
	private final Event event;
	
	public Photo(String thumbnailUrl, String photoUrl, PhotoMetadaten metadaten, Event event) {
		super();
		this.thumbnailUrl = thumbnailUrl;
		this.photoUrl = photoUrl;
		this.metadaten = metadaten;
		this.event = event;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public PhotoMetadaten getMetadaten() {
		return metadaten;
	}

	public Event getEvent() {
		return event;
	}
}
