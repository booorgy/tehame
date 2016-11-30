package de.tehame.event;

import java.util.HashMap;

public class Events {
	public HashMap<String, Event> events = new HashMap<>();
	
	public void speichereEvent(Event event) {
		this.events.put(event.getUuid(), event);
	}
	
	public Event sucheEvent(String uuid) {
		return this.events.get(uuid);
	}
}
